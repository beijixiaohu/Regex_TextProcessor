import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

public class ExRuleDialog extends JDialog {
    private final List<Ex> ExRules;
    private final JTable ExRuleTable;
    private final DefaultTableModel tableModel;

    public ExRuleDialog(JFrame parent, List<Ex> ExRules) {
        super(parent, "忽略规则管理", true);
        this.ExRules = ExRules;
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
        String[] columnNames = {"启用", "正则表达式", "备注"};
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
        ExRuleTable = new JTable(tableModel);
        ExRuleTable.setShowHorizontalLines(true);
        ExRuleTable.setShowVerticalLines(true);
        JScrollPane tableScrollPane = new JScrollPane(ExRuleTable);

        // 设置表格列宽
        ExRuleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = ExRuleTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(600);
        columnModel.getColumn(2).setPreferredWidth(230);

        // 加载文件中的忽略规则到表格中
        for (Ex rule : ExRules) {
            Object[] rowData = {rule.isOpen(), rule.regex(), rule.note()};
            tableModel.addRow(rowData);
        }

        // 创建添加和删除按钮，并为它们添加事件监听器
        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> {
            Object[] rowData = {true, "", ""};
            tableModel.addRow(rowData);
        });

        JButton removeButton = new JButton("删除");
        removeButton.addActionListener(e -> {
            int selectedRow = ExRuleTable.getSelectedRow();
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
                List<Ex> newExRules = getExRulesFromTableModel();
                // 保存忽略规则到ex.json文件中
                try {
                    MainWindow.writeExRules(newExRules);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ExRules.clear();
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * 获取用户编辑后的忽略规则列表
     */
    private List<Ex> getExRulesFromTableModel() {
        List<Ex> newExRules = new java.util.ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            String rule = (String) tableModel.getValueAt(i, 1);
            String note = (String) tableModel.getValueAt(i, 2);
            Ex rules = new Ex(checked, rule, note);
            newExRules.add(rules);
        }
        return newExRules;
    }
}
