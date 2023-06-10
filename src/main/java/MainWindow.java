/**
 * 文本排版处理器的主窗口类。
 * 读取和保存规则列表文件，对指定文件进行替换操作并输出操作记录到日志框中。
 * 可以配置替换规则和忽略规则。
 *
 * @author beijixiaohu
 */

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainWindow {
    /**
     * 当前工作目录路径
     */
    static String workingDir = System.getProperty("user.dir");

    /**
     * 应用程序的入口点
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 使用FlatLightLaf外观
        FlatLightLaf.setup();

        // 创建主窗口
        JFrame frame = new JFrame("文本正则处理器");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 窗口图标
        ImageIcon icon = new ImageIcon(workingDir + "/src/main/resources/icon.png"); // 创建图标对象
        frame.setIconImage(icon.getImage()); // 将图标对象设置为窗口的图标

        // 创建菜单栏及菜单项，并添加到主窗口中
        JMenuBar menuBar = new JMenuBar();

        // “配置”菜单
        JMenu ruleMenu = new JMenu("配置");

        // 替换规则管理按钮
        JMenuItem manageMenuItem = new JMenuItem("替换规则");
        manageMenuItem.setHorizontalTextPosition(SwingConstants.LEFT);
        manageMenuItem.setPreferredSize(new Dimension(75, 30));

        // 添加管理规则按钮的事件监听器
        manageMenuItem.addActionListener(e -> {
            try {
                List<Rule> Rules = FileHandler.readRules();
                // 显示规则管理界面
                RuleDialog dialog = new RuleDialog(frame, Rules);
                dialog.setSize(900, 450);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "读取/保存替换规则文件失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 忽略规则管理按钮
        JMenuItem ExMenuItem = new JMenuItem("忽略规则");
        ExMenuItem.setHorizontalTextPosition(SwingConstants.LEFT);
        ExMenuItem.setPreferredSize(new Dimension(75, 30));
        // 添加忽略按钮的事件监听器
        ExMenuItem.addActionListener(e -> {
            try {
                List<Ex> ExRules = FileHandler.readExRules();
                // 显示忽略规则管理界面
                ExRuleDialog dialog = new ExRuleDialog(frame, ExRules);
                dialog.setSize(900, 450);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "读取/保存忽略规则文件失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        ruleMenu.add(manageMenuItem);
        ruleMenu.add(ExMenuItem);

        // “帮助”菜单
        JMenu helpMenu = new JMenu("帮助");

        // 关于按钮
        JMenuItem aboutMenuItem = new JMenuItem("关于");
        aboutMenuItem.setHorizontalTextPosition(SwingConstants.LEFT);
        aboutMenuItem.setPreferredSize(new Dimension(50, 30));
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "文本正则处理器 v1.3\n@北极小狐 www.dorkyfox.com", "关于", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem explainMenuItem = new JMenuItem("说明");
        explainMenuItem.setHorizontalTextPosition(SwingConstants.LEFT);
        explainMenuItem.setPreferredSize(new Dimension(50, 30));
        explainMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "<html><body style='font-size:10px;'><p><strong>工作流程：</strong></p><p>轮询 替换规则管理页面 中的规则：</p><p>程序会按照正则表达式对打开的文本自动 逐行 进行匹配替换，</p><p>替换前会首先检查该行是否符合 忽略规则管理页面 中的某一条规则</p><p>如果符合则跳过这一行不做任何处理</p><p><strong>实现原理：</strong></p><p>正则表达式中的值会传递给replaceAll方法的第一个参数；</p><p>替换字符串中的值会传递给给replaceAll方法的第二个参数</p><p>因此只需要符合Java中的正则语法即可，包括分组，前后断言等等</p></body></html>",
                "说明", JOptionPane.QUESTION_MESSAGE));
        helpMenu.add(explainMenuItem);
        helpMenu.add(aboutMenuItem);

        // “云同步”菜单
        JMenu LoginMenu = new JMenu("云同步");

        // 登录按钮
        JMenuItem loginMenuItem = new JMenuItem("登录");
        loginMenuItem.setHorizontalTextPosition(SwingConstants.LEFT);
        loginMenuItem.setPreferredSize(new Dimension(100, 30));
        loginMenuItem.setHorizontalAlignment(SwingConstants.LEFT);

        // 添加登录按钮的事件监听器
        loginMenuItem.addActionListener(e -> {
            // 创建LoginDialog对象
            LoginDialog loginDialog = new LoginDialog(frame);
            loginDialog.setSize(300, 200);
            loginDialog.setLocationRelativeTo(null);
            loginDialog.setVisible(true);
        });

        // 将登录按钮添加到云同步菜单中
        LoginMenu.add(loginMenuItem);
        LoginMenu.setAlignmentX(Component.LEFT_ALIGNMENT);


        // 将菜单添加到菜单栏中，将菜单栏添加到主窗口中
        menuBar.add(ruleMenu);
        menuBar.add(helpMenu);
        menuBar.add(LoginMenu);
        frame.setJMenuBar(menuBar);

        // 日志框和滚动条
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(Color.WHITE);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(null);

        // 初始日志
        logArea.append("""
                =====================运行日志======================
                支持处理所有的纯文本格式文档，包括txt、md、json等，
                不支持二进制文档，比如word、pdf等
                                
                请注意备份文件！！！！！！！！
                更多说明请点击 帮助-说明
                =================================================
                """);

        // 自动登录与同步配置
        List<User> users = new ArrayList<>();
        try {
            users = FileHandler.readUser();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(users.size()>0){
            MysqlConn sqlconn = new MysqlConn();
            if(sqlconn.getUserID()!=0){
                User user = users.get(users.size()-1);
                loginMenuItem.setText("当前账户：" + user.username());

                // 退出账号
                JMenuItem logoutMenuItem = new JMenuItem("退出");
                logoutMenuItem.setHorizontalTextPosition(SwingConstants.LEFT);
                logoutMenuItem.setPreferredSize(new Dimension(100, 30));
                logoutMenuItem.addActionListener(e -> {
                    try {
                        FileHandler.eraserUser();
                        loginMenuItem.setText("登录");
                        FileHandler.eraserRules();
                        FileHandler.eraserExRules();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "退出登录失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
                LoginMenu.add(logoutMenuItem);
                sqlconn.syncDataToLocal();
                sqlconn.syncDataToDB();
                sqlconn.syncExDataToLocal();
                sqlconn.syncExDataToDB();
                sqlconn.closeConnections();
            }
        }

        // 创建控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(200, 75));
        controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.lightGray));
        JLabel dragLabel = new JLabel("<html><font style='font: bold 14px/1.6em 宋体, 微软雅黑; color: black;'>将文件拖拽到这里…</font></html>");
        dragLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dragLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        dragLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPanel.add(Box.createVerticalGlue());
        controlPanel.add(dragLabel);
        controlPanel.add(Box.createVerticalGlue());

        // 将日志框和控制面板添加到主窗口中
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        frame.getContentPane().add(logScrollPane, BorderLayout.CENTER);

        // 添加拖拽文件功能
        frame.getContentPane().setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() > 0) {
                        String filePath = droppedFiles.get(0).getPath();
                        int dialogResult = JOptionPane.showConfirmDialog(frame, "是否确认要执行？", "确认", JOptionPane.YES_NO_OPTION);
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            readAndReplace(filePath, logArea);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 显示主窗口
        frame.setVisible(true);
    }

    /**
     * 读取替换：从指定文件中读取文本，按照规则进行替换，并将结果原路保存，同时输出替换记录到日志框中
     *
     * @param filePath 文件路径
     * @param logArea  日志
     */
    private static void readAndReplace(String filePath, JTextArea logArea) {
        try {
            List<Ex> ExRules = FileHandler.readExRules();

            // 处理md文件
            String mdContent = Files.readString(Path.of(filePath));
            StringBuilder replaced = new StringBuilder();

            logArea.append("\n=====================开始处理======================\n");
            // 对每一行内容进行处理
            for (String line : mdContent.split("\\n")) {

                boolean ignoreLine = false;
                // 检查是否需要忽略该行
                for (Ex rule : ExRules) {
                    if (rule.isOpen()) {
                        if (Pattern.compile(rule.regex()).matcher(line).find()) {
                            ignoreLine = true;
                            break;
                        }
                    }
                }
                if (ignoreLine) {
                    replaced.append(line).append("\n");
                    continue;
                }

                // 对该行内容进行替换操作
                for (Rule rule : FileHandler.readRules()) {
                    if (rule.isOpen()) {
                        Pattern pattern = Pattern.compile(rule.regex());
                        String replacedLine = line.replaceAll(pattern.pattern(), rule.replacement());
                        if (!replacedLine.equals(line)) {
                            // 记录日志
                            int lineNumber = replaced.length() - replaced.toString().replace("\n", "").length() + 1;
                            logArea.append("第" + lineNumber + "行：" + "\n" + "替换规则备注：" + rule.note() + "\n" + line + " -> " + replacedLine + "\n\n");
                        }
                        line = replacedLine;
                    }
                }
                replaced.append(line).append("\n");
            }
            logArea.append("\n=====================处理完成======================\n");

            // 将替换后的内容原路保存
            Files.writeString(Path.of(filePath), replaced);
            JOptionPane.showMessageDialog(null, "替换完成", "提示", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "读取文件失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
