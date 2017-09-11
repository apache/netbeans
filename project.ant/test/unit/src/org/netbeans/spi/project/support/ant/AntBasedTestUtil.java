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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.spi.project.support.ant;

import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test-related utilities for use in ant/project.
 * @author Jesse Glick
 */
public class AntBasedTestUtil {
    
    private AntBasedTestUtil() {}
    
    /**
     * Create an AntBasedProjectType instance suitable for testing.
     * It has the type code <samp>test</samp>.
     * It uses <samp>&lt;data&gt;</samp> as the configuration data element,
     * with namespaces <samp>urn:test:shared</samp> and <samp>urn:test:private</samp>.
     * Loading the project succeeds unless there is a file in it <samp>nbproject/broken</samp>.
     * The project's methods mostly delegate to the helper; its lookup uses the helper's
     * supports for AuxiliaryConfiguration, CacheDirectoryProvider, and SubprojectProvider,
     * and also adds an instance of String, namely "hello".
     * It also puts the AntProjectHelper into its lookup to assist in testing.
     * <code>build-impl.xml</code> is generated from <code>data/build-impl.xsl</code>
     * by a ProjectXmlSavedHook using GeneratedFilesHelper.refreshBuildScript.
     * A ReferenceHelper is also added to its lookup for testing purposes.
     * An {@link AntArtifactProviderMutable} is added which initially publishes two artifacts:
     * one of target 'dojar' type 'jar' with artifact ${build.jar};
     * one of target 'dojavadoc' type 'javadoc' with artifact ${build.javadoc};
     * both using clean target 'clean'.
     * A GeneratedFilesHelper is added to its lookup for testing purposes.
     * @return a project type object for testing purposes
     */
    public static AntBasedProjectType testAntBasedProjectType() {
        return new TestAntBasedProjectType();
    }
    
    public static AntBasedProjectType testAntBasedProjectType(AntBuildExtenderImplementation extender) {
        return new TestAntBasedProjectType(extender);
    }
    /**
     * You can adjust which artifacts are supplied.
     */
    public interface AntArtifactProviderMutable extends AntArtifactProvider {
        void setBuildArtifacts(AntArtifact[] arts);
    }
    
    private static final class TestAntBasedProjectType implements AntBasedProjectType {
        private AntBuildExtenderImplementation ext;
        
        TestAntBasedProjectType() {}
        
        TestAntBasedProjectType(AntBuildExtenderImplementation ext) {
            this.ext = ext;
        }
        
        public String getType() {
            return "test";
        }
        
        public Project createProject(AntProjectHelper helper) throws IOException {
            return new TestAntBasedProject(helper, ext);
        }
        
        public String getPrimaryConfigurationDataElementName(boolean shared) {
            return "data";
        }
        
        public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
            return shared ? "urn:test:shared" : "urn:test:private";
        }
        
    }
    
    public static class TestAntBasedProject implements Project {
        
        private final AntProjectHelper helper;
        private final ReferenceHelper refHelper;
        private final GeneratedFilesHelper genFilesHelper;
        private final Lookup l;
        
        protected TestAntBasedProject(AntProjectHelper helper, AntBuildExtenderImplementation ext) throws IOException {
            if (helper.getProjectDirectory().getFileObject("nbproject/broken") != null) {
                throw new IOException("broken");
            }
            this.helper = helper;
            AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
            refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
            Object extContent;
            if (ext !=null) {
                AntBuildExtender e = AntBuildExtenderFactory.createAntExtender(ext, refHelper);
                genFilesHelper = new GeneratedFilesHelper(helper, e);
                extContent = e;
            } else {
                genFilesHelper = new GeneratedFilesHelper(helper);
                extContent = new Object();
            }       
            l = Lookups.fixed(new Object[] {
                new TestInfo(),
                helper,
                refHelper,
                genFilesHelper,
                aux,
                helper.createCacheDirectoryProvider(),
                helper.createSharabilityQuery2(helper.getStandardPropertyEvaluator(), new String[0], new String[0]),
                refHelper.createSubprojectProvider(),
                new TestAntArtifactProvider(),
                new ProjectXmlSavedHook() {
                    protected void projectXmlSaved() throws IOException {
                        genFilesHelper.refreshBuildScript(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            AntBasedTestUtil.class.getResource("data/build-impl.xsl"),
                            false);
                        genFilesHelper.refreshBuildScript(
                            GeneratedFilesHelper.BUILD_XML_PATH,
                            testBuildXmlStylesheet(),
                            false);
                    }
                },
                "hello",
                extContent
            });
        }
        
        public FileObject getProjectDirectory() {
            return helper.getProjectDirectory();
        }
        
        public Lookup getLookup() {
            return l;
        }
        
        public String toString() {
            return "TestAntBasedProject[" + getProjectDirectory() + "]";
        }
        
        private final class TestInfo implements ProjectInformation {
            
            TestInfo() {}
            
            private String getText(String elementName) {
                Element data = helper.getPrimaryConfigurationData(true);
                Element el = XMLUtil.findElement(data, elementName, "urn:test:shared");
                if (el != null) {
                    String text = XMLUtil.findText(el);
                    if (text != null) {
                        return text;
                    }
                }
                // Some kind of fallback here.
                return getProjectDirectory().getNameExt();
            }
            
            public String getName() {
                return getText("name");
            }
            
            public String getDisplayName() {
                return getText("display-name");
            }
            
            public Icon getIcon() {
                return null;
            }
            
            public Project getProject() {
                return TestAntBasedProject.this;
            }
            
            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
            
        }

        
        private final class TestAntArtifactProvider implements AntArtifactProviderMutable {
            
            private AntArtifact[] arts;
            
            TestAntArtifactProvider() {}
            
            public AntArtifact[] getBuildArtifacts() {
                if (arts != null) {
                    return arts;
                }
                URI[] uris = null;
                try {
                    uris = new URI[]{new URI("dist/foo.jar"), new URI("dist/bar.jar")};
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
                return new AntArtifact[] {
                    helper.createSimpleAntArtifact("jar", "build.jar", helper.getStandardPropertyEvaluator(), "dojar", "clean"),
                    helper.createSimpleAntArtifact("javadoc", "build.javadoc", helper.getStandardPropertyEvaluator(), "dojavadoc", "clean"),
                    new TestAntArtifact(uris, helper),
                };
            }
            
            public void setBuildArtifacts(AntArtifact[] arts) {
                this.arts = arts;
            }
            
        }
        
    }
    
    /**
     * Load a properties file from disk.
     * @param h a project reference
     * @param path the relative file path
     * @return properties available at that location, or null if no such file
     * @throws IOException if there is any problem loading it
     */
    public static Properties slurpProperties(AntProjectHelper h, String path) throws IOException {
        Properties p = new Properties();
        File f = h.resolveFile(path);
        if (!f.isFile()) {
            return null;
        }
        InputStream is = new FileInputStream(f);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return p;
    }
    
    /**
     * Load an XML file from disk.
     * @param h a project reference
     * @param path the relative file path
     * @return an XML document available at that location, or null if no such file
     * @throws IOException if there is any problem loading it
     * @throws SAXException if it is malformed
     */
    public static Document slurpXml(AntProjectHelper h, String path) throws IOException, SAXException {
        File f = h.resolveFile(path);
        if (!f.isFile()) {
            return null;
        }
        return XMLUtil.parse(new InputSource(Utilities.toURI(f).toString()), false, true, XMLUtil.defaultErrorHandler(), null);
    }
    
    /**
     * Load a text file from disk.
     * Assumes UTF-8 encoding.
     * @param h a project reference
     * @param path the relative file path
     * @return the raw contents of the text file at that point, or null if no such file
     * @throws IOException if there is any problem loading it
     */
    public static String slurpText(AntProjectHelper h, String path) throws IOException {
        File f = h.resolveFile(path);
        if (!f.isFile()) {
            return null;
        }
        InputStream is = new FileInputStream(f);
        try {
            Reader r = new InputStreamReader(is, "UTF-8");
            StringBuilder b = new StringBuilder();
            char[] buf = new char[4096];
            int read;
            while ((read = r.read(buf)) != -1) {
                b.append(buf, 0, read);
            }
            return b.toString();
        } finally {
            is.close();
        }
    }
    
    /**
     * Get a sample <code>build.xsl</code>.
     * @return a URL to a stylesheet
     */
    public static URL testBuildXmlStylesheet() {
        return AntBasedTestUtil.class.getResource("data/build.xsl");
    }

    /**
     * A sample listener that just collects events it receives.
     */
    public static final class TestListener implements AntProjectListener {
        
        private final List<AntProjectEvent> events = new ArrayList<AntProjectEvent>();
        
        /** Create a new listener. */
        public TestListener() {}
        
        /**
         * Get a list of received events, in order.
         * Also clears the list for the next call.
         * @return an ordered list of Ant project events
         */
        public AntProjectEvent[] events() {
            AntProjectEvent[] evs = events.toArray(new AntProjectEvent[0]);
            events.clear();
            return evs;
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            assert ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH) || ev.getPath().equals(AntProjectHelper.PRIVATE_XML_PATH);
            events.add(ev);
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            assert !ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH) && !ev.getPath().equals(AntProjectHelper.PRIVATE_XML_PATH);
            events.add(ev);
        }
        
    }
    
    /**
     * Count the number of (line-based) differences between two text files.
     * The returned count has, in this order:
     * <ol>
     * <li>Lines modified between the first and second files.
     * <li>Lines added in the second file that were not in the first.
     * <li>Lines removed in the second file that were in the first.
     * </ol>
     * It thus serves as a summary of the number of diff lines you would expect
     * to get from e.g. a version control system doing a normal text checkin.
     * @param r1 the first file (the reader will not be closed for you)
     * @param r2 the second file (the reader will not be closed for you)
     * @return a count of lines modified, added, and removed (resp.)
     * @throws IOException in case reading from the files failed
     */
    public static int[] countTextDiffs(Reader r1, Reader r2) throws IOException {
        DiffProvider dp = new BuiltInDiffProvider();
        Difference[] diffs = dp.computeDiff(r1, r2);
        int[] count = new int[3];
        for (int i = 0; i < diffs.length; i++) {
            switch (diffs[i].getType()) {
                case Difference.CHANGE:
                    count[0] += Math.max(diffs[i].getFirstEnd() - diffs[i].getFirstStart()+1, diffs[i].getSecondEnd() - diffs[i].getSecondStart()+1);
                    break;
                case Difference.ADD:
                    count[1] += (diffs[i].getSecondEnd() - diffs[i].getSecondStart()+1);
                    break;
                case Difference.DELETE:
                    count[2] += (diffs[i].getFirstEnd() - diffs[i].getFirstStart()+1);
                    break;
                default:
                    assert false : diffs[i];    
            }
        }
        return count;
    }
    
    /**
     * Get a sample file collocation query provider.
     * Files under the supplied root are normally considered to be collocated.
     * However the subdirectory <samp>separate</samp> (if it exists) forms its own root.
     * And the subdirectory <samp>transient</samp> (if it exists) does not form a root,
     * but any files in there are not considered collocated with anything.
     */
    public static CollocationQueryImplementation2 testCollocationQueryImplementation(URI root) {
        return new TestCollocationQueryImplementation(root);
    }
    
    private static final class TestCollocationQueryImplementation implements CollocationQueryImplementation2 {
        
        private final String rootPath;
        private final String separatePath;
        private final String transientPath;
        
        TestCollocationQueryImplementation(URI root) {
            rootPath = root.toString();
            separatePath = rootPath + "separate/";
            transientPath = rootPath + "transient/";
        }
        
        @Override public boolean areCollocated(URI file1, URI file2) {
            URI root1 = findRoot(file1);
            if (root1 == null) {
                return false;
            } else {
                return root1.equals(findRoot(file2));
            }
        }
        
        @Override public URI findRoot(URI file) {
            String path = file.toString();
            if (!path.startsWith(rootPath)) {
                return null;
            }
            if (path.startsWith(separatePath)) {
                return URI.create(separatePath);
            }
            if (path.startsWith(transientPath)) {
                return null;
            }
            return URI.create(rootPath);
        }

        @Override
        public String toString() {
            return "TestCollocationQueryImplementation[" + rootPath + "]";
        }
        
    }
    
    /**
     * Replace all occurrences of a given string in a file with a new string.
     * UTF-8 encoding is assumed.
     * @param f the file to modify
     * @param from the search string
     * @param to the replacement string
     * @return a count of how many occurrences were replaced
     * @throws IOException in case reading or writing the file failed
     */
    public static int replaceInFile(File f, String from, String to) throws IOException {
        StringBuilder b = new StringBuilder((int)f.length());
        InputStream is = new FileInputStream(f);
        try {
            Reader r = new InputStreamReader(is, "UTF-8");
            char[] buf = new char[4096];
            int i;
            while ((i = r.read(buf)) != -1) {
                b.append(buf, 0, i);
            }
        } finally {
            is.close();
        }
       String s = b.toString();
       String rx = "\\Q" + from + "\\E";
       Pattern patt;
       try {
           patt = Pattern.compile(rx);
       } catch (PatternSyntaxException e) {
           assert false : e;
           return -1;
       }
       Matcher m = patt.matcher(s);
       int count = 0;
       while (m.find()) {
           count++;
       }
       String s2 = s.replaceAll(rx, to);
       assert s2.length() - s.length() == count * (to.length() - from.length());
       OutputStream os = new FileOutputStream(f);
       try {
           Writer w = new OutputStreamWriter(os, "UTF-8");
           w.write(s2);
           w.flush();
       } finally {
           os.close();
       }
       return count;
    }
    
    public static class TestAntArtifact extends AntArtifact {

        private URI[] uris;
        private Project p;
        private AntProjectHelper h;

        public TestAntArtifact(URI[] uris, AntProjectHelper h) {
            this.uris = uris;
            try {
                this.p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
            } catch ( Exception e) {
                e.printStackTrace();
            }
            this.h = h;
        }

        public String getType() {
            return "multi-jar"; // NOI18N
        }

        public File getScriptLocation() {
            return h.resolveFile(GeneratedFilesHelper.BUILD_XML_PATH);
        }

        public String getTargetName() {
            return "build"; // NOI18N
        }

        public String getCleanTargetName() {
            return "clean"; // NOI18N
        }
        
        public URI[] getArtifactLocations() {
            return uris;
        }

        public Project getProject() {
            return p;
        }

    }

    public static final class TestMutablePropertyProvider implements PropertyProvider {

        public final Map<String,String> defs;
        private final ChangeSupport cs = new ChangeSupport(this);

        public TestMutablePropertyProvider(Map<String,String> defs) {
            this.defs = defs;
        }

        public void mutated() {
            cs.fireChange();
        }

        public Map<String,String> getProperties() {
            return defs;
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

    }

}
