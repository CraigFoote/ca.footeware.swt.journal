package journal;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import journal.exceptions.JournalException;
import journal.model.Journal;
import journal.model.JournalEntry;
import journal.model.Superstar;

public class Main {

	private static Text content;
	private static DateTime dateTime;
	private static final String ERROR = "Error";
	private static File file;
	private static Journal journal;
	private static String password;
	private static Shell shell;
	private static Label messageLabel;

	protected static String composeDateStr() {
		int y = dateTime.getYear();
		int m = dateTime.getMonth() + 1; // zero-based
		int d = dateTime.getDay();
		Calendar cal = Calendar.getInstance();
		cal.set(y, m, d);

		String year = String.valueOf(y);
		String month = m < 10 ? "0" + m : String.valueOf(m);
		String day = d < 10 ? "0" + d : String.valueOf(d);
		return year + "-" + month + "-" + day;
	}

	private static void createJournal(String filename, String foldername) {
		String path = foldername + File.separator + filename;
		file = new File(path);
		if (file.exists()) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setText("Overwrite?");
			messageBox.setMessage("A file by that name already exists.\nDo you want to overwrite it?");
			int result = messageBox.open();
			if (result == SWT.NO) {
				file = null;
			} else {
				try {
					journal = new Journal(file);
					messageLabel.setText("Journal created.");
					content.setFocus();
				} catch (IOException | JournalException e) {
					showError("An error occurred creating the journal.\n" + e.getMessage());
				}
			}
		} else {
			try {
				boolean created = file.createNewFile();
				if (created) {
					journal = new Journal(file);
					messageLabel.setText("Journal created.");
					content.setFocus();
				}
			} catch (IOException | JournalException e) {
				showError("An error occurred creating the journal.\n" + e.getMessage());
			}
		}
	}

	private static void createNewTabItem(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Create New");

		Composite container = new Composite(tabFolder, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		Label namelabel = new Label(container, SWT.NONE);
		namelabel.setText("Name:");
		namelabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		Text filenameText = new Text(container, SWT.BORDER);
		filenameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label folderlabel = new Label(container, SWT.RIGHT);
		folderlabel.setText("Folder:");
		folderlabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Text folderText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		folderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(shell);
				dialog.setFilterPath(System.getProperty("user.dir"));
				String path = dialog.open();
				if (path != null) {
					folderText.setText(path);
				}
			}
		});

		Label passwordLabel1 = new Label(container, SWT.NONE);
		passwordLabel1.setText("Password:");
		passwordLabel1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		Text passwordText1 = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordText1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label passwordLabel2 = new Label(container, SWT.NONE);
		passwordLabel2.setText("Repeat Password:");
		passwordLabel2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Text passwordText2 = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordText2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Button createButton = new Button(container, SWT.PUSH);
		createButton.setText("Create");
		createButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		createButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = filenameText.getText().trim();
				String foldername = folderText.getText().trim();
				String password1 = passwordText1.getText(); // do not trim
				String password2 = passwordText2.getText(); // do not trim
				if (!filename.isEmpty() && !foldername.isEmpty() && !password1.isEmpty() && !password2.isEmpty()) {
					if (password1.equals(password2)) {
						password = password1; // or 2, whatever, I don't care anymore
						createJournal(filename, foldername);
						shell.setText(filename);
						messageLabel.setText("Journal created.");
					} else {
						showError("Passwords do not match.");
					}
				}
			}
		});

		tabItem.setControl(container);
	}

	private static void createOpenTabItem(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Open Existing");

		Composite container = new Composite(tabFolder, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		Label filenameLabel = new Label(container, SWT.NONE);
		filenameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		filenameLabel.setText("File:");

		Text filepathText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		filepathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				String path = dialog.open();
				if (path != null) {
					filepathText.setText(path);
				}
			}
		});

		Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		passwordLabel.setText("Password:");

		Text passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Button openButton = new Button(container, SWT.PUSH);
		openButton.setText("Open");
		openButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		openButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filepath = filepathText.getText().trim();
				password = passwordText.getText(); // do not trim
				if (!filepath.isEmpty() && !password.isEmpty()) {
					file = new File(filepath);
					if (!file.exists() || !file.canRead() || !file.canWrite()) {
						showError("Unable to open " + filepath);
					} else {
						try {
							loadJournalFromFile();
							shell.setText(file.getName());
							messageLabel.setText("Journal opened.");
						} catch (JournalException | IOException e1) {
							showError("Unable to open journal at " + filepath + ",\n" + e1.getMessage());
						}
					}
				}
			}
		});

		tabItem.setControl(container);
	}

	private static TabFolder createTabFolder(Shell shell) {
		TabFolder tabFolder = new TabFolder(shell, SWT.BORDER_SOLID);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		tabFolder.setLayout(new GridLayout(2, false));

		createOpenTabItem(tabFolder);
		createNewTabItem(tabFolder);

		return tabFolder;
	}

	protected static String getDecrypted(String encrypted) {
		try {
			return Superstar.decrypt(encrypted, password);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e1) {
			showError("An error occurred selecting the journal entry.\n" + e1.getMessage());
			return "";
		}
	}

	protected static void loadJournalFromFile() throws JournalException, IOException {
		// get date from dateTime widget
		int year = dateTime.getYear();
		int month = dateTime.getMonth();
		int day = dateTime.getDay();
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		Date date = cal.getTime();
		journal = new Journal(file);
		String dateStr = journal.dateFormat.format(date);

		// get value for dateStr key
		Properties props = journal.getProperties();
		if (props.isEmpty()) {
			throw new JournalException("No properties found.");
		}

		Object object = props.get(dateStr);
		if (object instanceof String cipherText) {
			JournalEntry entry = new JournalEntry(date, null, cipherText, password);
			String plainText = entry.decrypt();
			entry.setPlainText(plainText);
			content.setText(plainText);
		}
	}

	public static void main(String[] args) {
		Display.setAppName("ca.footeware.swt.journal");
		Display display = new Display();
		shell = new Shell(display);
		shell.setText("Journal");
		shell.setSize(900, 600);
		shell.setLayout(new GridLayout(2, false));

		TabFolder tabFolder = createTabFolder(shell);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		dateTime = new DateTime(shell, SWT.CALENDAR | SWT.BORDER_SOLID);
		dateTime.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		dateTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (journal == null) {
					return;
				}
				String dateStr = composeDateStr();
				Object value = journal.getProperties().get(dateStr);
				if (value instanceof String encrypted) {
					String decrypted = getDecrypted(encrypted);
					content.setText(decrypted);
				} else {
					content.setText("");
				}
				content.setFocus();
			}
		});

		content = new Text(shell, SWT.MULTI | SWT.BORDER_SOLID | SWT.H_SCROLL | SWT.V_SCROLL);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		content.addModifyListener(_ -> {
			String text = shell.getText();
			if (!text.startsWith("• ")) {
				shell.setText("• " + shell.getText());
			}
		});

		messageLabel = new Label(shell, SWT.NONE);
		messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Button saveButton = new Button(shell, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveJournal();
			}
		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	protected static void saveJournal() {
		if (journal == null) {
			return;
		}
		String plainText = content.getText();
		Calendar cal = Calendar.getInstance();
		cal.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
		try {
			JournalEntry entry = new JournalEntry(cal.getTime(), plainText, null, password);
			journal.addEntry(entry);
			String title = shell.getText();
			if (title.startsWith("• ")) {
				shell.setText(title.substring(2));
			}
			messageLabel.setText("Journal saved.");
		} catch (JournalException e1) {
			showError("An error occurred saving the journal.\n" + e1.getMessage());
		}
	}

	protected static void showError(String string) {
		messageLabel.setText("");
		MessageBox messageBox = new MessageBox(shell, SWT.ERROR);
		messageBox.setText(ERROR);
		messageBox.setMessage(string);
		messageBox.open();
	}
}
