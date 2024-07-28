package org.netbeans.modules.php.blade.syntax.antlr4;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.netbeans.junit.NbTestCase;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;

/**
 *
 * @author bhaidu
 */
public class BladeAntlrLexerTestBase extends NbTestCase {

    public BladeAntlrLexerTestBase(String testName) {
        super(testName);
    }

    public File getDataDir() {
        URL codebase = getClass().getProtectionDomain().getCodeSource().getLocation();
        File dataDir = null;
        try {
            dataDir = new File(new File(codebase.toURI()), "data");
        } catch (URISyntaxException x) {
            throw new Error(x);
        }
        return dataDir;
    }

    protected void performTest(String filename) throws Exception {
        performTest(filename, null);
    }

    protected String getTestResult(String filename, String caretLine) throws Exception {
        return getTestResult(filename);
    }

    protected void performTest(String filename, String caretLine) throws Exception {
        // parse the file
        String result = getTestResult(filename, caretLine);
        System.out.print(result);
    }

    protected String getTestResult(String filename) throws Exception {
        String content = BladeUtils.getFileContent(new File(getDataDir(), "testfiles/" + filename));
        CommonTokenStream tokenStream = BladeUtils.getTokenStream(content);
        System.out.print("\n---Lexer scan for <<" + filename + ">>\n\n");
        return createResult(tokenStream);
    }

    protected String createResult(CommonTokenStream tokenStream) throws Exception {
        StringBuilder result = new StringBuilder();

        for (Token token : tokenStream.getTokens()) {
            int channel = token.getChannel();
            switch (token.getType()) {
                case BL_PARAM_STRING:
                    result.append("BL_PARAM_STRING ");
                    break;
                case BL_NAME_STRING:
                    result.append("BL_NAME_STRING ");
                    break;
                case BL_COMMA:
                    result.append("BL_COMMA ");
                    break;
                case BL_PARAM_COMMA:
                    result.append("BL_PARAM_COMMA ");
                    break;
                case BLADE_PARAM_LPAREN:
                    result.append("BLADE_PARAM_LPAREN ");
                    break;
                case BLADE_PARAM_RPAREN:
                    result.append("BLADE_PARAM_RPAREN ");
                    break;
                case BLADE_PARAM_EXTRA:
                    result.append("BLADE_PARAM_EXTRA ");
                    break;
                case D_VERBATIM:
                    result.append("D_VERBATIM ");
                    break;
                case D_ENDVERBATIM:
                    result.append("D_ENDVERBATIM ");
                    break;
                case HTML:
                    result.append("HTML ");
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
