package luxoft.ch.compression.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Dictionary {

	private final Map<String, List<Integer>> tokens;

	public Dictionary() {
		tokens = new HashMap<>();
	}

	public void addTokenEntry(String token, Integer entryIndex) {
		var entries = tokens.get(token);
		if (entries == null) {
			entries = new ArrayList<>();
		}
		entries.add(entryIndex);
		tokens.put(token, entries);
	}

	public void deleteSolitaries() {
		for (var iter = tokens.entrySet().iterator(); iter.hasNext();) {
			var entry = iter.next();
			if (entry.getValue().size() == 1) {
				iter.remove();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		for (var entry : tokens.entrySet()) {
			builder.append("  token: ").append(entry.getKey()).append(", indices: ");
			StringJoiner join = new StringJoiner(",", "[", "]");
			for (var index : entry.getValue()) {
				join.add(Integer.toString(index));
			}
			builder.append(join.toString()).append("\n");
		}
		builder.append("}\n");
		return builder.toString();
	}

}
