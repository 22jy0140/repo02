package pacServer;

import pacCommon.MsgCommon;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ClientInfo extends Thread {
    private Socket mySocket;
    private String myName = "noname";

    ClientInfo(Socket mySocket) {
        this.mySocket = mySocket;
    }

    public String getClientName() {
        return myName;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            String line;
            String[] buffs;
            String cmd, msg;
            while ((line = in.readLine()) != null) {
                System.out.println(" ←受信: (" + mySocket.getRemoteSocketAddress() + ")" + line);
                buffs = line.split(MsgCommon.CMD_DLM, 2);
                cmd = buffs[0];
                msg = buffs.length > 1 ? buffs[1] : "";
                if (cmd.equals("Name")) {
                    // 空文字チェック
                    if (msg.isEmpty()) {
                        line = "NG 名前が指定されていません。";
                        // 使用不可の文字チェック
                    } else if (msg.contains(MsgCommon.CMD_DLM)) {
                        line = "NG <" + MsgCommon.CMD_DLM + ">は、名前に使用できません。";
                    } else if (msg.contains(MsgCommon.NAME_DLM)) {
                        line = "NG <" + MsgCommon.NAME_DLM + ">は、名前に使用できません。";
                    } else if (msg.equals(MsgCommon.EVERYONE)) {
                        line = "NG <" + MsgCommon.EVERYONE + ">は、名前に使用できません。";
                    } else if (!SocServer.getInstance().chkDuplName(msg)) {
                        line = "NG <" + msg + ">は、すでに登録されています。";
                    } else {
                        myName = msg;
                        line = "OK";
                    }
                    sendMsg(line);

                    ArrayList<ClientInfo> users = SocServer.getInstance().getUsersInfo();
                    line = "";
                    for (ClientInfo cliInfo : users) {
                        line += cliInfo.getClientName() + MsgCommon.NAME_DLM;
                    }
                    for (ClientInfo cliInfo : users) {
                        cliInfo.sendMsg("NameList", line);
                    }
                } else if (cmd.equals("GetUsers")) {
                    ArrayList<ClientInfo> users = SocServer.getInstance().getUsersInfo();
                    line = "";
                    for (ClientInfo cliInfo : users) {
                        line += cliInfo.getClientName() + MsgCommon.NAME_DLM;
                    }
                    sendMsg("NameList", line);
                } else if (cmd.equals("Msg") || cmd.equals("FileSend") || cmd.equals("FileData") || cmd.equals("FileEnd")) {
                    ArrayList<ClientInfo> users = SocServer.getInstance().getUsersInfo();
                    String RedvMsgs[] = msg.split(MsgCommon.CMD_DLM, 2);
                    String sndMsg = RedvMsgs.length > 1 ? RedvMsgs[1] : "";
                    String names[] = RedvMsgs[0].split(MsgCommon.NAME_DLM);

                    for (String name : names) {
                        if (name.equals(MsgCommon.EVERYONE)) {
                            for (ClientInfo cliInfo : users) {
                                cliInfo.sendMsg(cmd, sndMsg);
                            }
                        } else {
                            for (ClientInfo cliInfo : users) {
                                if (name.equals(cliInfo.getClientName())) {
                                    cliInfo.sendMsg(cmd, sndMsg);
                                }
                            }
                        }
                    }
                } else if (cmd.equals("Discon")) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (mySocket != null)
                    mySocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("□切断" + mySocket.getRemoteSocketAddress());
        }
    }


    public void sendMsg(String msg) {
        try {
            PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
            out.println(msg);
            System.out.println(" →送信: (" + mySocket.getRemoteSocketAddress() + ")" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String cmd, String msg) {
        try {
            String line;
            line = cmd + MsgCommon.CMD_DLM + msg;
            PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
            out.println(line);
            System.out.println(" →送信: (" + mySocket.getRemoteSocketAddress() + ")" + line);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
