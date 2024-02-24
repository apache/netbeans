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

package org.netbeans.modules.csl.api.test;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.GsfLanguage;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.Rule.SelectionRule;
import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.ElementChange;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.EditHistory;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.core.CslEditorKit;
import org.netbeans.modules.csl.core.GsfIndentTaskFactory;
import org.netbeans.modules.csl.core.GsfReformatTaskFactory;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.editor.codetemplates.CslCorePackageAccessor;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.csl.hints.infrastructure.HintsSettings;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.*;
import org.netbeans.modules.parsing.impl.indexing.lucene.TestIndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.lucene.TestIndexFactoryImpl.TestIndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.lucene.TestIndexFactoryImpl.TestIndexImpl;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Pair;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import static org.openide.util.test.MockLookup.setLookup;

/**
 * @author Tor Norbye
 */
public abstract class CslTestBase extends NbTestCase {

    static {
        // testing performance: set scanner update delay to 0
        System.setProperty(PathRegistry.class.getName()+".FIRER_EVT_COLLAPSE_WINDOW", "0");
    }

    public CslTestBase(String testName) {
        super(testName);
    }

    private Map<String, ClassPath> classPathsForTest;
    private Object[] extraLookupContent = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        if (cleanCacheDir()) {
            clearWorkDir();
        }
        System.setProperty("netbeans.user", getWorkDirPath());
        // XXX are the following four lines actually necessary?
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        assert wd != null;
        FileObject cache = FileUtil.createFolder(wd, "var/cache");
        assert cache != null;
        CacheFolder.setCacheFolder(cache);

        List<URL> layers = new LinkedList<URL>();
        String[] additionalLayers = new String[]{"META-INF/generated-layer.xml"};
        Object[] additionalLookupContent = createExtraMockLookupContent();
        if (additionalLookupContent == null) {
            additionalLookupContent = new Object[0];
        }

        for (int cntr = 0; cntr < additionalLayers.length; cntr++) {
            boolean found = false;

            for (Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(additionalLayers[cntr]); en.hasMoreElements(); ) {
                found = true;
                layers.add(en.nextElement());
            }

            assertTrue(additionalLayers[cntr], found);
        }

        XMLFileSystem xmlFS = new XMLFileSystem();
        xmlFS.setXmlUrls(layers.toArray(new URL[0]));

        FileSystem system = new MultiFileSystem(new FileSystem[] {FileUtil.createMemoryFileSystem(), xmlFS});

        Repository repository = new Repository(system);
        // This has to be before touching ClassPath cla
        
        extraLookupContent = new Object[additionalLookupContent.length + 2];
        int at = 0;
        System.arraycopy(additionalLookupContent, 0, extraLookupContent, at, additionalLookupContent.length);
        at += additionalLookupContent.length;
        // act as a fallback: if no other Repository is found.
        extraLookupContent[at++] = new TestClassPathProvider();
        extraLookupContent[at++] = new TestPathRecognizer();

        // copied from MockLookup; but add 'repository' last, after META-INFs, so any potential 'system' definition takes precedence over 
        // the clumsy one here.
        ClassLoader l = MockLookup.class.getClassLoader();
        setLookup(Lookups.fixed(extraLookupContent), Lookups.metaInfServices(l), Lookups.singleton(l), Lookups.singleton(repository));

        classPathsForTest = createClassPathsForTest();
        if (classPathsForTest != null) {
            RepositoryUpdater.getDefault().start(true);

            Logger logger = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests");
            logger.setLevel(Level.FINEST);
            Waiter w = new Waiter(classPathContainsBinaries());
            logger.addHandler(w);

            // initialize classpaths indexing
            for(Map.Entry<String, ClassPath> entry : classPathsForTest.entrySet()) {
                String cpId = entry.getKey();
                ClassPath cp = entry.getValue();
                GlobalPathRegistry.getDefault().register(cpId, new ClassPath [] { cp });
            }

            w.waitForScanToFinish();
            logger.removeHandler(w);
        }
    }
    
    /**
     * Injects specific services into MockLookup, in preference to the standard ones.
     * @return instances to inject into the Lookup; {@code null} if none.
     * @since 2.65
     */
    protected Object[] createExtraMockLookupContent() {
        return new Object[0];
    }

    @Override
    protected void tearDown() throws Exception {
        if (classPathsForTest != null && !classPathsForTest.isEmpty()) {
            Logger logger = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests");
            logger.setLevel(Level.FINEST);
            Waiter w = new Waiter(classPathContainsBinaries());
            logger.addHandler(w);

            for(Map.Entry<String, ClassPath> entry : classPathsForTest.entrySet()) {
                String cpId = entry.getKey();
                ClassPath cp = entry.getValue();
                GlobalPathRegistry.getDefault().unregister(cpId, new ClassPath [] { cp });
            }

            w.waitForScanToFinish();
            logger.removeHandler(w);
        }

        super.tearDown();
    }

    protected void initializeRegistry() {
        DefaultLanguageConfig defaultLanguage = getPreferredLanguage();
        if (defaultLanguage == null) {
            fail("If you don't implement getPreferredLanguage(), you must override initializeRegistry!");
            return;
        }
        if (!LanguageRegistry.getInstance().isSupported(getPreferredMimeType())) {
            List<Action> actions = Collections.emptyList();
            org.netbeans.modules.csl.core.Language dl = new org.netbeans.modules.csl.core.Language(
                    "unknown", getPreferredMimeType(), actions,
                    defaultLanguage, getCodeCompleter(),
                    getRenameHandler(), defaultLanguage.getDeclarationFinder(),
                    defaultLanguage.getFormatter(), getKeystrokeHandler(),
                    getIndexerFactory(), getStructureScanner(), null,
                    defaultLanguage.isUsingCustomEditorKit());
            List<org.netbeans.modules.csl.core.Language> languages = new ArrayList<org.netbeans.modules.csl.core.Language>();
            languages.add(dl);
            CslCorePackageAccessor.get().languageRegistryAddLanguages(languages);
        }
    }

    protected FileObject touch(final String dir, final String path) throws IOException {
        return touch(new File(dir), path);
    }

    protected FileObject touch(final File dir, final String path) throws IOException {
        if (!dir.isDirectory()) {
            assertTrue("success to create " + dir, dir.mkdirs());
        }
        FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        return touch(dirFO, path);
    }

    protected FileObject touch(final FileObject dir, final String path) throws IOException {
        return FileUtil.createData(dir, path);
    }

    public static final FileObject copyStringToFileObject(FileObject fo, String content) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            try {
                FileUtil.copy(is, os);
                return fo;
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
    }

    /** Return the offset of the given position, indicated by ^ in the line fragment
     * from the fuller text
     */
    public static int getCaretOffset(String text, String caretLine) {
        return getCaretOffsetInternal(text, caretLine).offset;
    }

    /**
     * Like <code>getCaretOffset</code>, but the returned <code>CaretLineOffset</code>
     * contains also the modified <code>caretLine</code> param.

     * @param text
     * @param caretLine
     * @return
     */
    private static CaretLineOffset getCaretOffsetInternal(String text, String caretLine) {
        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue("No occurrence of caretLine " + caretLine + " in text '" + text + "'", lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        return new CaretLineOffset(caretOffset, caretLine);
    }


    /** Copy-pasted from APISupport. */
    protected static String slurp(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8");
        } finally {
            is.close();
        }
    }

    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

    /**
     * Gets the <code>Source</code> for a file. This method makes sure that a
     * <code>Document</code> is loaded from the file and accessible through the
     * returned <code>Source</code> instance. Many language-specific feature
     * implementations rely on that.
     *
     * @param relFilePath The file path relative to <code>getDataDir()</code>.
     *
     * @return The <code>Source</code> instance with a the <code>Document</code>
     *   loaded from the specified file.
     */
    protected Source getTestSource(FileObject f) {
        Document doc = GsfUtilities.getDocument(f, true);
        return Source.create(doc);
    }

    public Project getTestProject(String relativePath) throws Exception {
        FileObject projectDir = getTestFile(relativePath);
        assertNotNull(projectDir);
        Project project = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(project);

        return project;
    }

    protected String readFile(final FileObject fo) {
        return read(fo);
    }

    public static String read(final FileObject fo) {
        try {
            final StringBuilder sb = new StringBuilder(5000);
            fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                public void run() throws IOException {

                    if (fo == null) {
                        return;
                    }

                    InputStream is = fo.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                    while (true) {
                        String line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        sb.append(line);
                        sb.append('\n');
                    }
                }
            });

            if (sb.length() > 0) {
                return sb.toString();
            } else {
                return null;
            }
        }
        catch (IOException ioe){
            ErrorManager.getDefault().notify(ioe);

            return null;
        }
    }

    public BaseDocument getDocument(String s, final String mimeType, final Language language) {
        try {
            BaseDocument doc = new BaseDocument(true, mimeType) {
                @Override
                public boolean isIdentifierPart(char ch) {
                    if (mimeType != null) {
                        org.netbeans.modules.csl.core.Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
                        if (l != null) {
                            GsfLanguage gsfLanguage = l.getGsfLanguage();
                            if (gsfLanguage != null) {
                                return gsfLanguage.isIdentifierChar(ch);
                            }
                        }
                    }

                    return super.isIdentifierPart(ch);
                }
            };

            //doc.putProperty("mimeType", mimeType);
            doc.putProperty(org.netbeans.api.lexer.Language.class, language);

            doc.insertString(0, s, null);

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    public BaseDocument getDocument(String s, String mimeType) {
        Language<?> language = LanguageManager.getInstance().findLanguage(mimeType);
        assertNotNull(language);

        return getDocument(s, mimeType, language);
    }

//    public static BaseDocument createDocument(String s) {
//        try {
//            BaseDocument doc = new BaseDocument(null, false);
//            doc.insertString(0, s, null);
//
//            return doc;
//        }
//        catch (Exception ex){
//            fail(ex.toString());
//            return null;
//        }
//    }
//
    protected BaseDocument getDocument(String s) {
        String mimeType = getPreferredMimeType();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredMimeType()", mimeType);

        GsfLanguage language = getPreferredLanguage();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredLanguage()", language);

        return getDocument(s, mimeType, language.getLexerLanguage());
    }

    protected BaseDocument getDocument(FileObject fo) {
        return getDocument(fo, getPreferredMimeType(), getPreferredLanguage().getLexerLanguage());
    }

    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        try {
//             DataObject dobj = DataObject.find(fo);
//             assertNotNull(dobj);
//
//             EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//             assertNotNull(ec);
//
//             return (BaseDocument)ec.openDocument();
            BaseDocument doc = getDocument(readFile(fo), mimeType, language);
            try {
                DataObject dobj = DataObject.find(fo);
                doc.putProperty(Document.StreamDescriptionProperty, dobj);
            } catch (DataObjectNotFoundException dnfe) {
                fail(dnfe.toString());
            }

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    public static String readFile(File f) throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(f.toPath()), charset);
        return content;
    }

    protected File getDataSourceDir() {
        // Check whether token dump file exists
        // Try to remove "/build/" from the dump file name if it exists.
        // Otherwise give a warning.
        File inputFile = getDataDir();
        String inputFilePath = inputFile.getAbsolutePath();
        boolean replaced = false;
        if (inputFilePath.indexOf(pathJoin("build", "test")) != -1) {
            inputFilePath = inputFilePath.replace(pathJoin("build", "test"), pathJoin("test"));
            replaced = true;
        }
        if (!replaced && inputFilePath.indexOf(pathJoin("test", "work", "sys")) != -1) {
            inputFilePath = inputFilePath.replace(pathJoin("test", "work", "sys"), pathJoin("test", "unit"));
            replaced = true;
        }
        if (!replaced) {
            System.err.println("Warning: Attempt to use dump file " +
                    "from sources instead of the generated test files failed.\n" +
                    "Patterns '/build/test/' or '/test/work/sys/' not found in " + inputFilePath
            );
        }
        inputFile = new File(inputFilePath);
        assertTrue(inputFile.exists());

        return inputFile;
    }

    private static String pathJoin(String... chunks) {
        StringBuilder result = new StringBuilder(File.separator);
        for (String chunk : chunks) {
            result.append(chunk).append(File.separatorChar);
        }
        return result.toString();
    }

    protected File getDataFile(String relFilePath) {
        File inputFile = new File(getDataSourceDir(), relFilePath);
        return inputFile;
    }

    private static List<String> computeVersionVariantsFor(String version) {
        int dot = version.indexOf('.');
        version = version.substring(dot + 1);
        int versionNum = Integer.parseInt(version);
        List<String> versions = new ArrayList<>();

        for (int v = versionNum; v >= 9; v--) {
            versions.add("." + v);
        }

        return versions;
    }

    protected boolean failOnMissingGoldenFile() {
        return true;
    }

    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, String ext) throws Exception {
        assertDescriptionMatches(relFilePath, description, includeTestName, ext, true);
    }

    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, String ext, boolean checkFileExistence) throws Exception {
        assertDescriptionMatches(relFilePath, description, includeTestName, ext, checkFileExistence, false);
    }

    /**
     * A variant that accepts markers in the actual output. Markers identify words in the golden
     * file that should be ignored. Suitable for postprocessed output from partial implementations,
     * so they can be still checked against full specification - otherwise a new set of goldens would have 
     * to be created. Include "*-*" marker in the 'description' at a place where a single word (optional) should
     * be skipped.
     * @param relFilePath relative path to golden file
     * @param description description string
     * @param includeTestName true = append test name to relative path
     * @param ext extension of the golden file
     * @param checkFileExistence check the golden file exists; false means golden file will be created with 'description' as contents.
     * @param skipMarkers true to skip text that matches *-* in the actual output.
     * @throws Exception 
     */
    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, String ext, boolean checkFileExistence, boolean skipMarkers) throws Exception {
        assertDescriptionMatches(relFilePath, description, includeTestName, false, ext, checkFileExistence, skipMarkers);
    }

    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, boolean includeJavaVersion, String ext, boolean checkFileExistence, boolean skipMarkers) throws Exception {
        File rubyFile = getDataFile(relFilePath);
        if (checkFileExistence && !rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        File goldenFile = null;
        if (includeJavaVersion) {
            String version = System.getProperty("java.specification.version");
            for (String variant : computeVersionVariantsFor(version)) {
                goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + variant + ext);
                if (goldenFile.exists())
                    break;
            }
        }
        if (goldenFile == null || !goldenFile.exists()) {
            goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + ext);
        }
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            if (failOnMissingGoldenFile()) {
                NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
            }
            return;
        }

        String expected = readFile(goldenFile);

        // Because the unit test differ is so bad...
        if (false) { // disabled
            if (!expected.equals(description)) {
                BufferedWriter fw = new BufferedWriter(new FileWriter("/tmp/expected.txt"));
                fw.write(expected);
                fw.close();
                fw = new BufferedWriter(new FileWriter("/tmp/actual.txt"));
                fw.write(description);
                fw.close();
            }
        }

        String expectedTrimmed = expected.trim();
        String actualTrimmed = description.trim();

        if (expectedTrimmed.equals(actualTrimmed)) {
            return; // Actual and expected content are equals --> Test passed
        } else {
            // We want to ignore different line separators (like \r\n against \n) because they
            // might be causing failing tests on a different operation systems like Windows :]
            String expectedUnified = expectedTrimmed.replace("\r", "");
            String actualUnified = actualTrimmed.replace("\r", "");
            
            // if there is '**' in the actualUnified, it may stand for whatever word of the expected
            // content in that position.
            if (skipMarkers) {
                String[] linesExpected = expectedUnified.split("\n");
                String[] linesActual = actualUnified.split("\n");
                boolean allMatch = linesExpected.length == linesActual.length;
                for (int i = 0; allMatch && i < linesExpected.length; i++) {
                    String e = linesExpected[i];
                    String a = linesActual[i];
                    Pattern pattern = markerPattern(a);
                    allMatch = pattern == null ? a.equals(e) : pattern.matcher(e).matches();
                }
                if (allMatch) {
                    return;
                }
            }

            if (expectedUnified.equals(actualUnified)) {
                return; // Only difference is in line separation --> Test passed
            }

            // There are some diffrerences between expected and actual content --> Test failed
            fail(getContentDifferences(relFilePath, ext, includeTestName, expectedUnified, actualUnified, skipMarkers));
        }
    }
    
    private Pattern markerPattern(String line) {
        StringBuilder pattern = new StringBuilder();
        int start = 0;
        for (int idx = line.indexOf("*-*"); idx >= 0; start = idx + 3, idx = line.indexOf("*-*", start)) {
            pattern.append("\\s*");
            pattern.append(Pattern.quote(line.substring(start, idx).trim()));
            pattern.append("\\s*\\S*");
        }
        if (start > 0) {
            pattern.append("\\s*");
            pattern.append(Pattern.quote(line.substring(start).trim()));
            return Pattern.compile(pattern.toString());
        } else {
            return null;
        }
    }

    private String getContentDifferences(String relFilePath, String ext, boolean includeTestName, String expected, String actual, boolean skip) {
        StringBuilder sb = new StringBuilder();
        sb.append("Content does not match between '").append(relFilePath).append("' and '").append(relFilePath);
        if (includeTestName) {
            sb.append(getName());
        }
        sb.append(ext).append("'").append(lineSeparator(1));
        sb.append(getContentDifferences(expected, actual, skip));

        return sb.toString();
    }
    
    private boolean containsLine(List<String> lines, String line, boolean skipMarkers, boolean inArray) {
        if (lines.contains(line)) {
            return true;
        } else if (!skipMarkers) {
            return false;
        }
        if (inArray) {
            for (String l : lines) {
                Pattern toFind = markerPattern(l);
                if (toFind == null) {
                    if (l.equals(line)) {
                        return true;
                    }
                } else {
                    if (toFind.matcher(line).matches()) {
                        return true;
                    }
                }
            }
        } else {
            Pattern toFind = markerPattern(line);
            if (toFind == null) {
                return false;
            }
            for (String l : lines) {
                if (toFind.matcher(l).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getContentDifferences(String expected, String actual, boolean skipMarkers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Expected content is:").
           append(lineSeparator(2)).
           append(expected).
           append(lineSeparator(2)).
           append("but actual is:").
           append(lineSeparator(2)).
           append(actual).
           append(lineSeparator(2)).
           append("It differs in the following things:").
           append(lineSeparator(2));

        List<String> expectedLines = Arrays.asList(expected.split("\n"));
        List<String> actualLines = Arrays.asList(actual.split("\n"));

        if (expectedLines.size() != actualLines.size()) {
            sb.append("Number of lines: \n\tExpected: ").append(expectedLines.size()).append("\n\tActual: ").append(actualLines.size()).append("\n\n");
        }

        // Appending lines which are missing in expected content and are present in actual content
        boolean noErrorInActual = true;
        for (String actualLine : actualLines) {
            if (containsLine(expectedLines, actualLine, skipMarkers, false) == false) {
                if (noErrorInActual) {
                    sb.append("Actual content contains following lines which are missing in expected content: ").append(lineSeparator(1));
                    noErrorInActual = false;
                }
                sb.append("\t").append(actualLine).append(lineSeparator(1));
            }
        }

        // Appending lines which are missing in actual content and are present in expected content
        boolean noErrorInExpected = true;
        for (String expectedLine : expectedLines) {
            if (containsLine(actualLines, expectedLine, skipMarkers, true) == false) {
                // If at least one line missing in actual content we want to append header line
                if (noErrorInExpected) {
                    sb.append("Expected content contains following lines which are missing in actual content: ").append(lineSeparator(1));
                    noErrorInExpected = false;
                }
                sb.append("\t").append(expectedLine).append(lineSeparator(1));
            }
        }

        // If both values are true it means the content is the same, but some lines are
        // placed on a different line number in actual and expected content
        if (noErrorInActual && noErrorInExpected && expectedLines.size() == actualLines.size()) {
            for (int lineNumber = 0; lineNumber < expectedLines.size(); lineNumber++) {
                String expectedLine = expectedLines.get(lineNumber);
                String actualLine = actualLines.get(lineNumber);

                if (!expectedLine.equals(actualLine)) {
                    sb.append("Line ").
                        append(lineNumber).
                        append(" contains different content than expected: ").
                        append(lineSeparator(1)).
                        append("Expected: \t").
                        append(expectedLine).
                        append(lineSeparator(1)).
                        append("Actual:  \t").
                        append(actualLine).
                        append(lineSeparator(2));

                }
            }
        }

        return sb.toString();
    }

    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext) throws IOException {
            assertDescriptionMatches(fileObject, description, includeTestName, ext, false);
    }

    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {

        String goldenFileDir = goldenFileInTestFileDir ?
            FileUtil.getRelativePath(FileUtil.toFileObject(getDataDir()), fileObject.getParent()) :
            "testfiles";

        File goldenFile = getDataFile(goldenFileDir + "/" + fileObject.getNameExt() + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);

        // Because the unit test differ is so bad...
        if (false) { // disabled
            if (!expected.equals(description)) {
                BufferedWriter fw = new BufferedWriter(new FileWriter("/tmp/expected.txt"));
                fw.write(expected);
                fw.close();
                fw = new BufferedWriter(new FileWriter("/tmp/actual.txt"));
                fw.write(description);
                fw.close();
            }
        }

        final String expectedTrimmed = expected.trim();
        final String actualTrimmed = description.trim();

        if (expectedTrimmed.equals(actualTrimmed)) {
            return; // Actual and expected content are equals --> Test passed
        } else {
            // We want to ignore different line separators (like \r\n against \n) because they
            // might be causing failing tests on a different operation systems like Windows :]
            final String expectedUnified = expectedTrimmed.replace("\r", "");
            final String actualUnified = actualTrimmed.replace("\r", "");

            if (expectedUnified.equals(actualUnified)) {
                return; // Only difference is in line separation --> Test passed
            }

            // There are some diffrerences between expected and actual content --> Test failed

            fail("Not matching goldenfile: " + FileUtil.getFileDisplayName(fileObject) + lineSeparator(2) + getContentDifferences(expectedUnified, actualUnified, false));
        }
    }

    /**
     * Returns line separators with respect to the current platform.
     *
     * @param number use if you want to get more than one file separator
     * @return one or more independent line separators in one <code>String</code>
     */
    private String lineSeparator(int number) {
        final String lineSeparator = System.getProperty("line.separator");
        if (number > 1) {
            final StringBuilder sb = new StringBuilder();

            for (int i = 0; i < number; i++) {
                sb.append(lineSeparator);
            }
            return sb.toString();
        }
        return lineSeparator;
    }

    protected void assertFileContentsMatches(String relFilePath, String description, boolean includeTestName, String ext) throws Exception {
        File rubyFile = getDataFile(relFilePath);
        if (!rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        File goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);
        final String expectedTrimmed = expected.trim();
        final String actualTrimmed = description.trim();

        if (expectedTrimmed.equals(actualTrimmed)) {
            return; // Actual and expected content are equals --> Test passed
        } else {
            // We want to ignore different line separators (like \r\n against \n) because they
            // might be causing failing tests on a different operation systems like Windows :]
            final String expectedUnified = expectedTrimmed.replace("\r", "");
            final String actualUnified = actualTrimmed.replace("\r", "");

            if (expectedUnified.equals(actualUnified)) {
                return; // Only difference is in line separation --> Test passed
            }

            // There are some diffrerences between expected and actual content --> Test failed

            fail("Not matching goldenfile: " + FileUtil.getFileDisplayName(FileUtil.toFileObject(goldenFile)) + lineSeparator(2) + getContentDifferences(expectedUnified, actualUnified, false));
        }
    }

    public void assertEquals(Collection<String> s1, Collection<String> s2) {
        List<String> l1 = new ArrayList<String>();
        l1.addAll(s1);
        Collections.sort(l1);
        List<String> l2 = new ArrayList<String>();
        l2.addAll(s2);
        Collections.sort(l2);

        assertEquals(l1.toString(), l2.toString());
    }

    protected void createFilesFromDesc(FileObject folder, String descFile) throws Exception {
        File taskFile = new File(getDataDir(), descFile);
        assertTrue(taskFile.exists());
        BufferedReader br = new BufferedReader(new FileReader(taskFile));
        while (true) {
            String line = br.readLine();
            if (line == null || line.trim().length() == 0) {
                break;
            }

            if (line.endsWith("\r")) {
                line = line.substring(0, line.length()-1);
            }

            String path = line;
            if (path.endsWith("/")) {
                path = path.substring(0, path.length()-1);
                FileObject f = FileUtil.createFolder(folder, path);
                assertNotNull(f);
            } else {
                FileObject f = FileUtil.createData(folder, path);
                assertNotNull(f);
            }
        }
    }

   public static void createFiles(File baseDir, String... paths) throws IOException {
        assertNotNull(baseDir);
        for (String path : paths) {
            FileObject baseDirFO = FileUtil.toFileObject(baseDir);
            assertNotNull(baseDirFO);
            assertNotNull(FileUtil.createData(baseDirFO, path));
        }
    }

    public static void createFile(FileObject dir, String relative, String contents) throws IOException {
        FileObject datafile = FileUtil.createData(dir, relative);
        OutputStream os = datafile.getOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(contents);
        writer.close();
    }


    ////////////////////////////////////////////////////////////////////////////
    // Parsing Info Based Tests
    ////////////////////////////////////////////////////////////////////////////
    protected Parser getParser() {
        Parser parser = getPreferredLanguage().getParser();
        assertNotNull("You must override getParser(), either from your GsfLanguage or your test class", parser);
        return parser;
    }

    protected void validateParserResult(@NullAllowed ParserResult result) {
        // Clients can do checks to make sure everything is okay here.
    }

    protected DefaultLanguageConfig getPreferredLanguage() {
        return null;
    }

    protected String getPreferredMimeType() {
        return null;
    }

//    public FileObject createFileWithText(String text) throws IOException {
//        FileObject workDir = FileUtil.toFileObject(getWorkDir());
//
//        String name = getName() + System.currentTimeMillis();
//        FileObject file = workDir.getFileObject(name);
//        if (file != null) {
//            file.delete();
//        }
//        file = workDir.createData(name);
//        return copyStringToFileObject(file, text);
//    }

    ////////////////////////////////////////////////////////////////////////////
    // Parser tests
    ////////////////////////////////////////////////////////////////////////////
    protected void checkErrors(final String relFilePath) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertNotNull(r);
                assertTrue(r instanceof ParserResult);

                ParserResult pr = (ParserResult) r;
                List<? extends Error> diagnostics = pr.getDiagnostics();
                String annotatedSource = annotateErrors(diagnostics);
                assertDescriptionMatches(relFilePath, annotatedSource, false, ".errors");
            }
        });
    }

    protected String annotateErrors(List<? extends Error> errors) {
        List<String> descs = new ArrayList<String>();
        for (Error error : errors) {
            StringBuilder desc = new StringBuilder();
            if (error.getKey() != null) {
                desc.append("[");
                desc.append(error.getKey());
                desc.append("] ");
            }
            desc.append(error.getStartPosition());
            desc.append("-");
            desc.append(error.getEndPosition());
            desc.append(":");
            desc.append(error.getDisplayName());
            if (error.getDescription() != null) {
                desc.append(" ; " );
                desc.append(error.getDescription());
            }
            descs.add(desc.toString());
        }
        Collections.sort(descs);
        StringBuilder summary = new StringBuilder();
        for (String desc : descs) {
            summary.append(desc);
            summary.append("\n");
        }

        return summary.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Keystroke completion tests
    ////////////////////////////////////////////////////////////////////////////
    protected KeystrokeHandler getKeystrokeHandler() {
        KeystrokeHandler handler = getPreferredLanguage().getKeystrokeHandler();
        assertNotNull("You must override getKeystrokeHandler, either from your GsfLanguage or your test class", handler);
        return handler;
    }

    // Also requires getFormatter(IndentPref) defined below under the formatting tests

    protected void assertMatches(String original) throws BadLocationException {
        KeystrokeHandler bc = getKeystrokeHandler();
        int caretPos = original.indexOf('^');

        original = original.substring(0, caretPos) + original.substring(caretPos+1);
        int matchingCaretPos = original.indexOf('^');
        assertTrue(caretPos < matchingCaretPos);
        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos+1);

        BaseDocument doc = getDocument(original);

        OffsetRange range = bc.findMatching(doc, caretPos);

        assertNotSame("Didn't find matching token for " + /*LexUtilities.getToken(doc, caretPos).text().toString()*/ " position " + caretPos,
                OffsetRange.NONE, range);
        assertEquals("forward match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " +
                /*LexUtilities.getToken(doc, matchingCaretPos).text().toString()*/ " position " + matchingCaretPos,
                matchingCaretPos, range.getStart());

        // Perform reverse match
        range = bc.findMatching(doc, matchingCaretPos);

        assertNotSame(OffsetRange.NONE, range);
        assertEquals("reverse match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " +
                /*LexUtilities.getToken(doc, caretPos).text().toString()*/ " position " + caretPos,
                caretPos, range.getStart());
    }

    protected void assertMatches2(String original) throws BadLocationException {
        BracesMatcherFactory factory = MimeLookup.getLookup(getPreferredMimeType()).lookup(BracesMatcherFactory.class);
        int caretPos = original.indexOf('^');
        original = original.substring(0, caretPos) + original.substring(caretPos+1);

        int matchingCaretPos = original.indexOf('^');

        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos+1);

        BaseDocument doc = getDocument(original);

        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, caretPos, false, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int [] origin = null, matches = null;
        try {
            origin = matcher.findOrigin();
            matches = matcher.findMatches();
        } catch (InterruptedException ex) {
        }

        assertNotNull("Did not find origin for " + " position " + caretPos, origin);
        assertNotNull("Did not find matches for " + " position " + caretPos, matches);

        assertEquals("Incorrect origin", caretPos, origin[0]);
        assertEquals("Incorrect matches", matchingCaretPos, matches[0]);

        //Reverse direction
        context = BracesMatchingTestUtils.createMatcherContext(doc, matchingCaretPos, false, 1);
        matcher = factory.createMatcher(context);
        try {
            origin = matcher.findOrigin();
            matches = matcher.findMatches();
        } catch (InterruptedException ex) {
        }

        assertNotNull("Did not find origin for " + " position " + caretPos, origin);
        assertNotNull("Did not find matches for " + " position " + caretPos, matches);

        assertEquals("Incorrect origin", matchingCaretPos, origin[0]);
        assertEquals("Incorrect matches", caretPos, matches[0]);
    }

    // Copied from LexUtilities
    public static int getLineIndent(BaseDocument doc, int offset) {
        try {
            int start = Utilities.getRowStart(doc, offset);
            int end;

            if (Utilities.isRowWhite(doc, start)) {
                end = Utilities.getRowEnd(doc, offset);
            } else {
                end = Utilities.getRowFirstNonWhite(doc, start);
            }

            int indent = Utilities.getVisualColumn(doc, end);

            return indent;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode) throws Exception {
        String source = original;
        String reformatted = expected;
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertTrue("Source text must have a caret ^ marker", sourcePos != -1);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertTrue("Reformatted text must have a caret ^ marker", reformattedPos != -1);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        if (selection != null) {
            int start = source.indexOf(selection);
            assertTrue(start != -1);
            assertTrue("Ambiguous selection - multiple occurrences of selection string",
                    source.indexOf(selection, start+1) == -1);
            ta.setSelectionStart(start);
            ta.setSelectionEnd(start+selection.length());
            assertEquals(selection, ta.getSelectedText());
        }

        BaseDocument doc = (BaseDocument) ta.getDocument();

        if (codeTemplateMode) {
            // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
            String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N
            doc.putProperty(EDITING_TEMPLATE_DOC_PROPERTY, Boolean.TRUE);
        }

        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, null);

        runKitAction(ta, DefaultEditorKit.defaultKeyTypedAction, ""+insertText);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    protected void deleteChar(String original, String expected) throws Exception {
        String source = original;
        String reformatted = expected;
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertTrue("Source text must have a caret ^ marker", sourcePos != -1);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertTrue("Reformatted text must have a caret ^ marker", reformattedPos != -1);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();

        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, null);

        runKitAction(ta, DefaultEditorKit.deletePrevCharAction, "\n");

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    protected void deleteWord(String original, String expected) throws Exception {
        String source = original;
        String reformatted = expected;
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertTrue("Source text must have a caret ^ marker", sourcePos != -1);

        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertTrue("Reformatted text must have a caret ^ marker", reformattedPos != -1);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, null);

        runKitAction(ta, BaseKit.removePreviousWordAction, "\n");

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }

    }

    protected void assertLogicalRange(String sourceText, boolean up, String expected) throws Exception {
        String BEGIN = "%<%"; // NOI18N
        String END = "%>%"; // NOI18N
        final int sourceStartPos = sourceText.indexOf(BEGIN);
        if (sourceStartPos != -1) {
            sourceText = sourceText.substring(0, sourceStartPos) + sourceText.substring(sourceStartPos+BEGIN.length());
        }

        final int caretPos = sourceText.indexOf('^');
        sourceText = sourceText.substring(0, caretPos) + sourceText.substring(caretPos+1);

        final int sourceEndPos = sourceText.indexOf(END);
        if (sourceEndPos != -1) {
            sourceText = sourceText.substring(0, sourceEndPos) + sourceText.substring(sourceEndPos+END.length());
        }

        final int expectedStartPos = expected.indexOf(BEGIN);
        if (expectedStartPos != -1) {
            expected = expected.substring(0, expectedStartPos) + expected.substring(expectedStartPos+BEGIN.length());
        }

        final int expectedCaretPos = expected.indexOf('^');
        expected = expected.substring(0, expectedCaretPos) + expected.substring(expectedCaretPos+1);

        final int expectedEndPos = expected.indexOf(END);
        if (expectedEndPos != -1) {
            expected = expected.substring(0, expectedEndPos) + expected.substring(expectedEndPos+END.length());
        }

        assertEquals("Only range markers should differ", sourceText, expected);

        Document doc = getDocument(sourceText);
        Source testSource = Source.create(doc);

        final String finalSourceText = sourceText;
        final boolean finalUp = up;
        final String finalExpected = expected;

        enforceCaretOffset(testSource, caretPos);
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue("Expecting ParserResult, but got " + r, r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                KeystrokeHandler completer = getKeystrokeHandler();
                assertNotNull("getKeystrokeHandler() must be implemented!", completer);

                List<OffsetRange> ranges = completer.findLogicalRanges(pr, caretPos);
                OffsetRange expectedRange;
                if (expectedStartPos != -1) {
                    expectedRange = new OffsetRange(expectedStartPos, expectedEndPos);
                } else {
                    expectedRange = new OffsetRange(expectedCaretPos, expectedCaretPos);
                }

                if (sourceStartPos != -1) {
                    assertTrue(sourceEndPos != -1);
                    OffsetRange selected = new OffsetRange(sourceStartPos, sourceEndPos);

                    for (int i = 0; i < ranges.size(); i++) {
                        if (ranges.get(i).equals(selected)) {
                            if (finalUp) {
                                assertTrue(i < ranges.size()-1);
                                OffsetRange was = ranges.get(i+1);
                                assertEquals("Wrong selection: expected \"" +
                                        finalExpected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                        finalSourceText.substring(was.getStart(), was.getEnd()) + "\"",
                                        expectedRange, was);
                                return;
                            } else {
                                if (i == 0) {
                                    assertEquals(caretPos, expectedCaretPos);
                                    return;
                                }
                                OffsetRange was = ranges.get(i-1);
                                assertEquals("Wrong selection: expected \"" +
                                        finalExpected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                        finalSourceText.substring(was.getStart(), was.getEnd()) + "\"",
                                        expectedRange, was);
                                return;
                            }
                        }
                    }
                    fail("Selection range " + selected + " is not in the range; ranges=" + ranges);
                } else {
                    assertTrue(ranges.size() > 0);
                    OffsetRange was = ranges.get(0);
                    assertEquals("Wrong selection: expected \"" +
                            finalExpected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                            finalSourceText.substring(was.getStart(), was.getEnd()) + "\"",
                            expectedRange, was);
                }
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    // Mark Occurrences Tests
    ////////////////////////////////////////////////////////////////////////////
    protected OccurrencesFinder getOccurrencesFinder() {
        OccurrencesFinder handler = getPreferredLanguage().getOccurrencesFinder();
        assertNotNull("You must override getOccurrencesFinder, either from your GsfLanguage or your test class", handler);
        return handler;
    }

    /** Test the occurrences to make sure they equal the golden file.
     * If the symmetric parameter is set, this test will also ensure that asking for
     * occurrences on ANY of the matches produced by the original caret position will
     * produce the exact same map. This is obviously not appropriate for things like
     * occurrences on the exit points.
     */
    protected void checkOccurrences(String relFilePath, String caretLine, final boolean symmetric) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        Document doc = testSource.getDocument(true);
        final int caretOffset = getCaretOffset(doc.getText(0, doc.getLength()), caretLine);

        final OccurrencesFinder finder = getOccurrencesFinder();
        assertNotNull("getOccurrencesFinder must be implemented", finder);
        finder.setCaretPosition(caretOffset);

        UserTask task = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult(caretOffset);
                if (r instanceof ParserResult) {
                    finder.run((ParserResult) r, null);
                    Map<OffsetRange, ColoringAttributes> occurrences = finder.getOccurrences();
                    if (occurrences == null) {
                        occurrences = Collections.emptyMap();
                    }

                    String annotatedSource = annotateFinderResult(resultIterator.getSnapshot(), occurrences, caretOffset);
                    assertDescriptionMatches(resultIterator.getSnapshot().getSource().getFileObject(), annotatedSource, true, ".occurrences");

                    if (symmetric) {
                        // Extra check: Ensure that occurrences are symmetric: Placing the caret on ANY of the occurrences
                        // should produce the same set!!
                        for (OffsetRange range : occurrences.keySet()) {
                            int midPoint = range.getStart() + range.getLength() / 2;
                            finder.setCaretPosition(midPoint);
                            finder.run((ParserResult) r, null);
                            Map<OffsetRange, ColoringAttributes> alternates = finder.getOccurrences();
                            assertEquals("Marks differ between caret positions - failed at " + midPoint, occurrences, alternates);
                        }
                    }
                }
            }
        };
        if (classPathsForTest == null || classPathsForTest.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
        }
    }

    protected String annotateFinderResult(Snapshot snapshot, Map<OffsetRange, ColoringAttributes> highlights, int caretOffset) throws Exception {
        Set<OffsetRange> ranges = highlights.keySet();
        StringBuilder sb = new StringBuilder();
        CharSequence text = snapshot.getText();
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = findRowStart(text, index);
            int lineEnd = findRowEnd(text, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                for (int i = lineStart; i <= lineEnd; i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
                    if (starts.containsKey(i)) {
                        sb.append("|>");
                        OffsetRange range = starts.get(i);
                        ColoringAttributes ca = highlights.get(range);
                        if (ca != null) {
                            sb.append(ca.name());
                            sb.append(':');
                        }
                    }
                    if (ends.containsKey(i)) {
                        sb.append("<|");
                    }
                    if (i < length) {
                        sb.append(text.charAt(i));
                    }
                }
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }

    private static int findRowStart(CharSequence text, int startOffset) {
        for(int i = startOffset - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                return i + 1;
            }
        }
        return 0;
    }

    private static int findRowEnd(CharSequence text, int startOffset) {
        for(int i = startOffset; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                return i;
            }
        }
        return text.length();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Semantic Highlighting Tests
    ////////////////////////////////////////////////////////////////////////////
    protected SemanticAnalyzer getSemanticAnalyzer() {
        SemanticAnalyzer handler = getPreferredLanguage().getSemanticAnalyzer();
        assertNotNull("You must override getSemanticAnalyzer, either from your GsfLanguage or your test class", handler);
        return handler;
    }

    protected void checkSemantic(final String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        if (caretLine != null) {
            int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        }

        UserTask task = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                SemanticAnalyzer analyzer = getSemanticAnalyzer();
                assertNotNull("getSemanticAnalyzer must be implemented", analyzer);

                analyzer.run(pr, null);
                Map<OffsetRange, Set<ColoringAttributes>> highlights = analyzer.getHighlights();

                if (highlights == null) {
                    highlights = Collections.emptyMap();
                }

                Document doc = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                checkNoOverlaps(highlights.keySet(), doc);

                String annotatedSource = annotateSemanticResults(doc, highlights);
                assertDescriptionMatches(relFilePath, annotatedSource, false, ".semantic");
            }
        };

        if (classPathsForTest == null || classPathsForTest.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
        }

    }

    protected void checkNoOverlaps(Set<OffsetRange> ranges, Document doc) throws BadLocationException {
        // Make sure there are no overlapping ranges
        List<OffsetRange> sortedRanges = new ArrayList<>(ranges);
        Collections.sort(sortedRanges);
        for (int i = 0; i < sortedRanges.size(); i++) {
            OffsetRange prevRange = sortedRanges.get(i);
            for (int j = i + 1; j < sortedRanges.size(); j++) {
                OffsetRange targetRange = sortedRanges.get(j);
                if (prevRange.overlaps(targetRange)) {
                    fail("OffsetRanges should be non-overlapping! " + prevRange
                            + "(" + doc.getText(prevRange.getStart(), prevRange.getLength()) + ") and " + targetRange
                            + "(" + doc.getText(targetRange.getStart(), targetRange.getLength()) + ")");
                }
            }
        }
    }

    protected String annotateSemanticResults(Document doc, Map<OffsetRange, Set<ColoringAttributes>> highlights) throws Exception {
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : highlights.keySet()) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
                OffsetRange range = starts.get(i);
                Set<ColoringAttributes> cas = highlights.get(range);
                if (cas != null) {
                    // Sort to ensure stable unit test golden files
                    List<String> attrs = new ArrayList<String>(cas.size());
                    for (ColoringAttributes c : cas) {
                        attrs.add(c.name());
                    }
                    Collections.sort(attrs);
                    boolean first = true;
                    for (String name : attrs) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(",");
                        }
                        sb.append(name);
                    }
                    sb.append(':');
                }
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }

    protected void checkSemantic(String relFilePath) throws Exception {
        checkSemantic(relFilePath, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Rename Handling Tests
    ////////////////////////////////////////////////////////////////////////////
    protected InstantRenamer getRenameHandler() {
        InstantRenamer handler = getPreferredLanguage().getInstantRenamer();
        assertNotNull("You must override getRenameHandler, either from your GsfLanguage's getInstantRenamer or your test class", handler);
        return handler;
    }

    protected void checkRenameSections(final String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                InstantRenamer handler = getRenameHandler();
                assertNotNull("getRenameHandler must be implemented", handler);

                String annotatedSource;
                String[] desc = new String[1];
                if (handler.isRenameAllowed(pr, caretOffset, desc)) {
                    Set<OffsetRange> renameRegions = handler.getRenameRegions(pr, caretOffset);
                    annotatedSource = annotateRenameRegions(GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true), renameRegions);
                } else {
                    annotatedSource = "Refactoring not allowed here\n";
                    if (desc[0] != null) {
                        annotatedSource += desc[0] + "\n";
                    }
                }

                assertDescriptionMatches(relFilePath, annotatedSource, true, ".rename");
            }
        });
    }

    private String annotateRenameRegions(Document doc, Set<OffsetRange> ranges) throws Exception {
        if (ranges.size() == 0) {
            return "Requires Interactive Refactoring\n";
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }
        // Only print lines with result
        String[] lines = sb.toString().split("\n");
        sb = new StringBuilder();
        int lineno = 1;
        for (String line : lines) {
            if (line.indexOf("|>") != -1) {
                sb.append(Integer.toString(lineno));
                sb.append(": ");
                sb.append(line);
                sb.append("\n");
            }
            lineno++;
        }

        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Indexing Tests
    ////////////////////////////////////////////////////////////////////////////
    public EmbeddingIndexerFactory getIndexerFactory() {
        EmbeddingIndexerFactory handler = getPreferredLanguage().getIndexerFactory();
        assertNotNull("You must override getIndexerFactory, either from your GsfLanguage or your test class", handler);
        return handler;
    }

    private List<TestIndexDocumentImpl> _indexFile(String relFilePath) throws Exception {
        FileObject testSourceFile = getTestFile(relFilePath);
        Source testSource = getTestSource(testSourceFile);

        FileObject root = testSourceFile.getParent();
        final Indexable indexable = SPIAccessor.getInstance().create(new FileObjectIndexable(root, testSourceFile));
        final EmbeddingIndexerFactory factory = getIndexerFactory();
        assertNotNull("getIndexer must be implemented", factory);
        FileObject cacheRoot = CacheFolder.getDataFolder(root.getURL());

        TestIndexFactoryImpl tifi = new TestIndexFactoryImpl();
        final Context context = SPIAccessor.getInstance().createContext(
                cacheRoot,
                root.getURL(),
                factory.getIndexerName(),
                factory.getIndexVersion(),
                tifi,
                false,
                false,
                false,
                SuspendSupport.NOP,
                null,
                null
        );

        try {
            class UT extends UserTask implements IndexingTask {
                public @Override void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result r = resultIterator.getParserResult();
                    assertTrue(r instanceof ParserResult);

                    EmbeddingIndexer indexer = factory.createIndexer(indexable, r.getSnapshot());
                    assertNotNull("getIndexer must be implemented", factory);

                    SPIAccessor.getInstance().index(indexer, indexable, r, context);
                }
            }
            ParserManager.parse(Collections.singleton(testSource), new UT());
        } finally {
            DocumentIndex index = SPIAccessor.getInstance().getIndexFactory(context).getIndex(context.getIndexFolder());
            if (index != null) {
                index.removeDirtyKeys(Collections.singleton(indexable.getRelativePath()));
                index.store(true);
            }
        }

        TestIndexImpl tii = tifi.getTestIndex(context.getIndexFolder());
        if (tii != null) {
            List<TestIndexDocumentImpl> list = tii.documents.get(indexable.getRelativePath());
            if (list != null) {
                return list;
            }
        }

        return Collections.<TestIndexDocumentImpl>emptyList();
    }

    protected void indexFile(String relFilePath) throws Exception {
        _indexFile(relFilePath);
    }

    protected void checkIndexer(String relFilePath) throws Exception {
        File jsFile = new File(getDataDir(), relFilePath);
        String fileUrl = jsFile.toURI().toURL().toExternalForm();
        String localUrl = fileUrl;
        int index = localUrl.lastIndexOf('/');
        if (index != -1) {
            localUrl = localUrl.substring(0, index);
        }

        List<TestIndexDocumentImpl> result = _indexFile(relFilePath);
        String annotatedSource = result == null ? "" : prettyPrint(result, localUrl);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".indexed");
    }

    protected void checkIsIndexable(String relFilePath, boolean isIndexable) throws Exception {
        final EmbeddingIndexerFactory factory = getIndexerFactory();
        assertNotNull("getIndexerFactory must be implemented", factory);
        final FileObject fo = getTestFile(relFilePath);
        assertNotNull(fo);

        final Boolean result [] = new Boolean [] { null };
        Source testSource = getTestSource(fo);
        class UT extends UserTask implements IndexingTask {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                EmbeddingIndexer indexer = factory.createIndexer(
                    SPIAccessor.getInstance().create(new FileObjectIndexable(fo.getParent(), fo)),
                    r.getSnapshot());
                result[0] = Boolean.valueOf(indexer != null);
            }
        }
        ParserManager.parse(Collections.singleton(testSource), new UT());

        assertNotNull(result[0]);
        assertEquals(isIndexable, result[0].booleanValue());
    }

    private String sortCommaList(String s) {
        String[] items = s.split(",");
        Arrays.sort(items);
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(item);
        }

        return sb.toString();
    }

    protected String prettyPrintValue(String key, String value) {
        return value;
    }

    private String prettyPrint(List<TestIndexDocumentImpl> documents, String localUrl) throws IOException {
        List<String> nonEmptyDocuments = new ArrayList<String>();
        List<String> emptyDocuments = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();

        for (TestIndexDocumentImpl doc : documents) {

            sb = new StringBuilder();

            sb.append("Searchable Keys:");
            sb.append("\n");
            List<String> strings = new ArrayList<String>();

            List<String> keys = doc.indexedKeys;
            List<String> values = doc.indexedValues;
            for (int i = 0, n = keys.size(); i < n; i++) {
                String key = keys.get(i);
                String value = values.get(i);
                strings.add(key + " : " + prettyPrintValue(key, value));
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            sb.append("\n");
            sb.append("Not Searchable Keys:");
            sb.append("\n");
            strings = new ArrayList<String>();
            keys = doc.unindexedKeys;
            values = doc.unindexedValues;
            for (int i = 0, n = keys.size(); i < n; i++) {
                String key = keys.get(i);
                String value = prettyPrintValue(key, values.get(i));
                if (value.indexOf(',') != -1) {
                    value = sortCommaList(value);
                }
                strings.add(key + " : " + value);
            }

            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            String s = sb.toString();
            if (doc.indexedKeys.size() == 0 && doc.unindexedKeys.size() == 0) {
                emptyDocuments.add(s);
            } else {
                nonEmptyDocuments.add(s);
            }
        }

        Collections.sort(emptyDocuments);
        Collections.sort(nonEmptyDocuments);
        sb = new StringBuilder();
        int documentNumber = 0;
        for (String s : emptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }

        for (String s : nonEmptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }


        return sb.toString().replace(localUrl, "<TESTURL>");
    }

//    public class IndexDocumentImpl extends IndexDocument {
//        public List<String> indexedKeys = new ArrayList<String>();
//        public List<String> indexedValues = new ArrayList<String>();
//        public List<String> unindexedKeys = new ArrayList<String>();
//        public List<String> unindexedValues = new ArrayList<String>();
//
//        public String overrideUrl;
//
//        private IndexDocumentImpl(String overrideUrl) {
//            this.overrideUrl = overrideUrl;
//        }
//
//        public void addPair(String key, String value, boolean indexed) {
//            if (indexed) {
//                indexedKeys.add(key);
//                indexedValues.add(value);
//            } else {
//                unindexedKeys.add(key);
//                unindexedValues.add(value);
//            }
//        }
//    }
//
    ////////////////////////////////////////////////////////////////////////////
    // Structure Analyzer Tests
    ////////////////////////////////////////////////////////////////////////////
    public StructureScanner getStructureScanner() {
        StructureScanner handler = getPreferredLanguage().getStructureScanner();
        assertNotNull("You must override getStructureScanner, either from your GsfLanguage or your test class", handler);
        return handler;
    }

    protected void checkStructure(String relFilePath) throws Exception {
        checkStructure(relFilePath, false, false, false);
    }

    protected void checkStructure(String relFilePath, final boolean embedded,
            final boolean inTestDir, final boolean includePositions) throws Exception {

        final HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();

            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text, int fromInclusive, int toExclusive) {
                sb.append("ESCAPED{");
                sb.append(text, fromInclusive, toExclusive);
                sb.append("}");
            }

            @Override
            public void name(ElementKind kind, boolean start) {
                if (start) {
                    sb.append(kind);
                }
            }

            @Override
            public void active(boolean start) {
                if (start) {
                    sb.append("ACTIVE{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void parameters(boolean start) {
                if (start) {
                    sb.append("PARAMETERS{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void type(boolean start) {
                if (start) {
                    sb.append("TYPE{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void deprecated(boolean start) {
                if (start) {
                    sb.append("DEPRECATED{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public String getText() {
                return sb.toString();
            }

            @Override
            public void emphasis(boolean start) {
            }
        };

        Source testSource = getTestSource(getTestFile(relFilePath));

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                StructureScanner analyzer = getStructureScanner();
                assertNotNull("getStructureScanner must be implemented", analyzer);

                Parser.Result r = null;
                if (embedded) {
                    for (Embedding e : resultIterator.getEmbeddings()) {
                        if (e.getMimeType().equals(getPreferredMimeType())) {
                            r = resultIterator.getResultIterator(e).getParserResult();
                            break;
                        }
                    }
                } else {
                    r = resultIterator.getParserResult();
                }
                assertTrue(r instanceof ParserResult);
                List<? extends StructureItem> structure = analyzer.scan((ParserResult) r);

                String annotatedSource = annotateStructure(structure, formatter, includePositions);
                assertDescriptionMatches(resultIterator.getSnapshot().getSource().getFileObject(), annotatedSource,
                        false, ".structure", inTestDir);
            }
        });
    }

    protected void checkFolds(String relFilePath) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {

                StructureScanner analyzer = getStructureScanner();
                assertNotNull("getStructureScanner must be implemented", analyzer);

                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                Map<String,List<OffsetRange>> foldsMap = analyzer.folds((ParserResult) r);

                // Write folding structure
                String source = resultIterator.getSnapshot().getText().toString();
                List<Integer> begins = new ArrayList<Integer>();
                List<Integer> ends = new ArrayList<Integer>();

                begins.add(0);

                for (int i = 0; i < source.length(); i++) {
                    char c = source.charAt(i);
                    if (c == '\n') {
                        ends.add(i);
                        if (i < source.length()) {
                            begins.add(i+1);
                        }
                    }
                }

                ends.add(source.length());

                assertEquals(begins.size(), ends.size());
                List<Character> margin = new ArrayList<Character>(begins.size());
                for (int i = 0; i < begins.size(); i++) {
                    margin.add(' ');
                }

                List<String> typeList = new ArrayList<String>(foldsMap.keySet());
                Collections.sort(typeList);
                for (String type : typeList) {
                    List<OffsetRange> ranges = foldsMap.get(type);
                    for (OffsetRange range : ranges) {
                        int beginIndex = Collections.binarySearch(begins, range.getStart());
                        if (beginIndex < 0) {
                            beginIndex = -(beginIndex+2);
                        }
                        int endIndex = Collections.binarySearch(ends, range.getEnd());
                        if (endIndex < 0) {
                            endIndex = -(endIndex+1);
                        }
                        for (int i = beginIndex; i <= endIndex; i++) {
                            char c = margin.get(i);
                            if (i == beginIndex) {
                                c = '+';
                            } else if (c != '+' && c != '-') {
                                if (i == endIndex) {
                                    c = '-';
                                } else {
                                    c = '|';
                                }
                            }
                            margin.set(i, c);
                        }
                    }
                }

                StringBuilder sb = new StringBuilder(3000);
                for (int i = 0; i < begins.size(); i++) {
                    sb.append(margin.get(i));
                    sb.append(' ');
                    for (int j = begins.get(i), max = ends.get(i); j < max; j++) {
                        sb.append(source.charAt(j));
                    }
                    sb.append('\n');
                }
                String annotatedSource = sb.toString();

                assertDescriptionMatches(resultIterator.getSnapshot().getSource().getFileObject(), annotatedSource, false, ".folds");
            }
        });
    }

    private void annotateStructureItem(int indent, StringBuilder sb, List<? extends StructureItem> structure,
            HtmlFormatter formatter, boolean includePositions) {

        for (StructureItem element : structure) {
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append(element.getName());
            sb.append(":");
            sb.append(element.getKind());
            sb.append(":");
            sb.append(element.getModifiers());
            sb.append(":");
            formatter.reset();
            sb.append(element.getHtml(formatter));
            sb.append(":");
            if (includePositions) {
                sb.append(element.getPosition());
                sb.append(",");
                sb.append(element.getEndPosition());
                sb.append(":");
            }
            sb.append("\n");
            List<? extends StructureItem> children = element.getNestedItems();
            if (children != null && children.size() > 0) {
                List<? extends StructureItem> c = new ArrayList<StructureItem>(children);
                // Sort children to make tests more stable
                c.sort(new Comparator<StructureItem>() {
                    public int compare(StructureItem s1, StructureItem s2) {
                        String s1Name = s1.getName();
                        String s2Name = s2.getName();
                        if (s1Name == null || s2Name == null) {
                            if (s1Name == (Object)s2Name) { // Object Cast: avoid String==String semantic warning
                                return 0;
                            } else if (s1Name == null) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else {
                            return s1Name.compareTo(s2Name);
                        }
                    }

                });

                annotateStructureItem(indent+1, sb, c, formatter, includePositions);
            }
        }
    }

    private String annotateStructure(List<? extends StructureItem> structure, HtmlFormatter formatter,
            boolean includePositions) {
        StringBuilder sb = new StringBuilder();
        annotateStructureItem(0, sb, structure, formatter, includePositions);

        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Formatting Tests
    ////////////////////////////////////////////////////////////////////////////
    protected Formatter getFormatter(IndentPrefs preferences) {
        Formatter formatter = getPreferredLanguage().getFormatter();
        assertNotNull("You must override getFormatter, either from your GsfLanguage or your test class", formatter);
        return formatter;
    }

    public class IndentPrefs {

        private final int hanging;

        private final int indent;

        public IndentPrefs(int indent, int hanging) {
            super();
            this.indent = indent;
            this.hanging = hanging;
        }

        public int getIndentation() {
            return indent;
        }

        public int getHangingIndentation() {
            return hanging;
        }
    }

    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly) {
        configureIndenters(document, formatter, indentOnly, getPreferredMimeType());
    }

    protected void configureIndenters(Document document, Formatter formatter, boolean indentOnly, String mimeType) {
//        ReformatTask.Factory reformatFactory = new ReformatTask.Factory() {
//            public ReformatTask createTask(Context context) {
//                final Context ctx = context;
//                return new ReformatTask() {
//                    public void reformat() throws BadLocationException {
//                        if (formatter != null) {
//                            formatter.reformat(ctx, compilationInfo);
//                        }
//                    }
//
//                    public ExtraLock reformatLock() {
//                        return null;
//                    }
//                };
//            }
//        };
//        IndentTask.Factory indentFactory = new IndentTask.Factory() {
//            public IndentTask createTask(Context context) {
//                final Context ctx = context;
//                return new IndentTask() {
//                    public void reindent() throws BadLocationException {
//                        if (formatter != null) {
//                            formatter.reindent(ctx);
//                        }
//                    }
//
//                    public ExtraLock indentLock() {
//                        return null;
//                    }
//                };
//            }
//
//        };

        MockServices.setServices(MockMimeLookup.class);
        if (indentOnly) {
            MockMimeLookup.setInstances(MimePath.parse(mimeType), new GsfIndentTaskFactory());
        } else {
            MockMimeLookup.setInstances(MimePath.parse(mimeType), new GsfReformatTaskFactory(), new GsfIndentTaskFactory());
        }
    }

    protected void format(Document document, Formatter formatter, int startPos, int endPos, boolean indentOnly) throws BadLocationException {
        //assertTrue(SwingUtilities.isEventDispatchThread());
        configureIndenters(document, formatter, indentOnly);

        final Reformat f = Reformat.get(document);
        f.lock();
        try {
            if (document instanceof BaseDocument) {
                ((BaseDocument) document).atomicLock();
            }
            try {
                f.reformat(Math.min(document.getLength(), startPos), Math.min(document.getLength(), endPos));
            } finally {
                if (document instanceof BaseDocument) {
                    ((BaseDocument) document).atomicUnlock();
                }
            }
        } finally {
            f.unlock();
        }
    }

    public void format(String sourceText, String reformatted, IndentPrefs preferences) throws Exception {
        final Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        String BEGIN = "%<%"; // NOI18N
        int startPos = sourceText.indexOf(BEGIN);
        if (startPos != -1) {
            sourceText = sourceText.substring(0, startPos) + sourceText.substring(startPos+BEGIN.length());
        } else {
            startPos = 0;
        }

        String END = "%>%"; // NOI18N
        int endPos = sourceText.indexOf(END);
        if (endPos != -1) {
            sourceText = sourceText.substring(0, endPos) + sourceText.substring(endPos+END.length());
        }

        Document doc = getDocument(sourceText);

        if (endPos == -1) {
            endPos = doc.getLength();
        }

        setupDocumentIndentation(doc, preferences);
        format(doc, formatter, startPos, endPos, false);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
    }


    protected void reformatFileContents(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        //String before = doc.getText(0, doc.getLength());

        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);
        format(doc, formatter, 0, doc.getLength(), false);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }

    protected BaseKit getEditorKit(String mimeType) {
        org.netbeans.modules.csl.core.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        assertNotNull(language);
        if (!language.useCustomEditorKit()) {
            return new CslEditorKit(mimeType);
        }
        fail("Must override getEditorKit() for useCustomEditorKit languages");
        return null;
    }

    protected void toggleComment(String text, String expected) throws Exception {
        JEditorPane pane = getPane(text);

        runKitAction(pane, "toggle-comment", "");

        String toggled = pane.getText();
        assertEquals(expected, toggled);
    }

    protected JEditorPane getPane(String text) throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            fail("You must run this test from the event dispatch thread! To do that, add @Override protected boolean runInEQ() { return true } from your testcase!");
        }
        String BEGIN = "$start$"; // NOI18N
        String END = "$end$"; // NOI18N
        int sourceStartPos = text.indexOf(BEGIN);
        int caretPos = -1;
        int sourceEndPos = -1;
        if (sourceStartPos != -1) {
            text = text.substring(0, sourceStartPos) + text.substring(sourceStartPos+BEGIN.length());
            sourceEndPos = text.indexOf(END);
            assertTrue(sourceEndPos != -1);
            text = text.substring(0, sourceEndPos) + text.substring(sourceEndPos+END.length());
        } else {
            caretPos = text.indexOf('^');
            if (caretPos != -1) {
                text = text.substring(0, caretPos) + text.substring(caretPos+1);
            }
        }

        JEditorPane pane = new JEditorPane();
        pane.setContentType(getPreferredMimeType());
        final NbEditorKit kit = ((NbEditorKit)getEditorKit(getPreferredMimeType()));


        Thread preload = new Thread(new Runnable() {

            @Override
            public void run() {
                // Preload actions and other stuff
                if (kit instanceof Callable) {
                    try {
                        ((Callable) kit).call();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                kit.getActions();
            }
        });
        preload.start();
        preload.join();
        pane.setEditorKit(kit);
        pane.setText(text);

        BaseDocument bdoc = (BaseDocument)pane.getDocument();

        bdoc.putProperty(org.netbeans.api.lexer.Language.class, getPreferredLanguage().getLexerLanguage());
        bdoc.putProperty("mimeType", getPreferredMimeType());

        //bdoc.insertString(0, text, null);
        if (sourceStartPos != -1) {
            assertTrue(sourceEndPos != -1);
            pane.setSelectionStart(sourceStartPos);
            pane.setSelectionEnd(sourceEndPos);
        } else if (caretPos != -1) {
            pane.getCaret().setDot(caretPos);
        }
        pane.getCaret().setSelectionVisible(true);

        return pane;
    }

    protected void runKitAction(JEditorPane jt, String actionName, String cmd) {
        BaseKit kit = (BaseKit)jt.getEditorKit();
        Action a = kit.getActionByName(actionName);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(jt, 0, cmd));
    }

    protected void setupDocumentIndentation(Document doc, IndentPrefs preferences) {
        // Enforce indentprefs
        if (preferences != null) {
            assertEquals("Hanging indentation not yet supported; must be exposed through options", preferences.getIndentation(), preferences.getHangingIndentation());
            Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
            prefs.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, preferences.getIndentation());
        } else {
            int preferred = 4;
            if (getPreferredLanguage().hasFormatter()) {
                preferred = getPreferredLanguage().getFormatter().indentSize();
            }
            Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
            prefs.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, preferred);
        }
    }

    public void insertNewline(String source, String reformatted, IndentPrefs preferences) throws Exception {
        int sourcePos = source.indexOf('^');
        assertTrue("Source text must have a caret ^ marker", sourcePos != -1);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(null);

        int reformattedPos = reformatted.indexOf('^');
        assertTrue("Reformatted text must have a caret ^ marker", reformattedPos != -1);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    protected void insertBreak(String original, String expected) throws Exception {
        insertNewline(original, expected, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Code Completion Tests
    ////////////////////////////////////////////////////////////////////////////
    protected CodeCompletionHandler getCodeCompleter() {
        CodeCompletionHandler handler = getPreferredLanguage().getCompletionHandler();
        assertNotNull("You must override getCompletionHandler, either from your GsfLanguage or your test class", handler);
        return handler;
    }

    private String getSourceLine(String s, int offset) {
        int begin = offset;
        if (begin > 0) {
            begin = s.lastIndexOf('\n', offset-1);
            if (begin == -1) {
                begin = 0;
            } else if (begin < s.length()) {
                begin++;
            }
        }
        if (s.length() == 0) {
            return s;
        }
//        s.charAt(offset);
        int end = s.indexOf('\n', begin);
        if (end == -1) {
            end = s.length();
        }

        if (offset < end) {
            return (s.substring(begin, offset)+"|"+s.substring(offset,end)).trim();
        } else {
            return (s.substring(begin, end) + "|").trim();
        }
    }

    protected String getSourceWindow(String s, int offset) {
        int prevLineBegin;
        int nextLineEnd;
        int begin = offset;
        if (offset > 0) {
            begin = s.lastIndexOf('\n', offset);
            if (begin == -1) {
                begin = 0;
                prevLineBegin = 0;
            } else if (begin > 0) {
                prevLineBegin = s.lastIndexOf('\n', begin-1);
                if (prevLineBegin == -1) {
                    prevLineBegin = 0;
                } else if (prevLineBegin < s.length()) {
                    prevLineBegin++;
                }
            } else{
                prevLineBegin = 0;
            }
        } else {
            prevLineBegin = 0;
        }
        int end = s.indexOf('\n', offset);
        if (end == -1) {
            end = s.length();
            nextLineEnd = end;
        } else if (end < s.length()) {
            nextLineEnd = s.indexOf('\n', end+1);
            if (nextLineEnd == -1) {
                s.length();
            }
        } else {
            nextLineEnd = end;
        }
        return s.substring(prevLineBegin, offset)+"|"+s.substring(offset, nextLineEnd);
    }

    private String describeCompletion(String caretLine, String text, int caretOffset, boolean prefixSearch, boolean caseSensitive, QueryType type, List<CompletionProposal> proposals,
            boolean includeModifiers, boolean[] deprecatedHolder, final HtmlFormatter formatter) {
        assertTrue(deprecatedHolder != null && deprecatedHolder.length == 1);
        StringBuilder sb = new StringBuilder();
        sb.append("Code completion result for source line:\n");
        String sourceLine = getSourceLine(text, caretOffset);
        if (sourceLine.length() == 1) {
            sourceLine = getSourceWindow(text, caretOffset);
        }
        sb.append(sourceLine);
        sb.append("\n(QueryType=" + type + ", prefixSearch=" + prefixSearch + ", caseSensitive=" + caseSensitive + ")");
        sb.append("\n");

        // Sort to make test more stable
        proposals.sort(new Comparator<CompletionProposal>() {

            public int compare(CompletionProposal p1, CompletionProposal p2) {
                // Smart items first
                if (p1.isSmart() != p2.isSmart()) {
                    return p1.isSmart() ? -1 : 1;
                }

                if (p1.getKind() != p2.getKind()) {
                    return p1.getKind().compareTo(p2.getKind());
                }

                formatter.reset();
                String p1L = p1.getLhsHtml(formatter);
                formatter.reset();
                String p2L = p2.getLhsHtml(formatter);

                if (!p1L.equals(p2L)) {
                    return p1L.compareTo(p2L);
                }

                formatter.reset();
                String p1Rhs = p1.getRhsHtml(formatter);
                formatter.reset();
                String p2Rhs = p2.getRhsHtml(formatter);
                if (p1Rhs == null) {
                    p1Rhs = "";
                }
                if (p2Rhs == null) {
                    p2Rhs = "";
                }
                if (!p1Rhs.equals(p2Rhs)) {
                    return p1Rhs.compareTo(p2Rhs);
                }

                // Yuck - tostring comparison of sets!!
                if (!p1.getModifiers().toString().equals(p2.getModifiers().toString())) {
                    return p1.getModifiers().toString().compareTo(p2.getModifiers().toString());
                }

                return 0;
            }
        });

        boolean isSmart = true;
        for (CompletionProposal proposal : proposals) {
            if (isSmart && !proposal.isSmart()) {
                sb.append("------------------------------------\n");
                isSmart = false;
            }

            deprecatedHolder[0] = false;
            formatter.reset();
            proposal.getLhsHtml(formatter); // Side effect to deprecatedHolder used
            boolean strike = includeModifiers && deprecatedHolder[0];

            String n = proposal.getKind().toString();
            int MAX_KIND = 10;
            if (n.length() > MAX_KIND) {
                sb.append(n.substring(0, MAX_KIND));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_KIND; i++) {
                    sb.append(" ");
                }
            }

//            if (proposal.getModifiers().size() > 0) {
//                List<String> modifiers = new ArrayList<String>();
//                for (Modifier mod : proposal.getModifiers()) {
//                    modifiers.add(mod.name());
//                }
//                Collections.sort(modifiers);
//                sb.append(modifiers);
//            }

            sb.append(" ");

            formatter.reset();
            n = proposal.getLhsHtml(formatter);
            int MAX_LHS = 30;
            if (strike) {
                MAX_LHS -= 6; // Account for the --- --- strikethroughs
                sb.append("---");
            }
            if (n.length() > MAX_LHS) {
                sb.append(n.substring(0, MAX_LHS));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_LHS; i++) {
                    sb.append(" ");
                }
            }

            if (strike) {
                sb.append("---");
            }

            sb.append("  ");

            assertNotNull("Return Collections.emptySet() instead from getModifiers!", proposal.getModifiers());
            if (proposal.getModifiers().isEmpty()) {
                n = "";
            } else {
                n = proposal.getModifiers().toString();
            }
            int MAX_MOD = 9;
            if (n.length() > MAX_MOD) {
                sb.append(n.substring(0, MAX_MOD));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_MOD; i++) {
                    sb.append(" ");
                }
            }

            sb.append("  ");

            formatter.reset();
            sb.append(proposal.getRhsHtml(formatter));
            sb.append("\n");

            isSmart = proposal.isSmart();
        }

        return sb.toString();
    }

    private static org.netbeans.modules.csl.core.Language getCompletableLanguage(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument)doc;
        List<org.netbeans.modules.csl.core.Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
        for (org.netbeans.modules.csl.core.Language l : list) {
            if (l.getCompletionProvider() != null) {
                return l;
            }
        }

        return null;
    }

    public void checkCompletion(final String file, final String caretLine, final boolean includeModifiers) throws Exception {
        // TODO call TestCompilationInfo.setCaretOffset!
        final QueryType type = QueryType.COMPLETION;
        final boolean caseSensitive = true;

        Source testSource = getTestSource(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        final String[] described = new String[1];
        UserTask task = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler cc = getCodeCompleter();
                assertNotNull("getCodeCompleter must be implemented", cc);

                Document doc = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                boolean upToOffset = type == QueryType.COMPLETION;
                String prefix = cc.getPrefix(pr, caretOffset, upToOffset);
                if (prefix == null) {
                    if (prefix == null) {
                        int[] blk =
                            org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);

                        if (blk != null) {
                            int start = blk[0];
                            if (start < caretOffset ) {
                                if (upToOffset) {
                                    prefix = doc.getText(start, caretOffset - start);
                                } else {
                                    prefix = doc.getText(start, blk[1] - start);
                                }
                            }
                        }
                    }
                }

                final int finalCaretOffset = caretOffset;
                final String finalPrefix = prefix;
                final ParserResult finalParserResult = pr;
                CodeCompletionContext context = new CodeCompletionContext() {

                    @Override
                    public int getCaretOffset() {
                        return finalCaretOffset;
                    }

                    @Override
                    public ParserResult getParserResult() {
                        return finalParserResult;
                    }

                    @Override
                    public String getPrefix() {
                        return finalPrefix;
                    }

                    @Override
                    public boolean isPrefixMatch() {
                        return true;
                    }

                    @Override
                    public QueryType getQueryType() {
                        return type;
                    }

                    @Override
                    public boolean isCaseSensitive() {
                        return caseSensitive;
                    }
                };

                CodeCompletionResult completionResult = cc.complete(context);
                List<CompletionProposal> proposals = completionResult.getItems();

                final boolean deprecatedHolder[] = new boolean[1];
                final HtmlFormatter formatter = new HtmlFormatter() {
                    private StringBuilder sb = new StringBuilder();

                    @Override
                    public void reset() {
                        sb.setLength(0);
                    }

                    @Override
                    public void appendHtml(String html) {
                        sb.append(html);
                    }

                    @Override
                    public void appendText(String text, int fromInclusive, int toExclusive) {
                        sb.append(text, fromInclusive, toExclusive);
                    }

                    @Override
                    public void emphasis(boolean start) {
                    }

                    @Override
                    public void active(boolean start) {
                    }

                    @Override
                    public void name(ElementKind kind, boolean start) {
                    }

                    @Override
                    public void parameters(boolean start) {
                    }

                    @Override
                    public void type(boolean start) {
                    }

                    @Override
                    public void deprecated(boolean start) {
                        deprecatedHolder[0] = true;
                    }

                    @Override
                    public String getText() {
                        return sb.toString();
                    }
                };

                described[0] = describeCompletion(caretLine, pr.getSnapshot().getSource().createSnapshot().getText().toString(), caretOffset, true, caseSensitive, type, proposals, includeModifiers, deprecatedHolder, formatter);
            }
        };
        if (classPathsForTest == null || classPathsForTest.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
            assertDescriptionMatches(file, described[0], true, true, ".completion", true, false);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
            assertDescriptionMatches(file, described[0], true, true, ".completion", true, false);
        }
    }

    /**
     * Checks code completion result.
     * <p>
     * Should be used in situation when we are interested in the result that comes from the
     * completion of certain {@link CompletionProposal}.
     * <p>
     * One particular use case could be the fast import feature (i.e. automatic import of the
     * class that was chosen in code completion but wasn't imported yet).
     *
     * @param file the file we are testing against; will be used on the one hand as source
     *        file for the completion and on the other hand the corresponding file with the
     *        same name and additional ".ccresult" suffix will be find and used as a golden file
     * @param caretLine line where we are invoking completion
     * @param proposal proposal we want to complete
     * @throws ParseException encapsulating the user exception, might thrown by {@link ParserManager#parse(java.util.Collection, org.netbeans.modules.parsing.api.UserTask)
     */
    public void checkCompletionResult(
            final String file,
            final String caretLine,
            final CompletionProposal proposal) throws ParseException {

        final QueryType type = QueryType.COMPLETION;
        final boolean caseSensitive = true;

        Source testSource = getTestSource(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = caretOffset == -1 ? resultIterator.getParserResult() : resultIterator.getParserResult(caretOffset);
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler cc = getCodeCompleter();
                assertNotNull("getCodeCompleter must be implemented", cc);

                Document doc = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                boolean upToOffset = type == QueryType.COMPLETION;
                String prefix = cc.getPrefix(pr, caretOffset, upToOffset);
                if (prefix == null) {
                    int[] blk = org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);

                    if (blk != null) {
                        int start = blk[0];
                        if (start < caretOffset ) {
                            if (upToOffset) {
                                prefix = doc.getText(start, caretOffset - start);
                            } else {
                                prefix = doc.getText(start, blk[1] - start);
                            }
                        }
                    }
                }

                final int finalCaretOffset = caretOffset;
                final String finalPrefix = prefix;
                final ParserResult finalParserResult = pr;
                CodeCompletionContext context = new CodeCompletionContext() {

                    @Override
                    public int getCaretOffset() {
                        return finalCaretOffset;
                    }

                    @Override
                    public ParserResult getParserResult() {
                        return finalParserResult;
                    }

                    @Override
                    public String getPrefix() {
                        return finalPrefix;
                    }

                    @Override
                    public boolean isPrefixMatch() {
                        return true;
                    }

                    @Override
                    public QueryType getQueryType() {
                        return type;
                    }

                    @Override
                    public boolean isCaseSensitive() {
                        return caseSensitive;
                    }
                };

                CodeCompletionResult completionResult = cc.complete(context);
                completionResult.beforeInsert(proposal);
                completionResult.insert(proposal);
                completionResult.afterInsert(proposal);

                String fileContent = doc.getText(0, doc.getLength());
                assertFileContentsMatches(file, fileContent, false, ".ccresult");
            }
        });
    }

    public void checkCompletionDocumentation(final String file, final String caretLine, final boolean includeModifiers, final String itemPrefix) throws Exception {
        checkCompletionDocumentation(file, caretLine, includeModifiers, itemPrefix, QueryType.COMPLETION);
    }

    public void checkCompletionDocumentation(final String file, final String caretLine, final boolean includeModifiers, final String itemPrefix, QueryType queryType) throws Exception {
        // TODO call TestCompilationInfo.setCaretOffset!
        final QueryType type = queryType;
        final boolean caseSensitive = true;

        Source testSource = getTestSource(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        UserTask task = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler cc = getCodeCompleter();
                assertNotNull("getCodeCompleter must be implemented", cc);

                Document doc = GsfUtilities.getDocument(resultIterator.getSnapshot().getSource().getFileObject(), true);
                boolean upToOffset = type == QueryType.COMPLETION;
                String prefix = cc.getPrefix(pr, caretOffset, upToOffset);
                if (prefix == null) {
                    if (prefix == null) {
                        int[] blk =
                            org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);

                        if (blk != null) {
                            int start = blk[0];
                            if (start < caretOffset ) {
                                if (upToOffset) {
                                    prefix = doc.getText(start, caretOffset - start);
                                } else {
                                    prefix = doc.getText(start, blk[1] - start);
                                }
                            }
                        }
                    }
                }


                // resultIterator.getSource().testUpdateIndex();

                final int finalCaretOffset = caretOffset;
                final String finalPrefix = prefix;
                final ParserResult finalParserResult = pr;
                CodeCompletionContext context = new CodeCompletionContext() {

                    @Override
                    public int getCaretOffset() {
                        return finalCaretOffset;
                    }

                    @Override
                    public ParserResult getParserResult() {
                        return finalParserResult;
                    }

                    @Override
                    public String getPrefix() {
                        return finalPrefix;
                    }

                    @Override
                    public boolean isPrefixMatch() {
                        return false;
                    }

                    @Override
                    public QueryType getQueryType() {
                        return type;
                    }

                    @Override
                    public boolean isCaseSensitive() {
                        return caseSensitive;
                    }
                };

                CodeCompletionResult completionResult = cc.complete(context);
                List<CompletionProposal> proposals = completionResult.getItems();

                CompletionProposal match = null;
                for (CompletionProposal proposal : proposals) {
                    if (proposal.getName().startsWith(itemPrefix)) {
                        match = proposal;
                        break;
                    }
                }
                assertNotNull(match);
                assertNotNull(match.getElement());

                // Get documentation
                String documentation;
                if (cc instanceof CodeCompletionHandler2) {
                    CodeCompletionHandler2 cc2 = (CodeCompletionHandler2) cc;
                    Documentation docu = cc2.documentElement(pr, match.getElement(), new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return false;
                        }
                    });
                    documentation = docu == null ? cc2.document(pr, match.getElement()) : docu.getContent();
                } else {
                    documentation = cc.document(pr, match.getElement());
                }

                final boolean deprecatedHolder[] = new boolean[1];
                final HtmlFormatter formatter = new HtmlFormatter() {
                    private StringBuilder sb = new StringBuilder();

                    @Override
                    public void reset() {
                        sb.setLength(0);
                    }

                    @Override
                    public void appendHtml(String html) {
                        sb.append(html);
                    }

                    @Override
                    public void appendText(String text, int fromInclusive, int toExclusive) {
                        sb.append(text, fromInclusive, toExclusive);
                    }

                    @Override
                    public void emphasis(boolean start) {
                    }

                    @Override
                    public void active(boolean start) {
                    }

                    @Override
                    public void name(ElementKind kind, boolean start) {
                    }

                    @Override
                    public void parameters(boolean start) {
                    }

                    @Override
                    public void type(boolean start) {
                    }

                    @Override
                    public void deprecated(boolean start) {
                        deprecatedHolder[0] = true;
                    }

                    @Override
                    public String getText() {
                        return sb.toString();
                    }
                };

                String described = describeCompletionDoc(pr.getSnapshot().getText().toString(), caretOffset, false, caseSensitive, type, match, documentation, includeModifiers, deprecatedHolder, formatter);
                assertDescriptionMatches(file, described, true, ".html");
            }
        };

        if (classPathsForTest == null || classPathsForTest.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
        }
    }

    private String describeCompletionDoc(String text, int caretOffset, boolean prefixSearch, boolean caseSensitive, QueryType type,
             CompletionProposal proposal, String documentation,
            boolean includeModifiers, boolean[] deprecatedHolder, final HtmlFormatter formatter) {
        assertTrue(deprecatedHolder != null && deprecatedHolder.length == 1);
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>\n");
        sb.append("<pre>");
        sb.append("Code completion result for source line:\n");
        String sourceLine = getSourceLine(text, caretOffset);
        if (sourceLine.length() == 1) {
            sourceLine = getSourceWindow(text, caretOffset);
        }
        sb.append(sourceLine);
        sb.append("\n(QueryType=" + type + ", prefixSearch=" + prefixSearch + ", caseSensitive=" + caseSensitive + ")");
        sb.append("\n");

        boolean isSmart = true;
        if (isSmart && !proposal.isSmart()) {
            sb.append("------------------------------------\n");
            isSmart = false;
        }

        deprecatedHolder[0] = false;
        formatter.reset();
        proposal.getLhsHtml(formatter); // Side effect to deprecatedHolder used
        boolean strike = includeModifiers && deprecatedHolder[0];

        String n = proposal.getKind().toString();
        int MAX_KIND = 10;
        if (n.length() > MAX_KIND) {
            sb.append(n.substring(0, MAX_KIND));
        } else {
            sb.append(n);
            for (int i = n.length(); i < MAX_KIND; i++) {
                sb.append(" ");
            }
        }

//            if (proposal.getModifiers().size() > 0) {
//                List<String> modifiers = new ArrayList<String>();
//                for (Modifier mod : proposal.getModifiers()) {
//                    modifiers.add(mod.name());
//                }
//                Collections.sort(modifiers);
//                sb.append(modifiers);
//            }

        sb.append(" ");

        formatter.reset();
        n = proposal.getLhsHtml(formatter);
        int MAX_LHS = 30;
        if (strike) {
            MAX_LHS -= 6; // Account for the --- --- strikethroughs
            sb.append("---");
        }
        if (n.length() > MAX_LHS) {
            sb.append(n.substring(0, MAX_LHS));
        } else {
            sb.append(n);
            for (int i = n.length(); i < MAX_LHS; i++) {
                sb.append(" ");
            }
        }

        if (strike) {
            sb.append("---");
        }

        sb.append("  ");

        assertNotNull("Return Collections.emptySet() instead from getModifiers!", proposal.getModifiers());
        if (proposal.getModifiers().isEmpty()) {
            n = "";
        } else {
            n = proposal.getModifiers().toString();
        }
        int MAX_MOD = 9;
        if (n.length() > MAX_MOD) {
            sb.append(n.substring(0, MAX_MOD));
        } else {
            sb.append(n);
            for (int i = n.length(); i < MAX_MOD; i++) {
                sb.append(" ");
            }
        }

        sb.append("  ");

        formatter.reset();
        sb.append(proposal.getRhsHtml(formatter));
        sb.append("\n");

        isSmart = proposal.isSmart();
        sb.append("</pre>");
        sb.append("<h2>Documentation:</h2>");
        sb.append(alterDocumentationForTest(documentation));

        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Sometimes the documentation can contain absolute path. When you overwrite
     * this method, you can exclude such thinks from it.
     * @param documentation
     * @return changed documentation
     */
    protected String alterDocumentationForTest(String documentation) {
        return documentation;
    }

    protected void assertAutoQuery(QueryType queryType, String source, String typedText) {
        CodeCompletionHandler completer = getCodeCompleter();
        int caretPos = source.indexOf('^');
        source = source.substring(0, caretPos) + source.substring(caretPos+1);

        BaseDocument doc = getDocument(source);
        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(caretPos);

        QueryType qt = completer.getAutoQuery(ta, typedText);
        assertEquals(queryType, qt);
    }

    protected void checkCall(ParserResult info, int caretOffset, String param, boolean expectSuccess) {
    }

    public void checkComputeMethodCall(String file, final String caretLine, final String param, final boolean expectSuccess) throws Exception {
        final QueryType type = QueryType.COMPLETION;
        //boolean caseSensitive = true;

        Source testSource = getTestSource(getTestFile(file));

        final int caretOffset;
        if (caretLine != null) {
            caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        } else {
            caretOffset = -1;
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler cc = getCodeCompleter();
                assertNotNull("getCodeCompleter must be implemented", cc);

                Document doc = GsfUtilities.getDocument(pr.getSnapshot().getSource().getFileObject(), true);
                boolean upToOffset = type == QueryType.COMPLETION;
                String prefix = cc.getPrefix(pr, caretOffset, upToOffset);
                if (prefix == null) {
                    if (prefix == null) {
                        int[] blk = org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)doc, caretOffset);

                        if (blk != null) {
                            int start = blk[0];
                            if (start < caretOffset ) {
                                if (upToOffset) {
                                    prefix = doc.getText(start, caretOffset - start);
                                } else {
                                    prefix = doc.getText(start, blk[1] - start);
                                }
                            }
                        }
                    }
                }

                checkCall(pr, caretOffset, param, expectSuccess);
            }
        });
    }

    public void checkPrefix(final String relFilePath) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                CodeCompletionHandler completer = getCodeCompleter();
                assertNotNull("getSemanticAnalyzer must be implemented", completer);

                BaseDocument doc = GsfUtilities.getDocument(resultIterator.getSnapshot().getSource().getFileObject(), true);
                StringBuilder sb = new StringBuilder();

                int index = 0;
                while (index < doc.getLength()) {
                    int lineStart = index;
                    int lineEnd = Utilities.getRowEnd(doc, index);
                    if (lineEnd == -1) {
                        break;
                    }
                    if (Utilities.getRowFirstNonWhite(doc, index) != -1) {
                        String line = doc.getText(lineStart, lineEnd-lineStart);
                        for (int i = lineStart; i <= lineEnd; i++) {
                            String prefix = completer.getPrefix(pr, i, true); // line.charAt(i)
                            if (prefix == null) {
                                continue;
                            }
                            String wholePrefix = completer.getPrefix(pr, i, false);
                            assertNotNull(wholePrefix);

                            sb.append(line +"\n");
                            //sb.append("Offset ");
                            //sb.append(Integer.toString(i));
                            //sb.append(" : \"");
                            for (int j = lineStart; j < i; j++) {
                                sb.append(' ');
                            }
                            sb.append('^');
                            sb.append(prefix.length() > 0 ? prefix : "\"\"");
                            sb.append(",");
                            sb.append(wholePrefix.length() > 0 ? wholePrefix : "\"\"");
                            sb.append("\n");
                        }
                    }

                    index = lineEnd+1;
                }

                String annotatedSource = sb.toString();
                assertDescriptionMatches(relFilePath, annotatedSource, false, ".prefixes");
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////
    // Ast Offsets Test
    ////////////////////////////////////////////////////////////////////////////
    protected String describeNode(ParserResult info, Object node, boolean includePath) throws Exception {
        // Override in your test
        return null;
    }

    protected void initializeNodes(ParserResult result, List<Object> validNodes,
            Map<Object,OffsetRange> positions, List<Object> invalidNodes) throws Exception {
        // Override in your test
    }

    protected void checkOffsets(String relFilePath) throws Exception {
        checkOffsets(relFilePath, null);
    }

    protected void checkOffsets(final String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        if (caretLine != null) {
            int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                List<Object> validNodes = new ArrayList<Object>();
                List<Object> invalidNodes = new ArrayList<Object>();
                Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
                initializeNodes(pr, validNodes, positions, invalidNodes);

                String annotatedSource = annotateOffsets(validNodes, positions, invalidNodes, pr);
                assertDescriptionMatches(relFilePath, annotatedSource, false, ".offsets");
            }
        });
    }


    /** Pass the nodes in an in-order traversal order such that it can properly nest
     * items when they have identical starting or ending endpoints */
    private String annotateOffsets(List<Object> validNodes, Map<Object,OffsetRange> positions,
            List<Object> invalidNodes, ParserResult info) throws Exception {
        //
        StringBuilder sb = new StringBuilder();
        BaseDocument doc = GsfUtilities.getDocument(info.getSnapshot().getSource().getFileObject(), true);
        String text = doc.getText(0, doc.getLength());

        final Map<Object,Integer> traversalNumber = new HashMap<Object,Integer>();
        int id = 0;
        for (Object node : validNodes) {
            traversalNumber.put(node, id++);
        }

        Comparator<Object> FORWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));

                return traversalNumber.get(o1) - traversalNumber.get(o2);
            }
        };

        Comparator<Object> BACKWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));

                return traversalNumber.get(o2) - traversalNumber.get(o1);
            }
        };

        Map<Integer,List<Object>> starts = new HashMap<Integer,List<Object>>(100);
        Map<Integer,List<Object>> ends = new HashMap<Integer,List<Object>>(100);

        for (Object node : validNodes) {
            OffsetRange range = positions.get(node);
            List<Object> list = starts.get(range.getStart());
            if (list == null) {
                list = new ArrayList<Object>();
                starts.put(range.getStart(), list);
            }
            list.add(node);
            list = ends.get(range.getEnd());
            if (list == null) {
                list = new ArrayList<Object>();
                ends.put(range.getEnd(), list);
            }
            list.add(node);
        }

        // Sort nodes
        for (List<Object> list : starts.values()) {
            list.sort(FORWARDS_COMPARATOR);
        }
        for (List<Object> list : ends.values()) {
            list.sort(BACKWARDS_COMPARATOR);
        }

        // Include 0-0 nodes first
        List<String> missing = new ArrayList<String>();
        for (Object n : invalidNodes) {
            String desc = describeNode(info, n, true);
            assertNotNull("You must implement describeNode()", desc);

            missing.add("Missing position for node " + desc);
        }
        Collections.sort(missing);
        for (String s : missing) {
            sb.append(s);
            sb.append("\n");
        }
        sb.append("\n");

        for (int i = 0; i < text.length(); i++) {
            List<Object> deferred = null;
            if (ends.containsKey(i)) {
                List<Object> ns = ends.get(i);
                List<Object> sts = starts.get(i);
                for (Object n : ns) {
                    if (sts != null && sts.contains(n)) {
                        if (deferred == null) {
                            deferred = new ArrayList<Object>();
                        }
                        deferred.add(n);
                    } else {
                        sb.append("</");
                        String desc = describeNode(info, n, false);
                        assertNotNull(desc);
                        sb.append(desc);
                        sb.append(">");
                    }
                }
            }
            if (starts.containsKey(i)) {
                List<Object> ns = starts.get(i);
                List<Object> ets = ends.get(i);
                for (Object n : ns) {
                    if (ets != null && ets.contains(n)) {
                        if (deferred == null) {
                            deferred = new ArrayList<Object>();
                        } else if (deferred.get(deferred.size()-1) != n) {
                            deferred.add(n);
                        }
                    } else {
                        sb.append("<");
                        String desc = describeNode(info, n, false);
                        assertNotNull(desc);
                        sb.append(desc);
                        sb.append(">");
                    }
                }
            }
            if (deferred != null) {
                for (Object n : deferred) {
                    sb.append("<");
                    String desc = describeNode(info, n, false);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append("/>");
                }
            }
            char c = text.charAt(i);
            switch (c) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }



    ////////////////////////////////////////////////////////////////////////////
    // Incremental Parsing and Offsets
    ////////////////////////////////////////////////////////////////////////////
    protected void verifyIncremental(ParserResult result, EditHistory history, ParserResult oldResult) {
        // Your module should check that the parser results are really okay and incremental here
    }

    public class TestDocumentEvent implements DocumentEvent {
        private int offset;
        private int length;

        public TestDocumentEvent(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public int getOffset() {
            return offset;
        }
        public int getLength() {
            return length;
        }
        public Document getDocument() {
            return null;
        }
        public EventType getType() {
            return null;
        }
        public ElementChange getChange(Element elem) {
            return null;
        }
    }


    public static final String INSERT = "insert:"; // NOI18N
    public static final String REMOVE = "remove:"; // NOI18N

//    public class IncrementalParse {
//        public ParserResult oldParserResult;
//        public GsfTestCompilationInfo info;
//        public ParserResult newParserResult;
//        public ParserResult fullParseResult;
//        public EditHistory history;
//        public String initialSource;
//        public String modifiedSource;
//
//        public IncrementalParse(ParserResult oldParserResult, GsfTestCompilationInfo info, ParserResult newParserResult,
//                EditHistory history,
//                String initialSource, String modifiedSource,
//                ParserResult fullParseResult
//                ) {
//            this.oldParserResult = oldParserResult;
//            this.info = info;
//            this.newParserResult = newParserResult;
//            this.history = history;
//            this.initialSource = initialSource;
//            this.modifiedSource = modifiedSource;
//            this.fullParseResult = fullParseResult;
//        }
//    }

    protected final Pair<EditHistory,String> getEditHistory(String initialText, String... edits) {
        return getEditHistory(initialText, new EditHistory(), edits);
    }

    protected final Pair<EditHistory,String> getEditHistory(String initialText, EditHistory history, String... edits) {
        assertNotNull("Must provide a list of edits", edits);
        assertTrue("Should be an even number of edit events: pairs of caret, insert/remove", edits.length % 2 == 0);

        String modifiedText = initialText;
        for (int i = 0, n = edits.length; i < n; i += 2) {
            String caretLine = edits[i];
            String event = edits[i+1];
            int caretOffset = getCaretOffset(modifiedText, caretLine);

            assertTrue(event + " must start with " + INSERT + " or " + REMOVE,
                    event.startsWith(INSERT) || event.startsWith(REMOVE));
            if (event.startsWith(INSERT)) {
                event = event.substring(INSERT.length());
                history.insertUpdate(new TestDocumentEvent(caretOffset, event.length()));
                modifiedText = modifiedText.substring(0, caretOffset) + event + modifiedText.substring(caretOffset);
            } else {
                assertTrue(event.startsWith(REMOVE));
                event = event.substring(REMOVE.length());
                assertTrue(modifiedText.regionMatches(caretOffset, event, 0, event.length()));
                history.removeUpdate(new TestDocumentEvent(caretOffset, event.length()));
                modifiedText = modifiedText.substring(0, caretOffset) + modifiedText.substring(caretOffset+event.length());
            }
        }

        return Pair.<EditHistory,String>of(history, modifiedText);
    }

    protected final Pair<EditHistory,String> getEditHistory(BaseDocument doc, final EditHistory history, String... edits) throws BadLocationException {
        assertNotNull("Must provide a list of edits", edits);
        assertTrue("Should be an even number of edit events: pairs of caret, insert/remove", edits.length % 2 == 0);

        String initialText = doc.getText(0, doc.getLength());
        doc.addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                history.insertUpdate(e);
            }

            public void removeUpdate(DocumentEvent e) {
                history.removeUpdate(e);
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });

        TokenHierarchy th = TokenHierarchy.get((Document) doc);
        th.addTokenHierarchyListener(new TokenHierarchyListener() {
                public void tokenHierarchyChanged(TokenHierarchyEvent e) {
                    // I'm getting empty token change events from tests...
                    // Figure out why this happens
                    // assert e.type() != TokenHierarchyEventType.MODIFICATION || e.tokenChange().addedTokenCount() > 0 || e.tokenChange().removedTokenCount() > 0;
                    history.tokenHierarchyChanged(e);
                }
        });


        // Attempt to activate them token hierarchy, one of my attempts to get TokenHierarchyEvents fired
        //// doc.writeLock();
        //try {
        //    MutableTextInput input = (MutableTextInput)doc.getProperty(MutableTextInput.class);
        //    assertNotNull(input);
        //    input.tokenHierarchyControl().setActive(true);
        //} finally {
        //    // doc.writeUnlock();
        //}

        String modifiedText = initialText;
        for (int i = 0, n = edits.length; i < n; i += 2) {
            String caretLine = edits[i];
            String event = edits[i+1];
            int caretOffset = getCaretOffset(modifiedText, caretLine);

            assertTrue(event + " must start with " + INSERT + " or " + REMOVE,
                    event.startsWith(INSERT) || event.startsWith(REMOVE));
            if (event.startsWith(INSERT)) {
                event = event.substring(INSERT.length());
                //assertTrue(th.isActive());
                doc.insertString(caretOffset, event, null);
                modifiedText = modifiedText.substring(0, caretOffset) + event + modifiedText.substring(caretOffset);
            } else {
                assertTrue(event.startsWith(REMOVE));
                event = event.substring(REMOVE.length());
                assertTrue(modifiedText.regionMatches(caretOffset, event, 0, event.length()));
                doc.remove(caretOffset, event.length());
                modifiedText = modifiedText.substring(0, caretOffset) + modifiedText.substring(caretOffset+event.length());
            }
        }

        assertEquals(modifiedText, doc.getText(0, doc.getLength()));
        // Make sure the hierarchy is activated - this happens when we obtain a token sequence
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();
        for (int i = 0; i < 10; i++) {
            if (!ts.moveNext()) {
                break;
            }
        }

        return Pair.<EditHistory,String>of(history, modifiedText);
    }

//    /**
//     * Produce an incremental parser result for the given test file with the given
//     * series of edits. An edit is a pair of caret position string (with ^ representing
//     * the caret) and a corresponding insert or delete (with insert:string or remove:string)
//     * as the value.
//     */
//    protected final IncrementalParse getIncrementalResult(String relFilePath, double speedupExpectation, String... edits) throws Exception {
//        GsfTestCompilationInfo info = getInfo(relFilePath);
//
//        // Obtain the initial parse result
//        ParserResult initialResult = info.getEmbeddedResult(getPreferredMimeType(), 0);
//        assertNotNull(initialResult);
//
//        // Apply edits
//        String initialText = info.getText();
//        assertNotNull(initialText);
//        Pair<EditHistory,String> pair = getEditHistory(initialText, edits);
//        EditHistory history = pair.getA();
//        String modifiedText = pair.getB();
//
//        info.setText(modifiedText);
//        info.setEditHistory(history);
//        info.setPreviousResult(initialResult);
//
//        // Attempt to avoid garbage collection during timing
//        System.gc();
//        System.gc();
//        System.gc();
//        long incrementalStartTime = System.nanoTime();
//        int caretOffset = history.getStart();
//        if (history.getSizeDelta() > 0) {
//            caretOffset += history.getSizeDelta();
//        }
//        info.setCaretOffset(caretOffset);
//        ParserResult incrementalResult = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
//        assertNotNull(incrementalResult);
//        long incrementalEndTime = System.nanoTime();
//        verifyIncremental(incrementalResult, history, initialResult);
//
//        info.setEditHistory(null);
//        info.setPreviousResult(null);
//        info.setText(modifiedText);
//
//        System.gc();
//        System.gc();
//        System.gc();
//        long fullParseStartTime = System.nanoTime();
//        ParserResult fullParseResult = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
//        long fullParseEndTime = System.nanoTime();
//        assertNotNull(fullParseResult);
//
//        if (speedupExpectation > 0.0) {
//            long incrementalParseTime = incrementalEndTime-incrementalStartTime;
//            long fullParseTime = fullParseEndTime-fullParseStartTime;
//            // Figure out how to ensure garbage collections etc. make a fair run.
//            assertTrue("Incremental parsing time (" + incrementalParseTime + " ns) should be less than full parse time (" + fullParseTime + " ns); speedup was " +
//                    ((double)fullParseTime)/incrementalParseTime,
//                    ((double)incrementalParseTime)*speedupExpectation < fullParseTime);
//        }
//
//        assertEquals(incrementalResult.getDiagnostics().toString(), fullParseResult.getDiagnostics().size(), incrementalResult.getDiagnostics().size());
//
//        return new IncrementalParse(initialResult, info, incrementalResult, history, initialText, modifiedText, fullParseResult);
//    }
//
//    /**
//     * Check incremental parsing
//     * @param relFilePath Path to test file to be parsed
//     * @param speedupExpectation The speed up we're expecting for incremental processing
//     *   over normal full-file analysis. E.g. 1.0d means we want to ensure that incremental
//     *   parsing is at least as fast as normal parsing. For small files there may be extra
//     *   overhead; you can pass 0.0d to turn off this check (but the test runs to ensure
//     *   that things are working okay.)
//     * @param edits A list of edits to perform.
//     */
//    protected final void checkIncremental(String relFilePath, double speedupExpectation, String... edits) throws Exception {
//        IncrementalParse parse = getIncrementalResult(relFilePath, speedupExpectation, edits);
//
//        ParserResult incrementalResult = parse.newParserResult;
//        ParserResult fullParseResult = parse.fullParseResult;
//        ParserResult info = parse.info;
//
//        BaseDocument doc = (BaseDocument)info.getDocument();
//        assertEquals("Parse trees must equal", doc, fullParseResult,incrementalResult);
//
////        List<Object> incrValidNodes = new ArrayList<Object>();
////        List<Object> incrInvalidNodes = new ArrayList<Object>();
////        Map<Object,OffsetRange> incrPositions = new HashMap<Object,OffsetRange>();
////        initializeNodes(info, incrementalResult, incrValidNodes, incrPositions, incrInvalidNodes);
////
////        String incrementalAnnotatedSource = annotateOffsets(incrValidNodes, incrPositions, incrInvalidNodes, info);
////
////        // Now make sure we get an identical linearization of the non-incremental result
////        List<Object> validNodes = new ArrayList<Object>();
////        List<Object> invalidNodes = new ArrayList<Object>();
////        Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
////        initializeNodes(info, fullParseResult, validNodes, positions, invalidNodes);
////
////        String fullParseAnnotatedSource = annotateOffsets(validNodes, positions, invalidNodes, info);
////
////        assertEquals(fullParseAnnotatedSource, incrementalAnnotatedSource);
//    }

    protected void assertEquals(String message, BaseDocument doc, ParserResult expected, ParserResult actual) throws Exception {
        fail("You must override assertEquals(ParserResult,ParserResult)");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Type Test
    ////////////////////////////////////////////////////////////////////////////
    protected void initializeTypeNodes(ParserResult info, List<Object> nodes,
            Map<Object,OffsetRange> positions, Map<Object,String> types) throws Exception {
        // Override in your test
        // Associate type descriptions with a bunch of nodes.
        // For every node that has an associated type, add position and description information about it.
        // This will then be used to generate type hints in the source
    }

    protected void checkTypes(String relFilePath) throws Exception {
        checkTypes(relFilePath, null);
    }

    protected void checkTypes(final String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        if (caretLine != null) {
            int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
            enforceCaretOffset(testSource, caretOffset);
        }

        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                List<Object> nodes = new ArrayList<Object>();
                Map<Object,String> types = new HashMap<Object,String>();
                Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
                initializeTypeNodes(pr, nodes, positions, types);

                String annotatedSource = annotateTypes(nodes, positions, types, pr);
                assertDescriptionMatches(relFilePath, annotatedSource, false, ".types");
            }
        });
    }


    /** Pass the nodes in an in-order traversal order such that it can properly nest
     * items when they have identical starting or ending endpoints */
    private String annotateTypes(List<Object> validNodes, Map<Object,OffsetRange> positions,
            Map<Object,String> types, ParserResult info) throws Exception {
        //
        StringBuilder sb = new StringBuilder();
        String text = info.getSnapshot().getText().toString();

        final Map<Object,Integer> traversalNumber = new HashMap<Object,Integer>();
        int id = 0;
        for (Object node : validNodes) {
            traversalNumber.put(node, id++);
        }

        Comparator<Object> FORWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));

                return traversalNumber.get(o1) - traversalNumber.get(o2);
            }
        };

        Comparator<Object> BACKWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));

                return traversalNumber.get(o2) - traversalNumber.get(o1);
            }
        };

        Map<Integer,List<Object>> starts = new HashMap<Integer,List<Object>>(100);
        Map<Integer,List<Object>> ends = new HashMap<Integer,List<Object>>(100);

        for (Object node : validNodes) {
            OffsetRange range = positions.get(node);
            List<Object> list = starts.get(range.getStart());
            if (list == null) {
                list = new ArrayList<Object>();
                starts.put(range.getStart(), list);
            }
            list.add(node);
            list = ends.get(range.getEnd());
            if (list == null) {
                list = new ArrayList<Object>();
                ends.put(range.getEnd(), list);
            }
            list.add(node);
        }

        // Sort nodes
        for (List<Object> list : starts.values()) {
            list.sort(FORWARDS_COMPARATOR);
        }
        for (List<Object> list : ends.values()) {
            list.sort(BACKWARDS_COMPARATOR);
        }

        // TODO - include information here about nodes without correct positions

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                List<Object> ns = starts.get(i);
                for (Object n : ns) {
                    sb.append("<");
                    String desc = types.get(n);
                    //String desc = describeNode(info, n, false);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            if (ends.containsKey(i)) {
                List<Object> ns = ends.get(i);
                for (Object n : ns) {
                    sb.append("</");
                    //String desc = describeNode(info, n, false);
                    String desc = types.get(n);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            char c = text.charAt(i);
            switch (c) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Hints / Quickfix Tests
    ////////////////////////////////////////////////////////////////////////////
    protected HintsProvider getHintsProvider() {
        HintsProvider provider = getPreferredLanguage().getHintsProvider();
        assertNotNull("You must override getHintsProvider, either from your GsfLanguage or your test class", provider);
        return provider;
    }

    private GsfHintsManager getHintsManager(org.netbeans.modules.csl.core.Language language) {
        return new GsfHintsManager(getPreferredMimeType(), getHintsProvider(), language);
    }

    protected String annotateHints(BaseDocument doc, List<Hint> result, int caretOffset) throws Exception {
        Map<OffsetRange, List<Hint>> posToDesc = new HashMap<OffsetRange, List<Hint>>();
        Set<OffsetRange> ranges = new HashSet<OffsetRange>();
        for (Hint desc : result) {
            int start = desc.getRange().getStart();
            int end = desc.getRange().getEnd();
            OffsetRange range = new OffsetRange(start, end);
            List<Hint> l = posToDesc.get(range);
            if (l == null) {
                l = new ArrayList<Hint>();
                posToDesc.put(range, l);
            }
            l.add(desc);
            ranges.add(range);
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index);
            int lineEnd = Utilities.getRowEnd(doc, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                List<Hint> descsOnLine = null;
                int underlineStart = -1;
                int underlineEnd = -1;
                for (int i = lineStart; i <= lineEnd && i < text.length(); i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
                    if (starts.containsKey(i)) {
                        if (descsOnLine == null) {
                            descsOnLine = new ArrayList<Hint>();
                        }
                        underlineStart = i-lineStart;
                        OffsetRange range = starts.get(i);
                        if (posToDesc.get(range) != null) {
                            for (Hint desc : posToDesc.get(range)) {
                                descsOnLine.add(desc);
                            }
                        }
                    }
                    if (ends.containsKey(i)) {
                        underlineEnd = i-lineStart;
                    }
                    sb.append(text.charAt(i));
                }
                if (underlineStart != -1) {
                    for (int i = 0; i < underlineStart; i++) {
                        sb.append(" ");
                    }
                    for (int i = underlineStart; i < underlineEnd; i++) {
                        sb.append("-");
                    }
                    sb.append("\n");
                }
                if (descsOnLine != null) {
                    descsOnLine.sort(new Comparator<Hint>() {
                        public int compare(Hint arg0, Hint arg1) {
                            return arg0.getDescription().compareTo(arg1.getDescription());
                        }
                    });
                    for (Hint desc : descsOnLine) {
                        sb.append("HINT:");
                        sb.append(desc.getDescription());
                        sb.append("\n");
                        List<HintFix> list = desc.getFixes();
                        if (list != null) {
                            for (HintFix fix : list) {
                                sb.append("FIX:");
                                sb.append(fix.getDescription());
                                sb.append("\n");
                            }
                        }
                    }
                }
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }

    protected boolean parseErrorsOk;

    protected boolean checkAllHintOffsets() {
        return true;
    }

    protected void customizeHintError(Error error, int start) {
        // Optionally override
    }

    protected enum ChangeOffsetType { NONE, OVERLAP, OUTSIDE };

    private void customizeHintInfo(ParserResult result, ChangeOffsetType changeOffsetType) {
        if (changeOffsetType == ChangeOffsetType.NONE) {
            return;
        }
        if (result == null) {
            return;
        }
        // Test offset handling to make sure we can handle bogus node positions

//        Document doc = GsfUtilities.getDocument(result.getSnapshot().getSource().getFileObject(), true);
//        int docLength = doc.getLength();
        int docLength = result.getSnapshot().getText().length();

        // Replace errors with offsets
        List<Error> errors = new ArrayList<Error>();
        List<? extends Error> oldErrors = result.getDiagnostics();
        for (Error error : oldErrors) {
            int start = error.getStartPosition();
            int end = error.getEndPosition();

            // Modify document position to be off
            int length = end - start;
            if (changeOffsetType == ChangeOffsetType.OUTSIDE) {
                start = docLength + 1;
            } else {
                start = docLength - 1;
            }
            end = start + length;
            if (end <= docLength) {
                end = docLength + 1;
            }

            DefaultError newError = new DefaultError(
                    error.getKey(), error.getDisplayName(),
                    error.getDescription(), error.getFile(), start,
                    end, error.getSeverity()
            );
            newError.setParameters(error.getParameters());
            customizeHintError(error, start); // XXX: should not this be newError ??
            errors.add(newError);
        }
// XXX: there is no way to fiddle with parser errors
//        oldErrors.clear();
//        oldErrors.addAll(errors);
    }

    protected ComputedHints getHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints hints = computeHints(test, hint, relFilePath, fileObject, caretLine, ChangeOffsetType.NONE);

        if (checkAllHintOffsets()) {
            // Run alternate hint computation AFTER the real computation above since we will destroy the document...
            Logger.global.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    if (record.getThrown() != null) {
                        StringWriter sw = new StringWriter();
                        record.getThrown().printStackTrace(new PrintWriter(sw));
                        fail("Encountered error: " + sw.toString());
                    }
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }

            });
            for (ChangeOffsetType type : new ChangeOffsetType[] { ChangeOffsetType.OUTSIDE, ChangeOffsetType.OVERLAP }) {
                computeHints(test, hint, relFilePath, fileObject, caretLine, type);
            }
        }

        return hints;
    }

    protected ComputedHints computeHints(final NbTestCase test, final Rule hint, String relFilePath, FileObject fileObject, final String lineWithCaret, final ChangeOffsetType changeOffsetType) throws Exception {
        assertTrue(relFilePath == null || fileObject == null);

        initializeRegistry();

        if (fileObject == null) {
            fileObject = getTestFile(relFilePath);
        }

        Source testSource = getTestSource(fileObject);

        final int caretOffset;
        final String caretLine;
        if (lineWithCaret != null) {
            CaretLineOffset caretLineOffset = getCaretOffsetInternal(testSource.createSnapshot().getText().toString(), lineWithCaret);
            caretOffset = caretLineOffset.offset;
            caretLine = caretLineOffset.caretLine;
            enforceCaretOffset(testSource, caretLineOffset.offset);
        } else {
            caretOffset = -1;
            caretLine = lineWithCaret;
        }

        final ComputedHints [] result = new ComputedHints[] { null };
        UserTask task = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                Document document = pr.getSnapshot().getSource().getDocument(true);
                assert document != null : test;

                // remember the original document content, we are going to destroy it
                // and then restore
                String originalDocumentContent = null;
                if (changeOffsetType != ChangeOffsetType.NONE) {
                    customizeHintInfo(pr, changeOffsetType);

                    // Also: Delete te contents from the document!!!
                    // This ensures that the node offsets will be out of date by the time the rules run
                    originalDocumentContent = document.getText(0, document.getLength());
                    document.remove(0, document.getLength());
                }

                try {
                    if (!(hint instanceof ErrorRule)) { // only expect testcase source errors in error tests
                        if (parseErrorsOk) {
                            int caretOffset = 0;
                            result[0] = new ComputedHints(pr, new ArrayList<Hint>(), caretOffset);
                            return;
                        } else {
                            boolean errors = false;
                            for(Error e : pr.getDiagnostics()) {
                                if (e.getSeverity() == Severity.ERROR) {
                                    errors = true;
                                    break;
                                }
                            }
                            assertTrue("Unexpected parse error in test case " +
                                    FileUtil.getFileDisplayName(pr.getSnapshot().getSource().getFileObject()) + "\nErrors = " +
                                    pr.getDiagnostics(), !errors);
                        }
                    }

                    String text = pr.getSnapshot().getText().toString();

                    org.netbeans.modules.csl.core.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
                    HintsProvider provider = getHintsProvider();
                    GsfHintsManager manager = getHintsManager(language);

                    UserConfigurableRule ucr = null;
                    if (hint instanceof UserConfigurableRule) {
                        ucr = (UserConfigurableRule)hint;
                    }

                    // Make sure the hint is enabled
                    if (ucr != null && !HintsSettings.isEnabled(manager, ucr)) {
                        Preferences p = HintsSettings.getPreferences(manager, ucr, HintsSettings.getCurrentProfileId());
                        HintsSettings.setEnabled(p, true);
                    }

                    List<Hint> hints = new ArrayList<Hint>();
                    if (hint instanceof ErrorRule) {
                        RuleContext context = manager.createRuleContext(pr, language, -1, -1, -1);
                        // It's an error!
                        // Create a hint registry which contains ONLY our hint (so other registered
                        // hints don't interfere with the test)
                        Map<Object, List<? extends ErrorRule>> testHints = new HashMap<Object, List<? extends ErrorRule>>();
                        if (hint.appliesTo(context)) {
                            ErrorRule ErrorRule = (ErrorRule)hint;
                            for (Object key : ErrorRule.getCodes()) {
                                testHints.put(key, Collections.singletonList(ErrorRule));
                            }
                        }
                        manager.setTestingRules(testHints, null, null, null);
                        provider.computeErrors(manager, context, hints, new ArrayList<Error>());
                    } else if (hint instanceof SelectionRule) {
                        SelectionRule rule = (SelectionRule)hint;
                        List<SelectionRule> testHints = new ArrayList<SelectionRule>();
                        testHints.add(rule);

                        manager.setTestingRules(null, null, null, testHints);

                        if (caretLine != null) {
                            int start = text.indexOf(caretLine.toString());
                            int end = start+caretLine.length();
                            RuleContext context = manager.createRuleContext(pr, language, -1, start, end);
                            provider.computeSelectionHints(manager, context, hints, start, end);
                        }
                    } else {
                        assertTrue(hint instanceof AstRule && ucr != null);
                        AstRule AstRule = (AstRule)hint;
                        // Create a hint registry which contains ONLY our hint (so other registered
                        // hints don't interfere with the test)
                        Map<Object, List<? extends AstRule>> testHints = new HashMap<Object, List<? extends AstRule>>();
                        RuleContext context = manager.createRuleContext(pr, language, caretOffset, -1, -1);
                        if (hint.appliesTo(context)) {
                            for (Object nodeId : AstRule.getKinds()) {
                                testHints.put(nodeId, Collections.singletonList(AstRule));
                            }
                        }
                        if (HintsSettings.getSeverity(manager, ucr) == HintSeverity.CURRENT_LINE_WARNING) {
                            manager.setTestingRules(null, Collections.EMPTY_MAP, testHints, null);
                            provider.computeSuggestions(manager, context, hints, caretOffset);
                        } else {
                            manager.setTestingRules(null, testHints, null, null);
                            context.caretOffset = -1;
                            provider.computeHints(manager, context, hints);
                        }
                    }

                    result[0] = new ComputedHints(pr, hints, caretOffset);
                } finally {
                    if (originalDocumentContent != null) {
                        assert document != null;
                        document.remove(0, document.getLength());
                        document.insertString(0, originalDocumentContent, null);
                    }
                }
            }
        };

        if (classPathsForTest == null || classPathsForTest.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
        }

        return result[0];
    }

    protected void checkHints(NbTestCase test, Rule hint, String relFilePath, String caretLine) throws Exception {
        findHints(test, hint, relFilePath, null, caretLine);
    }

    protected void checkHints(Rule hint, String relFilePath,
            String selStartLine, String selEndLine) throws Exception {
        FileObject fo = getTestFile(relFilePath);
        String text = read(fo);

        assert selStartLine != null;
        assert selEndLine != null;

        int selStartOffset = -1;
        int lineDelta = selStartLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selStartLine = selStartLine.substring(0, lineDelta) + selStartLine.substring(lineDelta + 1);
        int lineOffset = text.indexOf(selStartLine);
        assertTrue(lineOffset != -1);

        selStartOffset = lineOffset + lineDelta;

        int selEndOffset = -1;
        lineDelta = selEndLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selEndLine = selEndLine.substring(0, lineDelta) + selEndLine.substring(lineDelta + 1);
        lineOffset = text.indexOf(selEndLine);
        assertTrue(lineOffset != -1);

        selEndOffset = lineOffset + lineDelta;

        String caretLine = text.substring(selStartOffset, selEndOffset) + "^";

        checkHints(this, hint, relFilePath, caretLine);
    }

    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, Rule hint, FileObject fileObject, String caretLine) throws Exception {
        findHints(test, hint, null, fileObject, caretLine);
    }

    protected String getGoldenFileSuffix() {
        return "";
    }

    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, fileObject, caretLine);
        ParserResult info = r.info;
        List<Hint> result = r.hints;
        int caretOffset = r.caretOffset;

        String annotatedSource = annotateHints((BaseDocument) info.getSnapshot().getSource().getDocument(true), result, caretOffset);

        if (fileObject != null) {
            assertDescriptionMatches(fileObject, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        } else {
            assertDescriptionMatches(relFilePath, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        }
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String selStartLine, String selEndLine, String fixDesc) throws Exception {
        FileObject fo = getTestFile(relFilePath);
        String text = read(fo);

        assert selStartLine != null;
        assert selEndLine != null;

        int selStartOffset = -1;
        int lineDelta = selStartLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selStartLine = selStartLine.substring(0, lineDelta) + selStartLine.substring(lineDelta + 1);
        int lineOffset = text.indexOf(selStartLine);
        assertTrue(lineOffset != -1);

        selStartOffset = lineOffset + lineDelta;

        int selEndOffset = -1;
        lineDelta = selEndLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selEndLine = selEndLine.substring(0, lineDelta) + selEndLine.substring(lineDelta + 1);
        lineOffset = text.indexOf(selEndLine);
        assertTrue(lineOffset != -1);

        selEndOffset = lineOffset + lineDelta;

        String caretLine = text.substring(selStartOffset, selEndOffset) + "^";

        applyHint(test, hint, relFilePath, caretLine, fixDesc);
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String caretLine, String fixDesc) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, null, caretLine);
        ParserResult info = r.info;

        HintFix fix = findApplicableFix(r, fixDesc);
        assertNotNull(fix);

        String fixed = null;
        Document doc = info.getSnapshot().getSource().getDocument(true);
        String originalDocumentContent = doc.getText(0, doc.getLength());
        try {
            if (fix.isInteractive() && fix instanceof PreviewableFix) {
                PreviewableFix preview = (PreviewableFix)fix;
                assertTrue(preview.canPreview());
                EditList editList = preview.getEditList();
                editList.applyToDocument((BaseDocument) doc);
            } else {
                fix.implement();
            }

            fixed = doc.getText(0, doc.getLength());
        } finally {
            doc.remove(0, doc.getLength());
            doc.insertString(0, originalDocumentContent, null);
        }

        assertDescriptionMatches(relFilePath, fixed, true, ".fixed");
    }

    @SuppressWarnings("unchecked")
    protected final void ensureRegistered(AstRule hint) throws Exception {
        org.netbeans.modules.csl.core.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
        assertNotNull(language.getHintsProvider());
        GsfHintsManager hintsManager = language.getHintsManager();
        Map<?, List<? extends AstRule>> hints = (Map<?, List<? extends AstRule>>)hintsManager.getHints();
        Set<?> kinds = hint.getKinds();
        for (Object nodeType : kinds) {
            List<? extends AstRule> rules = hints.get(nodeType);
            assertNotNull(rules);
            boolean found = false;
            for (AstRule rule : rules) {
                if (rule.getClass() == hint.getClass()) {
                    found  = true;
                    break;
                }
            }

            assertTrue(found);
        }
    }

    @SuppressWarnings("unchecked")
    protected final void ensureRegistered(ErrorRule hint) throws Exception {
        org.netbeans.modules.csl.core.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
        assertNotNull(language.getHintsProvider());
        GsfHintsManager hintsManager = language.getHintsManager();
        Map<?, List<? extends ErrorRule>> hints = (Map<?, List<? extends ErrorRule>>)hintsManager.getErrors();
        Set<?> kinds = hint.getCodes();
        for (Object codes : kinds) {
            List<? extends ErrorRule> rules = hints.get(codes);
            assertNotNull(rules);
            boolean found = false;
            for (ErrorRule rule : rules) {
                if (rule.getClass() == hint.getClass()) {
                    found  = true;
                    break;
                }
            }

            assertTrue(found);
        }
    }
//    public void ensureRegistered(AstRule hint) throws Exception {
//        Map<Integer, List<AstRule>> hints = JsRulesManager.getInstance().getHints();
//        Set<Integer> kinds = hint.getKinds();
//        for (int nodeType : kinds) {
//            List<AstRule> rules = hints.get(nodeType);
//            assertNotNull(rules);
//            boolean found = false;
//            for (AstRule rule : rules) {
//                if (rule instanceof BlockVarReuse) {
//                    found  = true;
//                    break;
//                }
//            }
//
//            assertTrue(found);
//        }
//    }

    private HintFix findApplicableFix(ComputedHints r, String text) {
        boolean substringMatch = true;
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length()-1);
            substringMatch = false;
        }
        int caretOffset = r.caretOffset;
        for (Hint desc : r.hints) {
            int start = desc.getRange().getStart();
            int end = desc.getRange().getEnd();
            OffsetRange range = new OffsetRange(start, end);
            if (range.containsInclusive(caretOffset) || caretOffset == range.getEnd()+1) { // special case for wrong JRuby offsets
                // Optionally make sure the text is the one we're after such that
                // tests can disambiguate among multiple fixes
                // special case for wrong JRuby offsets
                // Optionally make sure the text is the one we're after such that
                // tests can disambiguate among multiple fixes
                List<HintFix> list = desc.getFixes();
                assertNotNull(list);
                for (HintFix fix : list) {
                    if (text == null ||
                            (substringMatch && fix.getDescription().indexOf(text) != -1) ||
                            (!substringMatch && fix.getDescription().equals(text))) {
                        return fix;
                    }
                }
            }
        }

        return null;
    }

    protected static class ComputedHints {
        ComputedHints(ParserResult info, List<Hint> hints, int caretOffset) {
            this.info = info;
            this.hints = hints;
            this.caretOffset = caretOffset;
        }

        @Override
        public String toString() {
            return "ComputedHints(caret=" + caretOffset + ",info=" + info + ",hints=" + hints + ")";
        }

        public ParserResult info;
        public List<Hint> hints;
        public int caretOffset;
    }

    ////////////////////////////////////////////////////////////////////////////
    // DeclarationFinder
    ////////////////////////////////////////////////////////////////////////////
    protected DeclarationFinder getFinder() {
        DeclarationFinder finder = getPreferredLanguage().getDeclarationFinder();
        if (finder == null) {
            fail("You must override getFinder()");
        }

        return finder;
    }

    protected DeclarationLocation findDeclaration(String relFilePath, final String caretLine) throws Exception {
        Source testSource = getTestSource(getTestFile(relFilePath));

        final int caretOffset = getCaretOffset(testSource.createSnapshot().getText().toString(), caretLine);
        enforceCaretOffset(testSource, caretOffset);

        final DeclarationLocation [] location = new DeclarationLocation[] { null };
        UserTask task = new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result r = resultIterator.getParserResult();
                assertTrue(r instanceof ParserResult);
                ParserResult pr = (ParserResult) r;

                DeclarationFinder finder = getFinder();
                location[0] = finder.findDeclaration(pr, caretOffset);
            }
        };

        if (classPathsForTest == null || classPathsForTest.isEmpty()) {
            ParserManager.parse(Collections.singleton(testSource), task);
        } else {
            Future<Void> future = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
            if (!future.isDone()) {
                future.get();
            }
        }

        return location[0];
    }

    protected void checkDeclaration(String relFilePath, String caretLine, URL url) throws Exception {
        DeclarationLocation location = findDeclaration(relFilePath, caretLine);
        if (location == DeclarationLocation.NONE) {
            // if we dont found a declaration, bail out.
            assertTrue("DeclarationLocation.NONE", false);
        }

        assertEquals(location.getUrl(), url);
    }

    protected void checkDeclaration(String relFilePath, String caretLine, String declarationLine) throws Exception {
        DeclarationLocation location = findDeclaration(relFilePath, caretLine);
        if (location == DeclarationLocation.NONE) {
            // if we dont found a declaration, bail out.
            fail("DeclarationLocation.NONE");
        }

        int caretDelta = declarationLine.indexOf('^');
        assertTrue(caretDelta != -1);
        declarationLine = declarationLine.substring(0, caretDelta) + declarationLine.substring(caretDelta + 1);
        String text = readFile(location.getFileObject());
        int lineOffset = text.indexOf(declarationLine);
        assertTrue(lineOffset != -1);
        int caretOffset = lineOffset + caretDelta;

        if (caretOffset != location.getOffset()) {
            fail("Offset mismatch (expected " + caretOffset + " vs. actual " + location.getOffset() + ": got " + getSourceWindow(text, location.getOffset()));
        }
    }

    protected void checkDeclaration(String relFilePath, String caretLine, String file, int offset) throws Exception {
        DeclarationLocation location = findDeclaration(relFilePath, caretLine);
        if (location == DeclarationLocation.NONE) {
            // if we dont found a declaration, bail out.
            assertTrue("DeclarationLocation.NONE", false);
        }

        assertEquals(file, location.getFileObject() != null ? location.getFileObject().getNameExt() : "<none>");
        assertEquals(offset, location.getOffset());
    }

    protected final void enforceCaretOffset(Source source, int offset) {
        try {
            Method m = GsfUtilities.class.getDeclaredMethod("setLastKnowCaretOffset", Source.class, Integer.TYPE);
            m.setAccessible(true);
            m.invoke(null, source, offset);
        } catch (Exception e) {
            throw new IllegalStateException("Can't enforce caret offset on " + source, e);
        }
    }

    protected Map<String, ClassPath> createClassPathsForTest() {
        return null;
    }

    protected boolean classPathContainsBinaries() {
        return false;
    }

    protected boolean cleanCacheDir() {
        return true;
    }

    private class TestClassPathProvider implements ClassPathProvider {
        public TestClassPathProvider() {

        }

        public ClassPath findClassPath(FileObject file, String type) {
            Map<String, ClassPath> map = classPathsForTest;

            if (map != null) {
                return map.get(type);
            } else {
                return null;
            }
        }
    } // End of TestClassPathProvider class

    private class TestPathRecognizer extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return CslTestBase.this.getPreferredLanguage().getSourcePathIds();
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return CslTestBase.this.getPreferredLanguage().getLibraryPathIds();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return CslTestBase.this.getPreferredLanguage().getBinaryLibraryPathIds();
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.singleton(CslTestBase.this.getPreferredMimeType());
        }

    } // End of TestPathRecognizer class

    private static final class Waiter extends Handler {

        private final CountDownLatch latch;

        private final boolean binaries;

        public Waiter(boolean binaries) {
            latch = new CountDownLatch(binaries ? 2 : 1);
            this.binaries = binaries;
        }

        public void waitForScanToFinish() {
            try {
                latch.await(600000, TimeUnit.MILLISECONDS);
                if (latch.getCount() > 0) {
                    fail("Waiting for classpath scanning to finish timed out");
                }
            } catch (InterruptedException ex) {
                fail("Waiting for classpath scanning to finish was interrupted");
            }
        }

        @Override
        public void publish(LogRecord record) {
            String msg = record.getMessage();
            if ("scanSources".equals(msg) || (binaries && "scanBinary".equals(msg))) {
                latch.countDown();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    } // End of Waiter class

    private static class CaretLineOffset {
        private final int offset;
        private final String caretLine;

        public CaretLineOffset(int offset, String caretLine) {
            this.offset = offset;
            this.caretLine = caretLine;
        }

    }
}
