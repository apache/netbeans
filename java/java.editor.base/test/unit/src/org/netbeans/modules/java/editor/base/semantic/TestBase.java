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
package org.netbeans.modules.java.editor.base.semantic;

import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import javax.lang.model.SourceVersion;
import javax.swing.event.ChangeListener;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.util.Pair;

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
        performTest(() -> {
            File wd = getWorkDir();
            File testSource = new File(wd, "test/" + fileName + ".java");

            testSource.getParentFile().mkdirs();

            File dataFolder = new File(getDataDir(), "org/netbeans/modules/java/editor/base/semantic/data/");

            for (File f : dataFolder.listFiles()) {
                copyToWorkDir(f, new File(wd, "test/" + f.getName()));
            }

            return FileUtil.toFileObject(testSource);
        }, performer, doCompileRecursively,
        actual -> {
            File output = new File(getWorkDir(), getName() + ".out");
            try (Writer out2File = new FileWriter(output)) {
                out2File.append(actual);
            }

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
        });
    }

    protected void performTest(String filename, String content, final Performer performer, boolean doCompileRecursively, String expected) throws Exception {
        performTest(() -> {
            FileObject wd = FileUtil.toFileObject(getWorkDir());
            FileObject result = FileUtil.createData(wd, filename);

            try (Writer out = new OutputStreamWriter(result.getOutputStream())) {
                out.write(content);
            }

            return result;
        }, performer, doCompileRecursively,
        actual -> {
            assertEquals(expected, actual);
        });
    }

    protected void performTest(Input input, final Performer performer, boolean doCompileRecursively, Validator validator) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[] {
            new MIMEResolverImpl(),
            new CompilerOptionsQueryImplementation() {
                @Override
                public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
                    if (testSourceFO == file) {
                        return new CompilerOptionsQueryImplementation.Result() {
                            @Override
                            public List<? extends String> getArguments() {
                                return extraOptions;
                            }

                            @Override
                            public void addChangeListener(ChangeListener listener) {}

                            @Override
                            public void removeChangeListener(ChangeListener listener) {}
                        };
                    }
                    return null;
                }
            }
        });
        
	FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
	FileObject cache   = scratch.createFolder("cache");
	
        File wd         = getWorkDir();

        testSourceFO = input.prepare();

        assertNotNull(testSourceFO);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(testSourceFO, sourceLevel);
        }
        
        File testBuildTo = new File(wd, "test-build");
        
        testBuildTo.mkdirs();
        
        FileObject srcRoot = testSourceFO.getParent();
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
                
        StringWriter out = new StringWriter();
        
        for (HighlightImpl h : highlights) {
            out.write(h.getHighlightTestData());
            
            out.write("\n");
        }
        
        out.close();
        
        validator.validate(out.toString());
    }
    
    protected void performTest(String fileName, String code, Performer performer, String... expected) throws Exception {
        performTest(fileName, code, performer, false, expected);
    }

    protected void performTest(String fileName, String code, Performer performer, boolean doCompileRecursively, String... expected) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[] {new MIMEResolverImpl()});

	FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
	FileObject cache   = scratch.createFolder("cache");

        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");

        testSource.getParentFile().mkdirs();
        TestUtilities.copyStringToFile(testSource, code);

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

        assertEquals(Arrays.asList(expected),
                     highlights.stream()
                               .map(h -> h.getHighlightTestData())
                               .collect(Collectors.toList()));
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

    private List<String> extraOptions = new ArrayList<>();

    protected final void enablePreview() {
        String svName = SourceVersion.latest().name();
        setSourceLevel(svName.substring(svName.indexOf('_') + 1));
        extraOptions.add("--enable-preview");
    }

    private boolean showPrependedText;

    protected final void setShowPrependedText(boolean showPrependedText) {
        this.showPrependedText = showPrependedText;
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
        public void setHighlights(Document doc, Collection<Pair<int[], Coloring>> highlights, Map<int[], String> preText) {
            for (Pair<int[], Coloring> h : highlights) {
                this.highlights.add(new HighlightImpl(doc, h.first()[0], h.first()[1], h.second()));
            }
            if (showPrependedText) {
                for (Entry<int[], String> e : preText.entrySet()) {
                    this.highlights.add(new HighlightImpl(doc, e.getKey()[0], e.getKey()[1], e.getValue()));
                }
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
    
    protected interface Input {
        public FileObject prepare() throws Exception;
    }

    protected interface Validator {
        public void validate(String actual) throws Exception;
    }
}
