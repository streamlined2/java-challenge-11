package luxoft.ch.compression;

import java.util.Comparator;
import luxoft.ch.compression.model.Stash.Range;
import luxoft.ch.compression.tool.Compressor;
import luxoft.ch.compression.tool.Decompressor;

public class Driver {

	public static void main(String[] args) {
		Compressor compressor = new Compressor("real-sample.txt");
		compressor.compress();
		System.out.println("sorted set of tokens:\n");
		compressor.getTokens(/*
								 * new Comparator<String>() {
								 * 
								 * @Override public int compare(String s1, String s2) { StringBuilder b1 = new
								 * StringBuilder(s1); b1.reverse(); StringBuilder b2 = new StringBuilder(s2);
								 * b2.reverse(); return b1.compareTo(b2); } }
								 */).forEach(System.out::println);
		// System.out.println("sorted set of tokens backwards:\n");
		// compressor.getTokens(Comparator.reverseOrder()).forEach(System.out::println);
		// System.out.println("sorted set of ranges:\n" + compressor.getRanges());
		compressor.save("compressed.data");
		Decompressor decompressor = new Decompressor("compressed.data");
		decompressor.decompress();
		decompressor.save("uncompressed.data");
	}

}
