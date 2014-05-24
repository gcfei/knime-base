/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * ------------------------------------------------------------------------
 *
 * History
 *   22.01.2010 (hofer): created
 */
package org.knime.base.node.mine.regression.linear2.learner;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.regression.ModelSpecificationException;
import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.UpdatingMultipleLinearRegression;
import org.knime.base.node.mine.regression.RegressionStatisticsLearner;
import org.knime.base.node.mine.regression.RegressionTrainingData;
import org.knime.base.node.mine.regression.RegressionTrainingRow;
import org.knime.core.data.DataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;

import Jama.Matrix;

/**
 * A Linear Regression Learner.
 *
 * @author Heiko Hofer
 */
final class Learner extends RegressionStatisticsLearner {
    /**
     * @param spec The {@link PMMLPortObjectSpec} of the output table.
     */
    Learner(final PMMLPortObjectSpec spec) {
        this(spec, true, 0.0, true);
    }

    /**
     * @param spec The {@link PMMLPortObjectSpec} of the output table.
     * @param includeConstant include a constant automatically
     * @param offsetValue offset value (a user defined intercept)
     * @param failOnMissing when true an exception is thrown when a missing cell is observed
     */
    Learner(final PMMLPortObjectSpec spec, final boolean includeConstant, final double offsetValue,
        final boolean failOnMissing) {
        super(spec, failOnMissing, includeConstant);
        m_offsetValue = offsetValue;
    }

    /**
     * @param data The data table.
     * @param rowCount the number of rows in the input table, used for reporting progress. Set to -1 if progress should
     *            not be reported.
     * @param exec The execution context used for reporting progress.
     * @return An object which holds the results.
     * @throws CanceledExecutionException When method is cancelled
     * @throws InvalidSettingsException When settings are inconsistent with the data
     */
    @Override
    public LinearRegressionContent perform(final DataTable data, final int rowCount, final ExecutionContext exec)
        throws CanceledExecutionException, InvalidSettingsException {
        exec.checkCanceled();

        RegressionTrainingData trainingData = new RegressionTrainingData(data, m_outSpec, m_failOnMissing);

        final int regressorCount = Math.max(1,trainingData.getRegressorCount());
        SummaryStatistics[] stats = new SummaryStatistics[regressorCount];
        UpdatingMultipleLinearRegression regr = initStatistics(regressorCount, stats);

        exec.setProgress(0, "Estimating linear regression model.");
        processTable(rowCount, exec, trainingData, stats, regr);

        List<String> factorList = new ArrayList<String>();
        List<String> covariateList = createCovariateListAndFillFactors(data, trainingData, factorList);
        try {
            RegressionResults result = regr.regress();

            Matrix beta = new Matrix(result.getParameterEstimates(), 1);

            // The covariance matrix
            Matrix covMat = createCovarianceMatrix(result);

            LinearRegressionContent content =
                new LinearRegressionContent(m_outSpec, (int)stats[0].getN(), factorList, covariateList, beta,
                    m_includeConstant, m_offsetValue, covMat, result.getRSquared(), result.getAdjustedRSquared(), stats, null);

            return content;
        } catch (ModelSpecificationException e) {
            int dim = (m_includeConstant ? 1 : 0) + trainingData.getRegressorCount() + (factorList.size() > 0 ? Math.max(1, data.getDataTableSpec().getColumnSpec(factorList.get(0)).getDomain().getValues().size() - 1): 0);
            Matrix beta = new Matrix(1, dim);
            Matrix covMat = new Matrix(dim, dim);
            //fillWithNaNs(beta);
            fillWithNaNs(covMat);
            return new LinearRegressionContent(m_outSpec, (int)stats[0].getN(), factorList, covariateList, beta,
                m_includeConstant, m_offsetValue, covMat, Double.NaN, Double.NaN, stats, e.getMessage());
        }
    }

    /**
     * @param matrix
     */
    private void fillWithNaNs(final Matrix matrix) {
        for (int i = matrix.getRowDimension(); i-- > 0;) {
            for (int j = matrix.getColumnDimension(); j-- > 0;) {
                matrix.set(i, j, Double.NaN);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processTable(final int rowCount, final ExecutionMonitor exec,
        final RegressionTrainingData trainingData, final SummaryStatistics[] stats,
        final UpdatingMultipleLinearRegression regr) throws CanceledExecutionException {
        int r = 1;
        double progress = 0;
        for (RegressionTrainingRow row : trainingData) {
            exec.checkCanceled();
            if (!row.hasMissingCells()) {
                double[] parameter = row.getParameter().getArray()[0];
                for (int i = 0; i < trainingData.getRegressorCount(); i++) {
                    stats[i].addValue(parameter[i]);
                }
                regr.addObservation(parameter, row.getTarget() + (m_includeConstant? 0 : -m_offsetValue));
            }
            if (rowCount > 0) {
                double progressUpdate = r / (double)rowCount;
                // report progress if update above 1%
                if (progressUpdate - progress > 0.01) {
                    progress = progressUpdate;
                    exec.setProgress(progress);
                }
                r++;
            }
        }
    }
}
