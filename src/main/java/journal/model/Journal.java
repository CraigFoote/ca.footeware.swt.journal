package journal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import journal.exceptions.JournalException;

public class Journal {

	public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private File file;
	private Properties properties;

	public Journal(File file) throws IOException, JournalException {
		if (file == null || !file.exists() || !file.canRead() || !file.canRead()) {
			throw new IllegalArgumentException("Invalid file.");
		}
		this.file = file;
		this.properties = new Properties();
		try (final var in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			this.properties.load(in);
		} catch (IllegalArgumentException e) {
			throw new JournalException("Unable to create journal. \n" + e.getMessage(), e);
		}
	}

	public void addEntry(JournalEntry entry) throws JournalException {
		Date date = entry.getDate();
		String dateString = dateFormat.format(date);
		try {
			properties.setProperty(dateString, entry.encrypt());
			save();
		} catch (IOException e) {
			throw new JournalException("An error occurred adding an entry.", e);
		}
	}

	public Properties getProperties() {
		return properties;
	}

	private void save() throws IOException {
		try (final var out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			properties.store(out, null);
		}
	}
}
