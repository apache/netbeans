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

package org.netbeans.modules.java.project.ui;

import org.netbeans.modules.java.project.ui.JavaAntLogger;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.parsing.impl.indexing.implspi.ActiveDocumentProvider;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Test hyperlinking functionality of {@link JavaAntLogger}.
 * @author Jesse Glick
 */
public final class JavaAntLoggerTest extends NbTestCase {
    
    public JavaAntLoggerTest(String name) {
        super(name);
    }
    
    private File simpleAppDir;
    private Properties props;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(
                new IOP(),
                new IFL(),
                new SFBQ(),
                new StatusDisplayer() {
                    @Override public String getStatusText() {return "";}
                    @Override public void setStatusText(String text) {}
                    @Override public void addChangeListener(ChangeListener l) {}
                    @Override public void removeChangeListener(ChangeListener l) {}
                },
                new ActiveDocumentProvider() {
                    @Override public Document getActiveDocument() {return null;}
                    @Override public Set<? extends Document> getActiveDocuments() {return Collections.emptySet();}
                    @Override public void addActiveDocumentListener(ActiveDocumentProvider.ActiveDocumentListener listener) {}
                    @Override public void removeActiveDocumentListener(ActiveDocumentProvider.ActiveDocumentListener listener) {}
                });
        simpleAppDir = new File(getDataDir(), "simple-app");
        assertTrue("have dir " + simpleAppDir, simpleAppDir.isDirectory());
        Lookup.getDefault().lookup(SFBQ.class).setSimpleAppDir(simpleAppDir);
        nonhyperlinkedOut.clear();
        nonhyperlinkedErr.clear();
        hyperlinkedOut.clear();
        hyperlinkedErr.clear();
        String junitJarS = System.getProperty("test.junit.jar");
        assertNotNull("defined test.junit.jar", junitJarS);
        File junitJar = new File(junitJarS);
        assertTrue("file " + junitJar + " exists", junitJar.isFile());
        props = new Properties();
        props.setProperty("libs.junit.classpath", junitJar.getAbsolutePath()); // #50261
    }

    private void assertHyperlinkPattern(String resourceAndLineNumber, String line) {
        assertEquals(resourceAndLineNumber, String.valueOf(JavaAntLogger.parseStackTraceLine(line)));
    }

    public void testHyperlinkPattern() throws Exception {
        assertHyperlinkPattern("simpleapp/Clazz.java:4", "\tat simpleapp.Clazz.run(Clazz.java:4)");
        assertHyperlinkPattern("simpleapp/Clazz.java:4", "\tat simpleapp.Clazz.<clinit>(Clazz.java:4)");
        assertHyperlinkPattern("Main.java:4", "\tat Main.run(Main.java:4)");
        assertHyperlinkPattern("simpleapp/Clazz.java:4", "simpleapp.Clazz.run(Clazz.java:4)"); // # 153057
        assertHyperlinkPattern("org/openide/filesystems/MultiFileObject.java:1",
                "\tat org.openide.filesystems.MultiFileObject.fileFolderCreated(Unknown Source)"); // #17734
        assertHyperlinkPattern("org/openide/filesystems/MultiFileObject.java:1",
                "\tat org.openide.filesystems.MultiFileObject$Inner$1.fileFolderCreated(Unknown Source)"); // #17734
        assertHyperlinkPattern("some/pkg/PublicOuterClass.java:123", "\tat some.pkg.PrivateOuterClass.meth(PublicOuterClass.java:123)");
        assertHyperlinkPattern("používá/Háček.java:4", "\tat používá.Háček.run(Háček.java:4)");
    }
    
    public void testHyperlinkRun() throws Exception {
        FileObject buildXml = FileUtil.toFileObject(new File(simpleAppDir, "build.xml"));
        assertNotNull("have build.xml as a FileObject", buildXml);
        final int res = ActionUtils.runTarget(buildXml, new String[] {"clean", "run"}, props).result();
        assertEquals(0, res);
        //System.out.println("nonhyperlinkedOut=" + nonhyperlinkedOut + " nonhyperlinkedErr=" + nonhyperlinkedErr + " hyperlinkedOut=" + hyperlinkedOut + " hyperlinkedErr=" + hyperlinkedErr);
        assertTrue("got a hyperlink for Clazz.run NPE", hyperlinkedErr.contains("\tat simpleapp.Clazz.run(Clazz.java:43)"));
    }
    
    /** See #44328. */
    @RandomlyFails
    public void testHyperlinkTest() throws Exception {
        FileObject buildXml = FileUtil.toFileObject(new File(simpleAppDir, "build.xml"));
        assertNotNull("have build.xml as a FileObject", buildXml);
        ActionUtils.runTarget(buildXml, new String[] {"clean", "test"}, props).result();
        //System.out.println("nonhyperlinkedOut=" + nonhyperlinkedOut + " nonhyperlinkedErr=" + nonhyperlinkedErr + " hyperlinkedOut=" + hyperlinkedOut + " hyperlinkedErr=" + hyperlinkedErr);
        assertTrue("got a hyperlink for Clazz.run NPE in " + hyperlinkedErr + " vs. " + nonhyperlinkedErr + " (and " + nonhyperlinkedOut + ")", hyperlinkedErr.contains("\tat simpleapp.Clazz.run(Clazz.java:43)"));
    }
    
    private static final class SFBQ implements SourceForBinaryQueryImplementation {
        
        private URL buildClasses, buildTestClasses;
        private FileObject src, testSrc;
        
        public void setSimpleAppDir(File simpleAppDir) throws Exception {
            buildClasses = slashify(Utilities.toURI(new File(simpleAppDir, "build" + File.separatorChar + "classes")).toURL());
            buildTestClasses = slashify(Utilities.toURI(new File(simpleAppDir, "build" + File.separatorChar + "test" + File.separatorChar + "classes")).toURL());
            src = FileUtil.toFileObject(new File(simpleAppDir, "src"));
            testSrc = FileUtil.toFileObject(new File(simpleAppDir, "test"));
        }
        
        private static URL slashify(URL u) throws Exception {
            String s = u.toExternalForm();
            if (s.endsWith("/")) {
                return u;
            } else {
                return new URL(s + "/");
            }
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            if (binaryRoot.equals(buildClasses)) {
                return new FixedResult(src);
            } else if (binaryRoot.equals(buildTestClasses)) {
                return new FixedResult(testSrc);
            } else {
                return null;
            }
        }
        
        private static final class FixedResult implements SourceForBinaryQuery.Result {
            
            private final FileObject dir;
            
            public FixedResult(FileObject dir) {
                this.dir = dir;
            }

            public FileObject[] getRoots() {
                return new FileObject[] {dir};
            }

            public void addChangeListener(ChangeListener l) {}

            public void removeChangeListener(ChangeListener l) {}
            
        }
        
    }
    
    @SuppressWarnings("deprecation") // for flushReader
    private static final class IOP extends IOProvider implements InputOutput {
        
        public IOP() {}

        public InputOutput getIO(String name, boolean newIO) {
            return this;
        }

        public OutputWriter getStdOut() {
            throw new UnsupportedOperationException();
        }

        public OutputWriter getOut() {
            return new OW(false);
        }

        public OutputWriter getErr() {
            return new OW(true);
        }

        public Reader getIn() {
            return new StringReader("");
        }

        public Reader flushReader() {
            return getIn();
        }

        public void closeInputOutput() {}

        public boolean isClosed() {
            return false;
        }

        public boolean isErrSeparated() {
            return false;
        }

        public boolean isFocusTaken() {
            return false;
        }

        public void select() {}

        public void setErrSeparated(boolean value) {}

        public void setErrVisible(boolean value) {}

        public void setFocusTaken(boolean value) {}

        public void setInputVisible(boolean value) {}

        public void setOutputVisible(boolean value) {}
        
    }
    
    private static final List<String> nonhyperlinkedOut = new ArrayList<String>();
    private static final List<String> nonhyperlinkedErr = new ArrayList<String>();
    private static final List<String> hyperlinkedOut = new ArrayList<String>();
    private static final List<String> hyperlinkedErr = new ArrayList<String>();
    
    private static final class OW extends OutputWriter {
        
        private final boolean err;
        
        public OW(boolean err) {
            super(new StringWriter());
            this.err = err;
        }

        public void println(String s, OutputListener l) throws IOException {
            message(s, l != null);
        }

        @Override
        public void println(String x) {
            message(x, false);
        }
        
        private void message(String msg, boolean hyperlinked) {
            List<String> messages = hyperlinked ?
                (err ? hyperlinkedErr : hyperlinkedOut) :
                (err ? nonhyperlinkedErr : nonhyperlinkedOut);
            messages.add(msg);
        }
        
        public void reset() throws IOException {}

    }

    /** Copied from AntLoggerTest. */
    private static final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }

}
