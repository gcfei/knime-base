/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Jan 24, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.node.table.reader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.filehandling.core.node.table.reader.config.MultiTableReadConfig;
import org.knime.filehandling.core.node.table.reader.config.ReaderSpecificConfig;
import org.knime.filehandling.core.node.table.reader.config.TableReadConfig;
import org.knime.filehandling.core.node.table.reader.read.Read;
import org.knime.filehandling.core.node.table.reader.read.ReadUtils;
import org.knime.filehandling.core.node.table.reader.spec.ReaderTableSpec;
import org.knime.filehandling.core.node.table.reader.util.IndividualTableReader;
import org.knime.filehandling.core.node.table.reader.util.MultiTableRead;
import org.knime.filehandling.core.node.table.reader.util.MultiTableReadFactory;
import org.knime.filehandling.core.node.table.reader.util.MultiTableUtils;

/**
 * Uses a {@link TableReader} to read tables from multiple paths, combines them according to the user settings and
 * performs type mapping.
 *
 * All I/O is performed in this class and the IndividualTableReader.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @param <T> the type used by the reader to identify individual data types
 * @param <V> the type of tokens the reader produces
 */
final class MultiTableReader<C extends ReaderSpecificConfig<C>, T, V> {

    private final TableReader<C, T, V> m_reader;

    private final MultiTableReadFactory<T, V> m_multiTableReadFactory;

    private MultiTableRead<V> m_currentMultiRead;

    /**
     * Constructor.
     *
     * @param reader the {@link TableReader} implementation to use for reading
     * @param multiTableReadFactory for creating MultiTableRead objects
     */
    MultiTableReader(final TableReader<C, T, V> reader, final MultiTableReadFactory<T, V> multiTableReadFactory) {
        m_reader = reader;
        m_multiTableReadFactory = multiTableReadFactory;
    }

    /**
     * Resets the spec read by {@code createSpec}, {@code fillRowOutput} or {@code readTable} i.e. a subsequent call to
     * {@code fillRowOutput} or {@code readTable} will read the spec again.
     */
    public void reset() {
        m_currentMultiRead = null;
    }

    /**
     * Creates the {@link DataTableSpec} corresponding to the tables stored in <b>paths</b> combined according to the
     * provided {@link MultiTableReadConfig config}.
     *
     * @param paths to read from
     * @param config for reading
     * @return the {@link DataTableSpec} of the merged table consisting of the tables stored in <b>paths</b>
     * @throws IOException if reading the specs from {@link Path paths} fails
     */
    public DataTableSpec createTableSpec(final List<Path> paths, final MultiTableReadConfig<C> config)
        throws IOException {
        return createMultiRead(paths, config).getOutputSpec();
    }

    private MultiTableRead<V> createMultiRead(final List<Path> paths, final MultiTableReadConfig<C> config)
        throws IOException {
        final Map<Path, ReaderTableSpec<T>> specs = new LinkedHashMap<>(paths.size());
        // TODO parallelize
        for (Path path : paths) {
            final ReaderTableSpec<T> spec = m_reader.readSpec(path, config.getTableReadConfig());
            specs.put(path, MultiTableUtils.assignNamesIfMissing(spec));
        }
        m_currentMultiRead = m_multiTableReadFactory.create(specs, config);
        return m_currentMultiRead;
    }

    /**
     * Reads a table from the provided {@link Path paths} according to the provided {@link MultiTableReadConfig config}.
     *
     * @param paths to read from
     * @param config for reading
     * @param exec for table creation and reporting progress
     * @return the read table
     * @throws Exception
     */
    public BufferedDataTable readTable(final List<Path> paths, final MultiTableReadConfig<C> config,
        final ExecutionContext exec) throws Exception {
        exec.setMessage("Creating table spec");
        final MultiTableRead<V> runConfig = getMultiRead(paths, config);
        final BufferedDataTableRowOutput output =
            new BufferedDataTableRowOutput(exec.createDataContainer(runConfig.getOutputSpec()));
        fillRowOutput(runConfig, paths, config, output, exec);
        return output.getDataTable();
    }

    /**
     * Fills the {@link RowOutput output} with the tables stored in <b>paths</b> using the provided
     * {@link MultiTableReadConfig config}. The {@link ExecutionContext} is required for type mapping and reporting
     * progress. Note: The {@link RowOutput output} must have a {@link DataTableSpec} compatible with the
     * {@link DataTableSpec} returned by createTableSpec(List, MultiTableReadConfig).
     *
     * @param paths to read from
     * @param config for reading
     * @param output the {@link RowOutput} to fill
     * @param exec needed by the mapping framework
     * @throws Exception
     */
    public void fillRowOutput(final List<Path> paths, final MultiTableReadConfig<C> config, final RowOutput output,
        final ExecutionContext exec) throws Exception {
        exec.setMessage("Creating table spec");
        final MultiTableRead<V> multiRead = getMultiRead(paths, config);
        fillRowOutput(multiRead, paths, config, output, exec);
    }

    private MultiTableRead<V> getMultiRead(final List<Path> paths, final MultiTableReadConfig<C> config)
        throws IOException {
        if (m_currentMultiRead == null || !m_currentMultiRead.isValidFor(paths)) {
            return createMultiRead(paths, config);
        } else {
            return m_currentMultiRead;
        }
    }

    private void fillRowOutput(final MultiTableRead<V> multiTableRead, final List<Path> paths,
        final MultiTableReadConfig<C> config, final RowOutput output, final ExecutionContext exec) throws Exception {
        exec.setMessage("Reading table");
        // TODO parallelize
        final FileStoreFactory fsFactory = FileStoreFactory.createFileStoreFactory(exec);
        for (Path path : paths) {
            final ExecutionMonitor progress = exec.createSubProgress(1.0 / paths.size());
            final TableReadConfig<C> pathSpecificConfig = createIndividualConfig(config.getTableReadConfig());
            final IndividualTableReader<V> reader =
                multiTableRead.createIndividualTableReader(path, pathSpecificConfig, fsFactory);
            try (Read<V> read =
                ReadUtils.decorateForReading(m_reader.read(path, pathSpecificConfig), pathSpecificConfig)) {
                reader.fillOutput(read, output, progress);
            }
            progress.setProgress(1.0);
        }
        output.close();
    }

    private TableReadConfig<C> createIndividualConfig(final TableReadConfig<C> generalConfig) {
        return generalConfig.copy();
    }

}
