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

package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import junit.framework.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class CrashingAPTest extends NbTestCase {

    private FileObject src;
    private FileObject data;

    static {
        CrashingAPTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", CrashingAPTest.Lkp.class.getName());
        Assert.assertEquals(CrashingAPTest.Lkp.class, Lookup.getDefault().getClass());
    }

    public static class Lkp extends ProxyLookup {

        private static Lkp DEFAULT;

        public Lkp () {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            this.setLookups(
                 new Lookup [] {
                    Lookups.metaInfServices(l),
                    Lookups.singleton(l),
                    Lookups.singleton(ClassPathProviderImpl.getDefault()),
                    Lookups.singleton(SourceLevelQueryImpl.getDefault()),
            });
        }

    }


    public CrashingAPTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        FileObject wd = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull(wd);
        this.src = wd.createFolder("src");
        this.data = src.createData("Test","java");
        ClassPathProviderImpl.getDefault().setClassPaths(TestUtil.getBootClassPath(),
                                                         ClassPathSupport.createClassPath(new URL[0]),
                                                         ClassPathSupport.createClassPath(new FileObject[]{this.src}),
                                                         ClassPathSupport.createClassPath(System.getProperty("java.class.path")));
    }

    public void testElementHandle() throws Exception {
        try (OutputStream dataOut = data.getOutputStream();
            PrintWriter out = new PrintWriter ( new OutputStreamWriter (dataOut))) {
                out.println ("public class Test {}");
        }

        runWithProcessors(Arrays.asList(TestAP.class.getName()), () -> {
            final JavaSource js = JavaSource.forFileObject(data);
            assertNotNull(js);

            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws IOException {
                    parameter.toPhase(Phase.RESOLVED);
                    List<String> messages = parameter.getDiagnostics()
                                                     .stream()
                                                     .map(d -> d.getMessage(null))
                                                     .map(m -> firstLine(m))
                                                     .collect(Collectors.toList());
                    List<String> expected = Arrays.asList(Bundle.ERR_ProcessorException("org.netbeans.modules.java.source.indexing.CrashingAPTest$TestAP", "Crash"));
                    assertEquals(expected, messages);
                }
            },true);

            return null;
        });
    }

    public void testCompletionQuery() throws Exception {
        try (OutputStream dataOut = data.getOutputStream();
            PrintWriter out = new PrintWriter ( new OutputStreamWriter (dataOut))) {
                out.println ("@I(g=) public class Test {} @interface I { public String g(); }");
        }

        runWithProcessors(Arrays.asList(TestAP.class.getName()), () -> {
            final JavaSource js = JavaSource.forFileObject(data);
            assertNotNull(js);

            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws IOException {
                    parameter.toPhase(Phase.RESOLVED);
                    TypeElement clazz = parameter.getTopLevelElements().get(0);
                    AnnotationMirror am = clazz.getAnnotationMirrors().get(0);
                    ExecutableElement method = am.getElementValues().keySet().iterator().next();
                    List<String> result =
                            SourceUtils.getAttributeValueCompletions(parameter, clazz, am, method, "")
                                       .stream()
                                       .map(c -> c.getValue() + "-" + c.getMessage())
                                       .collect(Collectors.toList());

                    assertEquals(Arrays.asList("value-message"), result);
                }
            },true);

            return null;
        });
    }

    private void runWithProcessors(Iterable<? extends String> processors, Callable<Void> toRun) {
        ProxyLookup newLookup = new ProxyLookup(Lookup.getDefault(),
                                                Lookups.fixed(new APQImpl(processors)));
        Lookups.executeWith(newLookup, () -> {
            try {
                toRun.call();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private String firstLine(String m) {
        int newLine = m.indexOf('\n');
        if (newLine == (-1)) return m;
        return m.substring(0, newLine);
    }

    private static class ClassPathProviderImpl implements ClassPathProvider {

        private static ClassPathProviderImpl instance;

        private ClassPath compile;
        private ClassPath boot;
        private ClassPath src;
        private ClassPath processorPath;

        private ClassPathProviderImpl () {

        }

        public synchronized ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.COMPILE.equals(type)) {
                return compile;
            }
            else if (ClassPath.BOOT.equals(type)) {
                return boot;
            }
            else if (ClassPath.SOURCE.equals(type)) {
                return src;
            }
            else if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
                return processorPath;
            }
            else {
                return null;
            }
        }

        public synchronized void setClassPaths (ClassPath boot, ClassPath compile, ClassPath src, ClassPath processorPath) {
            this.boot = boot;
            this.compile = compile;
            this.src = src;
            this.processorPath = processorPath;
        }

        public static synchronized ClassPathProviderImpl getDefault () {
            if (instance == null) {
                instance = new ClassPathProviderImpl ();
            }
            return instance;
        }
    }

    private static class SourceLevelQueryImpl implements SourceLevelQueryImplementation {

        private static SourceLevelQueryImpl instance;

        private SourceLevelQueryImpl() {}

        @Override
        public String getSourceLevel(FileObject javaFile) {
            return "8";
        }

        public static synchronized SourceLevelQueryImpl getDefault () {
            if (instance == null) {
                instance = new SourceLevelQueryImpl();
            }
            return instance;
        }
    }

    private static class APQImpl implements AnnotationProcessingQueryImplementation {

        private final Result result;

        public APQImpl(Iterable<? extends String> processors) {
            result = new Result() {
                public @NonNull Set<? extends Trigger> annotationProcessingEnabled() {
                    return EnumSet.allOf(Trigger.class);
                }

                public @CheckForNull Iterable<? extends String> annotationProcessorsToRun() {
                    return processors;
                }

                public @CheckForNull URL sourceOutputDirectory() {
                    return null;
                }

                public @NonNull Map<? extends String, ? extends String> processorOptions() {
                    return Collections.emptyMap();
                }

                public void addChangeListener(@NonNull ChangeListener l) {}

                public void removeChangeListener(@NonNull ChangeListener l) {}
            };
        }

        @Override
        public AnnotationProcessingQuery.Result getAnnotationProcessingOptions(FileObject file) {
            return result;
        }

    }

    @SupportedAnnotationTypes("*")
    public static class TestAP extends AbstractProcessor {
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            throw new IllegalStateException("Crash");
        }

        @Override
        public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
            return Arrays.asList(new Completion() {
                @Override
                public String getValue() {
                    return "value";
                }

                @Override
                public String getMessage() {
                    return "message";
                }
            });
        }

    }

    static {
        System.setProperty("SourcePath.no.source.filter", "true");
    }
}
