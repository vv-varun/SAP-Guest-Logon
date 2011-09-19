package org.varunverma.sapguestlogon.Application;

public class EncryptionError extends ApplicationException {

	private static final long serialVersionUID = 6536757901707637662L;

	public EncryptionError() {
		super(new Exception("Error occured during Encryption/Decryption."));
	}
}