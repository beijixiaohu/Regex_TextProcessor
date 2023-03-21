/**
 * 文本排版处理器的主窗口类。
 * 读取和保存规则列表文件，对指定文件进行替换操作并输出操作记录到日志框中。
 * 可以配置替换规则和忽略规则。
 *
 * @author beijixiaohu
 */

import com.formdev.flatlaf.FlatLightLaf;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainWindow {
    /**
     * 当前工作目录路径
     */
    static String workingDir = System.getProperty("user.dir");
    /**
     * 替换规则列表文件路径
     */
    private static final String RULE_FILE_PATH = workingDir + "/src/main/resources/rules.json";
    /**
     * 替换规则列表文件路径的Path对象
     */
    static Path path = Paths.get(RULE_FILE_PATH);
    /**
     * 忽略规则列表文件路径
     */
    private static final String IGNORE_RULE_FILE_PATH = workingDir + "/src/main/resources/ex.json";

    /**
     * 忽略规则列表文件路径的Path对象
     */
    static Path ExPath = Paths.get(IGNORE_RULE_FILE_PATH);

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
                List<Rule> Rules = readRules();
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
                List<Ex> ExRules = readExRules();
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
        aboutMenuItem.setPreferredSize(new Dimension(45, 30));
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "文本正则处理器 v1.1\n@北极小狐 www.dorkyfox.com", "关于", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem explainMenuItem = new JMenuItem("说明");
        explainMenuItem.setHorizontalTextPosition(SwingConstants.LEFT);
        explainMenuItem.setPreferredSize(new Dimension(45, 30));
        explainMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, """
                        工作流程：
                        轮询 替换规则管理页面 中的规则：
                        程序会按照正则表达式对打开的文本自动 逐行 进行匹配替换，
                        替换前会首先检查该行是否符合 忽略规则管理页面 中的某一条规则
                        如果符合则跳过这一行不做任何处理
                        实现原理：
                        正则表达式中的值会传递给replaceAll方法的第一个参数；
                        替换字符串中的值会传递给给replaceAll方法的第二个参数
                        因此只需要符合Java中的正则语法即可，包括分组，前后断言等等""",
                "关于", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(explainMenuItem);
        helpMenu.add(aboutMenuItem);

        // 将菜单添加到菜单栏中，将菜单栏添加到主窗口中
        menuBar.add(ruleMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        // 日志框和滚动条
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // 初始日志
        logArea.append("""
                =====================运行日志======================
                支持处理所有的纯文本格式文档，包括txt、md、json等，
                不支持二进制文档，比如word、pdf等
                                
                点击打开文件，选择文件并确定后会立即执行替换，
                请注意备份文件！！！！！！！！
                更多说明请点击 帮助-说明
                =================================================
                """);

        // 打开文件按钮
        JButton openButton = new JButton("打开文件");

        // 创建控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(openButton);

        // 将日志框和控制面板添加到主窗口中
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        frame.getContentPane().add(logScrollPane, BorderLayout.CENTER);

        // 打开文件按钮的事件监听器
        openButton.addActionListener(e -> {
            // 创建并显示文件选择对话框
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getPath();
                readAndReplace(filePath, logArea);
            }
        });

        // 显示主窗口
        frame.setVisible(true);
    }

    /**
     * 从rules.json文件中读取替换规则
     *
     * @return 替换规则列表
     * @throws IOException   如果读取规则文件失败
     * @throws JSONException 如果规则文件格式错误
     */
    private static List<Rule> readRules() throws IOException {
        // 如果rules.json文件不存在，则创建
        if (!Files.exists(path)) {
            File file = new File(RULE_FILE_PATH);
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        List<Rule> rules = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(RULE_FILE_PATH))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String regex = jsonObject.getString("regex");
                String replacement = jsonObject.getString("replacement");
                boolean isOpen = jsonObject.getBoolean("isOpen");
                String note = jsonObject.getString("note");
                Rule rule = new Rule(regex, replacement, isOpen, note);
                rules.add(rule);
            }
        } catch (JSONException e) {
            // 如果rules.json是空白的，则其会解析错误，这是正常的，捕获异常不做处理即可
        }
        return rules;
    }

    /**
     * 将替换规则列表写入rules.json文件
     *
     * @param rules 替换规则列表
     * @throws IOException 如果保存规则文件失败
     */
    static void writeRules(List<Rule> rules) {
        JSONArray jsonArray = new JSONArray();
        for (Rule rule : rules) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("regex", rule.regex());
            jsonObject.put("replacement", rule.replacement());
            jsonObject.put("isOpen", rule.isOpen());
            jsonObject.put("note", rule.note());
            jsonArray.put(jsonObject);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(RULE_FILE_PATH))) {
            writer.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "保存规则失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 从ex.json文件中读取忽略规则列表
     *
     * @return 忽略规则列表
     * @throws IOException   如果读取忽略规则文件失败
     * @throws JSONException 如果忽略规则文件格式错误
     */
    private static List<Ex> readExRules() throws IOException {
        // 如果ex.json文件不存在，则创建
        if (!Files.exists(ExPath)) {
            File file = new File(IGNORE_RULE_FILE_PATH);
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        List<Ex> ExRules = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(ExPath)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject ExRule = jsonArray.getJSONObject(i);
                Ex rule = new Ex(ExRule.getBoolean("isOpen"), ExRule.getString("regex"), ExRule.getString("note"));
                ExRules.add(rule);
            }
        } catch (JSONException e) {
            // 如果ex.json是空白的，则其会解析错误，这是正常的，捕获异常不做处理即可
        }
        return ExRules;
    }

    /**
     * 将忽略规则列表写入ex.json文件
     *
     * @param rules 规则
     */
    static void writeExRules(List<Ex> rules) throws IOException {
        JSONArray jsonArray = new JSONArray();
        for (Ex rule : rules) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isOpen", rule.isOpen());
            jsonObject.put("regex", rule.regex());
            jsonObject.put("note", rule.note());
            jsonArray.put(jsonObject);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(IGNORE_RULE_FILE_PATH))) {
            writer.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "保存规则失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 从指定文件中读取文本，按照规则进行替换，并将结果原路保存，同时输出替换记录到日志框中
     *
     * @param filePath 文件路径
     * @param logArea  日志
     */
    private static void readAndReplace(String filePath, JTextArea logArea) {
        try {
            String content = Files.readString(Path.of(filePath));
            List<Ex> ExRules = readExRules();

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
                for (Rule rule : readRules()) {
                    if (rule.isOpen()) {
                        Pattern pattern = Pattern.compile(rule.regex());
                        String replacedLine = line.replaceAll(pattern.pattern(), rule.replacement());
                        if (!replacedLine.equals(line)) {
                            // 记录日志
                            int lineNumber = replaced.length() - replaced.toString().replace("\n", "").length() + 1;
                            logArea.append("第" + lineNumber + "行：" + "\n" + "替换规则备注：" + rule.note() + "\n" + line + " -> " + replacedLine + "\n");
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
