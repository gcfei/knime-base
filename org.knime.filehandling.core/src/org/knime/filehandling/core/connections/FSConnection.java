package org.knime.filehandling.core.connections;

import java.io.IOException;

import org.knime.core.node.util.FileSystemBrowser;

/**
 * Interface for file system connections.
 *
 * @author Tobias Urhaug, KNIME GmbH, Berlin, Germany
 */
public interface FSConnection extends AutoCloseable {

    /**
     * Closes the file system in this connection and deregisters it from the {@link FSConnectionRegistry}.
     */
    @Override
    public default void close() {
        try (FSFileSystem<?> fileSystem = getFileSystem()) {
            fileSystem.ensureClosed();
            FSConnectionRegistry.getInstance().deregister(this);
        } catch (IOException ex) {
            // not sure what to do here...
        }
    }

	/**
	 * Returns a file system for this connection.
	 *
	 * @return a file system for this connection
	 */
	public FSFileSystem<?> getFileSystem();

	/**
	 * Returns a file system browser for this connection.
	 *
	 * @return a file system browser for this connection
	 */
	public FileSystemBrowser getFileSystemBrowser();


}
