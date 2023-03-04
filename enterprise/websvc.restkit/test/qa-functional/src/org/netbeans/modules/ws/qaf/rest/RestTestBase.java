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
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.modules.ws.qaf.WebServicesTestBase;
import org.netbeans.modules.ws.qaf.utilities.ContentComparator;
import org.netbeans.modules.ws.qaf.utilities.FilteringLineDiff;
import org.netbeans.modules.ws.qaf.utilities.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Base class for REST tests
 * @author lukas
 */
public abstract class RestTestBase extends WebServicesTestBase {

    //don't try to (un)deploy REST apps on windows!!!
    //see: https://jersey.dev.java.net/issues/show_bug.cgi?id=45
    private static final String HOSTNAME = "localhost"; //NOI18N
    private static final int PORT = resolveServerPort();
    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver"; //NOI18N
    private static final String JDBC_URL = "jdbc:derby://localhost:1527/sample"; //NOI18N
    private static final String DB_USERNAME = "app"; //NOI18N
    private static final String DB_PASSWORD = "app"; //NOI18N
    private Connection connection;
    private static boolean CREATE_GOLDEN_FILES = Boolean.getBoolean("golden");
    private static final Logger LOGGER = Logger.getLogger(RestTestBase.class.getName());

    /**
     * Enum type to hold supported Mime Types
     */
    protected enum MimeType {

        APPLICATION_XML,
        APPLICATION_JSON,
        TEXT_PLAIN,
        TEXT_HTML;

        @Override
        public String toString() {
            switch (this) {
                case APPLICATION_XML:
                    return "application/xml"; //NOI18N
                case APPLICATION_JSON:
                    return "application/json"; //NOI18N
                case TEXT_PLAIN:
                    return "text/plain"; //NOI18N
                case TEXT_HTML:
                    return "text/html"; //NOI18N
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    public RestTestBase(String name) {
        super(name);
    }

    public RestTestBase(String name, Server server) {
        super(name, server);
//        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //XXX - avoid doubled start/restart of the server
        //because of JDBC driver deployment (IZ #106586)
//        workaroundIZ106586();
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException ex) {
        } catch (ClassNotFoundException cnfe) {
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
        }
    }

    /**
     * Deploy a project returned by <code>getProjectName()</code>
     */
    public void testDeploy() throws IOException {
        deployProject(getProjectName());
    }

    public void testRun() throws IOException {
        runProject(getProjectName());
    }

    /**
     * Undeploy a project returned by <code>getProjectName()</code>
     */
    public void testUndeploy() throws IOException {
        undeployProject(getProjectName());
    }

    /**
     * Close project
     */
    public void testCloseProject() {
        // Close
        String close = Bundle.getStringTrimmed("org.netbeans.core.ui.Bundle", "LBL_Close");
        getProjectRootNode().performPopupAction(close);
        System.gc();
    }

    /**
     * Helper method to get folder containing data for Rest tests
     * (&lt;dataDir&gt/resources)
     */
    protected File getRestDataDir() {
        return new File(getDataDir(), "resources"); //NOI18N
    }

    /**
     * Helper method to get RESTful Web Services node
     *
     * @return RESTful Web Services node
     */
    protected Node getRestNode() {
        waitScanFinished();
        String restNodeLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_RestServices");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 120000); //NOI18N
        Node restNode = new Node(getProjectRootNode(), restNodeLabel);
        if (restNode.isCollapsed()) {
            restNode.expand();
        }
        return restNode;
    }
    
    /**
     * Waits for defined number of children under RESTful Web Services node. It
     * throws TimeoutExpiredException if timeout passes.
     */
    protected void waitRestNodeChildren(final int count) {
        final Node restNode = getRestNode();
        try {
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    return restNode.getChildren().length == count ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return count + " children under " + restNode.getText(); //NOI18N
                }
            }).waitAction(null);
        } catch (InterruptedException ie) {
            throw new JemmyException("Interrupted.", ie); //NOI18N
        }
    }

    /**
     * Helper method to get URL on which current project will become available
     */
    protected String getRestAppURL() {
        return "http://" + HOSTNAME + ":" + PORT + "/" + getProjectName(); //NOI18N
    }

    /**
     * Helper method to get URL on which REST services from current project will
     * become available
     */
    protected String getResourcesURL() {
        return getRestAppURL() + "/resources"; //NOI18N
    }

    /**
     * Run given <code>query</code> against sample database
     * @param query query to run
     * @return result of given <code>query</code>
     * @throws java.sql.SQLException
     */
    protected ResultSet doQuery(String query) throws SQLException {
        return connection.createStatement().executeQuery(query);
    }

    /**
     * Create Rest test client under Web Pages in tested project so we're able
     * to test it
     * <strong>Note</strong>: This method uses reflection to call appropriate
     * method from RestSuport class because RESTful plugin is not part of
     * standart NetBeans distribution yet
     *
     * @throws java.io.IOException
     */
    protected void prepareRestClient() throws IOException {
        //generate Rest test client into the <projectDir>/web directory
        Class<?> c = null;
        try {
            c = Class.forName("org.netbeans.modules.websvc.rest.spi.RestSupport");
        } catch (ClassNotFoundException cnfe) {
            fail("Cannot find RestSupport class. Please make sure RESTful Web Services plugin is installed.");
        }
        assertNotNull(c);
        Object o = getProject().getLookup().lookup(c);
        File restClientFolder = new File(FileUtil.toFile(getProject().getProjectDirectory()), "web"); //NOI18N
        try {
            Method m = c.getMethod("generateTestClient", File.class);
            m.invoke(o, restClientFolder);
        } catch (IllegalAccessException iae) {
            LOGGER.info(iae.getMessage());
        } catch (InvocationTargetException ite) {
            LOGGER.info(ite.getMessage());
        } catch (NoSuchMethodException nsme) {
            LOGGER.info(nsme.getMessage());
        }
        Field f = null;
        File restClient = null;
        try {
            f = c.getDeclaredField("TEST_RESBEANS_HTML"); //NOI18N
            restClient = new File(restClientFolder, (String) f.get(o));
        } catch (IllegalAccessException iae) {
            LOGGER.info(iae.getMessage());
        } catch (NoSuchFieldException nsfe) {
            LOGGER.info(nsfe.getMessage());
        }
        //make sure the test client has been generated
        assertNotNull(f);
        assertNotNull(restClient);
        assertTrue(restClient.exists());
        assertTrue(restClient.isFile());
        //replace "___BASE_URL___" with proper URL of generated Rest resources
        //(this is usually done by "Test RESTful Web Services" action; this action
        // also opens browser, but this is something what we don't want to do
        // in tests)
        FileObject restClientFO = FileUtil.toFileObject(FileUtil.normalizeFile(restClient));
        DataObject dobj = DataObject.find(restClientFO);
        assertNotNull(dobj);
        EditorCookie editorCookie = dobj.getLookup().lookup(EditorCookie.class);
        assertNotNull(editorCookie);
        editorCookie.open();
        try {
            EditorOperator eo = new EditorOperator((String) f.get(o));
            assertNotNull(eo);
            f = c.getDeclaredField("BASE_URL_TOKEN"); //NOI18N
            assertNotNull(f);
            eo.replace((String) f.get(o), getRestAppURL() + "/"); //NOI18N
            eo.close(true);
        } catch (IllegalAccessException iae) {
            LOGGER.info(iae.getMessage());
        } catch (NoSuchFieldException nsfe) {
            LOGGER.info(nsfe.getMessage());
        }
    }

    /**
     * Check files against golden files.
     *
     *@param newFiles files to check
     */
    protected void checkFiles(Set<File> newFiles) {
        // save all instead of timeout
        new SaveAllAction().performAPI();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        if (!CREATE_GOLDEN_FILES) {
            Set<String> set = new HashSet<String>(newFiles.size() / 2);
            for (Iterator<File> i = newFiles.iterator(); i.hasNext();) {
                File newFile = i.next();
                File goldenFile;
                try {
                    goldenFile = getGoldenFile(getName() + "/" + newFile.getName() + ".pass"); //NOI18N
                    if (newFile.getName().endsWith(".xml") //NOI18N
                            && !newFile.getName().startsWith("sun-") //NOI18N
                            && !newFile.getName().startsWith("webservices.xml")) { //NOI18N
                        assertTrue(ContentComparator.equalsXML(goldenFile, newFile));
                    } else {
                        assertFile(goldenFile, newFile,
                                new File(getWorkDirPath(), newFile.getName() + ".diff"), //NOI18N
                                new FilteringLineDiff());
                    }
                } catch (Throwable t) {
                    goldenFile = getGoldenFile(getName() + "/" + newFile.getName() + ".pass"); //NOI18N
                    Utils.copyFile(newFile, new File(getWorkDirPath(), newFile.getName() + ".bad.txt")); //NOI18N
                    Utils.copyFile(goldenFile,
                            new File(getWorkDirPath(), newFile.getName() + ".gf.txt")); //NOI18N
                    set.add(newFile.getName());
                }
            }
            assertTrue("File(s) " + set.toString() + " differ(s) from golden files.", set.isEmpty()); //NOI18N
        } else {
            createGoldenFiles(newFiles);
        }
    }

    @Override
    public File getGoldenFile(String filename) {
        String fullClassName = this.getClass().getName().replace("Mvn", "").replaceAll("rest.*[6,7]", "rest.");
        String goldenFileName = fullClassName.replace('.', '/') + "/" + filename;
        // golden files are in ${xtest.data}/goldenfiles/${classname}/...
        File goldenFile = new File(getDataDir() + "/goldenfiles/" + goldenFileName);
        assertTrue("Golden file not found in the following locations:\n  " + goldenFile,
                goldenFile.exists());
        return goldenFile;
    }

    private void createGoldenFiles(Set<File> from) {
        File f = getDataDir();
        while (!f.getName().equals("test")) { //NOI18N
            f = f.getParentFile();
        }
        f = new File(f, "qa-functional/data/goldenfiles"); //NOI18N
        f = new File(f, getClass().getName().replace('.', File.separatorChar));
        File destDir = new File(f, getName());
        destDir.mkdirs();
        for (Iterator<File> i = from.iterator(); i.hasNext();) {
            File src = i.next();
            Utils.copyFile(src, new File(destDir, src.getName() + ".pass")); //NOI18N
        }
        assertTrue("Golden files generated.", false); //NOI18N
    }

    @Override
    protected void deployProject(String projectName) throws IOException {
            super.deployProject(projectName);       
    }

    @Override
    protected void undeployProject(String projectName) throws IOException {
            super.undeployProject(projectName);
    }

    private static int resolveServerPort() {
        Integer i = Integer.getInteger("glassfish.server.port"); //NOI18N
        return i != null ? i : 8080;
    }

    private void workaroundIZ106586() {
        String gfRoot = System.getProperty("glassfish.home"); //NOI18N
        if (gfRoot == null) {
            return;
        }
        File f = new File(gfRoot, "domains/domain1/lib/derbyclient.jar"); //NOI18N
        if (f.exists() && f.isFile()) {
            return;
        }
        f = FileUtil.normalizeFile(f);
        File origDriver = new File(gfRoot, "javadb/lib/derbyclient.jar"); //NOI18N
        origDriver = FileUtil.normalizeFile(origDriver);
        FileObject targetFolder = FileUtil.toFileObject(f.getParentFile());
        try {
            FileUtil.copyFile(FileUtil.toFileObject(origDriver), targetFolder, "derbyclient"); //NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "JDBC Driver was not copied", ex); //NOI18N
        }
        File jar = FileUtil.toFile(targetFolder.getFileObject("derbyclient", "jar")); //NOI18N
        LOGGER.log(Level.INFO, "JDBC Driver was copied to: {0}", jar.getAbsolutePath()); //NOI18N
    }
}
