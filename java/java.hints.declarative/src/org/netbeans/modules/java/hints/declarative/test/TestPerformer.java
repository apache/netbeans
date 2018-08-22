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

package org.netbeans.modules.java.hints.declarative.test;

import com.sun.source.util.TreePath;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintRegistry;
import java.util.Map.Entry;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.hints.declarative.test.TestParser.TestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author lahvac
 */
public class TestPerformer {

    @CheckForNull
    public static Map<TestCase, Collection<String>> performTest(FileObject ruleFile, FileObject test, TestCase[] tests, AtomicBoolean cancel) throws Exception {
        try {
            return performTestImpl(ruleFile, test, tests, cancel);
        } finally {
            setData(null, null, null);
        }
    }

    public static String normalize(String text) {
        return text.replaceAll("[ \t\n]+", " ");
    }
    
    private static File createScratchpadDir() throws IOException {
        String userdir = System.getProperty("netbeans.user");

        assert userdir != null;

        File varTmp = new File(new File(new File(userdir), "var"), "tmp");

        varTmp.mkdirs();

        assert varTmp.isDirectory();

        File sp = File.createTempFile("jackpot", "", varTmp);

        sp.delete();

        sp.mkdir();

        assert sp.isDirectory();

        return sp;
    }

    private static Map<TestCase, Collection<String>> performTestImpl(FileObject ruleFile, FileObject test, TestCase[] tests, final AtomicBoolean cancel) throws Exception {
        final List<HintDescription> hints = new LinkedList<HintDescription>();

        for (Collection<? extends HintDescription> descs : DeclarativeHintRegistry.parseHintFile(ruleFile).values()) {
            hints.addAll(descs);
        }
        
        FileObject scratchPad = FileUtil.toFileObject(createScratchpadDir());
        Map<TestCase, Collection<String>> result = new HashMap<TestCase, Collection<String>>();

        for (int cntr = 0; cntr < tests.length; cntr++) {
            FileObject srcRoot = scratchPad.createFolder("src" + cntr);
            FileObject src = FileUtil.createData(srcRoot, "test/Test.java");

            setData(test, srcRoot, tests[cntr].getSourceLevel());

            copyStringToFile(src, tests[cntr].getCode());

            final List<ErrorDescription> errors = new LinkedList<ErrorDescription>();

            JavaSource.forFileObject(src).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);

                    Map<HintDescription, List<ErrorDescription>> sortedByHintDescription = new TreeMap<HintDescription, List<ErrorDescription>>(new Comparator<HintDescription>() {
                        public int compare(HintDescription o1, HintDescription o2) {
                            return hints.indexOf(o1) - hints.indexOf(o2);
                        }
                    });
                    
                    Map<HintDescription, List<ErrorDescription>> computedHints = new HintsInvoker(HintsSettings.getGlobalSettings(), cancel).computeHints(parameter, new TreePath(parameter.getCompilationUnit()), hints, new LinkedList<MessageImpl>());

                    if (computedHints == null || cancel.get()) return;
                    
                    sortedByHintDescription.putAll(computedHints);

                    for (Entry<HintDescription, List<ErrorDescription>> e : sortedByHintDescription.entrySet()) {
                        errors.addAll(e.getValue());
                    }
                }
            }, true);

            if (cancel.get()) return null;
            
            LinkedList<String> currentResults = new LinkedList<String>();

            result.put(tests[cntr],currentResults);

            for (ErrorDescription ed : errors) {
                if (!ed.getFixes().isComputed()) {
                    throw new UnsupportedOperationException();
                }

                for (Fix f : ed.getFixes().getFixes()) {
                    //XXX: this fix is automatically added to all hints that do not have any fixes, filtering it out. Should be done more reliably:
                    if (f.getClass().getName().equals("org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory$TopLevelConfigureFix")) continue;
                    if (f.getClass().getName().equals("org.netbeans.spi.java.hints.ErrorDescriptionFactory$TopLevelConfigureFix")) continue;
                    currentResults.add(getFixResult(src, f));
                }

                if (currentResults.isEmpty()) {
                    currentResults.add(ed.getDescription() + ":" + ed.getRange().getText() + "\n");
                }
            }
        }

        //intentionally keeping the directory in case an exception occurs, to
        //simplify error diagnostics
        scratchPad.delete();

        return result;
    }

    /**
     * Copies a string to a specified file.
     *
     * @param f the {@link FilObject} to use.
     * @param content the contents of the returned file.
     * @return the created file
     */
    private final static FileObject copyStringToFile (FileObject f, String content) throws Exception {
        OutputStream os = f.getOutputStream();
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        FileUtil.copy(is, os);
        os.close ();
        is.close();

        return f;
    }

    private static void setData(FileObject from, FileObject sourceRoot, SourceVersion sourceLevel) {
        for (ClassPathProvider cpp : Lookup.getDefault().lookupAll(ClassPathProvider.class)) {
            if (cpp instanceof TestClassPathProvider) {
                ((TestClassPathProvider) cpp).setData(from, sourceRoot, sourceLevel);
            }
        }
    }

    private static String getFixResult(FileObject src, Fix fix) throws Exception {
        String original = getText(src);

        fix.implement();

        String nue = getText(src);

        copyStringToFile(src, original);

        return nue;
    }

    private static String getText(FileObject file) throws IOException {
        Charset encoding = FileEncodingQuery.getEncoding(file);

        return new String(file.asBytes(), encoding);
    }

    @ServiceProviders({
        @ServiceProvider(service=ClassPathProvider.class),
        @ServiceProvider(service=SourceLevelQueryImplementation.class)
    })
    public static final class TestClassPathProvider implements ClassPathProvider, SourceLevelQueryImplementation {

        private FileObject from;
        private FileObject sourceRoot;
        private String sourceLevel;

        public synchronized ClassPath findClassPath(FileObject file, String type) {
            if (from == null) {
                return null;
            }

            if (sourceRoot.equals(file) || FileUtil.isParentOf(sourceRoot, file)) {
                return ClassPath.getClassPath(from, type);
            }

            return null;
        }

        synchronized void setData(FileObject from, FileObject sourceRoot, SourceVersion sourceLevel) {
            this.from = from;
            this.sourceRoot = sourceRoot;
            this.sourceLevel = sourceLevel != null ? "1." + sourceLevel.name().substring("RELEASE_".length()) : null;
        }

        public String getSourceLevel(FileObject file) {
            if (from == null) {
                return null;
            }

            if (sourceRoot.equals(file) || FileUtil.isParentOf(sourceRoot, file)) {
                return sourceLevel;
            }

            return null;
        }
        
    }
}
