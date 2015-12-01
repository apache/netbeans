package org.black.kotlin.highlighter;

import static org.black.kotlin.highlighter.TokenType.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TokenTypeTest {
    public TokenTypeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of values method, of class TokenType.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        TokenType[] expResult = null;
        TokenType[] result = TokenType.values();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of valueOf method, of class TokenType.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "";
        TokenType expResult = null;
        TokenType result = TokenType.valueOf(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getId method, of class TokenType.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        TokenType instance = null;
        int expResult = 0;
        int result = instance.getId();
        assertEquals(KEYWORD, 0);
        assertEquals(STRING, 1);        
        assertEquals(IDENTIFIER, 2);
        assertEquals(SINGLE_LINE_COMMENT, 3);
        assertEquals(MULTI_LINE_COMMENT, 4);
        assertEquals(KDOC_TAG_NAME, 5);
        assertEquals(WHITESPACE, 6);
        assertEquals(UNDEFINED, 7);
        assertEquals(EOF, 7);        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
