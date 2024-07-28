package org.netbeans.modules.php.blade.syntax.antlr4;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.netbeans.junit.NbTestCase;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrColoringLexer.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author bhaidu
 */
public class BladeAntlrColoringLexerTestBase extends BladeBaseTest {

    public BladeAntlrColoringLexerTestBase(String testName) {
        super(testName);
    }

    public static final FileObject copyStringToFileObject(FileObject fo, String content) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            try {
                FileUtil.copy(is, os);
                return fo;
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
    }

    protected void performTest(String filename) throws Exception {
        String result = getTestResult(filename);
        String goldenFolder = getDataDir() + "/goldenfiles/";
        File goldenFile = new File(goldenFolder + filename + ".pass");
        if (!goldenFile.exists()) {
            FileObject goldenFO = touch(goldenFolder, filename + ".pass");
            copyStringToFileObject(goldenFO, result);
        } else {
            // if exist, compare it.
            FileObject resultFO = touch(getWorkDir(), filename + ".result");
            copyStringToFileObject(resultFO, result);
            assertFile(FileUtil.toFile(resultFO), goldenFile, getWorkDir());
        }
    }

    protected String getTestResult(String filename) throws Exception {
        String content = BladeUtils.getFileContent(new File(getDataDir(), "testfiles/" + filename));
        CommonTokenStream tokenStream = BladeUtils.getColoringTokenStream(content);
        System.out.print("\n---Lexer scan for <<" + filename + ">>\n\n");
        return createResult(tokenStream);
    }

    protected String createResult(CommonTokenStream tokenStream) throws Exception {
        StringBuilder result = new StringBuilder();

        for (Token token : tokenStream.getTokens()) {
            switch (token.getType()) {
                case HTML:
                    result.append("HTML ");
                    break;
                case RAW_TAG:
                    result.append("RAW_TAG ");
                    break;
                case PHP_EXPRESSION:
                    result.append("PHP_EXPRESSION ");
                    break;
                case BLADE_PHP_ECHO_EXPR:
                    result.append("PHP_BLADE_ECHO_EXPR ");
                    break;
                case BLADE_PHP_INLINE:
                    result.append("BLADE_PHP_INLINE ");
                    break;
                default:
                    result.append(token.getType());
                    result.append(" ");
                    break;
            }

            String text = replaceLinesAndTabs(token.getText());
            result.append(text);
            result.append(";");
            result.append("\n");
        }

        return result.toString();
    }

    public static String replaceLinesAndTabs(String input) {
        String escapedString = input;
        escapedString = escapedString.replaceAll("\n", "\\\\n"); //NOI18N
        escapedString = escapedString.replaceAll("\r", "\\\\r"); //NOI18N
        escapedString = escapedString.replaceAll("\t", "\\\\t"); //NOI18N
        return escapedString;
    }
}
