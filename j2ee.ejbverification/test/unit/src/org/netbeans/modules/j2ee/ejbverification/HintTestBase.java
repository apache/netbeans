/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbverification;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.providers.code.CodeHintProviderImpl;
import org.netbeans.modules.java.hints.providers.code.FSWrapper;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.ClassWrapper;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl.Accessor;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.SyntheticFix;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.MimeTypes;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Copy/pasted and refactored HintTestBase written by Honza Lahoda.
 *
 * A support class for writing a test for a Java Hint. A test verifying that correct
 * warnings are produced should look like:
 * <pre>
 * HintTestBase.create()
 *         .input("&lt;input Java source code>")
 *         .run(&lt;class containg the hint>)
 *         .assertWarnings("&lt;required warning(s)>");
 * </pre>
 *
 * Note: when verifying that no warnings are produced in a particular situation,
 * do not pass any warnings to the {@code assertWarnings} method.
 *
 * A test verifying that a hint's transformation is correct:
 * <pre>
 * HintTestBase.create()
 *         .input("&lt;input Java source code>")
 *         .run(&lt;class containg the hint>)
 *         .findWarning("&lt;a warning produce by the hint>")
 *         .applyFix() //fill apply the only fix in the given ErrorDescription
 *         .assertCompilable()
 *         .assertOutput("&lt;output Java source code>");
 * </pre>
 *
 * All the tests run under the {@code test} branding, which allows to specify test values
 * for bundle keys for warning and fix in {@code Bundle_test.properties}, to isolate the
 * test from changes in the production {@code Bundle.properties}.
 *
 * @author lahvac
 */
public class HintTestBase {

    private static final Logger INDEXING_LOGGER = /* RepositoryUpdater.UI_LOGGER */ Logger.getLogger("org.netbeans.ui.indexing");
    static {
        INDEXING_LOGGER.setLevel(Level.WARNING);
    }

    private final File workDir;
    private final FileObject sourceRoot;
    private final FileObject buildRoot;
    private final FileObject cache;
    private final Preferences testPreferences;
    private final HintsSettings hintSettings;
    private final List<FileObject> checkCompilable = new ArrayList<FileObject>();
    private String sourceLevel = "1.5";
    private Character caretMarker;
    private FileObject testFile;
    private int caret = -1;
    private ClassPath sourcePath;
    private ClassPath compileClassPath = ClassPathSupport.createClassPath(new URL[0]);

    private HintTestBase(FileObject sourceRoot) throws Exception {
        Set<String> amt = MimeTypes.getAllMimeTypes();
        if (amt == null) {
            amt = new HashSet<String>();
        } else {
            amt = new HashSet<String>(amt);
        }
        amt.add("text/x-java");
        MimeTypes.setAllMimeTypes(amt);
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();

        TreeLoader.DISABLE_CONFINEMENT_TEST = true;
        testPreferences = new TempPreferences();
        hintSettings = new HintsSettings() {
            @Override public boolean isEnabled(HintMetadata hint) {
                return true;
            }
            @Override public void setEnabled(HintMetadata hint, boolean value) {
                throw new UnsupportedOperationException("Not supported.");
            }
            @Override public Preferences getHintPreferences(HintMetadata hint) {
                return testPreferences;
            }
            @Override public Severity getSeverity(HintMetadata hint) {
                return hint.severity;
            }
            @Override public void setSeverity(HintMetadata hint, Severity severity) {
                throw new UnsupportedOperationException("Not supported.");
            }
        };

        workDir = getWorkDir();
        FileUtil.refreshFor(workDir);

        FileObject wd = FileUtil.toFileObject(workDir);
        
        assertNotNull(wd);

        this.sourceRoot = sourceRoot;
        buildRoot = FileUtil.createFolder(wd, "build");
        cache = FileUtil.createFolder(wd, "cache");

        CacheFolder.setCacheFolder(cache);

        NbBundle.setBranding("test");

        sourcePath = ClassPathSupport.createClassPath(sourceRoot);
        
        Main.initializeURLFactory();
    }

    /**Bootstraps the test framework.
     *
     * @return the test framework - call more methods on it to set-up a test, then call {@code run} method and assert results.
     */
    public static HintTestBase create(FileObject sourceRoot) throws Exception {
        return new HintTestBase(sourceRoot);
    }

    /**A character to use as a marker of a caret in the input code. The caret position
     * during the run method will be set to the position of this character in the first input file.
     *
     * @param c a caret marker
     * @return itself
     */
    public HintTestBase setCaretMarker(char c) {
        this.caretMarker = c;
        return this;
    }

    /**Use the specified {@link java.net.URL}s as compile classpath while parsing
     * the Java input. The {@link java.net.URL}s need to be "folder" {@link java.net.URL}s,
     * ready to be passed to {@link ClassPathSupport#createClassPath(java.net.URL[]) }.
     *
     * @param entries that should become roots of the compile classpath
     * @return itself
     * @see FileUtil#urlForArchiveOrDir(java.io.File)
     * @see FileUtil#getArchiveRoot(java.net.URL)
     */
    public HintTestBase classpath(URL... entries) {
        compileClassPath = ClassPathSupport.createClassPath(entries);
        return this;
    }

    /**Create a test file. Equivalent to calling {@code input("test/Test.java", code, true)}.
     *
     * @param code the content of the newly created test file
     * @return itself
     */
    public HintTestBase input(String code) throws Exception {
        return input("test/Test.java", code, true);
    }

    /**Create a test file. Equivalent to calling {@code input("test/Test.java", code, compilable)}.
     *
     * @param code the content of the newly created test file
     * @param compilable if true, it will be verified that the file does not contain
     *                   compilation errors before the hint is run on it
     * @return itself
     */
    public HintTestBase input(String code, boolean compilable) throws Exception {
        return input("test/Test.java", code, compilable);
    }

    /**Create a test file. Equivalent to calling {@code input(fileName, code, true)}.
     *
     * @param fileName a relative file name of the newly created file from a (automatically created) source root
     * @param code the content of the newly created test file
     * @return itself
     */
    public HintTestBase input(String fileName, String code) throws Exception {
        return input(fileName, code, true);
    }
    
    /**Create a test file. Any number of files can be created for one test, but the hint
     * will be run only on the first one.
     *
     * @param fileName a relative file name of the newly created file from a (automatically created) source root
     * @param code the content of the newly created test file
     * @param compilable if true, it will be verified that the file does not contain
     *                   compilation errors before the hint is run on it
     * @return itself
     */
    public HintTestBase input(String fileName, String code, boolean compilable) throws Exception {
        int caret = -1;

        if (caretMarker != null && testFile == null) {
            caret = code.indexOf(caretMarker);

            assertNotSame("A caret location must be specified", -1, caret);

            code = code.substring(0, caret) + code.substring(caret + 1);
        }

        FileObject file = FileUtil.createData(sourceRoot, fileName);

        copyStringToFile(file, code);

        if (compilable) {
            checkCompilable.add(file);
        }

        if (testFile == null) {
            testFile = file;
            this.caret = caret;
        }

        return this;
    }

    private void ensureCompilable(FileObject file) throws IOException, AssertionError, IllegalArgumentException {
        CompilationInfo info = parse(file);

        assertNotNull(info);

        for (Diagnostic d : info.getDiagnostics()) {
            if (d.getKind() == Diagnostic.Kind.ERROR)
                throw new AssertionError(d.getLineNumber() + ":" + d.getColumnNumber() + " " + d.getMessage(null));
        }
    }

    /**Sets a source level for all Java files used in this test.
     *
     * @param sourceLevel the source level to use while parsing Java files
     * @return itself
     */
    public HintTestBase sourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
        return this;
    }

    /**Sets a preference that will be visible to the hint.
     *
     * @param preferencesKey a key for the preferences
     * @param value the value to set
     * @return itself
     */
    public HintTestBase preference(String preferencesKey, String value) {
        this.testPreferences.put(preferencesKey, value);
        return this;
    }

    /**Sets a preference that will be visible to the hint.
     *
     * @param preferencesKey a key for the preferences
     * @param value the value to set
     * @return itself
     */
    public HintTestBase preference(String preferencesKey, int value) {
        this.testPreferences.putInt(preferencesKey, value);
        return this;
    }

    /**Sets a preference that will be visible to the hint.
     *
     * @param preferencesKey a key for the preferences
     * @param value the value to set
     * @return itself
     */
    public HintTestBase preference(String preferencesKey, boolean value) {
        this.testPreferences.putBoolean(preferencesKey, value);
        return this;
    }
    
    /**Runs the given hint(s) on the first file written by a {@code input} method.
     *
     * @param hint all hints in this class will be run on the file
     * @return a wrapper over the hint output that allows verifying results of the hint
     */
    public HintOutput run(Class<?> hint) throws Exception {
        return run(hint, null);
    }

    /**Runs the given hint(s) on the first file written by a {@code input} method.
     * Runs only hints with the specified {@code hintCode}. Null hintCode includes
     * all hints from the class
     *
     * @param hint all hints in this class will be run on the file
     * @param hintCode if not {@code null}, only hints with the same id will be run
     * @return a wrapper over the hint output that allows verifying results of the hint
     */
    public HintOutput run(Class<?> hint, String hintCode) throws Exception {
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.toURL(), null);
        
        for (FileObject file : checkCompilable) {
            ensureCompilable(file);
        }
        
        Map<HintMetadata, Collection<HintDescription>> hints = new HashMap<HintMetadata, Collection<HintDescription>>();
        List<ClassWrapper> found = new ArrayList<ClassWrapper>();

        for (ClassWrapper w : FSWrapper.listClasses()) {
            if (hint.getCanonicalName().equals(w.getName().replace('$', '.'))) {
                found.add(w);
            }
        }

        assertFalse(found.isEmpty());

        for (ClassWrapper w : found) {
            CodeHintProviderImpl.processClass(w, hints);
        }

        List<HintDescription> total = new LinkedList<HintDescription>();
        final Set<ErrorDescription> requiresJavaFix = Collections.newSetFromMap(new IdentityHashMap<ErrorDescription, Boolean>());

        for (final Entry<HintMetadata, Collection<HintDescription>> e : hints.entrySet()) {
            if (null != hintCode && !e.getKey().id.equals(hintCode)) {
                continue;
            }
            if (   e.getKey().options.contains(Options.NO_BATCH)
                || e.getKey().options.contains(Options.QUERY)
                || e.getKey().kind == Kind.ACTION) {
                total.addAll(e.getValue());
                continue;
            }
            for (final HintDescription hd : e.getValue()) {
                total.add(HintDescriptionFactory.create()
                                               .setTrigger(hd.getTrigger())
                                               .setMetadata(e.getKey())
                                               .setAdditionalConstraints(hd.getAdditionalConstraints())
                                               .addOptions(hd.getOptions().toArray(new Options[0]))
                                               .setWorker(new Worker() {
                                                    @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                                                        Collection<? extends ErrorDescription> errors = hd.getWorker().createErrors(ctx);
 
                                                        if (errors != null) {
                                                            for (ErrorDescription ed : errors) {
                                                                requiresJavaFix.add(ed);
                                                            }
                                                        }
                                                        
                                                        return errors;
                                                     }
                                                })
                                              .produce());
            }
        }
        
        CompilationInfo info = parse(testFile);

        assertNotNull(info);

        List<ErrorDescription> result = new ArrayList<ErrorDescription>();

        Handler h = new Handler() {
            @Override public void publish(LogRecord record) {
                if (   record.getLevel().intValue() >= Level.WARNING.intValue()
                    && record.getThrown() != null) {
                    throw new IllegalStateException(record.getThrown());
                }
            }
            @Override public void flush() { }
            @Override public void close() throws SecurityException { }
        };
        Logger log = Logger.getLogger(Exceptions.class.getName());
        log.addHandler(h);
        Map<HintDescription, List<ErrorDescription>> errors = computeErrors(info, total, new AtomicBoolean());
        log.removeHandler(h);
        for (Entry<HintDescription, List<ErrorDescription>> e : errors.entrySet()) {
            result.addAll(e.getValue());
        }

        Collections.sort(result, ERRORS_COMPARATOR);
        
        Reference<CompilationInfo> infoRef = new WeakReference<CompilationInfo>(info);
        Reference<CompilationUnitTree> cut = new WeakReference<CompilationUnitTree>(info.getCompilationUnit());
        
        info = null;
        
        DEBUGGING_HELPER.add(result);
        NbTestCase.assertGC("noone holds CompilationInfo", infoRef);
        NbTestCase.assertGC("noone holds javac", cut);
        DEBUGGING_HELPER.remove(result);
        
        return new HintOutput(result, requiresJavaFix);
    }
    
    //must keep the error descriptions (and their Fixes through them) in a field
    //so that assertGC is able to provide a useful trace of references:
    private static Set<List<ErrorDescription>> DEBUGGING_HELPER = Collections.newSetFromMap(new IdentityHashMap<List<ErrorDescription>, Boolean>());

    private CompilationInfo parse(FileObject file) throws DataObjectNotFoundException, IllegalArgumentException, IOException {
        DataObject od = DataObject.find(file);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        Document doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource.create(ClasspathInfo.create(file), file);

        assertNotNull("found JavaSource for " + file, js);

        final DeadlockTask bt = new DeadlockTask(Phase.RESOLVED);

        js.runUserActionTask(bt, true);
        
        return bt.info;
    }

    private Map<HintDescription, List<ErrorDescription>> computeErrors(CompilationInfo info, Iterable<? extends HintDescription> hints, AtomicBoolean cancel) {
        return new HintsInvoker(hintSettings, caret, cancel).computeHints(info, new TreePath(info.getCompilationUnit()), hints, new LinkedList<MessageImpl>());
    }

    FileObject getSourceRoot() {
        return sourceRoot;
    }

    private static class TempPreferences extends AbstractPreferences {

        /*private*/Properties properties;

        private TempPreferences() {
            super(null, "");
        }

        private  TempPreferences(TempPreferences parent, String name)  {
            super(parent, name);
            newNode = true;
        }

        protected final String getSpi(String key) {
            return properties().getProperty(key);
        }

        protected final String[] childrenNamesSpi() throws BackingStoreException {
            return new String[0];
        }

        protected final String[] keysSpi() throws BackingStoreException {
            return properties().keySet().toArray(new String[0]);
        }

        protected final void putSpi(String key, String value) {
            properties().put(key,value);
        }

        protected final void removeSpi(String key) {
            properties().remove(key);
        }

        protected final void removeNodeSpi() throws BackingStoreException {}
        protected  void flushSpi() throws BackingStoreException {}
        protected void syncSpi() throws BackingStoreException {
            properties().clear();
        }

        @Override
        public void put(String key, String value) {
            try {
                super.put(key, value);
            } catch (IllegalArgumentException iae) {
                if (iae.getMessage().contains("too long")) {
                    // Not for us!
                    putSpi(key, value);
                } else {
                    throw iae;
                }
            }
        }

        Properties properties()  {
            if (properties == null) {
                properties = new Properties();
            }
            return properties;
        }

        protected AbstractPreferences childSpi(String name) {
            return new TempPreferences(this, name);
        }
    }

    private static final SourceForBinaryQuery.Result EMPTY_SFBQ_RESULT = new Result() {
        private final FileObject[] roots = new FileObject[0];
        @Override public FileObject[] getRoots() {
            return roots;
        }
        @Override public void addChangeListener(ChangeListener l) {}
        @Override public void removeChangeListener(ChangeListener l) {}
    };
    
    private class TestSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            FileObject f = URLMapper.findFileObject(binaryRoot);

            if (buildRoot.equals(f)) {
                return new SourceForBinaryQuery.Result() {
                    public FileObject[] getRoots() {
                        return new FileObject[] {
                            sourceRoot,
                        };
                    }

                    public void addChangeListener(ChangeListener l) {
                    }

                    public void removeChangeListener(ChangeListener l) {
                    }
                };
            }

            return EMPTY_SFBQ_RESULT;
        }

    }

    private static List<URL> bootClassPath;

    private static Logger log = Logger.getLogger(HintTestBase.class.getName());

    private static synchronized List<URL> getBootClassPath() {
        if (bootClassPath == null) {
            try {
                String cp = System.getProperty("sun.boot.class.path");
                List<URL> urls = new ArrayList<URL>();
                String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));

                for (String path : paths) {
                    File f = new File(path);

                    if (!f.canRead())
                        continue;

                    FileObject fo = FileUtil.toFileObject(f);

                    if (FileUtil.isArchiveFile(fo)) {
                        fo = FileUtil.getArchiveRoot(fo);
                    }

                    if (fo != null) {
                        urls.add(fo.getURL());
                    }
                }

                bootClassPath = urls;
            } catch (FileStateInvalidException e) {
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return bootClassPath;
    }
    
    private class TestProxyClassPathProvider implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            try {
            if (ClassPath.BOOT == type) {
                // XXX simpler to use JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries()
                return ClassPathSupport.createClassPath(getBootClassPath().toArray(new URL[0]));
            }

            if (ClassPath.SOURCE == type) {
                return sourcePath;
            }

            if (ClassPath.COMPILE == type) {
                return compileClassPath;
            }

            if (ClassPath.EXECUTE == type) {
                return ClassPathSupport.createClassPath(new FileObject[] {
                    buildRoot
                });
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {

        public String getSourceLevel(FileObject javaFile) {
            return sourceLevel;
        }

    }


    private static class DeadlockTask implements Task<CompilationController> {

        private final Phase phase;
        private CompilationInfo info;

        public DeadlockTask(Phase phase) {
            assert phase != null;
            this.phase = phase;
        }

        public void run( CompilationController info ) {
            try {
                info.toPhase(this.phase);
                this.info = info;
            } catch (IOException ioe) {
                if (log.isLoggable(Level.SEVERE))
                    log.log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }

    }

    /**Encapsulated the output of the hint.
     */
    public final class HintOutput {
        
        private final List<ErrorDescription> errors;
        private final Set<ErrorDescription> requiresJavaFix;

        private HintOutput(List<ErrorDescription> errors, Set<ErrorDescription> requiresJavaFix) {
            this.errors = errors;
            this.requiresJavaFix = requiresJavaFix;

        }

        /**Assert that the hint(s) produced the given warnings. The provided strings
         * should match {@code toString()} results of {@link ErrorDescription}s produced
         * by the hint(s).
         *
         * @param warnings expected {@code toString()} results of {@link ErrorDescription}s produced
         *                 by the hint
         * @return itself
         * @throws AssertionError if the given warnings do not match the actual warnings
         */
        public HintOutput assertWarnings(String... warnings) {
            assertEquals("The warnings provided by the hint do not match expected warnings.", Arrays.toString(warnings), errors.toString());

            return this;
        }

        /**Assert that the hint(s) produced warnings include the given warnings. The provided strings
         * should match {@code toString()} results of {@link ErrorDescription}s produced
         * by the hint(s).
         *
         * @param warnings expected {@code toString()} results of {@link ErrorDescription}s produced
         *                 by the hint
         * @return itself
         * @throws AssertionError if the given warnings do not match the actual warnings
         */
        public HintOutput assertContainsWarnings(String... warnings) {
            Set<String> goldenSet = new HashSet<String>(Arrays.asList(warnings));
            List<String> errorsNames = new LinkedList<String>();

            for (ErrorDescription d : errors) {
                goldenSet.remove(d.toString());
                errorsNames.add(d.toString());
            }
            
            assertTrue("The warnings provided by the hint do not contain expected warnings. Provided warnings: " + errorsNames.toString(), goldenSet.isEmpty());

            return this;
        }

        /**Assert that the hint(s) produced warnings do not include the given warnings. The provided strings
         * should match {@code toString()} results of {@link ErrorDescription}s produced
         * by the hint(s).
         *
         * @param warnings expected {@code toString()} results of {@link ErrorDescription}s produced
         *                 by the hint
         * @return itself
         * @throws AssertionError if the given warnings do not match the actual warnings
         */
        public HintOutput assertNotContainsWarnings(String... warnings) {
            Set<String> goldenSet = new HashSet<String>(Arrays.asList(warnings));
            List<String> errorsNames = new LinkedList<String>();

            boolean fail = false;
            for (ErrorDescription d : errors) {
                if (goldenSet.remove(d.getDescription()))
                    fail = true;
                errorsNames.add(d.toString());
            }
            
            assertFalse("The warnings provided by the hint do not exclude expected warnings. Provided warnings: " + errorsNames.toString(), fail);

            return this;
        }
        
        /**Find a specific warning.
         *
         * @param warning the warning to find - must be equivalent to {@code toString()}
         *                results of the {@link ErrorDescription}.
         * @return a wrapper about the given specific warnings
         * @throws AssertionError if the given warning cannot be found
         */
        public HintWarning findWarning(String warning) {
            ErrorDescription toFix = null;

            for (ErrorDescription d : errors) {
                if (warning.equals(d.toString())) {
                    toFix = d;
                    break;
                }
            }

            assertNotNull("Warning: \"" + warning + "\" not found. All ErrorDescriptions: " + errors.toString(), toFix);

            return new HintWarning(toFix, requiresJavaFix.contains(toFix));
        }
    }

    /**A wrapper over a single warning.
     */
    public final class HintWarning {
        private final ErrorDescription warning;
        private final boolean requiresJavaFix;
        HintWarning(ErrorDescription warning, boolean requiresJavaFix) {
            this.warning = warning;
            this.requiresJavaFix = requiresJavaFix;
        }
        /**Applies the only fix of the current warning. Fails if the given warning
         * does not have exactly one fix.
         *
         * Note this is a destructive operation - the {@link #run(java.lang.Class)} or {@link #applyFix}
         * cannot be run in the future on any object that follows the chain from the same invocation of {@link #create()}.
         *
         * @return a wrapper over resulting source code
         * @throws AssertionError if there is not one fix for the given {@link ErrorDescription}
         */
        public AppliedFix applyFix() throws Exception {
            return applyFix(true);
        }

        AppliedFix applyFix(boolean saveAll) throws Exception {
            assertTrue("Must be computed", warning.getFixes().isComputed());

            List<Fix> fixes = warning.getFixes().getFixes();

            assertEquals(1, fixes.size());

            doApplyFix(fixes.get(0));

            if (saveAll)
                LifecycleManager.getDefault().saveAll();
            
            return new AppliedFix();
        }
        /**Applies the specified fix of the current warning.
         *
         * Note this is a destructive operation - the {@link #run(java.lang.Class)} or {@link #applyFix}
         * cannot be run in the future on any object that follows the chain from the same invocation of {@link #create()}.
         *
         * @param fix {@link Fix#getText() } result of the required fix
         * @return a wrapper over resulting source code
         * @throws AssertionError if the fix cannot be found
         */
        public AppliedFix applyFix(String fix) throws Exception {
            assertTrue("Must be computed", warning.getFixes().isComputed());

            List<Fix> fixes = warning.getFixes().getFixes();
            List<String> fixNames = new LinkedList<String>();
            Fix toApply = null;

            for (Fix f : fixes) {
                if (fix.equals(f.getText())) {
                    toApply = f;
                }

                fixNames.add(f.getText());
            }

            assertNotNull("Cannot find fix to invoke: " + fixNames.toString(), toApply);

            doApplyFix(toApply);
            
            LifecycleManager.getDefault().saveAll();

            return new AppliedFix();
        }
        private void doApplyFix(Fix f) throws Exception {
            Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
            preferences.putBoolean("importInnerClasses", true);
            try {
                if (requiresJavaFix) {
                    assertTrue("The fix must be a JavaFix", f instanceof JavaFixImpl);
                    
                    ModificationResult result1 = runJavaFix(((JavaFixImpl) f).jf);
                    ModificationResult result2 = runJavaFix(((JavaFixImpl) f).jf);
                    
                    //ensure the results are the same:
                    assertEquals("The fix must be repeatable", result1.getModifiedFileObjects(), result2.getModifiedFileObjects());
                    
                    for (FileObject file : result1.getModifiedFileObjects()) {
                        assertEquals("The fix must be repeatable", result1.getResultingSource(file), result2.getResultingSource(file));
                    }
                    
                    result1.commit();
                } else {
                    f.implement();
                }
            } finally {
                preferences.remove("importInnerClasses");
            }
        }
        private ModificationResult runJavaFix(final JavaFix jf) throws IOException {
            FileObject file = Accessor.INSTANCE.getFile(jf);
            JavaSource js = JavaSource.forFileObject(file);
            final Map<FileObject, List<Difference>> changes = new HashMap<FileObject, List<Difference>>();

            ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy wc) throws Exception {
                    if (wc.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        return;
                    }

                    Map<FileObject, byte[]> resourceContentChanges = new HashMap<FileObject, byte[]>();
                    Accessor.INSTANCE.process(jf, wc, true, resourceContentChanges, /*Ignored for now:*/new ArrayList<RefactoringElementImplementation>());
                    BatchUtilities.addResourceContentChanges(resourceContentChanges, changes);
                    
                }
            });
            
            changes.putAll(JavaSourceAccessor.getINSTANCE().getDiffsFromModificationResult(mr));
            
            return JavaSourceAccessor.getINSTANCE().createModificationResult(changes, Collections.<Object, int[]>emptyMap());
        }
        /**Verifies that the current warning provides the given fixes.
         *
         * @param fixes the {@link Fix#getText() } of the expected fixes
         * @return itself
         * @throws AssertionError if the expected fixes do not match the provided fixes
         * @since 1.1
         */
        public HintWarning assertFixes(String... expectedFixes) throws Exception {
            assertTrue("Must be computed", warning.getFixes().isComputed());

            List<String> fixNames = new LinkedList<String>();

            for (Fix f : warning.getFixes().getFixes()) {
                if (f instanceof SyntheticFix) continue;
                fixNames.add(f.getText());
            }

            assertEquals("Fixes for the current warning do not match the expected fixes. All fixes: " + fixNames.toString(), Arrays.asList(expectedFixes), fixNames);

            return this;
        }
    }

    /**A wrapper over result after applying a fix.
     */
    public final class AppliedFix {
        /**Require that the result is compilable. Equivalent to {@code assertCompilable("test/Test.java")}
         *
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertCompilable() throws Exception {
            return assertCompilable("test/Test.java");
        }
        /**Require that the given resulting file is compilable.
         *
         * @param fileName the name of the file that should be verified
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertCompilable(String fileName) throws Exception {
            FileObject toCheck = sourceRoot.getFileObject(fileName);

            assertNotNull(toCheck);

            ensureCompilable(toCheck);
            return this;
        }
        /**Verify the content of the resulting file. Equivalent to {@code assertOutput("test/Test.java")}.
         *
         * This method will "normalize" whitespaces in the file: generally, all
         * whitespaces are reduced to a single space both in the given code and
         * the code read from the file, before the comparison.
         *
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the file does not have the correct content
         */
        public AppliedFix assertOutput(String code) throws Exception {
            return assertOutput("test/Test.java", code);
        }
        /**Verify the content of the given resulting file.
         *
         * This method will "normalize" whitespaces in the file: generally, all
         * whitespaces are reduced to a single space both in the given code and
         * the code read from the file, before the comparison.
         *
         * @param fileName the name of the file that should be verified
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the file does not have the correct content
         */
        public AppliedFix assertOutput(String fileName, String code) throws Exception {
            FileObject toCheck = sourceRoot.getFileObject(fileName);

            assertNotNull("Required file: " + fileName + " not found", toCheck);

            DataObject toCheckDO = DataObject.find(toCheck);
            EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
            Document toCheckDocument = ec.openDocument();

            String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());

            //ignore whitespaces:
            realCode = reduceWhitespaces(realCode);

            assertEquals("The output code does not match the expected code.", reduceWhitespaces(code), realCode);

            return this;
        }
        
        private String reduceWhitespaces(String str) {
            StringBuilder result = new StringBuilder();
            int i = 0;
            boolean wasWhitespace = false;
            
            while (i < str.length()) {
                int codePoint = str.codePointAt(i);
                
                if (Character.isWhitespace(codePoint)) {
                    if (!wasWhitespace) {
                        result.append(" ");
                        wasWhitespace = true;
                    }
                } else {
                    result.appendCodePoint(codePoint);
                    wasWhitespace = false;
                }
                i += Character.charCount(codePoint);
            }
            
            return result.toString();
        }
        
        /**Verify the content of the resulting file. Equivalent to {@code assertVerbatimOutput("test/Test.java")}.
         *
         * This method will compare the content of the file exactly with the provided
         * code.
         *
         * @param fileName the name of the file that should be verified
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertVerbatimOutput(String code) throws Exception {
            return assertVerbatimOutput("test/Test.java", code);
        }
        /**Verify the content of the given resulting file.
         *
         * This method will compare the content of the file exactly with the provided
         * code.
         *
         * @param fileName the name of the file that should be verified
         * @param code expected content of the resulting file.
         * @return the wrapper itself
         * @throws AssertionError if the result is not compilable
         */
        public AppliedFix assertVerbatimOutput(String fileName, String code) throws Exception {
            FileObject toCheck = sourceRoot.getFileObject(fileName);

            assertNotNull(toCheck);

            DataObject toCheckDO = DataObject.find(toCheck);
            EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
            Document toCheckDocument = ec.openDocument();

            String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());

            assertEquals("The output code does not match the expected code.", code, realCode);

            return this;
        }

        /**Return code after the fix has been applied.
         *
         * @return the code after the fix has been applied
         */
        public String getOutput() throws Exception {
            return getOutput("test/Test.java");
        }

        /**Return code after the fix has been applied.
         *
         * @param fileName file for which the code should be returned
         * @return the code after the fix has been applied
         */
        public String getOutput(String fileName) throws Exception {
            FileObject toCheck = sourceRoot.getFileObject(fileName);

            assertNotNull(toCheck);

            DataObject toCheckDO = DataObject.find(toCheck);
            EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
            Document toCheckDocument = ec.openDocument();

            return toCheckDocument.getText(0, toCheckDocument.getLength());
        }
    }

    private static final Comparator<ErrorDescription> ERRORS_COMPARATOR = new Comparator<ErrorDescription> () {

        public int compare (ErrorDescription e1, ErrorDescription e2) {
            return e1.getRange ().getBegin ().getOffset () - e2.getRange ().getBegin ().getOffset ();
        }
    };

    //workdir computation (copied from NbTestCase):
    private static File getWorkDir() throws IOException {
        // now we have path, so if not available, create workdir
        File workdir = FileUtil.normalizeFile(new File(getWorkDirPath()));
        if (workdir.exists()) {
            if (!workdir.isDirectory()) {
                // work dir exists, but is not directory - this should not happen
                // trow exception
                throw new IOException("workdir exists, but is not a directory, workdir = " + workdir);
            } else {
                // everything looks correctly, return the path
                return workdir;
            }
        } else {
            // we need to create it
            boolean result = workdir.mkdirs();
            if (result == false) {
                // mkdirs() failed - throw an exception
                throw new IOException("workdir creation failed: " + workdir);
            } else {
                // everything looks ok - return path
                return workdir;
            }
        }
    }

    private static String getWorkDirPath() {
        StackTraceElement caller = null;
        boolean seenItself = false;
        
        for (StackTraceElement e : new Exception().getStackTrace()) {
            if (HintTestBase.class.getName().equals(e.getClassName())) seenItself = true;
            if (seenItself && !HintTestBase.class.getName().equals(e.getClassName())) {
                caller = e;
                break;
            }
        }
        
        String name = caller != null ? caller.getMethodName() : "unknownTest";
        // start - PerformanceTestCase overrides getName() method and then
        // name can contain illegal characters
        String osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Windows")) {
            char ntfsIllegal[] ={'"','/','\\','?','<','>','|',':'};
            for (int i=0; i<ntfsIllegal.length; i++) {
                name = name.replace(ntfsIllegal[i], '~');
            }
        }
        // end
        
        final String workDirPath = getWorkDirPathFromManager();
        
        // #94319 - shorten workdir path if the following is too long
        // "Manager.getWorkDirPath()+File.separator+getClass().getName()+File.separator+name"
        int len1 = workDirPath.length();
        String clazz = caller != null ? caller.getClassName() : "unknown.Class";
        int len2 = clazz.length();
        int len3 = name.length();
        
        int tooLong = Integer.getInteger("nbjunit.too.long", 100);
        if (len1 + len2 + len3 > tooLong) {
            clazz = abbrevDots(clazz);
            len2 = clazz.length();
        }

        if (len1 + len2 + len3 > tooLong) {
            name = abbrevCapitals(name);
        }
        
        String p = workDirPath + File.separator + clazz + File.separator + name;
        String realP;
        
        for (int i = 0; ; i++) {
            realP = i == 0 ? p : p + "-" + i;
            if (usedPaths.add(realP)) {
                break;
            }
        }
        
        return realP;
    }

    private static Set<String> usedPaths = new HashSet<String>();
    
    private static String abbrevDots(String dotted) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String item : dotted.split("\\.")) {
            sb.append(sep);
            sb.append(item.charAt(0));
            sep = ".";
        }
        return sb.toString();
    }

    private static String abbrevCapitals(String name) {
        if (name.startsWith("test")) {
            name = name.substring(4);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                sb.append(Character.toLowerCase(name.charAt(i)));
            }
        }
        return sb.toString();
    }

    private static final String JUNIT_PROPERTIES_FILENAME = "junit.properties";
    private static final String JUNIT_PROPERTIES_LOCATION_PROPERTY = "junit.properties.file";
    private static final String NBJUNIT_WORKDIR = "nbjunit.workdir";
    
    private static String getWorkDirPathFromManager() {
        String path = System.getProperty(NBJUNIT_WORKDIR);
                
        if (path == null) {            
            // try to get property from user's settings
            path = readProperties().getProperty(NBJUNIT_WORKDIR);
        }
        if (path != null) {
            path = path.replace('/', File.separatorChar);
        } else {
            // Fallback value, guaranteed to be defined.
            path = System.getProperty("java.io.tmpdir") + File.separatorChar + "tests-" + System.getProperty("user.name");
        }
        return path;
    }

    private static Properties readProperties() {
        Properties result = new Properties();
        try {
            File propFile = getPreferencesFile();
            FileInputStream is = new FileInputStream(propFile);
            try {
                result.load(is);
            } finally {
                is.close();
            }
        }  catch (IOException e) {
        }
        
        return result;
    }

    private static File getPreferencesFile() {
        String junitPropertiesLocation = System.getProperty(JUNIT_PROPERTIES_LOCATION_PROPERTY);
        if (junitPropertiesLocation != null) {
            File propertyFile = new File(junitPropertiesLocation);
            if (propertyFile.exists()) {
                return propertyFile;
            }
        }
        // property file was not found - lets fall back to defaults
        String home= System.getProperty("user.home");
        return new File(home, JUNIT_PROPERTIES_FILENAME);
    }

    // private method for deleting a file/directory (and all its subdirectories/files)
    private static void deleteFile(File file) throws IOException {
        if (file.isDirectory() && file.equals(file.getCanonicalFile())) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            
        }
        // file is a File :-)
        boolean result = file.delete();
        if (result == false ) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = "+file.getPath());
        }
    }
    
    // private method for deleting every subfiles/subdirectories of a file object
    private static void deleteSubFiles(File file) throws IOException {
        File files[] = file.getCanonicalFile().listFiles();
        if (files != null) {
            for (File f : files) {
                deleteFile(f);
            }
        } else {
            // probably do nothing - file is not a directory
        }
    }

    private static FileObject copyStringToFile (FileObject f, String content) throws Exception {
        OutputStream os = f.getOutputStream();
        os.write(content.getBytes("UTF-8"));
        os.close ();

        return f;
    }

}
