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
package org.netbeans.modules.java.j2seplatform.queries;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.modules.java.j2seplatform.AbstractJ2SEAttacherTestBase;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Mutex;

/**
 *
 * @author sdedic
 */
public class DefaultSourceJavadocAttacherTest extends AbstractJ2SEAttacherTestBase {

    public DefaultSourceJavadocAttacherTest(String name) {
        super(name);
    }
    
    /**
     * Def and def2 are complementary: def2 is more 'picky' that def, and if def2 accepts, then
     * def should not be called at all.
     */
    private Definer def;
    private Definer2 def2;
    
    private Definer selected;
    
    @Override
    protected List<Object> additionalServices() {
        List<Object> l = super.additionalServices();
        // definer2 comes first
        l.add(def2 = new Definer2());
        l.add(def = new Definer());
        return l;
    }
    
    URL sourceURL;
    URL javadocURL;
    URL classes2RootURL;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        File f = getWorkDir();
        FileObject fo = FileUtil.toFileObject(f);
        
        FileObject cl = fo.createFolder("classes");
        FileObject cl2 = fo.createFolder("classes2");
        FileObject src = fo.createFolder("src");
        FileObject jd = fo.createFolder("javadoc");
        
        classesRootURL = URLMapper.findURL(cl, URLMapper.INTERNAL);
        classes2RootURL = URLMapper.findURL(cl2, URLMapper.INTERNAL);
        sourceURL = URLMapper.findURL(src, URLMapper.INTERNAL);
        javadocURL = URLMapper.findURL(jd, URLMapper.INTERNAL);

        selected = def;
    }
    
    class Definer2 extends Definer implements SourceJavadocAttacherImplementation.Definer2 {
        @Override
        public boolean accepts(URL root) {
            return classes2RootURL.equals(root);
        }
    }
    
    class Definer implements SourceJavadocAttacherImplementation.Definer {
        volatile URL sourceRoot;
        volatile URL javadocRoot;

        @Override
        public String getDisplayName() {
            return "Test";
        }

        @Override
        public String getDescription() {
            return "Test";
        }

        @Override
        public List<? extends URL> getSources(URL root, Callable<Boolean> cancel) {
            sourceRoot = root;
            return Collections.singletonList(sourceURL);
        }

        @Override
        public List<? extends URL> getJavadoc(URL root, Callable<Boolean> cancel) {
            javadocRoot = root;
            return Collections.singletonList(javadocURL);
        }
        
    }
    
    private void assertDefiner(boolean sources, Definer d, boolean expect) throws Exception {
        if (expect) {
            assertNotNull(sources ? d.sourceRoot : d.javadocRoot);
        } else {
            assertNull(sources ? d.sourceRoot : d.javadocRoot);
        }
    }
    
    protected void assertAttacherResults(boolean sources) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        AtomicReference<Boolean> b = new AtomicReference<>();
        
        boolean expectAttach = Locale.getDefault().getLanguage().compareToIgnoreCase("da") == 0;
        
        SourceJavadocAttacher.AttachmentListener attachL = new SourceJavadocAttacher.AttachmentListener() {
            @Override
            public void attachmentSucceeded() {
                b.set(true);
                if (!expectAttach) {
                    fail("Must not attach to classes");
                }
            }

            @Override
            public void attachmentFailed() {
                b.set(false);
                if (expectAttach) {
                    fail("Must process classes");
                }
            }
        };
        
        BiConsumer<URL, SourceJavadocAttacher.AttachmentListener> c = (sources ? SourceJavadocAttacher::attachSources : SourceJavadocAttacher::attachJavadoc);
        c.accept(classesRootURL, attachL);
        Mutex.EVENT.writeAccess(() -> {
            cdl.countDown();
        });
        cdl.await();
        assertEquals(Boolean.valueOf(expectAttach), b.get());
        assertEquals(permitUI, uiPresented.get());
        
        assertDefiner(sources, selected, expectAttach);
        assertDefiner(!sources, selected, false);
        if (def != selected) {
            assertDefiner(sources, def, false);
        }
        if (def2 != selected) {
            assertDefiner(sources, def2, false);
        }
    }

    /**
     * Checks that Definer2 will run for javadoc for classes2, Definer won't be contacted.
     */
    public void testAttacherDifferentSourcesYes() throws Exception {
        Locale.setDefault(new Locale("DA"));
        classesRootURL = classes2RootURL;
        selected = def2;
        assertAttacherResults(true);
    }

    /**
     * Checks that Definer2 will be used by UI for classes2
     */
    public void testAttacherDifferentJavadocWithUI() throws Exception {
        permitUI = true;
        classesRootURL = classes2RootURL;
        selected = def2;
        assertAttacherResults(false);
    }
    
}
