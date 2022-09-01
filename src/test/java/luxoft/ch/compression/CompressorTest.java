package luxoft.ch.compression;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import luxoft.ch.compression.tool.Compressor;
import luxoft.ch.compression.tool.Decompressor;

class CompressorTest {

	@Test
	void test() {
		byte[] input = null;
		try {
			input = Files.readAllBytes(Paths.get("In a grove.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Original file size =" + input.length);
		Compressor compressor = new Compressor("In a grove.txt");
		compressor.compress();
		compressor.save("compressed.txt");
		byte[] compressed = null;
		try {
			compressed = Files.readAllBytes(Paths.get("compressed.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Compressed file size =" + compressed.length);
		Decompressor decompressor = new Decompressor("compressed.txt");
		decompressor.decompress();
		decompressor.save("deompressed.txt");
		byte[] decompressed = null;
		try {
			decompressed = Files.readAllBytes(Paths.get("deompressed.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Decompressed file size =" + decompressed.length);
		assertEquals(new String(input), new String(decompressed));
		assertArrayEquals(input, decompressed);
	}

}
