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
package org.netbeans.modules.php.api.annotation.registration;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.annotation.PhpAnnotations;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class PhpAnnotationsRegistrationProcessorTest extends NbTestCase {

    private static final String CONSTRUCTOR = "constructor";
    private static final String FACTORY = "factory";


    public PhpAnnotationsRegistrationProcessorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testRegistration() {
        MyAnnotations.factoryCalls = 0;
        MockLookup.init();
        assertSame("No factory method should not be used yet", 0, MyAnnotations.factoryCalls);
        Collection<? extends AnnotationCompletionTagProvider> all = Lookups.forPath(PhpAnnotations.ANNOTATIONS_COMPLETION_TAG_PROVIDERS_PATH).lookupAll(AnnotationCompletionTagProvider.class);
        assertSame("Two should be found", 2, all.size());
        // ???
        //assertSame("One factory method should be used", 1, MyAnnotations.factoryCalls);

        Iterator<? extends AnnotationCompletionTagProvider> it = all.iterator();
        assertSame(CONSTRUCTOR, it.next().getIdentifier());
        assertSame(FACTORY, it.next().getIdentifier());
    }

    //~ Inner classes

    public static final class MyAnnotationsFactory {
        @AnnotationCompletionTagProvider.Registration(position=200)
        public static MyAnnotations getInstance() {
            MyAnnotations.factoryCalls++;
            return new MyAnnotations(FACTORY);
        }
    }

    @AnnotationCompletionTagProvider.Registration(position=100)
    public static final class MyAnnotations extends AnnotationCompletionTagProvider {
        static int factoryCalls = 0;

        public MyAnnotations() {
            super(CONSTRUCTOR, "display name", null);
        }

        MyAnnotations(String name) {
            super(name, "display name", null);
        }

        @Override
        public List<AnnotationCompletionTag> getAnnotations() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<AnnotationCompletionTag> getFunctionAnnotations() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<AnnotationCompletionTag> getTypeAnnotations() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<AnnotationCompletionTag> getFieldAnnotations() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<AnnotationCompletionTag> getMethodAnnotations() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
