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
package org.netbeans.modules.java.j2seplatform.queries;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.junit.NbTestCase;
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

    @Override
    protected List<Object> additionalServices() {
        List<Object> l = super.additionalServices();
        l.add(new Definer());
        return l;
    }
    
    URL sourceURL;
    URL javadocURL;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        
        File f = getWorkDir();
        FileObject fo = FileUtil.toFileObject(f);
        
        FileObject cl = fo.createFolder("classes");
        FileObject src = fo.createFolder("src");
        FileObject jd = fo.createFolder("javadoc");
        
        classesRootURL = URLMapper.findURL(cl, URLMapper.INTERNAL);
        sourceURL = URLMapper.findURL(src, URLMapper.INTERNAL);
        javadocURL = URLMapper.findURL(jd, URLMapper.INTERNAL);
    }
    
    class Definer implements SourceJavadocAttacherImplementation.Definer {

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
            return Collections.singletonList(sourceURL);
        }

        @Override
        public List<? extends URL> getJavadoc(URL root, Callable<Boolean> cancel) {
            return Collections.singletonList(javadocURL);
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
    }
}
