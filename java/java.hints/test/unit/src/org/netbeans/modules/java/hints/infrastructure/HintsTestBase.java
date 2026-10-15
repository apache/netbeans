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
package org.netbeans.modules.java.hints.infrastructure;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.gen.WhitespaceIgnoringDiff;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * @author Jan Lahoda
 */
public class HintsTestBase extends NbTestCase {
    private static final String DATA_EXTENSION = "org/netbeans/test/java/hints/";
    private Document doc;
    
    /** Need to be defined because of JUnit */
    public HintsTestBase(String name) {
        super(name);
        
    }
    
    protected FileObject packageRoot;
    private FileObject testSource;
    private JavaSource js;
    protected CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    private CancellableTask<CompilationInfo> task;
    
    @Override
    protected void setUp() throws Exception {
        doSetUp(layer());
    }
    
    protected void doSetUp(String resource) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        SourceUtilsTestUtil.prepareTest(new String[] {"META-INF/generated-layer.xml", "org/netbeans/modules/java/editor/resources/layer.xml", resource}, new Object[] {
            JavaDataLoader.class,
            new MimeDataProvider() {
                public Lookup getLookup(MimePath mimePath) {
                    return Lookups.fixed(new Object[] {
                        new JavaKit(),
                    });
                }
            },
            new LanguageProvider() {
                public Language<?> findLanguage(String mimePath) {
                    return JavaTokenId.language();
                }
                
                public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token,
                        LanguagePath languagePath,
                        InputAttributes inputAttributes) {
                    return null;
                }
            },
            new JavaCustomIndexer.Factory()
        });

        clearWorkDir();

        if (cache == null) {
            cache = FileUtil.normalizeFile(new File(getWorkDir(), "cache"));
            cacheFO = FileUtil.createFolder(cache);
            
            IndexUtil.setCacheFolder(cache);
            
            if (createCaches()) {
                TestUtilities.analyzeBinaries(SourceUtilsTestUtil.getBootClassPath());
            }
        }
        
        Main.initializeURLFactory();
    }
    
    protected boolean createCaches() {
        return true;
    }
    
    protected String testDataExtension() {
        return DATA_EXTENSION;
    }
    
    protected boolean onlyMainResource() {
        return false;
    }
    
    protected String layer() {
        return "org/netbeans/modules/java/hints/resources/layer.xml";
    }
    
    protected void prepareTest(String capitalizedName) throws Exception {
        prepareTest(capitalizedName, "1.5");
    }
    
    protected void copyAdditionalData() throws Exception {
        
    }

    protected void prepareTest(String capitalizedName, String sourceLevel) throws Exception {
        File workFolder = new File(getWorkDir(), "work");
        FileObject workFO = FileUtil.createFolder(workFolder);
        
        assertNotNull(workFO);

        workFO.delete();

        workFO = FileUtil.createFolder(workFolder);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        
        packageRoot = FileUtil.createFolder(sourceRoot, DATA_EXTENSION);

        SourceUtilsTestUtil.setSourceLevel(packageRoot, sourceLevel);

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = testDataExtension();
        File   testPackageFile = new File(getDataDir(), testPackagePath);
        final String testFileName = capitalizedName.substring(capitalizedName.lastIndexOf('.') + 1) + ".java";
        
        String[] names = testPackageFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.equals(testFileName) || (!onlyMainResource() && name.endsWith(".java")))
                    return true;
                
                return false;
            }
        });
        
        String[] files = new String[names.length];
        
        for (int cntr = 0; cntr < files.length; cntr++) {
            files[cntr] = testPackagePath + names[cntr];
        }
        
        copyFiles(getDataDir(), FileUtil.toFile(sourceRoot), files);
        copyAdditionalData();
        packageRoot.refresh();
        
        testSource = packageRoot.getFileObject(testFileName);
        
        assertNotNull(testSource);

        DataObject od = DataObject.find(testSource);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", Utilities.JAVA_MIME_TYPE);

        //XXX: takes a long time
        //re-index, in order to find classes-living-elsewhere
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);

        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
        
        task = new LazyHintComputationFactory().createTask(testSource);
    }
    
    private void copyFiles(File sourceDir, File destDir, String[] resourceNames) throws IOException {
        for( String resourceName : resourceNames ) {

            File src = new File( sourceDir, resourceName );

            if ( !src.canRead() ) {
                TestCase.fail( "The test requires the file: " + resourceName + " to be readable and stored in: " + sourceDir );
            }

            InputStream is = new FileInputStream( src );
            BufferedInputStream bis = new BufferedInputStream( is );

            //Strip last folder to align with packagename
            File dest = new File( destDir, resourceName.replace(testDataExtension(), DATA_EXTENSION) );
            File parent = dest.getParentFile();

            if ( !parent.exists() ) {
                parent.mkdirs();
            }

            dest.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( dest ) );

            copyFile( bis, bos );
        }
    }

    private static int BLOCK_SIZE = 16384;
    private static void copyFile( InputStream is, OutputStream os ) throws IOException {
        byte[] b = new byte[ BLOCK_SIZE ];
        int count = is.read(b);

        while (count != -1)
        {
         os.write(b, 0, count);
         count = is.read(b);
        }

        is.close();
        os.close();
    }
    
//    private int getOffset(Document doc, int linenumber, int column) {
//        return NbDocument.findLineOffset((StyledDocument) doc, linenumber - 1) + column - 1;
//    }
    
    private List<Fix> getFixes(ErrorDescription d) throws Exception {
        LazyFixList f = d.getFixes();
        
        f.getFixes();
        
        task.run(info);
        
        return f.getFixes();
    }
    
    //XXX: copied from org.netbeans.modules.editor.hints.borrowed.ListCompletionView, would be nice to have this on one place only:
        private List<Fix> sortFixes(Collection<Fix> fixes) {
            fixes = new LinkedHashSet<Fix>(fixes);
            
            List<EnhancedFix> sortableFixes = new ArrayList<EnhancedFix>();
            List<Fix> other = new LinkedList<Fix>();
            
            for (Fix f : fixes) {
                if (f instanceof EnhancedFix) {
                    sortableFixes.add((EnhancedFix) f);
                } else {
                    other.add(f);
                }
            }
            
            sortableFixes.sort(new FixComparator());
            
            List<Fix> result = new ArrayList<Fix>();
            
            result.addAll(sortableFixes);
            result.addAll(other);
            
            return result;
        }

        private static final class FixComparator implements Comparator<EnhancedFix> {

            public int compare(EnhancedFix o1, EnhancedFix o2) {
                return compareText(o1.getSortText(), o2.getSortText());
            }
            
        }
        
        private static int compareText(CharSequence text1, CharSequence text2) {
            int len = Math.min(text1.length(), text2.length());
            for (int i = 0; i < len; i++) {
                char ch1 = text1.charAt(i);
                char ch2 = text2.charAt(i);
                if (ch1 != ch2) {
                    return ch1 - ch2;
                }
            }
            return text1.length() - text2.length();
        }
    
    private int getStartLine(ErrorDescription d) throws IOException {
        return d.getRange().getBegin().getLine();
    }
    
    protected void performHintsPresentCheck(String className, int line, int column, boolean present) throws Exception {
        prepareTest(className);
        DataObject od = DataObject.find(testSource);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        Document doc = ec.openDocument();
        
        List<ErrorDescription> errors = new ErrorHintsProvider().computeErrors(info, doc, Utilities.JAVA_MIME_TYPE);
        List<Fix> fixes = new ArrayList<Fix>();
        
        for (ErrorDescription d : errors) {
            if (getStartLine(d) + 1 == line)
                fixes.addAll(getFixes(d));
        }
        
        if (present) {
            assertTrue(fixes != null && !fixes.isEmpty());
        } else {
            assertTrue(fixes == null || fixes.isEmpty());
        }
    }
    
    protected void performTestDoNotPerform(String className, int line, int column) throws Exception {
        prepareTest(className);
        DataObject od = DataObject.find(testSource);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        Document doc = ec.openDocument();
        
        List<ErrorDescription> errors = new ErrorHintsProvider().computeErrors(info, doc, Utilities.JAVA_MIME_TYPE);
        List<Fix> fixes = new ArrayList<Fix>();
        
        for (ErrorDescription d : errors) {
            if (getStartLine(d) + 1 == line)
                fixes.addAll(getFixes(d));
        }
        
        fixes = sortFixes(fixes);
        
        File fixesDump = new File(getWorkDir(), getName() + "-hints.out");
        File diff   = new File(getWorkDir(), getName() + "-hints.diff");
        
        Writer hintsWriter = new FileWriter(fixesDump);
        
        for (Fix f : fixes) {
            hintsWriter.write(f.getText());
            hintsWriter.write("\n");
        }
        
        hintsWriter.close();
        
        File hintsGolden = getGoldenFile(getName() + "-hints.pass");
        
        assertFile(fixesDump, hintsGolden, diff);
    }
    
    protected  void performTest(String className, String performHint, int line, int column) throws Exception {
        performTest(className, className, performHint, line, column, true);
    }
    
    protected void performTest(String className, String modifiedClassName,
            String performHint, int line, int column, boolean checkHintList) throws Exception {
        performTest(className, modifiedClassName, performHint, line, column, checkHintList, "1.5");
    }

    protected void performTest(String className, String modifiedClassName,
            String performHint, int line, int column, boolean checkHintList, String sourceLevel) throws Exception {
        prepareTest(className, sourceLevel);
        DataObject od = DataObject.find(testSource);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        try {
            Document doc = ec.openDocument();
            
            List<ErrorDescription> errors = new ErrorHintsProvider().computeErrors(info, doc, Utilities.JAVA_MIME_TYPE);
            List<Fix> fixes = new ArrayList<Fix>();
            
            for (ErrorDescription d : errors) {
                if (getStartLine(d) + 1 == line)
                    fixes.addAll(getFixes(d));
            }
            
            fixes = sortFixes(fixes);
        
            Fix toPerform = null;
            
            if (checkHintList) {
                File fixesDump = new File(getWorkDir(), getName() + "-hints.out");
                
                Writer hintsWriter = new FileWriter(fixesDump);
                
                for (Fix f : fixes) {
                    if (!includeFix(f)) continue;
                    if (f.getText().indexOf(performHint) != (-1)) {
                        toPerform = f;
                    }
                    
                    hintsWriter.write(f.getText());
                    hintsWriter.write("\n");
                }
                
                hintsWriter.close();
                
                File hintsGolden = getGoldenFile(getName() + "-hints.pass");
                File diff   = new File(getWorkDir(), getName() + "-hints.diff");
                
                assertFile(fixesDump, hintsGolden, diff);
            } else {
                for (Fix f : fixes) {
                    if (f.getText().indexOf(performHint) != (-1)) {
                        toPerform = f;
                    }
                }
            }
            
            assertNotNull(toPerform);
            
            toPerform.implement();
            
            File dump   = new File(getWorkDir(), getName() + ".out");
            
            Writer writer = new FileWriter(dump);
            
            Document modifDoc;
            if (className.equals(modifiedClassName)) {
                modifDoc = doc;
            } else {
                FileObject modFile = packageRoot.getFileObject(modifiedClassName + ".java");
                od = DataObject.find(modFile);
                ec = od.getCookie(EditorCookie.class);
                modifDoc = ec.openDocument();
            }
            
            writer.write(modifDoc.getText(0, modifDoc.getLength()));
            
            writer.close();
            
            File golden = getGoldenFile();
            
            assertNotNull(golden);
            
            File diff   = new File(getWorkDir(), getName() + ".diff");
            
            assertFile(dump, golden, diff, new WhitespaceIgnoringDiff());
        } finally {
            LifecycleManager.getDefault().saveAll();
        }
    }

    protected boolean includeFix(Fix f) {
        return true;
    }
}
