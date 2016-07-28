package fsi.fsicli;

public class LoginResponse {
	private final boolean success;
	private final String username;

	private final String message;

	public LoginResponse(boolean success, String username, String message) {
		this.success = success;
		this.username = username;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getUserName() {
		return username;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "LoginResponse [success=" + success + ", username=" + username + ", message=" + message + "]";
	}
}
