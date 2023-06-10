/**
 * 规则类，包含是否开启、正则表达式和备注三个属性。
 */
record Ex(boolean isOpen, String regex, String note) {
    public boolean getIsOpen() {
        return isOpen;
    }

    public String getRegex() {
        return regex;
    }

    public String getNote() {
        return note;
    }
}