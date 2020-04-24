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
 *   Apr 24, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.filesystemchooser;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.CheckUtils;
import org.knime.filehandling.core.defaultnodesettings.FileSystemChoice;

/**
 * {@link SettingsModel} that stores information about a file system.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class SettingsModelFileSystem extends SettingsModel {

    private final String m_configName;

    private String m_selectedFileSystem;

    private long m_customUrlTimeout;

    private String m_selectedMountpoint;

    private String m_selectedRelativeTo;

    public SettingsModelFileSystem(final String configName) {
        m_configName = configName;
    }

    /**
     * Copy constructor.
     *
     * @param toCopy the instance to copy
     */
    SettingsModelFileSystem(final SettingsModelFileSystem toCopy) {
        m_configName = toCopy.m_configName;
        m_selectedFileSystem = toCopy.m_selectedFileSystem;
        m_customUrlTimeout = toCopy.m_customUrlTimeout;
        m_selectedMountpoint = toCopy.m_selectedMountpoint;
        m_selectedRelativeTo = toCopy.m_selectedRelativeTo;
    }

    public FSSpec getFSSpec() {
        final FileSystemChoice fsChoice = FileSystemChoice.getChoiceFromId(m_selectedFileSystem);
        switch (fsChoice.getType()) {
            case CONNECTED_FS:
                return new FSSpec(m_selectedFileSystem);
            case CUSTOM_URL_FS:
                return new FSSpec(m_selectedFileSystem, Long.toString(m_customUrlTimeout));
            case KNIME_FS:
                return new FSSpec(m_selectedFileSystem, m_selectedRelativeTo);
            case KNIME_MOUNTPOINT:
                return new FSSpec(m_selectedFileSystem, m_selectedMountpoint);
            case LOCAL_FS:
                return new FSSpec(m_selectedFileSystem);
            default:
                // can't be reached
                throw new IllegalStateException(
                    "The currently selected file system is not supported: " + m_selectedFileSystem);

        }
    }

    @SuppressWarnings("unchecked") // that's not how recursive generics work...
    @Override
    protected SettingsModelFileSystem createClone() {
        return new SettingsModelFileSystem(this);
    }

    @Override
    protected String getModelTypeID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected final String getConfigName() {
        return m_configName;
    }

    @Override
    protected final void loadSettingsForDialog(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        final NodeSettingsRO subsettings;
        try {
            subsettings = settings.getNodeSettings(m_configName);
        } catch (InvalidSettingsException ex) {
            // no settings available, so we just use whatever is currently set
            return;
        }
        // first load the additional settings to avoid loading the settings only partially if this call fails
        loadAdditionalSettingsForDialog(subsettings, specs);

        // TODO load
    }

    /**
     * Hook for extending classes that allows to load additional settings.
     *
     * @param settings the settings object to load from (already the subsettings of SettingsModelFileSystem)
     * @param specs the input specs of the node
     * @throws NotConfigurableException if the specs don't allow to open the dialog
     */
    void loadAdditionalSettingsForDialog(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        // no additional settings
    }

    @Override
    protected final void saveSettingsForDialog(final NodeSettingsWO settings) throws InvalidSettingsException {
        validateState();
        final NodeSettingsWO fsSettings = settings.addNodeSettings(m_configName);
        save(fsSettings);
        saveAdditionalSettingsForDialog(fsSettings);
    }

    private void validateState() throws InvalidSettingsException {
        // TODO implement
    }

    private void save(final NodeSettingsWO fsSettings) {
        fsSettings.addString("file_system", m_selectedFileSystem);
        fsSettings.addString("mountpoint", m_selectedMountpoint);
        fsSettings.addString("relative_to", m_selectedRelativeTo);
        fsSettings.addLong("custom_url_timeout", m_customUrlTimeout);
    }

    /**
     * Hook for extending classes that allows to save additional settings.
     *
     * @param settings settings to save to (already the subsettings of {@link SettingsModelFileSystem})
     * @throws InvalidSettingsException
     */
    void saveAdditionalSettingsForDialog(final NodeSettingsWO settings) throws InvalidSettingsException {
        // no additional settings to save
    }

    @Override
    protected final void validateSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        final NodeSettingsRO fsSettings = settings.getNodeSettings(m_configName);
        validate(fsSettings);
        validateAdditionalSettingsForModel(fsSettings);
    }

    private static void validate(final NodeSettingsRO fsSettings) throws InvalidSettingsException {
        // TODO is 0 a sensible lower bound?
        long customURLTimeout = fsSettings.getLong("custom_url_timeout");
        CheckUtils.checkSetting(customURLTimeout >= 0, "The timeout for a custom URL must be non-negative but was %s.",
            customURLTimeout);
        // TODO validate other fields
    }

    /**
     * Hook for extending classes that allows to validate additional settings.
     *
     * @param settings {@link NodeSettingsRO} to validate (already the subsettings of {@link SettingsModelFileSystem})
     */
    void validateAdditionalSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        // no additional settings to validate
    }

    @Override
    protected final void loadSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        final NodeSettingsRO subsettings = settings.getNodeSettings(m_configName);
        // TODO load
        loadAdditionalSettingsForModel(subsettings);
    }

    /**
     * Hook for extending classes that allows to load additional settings in the NodeModel.
     *
     * @param settings {@link NodeSettingsRO} to load from (already the subsettings of {@link SettingsModelFileSystem})
     */
    void loadAdditionalSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        // no additional settings to load
    }

    @Override
    protected final void saveSettingsForModel(final NodeSettingsWO settings) {
        final NodeSettingsWO fsSettings = settings.addNodeSettings(m_configName);
        save(fsSettings);
        saveAdditionalSettingsForModel(fsSettings);
    }

    /**
     * Hook for extending classes that allows to save additional settings in the NodeModel.
     *
     * @param settings {@link NodeSettingsWO} to save to (already the subsettings of {@link SettingsModelFileSystem})
     */
    void saveAdditionalSettingsForModel(final NodeSettingsWO settings) {
        // no additional settings to save
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(" + m_configName + ")";
    }

}
