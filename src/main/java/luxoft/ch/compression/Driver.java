package luxoft.ch.compression;

import luxoft.ch.compression.tool.Compressor;

public class Driver {

	public static void main(String[] args) {
		Compressor compressor = new Compressor("source.txt");
		compressor.process();
		System.out.println(compressor.getDictionary());
		compressor.getDictionary().getTokensByTotalSpaceReversed().forEach(System.out::println);
		System.out.println();
		compressor.getTokens().forEach(System.out::println);
		System.out.println();
		compressor.getTokenChain().values().forEach(System.out::println);
	}

}
