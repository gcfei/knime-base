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
 *   Apr 14, 2020 (Simon Schmid, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.revise;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang3.ArrayUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
public final class FilterModeDialogComponent extends DialogComponent {

    /** Panel containing filtering options */
    private final FilterOptionsPanel m_filterDialogPanel = new FilterOptionsPanel();

    /** Button to open the dialog that contains options for file filtering */
    private final JButton m_filterOptionsButton = new JButton("Filter options");

    private final JCheckBox m_includeSubfoldersCheckBox = new JCheckBox("Include subfolders");

    private final DialogComponentButtonGroup m_filterModeButtonGroup;

    private final SettingsModelString m_filterModeSettingsModel;

    private final JPanel m_filterModePanel;

    private final JPanel m_filterPanel;

    private final FilterMode[] m_filterModes;

    /**
     * @param model the settings model
     * @param filterModes filter modes that will be available and be arranged according the order (first one is
     *            leftmost)
     *
     * @throws IllegalArgumentException if the list of filter modes contains duplicates
     */
    public FilterModeDialogComponent(final FilterModeSettingsModel model, final FilterMode... filterModes) {
        super(model);
        m_filterModes = filterModes;
        // does not need a meaningful config name since it will never be saved
        m_filterModeSettingsModel = new SettingsModelString("dummy", model.getFilterMode().name());
        m_filterModeButtonGroup = new DialogComponentButtonGroup(m_filterModeSettingsModel, null, false, filterModes);
        m_filterOptionsButton.addActionListener(e -> showFileFilterConfigurationDialog());

        m_filterModePanel = createFilterModePanel();
        m_filterPanel = createFilterPanel();

        m_filterModeSettingsModel.addChangeListener(l -> {
            final FilterMode filterMode = FilterMode.valueOf(m_filterModeSettingsModel.getStringValue());
            m_filterDialogPanel.visibleComponents(filterMode);
            model.setFilterMode(filterMode);
        });
        m_includeSubfoldersCheckBox
            .addActionListener(l -> model.setIncludeSubfolders(m_includeSubfoldersCheckBox.isSelected()));

        initComponent();
    }

    private void initComponent() {
        getComponentPanel().setLayout(new GridBagLayout());
        final GridBagConstraints gbc = createAndInitGBC();
        gbc.weightx = 1;
        getComponentPanel().add(m_filterModePanel, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        getComponentPanel().add(m_filterPanel, gbc);
    }

    private JPanel createFilterModePanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = createAndInitGBC();
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(m_filterModeButtonGroup.getComponentPanel(), gbc);
        return panel;
    }

    private JPanel createFilterPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = createAndInitGBC();
        panel.add(m_filterOptionsButton, gbc);
        gbc.gridx++;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(m_includeSubfoldersCheckBox, gbc);
        return panel;
    }

    private static GridBagConstraints createAndInitGBC() {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        return gbc;
    }

    /**
     * Enables or disables the specified filter mode button. If the selected button is disabled, the first enabled
     * button starting from left will be selected instead. Returns the selected {@link FilterMode} after
     * enabling/disabling.
     *
     * @param filterMode the filter mode of the button to enable or disable
     * @param enabled if the button should be enabled or not
     * @return the selected filter mode after the call of this function
     */
    public FilterMode setEnabledFilterModeButton(final FilterMode filterMode, final boolean enabled) {
        if (!ArrayUtils.contains(m_filterModes, filterMode)) {
            return FilterMode.valueOf(m_filterModeSettingsModel.getStringValue());
        }
        final JRadioButton button = (JRadioButton)m_filterModeButtonGroup.getButton(filterMode.name());

        // if the button should be disabled and was the selected one, select a different one
        if (!enabled && button.isEnabled() && button.isSelected()) {
            for (final FilterMode fm : m_filterModes) {
                if (fm == filterMode) {
                    continue;
                }
                final AbstractButton buttonToSelect = m_filterModeButtonGroup.getButton(fm.name());
                // select the button only if it is not disabled
                if (buttonToSelect.isEnabled()) {
                    // button will be selected when we update the settings model
                    m_filterModeSettingsModel.setStringValue(fm.name());
                    button.setEnabled(false);
                    return fm;
                }
            }
        }
        button.setEnabled(enabled);
        return FilterMode.valueOf(m_filterModeSettingsModel.getStringValue());
    }

    /**
     * Returns if the specified filter mode button is enabled.
     *
     * @param filterMode the filter mode of the button to check
     * @return <code>true</code> if the button is enabled, otherwise <code>false</code>
     */
    public boolean isFilterModeEnabled(final FilterMode filterMode) {
        return m_filterModeButtonGroup.getButton(filterMode.name()).isEnabled();
    }

    /**
     * Returns the panel that contains the button group for selecting the filter mode.
     *
     * @return the panel containing filter mode button group
     */
    public JPanel getSelectionModePanel() {
        return m_filterModePanel;
    }

    /**
     * Returns the panel that contains the the button to open the dialog with filter options and the check box that
     * enables/disables subfolders inclusion.
     *
     * @return the panel containing filter options button and include subfolders check box
     */
    public JPanel getFilterConfigPanel() {
        return m_filterPanel;
    }

    /** Method called if filter options button is clicked */
    private void showFileFilterConfigurationDialog() {

        final Container c = getComponentPanel().getParent();
        Frame parentFrame = null;
        Container parent = getComponentPanel();
        while (parent != null) {
            if (parent instanceof Frame) {
                parentFrame = (Frame)parent;
                break;
            }
            parent = parent.getParent();
        }

        final FilterOptionsDialog filterDialog = new FilterOptionsDialog(parentFrame, m_filterDialogPanel);
        filterDialog.setLocationRelativeTo(c); // TODO
        filterDialog.setVisible(true);

        if (filterDialog.getResultStatus() == JOptionPane.OK_OPTION) {
            // update the settings model
            ((FilterModeSettingsModel)getModel())
                .setFilterConfigSettings(m_filterDialogPanel.getFilterConfigSettings());
        } else {
            // overwrite the values in the filter options panel with the saved ones
            m_filterDialogPanel
                .setFilterConfigSettings(((FilterModeSettingsModel)getModel()).getFilterOptionsSettings());
        }
    }

    /**
     * Enumeration of modes for file and folder filtering.
     *
     * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
     */
    public enum FilterMode implements ButtonGroupEnumInterface {
            /** Only one file */
            FILE("File"),
            /** Only one folder */
            FOLDER("Folder"),
            /** Several files in a folder */
            FILES_IN_FOLDERS("Files in folder"),
            /** Multiple folders */
            FOLDERS("Folders"),
            /** Multiple files and folders */
            FILES_AND_FOLDERS("Files and folders");

        private final String m_label;

        private FilterMode(final String label) {
            m_label = label;
        }

        @Override
        public String getText() {
            return m_label;
        }

        @Override
        public String getActionCommand() {
            return name();
        }

        @Override
        public String getToolTip() {
            return null;
        }

        @Override
        public boolean isDefault() {
            return this == FILE;
        }
    }

    @Override
    protected void updateComponent() {
        final FilterModeSettingsModel model = (FilterModeSettingsModel)getModel();
        m_filterModeSettingsModel.setStringValue(model.getFilterMode().name());
        m_includeSubfoldersCheckBox.setSelected(model.isIncludeSubfolders());
        m_filterDialogPanel.setFilterConfigSettings(model.getFilterOptionsSettings());
        m_filterDialogPanel.visibleComponents(FilterMode.valueOf(m_filterModeSettingsModel.getStringValue()));
        setEnabledComponents(model.isEnabled());
    }

    @Override
    protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        // nothing to do
    }

    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs) throws NotConfigurableException {
        // nothing to do
    }

    @Override
    protected void setEnabledComponents(final boolean enabled) {
        m_filterModePanel.setEnabled(enabled); // TODO does panel disable its components?
        m_filterModeButtonGroup.getModel().setEnabled(enabled);
        m_filterOptionsButton.setEnabled(enabled);
        m_includeSubfoldersCheckBox.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(final String text) {
        // nothing to do
    }
}
