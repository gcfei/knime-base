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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.util.CheckUtils;

/**
 * TODO
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class FileSystemChooserDialog {

    private static final String NO_SPECIFIER = "NO_SPECIFIER";

    private final List<ChangeListener> m_listeners = new LinkedList<>();

    private final ChangeEvent m_changeEvent = new ChangeEvent(this);

    private final JPanel m_panel = new JPanel(new GridBagLayout());

    private final JPanel m_specifierPanel = new JPanel(new CardLayout());

    private final ToggleSelectionComboBox<FileSystemDialog> m_fileSystemComboBox;

    private final List<FileSystemDialog> m_fileSystemDialogs;

    FileSystemChooserDialog(final FileSystemDialog... fileSystemDialogs) {
        CheckUtils.checkArgumentNotNull(fileSystemDialogs, "The fileSystemDialogs must not be null.");
        CheckUtils.checkArgument(fileSystemDialogs.length > 0, "At least one fileSystemDialog must be provided.");
        m_fileSystemDialogs = Arrays.asList(fileSystemDialogs.clone());
        m_fileSystemComboBox = new ToggleSelectionComboBox<>(fileSystemDialogs);
        m_fileSystemComboBox.setRenderer(new FileSystemDialogListCellRenderer());
        setupTopLevelPanel();
        setupFileSystemSpecifierPanel();
        m_fileSystemComboBox.addActionListener(e -> handleFileSystemSelection());
    }

    private void handleFileSystemSelection() {
        final FileSystemDialog fsd = (FileSystemDialog)m_fileSystemComboBox.getSelectedItem();
        // TODO can fsd be null?
        final CardLayout cardLayout = (CardLayout)m_specifierPanel.getLayout();
        cardLayout.show(m_panel, fsd.hasSpecifierComponent() ? fsd.getFileSystemInfo().getIdentifier() : NO_SPECIFIER);
        notifyListeners();
    }

    public void addChangeListener(final ChangeListener listener) {
        m_listeners.add(listener);
    }

    private void notifyListeners() {
        m_listeners.forEach(l -> l.stateChanged(m_changeEvent));
    }

    private void setupFileSystemSpecifierPanel() {
        // default is no specifier
        m_specifierPanel.add(new JPanel(), NO_SPECIFIER);
        m_fileSystemDialogs.stream()//
            .filter(FileSystemDialog::hasSpecifierComponent)//
            .forEach(this::addSpecifierCard);
    }

    private void addSpecifierCard(final FileSystemDialog fsd) {
        fsd.addSpecifierChangeListener(e -> notifyListeners());
        final Component specifierComponent = fsd.getSpecifierComponent();
        m_specifierPanel.add(specifierComponent, fsd.getFileSystemInfo());
    }

    private void setupTopLevelPanel() {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(5, 5, 5, 5);
        m_panel.add(m_fileSystemComboBox, gbc);
        gbc.gridx++;
        m_panel.add(m_specifierPanel, gbc);
    }

    FileSystemInfo getFileSystemInfo() {
        // TODO can this be null? And what then?
        final FileSystemDialog fsd = getSelectedFileSystem();
        return fsd.getFileSystemInfo();
    }

    private FileSystemDialog getSelectedFileSystem() {
        return (FileSystemDialog)m_fileSystemComboBox.getSelectedItem();
    }

    void setFileSystemInfo(final FileSystemInfo fileSystemInfo) {
        final String id =
            CheckUtils.checkArgumentNotNull(fileSystemInfo, "The fileSystemInfo must not be null.").getIdentifier();
        final FileSystemDialog newlySelected = m_fileSystemDialogs.stream()//
            .filter(d -> d.getFileSystemInfo().getIdentifier().equals(id))//
            .findFirst()//
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Unknown file system identifier '%s' encountered.", id)));
        // TODO check if this already triggers a change event (we might want to only trigger the event after we are completely done with updating)
        m_fileSystemComboBox.setSelectedItem(newlySelected);
        newlySelected.update(fileSystemInfo);
    }

    void setSelectable(final boolean selectable) {
        m_fileSystemComboBox.setSelectable(selectable);
    }

    void setTooltip(final String tooltip) {
        m_fileSystemComboBox.setToolTipText(tooltip);
    }

    private static class FileSystemDialogListCellRenderer implements ListCellRenderer<FileSystemDialog> {

        private final DefaultListCellRenderer m_defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(final JList<? extends FileSystemDialog> list,
            final FileSystemDialog value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            // The method returns the renderer itself
            m_defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value.isValid()) {
                m_defaultRenderer.setForeground(Color.LIGHT_GRAY);
            } else {
                m_defaultRenderer.setForeground(Color.BLACK);
            }
            return null;
        }

    }

}
