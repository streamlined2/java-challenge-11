package luxoft.ch.compression.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Writer;

import luxoft.ch.compression.CompressionException;
import luxoft.ch.compression.model.Stash;

public class Decompressor {

	private final String sourceFileName;
	private Stash stash;

	public Decompressor(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public void decompress() {
		try (ObjectInputStream inStream = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(new File(sourceFileName))))) {
			stash = (Stash) inStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new CompressionException("cannot open file %s".formatted(sourceFileName), e);
		}
	}

	public void save(String targetFileName) {
		if (stash == null)
			throw new IllegalStateException("must decompress source data before saving result");
		try (Writer writer = new PrintWriter(
				new BufferedOutputStream(new FileOutputStream(new File(targetFileName))))) {
			int position = 0;
			for (var rangeIter = stash.getUncompressedRangesIterator(); rangeIter.hasNext();) {
				char[] range = rangeIter.next();
				writer.write(String.valueOf(range));
				position += range.length;
				if (rangeIter.hasNext()) {
					String token = stash.findTokenByStartPosition(position);
					writer.write(token);
					position += token.length();
				}
			}
		} catch (IOException e) {
			throw new CompressionException("cannot open file %s".formatted(targetFileName), e);
		}
	}

}
