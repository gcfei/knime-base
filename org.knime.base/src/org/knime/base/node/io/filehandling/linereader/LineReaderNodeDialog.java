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
 *   14.08.2019 (Mareike Hoeger, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.io.filehandling.linereader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.filehandling.core.defaultnodesettings.DialogComponentFileChooser2;
import org.knime.filehandling.core.defaultnodesettings.SettingsModelFileChooser2;

/**
 *
 * @author Mareike Hoeger, KNIME GmbH, Konstanz, Germany
 */
final class LineReaderNodeDialog extends NodeDialogPane {

    private static final int COMP_WIDTH = 10;

    private final LineReaderConfig m_config = new LineReaderConfig();

    private final DialogComponentFileChooser2 m_filePanel;

    private final DialogComponentString m_columnHeaderField;

    private final DialogComponentString m_rowHeadPrefixField;

    private final DialogComponentBoolean m_limitRowCountChecker;

    private final DialogComponentBoolean m_skipEmptyLinesChecker;

    private final DialogComponentNumber m_limitRowCountSpinner;

    private final DialogComponentString m_regexField;

    private final DialogComponentButtonGroup m_customColumnSelect;

    private final DialogComponentBoolean m_useRegexButton;

    private final JLabel m_encodingLabel;

    private final JComboBox<String> m_encodingSelection;

    private final JLabel m_encodingWarning;

    /** Create new dialog, init layout. */
    LineReaderNodeDialog() {
    	final FlowVariableModel fvm = createFlowVariableModel(
                new String[]{m_config.getFileChooserModel().getConfigName(), SettingsModelFileChooser2.PATH_OR_URL_KEY},
                Type.STRING);
        m_filePanel = new DialogComponentFileChooser2(0, m_config.getFileChooserModel(), "line_read",
            JFileChooser.OPEN_DIALOG, JFileChooser.FILES_AND_DIRECTORIES, fvm);
        m_rowHeadPrefixField =
            new DialogComponentString(m_config.getRowPrefixModel(), "Row header prefix ", false, COMP_WIDTH);

        m_customColumnSelect =
            new DialogComponentButtonGroup(m_config.getChooserModel(), null, true, CustomColumnHeader.values());
        m_columnHeaderField = new DialogComponentString(m_config.getColumnHeaderModel(), "", true, COMP_WIDTH);

        m_skipEmptyLinesChecker = new DialogComponentBoolean(m_config.getSkipEmptyLinesModel(), "Skip empty lines");

        //  TODO Regex hhistory new StringHistoryPanel("org.knime.base.node.io.linereader.RegexHistory");

        //Regex setup
        m_useRegexButton = new DialogComponentBoolean(m_config.getUseRegexModel(), "Match input against regex");
        m_regexField = new DialogComponentString(m_config.getRegexModel(), "");
        m_config.getUseRegexModel().addChangeListener(e -> m_config.getRegexModel().setEnabled(m_config.getUseRegex()));

        //Row limit setup
        m_limitRowCountSpinner = new DialogComponentNumber(m_config.getLimitRowCountModel(), "", 100, 30);
        m_limitRowCountChecker = new DialogComponentBoolean(m_config.getLimitLinesModel(), "Limit number of rows");
        m_config.getLimitLinesModel()
            .addChangeListener(e -> m_config.getLimitRowCountModel().setEnabled(m_config.getLimitLines()));

        // Encoding setup
        m_encodingWarning = new JLabel("");
        m_encodingWarning.setForeground(Color.RED);

        m_encodingLabel = new JLabel("Encoding:");
        m_encodingSelection = new JComboBox<>( //
                new String[] { //
                    LineReaderConfig.DEFAULT_ENCODING, //
                    "US-ASCII", //
                    "ISO-8859-1", //
                    "UTF-8", //
                    "UTF-16LE", //
                    "UTF-16BE", //
                    "UTF-16" //
                }); //

        m_encodingSelection.setEditable(true);
        m_encodingSelection.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent e) {
                final String selectedItem = (String)m_encodingSelection.getEditor().getItem();
                updateEncodingWarning(selectedItem);
                m_config.getEncodingModel().setStringValue(selectedItem);
                // Needed for the label to be updated after each key has been typed!
                m_encodingWarning.revalidate();
            }

        });

        m_encodingSelection.addActionListener(e -> {
            String selectedEncoding = (String) m_encodingSelection.getSelectedItem();
            m_config.getEncodingModel().setStringValue(selectedEncoding);
            updateEncodingWarning(selectedEncoding);
        });

        addTab("Settings", initLayout());
    }

    private void updateEncodingWarning(final String selectedEncoding) {
        try {
            if (!Charset.isSupported(selectedEncoding)) {
                m_encodingWarning.setText(String.format("The encoding '%s' is not supported", selectedEncoding));
            } else {
                m_encodingWarning.setText("");
            }
        } catch (Exception e) {
            m_encodingWarning.setText(String.format("Invalid encoding name '%s'.", selectedEncoding));
        }
    }

    private JPanel initLayout() {
        final JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
        filePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Input location:"));

        filePanel.add(m_filePanel.getComponentPanel());
        filePanel.add(Box.createHorizontalGlue());

        final JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Reader options:"));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;
        optionsPanel.add(m_rowHeadPrefixField.getComponentPanel(), gbc);

        final JPanel panelColumnHeader = new JPanel(new GridBagLayout());
        panelColumnHeader.setBorder(BorderFactory.createTitledBorder("Column Header"));
        gbc.gridy++;
        gbc.gridwidth = 2;
        optionsPanel.add(panelColumnHeader, gbc);
        final GridBagConstraints gbcModusRowNr = new GridBagConstraints();
        gbcModusRowNr.fill = GridBagConstraints.VERTICAL;
        gbcModusRowNr.gridx = 0;
        gbcModusRowNr.gridy = 0;
        gbcModusRowNr.anchor = GridBagConstraints.WEST;
        gbcModusRowNr.insets = new Insets(14, 0, 5, 0);
        gbcModusRowNr.gridheight = 2;
        gbcModusRowNr.gridx++;
        panelColumnHeader.add(m_customColumnSelect.getComponentPanel(), gbcModusRowNr);
        gbcModusRowNr.gridx++;
        gbcModusRowNr.weightx = 1;

        panelColumnHeader.add(m_columnHeaderField.getComponentPanel(), gbcModusRowNr);
        gbc.gridy += 1;
        gbc.gridwidth = 1;
        optionsPanel.add(m_skipEmptyLinesChecker.getComponentPanel(), gbc);

        gbc.gridy += 1;
        gbc.gridwidth = 1;
        optionsPanel.add(m_limitRowCountChecker.getComponentPanel(), gbc);
        gbc.gridx += 1;
        optionsPanel.add(m_limitRowCountSpinner.getComponentPanel(), gbc);

        gbc.gridy += 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        optionsPanel.add(m_useRegexButton.getComponentPanel(), gbc);
        gbc.gridx += 1;
        optionsPanel.add(m_regexField.getComponentPanel(), gbc);

        JPanel encodingPanel = new JPanel();
        encodingPanel.setLayout(new BoxLayout(encodingPanel, BoxLayout.X_AXIS));

        encodingPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        encodingPanel.add(m_encodingLabel);
        encodingPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        encodingPanel.add(m_encodingSelection);
        encodingPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        encodingPanel.add(m_encodingWarning);
        gbc.gridwidth = 3;
        gbc.gridy += 1;
        gbc.gridx = 0;
        optionsPanel.add(encodingPanel, gbc);

        //empty panel to eat up extra space
        gbc.gridwidth = 1;
        gbc.gridx++;
        gbc.gridy++;
        gbc.weightx = 1;
        gbc.weighty = 1;
        optionsPanel.add(new JPanel(), gbc);

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(filePanel);
        panel.add(optionsPanel);

        return panel;
    }

    /** {@inheritDoc} */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        try {
            m_config.loadConfiguration(settings);
            m_filePanel.loadSettingsFrom(settings, specs);

            final String configuredEncoding = m_config.getEncoding();
            m_encodingSelection.setSelectedItem(configuredEncoding);
            updateEncodingWarning(configuredEncoding);
        } catch (final InvalidSettingsException ex) {
            throw new NotConfigurableException(ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_filePanel.saveSettingsTo(settings);
        m_config.saveConfiguration(settings);
    }

}
