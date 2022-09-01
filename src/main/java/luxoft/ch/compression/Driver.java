package luxoft.ch.compression;

import luxoft.ch.compression.model.Stash;
import luxoft.ch.compression.tool.Compressor;
import luxoft.ch.compression.tool.Decompressor;

public class Driver {

	public static void main(String[] args) {
		Compressor compressor = new Compressor("In a grove.txt");
		compressor.compress();
		Stash.Statistics stats = compressor.getStatistics();
		System.out.println("tokens count: " + stats.tokenCount());
		System.out.println("tokens total size (chars): " + stats.tokenSize());
		System.out.println("entries count: " + stats.entryCount());
		System.out.println("entries total size (chars): " + stats.entrySize());
		System.out.println("uncompressed ranges count: " + stats.uncompressedCount());
		System.out.println("uncompressed ranges size (chars): " + stats.uncompressedSize());
		// System.out.println("sorted set of tokens:\n");
		// compressor.getTokens().forEach(System.out::println);
		// System.out.println("sorted set of ranges:\n" + compressor.getRanges());
		compressor.save("compressed.data");
		Decompressor decompressor = new Decompressor("compressed.data");
		decompressor.decompress();
		decompressor.save("uncompressed.data");
	}

}
