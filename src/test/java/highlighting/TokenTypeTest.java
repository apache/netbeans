package highlighting;

import org.jetbrains.kotlin.highlighter.TokenType;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Александр
 */
public class TokenTypeTest extends NbTestCase{

    public TokenTypeTest() {
        super("Token type test");
    }
    /**
     * Test of getId method, of class TokenType.
     */
    @Test
    public void testGetId() {
        assertEquals(1, TokenType.IDENTIFIER.getId());
        assertEquals(0, TokenType.KEYWORD.getId());
        assertEquals(6, TokenType.WHITESPACE.getId());
        assertEquals(2, TokenType.STRING.getId());
    }
    
}
