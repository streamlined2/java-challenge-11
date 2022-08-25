package luxoft.ch.compression;

import luxoft.ch.compression.tool.Compressor;
import luxoft.ch.compression.tool.Decompressor;

public class Driver {

	public static void main(String[] args) {
		Compressor compressor = new Compressor("sample.txt");
		compressor.compress();
		compressor.save("compressed.data");
		Decompressor decompressor = new Decompressor("compressed.data");
		decompressor.decompressAndSave("uncompressed.data");
	}

}
