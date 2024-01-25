package pacClient;

import pacCommon.MsgCommon;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

public class SocClient extends JFrame implements Runnable, ActionListener {

    private static final long serialVersionUID = 1L;
    private final static String APPNAME = "チャットへようこそ"; // App Name
    private String destHost = "localHost"; // Server IP-Addr/destHostName
    private int destPort = 61000; // Server Port No
    private Socket mySocket; // my Socket

    // 以下、コンポーネント
    private JPanel paneParent;
    private JTextField txtDestHost; // 接続ホスト名入力用の１行テキスト
    private JTextField txtDestPort; // 接続ポート入力用の１行テキスト
    private JTextField txtUserName; // ユーザー名を入力する１行テキスト
    private JList<String> userList; // ユーザー名一覧
    private JTextArea txtsRecvMsg; // 受信メッセージを表示するテキストエリア
    private JTextField txtSendMsg; // 送信メッセージを入力する１行テキスト
    private JButton btnConnect; // 「接続」ボタン
    private JButton btnDiscon; // 「切断」ボタン
    private JButton btnRegstName; // 「登録」ボタン
    private JButton btnClrMsg; // 「クリア」ボタン
    private JButton btnSend; // 「送信」ボタン

    private JTextField txtFileName; // ファイル名を入力する１行テキスト
    private JButton btnFileSend; // 「ファイル送信」ボタン
    private JButton btnFileSelect;// 「ファイル」ボタン

    public static void main(String[] args) {
        SocClient mainFrame = new SocClient();
        mainFrame.setSize(800, 600);
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }

    private SocClient() {
        super(APPNAME); // JFrameのコンストラクタ(ウィンドウタイトル登録)
        paneParent = new JPanel();
        setContentPane(paneParent);
        paneParent.setLayout(new BorderLayout());

        // 上位パネル(接続先、接続ボタン、切断ボタン)
        JPanel paneTop = new JPanel();
        paneTop.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        TitledBorder borderTop = new TitledBorder(new EtchedBorder(), "接続先情報");
        paneTop.setBorder(borderTop);
        paneTop.add(new JLabel("接続先ホスト名"));

        txtDestHost = new JTextField(destHost);
        txtDestHost.setPreferredSize(new Dimension(200, txtDestHost.getPreferredSize().height));
        paneTop.add(txtDestHost);
        paneTop.add(new JLabel("ポート番号"));
        txtDestPort = new JTextField(String.valueOf(destPort));
        txtDestPort.setPreferredSize(new Dimension(100, txtDestPort.getPreferredSize().height));
        paneTop.add(txtDestPort);
        btnConnect = new JButton("接続");
        btnConnect.addActionListener(this);
        paneTop.add(btnConnect);
        btnDiscon = new JButton("切断");
        btnDiscon.addActionListener(this);
        paneTop.add(btnDiscon);

        // 左パネル(ユーザー名、登録ボタン、ユーザー名一覧)
        JPanel paneLeft = new JPanel();
        paneLeft.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        TitledBorder borderLeft = new TitledBorder(new EtchedBorder(), "ユーザー一覧");
        paneLeft.setBorder(borderLeft);
        paneLeft.setPreferredSize(new Dimension(300, 100));

        paneLeft.add(new JLabel("ユーザー名"));
        txtUserName = new JTextField();
        txtUserName.setPreferredSize(new Dimension(100, txtUserName.getPreferredSize().height));
        paneLeft.add(txtUserName);
        btnRegstName = new JButton("登録");
        btnRegstName.addActionListener(this);
        paneLeft.add(btnRegstName);

        userList = new JList<>();
        JScrollPane spUserList = new JScrollPane();
        spUserList.getViewport().setView(userList);
        spUserList.setPreferredSize(new Dimension(200, 300));
        paneLeft.add(spUserList);

        // 中央パネル(受信メッセージ、クリアボタン)
        JPanel paneCenter = new JPanel();
        paneCenter.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        TitledBorder borderCenter = new TitledBorder(new EtchedBorder(), "受信メッセージ");
        paneCenter.setBorder(borderCenter);
        paneCenter.setPreferredSize(new Dimension(480, 100));

        txtsRecvMsg = new JTextArea(18, 36);
        txtsRecvMsg.setLineWrap(true);
        txtsRecvMsg.setEditable(false); // 受信メッセージは編集不可
        JScrollPane scrollPane = new JScrollPane(txtsRecvMsg);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        paneCenter.add(scrollPane);

        btnClrMsg = new JButton("クリア");
        btnClrMsg.addActionListener(this);
        paneCenter.add(btnClrMsg);

        // 下位パネル(送信メッセージ、送信ボタン)
        JPanel paneBottom = new JPanel();
        paneBottom.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        TitledBorder borderBottom = new TitledBorder(new EtchedBorder(), "送信先情報");
        paneBottom.setBorder(borderBottom);
        paneBottom.setPreferredSize(new Dimension(720, 120));

        paneBottom.add(new JLabel("送信メッセージ"));
        txtSendMsg = new JTextField();
        txtSendMsg.setPreferredSize(new Dimension(300, txtSendMsg.getPreferredSize().height));
        paneBottom.add(txtSendMsg);

        btnSend = new JButton("送信");
        btnSend.addActionListener(this);
        paneBottom.add(btnSend);

        txtFileName = new JTextField(); // ファイル名を入力する１行テキスト
        txtFileName.setPreferredSize(new Dimension(300, txtFileName.getPreferredSize().height));
        paneBottom.add(txtFileName);
        btnFileSelect = new JButton("選択"); // 「ファイル」ボタン
        btnFileSelect.addActionListener(this);
        paneBottom.add(btnFileSelect);
        btnFileSend = new JButton("ファイル送信"); // 「ファイル送信」ボタン
        btnFileSend.addActionListener(this);
        paneBottom.add(btnFileSend);

        // 親パネルへの登録
        paneParent.add(paneTop, BorderLayout.NORTH);
        paneParent.add(paneLeft, BorderLayout.WEST);
        paneParent.add(paneCenter, BorderLayout.CENTER);
        paneParent.add(paneBottom, BorderLayout.SOUTH);

        setBtnEnable(false);
    }

    // flg: 接続状態
    void setBtnEnable(Boolean flg) {
        btnConnect.setEnabled(!flg);
        btnDiscon.setEnabled(flg);
        btnRegstName.setEnabled(flg);
        btnClrMsg.setEnabled(flg);
        btnSend.setEnabled(flg);
        btnFileSend.setEnabled(flg);
        btnFileSelect.setEnabled(flg);
    }

    public void actionPerformed(ActionEvent actEvt) {
        StringBuilder names = new StringBuilder();
        if (actEvt.getSource() == btnConnect) { // 接続要求
            destHost = txtDestHost.getText();
            if (MsgCommon.isNumeric(txtDestPort.getText())) {
                destPort = Integer.parseInt(txtDestPort.getText());
                if (destPort < 0 || 65535 < destPort) {
                    JOptionPane.showMessageDialog(paneParent, "0～65535のポート番号を入力してください",
                            "エラー", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!connServer()) {
                    JOptionPane.showMessageDialog(paneParent, "只今サーバーに接続できません",
                            "エラー", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                setBtnEnable(true);
            } else {
                txtDestHost.setText(String.valueOf(destPort));
            }
        } else if (actEvt.getSource() == btnRegstName) { // 名前登録要求
            sendMessage("Name", txtUserName.getText());
            sendMessage("GetUsers");
        } else if (actEvt.getSource() == btnSend) { // メッセージ送信要求
            if (userList.isSelectionEmpty()) {
                sendMessage("Msg", MsgCommon.EVERYONE, txtSendMsg.getText());
            } else {
                for (Object selObj : userList.getSelectedValuesList()) {
                    names.append(MsgCommon.getNowTime())
                            .append(selObj).append(MsgCommon.NAME_DLM);
                }
                sendMessage("Msg", names.toString(), txtSendMsg.getText());
            }
            sendMessage("GetUsers");
            userList.clearSelection();
        } else if (actEvt.getSource() == btnFileSend) { // ファイル送信要求
            String file_name = txtFileName.getText();
            File file = new File(file_name);
            if (!file.exists()) {
                txtsRecvMsg.append(MsgCommon.getNowTime() + "ファイルが見つかりません。" + file + "\n");
                return;
            }

            if (userList.isSelectionEmpty()) {
                names = new StringBuilder(MsgCommon.EVERYONE);
            } else {
                for (Object selObj : userList.getSelectedValuesList()) {
                    names.append(selObj.toString()).append(MsgCommon.NAME_DLM);
                }
            }
            sendMessage("FileSend", names.toString(), file.getName());
            sendFile("FileData", names.toString(), file);
            sendMessage("FileEnd", names.toString(), file.getName());
            userList.clearSelection();
        } else if (actEvt.getSource() == btnDiscon) { // 切断要求
            disconServer();
            setBtnEnable(false);
        } else if (actEvt.getSource() == btnClrMsg) {
            txtsRecvMsg.setText(MsgCommon.EMPTY);
        } else if (actEvt.getSource() == btnFileSelect) {
            JFileChooser jFileChooser = new JFileChooser();
            int option = jFileChooser.showDialog(this, "選択");
            if (option == JFileChooser.APPROVE_OPTION) {
                txtFileName.setText(jFileChooser.getSelectedFile().getPath());
            }
        }
    }

    public void run() {
        try {
            OutputStream out = null;
            while (mySocket.isConnected()) {
                BufferedReader ois = new BufferedReader(
                        new InputStreamReader(mySocket.getInputStream()));
                String message = ois.readLine();
                String[] buffs = message.split(MsgCommon.CMD_DLM, 2);
                String cmd = buffs[0];
                String msg = buffs.length > 1 ? buffs[1] : "";
                switch (cmd) {
                    case "NameList":
                        userList.setListData(msg.split(MsgCommon.NAME_DLM));
                        break;
                    case "FileSend":
                        out = Files.newOutputStream(Paths.get(msg));
                        break;
                    case "FileData":
                        out.write(Base64.getDecoder().decode(msg));
                        txtsRecvMsg.append(MsgCommon.getNowTime() + "wite date:" + msg + "\n");
                        break;
                    case "FileEnd":
                        out.close();
                        out = null;
                        break;
                }
                txtsRecvMsg.append(MsgCommon.getNowTime() + message + "\n");
                txtsRecvMsg.selectAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // サーバーに接続する
    public boolean connServer() {
        try {
            mySocket = new Socket(destHost, destPort);
            txtsRecvMsg.append(MsgCommon.getNowTime() + ">サーバーに接続しました\n");
            // メッセージ受信監視用のスレッドを生成->スタート(runメソッド起動)させる
            new Thread(this).start();
            // 現在のユーザー一覧を取得する
            sendMessage("GetUsers");
            return true;
        } catch (Exception err) {
            txtsRecvMsg.append(MsgCommon.getNowTime() + "Connect Erroe>" + err + "\n");
        }
        return false;
    }

    // サーバーを切断する
    public void disconServer() {
        try {
            sendMessage("Discon");
            mySocket.close();
        } catch (Exception err) {
            txtsRecvMsg.append(MsgCommon.getNowTime() + "Disconnect Erroe>" + err + "\n");
        }
    }

    // メッセージをサーバーに送信する
    public void sendMessage(String cmd) {
        sendMessage(cmd, "");
    }

    public void sendMessage(String cmd, String msg) {
        try {
            PrintWriter writer = new PrintWriter(mySocket.getOutputStream());
            writer.println(cmd + MsgCommon.CMD_DLM + msg);
            writer.flush();
        } catch (Exception err) {
            txtsRecvMsg.append(MsgCommon.getNowTime() + "ERROR>" + err + "\n");
        }
    }

    public void sendFile(String cmd, String names, File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bb = new byte[(int)file.length()], cc;
            int n;
            while ((n = fileInputStream.read(bb)) != -1) {
                cc = new byte[n];
                System.arraycopy(bb, 0, cc, 0, n);
                sendMessage(cmd, names, Base64.getEncoder().encodeToString(cc));
            }
        } catch (Exception err) {
            txtsRecvMsg.append(MsgCommon.getNowTime() + "ERROR>" + err + "\n");
        }
    }

    public void sendMessage(String cmd, String names, String msg) {
        try {
            PrintWriter writer = new PrintWriter(mySocket.getOutputStream());
            writer.println(cmd + MsgCommon.CMD_DLM + names + MsgCommon.CMD_DLM + msg);
            writer.flush();
        } catch (Exception err) {
            txtsRecvMsg.append(MsgCommon.getNowTime() + "ERROR>" + err + "\n");
        }
    }

}