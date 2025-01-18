package journal.model;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import journal.exceptions.JournalException;

public class JournalEntry {

	private String cipherText;
	private Date date;
	private String password;
	private String plainText;

	public JournalEntry(Date date, String plainText, String cipherText, String password) {
		this.date = date;
		this.plainText = plainText;
		this.cipherText = cipherText;
		this.password = password;
	}

	public String decrypt() throws JournalException {
		try {
			return Superstar.decrypt(cipherText, password);
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException
				| IllegalBlockSizeException e) {
			throw new JournalException("An error occurred decrypting the text.", e);
		}
	}

	public String encrypt() throws JournalException {
		try {
			return Superstar.encrypt(plainText, password);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new JournalException("An error occurred encrypting the text.", e);
		}
	}

	public String getCipherText() {
		return cipherText;
	}

	public Date getDate() {
		return date;
	}

	public String getPlainText() {
		return plainText;
	}

	public void setCipherText(String cipherText) {
		this.cipherText = cipherText;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}
}
