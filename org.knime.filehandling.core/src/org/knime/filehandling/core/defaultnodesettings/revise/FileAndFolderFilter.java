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
 *   Aug 15, 2019 (Tobias Urhaug, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.revise;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * File and folder filter based on {@link FilterOptionsSettings}.
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 * @author Tobias Urhaug, KNIME GmbH, Berlin, Germany
 * @author Mareike Hoeger, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("javadoc")
public final class FileAndFolderFilter implements Predicate<Path> {

    /**
     * FilterType enumeration used for {@link FileAndFolderFilter}.
     *
     * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
     * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
     */
    enum FilterType implements ButtonGroupEnumInterface {

            /**
             * Only files or folders with names containing the wildcard pass this filter.
             */
            WILDCARD("Wildcard", "Only files/folders with names containing the wildcard pass this filter.",
                "Wildcard patterns contain '*' (sequence of characters) and '?' (one character)."),
            /**
             * Only files or folders with names that match the regex pass this filter.
             */
            REGEX("Regular expression", "Only files/folders with names that matches the regex pass this filter.",
                "As an example, the regual expression [0-9]* matches any string of digits. "
                    + "For more examples see java.util.regex.Pattern");

        private final String m_displayText;

        private final String m_description;

        private final String m_inputTooltip;

        private FilterType(final String displayText, final String description, final String tooltip) {
            m_displayText = displayText;
            m_description = description;
            m_inputTooltip = tooltip;
        }

        /**
         * Returns true, if the argument represents a filter type in this enum.
         *
         * @param filterType filter type to check
         * @return true, if the argument represents a filter type in this enum
         */
        public static final boolean contains(final String filterType) {
            return Arrays.stream(values()).anyMatch(f -> f.name().equals(filterType));
        }

        @Override
        public String getText() {
            return m_displayText;
        }

        @Override
        public String getActionCommand() {
            return name();
        }

        @Override
        public String getToolTip() {
            return m_description;
        }

        /**
         * @return the inputTooltip
         */
        public String getInputTooltip() {
            return m_inputTooltip;
        }

        @Override
        public boolean isDefault() {
            return this == WILDCARD;
        }
    }

    /** Total number of filtered files */
    private int m_numberOfFilteredFiles;

    /** Total number of filtered folders */
    private int m_numberOfFilteredFolders;

    private final FilterOptionsSettings m_fileFilterSettings;

    /**
     * Constructor using a {@link FilterOptionsSettings} that contains all necessary parameters.
     *
     * @param filterOptionSettings settings model containing necessary parameters
     */
    public FileAndFolderFilter(final FilterOptionsSettings filterOptionSettings) {
        m_fileFilterSettings = filterOptionSettings;
    }

    private final boolean isSatisfiedFilterHidden(final Path path) {
        try {
            return !Files.isHidden(path) || m_fileFilterSettings.isIncludeHiddenFiles();
        } catch (final IOException ex) {
            return true;
        }
    }

    private final boolean isSatisfiedFolderHidden(final Path path) {
        try {
            return !Files.isHidden(path) || m_fileFilterSettings.isIncludeHiddenFolders();
        } catch (final IOException ex) {
            return true;
        }
    }

    private final boolean isSatisfiedFileExtension(final Path path) {
        if (!m_fileFilterSettings.isFilterFilesByExtension()) {
            return true;
        }
        final boolean accept = isSatisfiedExtension(path, m_fileFilterSettings.getFilesExtensionExpression(),
            m_fileFilterSettings.isFilesExtensionCaseSensitive());
        if (!accept) {
            m_numberOfFilteredFiles++;
        }

        return accept;
    }

    private final static boolean isSatisfiedExtension(final Path path, final String foldersExtensionExpression,
        final boolean isCaseSensitive) {
        final String pathAsString = path.getFileName().toString();
        final List<String> extensions =
            Arrays.stream(foldersExtensionExpression.split(";")).map(ex -> "." + ex).collect(Collectors.toList());
        if (isCaseSensitive) {
            return extensions.stream().anyMatch(pathAsString::endsWith);
        } else {
            return extensions.stream()//
                .anyMatch(ext -> pathAsString.toLowerCase().endsWith(ext.toLowerCase()));
        }
    }

    private final boolean isSatisfiedFileName(final Path path) {
        if (!m_fileFilterSettings.isFilterFilesByName()) {
            return true;
        }
        final boolean accept = isSatisfiedName(path, m_fileFilterSettings.getFilesNameFilterMode(),
            m_fileFilterSettings.getFilesNameExpression(), m_fileFilterSettings.isFilesNameCaseSensitive());
        if (!accept) {
            m_numberOfFilteredFiles++;
        }

        return accept;
    }

    private final boolean isSatisfiedFolderName(final Path path) {
        if (!m_fileFilterSettings.isFilterFoldersByName()) {
            return true;
        }
        final boolean accept = isSatisfiedName(path, m_fileFilterSettings.getFoldersNameFilterMode(),
            m_fileFilterSettings.getFoldersNameExpression(), m_fileFilterSettings.isFoldersNameCaseSensitive());
        if (!accept) {
            m_numberOfFilteredFolders++;
        }
        return accept;
    }

    private static boolean isSatisfiedName(final Path path, final FilterType filterMode, final String filterExpression,
        final boolean isCaseSensitive) {
        final String regexString =
            filterMode.equals(FilterType.WILDCARD) ? wildcardToRegex(filterExpression, false) : filterExpression;
        final Pattern regex =
            isCaseSensitive ? Pattern.compile(regexString) : Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);
        final String pathAsString = path.getFileName().toString();
        return regex.matcher(pathAsString).matches();
    }

    /**
     * Returns the number of filtered files.
     *
     * @return the number of filtered files
     */
    public final int getNumberOfFilteredFiles() {
        return m_numberOfFilteredFiles;
    }

    /**
     * Returns the number of filtered folders.
     *
     * @return the number of filtered folders
     */
    public final int getNumberOfFilteredFolders() {
        return m_numberOfFilteredFolders;
    }

    /**
     * Resets the counters of filtered files and filtered folders.
     */
    public final void resetCounter() {
        m_numberOfFilteredFiles = 0;
        m_numberOfFilteredFolders = 0;
    }

    /**
     * Converts a wildcard pattern containing '*' and '?' as meta characters into a regular expression. Optionally, the
     * backslash can be enabled as escape character for the wildcards. In this case a backslash has a special meaning
     * and needs may need to be escaped itself.
     *
     * @param wildcard a wildcard expression
     * @param enableEscaping {@code true} if the wildcards may be escaped (i.e. they loose their special meaning) by
     *            prepending a backslash
     * @return the corresponding regular expression
     */
    private static final String wildcardToRegex(final String wildcard, final boolean enableEscaping) {
        // FIXME: This method is copied from org.knime.base.util.WildcardMatcher
        // (we don't want to import org.knime.base)
        // This needs to be replaced by a more convenient solutions
        final StringBuilder buf = new StringBuilder(wildcard.length() + 20);

        for (int i = 0; i < wildcard.length(); i++) {
            final char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    if (enableEscaping && (i > 0) && (wildcard.charAt(i - 1) == '\\')) {
                        buf.append('*');
                    } else {
                        buf.append(".*");
                    }
                    break;
                case '?':
                    if (enableEscaping && (i > 0) && (wildcard.charAt(i - 1) == '\\')) {
                        buf.append('?');
                    } else {
                        buf.append(".");
                    }
                    break;
                case '\\':
                    if (enableEscaping) {
                        buf.append(c);
                        break;
                    }
                case '^':
                case '$':
                case '[':
                case ']':
                case '{':
                case '}':
                case '(':
                case ')':
                case '|':
                case '+':
                case '.':
                    buf.append("\\");
                    buf.append(c);
                    break;
                default:
                    buf.append(c);
            }
        }

        return buf.toString();
    }

    @Override
    public boolean test(final Path path) {
        if (Files.isDirectory(path)) {
            return isSatisfiedFolderHidden(path) && //
                isSatisfiedFolderName(path);
        }
        return Files.isRegularFile(path) && //
            isSatisfiedFilterHidden(path) && //
            isSatisfiedFileExtension(path) && //
            isSatisfiedFileName(path);
    }

}
