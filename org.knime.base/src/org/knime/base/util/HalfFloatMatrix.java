/* Created on 06.02.2007 16:32:34 by thor
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2006
 * University of Konstanz, Germany.
 * Chair for Bioinformatics and Information Mining
 * Prof. Dr. Michael R. Berthold
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
 * ------------------------------------------------------------------- * 
 */
package org.knime.base.util;

/**
 * This stores half a matrix of floats efficiently in just one array. The access
 * function {@link #get(int, int)} works symmetrically. Upon creating the matrix
 * you can choose if place for the diagonal should be reserved or not.
 * 
 * @author Thorsten Meinl, University of Konstanz
 */
public final class HalfFloatMatrix {
    private final boolean m_withDiagonal;

    private final float[] m_matrix;

    /**
     * Creates a new half-matrix of floats.
     * 
     * @param rows the number of rows (and columns) in the matrix
     * @param withDiagonal <code>true</code> if the diagonal should be stored
     *            too, <code>false</code> otherwise
     */
    public HalfFloatMatrix(final int rows, final boolean withDiagonal) {
        m_withDiagonal = withDiagonal;
        if (withDiagonal) {
            m_matrix = new float[(rows * rows + rows) / 2];
        } else {
            m_matrix = new float[(rows * rows - rows) / 2];
        }
    }

    /**
     * Sets a value in the matrix. This function works symmetrically, i.e.
     * <code>set(i, j, 1)</code> is the same as <code>set(j, i, 1)</code>.
     * 
     * @param row the value's row
     * @param col the value's column
     * @param value the value
     */
    public void set(final int row, final int col, final float value) {
        if (row > col) {
            if (m_withDiagonal) {
                m_matrix[row * (row + 1) / 2 + col] = value;
            } else {
                m_matrix[row * (row - 1) / 2 + col] = value;
            }
        } else {
            if (m_withDiagonal) {
                m_matrix[col * (col + 1) / 2 + row] = value;
            } else {
                m_matrix[col * (col - 1) / 2 + row] = value;
            }
        }
    }

    /**
     * Returns a value in the matrix. This function works symmetrically, i.e.
     * <code>get(i, j)</code> is the same as <code>get(j, i)</code>.
     * 
     * @param row the value's row
     * @param col the value's column
     * @return the value
     */
    public float get(final int row, final int col) {
        if (row > col) {
            if (m_withDiagonal) {
                return m_matrix[row * (row + 1) / 2 + col];
            } else {
                return m_matrix[row * (row - 1) / 2 + col];
            }
        } else {
            if (m_withDiagonal) {
                return m_matrix[col * (col + 1) / 2 + row];
            } else {
                return m_matrix[col * (col - 1) / 2 + row];
            }
        }
    }

    /**
     * Fills the matrix with the given value.
     * 
     * @param value any value
     */
    public void fill(final float value) {
        for (int i = 0; i < m_matrix.length; i++) {
            m_matrix[i] = value;
        }
    }
}
