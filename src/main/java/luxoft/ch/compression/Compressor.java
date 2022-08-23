package luxoft.ch.compression;

import luxoft.ch.compression.model.Dictionary;

public class Compressor {

	private final Dictionary dictionary;

	public Compressor(String sourceFileName) {
		dictionary = new Dictionary();
		dictionary.initialize(sourceFileName);
	}

	public void extractTokens() {
		dictionary.extractTokens();
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public static void main(String... args) {
		Compressor compressor = new Compressor("source.txt");
		compressor.extractTokens();
		System.out.println(compressor.getDictionary());
	}

}
