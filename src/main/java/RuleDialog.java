import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 匹配规则管理对话框，用于添加、编辑和删除匹配规则。
 *
 * @author beijixiaohu
 */
public class RuleDialog extends JDialog {
    /**
     * 匹配规则表格。
     */
    private final JTable RuleTable;
    /**
     * 匹配规则表格的模型。
     */
    private final DefaultTableModel tableModel;

    /**
     * 创建一个新的 RuleDialog 对象。
     *
     * @param parent 父级窗口。
     * @param Rules  规则列表。
     */
    public RuleDialog(JFrame parent, List<Rule> Rules) {
        super(parent, "替换规则管理", true);

        // 创建上下左右布局的面板
        JPanel panel = new JPanel(new BorderLayout());
        getContentPane().add(panel);

        // 创建文本标签
        JLabel label = new JLabel("双击对应的方框即可编辑！编辑完按Enter键保存\n注意！正则表达式中如果需要转义，请使用单斜杠转义，而不是双斜杠\n");
        // 设置文本标签水平居中
        label.setHorizontalAlignment(SwingConstants.CENTER);
        // 添加文本标签到主面板中
        panel.add(label, BorderLayout.NORTH);

        // 表头
        String[] columnNames = {"启用", "正则表达式", "替换字符串", "备注"};
        // 创建忽略规则表格及其模型
        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        RuleTable = new JTable(tableModel);
        RuleTable.setShowHorizontalLines(true);
        RuleTable.setShowVerticalLines(true);
        JScrollPane tableScrollPane = new JScrollPane(RuleTable);

        // 设置表格列宽
        TableColumnModel columnModel = RuleTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(500);
        columnModel.getColumn(2).setPreferredWidth(200);
        columnModel.getColumn(3).setPreferredWidth(100);

        // 加载文件中的替换规则到表格中
        for (Rule rule : Rules) {
            Object[] rowData = {rule.isOpen(), rule.regex(), rule.replacement(), rule.note()};
            tableModel.addRow(rowData);
        }

        // 创建添加和删除按钮，并为它们添加事件监听器
        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> {
            Object[] rowData = {true, "", "", ""};
            tableModel.addRow(rowData);
        });

        JButton removeButton = new JButton("删除");
        removeButton.addActionListener(e -> {
            int selectedRow = RuleTable.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow);
            }
        });

        // 将表格添加到主面板中
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // 创建面板并添加按钮
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPane.add(addButton);
        buttonPane.add(removeButton);

        // 将按钮面板添加到主面板中
        panel.add(buttonPane, BorderLayout.SOUTH);

        // 为关闭按钮添加事件监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                List<Rule> newRules = getRulesFromTableModel(tableModel);
                // 保存忽略规则到rules.json文件中
                FileHandler.writeRules(newRules);
                Rules.clear();
                // 同步到数据库
                SqlServerConn sqlconn = new SqlServerConn();
                sqlconn.syncDataToDB();
                sqlconn.syncDataToLocal();
                sqlconn.closeConnections();
                
                RuleDialog.this.dispose();
            }
        });
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * 根据表格模型返回规则列表。
     *
     * @param model 表格模型。
     * @return 规则列表。
     */
    private static List<Rule> getRulesFromTableModel(DefaultTableModel model) {
        List<Rule> newRules = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            boolean isOpen = (Boolean) model.getValueAt(i, 0);
            String regex = (String) model.getValueAt(i, 1);
            String replacement = (String) model.getValueAt(i, 2);
            String note = (String) model.getValueAt(i, 3);
            Rule rules = new Rule(regex, replacement, isOpen, note);
            newRules.add(rules);
        }
        return newRules;
    }
}
