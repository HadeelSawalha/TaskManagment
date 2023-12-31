package TaskManagerProject;

public class User {
	private String firstName;
	private String lastName;
	private String password;
	private String email;

	public User() {
	}

	private String role;

	public User(String firstName, String lastName, String password, String email, String role) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.email = email;
		this.role = role;

	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	@Override
	public String toString() {
		return "User{" + "firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", email='" + email
				+ '\'' + '}';
	}
}
