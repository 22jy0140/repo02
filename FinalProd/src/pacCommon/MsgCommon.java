package pacCommon;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class MsgCommon {
    public static final String CMD_DLM = ":";
    public static final String NAME_DLM = ",";
    public static final String EVERYONE = "ALL";
    public static final String EMPTY = "";

    // 入力文字の数値形式チェック
    public static boolean isNumeric(String str) {
        return str.matches("[+-]?\\d*(\\.\\d+)?");
    }

    public static String getNowTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
