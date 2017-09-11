/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.validation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.startup.layers.LayerCacheManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.Log;
import org.netbeans.junit.RandomlyFails;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.modules.Dependency;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbCollections;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Checks consistency of System File System contents.
 */
public class ValidateLayerConsistencyTest extends NbTestCase {

    static {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("org.openide.util.lookup.level", "FINE");
    }

    private static final String SFS_LB = "SystemFileSystem.localizingBundle";

    private ClassLoader contextClassLoader;   
    
    public ValidateLayerConsistencyTest(String name) {
        super (name);
    }

    @Override
    protected int timeOut() {
        // sometimes can deadlock and then we need to see the thread dump
        return 1000 * 60 * 10;
    }
    
    public @Override void setUp() throws Exception {
        clearWorkDir();
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                contextClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(Lookup.getDefault().lookup(ClassLoader.class));
                return null;
            }
        });
    }
    
    public @Override void tearDown() {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                return null;
            }
        });
    }
    
    protected @Override boolean runInEQ() {
        return true;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(ValidateLayerConsistencyTest.class).
                clusters("(?!ergonomics).*").enableClasspathModules(false).enableModules(".*").gui(false).suite());
        suite.addTest(NbModuleSuite.createConfiguration(ValidateLayerConsistencyTest.class).
                clusters("platform|ide").enableClasspathModules(false).enableModules(".*").gui(false).suite());
        return suite;
    }

    private void assertNoErrors(String message, Collection<String> warnings) {
        if (warnings.isEmpty()) {
            return;
        }
        StringBuilder b = new StringBuilder(message);
        for (String warning : new TreeSet<String>(warnings)) {
            b.append('\n').append(warning);
        }
        fail(b.toString());
    }

    /* Causes mysterious failure in otherwise OK-looking UI/Runtime/org-netbeans-modules-db-explorer-nodes-RootNode.instance: 
    @Override
    protected Level logLevel() {
        return Level.FINER;
    }
    */

    /** whether an attribute will be handled in testInstantiateAllInstances anyway */
    private static boolean isInstanceAttribute(String attributeName) {
        if (attributeName.equals("instanceCreate")) {
            return true;
        }
        if (attributeName.equals("component")) {
            return true; // probably being used by TopComponent.openAction
        }
        return false;
    }
    
    public void testAreAttributesFine () {
        List<String> errors = new ArrayList<String>();
        
        FileObject root = FileUtil.getConfigRoot();
        Enumeration<? extends FileObject> files = Enumerations.concat(Enumerations.singleton(root), root.getChildren(true));
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            
            if (
                "Keymaps/NetBeans/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/NetBeans55/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/Emacs/D-BACK_QUOTE.shadow".equals(fo.getPath())
            ) {
                // #46753
                continue;
            }
            if (
                "Services/Browsers/FirefoxBrowser.settings".equals(fo.getPath()) ||
                "Services/Browsers/MozillaBrowser.settings".equals(fo.getPath()) ||
                "Services/Browsers/NetscapeBrowser.settings".equals(fo.getPath())
            ) {
                // #161784
                continue;
            }
            
            Enumeration<String> attrs = fo.getAttributes();
            while (attrs.hasMoreElements()) {
                String name = attrs.nextElement();

                if (isInstanceAttribute(name)) {
                    continue;
                }
                
                if (name.indexOf('\\') != -1) {
                    errors.add("File: " + fo.getPath() + " attribute name must not contain backslashes: " + name);
                }
                
                Object attr = fo.getAttribute(name);
                if (attr == null) {
                    CharSequence warning = Log.enable("", Level.WARNING);
                    if (
                        fo.getAttribute("class:" + name) != null &&
                        fo.getAttribute(name) == null &&
                        warning.length() == 0
                    ) {
                        // ok, factory method returned null
                        continue;
                    }

                    errors.add("File: " + fo.getPath() + " attribute name: " + name);
                }

                if (attr instanceof URL) {
                    URL u = (URL) attr;
                    int read = -1;
                    try {
                        read = u.openStream().read(new byte[4096]);
                    } catch (IOException ex) {
                        errors.add(fo.getPath() + ": " + ex.getMessage());
                    }
                    if (read <= 0) {
                        errors.add("URL resource does not exist: " + fo.getPath() + " attr: " + name + " value: " + attr);
                    }
                }

            }
        }
        
        assertNoErrors("Some attributes in files are unreadable", errors);
    }
    
    public void testValidShadows () {
        // might be better to move into editor/options tests as it is valid only if there are options
        List<String> errors = new ArrayList<String>();
        
        FileObject root = FileUtil.getConfigRoot();
        
        Enumeration<? extends FileObject> en = root.getChildren(true);
        int cnt = 0;
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            cnt++;
            
            // XXX #16761 Removing attr in MFO causes storing special-null value even in unneeded cases.
            // When the issue is fixed remove this hack.
            if("Windows2/Modes/debugger".equals(fo.getPath()) // NOI18N
            || "Windows2/Modes/explorer".equals(fo.getPath())) { // NOI18N
                continue;
            }
            
            if (
                "Keymaps/NetBeans/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/NetBeans55/D-BACK_QUOTE.shadow".equals(fo.getPath()) ||
                "Keymaps/Emacs/D-BACK_QUOTE.shadow".equals(fo.getPath())
            ) {
                // #46753
                continue;
            }
            
            try {
                DataObject obj = DataObject.find (fo);
                DataShadow ds = obj.getLookup().lookup(DataShadow.class);
                if (ds != null) {
                    Object o = ds.getOriginal();
                    if (o == null) {
                        errors.add("File " + fo + " has no original.");
                    }
                }
                else if ("shadow".equals(fo.getExt())) {
                    errors.add("File " + fo + " is not a valid DataShadow.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errors.add ("File " + fo + " threw " + ex);
            }
        }
        
        assertNoErrors("Some shadow files in NetBeans profile are broken", errors);
        
        if (ValidateLayerConsistencyTest.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
            // do not check the count as this probably means we are running
            // plain Unit test and not inside the IDE mode
            return;
        }
        
        
        if (cnt == 0) {
            fail("No file objects on system file system!");
        }
    }
    
    @RandomlyFails
    public void testContentCanBeRead () {
        List<String> errors = new ArrayList<String>();
        byte[] buffer = new byte[4096];
        
        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            
            if (!fo.isData ()) {
                continue;
            }
            long size = fo.getSize();
            
            try {
                long read = 0;
                InputStream is = fo.getInputStream();
                try {
                    for (;;) {
                        int len = is.read (buffer);
                        if (len == -1) {
                            break;
                        }
                        read += len;
                    }
                } finally {
                    is.close ();
                }
                
                if (size != -1) {
                    assertEquals ("The amount of data in stream is the same as the length", size, read);
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
                errors.add ("File " + fo + " cannot be read: " + ex);
            }
        }
        
        assertNoErrors("Some files are unreadable", errors);
    }
    
    public void testInstantiateAllInstances () {
        List<String> errors = new ArrayList<String>();
        
        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            
            if (skipFile(fo)) {
                continue;
            }
            
            try {
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic = obj.getLookup().lookup(InstanceCookie.class);
                if (ic != null) {
                    Object o = ic.instanceCreate ();
                    if (fo.getPath().matches("Services/.+[.]instance")) {
                        String instanceOf = (String) fo.getAttribute("instanceOf");
                        if (instanceOf == null) {
                            errors.add("File " + fo.getPath() + " should declare instanceOf");
                        } else if (o != null) {
                            for (String piece : instanceOf.split(", ?")) {
                                if (!Class.forName(piece, true, Lookup.getDefault().lookup(ClassLoader.class)).isInstance(o)) {
                                    errors.add("File " + fo.getPath() + " claims to be a " + piece + " but is not (instance of " + o.getClass() + ")");
                                }
                            }
                        }
                    } else if (fo.getPath().matches("Services/.+[.]settings")) {
                        if (!fo.asText().contains("<instanceof")) {
                            errors.add("File " + fo.getPath() + " should declare <instanceof class=\"...\"/>");
                        }
                        // XXX test assignability here too, perhaps (but only used in legacy code)
                    }
                }
            } catch (Exception ex) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                ex.printStackTrace(ps);
                ps.flush();
                errors.add(
                    "File " + fo.getPath() +
                    "\nRead from: " + Arrays.toString((Object[])fo.getAttribute("layers")) +
                    "\nthrew: " + baos);
            }
        }
        
        assertNoErrors("Some instances cannot be created", errors);
    }

    public void testActionInstancesOnlyInActionsFolder() {
        List<String> errors = new ArrayList<String>();

        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
        FILE: while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();

            if (skipFile(fo)) {
                continue;
            }

            try {
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic = obj.getLookup().lookup(InstanceCookie.class);
                if (ic == null) {
                    continue;
                }
                Object o;
                try {
                    o = ic.instanceCreate();
                } catch (ClassNotFoundException ok) {
                    // wrong instances are catched by another test
                    continue;
                }
                if (!(o instanceof Action)) {
                    continue;
                }
                if (fo.hasExt("xml")) {
                    continue;
                }
                if (fo.getPath().startsWith("Actions/")) {
                    continue;
                }
                if (fo.getPath().startsWith("Editors/")) {
                    // editor is a bit different world
                    continue;
                }
                if (fo.getPath().startsWith("Databases/Explorer/")) {
                    // db explorer actions shall not influence start
                    // => let them be for now.
                    continue;
                }
                if (fo.getPath().startsWith("WelcomePage/")) {
                    // welcome screen actions are not intended for end user
                    continue;
                }
                if (fo.getPath().startsWith("Projects/org-netbeans-modules-mobility-project/Actions/")) {
                    // I am not sure what mobility is doing, but
                    // I guess I do not need to care
                    continue;
                }
                if (fo.getPath().startsWith("NativeProjects/Actions/")) {
                    // XXX should perhaps be replaced
                    continue;
                }
                if (fo.getPath().startsWith("contextmenu/uml/")) {
                    // UML is not the most important thing to fix
                    continue;
                }
                if (fo.getPath().equals("Menu/Help/org-netbeans-modules-j2ee-blueprints-ShowBluePrintsAction.instance")) {
                    // action included in some binary blob
                    continue;
                }
                if (Boolean.TRUE.equals(fo.getAttribute("misplaced.action.allowed"))) {
                    // it seems necessary some actions to stay outside
                    // of the Actions folder
                    continue;
                }
                if (fo.hasExt("shadow")) {
                    o = fo.getAttribute("originalFile");
                    if (o instanceof String) {
                        String origF = o.toString().replaceFirst("\\/*", "");
                        if (origF.startsWith("Actions/")) {
                            continue;
                        }
                        if (origF.startsWith("Editors/")) {
                            continue;
                        }
                    }
                }
                errors.add("File " + fo.getPath() + " represents an action which is not in Actions/ subfolder. Provided by " + Arrays.toString((Object[])fo.getAttribute("layers")));
            } catch (Exception ex) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                ex.printStackTrace(ps);
                ps.flush();
                errors.add ("File " + fo.getPath() + " threw: " + baos);
            }
        }

        assertNoErrors(errors.size() + " actions is not registered properly", errors);
    }
    
    public void testLayerOverrides() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        class ContentAndAttrs {
            final byte[] contents;
            final Map<String,Object> attrs;
            private final URL layerURL;
            ContentAndAttrs(byte[] contents, Map<String,Object> attrs, URL layerURL) {
                this.contents = contents;
                this.attrs = attrs;
                this.layerURL = layerURL;
            }
            public @Override String toString() {
                return "ContentAndAttrs[contents=" + Arrays.toString(contents) + ",attrs=" + attrs + ";from=" + layerURL + "]";
            }
            public @Override int hashCode() {
                return Arrays.hashCode(contents) ^ attrs.hashCode();
            }
            public @Override boolean equals(Object o) {
                if (!(o instanceof ContentAndAttrs)) {
                    return false;
                }
                ContentAndAttrs caa = (ContentAndAttrs) o;
                return Arrays.equals(contents, caa.contents) && attrs.equals(caa.attrs);
            }
        }
        Map</* path */String,Map</* owner */String,ContentAndAttrs>> files = new TreeMap<String,Map<String,ContentAndAttrs>>();
        Map</* path */String,Map</* attr name */String,Map</* module name */String,/* attr value */Object>>> folderAttributes =
                new TreeMap<String,Map<String,Map<String,Object>>>();
        Map<String,Set<String>> directDeps = new HashMap<String,Set<String>>();
        StringBuffer sb = new StringBuffer();
        Map<String,URL> hiddenFiles = new HashMap<String, URL>();
        Set<String> allFiles = new HashSet<String>();
        final String suffix = "_hidden";

        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            URL u = en.nextElement();
            InputStream is = u.openStream();
            Manifest mf;
            try {
                mf = new Manifest(is);
            } finally {
                is.close();
            }
            String module = mf.getMainAttributes ().getValue ("OpenIDE-Module");
            if (module == null) {
                continue;
            }
            String depsS = mf.getMainAttributes().getValue("OpenIDE-Module-Module-Dependencies");
            if (depsS != null) {
                Set<String> deps = new HashSet<String>();
                for (Dependency d : Dependency.create(Dependency.TYPE_MODULE, depsS)) {
                    deps.add(d.getName().replaceFirst("/.+$", ""));
                }
                directDeps.put(module, deps);
            }
            for (boolean generated : new boolean[] {false, true}) {
                String layer;
                if (generated) {
                    layer = "META-INF/generated-layer.xml";
                } else {
                    layer = mf.getMainAttributes ().getValue ("OpenIDE-Module-Layer");
                    if (layer == null) {
                        continue;
                    }
                }

                URL base = new URL(u, "../");
                URL layerURL = new URL(base, layer);
                URLConnection connect;
                try {
                    connect = layerURL.openConnection();
                    connect.connect();
                } catch (FileNotFoundException x) {
                    if (generated) {
                        continue;
                    } else {
                        throw x;
                    }
                }
                connect.setDefaultUseCaches (false);
                FileSystem fs = new XMLFileSystem(layerURL);

                Enumeration<? extends FileObject> all = fs.getRoot().getChildren(true);
                while (all.hasMoreElements ()) {
                    FileObject fo = all.nextElement ();
                    String simplePath = fo.getPath();

                    if (simplePath.endsWith(suffix)) {
                        hiddenFiles.put(simplePath, layerURL);
                    } else {
                        allFiles.add(simplePath);
                    }

                    Number weight = (Number) fo.getAttribute("weight");
                    // XXX if weight != null, test that it is actually overriding something or being overridden
                    String weightedPath = weight == null ? simplePath : simplePath + "#" + weight;

                    Map<String,Object> attributes = getAttributes(fo, base);

                    if (fo.isFolder()) {
                        for (Map.Entry<String,Object> attr : attributes.entrySet()) {
                            Map<String,Map<String,Object>> m1 = folderAttributes.get(weightedPath);
                            if (m1 == null) {
                                m1 = new TreeMap<String,Map<String,Object>>();
                                folderAttributes.put(weightedPath, m1);
                            }
                            Map<String,Object> m2 = m1.get(attr.getKey());
                            if (m2 == null) {
                                m2 = new TreeMap<String,Object>();
                                m1.put(attr.getKey(), m2);
                            }
                            m2.put(module, attr.getValue());
                        }
                        continue;
                    }

                    Map<String,ContentAndAttrs> overrides = files.get(weightedPath);
                    if (overrides == null) {
                        overrides = new TreeMap<String,ContentAndAttrs>();
                        files.put(weightedPath, overrides);
                    }
                    overrides.put(module, new ContentAndAttrs(fo.asBytes(), attributes, layerURL));
                }
                // make sure the filesystem closes the stream
                connect.getInputStream ().close ();
            }
        }
        assertFalse("At least one layer file is usually used", allFiles.isEmpty());

        for (Map.Entry<String,Map<String,ContentAndAttrs>> e : files.entrySet()) {
            Map<String,ContentAndAttrs> overrides = e.getValue();
            if (overrides.size() == 1) {
                continue;
            }
            Set<String> overriders = overrides.keySet();
            String file = e.getKey();

            if (new HashSet<ContentAndAttrs>(overrides.values()).size() == 1) {
                // All the same. Check whether these are parallel declarations (e.g. CND debugger vs. Java debugger), or vertical.
                for (String overrider : overriders) {
                    Set<String> deps = new HashSet<String>(directDeps.get(overrider));
                    deps.retainAll(overriders);
                    if (!deps.isEmpty()) {
                        sb.append(file).append(" is pointlessly overridden in ").append(overrider).
                                append(" relative to ").append(deps.iterator().next()).append('\n');
                    }
                }
                continue;
            }

            sb.append(file).append(" is provided by: ").append(overriders).append('\n');
            for (Map.Entry<String,ContentAndAttrs> entry : overrides.entrySet()) {
                ContentAndAttrs contentAttrs = entry.getValue();
                sb.append(" ").append(entry.getKey()).append(": content = '").append(new String(contentAttrs.contents)).
                        append("', attributes = ").append(contentAttrs.attrs).append("\n");
            }
        }        
        
        for (Map.Entry<String,Map<String,Map<String,Object>>> entry1 : folderAttributes.entrySet()) {
            for (Map.Entry<String,Map<String,Object>> entry2 : entry1.getValue().entrySet()) {
                if (new HashSet<Object>(entry2.getValue().values()).size() > 1) {
                    sb.append("Some modules conflict on the definition of ").append(entry2.getKey()).append(" for ").
                            append(entry1.getKey()).append(": ").append(entry2.getValue()).append("\n");
                }
            }
        }

        if (sb.length () > 0) {
            fail("Some modules override some files without using the weight attribute correctly\n" + sb);
        }


        for (Map.Entry<String, URL> e : hiddenFiles.entrySet()) {
            String p = e.getKey().substring(0, e.getKey().length() - suffix.length());
            if (allFiles.contains(p)) {
                continue;
            }
            sb.append("file ").append(e.getKey()).append(" from ").append(e.getValue()).append(" does not hide any other file\n");
        }

        if (sb.length () > 0) {
            fail ("There are some useless hidden files\n" + sb);
        }
    }
    
    /* Too many failures to solve right now.
    public void testLocalizingBundles() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (URL u : NbCollections.iterable(Lookup.getDefault().lookup(ClassLoader.class).getResources("META-INF/MANIFEST.MF"))) {
            String layer;
            InputStream is = u.openStream();
            try {
                layer = new Manifest(is).getMainAttributes().getValue("OpenIDE-Module-Layer");
                if (layer == null) {
                    continue;
                }
            } finally {
                is.close();
            }
            URL base = new URL(u, "../");
            URL layerURL = new URL(base, layer);
            URLConnection connect = layerURL.openConnection();
            connect.setDefaultUseCaches(false);
            for (FileObject fo : NbCollections.iterable(new XMLFileSystem(layerURL).getRoot().getChildren(true))) {
                Object v = getAttributes(fo, base).get(SFS_LB);
                if (v instanceof Exception) {
                    sb.append(layerURL).append(": ").append(v).append("\n");
                }
            }
        }
        if (sb.length() > 0) {
            fail("Some localizing bundle declarations are wrong\n" + sb);
        }
    }
     */

    public void testNoWarningsFromLayerParsing() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        assertNotNull ("In the IDE mode, there always should be a classloader", l);
        
        List<URL> urls = new ArrayList<URL>();
        Enumeration<URL> en = l.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements ()) {
            URL u = en.nextElement();
            InputStream is = u.openStream();
            Manifest mf;
            try {
                mf = new Manifest(is);
            } finally {
                is.close();
            }
            String module = mf.getMainAttributes ().getValue ("OpenIDE-Module");
            if (module == null) {
                continue;
            }
            String layer = mf.getMainAttributes ().getValue ("OpenIDE-Module-Layer");
            if (layer == null) {
                continue;
            }
            URL layerURL = new URL(u, "../" + layer);
            urls.add(layerURL);
        }
        
        File cacheDir;
        File workDir = getWorkDir();
        int i = 0;
        do {
            cacheDir = new File(workDir, "layercache"+i);
            i++;
        } while (!cacheDir.mkdir());
        System.setProperty("netbeans.user", cacheDir.getPath());

        LayerCacheManager bcm = LayerCacheManager.manager(true);
        Logger err = Logger.getLogger("org.netbeans.core.projects.cache");
        TestHandler h = new TestHandler();
        err.addHandler(h);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bcm.store(bcm.createEmptyFileSystem(), urls, os);
        assertNoErrors("No errors or warnings during layer parsing", h.errors);
    }

    private static class TestHandler extends Handler {
        List<String> errors = new ArrayList<String>();
        
        TestHandler () {}
        
        public @Override void publish(LogRecord rec) {
            if (Level.WARNING.equals(rec.getLevel()) || Level.SEVERE.equals(rec.getLevel())) {
                errors.add(MessageFormat.format(rec.getMessage(), rec.getParameters()));
            }
        }
        
        List<String> errors() {
            return errors;
        }

        public @Override void flush() {}

        public @Override void close() throws SecurityException {}
    }

    public void testFolderOrdering() throws Exception {
        TestHandler h = new TestHandler();
        Logger.getLogger("org.openide.filesystems.Ordering").addHandler(h);
        Set<List<String>> editorMultiFolders = new HashSet<List<String>>();
        Pattern editorFolder = Pattern.compile("Editors/(application|text)/([^/]+)(/.+|$)");
        Enumeration<? extends FileObject> files = FileUtil.getConfigRoot().getChildren(true);
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            if (fo.isFolder()) {
                loadChildren(fo);
                assertNull("OpenIDE-Folder-Order attr should not be used on " + fo, fo.getAttribute("OpenIDE-Folder-Order"));
                assertNull("OpenIDE-Folder-SortMode attr should not be used on " + fo, fo.getAttribute("OpenIDE-Folder-SortMode"));
                String path = fo.getPath();
                Matcher m = editorFolder.matcher(path);
                if (m.matches()) {
                    List<String> multiPath = new ArrayList<String>(3);
                    multiPath.add(path);
                    if (m.group(2).endsWith("+xml")) {
                        multiPath.add("Editors/" + m.group(1) + "/xml" + m.group(3));
                    }
                    multiPath.add("Editors" + m.group(3));
                    editorMultiFolders.add(multiPath);
                }
            }
        }
        assertNoErrors("No warnings relating to folder ordering; " +
                "cf: http://deadlock.netbeans.org/job/nbms-and-javadoc/lastSuccessfulBuild/artifact/nbbuild/build/generated/layers.txt", h.errors());
        for (List<String> multiPath : editorMultiFolders) {
            List<FileSystem> layers = new ArrayList<FileSystem>(3);
            for (final String path : multiPath) {
                FileObject folder = FileUtil.getConfigFile(path);
                if (folder != null) {
                    layers.add(new MultiFileSystem(folder.getFileSystem()) {
                        protected @Override FileObject findResourceOn(FileSystem fs, String res) {
                            FileObject f = fs.findResource(path + '/' + res);
                            return Boolean.TRUE.equals(f.getAttribute("hidden")) ? null : f;
                        }
                    });
                }
            }
            loadChildren(new MultiFileSystem(layers.toArray(new FileSystem[layers.size()])).getRoot());
            assertNoErrors("No warnings relating to folder ordering in " + multiPath + 
                    "; cf: http://deadlock.netbeans.org/job/nbms-and-javadoc/lastSuccessfulBuild/artifact/nbbuild/build/generated/layers.txt",
                    h.errors());
        }
    }
    private static void loadChildren(FileObject folder) {
        List<FileObject> kids = new ArrayList<FileObject>();
        for (DataObject kid : DataFolder.findFolder(folder).getChildren()) {
            kids.add(kid.getPrimaryFile());
        }
        FileUtil.getOrder(kids, true);
    }

    private static Map<String,Object> getAttributes(FileObject fo, URL base) {
        Map<String,Object> attrs = new TreeMap<String,Object>();
        Enumeration<String> en = fo.getAttributes();
        while (en.hasMoreElements()) {
            String attrName = en.nextElement();
            if (isInstanceAttribute(attrName)) {
                continue;
            }
            Object attr = fo.getAttribute(attrName);
            if (attrName.equals(SFS_LB)) {
                try {
                    String bundleName = (String) attr;
                    URL bundle = new URL(base, bundleName.replace('.', '/') + ".properties");
                    Properties p = new Properties();
                    InputStream is = bundle.openStream();
                    try {
                        p.load(is);
                    } finally {
                        is.close();
                    }
                    String path = fo.getPath();
                    attr = p.get(path);
                    if (attr == null) {
                        attr = new MissingResourceException("No such bundle entry " + path + " in " + bundleName, bundleName, path);
                    }
                } catch (Exception x) {
                    attr = x;
                }
            }
            attrs.put(attrName, attr);
        }
        return attrs;
    }

    private static final String[] SKIPPED = {
        "Templates/GUIForms",
        "Palette/Borders/javax-swing-border-",
        "Palette/Layouts/javax-swing-BoxLayout",
        "Templates/Beans/",
        "PaletteUI/org-netbeans-modules-form-palette-CPComponent",
        "Templates/Ant/CustomTask.java",
        "Templates/Privileged/Main.shadow",
        "Templates/Privileged/JFrame.shadow",
        "Templates/Privileged/Class.shadow",
        "Templates/Classes",
        "Templates/JSP_Servlet",
        "EnvironmentProviders/ProfileTypes/Execution/nb-j2ee-deployment.instance",
        "Shortcuts/D-BACK_QUOTE.shadow",
        "Windows2/Components/", // cannot be loaded with a headless toolkit, so we have to skip these for now
    };
    private boolean skipFile(FileObject fo) {
        String s = fo.getPath();

        if (s.startsWith ("Templates/") && !s.startsWith ("Templates/Services")) {
            if (s.endsWith (".shadow") || s.endsWith (".java")) {
                return true;
            }
        }

        for (String skipped : SKIPPED) {
            if (s.startsWith(skipped)) {
                return true;
            }
        }
        
        String iof = (String) fo.getAttribute("instanceOf");
        if (iof != null) {
            for (String clz : iof.split("[, ]+")) {
                try {
                    Class<?> c = Lookup.getDefault().lookup(ClassLoader.class).loadClass(clz);
                } catch (ClassNotFoundException x) {
                    // E.g. Services/Hidden/org-netbeans-lib-jsch-antlibrary.instance in ide cluster
                    // cannot be loaded (and would just be ignored) if running without java cluster
                    System.err.println("Warning: skipping " + fo.getPath() + " due to inaccessible interface " + clz);
                    return true;
                }
            }
        }

        return false;
    }

    public void testKeymapOverrides() throws Exception { // #170677
        List<String> warnings = new ArrayList<String>();
        FileObject[] keymaps = FileUtil.getConfigFile("Keymaps").getChildren();
        Map<String,Integer> definitionCountById = new HashMap<String,Integer>();
        assertTrue("Too many keymaps for too little bitfield", keymaps.length < 31);
        int keymapFlag = 1;
        for (FileObject keymap : keymaps) {
            for (FileObject shortcut : keymap.getChildren()) {
                DataObject d = DataObject.find(shortcut);
                if (d instanceof DataShadow) {
                    String id = ((DataShadow) d).getOriginal().getPrimaryFile().getPath();
                    Integer prior = definitionCountById.get(id);
                    // a single keymap may provide alternative shortcuts for a given action. Count just once
                    // per keymap.
                    definitionCountById.put(id, prior == null ? keymapFlag : prior | keymapFlag);
                } else if (!d.getPrimaryFile().hasExt("shadow") && !d.getPrimaryFile().hasExt("removed")) {
                    warnings.add("Anomalous file " + d);
                } // else #172453: BrokenDataShadow, OK
            }
            keymapFlag <<= 1;
        }
        int expected = (1 << keymaps.length) - 1;
        for (FileObject shortcut : FileUtil.getConfigFile("Shortcuts").getChildren()) {
            DataObject d = DataObject.find(shortcut);
            if (d instanceof DataShadow) {
                String id = ((DataShadow) d).getOriginal().getPrimaryFile().getPath();
                if (!org.openide.util.Utilities.isMac() && // Would fail on Mac due to applemenu module
                        Integer.valueOf(expected).equals(definitionCountById.get(id)))
                {
                    String layers = Arrays.toString((URL[]) d.getPrimaryFile().getAttribute("layers"));
                    warnings.add(d.getPrimaryFile().getPath() + " " + layers + " useless since " + id + " is bound (somehow) in all keymaps");
                }
            } else if (!d.getPrimaryFile().hasExt("shadow")) {
                warnings.add("Anomalous file " + d);
            }
        }
        // XXX consider also checking for bindings in Shortcuts/ which are overridden in all keymaps or at least NetBeans
        // taking into consideration O- and D- virtual modifiers
        // (this is likely to be more common, e.g. mysterious Shortcuts/D-A.shadow in uml.drawingarea)
        // XXX check for shortcut conflict between Shortcuts and each keymap, e.g. Ctrl-R in Eclipse keymap
        assertNoErrors("Some shortcuts were overridden by keymaps", warnings);
    }
    
    /* XXX too many failures for now, some spurious; use regex, or look for unloc files/folders with loc siblings?
    public void testLocalizedFolderNames() throws Exception {
        List<String> warnings = new ArrayList<String>();
        for (String folder : new String[] {
            "Actions", // many legit failures!
            "OptionsDialog/Actions", // XXX #71280
            "Menu",
            "Toolbars",
            "org-netbeans-modules-java-hints/rules/hints",
            "Editors/FontsColors", // XXX exclude .../Defaults
            "Keymaps",
            "FormDesignerPalette", // XXX match any *Palette?
            "HTMLPalette",
            "XHTMLPalette",
            "JSPPalette",
            "SVGXMLPalette",
            "OptionsExport",
            // "Projects/.../Customizer",
            "QuickSearch",
            "Templates", // XXX exclude Privileged, Recent, Services
        }) {
            FileObject root = FileUtil.getConfigFile(folder);
            if (root == null) {
                continue;
            }
            for (FileObject d : NbCollections.iterable(root.getFolders(true))) {
                if (d.getAttribute("displayName") == null && d.getAttribute("SystemFileSystem.localizingBundle") == null) {
                    warnings.add("No displayName for " + d.getPath());
                }
            }
        }
        assertNoErrors("Some folders need a localized display name", warnings);
    }
    */

    public void testTemplates() throws Exception { // #167205
        List<String> warnings = new ArrayList<String>();
        for (FileObject f : NbCollections.iterable(FileUtil.getConfigFile("Templates").getData(true))) {
            if (!Boolean.TRUE.equals(f.getAttribute("template"))) {
                continue; // will not appear in Template Manager
            }
            if (f.getSize() > 0) {
                continue; // Open in Editor will be enabled
            }
            if (f.getAttribute("instantiatingIterator") != null) { // TemplateWizard.CUSTOM_ITERATOR
                continue; // probably not designed to be edited as text
            }
            if (f.getAttribute("templateWizardIterator") != null) { // TemplateWizard.EA_ITERATOR
                continue; // same
            }
            String path = f.getPath();
            if (path.equals("Templates/Other/file") ||
                path.equals("Templates/Other/group.group")) {
                
                // If there're more files like this, consider adding an API
                // to mark them as intentionally non-editable
                continue; // intentionally empty and uneditable
            }
            warnings.add(path + " is empty but has no iterator and will therefore not be editable");
        }
        assertNoErrors("Problems in templates", warnings);
    }

}
