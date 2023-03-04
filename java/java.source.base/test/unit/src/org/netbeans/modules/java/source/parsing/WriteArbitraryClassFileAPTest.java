/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
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
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import javax.tools.StandardLocation;
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
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class WriteArbitraryClassFileAPTest extends NbTestCase {

    private FileObject src1;
    private FileObject src2;
    private FileObject data;

    static {
        WriteArbitraryClassFileAPTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", WriteArbitraryClassFileAPTest.Lkp.class.getName());
        Assert.assertEquals(WriteArbitraryClassFileAPTest.Lkp.class, Lookup.getDefault().getClass());
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
                    Lookups.singleton(new APQImpl()),
            });
        }

    }


    public WriteArbitraryClassFileAPTest(String testName) {
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
        this.src1 = wd.createFolder("src1");
        this.data = src1.createData("Test","java");
        this.src2 = wd.createFolder("src2");
        FileLock lock = data.lock();
        try {
            PrintWriter out = new PrintWriter ( new OutputStreamWriter (data.getOutputStream(lock)));
            try {
                out.println ("public class Test {}");
            } finally {
                out.close ();
            }
        } finally {
            lock.releaseLock();
        }
        ClassPathProviderImpl.getDefault().setClassPaths(TestUtil.getBootClassPath(),
                                                         ClassPathSupport.createClassPath(new URL[0]),
                                                         ClassPathSupport.createClassPath(new FileObject[]{this.src1, this.src2}),
                                                         ClassPathSupport.createClassPath(System.getProperty("java.class.path")));
    }

    public void testWriteArbitraryClassFile() throws Exception {
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
                assertEquals(Collections.emptyList(), messages);
            }
        },true);
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

        private final Result result = new Result() {
            public @NonNull Set<? extends Trigger> annotationProcessingEnabled() {
                return EnumSet.allOf(Trigger.class);
            }

            public @CheckForNull Iterable<? extends String> annotationProcessorsToRun() {
                return Arrays.asList(TestAP.class.getName());
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
        @Override
        public AnnotationProcessingQuery.Result getAnnotationProcessingOptions(FileObject file) {
            return result;
        }

    }

    @SupportedAnnotationTypes("*")
    public static class TestAP extends AbstractProcessor {
        int round = 0;
        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            if (round++ == 0) {
                try {
                    processingEnv.getFiler().createClassFile("any.Name");
                    processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "any", "Name.class");
                } catch (IOException ex) {
                    throw new AssertionError(ex);
                }
            }
            return false;
        }
    }

    static {
        System.setProperty("SourcePath.no.source.filter", "true");
    }
}
