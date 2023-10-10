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
package org.netbeans.modules.profiler.categories.j2ee;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.ClassParser;
import org.netbeans.modules.java.source.parsing.ClassParserFactory;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;


/**
 * @author ads
 *
 */
public class TestUtilities extends ProxyLookup {
    
    private static TestUtilities DEFAULT;
    private static final Lookup PROJECTS;
    
    static {
        TestUtilities.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestUtilities.class.getName());
        Assert.assertEquals(TestUtilities.class, Lookup.getDefault().getClass());
        Lookup p = Lookups.forPath("Services/AntBasedProjectTypes/");
        p.lookupAll(AntBasedProjectType.class);
        PROJECTS = p;
        setLookup(new Object[0]);
    }

    public TestUtilities() {
        Assert.assertNull(DEFAULT);
        DEFAULT = this;
        ClassLoader l = TestUtilities.class.getClassLoader();
        setLookups(new Lookup[] {
            Lookups.metaInfServices(l),
            Lookups.singleton(l)
        });
    }
    
    public static final FileObject copyStringToFileObject(FileObject fo, String content) 
        throws IOException 
     {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            FileUtil.copy(is, os);
            return fo;
        } finally {
            os.close();
        }
    }

    public static final String copyFileObjectToString (FileObject fo) throws java.io.IOException {
        int s = (int)FileUtil.toFile(fo).length();
        byte[] data = new byte[s];
        InputStream stream = fo.getInputStream();
        try {
            int len = stream.read(data);
            if (len != s) {
                throw new EOFException("truncated file");
            }
            return new String (data);
        } finally {
            stream.close();
        }
    }
    
    /**
     * Creates a cache folder for the Java infrastructure.
     * 
     * @param folder the parent folder for the cache folder, 
     * typically the working dir.
     */ 
    public static void setCacheFolder(File folder){
        File cacheFolder = new File(folder,"cache");
        cacheFolder.mkdirs();
        CacheFolder.setCacheFolder(FileUtil.toFileObject(cacheFolder));
    }
    
    /**
     * Set the global default lookup.
     * Caution: if you don't include Lookups.metaInfServices, you may have trouble,
     * e.g. {@link #makeScratchDir} will not work.
     */
    public static void setLookup(Lookup l) {
        DEFAULT.setLookups(new Lookup[] {l});
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object... instances) {
        ClassLoader l = TestUtilities.class.getClassLoader();
        DEFAULT.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(l),
            Lookups.singleton(l),
            PROJECTS
        });
    }
    
    @ServiceProvider(service=MimeDataProvider.class)
    public static final class JavacParserProvider implements MimeDataProvider {
        private Lookup javaLookup = Lookups.fixed(
            new JavacParserFactory(),
            new JavaCustomIndexer.Factory()
        );
        private Lookup classLookup = Lookups.fixed(
            new ClassParserFactory(),
            new JavaCustomIndexer.Factory()
        );

        @Override
        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().endsWith(JavacParser.MIME_TYPE)) {
                return javaLookup;
            }
            if (mimePath.getPath().endsWith(ClassParser.MIME_TYPE)) {
                return classLookup;
            }
            return Lookup.EMPTY;
        }
    }
    
    @ServiceProvider(service = InstalledFileLocator.class)
    public static class InstalledFileLocatorImpl extends InstalledFileLocator{

        /* (non-Javadoc)
         * @see org.openide.modules.InstalledFileLocator#locate(java.lang.String, java.lang.String, boolean)
         */
        @Override
        public File locate( String arg0, String arg1, boolean arg2 ) {
            if ( arg1== null && "catalina_home".equals(arg0)){
                File file = new File(System.getProperty("netbeans.user"));
                return file.getParentFile();
            }
            return null;
        }
        
    }
    
}
