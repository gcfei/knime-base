/*
 * ------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 * 
 * History
 *   17.02.2005 (ohl): created
 */
package org.knime.base.node.io.arffwriter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeView;

/**
 * 
 * @author Peter Ohl, University of Konstanz
 */
public class ARFFWriterNodeFactory extends NodeFactory {

    private final String m_file;

    /**
     * New factory - no default file.
     */
    public ARFFWriterNodeFactory() {
        m_file = null;
    }

    /**
     * New ARFF factory with default output file.
     * 
     * @param defFile the default file to write to
     */
    public ARFFWriterNodeFactory(final String defFile) {
        m_file = defFile;
    }

    /**
     * @see NodeFactory#createNodeDialogPane()
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ARFFWriterNodeDialog();
    }

    /**
     * @see org.knime.core.node.NodeFactory#createNodeModel()
     */
    @Override
    public NodeModel createNodeModel() {
        if (m_file == null) {
            return new ARFFWriterNodeModel();
        } else {
            return new ARFFWriterNodeModel(m_file);
        }
    }

    /**
     * @see NodeFactory#createNodeView(int, NodeModel)
     */
    @Override
    public NodeView createNodeView(final int viewIndex,
            final NodeModel nodeModel) {
        return null;
    }

    /**
     * @see org.knime.core.node.NodeFactory#getNrNodeViews()
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * @see org.knime.core.node.NodeFactory#hasDialog()
     */
    @Override
    public boolean hasDialog() {
        return true;
    }
}
