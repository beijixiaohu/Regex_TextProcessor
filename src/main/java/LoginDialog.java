
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 *
 * @author beijixiaohu
 */
public class LoginDialog extends JDialog {
    
    public LoginDialog(JFrame parent) {
        List<User> users = new ArrayList<>();
        try {
            users = FileHandler.readUser();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JPanel panel = new JPanel(new FlowLayout());
        JLabel userLabel = new JLabel("用户名:");
        JTextField userText = new JTextField(20);
        JLabel passwordLabel = new JLabel("密码:");
        JTextField passwordText = new JTextField(20);
        User user = users.get(users.size()-1);
        userText.setText(user.username());
        passwordText.setText(user.password());

        panel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // 设置panel布局为右对齐
        panel.add(userLabel);
        panel.add(userText);
        panel.add(passwordLabel);
        panel.add(passwordText);
        JButton saveButton = new JButton("登录");
        JButton registerButton = new JButton("注册");

        saveButton.addActionListener(new SaveButtonListener(users, userText, passwordText, parent));
        registerButton.addActionListener(new RegisterButtonListener(users, userText, passwordText, parent));
        panel.add(saveButton);
        panel.add(registerButton);
        this.add(panel);
        this.setSize(new Dimension(300, 200));
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private class SaveButtonListener implements ActionListener {
        private List<User> users;
        private JTextField userText;
        private JTextField passwordText;
        private JFrame parent;

        public SaveButtonListener(List<User> users, JTextField userText, JTextField passwordText, JFrame parent) {
            this.users = users;
            this.userText = userText;
            this.passwordText = passwordText;
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent event) {
            User user = new User(userText.getText(), passwordText.getText());
            users.add(user);
            FileHandler.writeUser(users);
            SqlServerConn sqlconn = new SqlServerConn();
            if(sqlconn.getUserID()==0){
                JOptionPane.showMessageDialog(null, "账号或密码错误");
            }else{
                dispose();
                sqlconn.syncDataToLocal();
                sqlconn.syncDataToDB();
                sqlconn.syncExDataToLocal();
                sqlconn.syncExDataToDB();
                String username = userText.getText(); //获取当前登录的账号名
                parent.getJMenuBar().getMenu(2).getItem(0).setText("当前账户：" + username); //将LoginMenu中的loginMenuItem改为当前登录的账号名
            }
        }
    }

    private class RegisterButtonListener implements ActionListener {
        private List<User> users;
        private JTextField userText;
        private JTextField passwordText;
        private JFrame parent;

        public RegisterButtonListener(List<User> users, JTextField userText, JTextField passwordText, JFrame parent) {
            this.users = users;
            this.userText = userText;
            this.passwordText = passwordText;
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent event) {
            SqlServerConn sqlconn = new SqlServerConn();
            sqlconn.registerUser(userText.getText(), passwordText.getText());
            sqlconn.closeConnections();        
            JOptionPane.showMessageDialog(null, "注册成功");
        }
    }
}


