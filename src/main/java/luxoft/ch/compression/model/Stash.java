package luxoft.ch.compression.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

	private final Map<String, int[]> tokenEntries;
	private final List<char[]> uncompressedRanges;

	public Stash() {
		tokenEntries = new HashMap<>();
		uncompressedRanges = new ArrayList<>();
	}

	public Iterator<char[]> getUncompressedRangesIterator() {
		return uncompressedRanges.iterator();
	}

	public String findTokenByStartPosition(int start) {
		for (var entry : tokenEntries.entrySet()) {
			OptionalInt pos = IntStream.of(entry.getValue()).filter(position -> position == start).findFirst();
			if (pos.isPresent()) {
				return entry.getKey();
			}
		}
		throw new CompressionException("proper token not found for position %d".formatted(start));
	}

	public SortedSet<String> getTokens() {
		return tokenEntries.keySet().stream().collect(Collectors.toCollection(TreeSet::new));
	}

	public SortedSet<Range> getRanges() {
		return tokenEntries.entrySet().stream().flatMap(this::entryIndicesStream).sorted()
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
			for (var start : entry.getValue()) {
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

	private boolean isIntersected(int startPosition, int endPosition, int start, int end) {
		return end >= startPosition && endPosition >= start;
	}

}
