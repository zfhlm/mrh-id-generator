package org.lushen.mrh.id.generator.revision;

/**
 * revision 异常
 * 
 * @author hlm
 */
public class RevisionException extends RuntimeException {

	private static final long serialVersionUID = 4412234094204684275L;

	public RevisionException(String message, Throwable cause) {
		super(message, cause);
	}

	public RevisionException(String message) {
		super(message);
	}

}
