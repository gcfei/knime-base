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
 *   23 Apr 2020 (Timmo Waller-Ehrat, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.io.filehandling.table.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.commons.io.input.CountingInputStream;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.SwingWorkerWithContext;
import org.knime.filehandling.core.defaultnodesettings.SettingsModelFileChooser2;
import org.knime.filehandling.core.util.BomEncodingUtils;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 *
 * @author Timmo Waller-Ehrat, KNIME GmbH, Konstanz, Germany
 */
final class CSVFormatAutoDetectionSwingWorker extends SwingWorkerWithContext<CsvFormat, Integer> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CSVFormatAutoDetectionSwingWorker.class);

    private final JTextField m_colDelimiterField;

    private final JTextField m_rowDelimiterField;

    private final JTextField m_quoteField;

    private final JTextField m_quoteEscapeField;

    private final JTextField m_commentStartField;

    private final JProgressBar m_analyzeProgressBar;

    private final JPanel m_panel;

    private final SettingsModelFileChooser2 m_model;

    private final CSVTableReaderConfig m_csvReaderconfig;

    private BufferedReader m_reader;

    public CSVFormatAutoDetectionSwingWorker(final SettingsModelFileChooser2 model, final CSVTableReaderConfig config,
        final JTextField colDelimiterField, final JTextField rowDelimiterField, final JTextField quoteField,
        final JTextField quoteEscapeField, final JTextField commentStartField, final JProgressBar analyzeProgressBar,
        final JPanel panel) {
        m_colDelimiterField = colDelimiterField;
        m_rowDelimiterField = rowDelimiterField;
        m_quoteField = quoteField;
        m_quoteEscapeField = quoteEscapeField;
        m_commentStartField = commentStartField;
        m_analyzeProgressBar = analyzeProgressBar;
        m_panel = panel;
        m_model = model;
        m_csvReaderconfig = config;
    }

    @Override
    protected CsvFormat doInBackgroundWithContext() throws IOException {
        try (InputStream inputStream =
            new CountingInputStream(Files.newInputStream(Paths.get(m_model.getPathOrURL())))) {

            final String charSetName = m_csvReaderconfig.getCharSetName();
            final Charset charset = charSetName == null ? Charset.defaultCharset() : Charset.forName(charSetName);
            m_reader = BomEncodingUtils.createBufferedReader(inputStream, charset);
            if (m_csvReaderconfig.skipLines()) {
                skipLines(m_csvReaderconfig.getNumLinesToSkip());
            }
            final CsvParserSettings settings = m_csvReaderconfig.getSettings();
            final CsvFormat defaultFormat = new CsvFormat();
            defaultFormat.setComment('#');
            settings.setFormat(defaultFormat);
            settings.detectFormatAutomatically();

            final CsvParser parser = new CsvParser(settings);
            parser.beginParsing(m_reader);

            int counter = 0;
            while ((parser.parseNext()) != null) {
                publish(counter);
                counter++;
            }

            parser.stopParsing();

            return parser.getDetectedFormat();
        }
    }

    @Override
    protected void processWithContext(final List<Integer> chunks) {
        final int progress = chunks.get(chunks.size() - 1);
        m_analyzeProgressBar.setValue(progress);
        m_panel.revalidate();
        m_panel.repaint();
    }

    @Override
    protected void doneWithContext() {
        try {
            m_analyzeProgressBar.setVisible(false);
            updateTextFields(get());
        } catch (final ExecutionException e) {
            if (!(e.getCause() instanceof InterruptedException)) {
                LOGGER.debug(e.getMessage(), e);
            }
        } catch (InterruptedException | CancellationException ex) {
            // ignore
        }
    }

    private void updateTextFields(final CsvFormat format) {
        m_colDelimiterField.setText(format.getDelimiterString());
        m_rowDelimiterField.setText(EscapeUtils.escape(format.getLineSeparatorString()));
        m_quoteField.setText(Character.toString(format.getQuote()));
        m_quoteEscapeField.setText(Character.toString(format.getQuoteEscape()));
        m_commentStartField.setText(Character.toString(format.getComment()));
    }

    private void skipLines(final long n) throws IOException {
        for (int i = 0; i < n; i++) {
            m_reader.readLine();
        }
    }

}
