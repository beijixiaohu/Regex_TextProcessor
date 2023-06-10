/**
 * 用户类，包含正则表达式、替换字符串、是否开启、备注四个属性
 *
 * @param regex 表示规则的正则表达式
 * @param replacement 匹配到正则表达式后要替换的字符串
 * @param isOpen 表示该规则是否已开启
 * @param note 备注内容
 */
record User(String username, String password) {

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
