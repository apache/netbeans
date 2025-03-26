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
package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.MockLookup;

/**
 * A base class for unit tests.
 *
 * @author Erno Mononen
 */
public abstract class SourceTestSupport extends NbTestCase {
    static {
        setLookups();
    }
    
    
    public SourceTestSupport(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        ClassPathProviderImpl classPathProvider = new ClassPathProviderImpl(new FileObject[]{FileUtil.toFileObject(getWorkDir())});
        setLookups(
                classPathProvider,
                new JavaFileResolver(),
                new FakeJavaDataLoaderPool(),
                new TestSourceLevelQueryImplementation()
                );
        initTemplates();
        setCacheFolder();
    }
    
    protected void tearDown() throws Exception{
        super.tearDown();
    }

    private void setCacheFolder() throws IOException{
        File cacheFolder = new File(getWorkDir(),"cache");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
    private void initTemplates() throws Exception{
        FileObject interfaceTemplate = FileUtil.getConfigFile("Templates/Classes/Interface.java");
        TestUtilities.copyStringToFileObject(interfaceTemplate,
                "package ${package};" +
                "public interface ${name} {\n" +
                "}");
        FileObject classTemplate = FileUtil.getConfigFile("Templates/Classes/Class.java");
        TestUtilities.copyStringToFileObject(classTemplate,
                "package ${package};" +
                "public class ${name} {\n" +
                "}");
    }
    
    protected void assertFile(FileObject result){
        assertFile( getGoldenFile(), FileUtil.toFile(result));
    }
    
    // temporary methods for debugging
    
    protected void print(FileObject fo) throws IOException {
        print(FileUtil.toFile(fo));
    }
    
    protected void print(File file) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            PrintStream out = System.out;
            String str;
            while ((str = in.readLine()) != null) {
                out.println(str);
            }
        }
    }
    
    private static void setLookups(Object... instances) {
        Object[] allInstances = new Object[instances.length + 1];
        ClassLoader classLoader = SourceTestSupport.class.getClassLoader();
        allInstances[0] = classLoader;
        System.arraycopy(instances, 0, allInstances, 1, instances.length);
        MockLookup.setInstances(allInstances);
    }
    
    public static final class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {
        
        public String getSourceLevel(FileObject javaFile) {
            return "1.5";
        }
        
    }
    
    private static class JavaFileResolver extends MIMEResolver
    {

        public JavaFileResolver() {
            super("text/x-java");
        }


        @Override
        public String findMIMEType(FileObject fo) {
            if(JavaDataLoader.JAVA_EXTENSION.equals(fo.getExt())) {
                return JavaDataLoader.JAVA_MIME_TYPE;
            } else {
                return null;
            }
        }

    }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class JavacParserProvider implements MimeDataProvider {

        private Lookup javaLookup = Lookups.fixed(new JavacParserFactory());

        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().endsWith(JavacParser.MIME_TYPE)) {
                return javaLookup;
            }

            return Lookup.EMPTY;
        }

    }
}
