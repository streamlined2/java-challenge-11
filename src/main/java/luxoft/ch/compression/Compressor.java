package luxoft.ch.compression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import luxoft.ch.compression.model.Dictionary;

public class Compressor {

	private static final int BUFFER_CAPACITY = 1024 * 1024;

	private final String sourceFileName;

	public Compressor(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	private Dictionary initialize() {
		Dictionary tokens = new Dictionary();
		CharBuffer buffer = CharBuffer.allocate(BUFFER_CAPACITY);
		try (Reader reader = new BufferedReader(
				new FileReader(new File(getClass().getClassLoader().getResource(sourceFileName).toURI())))) {
			reader.read(buffer);
			buffer.flip();
			char[] tokenData = new char[2];
			final int size = buffer.limit() - tokenData.length + 1;
			for (int index = 0; index < size; index++) {
				buffer.get(index, tokenData);
				tokens.addTokenEntry(String.valueOf(tokenData), index);
			}
			tokens.deleteSolitaries();
		} catch (IOException | URISyntaxException e) {
			throw new CompressionException("cannot open file %s".formatted(sourceFileName), e);
		}
		return tokens;
	}

	public static void main(String... args) {
		Dictionary tokens = new Compressor("source.txt").initialize();
		System.out.println(tokens);
	}

}
