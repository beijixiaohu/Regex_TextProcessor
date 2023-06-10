/**
 * 用户类
 *
 */
record User(String username, String password) {

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
