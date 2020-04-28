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
 *   11.11.2019 (Mareike Hoeger, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.revise;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;
import org.knime.filehandling.core.defaultnodesettings.revise.FileAndFolderFilter.FilterType;

/**
 * This class stores the data from the {@link FilterOptionsPanel}.
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 * @author Mareike Hoeger, KNIME GmbH, Konstanz, Germany
 */
final class FilterOptionsSettings {

    /** Configuration key for the option to filter files by extension in selected folder. */
    private static final String CFG_FILES_FILTER_BY_EXTENSION = "files_filter_extension";

    /** Configuration key to store the filter expression for the file extension filter. */
    private static final String CFG_FILES_EXTENSION_EXPRESSION = "files_extension_expression";

    /** Configuration key to store whether the filter expression for file extensions is case sensitive or not. */
    private static final String CFG_FILES_EXTENSION_CASE_SENSITIVE = "files_extension_case_sensitive";

    /** Configuration key for the option to filter files by name in selected folder. */
    private static final String CFG_FILES_FILTER_BY_NAME = "files_filter_name";

    /** Configuration key to store the filter expression for the file name filter. */
    private static final String CFG_FILES_NAME_EXPRESSION = "files_name_expression";

    /** Configuration key to store whether the filter expression for file names is case sensitive or not. */
    private static final String CFG_FILES_NAME_CASE_SENSITIVE = "files_name_case_sensitive";

    /** Configuration key to store the file name filter mode. */
    private static final String CFG_FILES_NAME_FILTER_MODE = "files_name_filter_mode";

    /** Configuration key for the option to filter hidden files. */
    private static final String CFG_FILTER_HIDDEN_FILES = "filter_hidden_files";

    /** Configuration key for the option to filter folders by extension in selected folder. */
    private static final String CFG_FOLDERS_FILTER_BY_EXTENSION = "folders_filter_extension";

    /** Configuration key to store the filter expression for the folder extension filter. */
    private static final String CFG_FOLDERS_EXTENSION_EXPRESSION = "folders_extension_expression";

    /** Configuration key to store whether the filter expression for folder extensions is case sensitive or not. */
    private static final String CFG_FOLDERS_EXTENSION_CASE_SENSITIVE = "folders_extension_case_sensitive";

    /** Configuration key for the option to filter folders by name in selected folder. */
    private static final String CFG_FOLDERS_FILTER_BY_NAME = "folders_filter_name";

    /** Configuration key to store the filter expression for the folder name filter. */
    private static final String CFG_FOLDERS_NAME_EXPRESSION = "folders_name_expression";

    /** Configuration key to store whether the filter expression for folder names is case sensitive or not. */
    private static final String CFG_FOLDERS_NAME_CASE_SENSITIVE = "folders_name_case_sensitive";

    /** Configuration key to store the folder name filter mode. */
    private static final String CFG_FOLDERS_NAME_FILTER_MODE = "folders_name_filter_mode";

    /** Configuration key for the option to filter hidden folders. */
    private static final String CFG_FILTER_HIDDEN_FOLDERS = "filter_hidden_folders";

    /** True, if hidden files should be filtered. */
    private boolean m_filterHiddenFiles;

    /** True, if files should be filtered by extension. */
    private boolean m_filterFilesByExtension;

    /** The expression used to filter files by extension in the selected folder/directory. */
    private String m_filesExtensionExpression;

    /** True, if expression to filter should work regardless the case of the file extension. */
    private boolean m_filesExtensionCaseSensitive;

    /** True, if files should be filtered by name. */
    private boolean m_filterFilesByName;

    /** The expression used to filter files in the selected folder/directory. */
    private String m_filesNameExpression;

    /** True, if expression to filter should work regardless the case of the file names. */
    private boolean m_filesNameCaseSensitive;

    /** Mode used to filter files (e.g. regex or wildcard). */
    private FilterType m_filesNameFilterMode;

    /** True, if hidden folders should be filtered. */
    private boolean m_filterHiddenFolders;

    /** True, if folders should be filtered by extension. */
    private boolean m_filterFoldersByExtension;

    /** The expression used to filter folders by extension in the selected folder/directory. */
    private String m_foldersExtensionExpression;

    /** True, if expression to filter should work regardless the case of the folder extension. */
    private boolean m_foldersExtensionCaseSensitive;

    /** True, if folders should be filtered by name. */
    private boolean m_filterFoldersByName;

    /** The expression used to filter folders in the selected folder/directory. */
    private String m_foldersNameExpression;

    /** True, if expression to filter should work regardless the case of the folder names. */
    private boolean m_foldersNameCaseSensitive;

    /** Mode used to filter folders (e.g. regex or wildcard). */
    private FilterType m_foldersNameFilterMode;

    /** The default filter. */
    private static final FilterType DEFAULT_FILTER = FilterType.WILDCARD;

    /** The default filter expression. */
    private static final String DEFAULT_FILTER_EXPRESSION = "*";

    //    /** Configuration key for the option to filter files by extension in selected folder. */
    //    private static final boolean DEFAULT_FILES_FILTER_BY_EXTENSION = false;
    //
    //    /** Configuration key to store the filter expression for the file extension filter. */
    //    private static final String DEFAULT_FILES_EXTENSION_EXPRESSION = "files_extension_expression";
    //
    //    /** Configuration key to store whether the filter expression for file extensions is case sensitive or not. */
    //    private static final boolean DEFAULT_FILES_EXTENSION_CASE_SENSITIVE = false;
    //
    //    /** Configuration key for the option to filter files by name in selected folder. */
    //    private static final boolean DEFAULT_FILES_FILTER_BY_NAME = false;
    //
    //    /** Configuration key to store the filter expression for the file name filter. */
    //    private static final String DEFAULT_FILES_NAME_EXPRESSION = "files_name_expression";
    //
    //    /** Configuration key to store whether the filter expression for file names is case sensitive or not. */
    //    private static final boolean DEFAULT_FILES_NAME_CASE_SENSITIVE = false;
    //
    //    /** Configuration key to store the file name filter mode. */
    //    private static final FilterType DEFAULT_FILES_NAME_FILTER_MODE = FilterType.WILDCARD;
    //
    //    /** Configuration key for the option to filter hidden files. */
    //    private static final boolean DEFAULT_FILTER_HIDDEN_FILES = true;
    //
    //    /** Configuration key for the option to filter folders by extension in selected folder. */
    //    private static final boolean DEFAULT_FOLDERS_FILTER_BY_EXTENSION = false;
    //
    //    /** Configuration key to store the filter expression for the folder extension filter. */
    //    private static final String DEFAULT_FOLDERS_EXTENSION_EXPRESSION = "folders_extension_expression";
    //
    //    /** Configuration key to store whether the filter expression for folder extensions is case sensitive or not. */
    //    private static final boolean DEFAULT_FOLDERS_EXTENSION_CASE_SENSITIVE = false;
    //
    //    /** Configuration key for the option to filter folders by name in selected folder. */
    //    private static final boolean DEFAULT_FOLDERS_FILTER_BY_NAME = false;
    //
    //    /** Configuration key to store the filter expression for the folder name filter. */
    //    private static final String DEFAULT_FOLDERS_NAME_EXPRESSION = "folders_name_expression";
    //
    //    /** Configuration key to store whether the filter expression for folder names is case sensitive or not. */
    //    private static final boolean DEFAULT_FOLDERS_NAME_CASE_SENSITIVE = false;
    //
    //    /** Configuration key to store the folder name filter mode. */
    //    private static final FilterType DEFAULT_FOLDERS_NAME_FILTER_MODE = FilterType.WILDCARD;
    //
    //    /** Configuration key for the option to filter hidden folders. */
    //    private static final boolean DEFAULT_FILTER_HIDDEN_FOLDERS = true;

    /**
     * Default constructor for filter settings.
     */
    FilterOptionsSettings() {
        this(new String[0]);
    }

    private FilterOptionsSettings(final boolean filterFilesByExtension, final String filesExtensionExpression) {
        m_filterHiddenFiles = true;
        m_filterFilesByExtension = filterFilesByExtension;
        m_filterFilesByName = false;
        m_filesExtensionExpression = filesExtensionExpression;
        m_filesExtensionCaseSensitive = false;
        m_filesNameExpression = DEFAULT_FILTER_EXPRESSION;
        m_filesNameCaseSensitive = false;
        m_filesNameFilterMode = DEFAULT_FILTER;

        m_filterHiddenFolders = true;
        m_filterFoldersByExtension = false;
        m_filterFoldersByName = false;
        m_foldersExtensionExpression = DEFAULT_FILTER_EXPRESSION;
        m_foldersExtensionCaseSensitive = false;
        m_foldersNameExpression = DEFAULT_FILTER_EXPRESSION;
        m_foldersNameCaseSensitive = false;
        m_foldersNameFilterMode = DEFAULT_FILTER;
    }

    /**
     * Filter settings with pre-set file suffixes to filter.
     *
     * @param fileSuffixes possibly empty array for file suffixes that should be filtered
     */
    FilterOptionsSettings(final String[] fileSuffixes) {
        this(fileSuffixes.length > 0,
            fileSuffixes.length > 0 ? String.join(";", fileSuffixes) : DEFAULT_FILTER_EXPRESSION);
    }

    /**
     * @return the filterHiddenFiles
     */
    boolean isFilterHiddenFiles() {
        return m_filterHiddenFiles;
    }

    /**
     * @param filterHiddenFiles the filterHiddenFiles to set
     */
    void setFilterHiddenFiles(final boolean filterHiddenFiles) {
        m_filterHiddenFiles = filterHiddenFiles;
    }

    /**
     * @return the filterFilesByExtension
     */
    boolean isFilterFilesByExtension() {
        return m_filterFilesByExtension;
    }

    /**
     * @param filterFilesByExtension the filterFilesByExtension to set
     */
    void setFilterFilesByExtension(final boolean filterFilesByExtension) {
        m_filterFilesByExtension = filterFilesByExtension;
    }

    /**
     * @return the filesExtensionExpression
     */
    String getFilesExtensionExpression() {
        return m_filesExtensionExpression;
    }

    /**
     * @param filesExtensionExpression the filesExtensionExpression to set
     */
    void setFilesExtensionExpression(final String filesExtensionExpression) {
        m_filesExtensionExpression = filesExtensionExpression;
    }

    /**
     * @return the filesExtensionCaseSensitive
     */
    boolean isFilesExtensionCaseSensitive() {
        return m_filesExtensionCaseSensitive;
    }

    /**
     * @param filesExtensionCaseSensitive the filesExtensionCaseSensitive to set
     */
    void setFilesExtensionCaseSensitive(final boolean filesExtensionCaseSensitive) {
        m_filesExtensionCaseSensitive = filesExtensionCaseSensitive;
    }

    /**
     * @return the filterFilesByName
     */
    boolean isFilterFilesByName() {
        return m_filterFilesByName;
    }

    /**
     * @param filterFilesByName the filterFilesByName to set
     */
    void setFilterFilesByName(final boolean filterFilesByName) {
        m_filterFilesByName = filterFilesByName;
    }

    /**
     * @return the filesNameExpression
     */
    String getFilesNameExpression() {
        return m_filesNameExpression;
    }

    /**
     * @param filesNameExpression the filesNameExpression to set
     */
    void setFilesNameExpression(final String filesNameExpression) {
        m_filesNameExpression = filesNameExpression;
    }

    /**
     * @return the filesNameCaseSensitive
     */
    public boolean isFilesNameCaseSensitive() {
        return m_filesNameCaseSensitive;
    }

    /**
     * @param filesNameCaseSensitive the filesNameCaseSensitive to set
     */
    void setFilesNameCaseSensitive(final boolean filesNameCaseSensitive) {
        m_filesNameCaseSensitive = filesNameCaseSensitive;
    }

    /**
     * @return the filesNameFilterMode
     */
    FilterType getFilesNameFilterMode() {
        return m_filesNameFilterMode;
    }

    /**
     * @param filesNameFilterMode the filesNameFilterMode to set
     */
    void setFilesNameFilterMode(final FilterType filesNameFilterMode) {
        m_filesNameFilterMode = filesNameFilterMode;
    }

    /**
     * @return the filterHiddenFolders
     */
    boolean isFilterHiddenFolders() {
        return m_filterHiddenFolders;
    }

    /**
     * @param filterHiddenFolders the filterHiddenFolders to set
     */
    void setFilterHiddenFolders(final boolean filterHiddenFolders) {
        m_filterHiddenFolders = filterHiddenFolders;
    }

    /**
     * @return the filterFoldersByExtension
     */
    boolean isFilterFoldersByExtension() {
        return m_filterFoldersByExtension;
    }

    /**
     * @param filterFoldersByExtension the filterFoldersByExtension to set
     */
    void setFilterFoldersByExtension(final boolean filterFoldersByExtension) {
        m_filterFoldersByExtension = filterFoldersByExtension;
    }

    /**
     * @return the foldersExtensionExpression
     */
    String getFoldersExtensionExpression() {
        return m_foldersExtensionExpression;
    }

    /**
     * @param foldersExtensionExpression the foldersExtensionExpression to set
     */
    void setFoldersExtensionExpression(final String foldersExtensionExpression) {
        m_foldersExtensionExpression = foldersExtensionExpression;
    }

    /**
     * @return the foldersExtensionCaseSensitive
     */
    boolean isFoldersExtensionCaseSensitive() {
        return m_foldersExtensionCaseSensitive;
    }

    /**
     * @param foldersExtensionCaseSensitive the foldersExtensionCaseSensitive to set
     */
    public void setFoldersExtensionCaseSensitive(final boolean foldersExtensionCaseSensitive) {
        m_foldersExtensionCaseSensitive = foldersExtensionCaseSensitive;
    }

    /**
     * @return the filterFoldersByName
     */
    boolean isFilterFoldersByName() {
        return m_filterFoldersByName;
    }

    /**
     * @param filterFoldersByName the filterFoldersByName to set
     */
    void setFilterFoldersByName(final boolean filterFoldersByName) {
        m_filterFoldersByName = filterFoldersByName;
    }

    /**
     * @return the foldersNameExpression
     */
    String getFoldersNameExpression() {
        return m_foldersNameExpression;
    }

    /**
     * @param foldersNameExpression the foldersNameExpression to set
     */
    void setFoldersNameExpression(final String foldersNameExpression) {
        m_foldersNameExpression = foldersNameExpression;
    }

    /**
     * @return the foldersNameCaseSensitive
     */
    boolean isFoldersNameCaseSensitive() {
        return m_foldersNameCaseSensitive;
    }

    /**
     * @param foldersNameCaseSensitive the foldersNameCaseSensitive to set
     */
    void setFoldersNameCaseSensitive(final boolean foldersNameCaseSensitive) {
        m_foldersNameCaseSensitive = foldersNameCaseSensitive;
    }

    /**
     * @return the foldersNameFilterMode
     */
    FilterType getFoldersNameFilterMode() {
        return m_foldersNameFilterMode;
    }

    /**
     * @param foldersNameFilterMode the foldersNameFilterMode to set
     */
    void setFoldersNameFilterMode(final FilterType foldersNameFilterMode) {
        m_foldersNameFilterMode = foldersNameFilterMode;
    }

    /**
     * Saves the the file filter settings to the given {@link Config}.
     *
     * @param config the configuration to save to
     */
    void saveToConfig(final Config config) {
        config.addBoolean(CFG_FILES_FILTER_BY_EXTENSION, m_filterFilesByExtension);
        config.addString(CFG_FILES_EXTENSION_EXPRESSION, m_filesExtensionExpression);
        config.addBoolean(CFG_FILES_EXTENSION_CASE_SENSITIVE, m_filesExtensionCaseSensitive);
        config.addBoolean(CFG_FILES_FILTER_BY_NAME, m_filterFilesByName);
        config.addString(CFG_FILES_NAME_EXPRESSION, m_filesNameExpression);
        config.addBoolean(CFG_FILES_NAME_CASE_SENSITIVE, m_filesNameCaseSensitive);
        config.addString(CFG_FILES_NAME_FILTER_MODE, m_filesNameFilterMode.name());
        config.addBoolean(CFG_FILTER_HIDDEN_FILES, m_filterHiddenFiles);

        config.addBoolean(CFG_FOLDERS_FILTER_BY_EXTENSION, m_filterFoldersByExtension);
        config.addString(CFG_FOLDERS_EXTENSION_EXPRESSION, m_foldersExtensionExpression);
        config.addBoolean(CFG_FOLDERS_EXTENSION_CASE_SENSITIVE, m_foldersExtensionCaseSensitive);
        config.addBoolean(CFG_FOLDERS_FILTER_BY_NAME, m_filterFoldersByName);
        config.addString(CFG_FOLDERS_NAME_EXPRESSION, m_foldersNameExpression);
        config.addBoolean(CFG_FOLDERS_NAME_CASE_SENSITIVE, m_foldersNameCaseSensitive);
        config.addString(CFG_FOLDERS_NAME_FILTER_MODE, m_foldersNameFilterMode.name());
        config.addBoolean(CFG_FILTER_HIDDEN_FOLDERS, m_filterHiddenFolders);
    }

    /**
     * Loads the filter configuration from the given {@link Config} into the {@link FilterOptionsSettings}.
     *
     * @param config the configuration to load the values from
     * @throws InvalidSettingsException if keys are not available
     */
    void loadFromConfigForModel(final Config config) throws InvalidSettingsException {
        m_filterFilesByExtension = config.getBoolean(CFG_FILES_FILTER_BY_EXTENSION);
        m_filesExtensionExpression = config.getString(CFG_FILES_EXTENSION_EXPRESSION);
        m_filesExtensionCaseSensitive = config.getBoolean(CFG_FILES_EXTENSION_CASE_SENSITIVE);
        m_filterFilesByName = config.getBoolean(CFG_FILES_FILTER_BY_NAME);
        m_filesNameExpression = config.getString(CFG_FILES_NAME_EXPRESSION);
        m_filesNameCaseSensitive = config.getBoolean(CFG_FILES_NAME_CASE_SENSITIVE);
        m_filesNameFilterMode = FilterType.valueOf(config.getString(CFG_FILES_NAME_FILTER_MODE));
        m_filterHiddenFiles = config.getBoolean(CFG_FILTER_HIDDEN_FILES);

        m_filterFoldersByExtension = config.getBoolean(CFG_FOLDERS_FILTER_BY_EXTENSION);
        m_foldersExtensionExpression = config.getString(CFG_FOLDERS_EXTENSION_EXPRESSION);
        m_foldersExtensionCaseSensitive = config.getBoolean(CFG_FOLDERS_EXTENSION_CASE_SENSITIVE);
        m_filterFoldersByName = config.getBoolean(CFG_FOLDERS_FILTER_BY_NAME);
        m_foldersNameExpression = config.getString(CFG_FOLDERS_NAME_EXPRESSION);
        m_foldersNameCaseSensitive = config.getBoolean(CFG_FOLDERS_NAME_CASE_SENSITIVE);
        m_foldersNameFilterMode = FilterType.valueOf(config.getString(CFG_FOLDERS_NAME_FILTER_MODE));
        m_filterHiddenFolders = config.getBoolean(CFG_FILTER_HIDDEN_FOLDERS);
    }

    /**
     * Loads the filter configuration from the given {@link Config} into the {@link FilterOptionsSettings}.
     *
     * @param config the configuration to load the values from
     */
    void loadFromConfigForDialog(final Config config) {
        m_filterFilesByExtension = config.getBoolean(CFG_FILES_FILTER_BY_EXTENSION, m_filterFilesByExtension);
        m_filesExtensionExpression = config.getString(CFG_FILES_EXTENSION_EXPRESSION, m_filesExtensionExpression);
        m_filesExtensionCaseSensitive =
            config.getBoolean(CFG_FILES_EXTENSION_CASE_SENSITIVE, m_filesExtensionCaseSensitive);
        m_filterFilesByName = config.getBoolean(CFG_FILES_FILTER_BY_NAME, m_filterFilesByName);
        m_filesNameExpression = config.getString(CFG_FILES_NAME_EXPRESSION, m_filesNameExpression);
        m_filesNameCaseSensitive = config.getBoolean(CFG_FILES_NAME_CASE_SENSITIVE, m_filesNameCaseSensitive);
        m_filesNameFilterMode =
            FilterType.valueOf(config.getString(CFG_FILES_NAME_FILTER_MODE, m_filesNameFilterMode.name()));
        m_filterHiddenFiles = config.getBoolean(CFG_FILTER_HIDDEN_FILES, m_filterHiddenFiles);

        m_filterFoldersByExtension = config.getBoolean(CFG_FOLDERS_FILTER_BY_EXTENSION, m_filterFoldersByExtension);
        m_foldersExtensionExpression = config.getString(CFG_FOLDERS_EXTENSION_EXPRESSION, m_foldersExtensionExpression);
        m_foldersExtensionCaseSensitive =
            config.getBoolean(CFG_FOLDERS_EXTENSION_CASE_SENSITIVE, m_foldersExtensionCaseSensitive);
        m_filterFoldersByName = config.getBoolean(CFG_FOLDERS_FILTER_BY_NAME, m_filterFoldersByName);
        m_foldersNameExpression = config.getString(CFG_FOLDERS_NAME_EXPRESSION, m_foldersNameExpression);
        m_foldersNameCaseSensitive = config.getBoolean(CFG_FOLDERS_NAME_CASE_SENSITIVE, m_foldersNameCaseSensitive);
        m_foldersNameFilterMode =
            FilterType.valueOf(config.getString(CFG_FOLDERS_NAME_FILTER_MODE, m_foldersNameFilterMode.name()));
        m_filterHiddenFolders = config.getBoolean(CFG_FILTER_HIDDEN_FOLDERS, m_filterHiddenFolders);
    }

    /**
     * Validates the {@link Config}, i.e. checks if all configuration keys are available.
     *
     * @param config the configuration to validate.
     * @throws InvalidSettingsException if keys are not available.
     */
    static void validate(final Config config) throws InvalidSettingsException {
        config.getBoolean(CFG_FILES_FILTER_BY_EXTENSION);
        config.getString(CFG_FILES_EXTENSION_EXPRESSION);
        config.getBoolean(CFG_FILES_EXTENSION_CASE_SENSITIVE);
        config.getBoolean(CFG_FILES_FILTER_BY_NAME);
        config.getString(CFG_FILES_NAME_EXPRESSION);
        config.getBoolean(CFG_FILES_NAME_CASE_SENSITIVE);
        final String fileFilterMode = config.getString(CFG_FILES_NAME_FILTER_MODE);
        if (!FilterType.contains(fileFilterMode)) {
            throw new InvalidSettingsException("'" + fileFilterMode + "' is not a valid filter mode.");
        }
        config.getBoolean(CFG_FILTER_HIDDEN_FILES);

        config.getBoolean(CFG_FOLDERS_FILTER_BY_EXTENSION);
        config.getString(CFG_FOLDERS_EXTENSION_EXPRESSION);
        config.getBoolean(CFG_FOLDERS_EXTENSION_CASE_SENSITIVE);
        config.getBoolean(CFG_FOLDERS_FILTER_BY_NAME);
        config.getString(CFG_FOLDERS_NAME_EXPRESSION);
        config.getBoolean(CFG_FOLDERS_NAME_CASE_SENSITIVE);
        final String folderFilterMode = config.getString(CFG_FOLDERS_NAME_FILTER_MODE);
        if (!FilterType.contains(folderFilterMode)) {
            throw new InvalidSettingsException("'" + folderFilterMode + "' is not a valid filter mode.");
        }
        config.getBoolean(CFG_FILTER_HIDDEN_FOLDERS);
    }

}
