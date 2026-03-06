//======= Проверка типа записей =======
package clipboard.utils;

import clipboard.model.ClipboardType;

import java.util.regex.Pattern;

public class ContentDetector {

    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,5}[-\\s.]?[0-9]{1,5}$");

    private static final Pattern CODE_PATTERN =
            Pattern.compile(".*(public class|function|def |import |package |#include|if\\s*\\(|for\\s*\\(|while\\s*\\().*",
                    Pattern.DOTALL);

    public ClipboardType detectType(String content) {
        if (content == null || content.trim().isEmpty()) {
            return ClipboardType.TEXT;
        }

        content = content.trim();

        if (URL_PATTERN.matcher(content).matches()) {
            return ClipboardType.URL;
        }

        if (EMAIL_PATTERN.matcher(content).matches()) {
            return ClipboardType.EMAIL;
        }

        if (PHONE_PATTERN.matcher(content).matches()) {
            return ClipboardType.PHONE;
        }

        if (CODE_PATTERN.matcher(content).matches()) {
            return ClipboardType.CODE;
        }

        return ClipboardType.TEXT;
    }
}