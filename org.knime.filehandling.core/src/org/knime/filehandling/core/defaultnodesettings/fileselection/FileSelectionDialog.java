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
 *   Apr 15, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.fileselection;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.util.FileSystemBrowser;
import org.knime.core.node.util.FileSystemBrowser.DialogType;
import org.knime.core.node.util.FileSystemBrowser.FileSelectionMode;
import org.knime.filehandling.core.util.GBCBuilder;

/**
 * A dialog for selecting files from a file system.</br>
 * Contains an editable combo box that allows typing paths manually as well as a browse button that opens a separate
 * dialog that allows browsing the underlying file system and selecting files, folders or both. For convenience, the
 * combo box stores files/folders that are selected via the browse button in its history.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class FileSelectionDialog {

    private final List<ChangeListener> m_listeners = new LinkedList<>();

    private final JPanel m_panel = new JPanel(new GridBagLayout());

    private final JButton m_browseButton = new JButton("Browse...");

    private final FileSelectionComboBox m_fileSelectionComboBox;

    private final DialogType m_dialogType;

    private final ChangeEvent m_event;

    private FileSelectionMode m_fileSelectionMode;

    private FileSystemBrowser m_fsBrowser;

    private String[] m_fileExtensions;

    /**
     * Constructor.
     *
     * @param historyID ID for storing a file history
     * @param historyLength the number of entries to store in the file history
     * @param fileSystemBrowser the initial {@link FileSystemBrowser}
     * @param dialogType the type of dialog (Open or Save)
     * @param fileSelectionMode the initial FileSelectionMode
     * @param fileExtensions the initially supported file extensions
     * @throws IllegalArgumentException if any argument is {@code null}
     */
    public FileSelectionDialog(final String historyID, final int historyLength,
        final FileSystemBrowser fileSystemBrowser, final DialogType dialogType,
        final FileSelectionMode fileSelectionMode, final String[] fileExtensions) {
        CheckUtils.checkArgument(historyLength > 0, "The historyLength must be positive.");
        m_fileSelectionComboBox = new FileSelectionComboBox(
            CheckUtils.checkArgumentNotNull(historyID, "The historyID must not be null."), historyLength);
        m_fsBrowser = CheckUtils.checkArgumentNotNull(fileSystemBrowser, "The fileSystemBrowser must not be null.");
        m_dialogType = CheckUtils.checkArgumentNotNull(dialogType, "The dialogType must not be null.");
        m_fileExtensions = CheckUtils.checkArgumentNotNull(fileExtensions, "The suffixes must not be null.");
        m_fileSelectionMode =
            CheckUtils.checkArgumentNotNull(fileSelectionMode, "The fileSelectionMode must not be null.");
        m_event = new ChangeEvent(this);
        m_browseButton.addActionListener(e -> clickBrowse());
        registerComboBoxListeners();
        layout();
    }

    private void registerComboBoxListeners() {
        m_fileSelectionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                notifyListeners();
            }
        });

        final Component editor = m_fileSelectionComboBox.getEditor().getEditorComponent();
        if (editor instanceof JTextComponent) {
            Document d = ((JTextComponent)editor).getDocument();
            d.addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(final DocumentEvent e) {
                    notifyListeners();
                }

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    notifyListeners();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    notifyListeners();
                }
            });
        }
    }

    /**
     * Allows to set the supported file extensions. No "." is prepended.</br>
     *
     * <b>Note:</b> the first value in <b>fileExtensions</b> is considered to be the default file extension that is
     * appended in case of save dialogs if no specific file extension is selected or specified.
     *
     * @param fileExtensions the supported file extensions (may be empty but never {@code null})
     */
    public void setFileExtensions(final String... fileExtensions) {
        m_fileExtensions =
            CheckUtils.checkArgumentNotNull(fileExtensions, "The fileExtensions must not be null.").clone();
    }

    /**
     * Adds a {@link ChangeListener}.
     *
     * @param listener to add
     */
    public void addListener(final ChangeListener listener) {
        m_listeners.add(listener);
    }

    /**
     * Removes a {@link ChangeListener}.
     *
     * @param listener to remove
     */
    public void removeListener(final ChangeListener listener) {
        m_listeners.remove(listener);
    }

    private void notifyListeners() {
        m_listeners.forEach(l -> l.stateChanged(m_event));
    }

    private void clickBrowse() {
        final String currentlySelected = m_fileSelectionComboBox.getSelectedItem();
        final String selectedInBrowser = m_fsBrowser.openDialogAndGetSelectedFileName(m_fileSelectionMode, m_dialogType,
            m_panel, getDefaultFileExtension(), currentlySelected, m_fileExtensions);
        // selectedInBrowser is null if browsing was canceled via the cancel button or closing the browser
        if (selectedInBrowser != null && !Objects.equals(currentlySelected, selectedInBrowser)) {
            m_fileSelectionComboBox.setSelectedItem(selectedInBrowser);
            m_fileSelectionComboBox.addCurrentSelectionToHistory();
        }
        notifyListeners();
    }

    private String getDefaultFileExtension() {
        return m_fileExtensions.length > 0 ? m_fileExtensions[0] : null;
    }

    /**
     * Sets a new {@link FileSystemBrowser} e.g. if the used file system changes.
     *
     * @param fileSystemBrowser the {@link FileSystemBrowser} this instance should use from now on
     */
    public void setFileSystemBrowser(final FileSystemBrowser fileSystemBrowser) {
        m_fsBrowser = CheckUtils.checkArgumentNotNull(fileSystemBrowser, "The fileSystemBrowser must not be null.");
    }

    /**
     * Sets the FileSelectionMode of the dialog.
     *
     * @param fileSelectionMode new fileSelectionMode
     */
    public void setFileSelectionMode(final FileSelectionMode fileSelectionMode) {
        m_fileSelectionMode =
            CheckUtils.checkArgumentNotNull(fileSelectionMode, "The fileSelectionMode must not be null.");
    }

    private void layout() {
        final GBCBuilder gbc = new GBCBuilder(new Insets(5, 5, 5, 5)).resetX().resetY();
        m_panel.add(m_fileSelectionComboBox, gbc.fillHorizontal().setWeightX(1.0).build());
        m_panel.add(m_browseButton, gbc.incX().setWeightX(0).build());
    }

    /**
     * Returns the {@link JPanel} controlled by this instance.
     *
     * @return the panel controlled by this instance
     */
    public JPanel getPanel() {
        return m_panel;
    }

    /**
     * Returns the currently selected file.
     *
     * @return the current selection
     */
    public String getSelected() {
        return m_fileSelectionComboBox.getSelectedItem();
    }

    /**
     * Allows to set the currently selected file.
     *
     * @param selected the file that should be selected
     */
    public void setSelected(final String selected) {
        m_fileSelectionComboBox
            .setSelectedItem(CheckUtils.checkArgumentNotNull(selected, "Selected must not be null."));
    }

    /**
     * Allows to enable and disable the browse button e.g. in the case that the custom url file system is used.
     *
     * @param enableBrowsing {@code true} if browsing should be enabled
     */
    public void setEnableBrowsing(final boolean enableBrowsing) {
        m_browseButton.setEnabled(enableBrowsing);
    }

    /**
     * Adds the currently selected file to the history.
     */
    public void addCurrentSelectionToHistory() {
        m_fileSelectionComboBox.addCurrentSelectionToHistory();
    }

}
