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
package org.netbeans.modules.java.editor.base.semantic;

import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public abstract class TestBase extends NbTestCase {
    
    private static final boolean SHOW_GUI_DIFF = false;
    
    /**
     * Creates a new instance of TestBase
     */
    public TestBase(String name) {
        super(name);
    }
    
    private FileObject testSourceFO;
    private URL        testBuildDir;
    
    protected final void copyToWorkDir(File resource, File toFile) throws IOException {
        //TODO: finally:
        InputStream is = new FileInputStream(resource);
        OutputStream outs = new FileOutputStream(toFile);
        
        int read;
        
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        
        outs.close();
        
        is.close();
    }
    
    protected void performTest(String fileName, final Performer performer) throws Exception {
        performTest(fileName, performer, false);
    }
    
    protected void performTest(String fileName, final Performer performer, boolean doCompileRecursively) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[] {new MIMEResolverImpl()});
        
	FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
	FileObject cache   = scratch.createFolder("cache");
	
        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");
        
        testSource.getParentFile().mkdirs();
        
        File dataFolder = new File(getDataDir(), "org/netbeans/modules/java/editor/base/semantic/data/");
        
        for (File f : dataFolder.listFiles()) {
            copyToWorkDir(f, new File(wd, "test/" + f.getName()));
        }
        
        testSourceFO = FileUtil.toFileObject(testSource);

        assertNotNull(testSourceFO);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(testSourceFO, sourceLevel);
        }
        
        File testBuildTo = new File(wd, "test-build");
        
        testBuildTo.mkdirs();
        
        FileObject srcRoot = FileUtil.toFileObject(testSource.getParentFile());
        SourceUtilsTestUtil.prepareTest(srcRoot,FileUtil.toFileObject(testBuildTo), cache);
        
        if (doCompileRecursively) {
            SourceUtilsTestUtil.compileRecursively(srcRoot);
        }

        final Document doc = getDocument(testSourceFO);
        final List<HighlightImpl> highlights = new ArrayList<HighlightImpl>();
        
        JavaSource source = JavaSource.forFileObject(testSourceFO);
        
        assertNotNull(source);
        
	final CountDownLatch l = new CountDownLatch(1);
	
        source.runUserActionTask(new Task<CompilationController>() {
            
            
            public void run(CompilationController parameter) {
                try {
                    parameter.toPhase(Phase.UP_TO_DATE);
                    
                    ErrorDescriptionSetterImpl setter = new ErrorDescriptionSetterImpl();
                    
                    performer.compute(parameter, doc, setter);
                    
                    highlights.addAll(setter.highlights);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
		    l.countDown();
		}
            }
            
        }, true);
	
        l.await();
                
        File output = new File(getWorkDir(), getName() + ".out");
        Writer out = new FileWriter(output);
        
        for (HighlightImpl h : highlights) {
            out.write(h.getHighlightTestData());
            
            out.write("\n");
        }
        
        out.close();
                
        boolean wasException = true;
        
        try {
            File goldenFile = getGoldenFile();
            File diffFile = new File(getWorkDir(), getName() + ".diff");
            
            assertFile(output, goldenFile, diffFile);
            wasException = false;
        } finally {
            if (wasException && SHOW_GUI_DIFF) {
                try {
                    String name = getClass().getName();
                    
                    name = name.substring(name.lastIndexOf('.') + 1);
                    
                    ShowGoldenFiles.run(name, getName(), fileName);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected ColoringAttributes getColoringAttribute() {
        return ColoringAttributes.UNUSED;
    }
    
    public static Collection<HighlightImpl> toHighlights(Document doc, Map<Token, Coloring> colors) {
        List<HighlightImpl> highlights = new ArrayList<HighlightImpl>();
        
        for (Entry<Token, Coloring> e : colors.entrySet()) {
            highlights.add(new HighlightImpl(doc, e.getKey(), e.getValue()));
        }
        
        return highlights;
    }
    
    public static interface Performer {
        
        public void compute(CompilationController parameter, Document doc, SemanticHighlighterBase.ErrorDescriptionSetter setter);
        
    }
    
    protected final Document getDocument(FileObject file) throws IOException {
        DataObject od = DataObject.find(file);
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
        
        if (ec != null) {
            Document doc = ec.openDocument();
            
            doc.putProperty(Language.class, JavaTokenId.language());
            doc.putProperty("mimeType", "text/x-java");
            
            return doc;
        } else {
            return null;
        }
    }

    private String sourceLevel;

    protected final void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    final class ErrorDescriptionSetterImpl implements SemanticHighlighterBase.ErrorDescriptionSetter {
        private final Set<HighlightImpl> highlights = new TreeSet<HighlightImpl>(new Comparator<HighlightImpl>() {
            public int compare(HighlightImpl o1, HighlightImpl o2) {
                return o1.getEnd() - o2.getEnd();
            }
            
        });
        
        public void setErrors(Document doc, List<ErrorDescription> errs, List<TreePathHandle> allUnusedImports) {
        }
    
        @Override
        public void setHighlights(Document doc, Collection<int[]> highlights) {
            for (int[] h : highlights) {
                this.highlights.add(new HighlightImpl(doc, h[0], h[1], EnumSet.of(getColoringAttribute())));
            }
        }

        @Override
        public void setColorings(Document doc, Map<Token, Coloring> colorings) {
            highlights.addAll(toHighlights(doc, colorings));
        }
    }
    
    static class MIMEResolverImpl extends MIMEResolver {
        public String findMIMEType(FileObject fo) {
            if ("java".equals(fo.getExt())) {
                return "text/x-java";
            } else {
                return null;
            }
        }
    }
    
}
