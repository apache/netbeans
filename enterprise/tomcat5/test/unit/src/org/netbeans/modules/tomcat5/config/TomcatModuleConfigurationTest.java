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

package org.netbeans.modules.tomcat5.config;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.tomcat5.deploy.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.config.gen.Context;
import org.netbeans.modules.tomcat5.config.gen.Parameter;
import org.netbeans.modules.tomcat5.config.gen.ResourceParams;
import org.netbeans.modules.tomcat5.util.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sherold
 */
public class TomcatModuleConfigurationTest extends TestBase {

    final String DRIVER_NAME = "driverName";
    final String DRIVER_DISPLAY_NAME = "driverDisplayName";
    final String DRIVER_CLASS = "driverClass";
    final String USER = "user";
    final String PASSWORD = "password";
    final String DB_URL = "dburl";
    final String SCHEMA = "schema";

    final String SIMPLE_CONTEXT_XML_DATA = "<Context path='/testweb'>\n" +
                                           "</Context>";

    private DatabaseConnection dbCon;

    public TomcatModuleConfigurationTest(java.lang.String testName) {
        super (testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JDBCDriver driver = JDBCDriver.create(DRIVER_NAME, DRIVER_DISPLAY_NAME, DRIVER_CLASS, new URL[]{});
        dbCon = DatabaseConnection.create(driver, DB_URL, USER, SCHEMA, PASSWORD, true);
        ConnectionManager.getDefault().addConnection(dbCon);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (dbCon != null) {
            ConnectionManager.getDefault().removeConnection(dbCon);
        }
        dbCon = null;
    }

    public void testCreateDatasource50() throws Exception {

        final String[][] res = {
            {"jdbc/myDatabase", "jdbc:derby://localhost:1527/sample", "app", "app", "org.apache.derby.jdbc.ClientDriver"},
            {"jdbc/myDatabase1", "jdbc:derby://localhost:1527/sample1", "app1", "app1", "org.apache.derby.jdbc.ClientDriver"},
        };
        J2eeModule j2eeModule = createJ2eeModule("context.xml", SIMPLE_CONTEXT_XML_DATA);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_50, null);

        String dbName = ConnectionManager.getDefault().getConnections()[0].getName();
        for (int i = 0; i < res.length; i++) {
            conf.createDatasource(res[i][0], res[i][1], res[i][2], res[i][3], res[i][4]);
        }

        Context ctx = conf.getContext();
        boolean[] resource = ctx.getResource();
        for (int i = 0; i < res.length; i++) {
            assertEquals("javax.sql.DataSource", ctx.getResourceType(i));
            assertEquals("Container", ctx.getResourceAuth(i));
            assertEquals(res[i][0], ctx.getResourceName(i));

            ResourceParams[] resourceParams = ctx.getResourceParams();
            assertEquals(res[i][0], resourceParams[i].getName());
            Map<String,String> paramMap = new HashMap<>();
            for (Parameter param : resourceParams[i].getParameter()) {
                paramMap.put(param.getName(), param.getValue());
            }
            assertEquals(res[i][1], paramMap.get("url"));
            assertEquals(res[i][2], paramMap.get("username"));
            assertEquals(res[i][3], paramMap.get("password"));
            assertEquals(res[i][4], paramMap.get("driverClassName"));
        }

        // lets try to add an already defined resource now
        try {
            conf.createDatasource(res[0][0], res[0][1], res[0][2], res[0][3], res[0][4]);
            fail();
        } catch (DatasourceAlreadyExistsException e) {
            // expected exception
        }
    }

    public void testCreateDatasource60() throws Exception {

        final String[][] res = {
            {"jdbc/myDatabase", "jdbc:derby://localhost:1527/sample", "app", "app", "org.apache.derby.jdbc.ClientDriver"},
            {"jdbc/myDatabase1", "jdbc:derby://localhost:1527/sample1", "app1", "app1", "org.apache.derby.jdbc.ClientDriver"},
        };

        J2eeModule j2eeModule = createJ2eeModule("context.xml", SIMPLE_CONTEXT_XML_DATA);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_60, null);

        for (int i = 0; i < res.length; i++) {
            conf.createDatasource(res[i][0], res[i][1], res[i][2], res[i][3], res[i][4]);
        }

        Context ctx = conf.getContext();
        boolean[] resource = ctx.getResource();
        for (int i = 0; i < res.length; i++) {
            assertEquals("javax.sql.DataSource", ctx.getResourceType(i));
            assertEquals(res[i][0], ctx.getResourceName(i));
            assertEquals(res[i][1], ctx.getResourceUrl(i));
            assertEquals(res[i][2], ctx.getResourceUsername(i));
            assertEquals(res[i][3], ctx.getResourcePassword(i));
            assertEquals(res[i][4], ctx.getResourceDriverClassName(i));
        }

        // lets try to add an already defined resource now
        try {
            conf.createDatasource(res[0][0], res[0][1], res[0][2], res[0][3], res[0][4]);
            fail();
        } catch (DatasourceAlreadyExistsException e) {
            // expected exception
        }
    }

    public void testBindDatasourceReference60() throws Exception {
        String contextXmlData = "<Context path='/testweb'>\n" +
                                "<Resource name='jdbc/derby'  auth='Container' driverClassName='org.apache.derby.jdbc.ClientDriver' maxActive='20' maxIdle='10' maxWait='-1' password='apppassword' type='javax.sql.DataSource' url='jdbc:derby://localhost:1527/sample' username='app'/> \n" +
                                "</Context>";
        J2eeModule j2eeModule = createJ2eeModule("context.xml", contextXmlData);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_60, null);

        // add a binding when reference name == JNDI name
        conf.bindDatasourceReference("jdbc/derby", "jdbc/derby");
        Context context = conf.getContext();
        // nothing should be changed
        assertEquals(1 , context.getResource().length);
        assertEquals("jdbc/derby", context.getResourceName(0));

        // add another reference to the same DS
        conf.bindDatasourceReference("jdbc/myDerby", "jdbc/derby");
        context = conf.getContext();
        int lengthResource = context.getResource().length;
        assertEquals(1 , lengthResource);
        int lengthLink = context.getResourceLink().length;
        assertEquals(1 , lengthLink);
        boolean myDerbyPresent = false;
        boolean derbyPresent = false;
        for (int i = 0; i < lengthResource; i++) {
            if ("jdbc/myDerby".equals(context.getResourceName(i))
                    && "org.apache.derby.jdbc.ClientDriver".equals(context.getResourceDriverClassName(i))
                    && "app".equals(context.getResourceUsername(i))
                    && "apppassword".equals(context.getResourcePassword(i))
                    && "jdbc:derby://localhost:1527/sample".equals(context.getResourceUrl(i))) {
                myDerbyPresent = true;
            }
            if ("jdbc/derby".equals(context.getResourceName(i))
                    && "org.apache.derby.jdbc.ClientDriver".equals(context.getResourceDriverClassName(i))
                    && "app".equals(context.getResourceUsername(i))
                    && "apppassword".equals(context.getResourcePassword(i))
                    && "jdbc:derby://localhost:1527/sample".equals(context.getResourceUrl(i))) {
                derbyPresent = true;
            }
        }

        // bind global DS with the resource reference
        conf.bindDatasourceReference("jdbc/globalDerbyRef", "jdbc/globalDerby");
        context = conf.getContext();
        assertEquals(1 , context.getResource().length);
        assertEquals(2 , context.getResourceLink().length);

        assertEquals("jdbc/myDerby" , context.getResourceLinkName(0));
        assertEquals("jdbc/derby" , context.getResourceLinkGlobal(0));
        assertEquals("javax.sql.DataSource" , context.getResourceLinkType(0));

        assertEquals("jdbc/globalDerbyRef" , context.getResourceLinkName(1));
        assertEquals("jdbc/globalDerby" , context.getResourceLinkGlobal(1));
        assertEquals("javax.sql.DataSource" , context.getResourceLinkType(1));
    }

    public void testBindDatasourceReference50() throws Exception {
        String contextXmlData = "<Context path='/testweb'>\n" +
                                "  <Resource auth='Container' name='jdbc/derby' type='javax.sql.DataSource'/>\n" +
                                "  <ResourceParams name='jdbc/derby'>\n" +
                                "    <parameter>\n" +
                                "        <name>factory</name>\n" +
                                "        <value>org.apache.commons.dbcp.BasicDataSourceFactory</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>driverClassName</name>\n" +
                                "        <value>org.apache.derby.jdbc.ClientDriver</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>url</name>\n" +
                                "        <value>jdbc:derby://localhost:1527/sample</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>username</name>\n" +
                                "        <value>app</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>password</name>\n" +
                                "        <value>app</value>\n" +
                                "    </parameter>\n" +
                                "  </ResourceParams>\n" +
                                "</Context>";
        J2eeModule j2eeModule = createJ2eeModule("context.xml", contextXmlData);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_50, null);

        // add a binding when reference name == JNDI name
        conf.bindDatasourceReference("jdbc/derby", "jdbc/derby");
        Context context = conf.getContext();
        // nothing should be changed
        assertEquals(1 , context.getResource().length);
        assertEquals(1 , context.getResourceParams().length);
        assertEquals("jdbc/derby", context.getResourceName(0));
        assertEquals("jdbc/derby", context.getResourceParams(0).getName());

        // add another reference to the same DS
        conf.bindDatasourceReference("jdbc/myDerby", "jdbc/derby");
        context = conf.getContext();
        int lengthResource = context.getResource().length;
        assertEquals(1 , lengthResource);
        int lengthLink = context.getResourceLink().length;
        assertEquals(1 , lengthLink);
        assertEquals(1 , context.getResourceParams().length);
        boolean myDerbyPresent = false;
        boolean myDerbyParamsPresent = false;
        boolean derbyPresent = false;
        boolean derbyParamsPresent = false;
        for (int i = 0; i < lengthResource; i++) {
            // check the existence of resource def
            if ("jdbc/myDerby".equals(context.getResourceName(i))) {
                myDerbyPresent = true;
            }
            if ("jdbc/derby".equals(context.getResourceName(i))) {
                derbyPresent = true;
            }
            // check the existence of resource params def
            if ("jdbc/myDerby".equals(context.getResourceParams(i).getName())) {
                myDerbyParamsPresent = true;
            }
            if ("jdbc/derby".equals(context.getResourceParams(i).getName())) {
                derbyParamsPresent = true;
            }
        }

        // bind global DS with the resource reference
        conf.bindDatasourceReference("jdbc/globalDerbyRef", "jdbc/globalDerby");
        context = conf.getContext();
        assertEquals(1 , context.getResource().length);
        assertEquals(2 , context.getResourceLink().length);

        assertEquals("jdbc/myDerby" , context.getResourceLinkName(0));
        assertEquals("jdbc/derby" , context.getResourceLinkGlobal(0));
        assertEquals("javax.sql.DataSource" , context.getResourceLinkType(0));

        assertEquals("jdbc/globalDerbyRef" , context.getResourceLinkName(1));
        assertEquals("jdbc/globalDerby" , context.getResourceLinkGlobal(1));
        assertEquals("javax.sql.DataSource" , context.getResourceLinkType(1));
    }

        public void testGetDatasources60() throws Exception {
        String contextXmlData = "<Context path='/testweb'>\n" +
                                "  <Resource name='jdbc/derby0'  auth='Container' driverClassName='org.apache.derby.jdbc.ClientDriver0' maxActive='20' maxIdle='10' maxWait='-1' password='apppassword0' type='javax.sql.DataSource' url='jdbc:derby://localhost:1527/sample0' username='app0'/> \n" +
                                "  <Resource name='jdbc/derby1'  auth='Container' driverClassName='org.apache.derby.jdbc.ClientDriver1' maxActive='20' maxIdle='10' maxWait='-1' password='apppassword1' type='javax.sql.DataSource' url='jdbc:derby://localhost:1527/sample1' username='app1'/> \n" +
                                "</Context>";
        J2eeModule j2eeModule = createJ2eeModule("context.xml", contextXmlData);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_60, null);
        boolean ds0Present = false;
        boolean ds1Present = false;
        for (Datasource ds : conf.getDatasources()) {
            if (ds.getUsername().equals("app0") && ds.getPassword().equals("apppassword0")
                    && ds.getUrl().equals("jdbc:derby://localhost:1527/sample0")
                    && ds.getJndiName().equals("jdbc/derby0")
                    && ds.getDriverClassName().equals("org.apache.derby.jdbc.ClientDriver0")) {
                ds0Present = true;
            }
            if (ds.getUsername().equals("app1") && ds.getPassword().equals("apppassword1")
                    && ds.getUrl().equals("jdbc:derby://localhost:1527/sample1")
                    && ds.getJndiName().equals("jdbc/derby1")
                    && ds.getDriverClassName().equals("org.apache.derby.jdbc.ClientDriver1")) {
                ds1Present = true;
            }
        }
        assertTrue(ds0Present);
        assertTrue(ds1Present);
    }

    public void testGetDatasources50() throws Exception {
        String contextXmlData = "<Context path='/testweb'>\n" +
                                "  <Resource auth='Container' name='jdbc/derby0' type='javax.sql.DataSource'/>\n" +
                                "  <ResourceParams name='jdbc/derby0'>\n" +
                                "    <parameter>\n" +
                                "        <name>factory</name>\n" +
                                "        <value>org.apache.commons.dbcp.BasicDataSourceFactory</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>driverClassName</name>\n" +
                                "        <value>org.apache.derby.jdbc.ClientDriver0</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>url</name>\n" +
                                "        <value>jdbc:derby://localhost:1527/sample0</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>username</name>\n" +
                                "        <value>app0</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>password</name>\n" +
                                "        <value>apppassword0</value>\n" +
                                "    </parameter>\n" +
                                "  </ResourceParams>\n" +
                                "  <Resource auth='Container' name='jdbc/derby1' type='javax.sql.DataSource'/>\n" +
                                "  <ResourceParams name='jdbc/derby1'>\n" +
                                "    <parameter>\n" +
                                "        <name>factory</name>\n" +
                                "        <value>org.apache.commons.dbcp.BasicDataSourceFactory</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>driverClassName</name>\n" +
                                "        <value>org.apache.derby.jdbc.ClientDriver1</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>url</name>\n" +
                                "        <value>jdbc:derby://localhost:1527/sample1</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>username</name>\n" +
                                "        <value>app1</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>password</name>\n" +
                                "        <value>apppassword1</value>\n" +
                                "    </parameter>\n" +
                                "  </ResourceParams>\n" +
                                "</Context>";
        J2eeModule j2eeModule = createJ2eeModule("context.xml", contextXmlData);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_50, null);
        boolean ds0Present = false;
        boolean ds1Present = false;
        for (Datasource ds : conf.getDatasources()) {
            if (ds.getUsername().equals("app0") && ds.getPassword().equals("apppassword0")
                    && ds.getUrl().equals("jdbc:derby://localhost:1527/sample0")
                    && ds.getJndiName().equals("jdbc/derby0")
                    && ds.getDriverClassName().equals("org.apache.derby.jdbc.ClientDriver0")) {
                ds0Present = true;
            }
            if (ds.getUsername().equals("app1") && ds.getPassword().equals("apppassword1")
                    && ds.getUrl().equals("jdbc:derby://localhost:1527/sample1")
                    && ds.getJndiName().equals("jdbc/derby1")
                    && ds.getDriverClassName().equals("org.apache.derby.jdbc.ClientDriver1")) {
                ds1Present = true;
            }
        }
        assertTrue(ds0Present);
        assertTrue(ds1Present);
    }

    public void testFindDatasourceJndiName60() throws Exception {
        String contextXmlData = "<Context path='/testweb'>\n" +
                                "<Resource name='jdbc/derby'  auth='Container' driverClassName='org.apache.derby.jdbc.ClientDriver' maxActive='20' maxIdle='10' maxWait='-1' password='apppassword' type='javax.sql.DataSource' url='jdbc:derby://localhost:1527/sample' username='app'/> \n" +
                                "<ResourceLink name='jdbc/globalDerbyRef' global='jdbc/globalDerby' type='javax.sql.DataSource'/>\n" +
                                "</Context>";
        J2eeModule j2eeModule = createJ2eeModule("context.xml", contextXmlData);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_60, null);
        // existing ref name (equal the JNDI name)
        assertEquals("jdbc/derby", conf.findDatasourceJndiName("jdbc/derby"));
        // non-existing ref name
        assertNull(conf.findDatasourceJndiName("jdbc/derby1"));
        // existing ref name
        assertEquals("jdbc/globalDerby", conf.findDatasourceJndiName("jdbc/globalDerbyRef"));
        // non-existing ref name
        assertNull(conf.findDatasourceJndiName("jdbc/globalDerby"));
    }

    public void testFindDatasourceJndiName50() throws Exception {
        String contextXmlData = "<Context path='/testweb'>\n" +
                                "  <Resource auth='Container' name='jdbc/derby' type='javax.sql.DataSource'/>\n" +
                                "  <ResourceParams name='jdbc/derby'>\n" +
                                "    <parameter>\n" +
                                "        <name>factory</name>\n" +
                                "        <value>org.apache.commons.dbcp.BasicDataSourceFactory</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>driverClassName</name>\n" +
                                "        <value>org.apache.derby.jdbc.ClientDriver</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>url</name>\n" +
                                "        <value>jdbc:derby://localhost:1527/sample</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>username</name>\n" +
                                "        <value>app</value>\n" +
                                "    </parameter>\n" +
                                "    <parameter>\n" +
                                "        <name>password</name>\n" +
                                "        <value>app</value>\n" +
                                "    </parameter>\n" +
                                "  </ResourceParams>\n" +
                                "  <ResourceLink name='jdbc/globalDerbyRef' global='jdbc/globalDerby' type='javax.sql.DataSource'/>\n" +
                                "</Context>";
        J2eeModule j2eeModule = createJ2eeModule("context.xml", contextXmlData);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_50, null);
        // existing ref name (equal the JNDI name)
        assertEquals("jdbc/derby", conf.findDatasourceJndiName("jdbc/derby"));
        // non-existing ref name
        assertNull(conf.findDatasourceJndiName("jdbc/derby1"));
        // existing ref name
        assertEquals("jdbc/globalDerby", conf.findDatasourceJndiName("jdbc/globalDerbyRef"));
        // non-existing ref name
        assertNull(conf.findDatasourceJndiName("jdbc/globalDerby"));
    }

    public void testThrowsConfigurationExceptionWhenBrokenContextXml() throws Exception {
        String contextXmlData = "<Context ";
        J2eeModule j2eeModule = createJ2eeModule("context.xml", contextXmlData);
        for (TomcatVersion version : new TomcatVersion[]{TomcatVersion.TOMCAT_50, TomcatVersion.TOMCAT_55, TomcatVersion.TOMCAT_60}) {
            TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, version, null);
            try {
                conf.getContextRoot();
                fail();
            } catch (ConfigurationException e) {
                // expected exception
            }
            try {
                conf.setContextRoot("/dsf");
                fail();
            } catch (ConfigurationException e) {
                // expected exception
            }
            try {
                conf.createDatasource("jdbc/derby", "jdbc:derby://localhost:1527/sample", "app", "app", "org.apache.derby.jdbc.ClientDriver");
                fail();
            } catch (ConfigurationException e) {
                // expected exception
            }
            try {
                conf.bindDatasourceReference("jdbc/derbyRef", "jdbc/derby");
                fail();
            } catch (ConfigurationException e) {
                // expected exception
            }
            try {
                conf.findDatasourceJndiName("jdbc/derbyRef");
                fail();
            } catch (ConfigurationException e) {
                // expected exception
            }
            try {
                conf.getDatasources();
                fail();
            } catch (ConfigurationException e) {
                // expected exception
            }
        }
    }

    public void testSetContextPath50() throws Exception {
        J2eeModule j2eeModule = createJ2eeModule("context.xml", SIMPLE_CONTEXT_XML_DATA);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_50, null);
        String[][] res = {
            {"/test", null},
            {"/test/test", null},
            {"", null},
        };
        for (int i = 0; i < res.length; i++) {
            conf.setContextRoot(res[i][0]);
            assertEquals(res[i][0], conf.getContextRoot());
            assertEquals(res[i][1], conf.getContext().getLoggerPrefix()); // no logger should be defined
        }


        String contextXmlData = "<Context path='/testweb'>\n" +
                                "<Logger prefix='testweb.'/> \n" +
                                "</Context>";
        J2eeModule j2eeModule1 = createJ2eeModule("context1.xml", contextXmlData);
        TomcatModuleConfiguration conf1 = new TomcatModuleConfiguration(j2eeModule1, TomcatVersion.TOMCAT_50, null);
        String[][] res1 = {
            {"/test", "test."},
            {"/test/test", "test_test."},
            {"", "ROOT."},
        };
        for (int i = 0; i < res.length; i++) {
            conf.setContextRoot(res[i][0]);
            assertEquals(res[i][0], conf.getContextRoot());
            assertEquals(res[i][1], conf.getContext().getLoggerPrefix());
        }
    }

    public void testSetContextPath60() throws Exception {
        J2eeModule j2eeModule = createJ2eeModule("context.xml", SIMPLE_CONTEXT_XML_DATA);
        TomcatModuleConfiguration conf = new TomcatModuleConfiguration(j2eeModule, TomcatVersion.TOMCAT_60, null);
        String[] res = {
            "/test",
            "/test/test",
            "",
        };
        for (int i = 0; i < res.length; i++) {
            conf.setContextRoot(res[i]);
            assertEquals(res[i], conf.getContextRoot());
            assertEquals(null, conf.getContext().getLoggerPrefix()); // no logger should be defined
        }
    }

    public void testOldCreate50MethodForAutoupdateModule() {
        assertNotNull(TomcatModuleConfigurationFactory.create50());
    }

    private J2eeModule createJ2eeModule(String fileName, String content) throws Exception {
        File file = new File(getWorkDir(), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        J2eeModuleImpl j2eeModuleImpl = new J2eeModuleImpl(file);
        return J2eeModuleFactory.createJ2eeModule(j2eeModuleImpl);
    }

    private static class J2eeModuleImpl implements J2eeModuleImplementation2 {

        private final File contextXml;

        public J2eeModuleImpl(File contextXml) {
            this.contextXml = contextXml;
        }

        @Override
        public String getModuleVersion() {
            return J2eeModule.J2EE_14;
        }

        @Override
        public J2eeModule.Type getModuleType() {
            return J2eeModule.Type.WAR;
        }

        @Override
        public String getUrl() {
            return null;
        }

        public void setUrl(String url) {
        }

        @Override
        public FileObject getArchive() throws IOException {
            return null;
        }

        @Override
        public Iterator getArchiveContents() throws IOException {
            return null;
        }

        @Override
        public FileObject getContentDirectory() throws IOException {
            return null;
        }

        public RootInterface getDeploymentDescriptor(String location) {
            return null;
        }

        @Override
        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            return null;
        }

        @Override
        public File getResourceDirectory() {
            return null;
        }

        @Override
        public File getDeploymentConfigurationFile(String name) {
            return contextXml;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

    };
}
