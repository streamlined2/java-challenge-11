package luxoft.ch.compression;

public class Driver {

	public static void main(String[] args) {
		Compressor compressor = new Compressor("source.txt");
		compressor.extractTokens();
		System.out.println(compressor.getDictionary());
		compressor.getDictionary().getStreamOfTokensSortedByTotalSpaceReversed().forEach(System.out::println);

	}

}
