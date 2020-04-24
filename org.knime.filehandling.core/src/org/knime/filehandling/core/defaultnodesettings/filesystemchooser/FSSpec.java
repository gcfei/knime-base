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
 *   Apr 22, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.filesystemchooser;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.knime.core.node.util.CheckUtils;
import org.knime.filehandling.core.defaultnodesettings.FileSystemChoice;

/**
 * Represents a file system in the {@link FileSystemChooserDialog}.</br>
 * TODO figure out if we can replace it with FileSystemChoice or something similar
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class FSSpec {

    private final String m_identifier;

    private final String m_specifier;

    private final int m_hashCode;

    FSSpec(final String identifier, final String specifier) {
        m_identifier = CheckUtils.checkArgumentNotNull(identifier, "The fileSystemIdentifier must not be null.");
        m_specifier = specifier;
        m_hashCode = new HashCodeBuilder().append(m_identifier).append(m_specifier).toHashCode();
    }

    FSSpec(final String identifier) {
        this(identifier, null);
    }

    String getIdentifier() {
        return m_identifier;
    }

    Optional<String> getSpecifier() {
        return Optional.ofNullable(m_specifier);
    }

    FileSystemChoice toFileSystemChoice() {
        return FileSystemChoice.getChoiceFromId(m_identifier);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(m_identifier);
        if (m_specifier != null) {
            sb.append("; ").append(m_specifier);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof FSSpec) {
            final FSSpec other = (FSSpec)obj;
            return Objects.equals(m_identifier, other.m_identifier) && Objects.equals(m_specifier, other.m_specifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return m_hashCode;
    }

}
