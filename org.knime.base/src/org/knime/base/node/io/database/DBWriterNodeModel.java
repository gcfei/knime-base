/*
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
 * -------------------------------------------------------------------
 * 
 * History
 *   21.08.2005 (gabriel): created
 */
package org.knime.base.node.io.database;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.HashSet;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * 
 * @author Thomas Gabriel, University of Konstanz
 */
class DBWriterNodeModel extends NodeModel {
    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(DBWriterNodeModel.class);

    private String m_driver = "sun.jdbc.odbc.JdbcOdbcDriver";

    private String m_url = "jdbc:odbc:<database_name>";

    private String m_user = "<user>";

    private String m_pass = "";
    
    private String m_table = "<table_name>";

    private final HashSet<String> m_driverLoaded = new HashSet<String>();
    
    static {        
        try {
            Class<?> driverClass = Class.forName(
                    "sun.jdbc.odbc.JdbcOdbcDriver");
            Driver theDriver = new WrappedDriver((Driver)driverClass
                    .newInstance());
            DriverManager.registerDriver(theDriver);
        } catch (Exception e) {
            LOGGER.warn("Could not load 'sun.jdbc.odbc.JdbcOdbcDriver'.");
            LOGGER.debug("", e);
        }
    }

    /**
     * Creates a new model with one data outport.
     */
    DBWriterNodeModel() {
        super(1, 0);
        reset();
    }

    /**
     * @see NodeModel#saveSettingsTo(NodeSettingsWO)
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addString("driver", m_driver);
        settings.addString("database", m_url);
        settings.addString("user", m_user);
        settings.addStringArray("loaded_driver", m_driverLoaded
                .toArray(new String[0]));
        settings.addString("table", m_table);
    }

    /**
     * @see NodeModel#validateSettings(NodeSettingsRO)
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        loadSettings(settings, false);
    }

    /**
     * @see NodeModel#loadValidatedSettingsFrom(NodeSettingsRO)
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        loadSettings(settings, true);
    }

    private void loadSettings(final NodeSettingsRO settings,
            final boolean write)
            throws InvalidSettingsException {
        String driver = settings.getString("driver");
        String database = settings.getString("database");
        String user = settings.getString("user");
        String table = settings.getString("table");
        // password
        String password = settings.getString("password", "");
        // loaded driver
        String[] loadedDriver = settings.getStringArray("loaded_driver");
        try {
            password = DBReaderConnection.decrypt(password);
        } catch (Exception e) {
            throw new InvalidSettingsException("Could not decrypt password", e);
        }
        if (write) {
            m_driver = driver;
            m_url = database;
            m_user = user;
            m_pass = password;
            m_table = table;
            m_driverLoaded.clear();
            m_driverLoaded.addAll(Arrays.asList(loadedDriver));
            for (String fileName : m_driverLoaded) {
                try {
                    DBDriverLoader.loadDriver(new File(fileName));
                } catch (Exception e) {
                    LOGGER.info("Could not load driver from: " + loadedDriver);
                }
            }
        }
    }

    /**
     * @see NodeModel#execute(BufferedDataTable[], ExecutionContext)
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws CanceledExecutionException,
            Exception {
        exec.setProgress(-1, "Opening database connection...");
        Connection conn = null;
        try {
            // create database connection
            conn = DriverManager.getConnection(m_url, m_user, m_pass);
            // write entire data
            new DBWriterConnection(conn, m_url, m_table, inData[0], exec);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return new BufferedDataTable[0];
    }

    /**
     * @see org.knime.core.node.NodeModel#reset()
     */
    @Override
    protected void reset() {
    }

    /**
     * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
     *      ExecutionMonitor)
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) {

    }

    /**
     * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
     *      ExecutionMonitor)
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) {

    }

    /**
     * @see NodeModel#configure(org.knime.core.data.DataTableSpec[])
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        // bugfix 730, don't create database connection during configure, since
        // it is time consuming and it can't be ensured that connection will 
        // still be valid during execute
        
        // throw exception if no data provided
        if (inSpecs[0].getNumColumns() == 0) {
            throw new InvalidSettingsException("No columns in input data.");
        }
        
        // print warning if password is empty
        if (m_pass == null || m_pass.length() == 0) {
            super.setWarningMessage(
                    "Please check if you need to set a password.");
        }
        
        return new DataTableSpec[0];
    }
}
