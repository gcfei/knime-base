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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.connections.FSLocation;
import org.knime.filehandling.core.data.location.settingsmodel.SettingsModelFSLocation;

/**
 * DialogComponent for selecting a file system.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class DialogComponentFileSystem extends DialogComponent {

    private final FileSystemChooserDialog m_fileSystemChooser;

    /**
     * Constructor.
     *
     * @param model an {@link SettingsModelFSLocation} (note that only file system identifier and specifier are used
     *            while the path is ignored)
     */
    public DialogComponentFileSystem(final SettingsModelFSLocation model) {
        super(model);
        final LocalFileSystemDialog local = new LocalFileSystemDialog();
        final MountpointFileSystemDialog mountpoint = new MountpointFileSystemDialog();
        final RelativeToFileSystemDialog relativeTo = new RelativeToFileSystemDialog();
        final CustomURLFileSystemDialog customUrl = new CustomURLFileSystemDialog();
        m_fileSystemChooser = new FileSystemChooserDialog(local, mountpoint, relativeTo, customUrl);
    }

    @Override
    protected void updateComponent() {
        final FSLocation fsLocation = getFSLocation();
        final FileSystemInfo fsInfo =
            new FileSystemInfo(fsLocation.getFileSystemType(), fsLocation.getFileSystemSpecifier().orElse(null));
        m_fileSystemChooser.setFileSystemInfo(fsInfo);
    }

    private FSLocation getFSLocation() {
        final SettingsModelFSLocation sm = (SettingsModelFSLocation)getModel();
        return sm.getLocation();
    }

    @Override
    protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs) throws NotConfigurableException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setEnabledComponents(final boolean enabled) {

    }

    @Override
    public void setToolTipText(final String text) {
        m_fileSystemChooser.setTooltip(text);
    }

}
