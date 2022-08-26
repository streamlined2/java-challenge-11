package luxoft.ch.compression;

import luxoft.ch.compression.tool.Compressor;
import luxoft.ch.compression.tool.Decompressor;

public class Driver {

	public static void main(String[] args) {
		Compressor compressor = new Compressor("real-sample.txt");
		compressor.compress();
		//System.out.println("sorted set of tokens:\n" + compressor.getTokens());
		//System.out.println("sorted set of token entries:\n" + compressor.getTokenEntries());
		compressor.save("compressed.data");
		Decompressor decompressor = new Decompressor("compressed.data");
		decompressor.decompressAndSave("uncompressed.data");
	}

}
