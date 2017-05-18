/*
 * ------------------------------------------------------------------------
 *
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
 * ---------------------------------------------------------------------
 *
 * History
 *   28.03.2017 (Adrian): created
 */
package org.knime.base.node.mine.regression.logistic.learner4.sg;

import java.util.Arrays;

import org.knime.base.node.mine.regression.logistic.learner4.data.TrainingData;
import org.knime.base.node.mine.regression.logistic.learner4.data.TrainingRow;
import org.knime.base.node.mine.regression.logistic.learner4.sg.IndexCache.IndexIterator;

/**
 *
 * @author Adrian Nembach, KNIME.com
 */
final class LazySGOptimizer <T extends TrainingRow, U extends LazyUpdater<T>, R extends LazyRegularizationUpdater> extends AbstractSGOptimizer<T, U, R> {

    private int[] m_lastVisited;


    /**
     * @param data
     * @param loss
     * @param updaterFactory
     * @param prior
     * @param learningRateStrategy
     * @param stoppingCriterion
     */
    public LazySGOptimizer(final TrainingData<T> data, final Loss<T> loss, final UpdaterFactory<T, U> updaterFactory,
        final R prior, final LearningRateStrategy<T> learningRateStrategy,
        final StoppingCriterion<T> stoppingCriterion) {
        super(data, loss, updaterFactory, prior, learningRateStrategy, stoppingCriterion);
        m_lastVisited = new int[data.getFeatureCount()];
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void performUpdate(final T x, final U updater, final double[] gradient,
        final WeightVector<T> beta, final double stepSize, final int iteration, final IndexCache indexCache) {
        updater.update(x, gradient, beta, stepSize, iteration, indexCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IndexCache createIndexCache(final int nFets) {
        return new SuperLazyIndexCache();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareIteration(final WeightVector<T> beta, final T x, final U updater, final R regUpdater,
        final int iteration, final IndexCache indexCache) {
        // apply lazy updates
        updater.lazyUpdate(beta, x, indexCache, m_lastVisited, iteration);
        regUpdater.lazyUpdate(beta, indexCache, m_lastVisited, iteration);
        // update when present features were last visited
        for (IndexIterator iter = indexCache.getIterator(); iter.hasNext();) {
            m_lastVisited[iter.next()] = iteration;
        }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void postProcessEpoch(final WeightVector<T> beta, final U updater, final R regUpdater) {
        updater.resetJITSystem(beta, m_lastVisited);
        regUpdater.resetJITSystem(beta, m_lastVisited);
        Arrays.fill(m_lastVisited, 0);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void normalize(final WeightVector<T> beta, final U updater, final int iteration) {
        updater.normalize(beta, m_lastVisited, iteration);
        for (int i = 0; i < m_lastVisited.length; i++) {
            m_lastVisited[i] = iteration + 1;
        }
    }

}
