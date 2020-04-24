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
 *   23 Apr 2020 (Timmo Waller-Ehrat, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.io.filehandling.table.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.commons.io.input.CountingInputStream;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.SwingWorkerWithContext;
import org.knime.filehandling.core.defaultnodesettings.SettingsModelFileChooser2;
import org.knime.filehandling.core.util.BomEncodingUtils;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 *
 * @author Timmo Waller-Ehrat, KNIME GmbH, Konstanz, Germany
 */
final class CSVFormatAutoDetectionSwingWorker extends SwingWorkerWithContext<CsvFormat, Double> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CSVFormatAutoDetectionSwingWorker.class);

    private final JTextField m_colDelimiterField;

    private final JTextField m_rowDelimiterField;

    private final JTextField m_quoteField;

    private final JTextField m_quoteEscapeField;

    private final JButton m_startAutodetection;

    private final JProgressBar m_analyzeProgressBar;

    private final SettingsModelFileChooser2 m_model;

    private final Charset m_charset;

    private final boolean m_skipLines;

    private final long m_numLinesToSkip;

    private final CsvParser m_parser;

    private BufferedReader m_reader;

    public CSVFormatAutoDetectionSwingWorker(final SettingsModelFileChooser2 model, final Charset charset,
        final boolean skipLines, final long numLinesToSkip, final JTextField colDelimiterField,
        final JTextField rowDelimiterField, final JTextField quoteField, final JTextField quoteEscapeField,
        final JProgressBar analyzeProgressBar, final JButton startAutodetection) {
        m_colDelimiterField = colDelimiterField;
        m_rowDelimiterField = rowDelimiterField;
        m_quoteField = quoteField;
        m_quoteEscapeField = quoteEscapeField;
        m_analyzeProgressBar = analyzeProgressBar;
        m_model = model;
        m_charset = charset;
        m_skipLines = skipLines;
        m_numLinesToSkip = numLinesToSkip;
        m_startAutodetection = startAutodetection;

        final CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically();
        m_parser = new CsvParser(settings);
    }

    @Override
    protected CsvFormat doInBackgroundWithContext() throws IOException {
        final Path path = Paths.get(m_model.getPathOrURL());

        try (CountingInputStream inputStream = new CountingInputStream(Files.newInputStream(path))) {

            final double doubleSize = Files.size(Paths.get(m_model.getPathOrURL()));
            m_analyzeProgressBar.setValue(0);
            m_analyzeProgressBar.setVisible(true);

            m_reader = BomEncodingUtils.createBufferedReader(inputStream, m_charset);
            if (m_skipLines) {
                skipLines(m_numLinesToSkip);
            }

            m_parser.beginParsing(m_reader);

            m_analyzeProgressBar.setVisible(true);
            while (m_parser.parseNext() != null) {
                final double progress = (inputStream.getCount() / doubleSize);
                publish(progress);
            }

            m_parser.stopParsing();
            m_reader.close();

            return m_parser.getDetectedFormat();
        }
    }

    @Override
    protected void processWithContext(final List<Double> chunks) {
        final double progress = chunks.get(chunks.size() - 1) * 100;
        m_analyzeProgressBar.setValue((int)Math.round(progress));
    }

    @Override
    protected void doneWithContext() {
        try {
            updateUI(get());
        } catch (final ExecutionException e) {
            if (!(e.getCause() instanceof InterruptedException)) {
                LOGGER.debug(e.getMessage(), e);
            }
        } catch (InterruptedException | CancellationException ex) {
            // early stop, use results so far collected
            updateUI(m_parser.getDetectedFormat());
        }
    }

    private void updateUI(final CsvFormat format) {
        m_startAutodetection.setText(CSVTableReaderNodeDialog.START_AUTODETECT_LABEL);
        m_analyzeProgressBar.setVisible(false);
        m_analyzeProgressBar.setValue(0);
        m_colDelimiterField.setText(format.getDelimiterString());
        m_rowDelimiterField.setText(EscapeUtils.escape(format.getLineSeparatorString()));
        m_quoteField.setText(Character.toString(format.getQuote()));
        m_quoteEscapeField.setText(Character.toString(format.getQuoteEscape()));
    }

    private void skipLines(final long n) throws IOException {
        for (int i = 0; i < n; i++) {
            m_reader.readLine();
        }
    }

}
