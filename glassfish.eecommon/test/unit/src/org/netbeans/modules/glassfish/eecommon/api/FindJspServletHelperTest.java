package org.netbeans.modules.glassfish.eecommon.api;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FindJspServletHelperTest {

    public FindJspServletHelperTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    //test copied from
    //j2ee.sun.appsrv81/test/unit/src/org/netbeans/modules/j2ee/sun/ide/j2ee/jsps/FindJSPServletImplTest.java
    /**
     * Test of getServletResourcePath method, of class FindJSPServletHelper.
     * 
     */
    @org.junit.Test
    public void testGetServletResourcePath() {
        System.out.println("getServletResourcePath");
        String moduleContextPath = "";
        String jspResourcePath = "/test/index.jsp";
        String expResult = "org/apache/jsp/test/index_jsp.java";
        String result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "/index.jsp";
        expResult = "org/apache/jsp/index_jsp.java";
        result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "index.jsp";
        expResult = "org/apache/jsp/index_jsp.java";
        result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        jspResourcePath = "a";
        expResult = "org/apache/jsp/a.java";
        result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
        assertEquals(expResult, result);
        try {
            jspResourcePath = "";
            expResult = "";
            result = FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
            fail("should have triggered an exception");            
        } catch (IllegalArgumentException iae) {
            
        }
    }

    
}
