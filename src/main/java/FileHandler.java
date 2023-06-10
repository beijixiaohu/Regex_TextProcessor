
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 规则读写
public class FileHandler {
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
     * user.json文件路径
     */
    private static final String USER_FILE_PATH = workingDir + "/src/main/resources/user.json";
    /**
     * user.json文件路径的Path对象
     */
    static Path Userpath = Paths.get(USER_FILE_PATH);

    /**
     * 从rules.json文件中读取替换规则
     *
     * @return 替换规则列表
     * @throws IOException   如果读取规则文件失败
     * @throws JSONException 如果规则文件格式错误
     */
    static List<Rule> readRules() throws IOException {
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
    static List<Ex> readExRules() throws IOException {
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
    static void writeExRules(List<Ex> rules) {
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
     * 从user.json文件中读取账号密码
     *
     * @return 账号密码列表
     * @throws IOException   如果读取user.json文件失败
     * @throws JSONException 如果user.json文件格式错误
     */
    static List<User> readUser() throws IOException {
        // 如果user.json文件不存在，则创建
        if (!Files.exists(Userpath)) {
            File file = new File(USER_FILE_PATH);
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Userpath)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");
                User user = new User(username, password);
                users.add(user);
            }
        } catch (JSONException e) {
            // 如果user.json是空白的，则其会解析错误，这是正常的，捕获异常不做处理即可

        }
        return users;
    }


    /**
     * 将用户信息写入user.json文件
     *
     * @param users 用户信息列表
     * @throws IOException 如果保存用户信息失败
     */
    static void writeUser(List<User> users) {
        JSONArray jsonArray = new JSONArray();
        Set<String> usernameSet = new HashSet<>();
        for (User user : users) {
            if (usernameSet.contains(user.username())) {
                continue;
            }
            usernameSet.add(user.username());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", user.username());
            jsonObject.put("password", user.password());
            jsonArray.put(jsonObject);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Userpath)) {
            writer.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "保存用户信息失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 擦除user.json中的所有信息
     *
     * @throws IOException 如果擦除用户信息失败
     */
    static void eraserUser() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Userpath)) {
            writer.write("[{'username':'','password':''}]");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "擦除用户信息失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 擦除rules.json中的所有信息
     *
     * @throws IOException 如果擦除用户信息失败
     */
    static void eraserRules() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("[]");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "擦除rules.json信息失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 擦除ex.json中的所有信息
     *
     * @throws IOException 如果擦除用户信息失败
     */
    static void eraserExRules() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(ExPath)) {
            writer.write("[]");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "擦除ex.json信息失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
