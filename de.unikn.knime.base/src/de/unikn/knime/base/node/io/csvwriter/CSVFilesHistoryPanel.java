/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   Dec 17, 2005 (wiswedel): created
 */
package de.unikn.knime.base.node.io.csvwriter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.unikn.knime.core.node.util.StringHistory;

/**
 * Panel that contains an editable Combo Box showing the file to write to and
 * a button to trigger a file chooser. The elements in the combo are files 
 * that have been recently used.
 * 
 * @see de.unikn.knime.core.node.util.StringHistory
 * @author wiswedel, University of Konstanz
 */
public final class CSVFilesHistoryPanel extends JPanel {
    
    private final JComboBox m_textBox;
    private final JButton m_chooseButton;

    /** Creates new instance, sets properties, for instance renderer, 
     * accordingly.
     */
    public CSVFilesHistoryPanel() {
        m_textBox = new JComboBox(new DefaultComboBoxModel());
        m_textBox.setEditable(true);
        m_textBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        m_textBox.setRenderer(new MyComboBoxRenderer());
        m_chooseButton = new JButton("...");
        m_chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // file chooser triggered by choose button 
                final JFileChooser fileChooser = new JFileChooser();
                String f = m_textBox.getEditor().getItem().toString();
                File dirOrFile = getFile(f);
                if (dirOrFile.isDirectory()) {
                    fileChooser.setCurrentDirectory(dirOrFile);
                } else {
                    fileChooser.setSelectedFile(dirOrFile);
                }
                int r = fileChooser.showDialog(
                        CSVFilesHistoryPanel.this, "Apply");
                if (r == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    m_textBox.setSelectedItem(file.getAbsoluteFile());
                }
            }
        });
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(m_textBox);
        add(m_chooseButton);
        updateHistory();
    }
    
    /**
     * Get currently selected file.
     * @return The current file url.
     * @see javax.swing.JComboBox#getSelectedItem()
     */
    public String getSelectedFile() {
        return (String)m_textBox.getEditor().getItem().toString();
    }

    /**
     * Set the file url as default.
     * @param url The file to choose.
     * @see javax.swing.JComboBox#setSelectedItem(java.lang.Object)
     */
    public void setSelectedFile(final String url) {
        m_textBox.setSelectedItem(url);
    }

    /** Updates the elements in the combo box, reads the file history. */
    public void updateHistory() {
        StringHistory history = StringHistory.getInstance(
                CSVWriterNodeModel.FILE_HISTORY_ID);
        String[] allVals = history.getHistory();
        LinkedHashSet<String> list = new LinkedHashSet<String>();
        for (int i = 0; i < allVals.length; i++) {
            try {
                String cur = allVals[i];
                File file = textToFile(cur);
                list.add(file.getAbsolutePath());
            } catch (MalformedURLException mue) {
                continue;
            }
        }
        DefaultComboBoxModel comboModel = 
            (DefaultComboBoxModel)m_textBox.getModel();
        comboModel.removeAllElements();
        for (Iterator it = list.iterator(); it.hasNext();) {
            comboModel.addElement(it.next());
        }
        // changing the model will also change the minimum size to be
        // quite big. We have tooltips, we don't need that
        Dimension newMin = new Dimension(0, getPreferredSize().height);
        setMinimumSize(newMin);
    }
    
    /**
     * Tries to create a File from the passed string.
     * 
     * @param url the string to transform into an File
     * @return File if entered value could be properly tranformed, or
     * @throws MalformedURLException if the value passed was invalid
     */
    private static File textToFile(final String url) 
        throws MalformedURLException {
        if ((url == null) || (url.equals(""))) {
            throw new MalformedURLException("Specify a not empty valid URL");
        }

        String file;
        try {
            URL newURL = new URL(url);
            file = newURL.getFile();
        } catch (MalformedURLException e) {
            // see if they specified a file without giving the protocol
            return new File(url);
        }
        if (file == null || file.equals("")) {
            throw new MalformedURLException(
                    "Can't get file from file '" + url + "'");
        }
        return new File(file);
    }

    /** Return a file object for the given fileName. It makes sure that if
     * the fileName is not absolute it will be relative to the user's home dir.
     * @param fileName The file name to convert to a file.
     * @return A file representing fileName.
     */
    public static final File getFile(final String fileName) {
        File f = new File(fileName);
        if (!f.isAbsolute()) {
            f = new File(new File(System.getProperty("user.home")), fileName);
        }
        return f;
    }
    
    /** renderer that also supports to show customized tooltip. */
    private static class MyComboBoxRenderer extends BasicComboBoxRenderer {
        /**
         * @see BasicComboBoxRenderer#getListCellRendererComponent(
         * javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(final JList list, 
                final Object value, final int index, final boolean isSelected, 
                final boolean cellHasFocus) {
            if (index > -1) {
                list.setToolTipText(value.toString());
            }
            return super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
        }  
    }

}
