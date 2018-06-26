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
