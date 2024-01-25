package pacServer;

import pacCommon.MsgCommon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocServer {

    // サーバーはシングルトン設計。唯一のインスタンス
    private static SocServer instance;
    // サーバーソケット
    private static ServerSocket server;

    private SocServer() {
    }

    // クライアント情報配列化
    private static ArrayList<ClientInfo> users = new ArrayList<>();

    public static SocServer getInstance() {
        if (instance == null) {
            instance = new SocServer();
        }
        return instance;
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || !MsgCommon.isNumeric(args[0])) {
            System.out.println("ポート番号とする起動引数は正しくありません");
            return;
        }
        try {
            server = new ServerSocket(Integer.parseInt(args[0]));
            System.out.format("サーバーは%sで起動しました", args[0]);
            while (true) {
                // 新しいクライアントの接続を待つ
                Socket client = server.accept();
                // ユーザーオブジェクトを生成する
                ClientInfo cliUser = new ClientInfo(client);
                users.add(cliUser);
                cliUser.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean chkDuplName(String name) {

        boolean flg_OK = true;

        for (ClientInfo cli : users) {
            if (name.equals(cli.getClientName())) {
                flg_OK = false;
                break;
            }
        }
        return flg_OK;
    }

    public ArrayList<ClientInfo> getUsersInfo() {
        return users;
    }
}
