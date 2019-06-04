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
 *   Jun 3, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.meta.explain;

import org.knime.core.node.util.CheckUtils;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public abstract class AbstractExplanation implements Explanation {

    protected final String m_roiKey;

    protected final Matrix m_explanationValues;

    protected final int m_numTargets;

    protected final int m_numFeatures;

    /**
     * @param roiKey
     * @param explanationValues
     * @param numTargets
     * @param numFeatures
     *
     */
    public AbstractExplanation(final String roiKey, final Matrix explanationValues, final int numTargets,
        final int numFeatures) {
        m_roiKey = roiKey;
        m_numFeatures = numFeatures;
        m_numTargets = numTargets;
        m_explanationValues = explanationValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoiKey() {
        return m_roiKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFeatures() {
        return m_numFeatures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTargets() {
        return m_numTargets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getExplanationValue(final int target, final int feature) {
        return m_explanationValues.get(target, feature);
    }

    /**
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    protected abstract static class AbstractExplanationBuilder {
        private final String m_roiKey;

        private final Matrix m_explanationValues;

        private final int m_numTargets;

        private final int m_numFeatures;

        private boolean m_built = false;

        /**
         * @param roiKey
         * @param numTargets
         * @param numFeatures
         */
        protected AbstractExplanationBuilder(final String roiKey, final int numTargets, final int numFeatures) {
            m_roiKey = roiKey;
            m_numTargets = numTargets;
            m_numFeatures = numFeatures;
            m_explanationValues = new Matrix(numTargets, numFeatures, "target", "feature");
        }

        protected final String getRoiKey() {
            return m_roiKey;
        }

        protected final int getNumFeatures() {
            return m_numFeatures;
        }

        protected final int getNumTargets() {
            return m_numTargets;
        }

        protected final Matrix getExplanationValues() {
            return m_explanationValues;
        }

        protected final void checkBuilt() {
            CheckUtils.checkState(!m_built, "The build method can be only called once.");
            m_built = true;
        }

        /**
         * Sets an explanation value for <b>target</b> and <b>feature</b>. Must not be called after the build method has
         * been called!
         *
         * @param target the target for which the explanationValue is meant
         * @param feature the feature for which the explanationValue is meant
         * @param explanationValue the actual value of <b>feature</b> for <b>target</b>
         * @throws IllegalStateException if called after the build method has been called
         */
        public void setExplanationValue(final int target, final int feature, final double explanationValue) {
            CheckUtils.checkState(!m_built,
                "The builder can no longer be modified after the build method has been called.");
            m_explanationValues.set(target, feature, explanationValue);
        }
    }

}