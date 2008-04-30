/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   Mar 25, 2008 (sellien): created
 */
package org.knime.base.util.coordinate;

import org.knime.core.data.DataColumnSpec;

/**
 *
 * @author sellien, University of Konstanz
 */
public class IntegerCoordinateFactory implements CoordinateFactory {

    /**
     * {@inheritDoc}
     */
    public Coordinate createCoordinate(final DataColumnSpec columnSpec) {
        return new IntegerCoordinate(columnSpec);
    }

}
