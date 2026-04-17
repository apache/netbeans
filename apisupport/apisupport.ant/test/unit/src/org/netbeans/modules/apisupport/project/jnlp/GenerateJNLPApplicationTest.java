/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.jnlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;

/**
 * Checks JNLP support behaviour.
 * @author Jaroslav Tulach
 */
public class GenerateJNLPApplicationTest extends TestBase {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
    }
    
    private SuiteProject suite;
    private Logger LOG;
    
    public GenerateJNLPApplicationTest(String name) {
        super(name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected @Override void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        
        super.setUp();

        InstalledFileLocatorImpl.registerDestDir(destDirF);
        
        suite = TestBase.generateSuite(new File(getWorkDir(), "projects"), "suite");
        NbModuleProject proj = TestBase.generateSuiteComponent(suite, "mod1");
        
        suite.open();
        proj.open();
    }
    
    public void testBuildTheJNLPAppWhenAppNamePropIsNotSet() throws Exception {
        SuiteActions p = (SuiteActions) suite.getLookup().lookup(ActionProvider.class);
        assertNotNull("Provider is here");
        
        List l = Arrays.asList(p.getSupportedActions());
        assertTrue("We support build-jnlp: " + l, l.contains("build-jnlp"));
        
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
        ExecutorTask task = p.invokeActionImpl("build-jnlp", suite.getLookup());
        assertNull("did not even run task", task);
    }

    //    XXX: failing test, fix or delete
//    public void testBuildTheJNLPAppWhenAppNamePropIsSet() throws Exception {
//        EditableProperties ep = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        ep.setProperty("app.name", "fakeapp");
//
//        File someJar = createNewJarFile("fake-jnlp-servlet");
//
//        ep.setProperty("enabled.clusters", TestBase.CLUSTER_PLATFORM);
//        ep.setProperty("disabled.modules", "org.netbeans.modules.autoupdate," +
//            "org.openide.compat," +
//            "org.netbeans.api.progress," +
//            "org.netbeans.core.multiview," +
//            "org.openide.filesystems," +
//            "org.openide.modules," +
//            "org.openide.util," +
//            "org.netbeans.core.execution," +
//            "org.netbeans.core.output2," +
//            "org.netbeans.core.ui," +
//            "org.netbeans.core.windows," +
//            "org.netbeans.core," +
//            "org.netbeans.modules.favorites," +
//            "org.netbeans.modules.javahelp," +
//            "org.netbeans.modules.masterfs," +
//            "org.netbeans.modules.queries," +
//            "org.netbeans.modules.settings," +
//            "org.netbeans.swing.plaf," +
//            "org.netbeans.swing.tabcontrol," +
//            "org.openide.actions," +
//            "org.openide.awt," +
//            "org.openide.dialogs," +
//            "org.openide.execution," +
//            "org.openide.explorer," +
//            "org.openide.io," +
//            "org.openide.loaders," +
//            "org.openide.nodes," +
//            "org.openide.options," +
//            "org.openide.text," +
//            "org.openide.windows," +
//            "");
//        ep.setProperty("jnlp.servlet.jar", someJar.toString());
//        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
//        ProjectManager.getDefault().saveProject(suite);
//        LOG.info("Properties stored");
//
//        SuiteActions p = (SuiteActions)suite.getLookup().lookup(ActionProvider.class);
//        assertNotNull("Provider is here");
//
//        List l = Arrays.asList(p.getSupportedActions());
//        assertTrue("We support build-jnlp: " + l, l.contains("build-jnlp"));
//
//        DialogDisplayerImpl.returnFromNotify(null);
//        LOG.info("invoking build-jnlp");
//        ExecutorTask task = p.invokeActionImpl("build-jnlp", suite.getLookup());
//        LOG.info("Invocation started");
//
//        assertNotNull("Task was started", task);
//        LOG.info("Waiting for task to finish");
//        task.waitFinished();
//        LOG.info("Checking the result");
//        assertEquals("Finished ok", 0, task.result());
//        LOG.info("Testing the content of the directory");
//
//        FileObject[] arr = suite.getProjectDirectory().getChildren();
//        List<FileObject> subobj = new ArrayList<FileObject>(Arrays.asList(arr));
//        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
//        FileObject master = suite.getProjectDirectory().getFileObject("master.jnlp");
//        assertNotNull("Master must be created", master);
//        FileObject branding = suite.getProjectDirectory().getFileObject("branding.jnlp");
//        assertNotNull("Branding must be created", branding);
//        subobj.remove(master);
//        subobj.remove(branding);
//        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
//        FileObject dist = suite.getProjectDirectory().getFileObject("dist");
//        assertNotNull("dist created", dist);
//        subobj.remove(dist);
//
//        if (!subobj.isEmpty()) {
//            fail("There should be no created directories in the suite dir: " + subobj);
//        }
//
//        FileObject war = dist.getFileObject("fakeapp.war");
//        assertNotNull("War file created: " + war, war);
//
//        File warF = FileUtil.toFile(war);
//        JarFile warJ = new JarFile(warF);
//        Enumeration en = warJ.entries();
//        int cntJnlp = 0;
//        while (en.hasMoreElements()) {
//            JarEntry entry = (JarEntry)en.nextElement();
//            if (!entry.getName().endsWith(".jnlp")) {
//                continue;
//            }
//            cntJnlp++;
//
//            byte[] data = new byte[(int)entry.getSize()];
//            int len = 0;
//            InputStream is = warJ.getInputStream(entry);
//            for(int pos = 0; pos < data.length; ) {
//                int r = is.read(data, pos, data.length - pos);
//                pos += r;
//                len += r;
//            }
//            is.close();
//            assertEquals("Correct data read: " + entry, data.length, len);
//
//            String s = new String(data);
//            if (s.indexOf(getWorkDir().getName()) >= 0) {
//                fail("Name of work dir in a file, means that there is very likely local reference to a file: " + entry + "\n" + s);
//            }
//        }
//
//        if (cntJnlp == 0) {
//            fail("There should be at least one jnlp entry");
//        }
//    }

//    XXX: failing test, fix or delete
//    public void testItIsPossibleToGenerateStaticRepository() throws Exception {
//        EditableProperties ep = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        ep.setProperty("app.name", "fakeapp");
//
//        ep.setProperty("enabled.clusters", TestBase.CLUSTER_PLATFORM);
//        ep.setProperty("disabled.modules", "org.netbeans.modules.autoupdate," +
//            "org.openide.compat," +
//            "org.netbeans.api.progress," +
//            "org.netbeans.core.multiview," +
//            "org.openide.filesystems," +
//            "org.openide.modules," +
//            "org.openide.util," +
//            "org.netbeans.core.execution," +
//            "org.netbeans.core.output2," +
//            "org.netbeans.core.ui," +
//            "org.netbeans.core.windows," +
//            "org.netbeans.core," +
//            "org.netbeans.modules.favorites," +
//            "org.netbeans.modules.javahelp," +
//            "org.netbeans.modules.masterfs," +
//            "org.netbeans.modules.queries," +
//            "org.netbeans.modules.settings," +
//            "org.netbeans.swing.plaf," +
//            "org.netbeans.swing.tabcontrol," +
//            "org.openide.actions," +
//            "org.openide.awt," +
//            "org.openide.dialogs," +
//            "org.openide.execution," +
//            "org.openide.explorer," +
//            "org.openide.io," +
//            "org.openide.loaders," +
//            "org.openide.nodes," +
//            "org.openide.options," +
//            "org.openide.text," +
//            "org.openide.windows," +
//            "");
//        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
//        ProjectManager.getDefault().saveProject(suite);
//        LOG.info("Properties stored");
//
//        FileObject fo = suite.getProjectDirectory().getFileObject("build.xml");
//        assertNotNull("There should be a build script", fo);
//
//        {
//            String s = readFile(fo);
//            int insert = s.indexOf("</project>");
//            if (insert == -1) {
//                fail("there should be insert place: " + s);
//            }
//
//            s = s.substring(0, insert) +
//                "  <target name='build-jnlp' depends='build,-jdk-init' description='Builds a static JNLP version of the app.' >" +
//                "   <ant antfile='${harness.dir}/jnlp.xml' target='build-jnlp-nowar'>" +
//                "       <property name='jnlp.codebase' value='http://www.netbeans.org/download/samples/jnlp/htmleditor/' />" +
//                "   </ant>" +
//                "</target>"
//                + s.substring(insert);
//            FileLock lock = fo.lock();
//            OutputStream os = fo.getOutputStream(lock);
//            try {
//                os.write(s.getBytes());
//            } finally {
//                os.close();
//                lock.releaseLock();
//            }
//        }
//
//        SuiteActions p = (SuiteActions)suite.getLookup().lookup(ActionProvider.class);
//        assertNotNull("Provider is here");
//
//        List l = Arrays.asList(p.getSupportedActions());
//        assertTrue("We support build-jnlp: " + l, l.contains("build-jnlp"));
//
//        DialogDisplayerImpl.returnFromNotify(null);
//        LOG.info("invoking build-jnlp");
//        ExecutorTask task = p.invokeActionImpl("build-jnlp", suite.getLookup());
//        LOG.info("Invocation started");
//
//        assertNotNull("Task was started", task);
//        LOG.info("Waiting for task to finish");
//        task.waitFinished();
//        LOG.info("Checking the result");
//        assertEquals("Finished ok", 0, task.result());
//        LOG.info("Testing the content of the directory");
//
//        FileObject[] arr = suite.getProjectDirectory().getChildren();
//        List<FileObject> subobj = new ArrayList<FileObject>(Arrays.asList(arr));
//        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
//        FileObject buildXML = suite.getProjectDirectory().getFileObject("build.xml");
//        subobj.remove(buildXML);
//        FileObject master = suite.getProjectDirectory().getFileObject("master.jnlp");
//        assertNotNull("Master must be created", master);
//        subobj.remove(master);
//        FileObject build = suite.getProjectDirectory().getFileObject("build");
//        subobj.remove(build);
//
//        {
//            // check content of build
//            FileObject jnlpDir = build.getFileObject("jnlp/app/");
//            assertNotNull("app dir exists", jnlpDir);
//            FileObject[] arrX = jnlpDir.getChildren();
//            int cnt = 0;
//            for (int i = 0; i < arrX.length; i++) {
//                if (arrX[i].hasExt("jnlp")) {
//                    cnt++;
//                    String jnlpContent = readFile(arrX[i]);
//                    if (jnlpContent.indexOf("http://www.netbeans.org/download/samples/jnlp/htmleditor/app/") == -1) {
//                        fail(" for " + arrX[i] + " URL with /app/ must be present: " + jnlpContent);
//                    }
//                }
//            }
//
//            if (cnt == 0) fail("At least one jnlp file in app dir");
//        }
//
//        {
//            // check content of netbeans default dir
//            FileObject jnlpDir = build.getFileObject("jnlp/netbeans/");
//            assertNotNull("netbeans dir exists", jnlpDir);
//            FileObject[] arrX = jnlpDir.getChildren();
//            int cnt = 0;
//            for (int i = 0; i < arrX.length; i++) {
//                if (arrX[i].hasExt("jnlp")) {
//                    cnt++;
//                    String jnlpContent = readFile(arrX[i]);
//                    if (jnlpContent.indexOf("http://www.netbeans.org/download/samples/jnlp/htmleditor/netbeans/") == -1) {
//                        fail(" for " + arrX[i] + " URL with /netbeans/ must be present: " + jnlpContent);
//                    }
//                }
//            }
//
//            if (cnt == 0) fail("At least one jnlp file in app dir");
//        }
//
//        // check master file has it
//        String masterContent = readFile(master);
//        if (masterContent.indexOf("http://www.netbeans.org/download/samples/jnlp/htmleditor/") == -1) {
//            fail("URL must be present in master: " + masterContent);
//        }
//
//
//        FileObject dist = suite.getProjectDirectory().getFileObject("dist");
//        assertNull("no dist created", dist);
//
//        FileObject branding = suite.getProjectDirectory().getFileObject("branding.jnlp");
//        assertNotNull("Branding must be created", branding);
//        subobj.remove(branding);
//
//        if (!subobj.isEmpty()) {
//            fail("There should be no created directories in the suite dir: " + subobj);
//        }
//
//    }

    //    XXX: failing test, fix or delete
//    public void testBuildJNLPWhenLocalizedFilesAreMissing() throws Exception {
//        File openideUtil = new File(
//            Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI()
//        );
//        File platformC = openideUtil.getParentFile().getParentFile();
//
//        File copyP = new File(new File(getWorkDir(), "netbeans"), "platform");
//        copyP.mkdirs();
//
//        copyFiles(platformC, copyP);
//
//        EditableProperties ep = suite.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        ep.setProperty("app.name", "fakeapp");
//
//        File someJar = createNewJarFile("fake-jnlp-servlet");
//
//        String platformPropsPath = "nbproject/platform.properties";
//        EditableProperties props = suite.getHelper().getProperties(platformPropsPath);
//        props.setProperty("harness.dir", platformC.getParent() + File.separator + "harness");
//        props.setProperty(ModuleList.NETBEANS_DEST_DIR, copyP.getParent());
//        props.setProperty("app.name", "fakeapp");
//        props.setProperty("jnlp.servlet.jar", someJar.getAbsolutePath());
//        suite.getHelper().putProperties(platformPropsPath, props);
//
//        File where = new File(copyP, "update_tracking");
//        Source xslt = new StreamSource(getClass().getResourceAsStream("GenerateJNLPApplicationTest.xsl"));
//        Transformer t = TransformerFactory.newInstance().newTransformer(xslt);
//        File f = new File(where, "org-netbeans-core-startup.xml");
//        assertTrue("core exists: " + f, f.exists());
//        File tmp = new File(f.getParent(), f.getName() + ".copy");
//        // delete & renameTo has problems on Windows, see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4017593
//        boolean renamed = f.renameTo(tmp);
//        assertTrue("rename " + f + " --> " + f + ".copy succeeded", renamed);
//
//        {
//            Source s = new StreamSource(tmp);
//            Result r = new StreamResult(f);
//            t.clearParameters();
//            t.setParameter("file", "core/locale/core_cs.jar");
//            t.transform(s, r);
//            tmp.delete();
//            assertFalse("File.delete() works as expected", tmp.exists());
//            assertTrue("modified core exists: " + f, f.exists());
//        }
//
//        ProjectManager.getDefault().saveProject(suite);
//
//        ep.setProperty("enabled.clusters", TestBase.CLUSTER_PLATFORM);
//        ep.setProperty("disabled.modules", "org.netbeans.modules.autoupdate," +
//            "org.openide.compat," +
//            "org.netbeans.api.progress," +
//            "org.netbeans.core.multiview," +
//            "org.openide.filesystems," +
//            "org.openide.modules," +
//            "org.openide.util," +
//            "org.netbeans.api.visual," +
//            "org.netbeans.core.execution," +
//            "org.netbeans.core.output2," +
//            "org.netbeans.core.ui," +
//            "org.netbeans.core.windows," +
//            "org.netbeans.core," +
//            "org.netbeans.modules.favorites," +
//            "org.netbeans.modules.javahelp," +
//            "org.netbeans.modules.masterfs," +
//            "org.netbeans.modules.queries," +
//            "org.netbeans.modules.settings," +
//            "org.netbeans.swing.plaf," +
//            "org.netbeans.swing.tabcontrol," +
//            "org.openide.actions," +
//            "org.openide.awt," +
//            "org.openide.dialogs," +
//            "org.openide.execution," +
//            "org.openide.explorer," +
//            "org.openide.io," +
//            "org.openide.loaders," +
//            "org.openide.nodes," +
//            "org.openide.options," +
//            "org.openide.text," +
//            "org.openide.windows," +
//            "");
//        ep.setProperty("jnlp.servlet.jar", someJar.toString());
//        suite.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
//        ProjectManager.getDefault().saveProject(suite);
//        LOG.info("Properties stored");
//
//        SuiteActions p = (SuiteActions)suite.getLookup().lookup(ActionProvider.class);
//        assertNotNull("Provider is here");
//
//        List l = Arrays.asList(p.getSupportedActions());
//        assertTrue("We support build-jnlp: " + l, l.contains("build-jnlp"));
// /*
//        WeakReference<?> ref = new WeakReference<Object>(suite);
//        suite = null;
//        assertGC("Project can go away", ref);
//
//        NbPlatform nbp = suite.getPlatform(false);
//        assertNotNull("Platform is associated", nbp);
//        assertEquals(copyP.getParentFile(), nbp.getDestDir());
//        */
//        DialogDisplayerImpl.returnFromNotify(null);
//        LOG.info("invoking build-jnlp");
//        ExecutorTask task = p.invokeActionImpl("build-jnlp", suite.getLookup());
//        LOG.info("Invocation started");
//
//        assertNotNull("Task was started", task);
//        LOG.info("Waiting for task to finish");
//        task.waitFinished();
//        LOG.info("Checking the result");
//        assertEquals("Finished ok", 0, task.result());
//        LOG.info("Testing the content of the directory");
//
//        FileObject[] arr = suite.getProjectDirectory().getChildren();
//        List<FileObject> subobj = new ArrayList<FileObject>(Arrays.asList(arr));
//        subobj.remove(suite.getProjectDirectory().getFileObject("mod1"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("nbproject"));
//        subobj.remove(suite.getProjectDirectory().getFileObject("build.xml"));
//        FileObject master = suite.getProjectDirectory().getFileObject("master.jnlp");
//        assertNotNull("Master must be created", master);
//        FileObject branding = suite.getProjectDirectory().getFileObject("branding.jnlp");
//        assertNotNull("Branding must be created", branding);
//        subobj.remove(master);
//        subobj.remove(branding);
//        subobj.remove(suite.getProjectDirectory().getFileObject("build"));
//        FileObject dist = suite.getProjectDirectory().getFileObject("dist");
//        assertNotNull("dist created", dist);
//        subobj.remove(dist);
//
//        if (!subobj.isEmpty()) {
//            fail("There should be no created directories in the suite dir: " + subobj);
//        }
//
//        FileObject war = dist.getFileObject("fakeapp.war");
//        assertNotNull("War file created: " + war, war);
//
//        File warF = FileUtil.toFile(war);
//        JarFile warJ = new JarFile(warF);
//        Enumeration en = warJ.entries();
//        int cntJnlp = 0;
//        while (en.hasMoreElements()) {
//            JarEntry entry = (JarEntry)en.nextElement();
//            if (!entry.getName().endsWith(".jnlp")) {
//                continue;
//            }
//            cntJnlp++;
//
//            byte[] data = new byte[(int)entry.getSize()];
//            int len = 0;
//            InputStream is = warJ.getInputStream(entry);
//            for(int pos = 0; pos < data.length; ) {
//                int r = is.read(data, pos, data.length - pos);
//                pos += r;
//                len += r;
//            }
//            is.close();
//            assertEquals("Correct data read: " + entry, data.length, len);
//
//            String s = new String(data);
//            if (s.indexOf(getWorkDir().getName()) >= 0) {
//                fail("Name of work dir in a file, means that there is very likely local reference to a file: " + entry + "\n" + s);
//            }
//        }
//
//        if (cntJnlp == 0) {
//            fail("There should be at least one jnlp entry");
//        }
//    }
    
    private static String readFile(final FileObject fo) throws IOException, FileNotFoundException {
        // write user modified version of the file
        byte[] arr = new byte[(int)fo.getSize()];
        InputStream is = fo.getInputStream();
        int len = is.read(arr);
        assertEquals("Read all", arr.length, len);
        String s = new String(arr);
        is.close();
        return s;
    }

    private void copyFiles(File from, File to) throws IOException {
        LOG.fine("Copy " + from + " to " + to);
        if (from.isDirectory()) {
            to.mkdirs();
            for (File f : from.listFiles()) {
                copyFiles(f, new File(to, f.getName()));
            }
        } else {
            byte[] arr = new byte[4096];
            FileInputStream is = new FileInputStream(from);
            FileOutputStream os = new FileOutputStream(to);
            for (;;) {
                int r = is.read(arr);
                if (r == -1) {
                    break;
                }
                os.write(arr, 0, r);
            }
            is.close();
            os.close();
        }
        
    }
    
    private File createNewJarFile (String prefix) throws IOException {
        if (prefix == null) {
            prefix = "modules";
        }
        
        File dir = new File(this.getWorkDir(), prefix);
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists ()) {
                f.createNewFile();
                return f;
            }
        }
    }
}
