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
 *   Apr 23, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.filesystemchooser;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.filehandling.core.defaultnodesettings.FileSystemChoice;
import org.knime.filehandling.core.defaultnodesettings.KNIMEConnection;
import org.knime.filehandling.core.util.MountPointFileSystemAccessService;

/**
 * FileSystemDialog for the Mountpoint filesystem.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class MountpointFileSystemDialog extends AbstractFileSystemDialog {

    private static final String ID = FileSystemChoice.getKnimeMountpointChoice().getId();

    private final JComboBox<KNIMEConnection> m_mountpoints = new JComboBox<>();

    private final ChangeEvent m_changeEvent = new ChangeEvent(this);

    MountpointFileSystemDialog() {
        super(ID);
        m_mountpoints.setRenderer(new KNIMEConnectionRenderer());
        updateMountpoints();
        m_mountpoints.setSelectedIndex(0);
    }

    private void updateMountpoints() {
        m_mountpoints.removeAllItems();
        MountPointFileSystemAccessService.instance().getAllMountedIDs().stream()
            .forEach(id -> m_mountpoints.addItem(KNIMEConnection.getOrCreateMountpointAbsoluteConnection(id)));
    }

    @Override
    public Component getSpecifierComponent() {
        return m_mountpoints;
    }

    @Override
    public boolean hasSpecifierComponent() {
        return true;
    }

    @Override
    public FSSpec getFSSpec() {
        return new FSSpec(ID, getSelected().getId());
    }

    private void setSelected(final String mountpoint) {
        final ComboBoxModel<KNIMEConnection> model = m_mountpoints.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            final KNIMEConnection connection = model.getElementAt(i);
            if (mountpoint.equals(connection.getId())) {
                model.setSelectedItem(connection);
                break;
            }
        }
        // TODO what happens if the stored connection is no longer available?!
    }

    @Override
    protected void updateSpecifier(final FSSpec fileSystemInfo) {
        // TODO we might want to disable events during updating
        updateMountpoints();
        setSelected(fileSystemInfo.getSpecifier()
            .orElseThrow(() -> new IllegalArgumentException("The mountpoint specifier must not be null.")));
    }

    @Override
    public void addSpecifierChangeListener(final ChangeListener listener) {
        m_mountpoints.addActionListener(e -> listener.stateChanged(m_changeEvent));
    }

    private KNIMEConnection getSelected() {
        return (KNIMEConnection)m_mountpoints.getSelectedItem();
    }

    @Override
    public boolean isValid() {
        // TODO not sure if this is the right check
        return getSelected().isConnected();
    }

    private static class KNIMEConnectionRenderer implements ListCellRenderer<KNIMEConnection> {

        private final DefaultListCellRenderer m_defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(final JList<? extends KNIMEConnection> list,
            final KNIMEConnection value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            // TODO figure out if we even have to call this, in the old implementation the super method was never called
            // DefaultListCellRenderer returns itself when getListCellRenderer is called
            m_defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            m_defaultRenderer
                .setForeground(KNIMEConnectionRenderer.getForegroundColor(value, list.getParent().getForeground()));
            if (value != null) {
                m_defaultRenderer.setText(value.getId());
            }

            if (!isSelected) {
                m_defaultRenderer.setBackground(list.getBackground());
            } else {
                m_defaultRenderer.setBackground(list.getSelectionBackground());
                m_defaultRenderer.setForeground(list.getSelectionForeground());
            }
            return m_defaultRenderer;
        }

        /**
         * Returns the foreground color for the given KNIMEConnection
         *
         * @param connection the KNIMEConnection
         * @param defaultColor the default color
         * @return the foreground color for the given KNIMEConnection
         */
        private static Color getForegroundColor(final KNIMEConnection connection, final Color defaultColor) {
            if (connection != null && !connection.isValid()) {
                return Color.RED;
            } else if (connection != null && !connection.isConnected()) {
                return Color.GRAY;
            } else {
                return defaultColor;
            }
        }

    }

}
