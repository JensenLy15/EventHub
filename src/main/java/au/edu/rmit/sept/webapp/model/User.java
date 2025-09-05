package au.edu.rmit.sept.webapp.model;

public class User {
  private Long userId;
  private String name;
  private String email;
  private String password;
  private String role;   // student, organiser, admin
  private String status; // active, banned, suspended

  public User() {
    this.userId = 0L;
    this.name = "";
    this.email = "";
    this.password = "";
    this.role = "student";
    this.status = "active";
  }

  public User(Long userId, String name, String email, String password, String role, String status) {
    this.userId = userId;
    this.name = name;
    this.email = email;
    this.password = password;
    this.role = role;
    this.status = status;
  }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
