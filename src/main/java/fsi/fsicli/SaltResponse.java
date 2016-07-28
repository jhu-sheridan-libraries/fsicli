package fsi.fsicli;

public class SaltResponse {
	private final boolean success;
	private final String salt;
	
	public SaltResponse(boolean success, String salt) {
		this.success = success;
		this.salt = salt;
	}

	public boolean isSuccess() {
		return success;
	}
	
	public String getSalt() {
		return salt;
	}
}
