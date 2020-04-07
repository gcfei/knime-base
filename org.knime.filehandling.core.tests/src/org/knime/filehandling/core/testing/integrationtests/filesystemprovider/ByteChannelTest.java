package org.knime.filehandling.core.testing.integrationtests.filesystemprovider;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.knime.filehandling.core.testing.FSTestInitializer;
import org.knime.filehandling.core.testing.integrationtests.AbstractParameterizedFSTest;

/**
 * Test class for byte channel operations on file systems.
 * 
 * @author Tobias Urhaug, KNIME GmbH, Berlin, Germany
 *
 */
public class ByteChannelTest extends AbstractParameterizedFSTest {

	public ByteChannelTest(String fsType, FSTestInitializer testInitializer) {
		super(fsType, testInitializer);
	}

	@Test
	public void test_read_all() throws Exception {
		String testContent = "Some content to test this byte channel";
		Path file = m_testInitializer.createFileWithContent(testContent, "dir", "fileName");
		
		String result;
		try (SeekableByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			result = readFromByteChannel(byteChannel);
		}

		assertEquals(testContent, result);
	}
	
	@Test
	public void test_read_from_postition() throws Exception {
		String testContent = "!!!This starts at byte number 3!";
		Path file = m_testInitializer.createFileWithContent(testContent, "dir", "fileName");
		
		String result;
		try (SeekableByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			byteChannel.position(3);
			result = readFromByteChannel(byteChannel);
		}
		
		assertEquals("This starts at byte number 3!", result);
	}
	
	@Test
	public void test_truncate_less_than_channel_size_removes_trailing_content() throws Exception {
		String testContent = "After truncate (this is all gone)!";
		Path file = m_testInitializer.createFileWithContent(testContent, "dir", "fileName");
		
		String result;
		try (SeekableByteChannel byteChannel = 
				Files.newByteChannel(file, EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.READ))) {
			byteChannel.truncate(14);
			result = readFromByteChannel(byteChannel);
		}
		
		assertEquals("After truncate", result);
	}
	
	@Test
	public void test_truncate_greater_than_channel_size_does_not_change_it() throws Exception {
		String testContent = "After truncate (this is still there)!";
		Path file = m_testInitializer.createFileWithContent(testContent, "dir", "fileName");
		
		String result;
		try (SeekableByteChannel byteChannel = 
				Files.newByteChannel(file, EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.READ))) {
			byteChannel.truncate(143);
			result = readFromByteChannel(byteChannel);
		}
		
		assertEquals("After truncate (this is still there)!", result);
	}
	
	@Test
	public void test_channel_size() throws IOException {
		String testContent = "This has size 16";
		Path file = m_testInitializer.createFileWithContent(testContent, "dir", "fileName");
		
		try (SeekableByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			assertEquals(16, byteChannel.size());
		}
	}
	
	@Test
	public void test_seekable_position_can_be_set() throws IOException {
		String testContent = "Position will be changed!";
		Path file = m_testInitializer.createFileWithContent(testContent, "dir", "fileName");
		
		try (SeekableByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			assertEquals(0, byteChannel.position());
			byteChannel.position(7);
			assertEquals(7, byteChannel.position());
		}
	}

	@Test
	public void test_byte_channel_close() throws Exception {
		String testContent = "Channel will be closed!";
		Path file = m_testInitializer.createFileWithContent(testContent, "dir", "fileName");
		
		try (SeekableByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			assertEquals(true, byteChannel.isOpen());
			byteChannel.close();
			assertEquals(false, byteChannel.isOpen());
		}
	}
	
	@Test
	public void test_byte_channel_write() throws Exception {
		Path initiallyEmptyFile = m_testInitializer.createFile("dir", "fileName");
		
		String content = "A very useful sentence!";
		try (SeekableByteChannel byteChannel = 
				Files.newByteChannel(initiallyEmptyFile, EnumSet.of(StandardOpenOption.WRITE))) {
			byteChannel.write(ByteBuffer.wrap(content.getBytes()));
		}
		
		List<String> result = Files.readAllLines(initiallyEmptyFile);
		assertEquals(1, result.size());
		assertEquals(content, result.get(0));
	}
	
	@Test
	public void test_byte_channel_overwrite() throws Exception {
		String content = "Initial content";
		Path file = m_testInitializer.createFileWithContent(content, "dir", "fileName");
		
		String overwrittenContent = "Overwritten content!";
		try (SeekableByteChannel byteChannel = 
				Files.newByteChannel(file, EnumSet.of(StandardOpenOption.WRITE))) {
			byteChannel.write(ByteBuffer.wrap(overwrittenContent.getBytes()));
		}
		
		List<String> result = Files.readAllLines(file);
		assertEquals(1, result.size());
		assertEquals(overwrittenContent, result.get(0));
	}
	
	@Test
	public void test_create_new_file_and_write() throws Exception {
		Path directory = m_testInitializer.getRoot().resolve("dir");
		Files.createDirectories(directory);
		
		Path nonExistingFile = directory.resolve("non-existing");
		
		String contentToWrite = "A very useful sentence!";
		try (SeekableByteChannel byteChannel = 
			Files.newByteChannel(
				nonExistingFile, 
				EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
			)
		) {
			byteChannel.write(ByteBuffer.wrap(contentToWrite.getBytes()));
		}
		
		List<String> result = Files.readAllLines(nonExistingFile);
		assertEquals(1, result.size());
		assertEquals(contentToWrite, result.get(0));
	}
	
	private String readFromByteChannel(SeekableByteChannel byteChannel) throws IOException {
		String result = "";
		Charset charset = Charset.defaultCharset();
		ByteBuffer buffer = ByteBuffer.allocate(10);
		while (byteChannel.read(buffer) > 0) {
			buffer.flip();
			CharBuffer decode = charset.decode(buffer);
			result += decode.toString();
			buffer.clear();
		}
		return result;
	}
	
}
