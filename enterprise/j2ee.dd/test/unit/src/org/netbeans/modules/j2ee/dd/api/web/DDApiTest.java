/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.dd.api.web;

import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import java.io.*;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.filesystems.*;
import java.util.*;


public class DDApiTest extends NbTestCase {
    private static final String VERSION="3.0";
    private static final int TIMEOUT=30;
    private static final int WF_NUMBER=3;
    private static final String SERVLET_NAME = "FordServlet";
    private static final String SERVLET_CLASS = "org.package.mypackage.CarServlet";
    private static final String SERVLET_NAME1 = "VolvoServlet";
    private static final String[] URL_PATTERN = new String[]{"/ford"};
    private static final String URL_PATTERN1 = "/volvo";
    private static final java.math.BigInteger LOAD_ON_STARTUP = java.math.BigInteger.valueOf(10);
    private static final java.math.BigInteger LOAD_ON_STARTUP1 = java.math.BigInteger.valueOf(25);
    private static final String PARAM1 = "car";
    private static final String VALUE1 = "Ford";
    private static final String VALUE11 = "Volvo";
    private static final String PARAM2 = "color";
    private static final String VALUE2 = "red";
    private static final String PARAM3 = "type";
    private static final String VALUE3 = "Puma";
    private static final String DESCRIPTION = "the color of the car";
    private static final String DESCRIPTION_EN = "the colour of the car";
    private static final String DESCRIPTION_DE = "die automobile farbe";
    private static final String DESCRIPTION_CZ = "barva automobilu";
    private static final String DESCRIPTION_SK = "farba automobilu";
    private static final String URL_PATTERN_JSP = "*.jsp";
    private static final String PRELUDE = "/jsp/prelude.jsp";
    private static final String CODA = "/jsp/coda.jsp";
    private static final String LARGE_ICON = "/img/icon32x32.gif";
    private static final String SMALL_ICON = "/img/icon16x16.gif";
    
    private WebApp webApp;

    public DDApiTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(DDApiTest.class);
        
        return suite;
    }
    
    /** Test of greeting method, of class HelloWorld. */
    public void test_InitDataChecking () {
        System.out.println("Init Data Checking");
        String version = webApp.getVersion();
        assertEquals("Incorrect servlet spec.version :",VERSION,version);
        assertEquals("Incorrect number of servlets :",0,webApp.sizeServlet());
        assertEquals("Incorrect Session Timeout :",TIMEOUT,webApp.getSingleSessionConfig().getSessionTimeout().intValue());
        assertEquals("Incorrect number of welcome files : ",WF_NUMBER,webApp.getSingleWelcomeFileList().sizeWelcomeFile());
    }
    
    public void test_Servlet() {
        System.out.println("Testing servlet, servlet-mapping");
        try {
            Servlet servlet = (Servlet) webApp.createBean("Servlet");
            servlet.setServletName(SERVLET_NAME);
            servlet.setServletClass(SERVLET_CLASS);
            servlet.setLoadOnStartup(LOAD_ON_STARTUP);
            webApp.addServlet(servlet);
            ServletMapping25 mapping = (ServletMapping25) webApp.createBean("ServletMapping");
            mapping.setServletName(SERVLET_NAME);
            mapping.setUrlPatterns(URL_PATTERN);
            webApp.addServletMapping(mapping);
            webApp.write(fo);
        } catch (ClassNotFoundException ex) {
            throw new AssertionFailedErrorException("createBean() method failed",ex);
        } catch (java.io.IOException ex) {
            throw new AssertionFailedErrorException("write method failed",ex);
        }
        assertEquals("Incorrect number of servlets :",1,webApp.sizeServlet());
        Servlet s = (Servlet)webApp.findBeanByName("Servlet","ServletName",SERVLET_NAME);
        assertTrue("Servlet "+SERVLET_NAME+" not found", null != s);
        assertEquals("Wrong Servlet Name :",SERVLET_NAME,s.getServletName());
        assertEquals("Wrong Servlet Class :",SERVLET_CLASS,s.getServletClass());
        assertEquals("Wrong load-on-startup :",LOAD_ON_STARTUP,s.getLoadOnStartup());
        
    }
    
    public void test_InitParams() {
        // unfortunately the test depends on actions made by other tests
        test_Servlet();

        System.out.println("Testing init-params, context-params");
        Servlet s = (Servlet)webApp.findBeanByName("Servlet","ServletName",SERVLET_NAME);
        assertTrue("Servlet "+SERVLET_NAME+" not found", null != s);
        try {
            InitParam param = (InitParam) s.createBean("InitParam");
            param.setParamName(PARAM1);
            param.setParamValue(VALUE1);
            s.addInitParam(param);
            InitParam clonnedParam1 = (InitParam)param.clone();
            param = (InitParam) s.createBean("InitParam");
            param.setParamName(PARAM2);
            param.setParamValue(VALUE2);
            s.addInitParam(param);
            InitParam clonnedParam2 = (InitParam)param.clone();
            webApp.setContextParam(new InitParam[]{clonnedParam1, clonnedParam2});
            webApp.write(fo);
        } catch (ClassNotFoundException ex) {
            throw new AssertionFailedErrorException("createBean() method failed",ex);
        } catch (java.io.IOException ex) {
            throw new AssertionFailedErrorException("write method failed",ex);
        }
        s = (Servlet)webApp.findBeanByName("Servlet","ServletName",SERVLET_NAME);
        assertTrue("Servlet "+SERVLET_NAME+" not found", null != s);
        assertEquals("Incorrect number of context-params :",2,webApp.sizeContextParam());
        assertEquals("Incorrect number of init-params in servlet:",2,s.sizeInitParam());
        // context-param test
        InitParam[] params = webApp.getContextParam();
        assertEquals("Incorrect context-param name :",PARAM1,params[0].getParamName());
        assertEquals("Incorrect context-param name :",PARAM2,params[1].getParamName());
        assertEquals("Incorrect context-param value :",VALUE1,params[0].getParamValue());
        assertEquals("Incorrect context-param value :",VALUE2,params[1].getParamValue());
        // init-param test
        assertEquals("Incorrect servlet's init-param name :",PARAM1,s.getInitParam(0).getParamName());
        assertEquals("Incorrect servlet's init-param name :",PARAM2,s.getInitParam(1).getParamName());
        assertEquals("Incorrect servlet's init-param value :",VALUE1,s.getInitParam(0).getParamValue());
        assertEquals("Incorrect servlet's init-param value :",VALUE2,s.getInitParam(1).getParamValue());
        // init-param/context-param, searching
        InitParam p = (InitParam)s.findBeanByName("InitParam","ParamName",PARAM2);
        assertTrue("InitParam "+PARAM2+" not found", null != p);
        p = (InitParam)webApp.findBeanByName("InitParam","ParamName",PARAM1);
        assertTrue("Context Param "+PARAM1+" not found", null != p);
    }
    
    public void test_Description() {
        // unfortunately the test depends on actions made by other tests
        test_InitParams();

        System.out.println("Testing description, description for locales");
        Servlet s = (Servlet)webApp.findBeanByName("Servlet","ServletName",SERVLET_NAME);
        assertTrue("Servlet "+SERVLET_NAME+" not found", null != s);
        InitParam p = (InitParam)s.findBeanByName("InitParam","ParamName",PARAM2);
        assertTrue("InitParam "+PARAM2+" not found", null != p);
        p.setDescription(DESCRIPTION);
        try {
            p.setDescription("en",DESCRIPTION_EN);
            p.setDescription("de",DESCRIPTION);
            p.setDescription("cz",DESCRIPTION_CZ);
            p.setDescription("sk",DESCRIPTION_SK);
            p.setDescription("de",DESCRIPTION_DE); // correction
        } catch (VersionNotSupportedException ex) {
            throw new AssertionFailedErrorException("setDescription() method failed",ex);
        }
        java.util.Map map = p.getAllDescriptions();
        assertEquals("Incorrect size of description :",5,map.size());
        assertEquals("Incorrect default description :",DESCRIPTION,map.get(null));
        assertEquals("Incorrect english description :",DESCRIPTION_EN,map.get("en"));
        assertEquals("Incorrect german description :",DESCRIPTION_DE,map.get("de"));
        assertEquals("Incorrect czech description :",DESCRIPTION_CZ,map.get("cz"));
        assertEquals("Incorrect slovak description :",DESCRIPTION_SK,map.get("sk"));
        try {
            p.removeDescriptionForLocale("de");
        } catch (VersionNotSupportedException ex) {
            throw new AssertionFailedErrorException("removeDescription() method failed",ex);
        }
        assertEquals("Incorrect size of description :",4,p.getAllDescriptions().size());
        assertEquals("Incorrect default description :",DESCRIPTION,p.getDefaultDescription());
        try {
            assertEquals("Incorrect default description :",DESCRIPTION,p.getDescription(null));
            assertEquals("Incorrect english description :",DESCRIPTION_EN,p.getDescription("en"));
            assertEquals("German description was removed :",null,p.getDescription("de"));
            assertEquals("Incorrect czech description :",DESCRIPTION_CZ,p.getDescription("cz"));
            assertEquals("Incorrect slovak description :",DESCRIPTION_SK,p.getDescription("sk"));
        } catch (VersionNotSupportedException ex) {
            throw new AssertionFailedErrorException("getDescription(String locale) method failed",ex);
        }
        try {
            webApp.write(fo);
        } catch (java.io.IOException ex) {
            throw new AssertionFailedErrorException("write method failed",ex);
        }
    }
    
    public void test_addBean() {
        System.out.println("Testing addBean method");
        try {
            InitParam context = (InitParam)webApp.addBean("InitParam",null,null, null);
            context.setParamName(PARAM3);
            context.setParamValue(VALUE3);
            JspConfig jspConfig = (JspConfig)webApp.addBean("JspConfig", null, null, null);
            jspConfig.addBean("JspPropertyGroup",new String[]{"UrlPattern","IncludePrelude","IncludeCoda"},
                    new String[]{URL_PATTERN_JSP,PRELUDE,CODA},null);
            webApp.addBean("Icon",new String[]{"LargeIcon","SmallIcon"},new String[]{LARGE_ICON,SMALL_ICON},null);
        } catch (Exception ex){
            throw new AssertionFailedErrorException("addBean() method failed for ContextParam,JspConfig or Icon",ex);
        }
        // addinng new Servlet
        try {
            Servlet servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass","LoadOnStartup"},
                    new Object[]{SERVLET_NAME1,SERVLET_CLASS,LOAD_ON_STARTUP1}, "ServletName");
            servlet.addBean("InitParam", new String[]{"ParamName","ParamValue"}, new String[]{PARAM1,VALUE11},null);
            webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},new String[]{SERVLET_NAME1,URL_PATTERN1},"UrlPattern");
        } catch (Exception ex){
            new AssertionFailedErrorException("addBean() method failed for Servlet",ex);
        }
        // attempt to add servlet with the same name
        try {
            Servlet servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                    new Object[]{SERVLET_NAME1,SERVLET_CLASS}, "ServletName");
            throw new AssertionFailedError("Servlet shouldn't have been added because of the same name");
        } catch (NameAlreadyUsedException ex){
            System.out.println("Expected exception : "+ex);
        } catch (ClassNotFoundException ex) {
            new AssertionFailedErrorException("addBean() method failed for Servlet",ex);
        }
        try {
            webApp.write(fo);
        } catch (java.io.IOException ex) {
            throw new AssertionFailedErrorException("write method failed",ex);
        }
    }
    
    public void test_Result() {
        // unfortunately the test depends on actions made by other tests
        test_Description();
        test_addBean();

        System.out.println("Comparing result with golden file");
        
        String testDataDirS = System.getProperty("test.data.dir");
        java.io.File pass = new File(getDataDir(),"/web.pass");
        File test = FileUtil.toFile(fo);
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(test));
            BufferedReader reader2 = new BufferedReader(new FileReader(pass));
            String line1=null;
            Set set1 = new HashSet();
            Set set2 = new HashSet();
            while((line1=reader1.readLine())!=null) {
                line1 = line1.trim();
                String line2 = reader2.readLine();
                if (line2==null) {
                    assertFile("Result different than golden file " + pass.getAbsolutePath() + " " + test.getAbsolutePath(), pass, test, test.getParentFile());
                }
                line2=line2.trim();
                // description order can be changed so it must be compared differently
                if (line1.startsWith("<description")) {
                    set1.add(line1);
                    set2.add(line2);
                } else if (!line1.equals(line2)) {
                    assertFile("Result different than golden file " + pass.getAbsolutePath() + " " + test.getAbsolutePath(), pass, test, test.getParentFile());
                }
            }
            reader1.close();reader2.close();
            if (!set1.equals(set2)) {
                assertFile("Problem with descriotion elements", pass, test, test.getParentFile());
            }
        } catch (IOException ex) {
            throw new AssertionFailedErrorException("Comparing to golden file failed",ex);
        }
    }
    
    private static DDProvider ddProvider;
    private static FileObject fo;
    private static boolean initialized;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("setUp() .......................");
        
        if (ddProvider==null) ddProvider = DDProvider.getDefault();
        assertTrue("DDProvider object not found",null != ddProvider);
        
        FileObject dataFolder = FileUtil.toFileObject(getDataDir());
        
        if (!initialized){
            FileObject old = dataFolder.getFileObject("web", "xml");
            if (old != null){
                old.delete();
            }
            initialized = true;
        }
        
        if (fo==null) {
            fo = FileUtil.copyFile(dataFolder.getFileObject("web_org","xml"), dataFolder, "web");
        }
        
        
        assertTrue("FileObject web.xml not found",null != fo);
        
        try {
            webApp = ddProvider.getDDRoot(fo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assertTrue("WebApp object not found", null != webApp);
        
    }

    @Override
    protected void tearDown() throws Exception {
        if (initialized && fo != null) {
            fo.delete();
            fo = null;
            webApp = null;
        }
        super.tearDown();
    }

}
