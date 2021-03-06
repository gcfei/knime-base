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
 *   Mar 6, 2020 (bjoern): created
 */
package org.knime.filehandling.core.connections;

import java.nio.file.FileSystem;
import java.util.Optional;

import org.knime.filehandling.core.defaultnodesettings.FileSystemChoice.Choice;

public abstract class FSFileSystem<T extends FSPath> extends FileSystem {

    private final Choice m_fsChoice;

    private final Optional<String> m_fsSpecifier;

    private final String m_workingDirectory;

    public FSFileSystem(final Choice fsChoice, final Optional<String> fsSpecifier, final String workingDir) {
        m_fsChoice = fsChoice;
        m_fsSpecifier = fsSpecifier;
        m_workingDirectory = workingDir;
    }

    public Choice getFileSystemChoice() {
        return m_fsChoice;
    }

    public Optional<String> getFileSystemSpecifier() {
        return m_fsSpecifier;
    }

    /**
     * Each file system has a working directory, aka current directory. The working directory allows users of the file
     * system to supply relative paths to many of the provider methods, e.g. to open an input stream. The working
     * directory will be used to resolve such relative paths to absolute ones. The working directory of a file system
     * instance is final and does not change over the lifetime of the file system.
     *
     * @return the working directory, aka current directory.
     */
    public T getWorkingDirectory() {
        return getPath(m_workingDirectory);
    }

    public T getPath(final FSLocation fsLocation) {
        if (fsLocation.equals(FSLocation.NULL) || fsLocation.getFileSystemChoice() != m_fsChoice) {
            throw new IllegalArgumentException(
                String.format("Only FSLocations of type %s are allowed.", Choice.KNIME_FS));
        }

        return getPath(fsLocation.getPath());
    }

    @Override
    public abstract FSFileSystemProvider provider();

    @Override
    public abstract T getPath(String first, String... more);

}
