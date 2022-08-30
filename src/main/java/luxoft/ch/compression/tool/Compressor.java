package luxoft.ch.compression.tool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import luxoft.ch.compression.CompressionException;
import luxoft.ch.compression.model.Dictionary;
import luxoft.ch.compression.model.Stash;
import luxoft.ch.compression.model.Stash.Range;

public class Compressor {

	private static final int MIN_TOKEN_LENGTH = 4;
	private static final int MIN_TOKEN_ENTRY_COUNT = 2;
	private static final int MIN_NUMBER_OF_TOKEN_INDICES = 50;

	private final Dictionary dictionary;
	private final Stash stash;

	public Compressor(String sourceFileName) {
		stash = new Stash();
		dictionary = new Dictionary();
		dictionary.initialize(sourceFileName);
	}

	public SortedSet<String> getTokens() {
		return stash.getTokens();
	}

	public SortedSet<Range> getRanges() {
		return stash.getRanges();
	}

	public void save(String targetFileName) {
		try (ObjectOutputStream outStream = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(new File(targetFileName))))) {
			outStream.writeObject(stash);
		} catch (IOException e) {
			throw new CompressionException("cannot open file %s".formatted(targetFileName), e);
		}
	}

	public void compress() {
		dictionary.growLargerTokens();
		formSetOfTokensAndChain();
		collectUncompressedData();
	}

	private void collectUncompressedData() {
		List<char[]> uncompressedRanges = new ArrayList<>();
		int start = 0;
		for (var compressedRange : stash.getRanges()) {
			uncompressedRanges.add(dictionary.getChars(start, compressedRange.start() - start));
			start = compressedRange.end() + 1;
		}
		uncompressedRanges.add(dictionary.getChars(start));
		stash.addUncompressedData(uncompressedRanges);
	}

	private void formSetOfTokensAndChain() {
		for (var iter = dictionary.getTokensByTotalSpaceReversed(MIN_TOKEN_LENGTH).iterator(); iter.hasNext();) {
			Entry<String, List<Integer>> token = iter.next();
			int tokenEntryCount = 0;
			var indices = new ArrayList<Integer>(MIN_NUMBER_OF_TOKEN_INDICES);
			for (var startPosition : token.getValue()) {
				final int endPosition = getEndPosition(token, startPosition);
				if (stash.isTokenEntryMayBeApplied(startPosition, endPosition)) {
					indices.add(startPosition);
					tokenEntryCount++;
				}
			}
			if (tokenEntryCount >= MIN_TOKEN_ENTRY_COUNT) {
				stash.add(token.getKey(), indices);
			}
		}
	}

	private int getEndPosition(Entry<String, List<Integer>> token, int startPosition) {
		return startPosition + token.getKey().length() - 1;
	}

}
