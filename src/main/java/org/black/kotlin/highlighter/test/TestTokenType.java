package org.black.kotlin.highlighter.test;

import junit.framework.TestCase;
import org.black.kotlin.highlighter.TokenType;

/**
 *
 * @author Александр
 */
public class TestTokenType extends TestCase {
    private static TokenType type;
    private int id = 1;
    
    static {
        type = TokenType.KEYWORD;
    }
    
    public TestTokenType(String name){
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        id = type.getId(type);
    }
    
    @Override
    protected void tearDown () throws Exception {
    }
    
    public void testTokenType() throws Exception {
        assertEquals("Keyword id is 0",id,0);
    }
}
