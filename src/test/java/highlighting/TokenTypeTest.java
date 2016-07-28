package highlighting;

import org.black.kotlin.highlighter.TokenType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Александр
 */
public class TokenTypeTest {
    
    public TokenTypeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getId method, of class TokenType.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        assertEquals(1, TokenType.IDENTIFIER.getId());
        assertEquals(0, TokenType.KEYWORD.getId());
        assertEquals(6, TokenType.WHITESPACE.getId());
        assertEquals(2, TokenType.STRING.getId());
    }
    
}
