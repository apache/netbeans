package org.netbeans.modules.php.blade.syntax.antlr4;

import org.junit.Test;

/**
 *
 * @author bhaidu
 */
public class BladeAntrlColoringLexerTest extends BladeAntlrColoringLexerTestBase {

    public BladeAntrlColoringLexerTest(String testName) {
        super(testName);
    }

    @Test
    public void test_smoke_content_tag() throws Exception {
        performTest("coloring_lexer/smoke/content_tag.blade.php");
    }

}
