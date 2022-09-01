package luxoft.ch.compression.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import luxoft.ch.compression.CompressionException;

public class Stash implements Iterable<Map.Entry<String, int[]>>, Serializable {

	public record Range(String token, int start, int end) implements Comparable<Range> {

		public Range {
			if (token == null || token.isEmpty())
				throw new IllegalArgumentException("token shouldn't be empty or null");
			if (start > end)
				throw new IllegalArgumentException(
						"starting index %d must be less or equal than ending index %d".formatted(start, end));
		}

		@Override
		public int compareTo(Range range) {
			return Integer.compare(start, range.start);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Range range) {
				return compareTo(range) == 0;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return start;
		}

		@Override
		public String toString() {
			return "%s [%d,%d]".formatted(token, start, end);
		}

	}

	public record Statistics(int tokenCount, int tokenSize, int entryCount, int entrySize, int uncompressedCount,
			int uncompressedSize) {
	}

	public Statistics getStatistics() {
		return new Statistics(getTokenCount(), getTokenSize(), getEntryCount(), getEntrySize(), getUncompressedCount(),
				getUncompressedSize());
	}

	private int getTokenCount() {
		return tokenEntries.size();
	}

	private int getTokenSize() {
		int size = 0;
		for (var key : tokenEntries.keySet()) {
			size += key.length();
		}
		return size;
	}

	private int getEntryCount() {
		int count = 0;
		for (var entry : tokenEntries.entrySet()) {
			count += entry.getValue().length;
		}
		return count;
	}

	private int getEntrySize() {
		int size = 0;
		for (var entry : tokenEntries.entrySet()) {
			size += entry.getValue().length * entry.getKey().length();
		}
		return size;
	}

	private int getUncompressedCount() {
		return uncompressedRanges.size();
	}

	private int getUncompressedSize() {
		int size = 0;
		for (var range : uncompressedRanges) {
			size += range.length;
		}
		return size;
	}

	private Map<String, int[]> tokenEntries;
	private List<char[]> uncompressedRanges;

	public Stash() {
		tokenEntries = new HashMap<>();
		uncompressedRanges = new ArrayList<>();
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		int tESize = stream.readShort();
		tokenEntries = new HashMap<>(tESize);
		for (int k = tESize; k > 0; k--) {
			String key = stream.readUTF();
			int entryCount = stream.readByte();
			int[] entries = new int[entryCount];
			for (int m = 0; m < entryCount; m++) {
				entries[m] = stream.readShort();
			}
			tokenEntries.put(key, entries);
		}
		int uRSize = stream.readShort();
		uncompressedRanges = new ArrayList<>(uRSize);
		for (int k = uRSize; k > 0; k--) {
			String data = stream.readUTF();
			uncompressedRanges.add(data.toCharArray());
		}
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeShort(tokenEntries.size());
		for (var entry : tokenEntries.entrySet()) {
			stream.writeUTF(entry.getKey());
			stream.writeByte(entry.getValue().length);
			for (var index : entry.getValue()) {
				stream.writeShort(index);
			}
		}
		stream.writeShort(uncompressedRanges.size());
		for (var range : uncompressedRanges) {
			StringBuilder sb = new StringBuilder();
			for (var ch : range) {
				sb.append(ch);
			}
			stream.writeUTF(sb.toString());
		}
	}

	public Iterator<char[]> getUncompressedRangesIterator() {
		return uncompressedRanges.iterator();
	}

	public String findTokenByStartPosition(int start) {
		for (var entry : tokenEntries.entrySet()) {
			if (Arrays.binarySearch(entry.getValue(), start) >= 0) {
				return entry.getKey();
			}
		}
		throw new CompressionException("proper token not found for position %d".formatted(start));
	}

	public SortedSet<String> getTokens() {
		return getTokens(Comparator.naturalOrder());
	}

	public SortedSet<String> getTokens(Comparator<String> comparator) {
		return tokenEntries.keySet().stream().sorted(comparator).collect(Collectors.toCollection(TreeSet::new));
	}

	public SortedSet<Range> getRanges() {
		return getRanges(Comparator.naturalOrder());
	}

	public SortedSet<Range> getRanges(Comparator<Range> comparator) {
		return tokenEntries.entrySet().stream().flatMap(this::entryIndicesStream).sorted(comparator)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	private Stream<Range> entryIndicesStream(Map.Entry<String, int[]> entry) {
		final int tokenLength = entry.getKey().length();
		final Builder<Range> builder = Stream.builder();
		for (int k = 0; k < entry.getValue().length; k++) {
			final int startIndex = entry.getValue()[k];
			final int endIndex = startIndex + tokenLength - 1;
			builder.accept(new Range(entry.getKey(), startIndex, endIndex));
		}
		return builder.build();
	}

	@Override
	public Iterator<Map.Entry<String, int[]>> iterator() {
		return tokenEntries.entrySet().iterator();
	}

	public void add(String token, List<Integer> indices) {
		tokenEntries.put(token, indices.stream().mapToInt(Integer::intValue).toArray());
	}

	public void addUncompressedData(List<char[]> data) {
		uncompressedRanges.addAll(data);
	}

	public boolean isTokenEntryMayBeApplied(int startPosition, int endPosition) {
		for (var entry : tokenEntries.entrySet()) {
			final int tokenLength = entry.getKey().length();
			final int startIndex = getStartIndex(entry.getValue(), startPosition);
			for (int index = startIndex; index < entry.getValue().length; index++) {
				final int start = entry.getValue()[index];
				final int end = start + tokenLength - 1;
				if (start > endPosition) {
					break;
				}
				if (isIntersected(startPosition, endPosition, start, end)) {
					return false;
				}
			}
		}
		return true;
	}

	private int getStartIndex(int[] indices, int key) {
		int startIndex = Arrays.binarySearch(indices, key);
		if (startIndex >= 0) {
			return startIndex;
		}
		return Math.max(-startIndex - 2, 0);
	}

	private boolean isIntersected(int startPosition, int endPosition, int start, int end) {
		return end >= startPosition && endPosition >= start;
	}

}
