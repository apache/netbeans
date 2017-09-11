/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 * Tests if PersistentObjectManager caches multiple objects based on
 * the same TypeElement correctly.
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerMultipleTest extends PersistenceTestCase {

    // XXX should refactor the waiting for added/changed/removed types to an utility class

    private static final int EVENT_TIMEOUT = 20; // seconds

    private PersistentObjectManager<ResourceImpl> manager;

    public PersistentObjectManagerMultipleTest(String name) {
        super(name);
    }

    public void testChangedFiles() throws Exception {
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { ClassPath.getClassPath(srcFO, ClassPath.SOURCE) });
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                manager = helper.createPersistentObjectManager(new ResourceProvider(helper));
            }
        });
        // adding a class with two resources
        final AtomicBoolean departmentAdded = new AtomicBoolean();
        final CountDownLatch addedLatch = new CountDownLatch(1);
        ClassIndexListener listener = new ClassIndexAdapter() {
            public void typesAdded(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentAdded.set(true);
                        addedLatch.countDown();
                    }
                }
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        TestUtilities.copyStringToFileObject(srcFO, "foo/Department.java",
                "package foo;" +
                "import javax.annotation.*;" +
                "public class Department {" +
                "   @Resource(name = \"foo\")" +
                "   private Object foo;" +
                "   @Resource(name = \"bar\")" +
                "   private Object bar;" +
                "}");
        addedLatch.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a typesAdded event for Department", departmentAdded.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        SourceUtils.waitScanFinished(); // otherwise the PMO will initialize temporarily
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(2, manager.getObjects().size());
                assertFalse(manager.temporary);
            }
        });
        // removing one of the resources
        final AtomicBoolean departmentChanged = new AtomicBoolean();
        final CountDownLatch changedLatch = new CountDownLatch(1);
        listener = new ClassIndexAdapter() {
            public void typesChanged(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentChanged.set(true);
                        changedLatch.countDown();
                    }
                }
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        TestUtilities.copyStringToFileObject(srcFO, "foo/Department.java",
                "package foo;" +
                "import javax.annotation.*;" +
                "public class Department {" +
                "   @Resource(name = \"bar\")" +
                "   private Object bar;" +
                "}");
        changedLatch.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a typesChanged event for Department", departmentChanged.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(1, manager.getObjects().size());
            }
        });
        // adding another resource
        final AtomicBoolean departmentChanged2 = new AtomicBoolean();
        final CountDownLatch changedLatch2 = new CountDownLatch(1);
        listener = new ClassIndexAdapter() {
            public void typesChanged(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentChanged2.set(true);
                        changedLatch2.countDown();
                    }
                }
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        TestUtilities.copyStringToFileObject(srcFO, "foo/Department.java",
                "package foo;" +
                "import javax.annotation.*;" +
                "public class Department {" +
                "   @Resource(name = \"bar\")" +
                "   private Object bar;" +
                "   @Resource(name = \"baz\")" +
                "   private Object baz;" +
                "}");
        changedLatch2.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a typesChanged event for Department", departmentChanged2.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(2, manager.getObjects().size());
            }
        });
    }

    private static final class ResourceProvider implements ObjectProvider<ResourceImpl> {

        private final AnnotationModelHelper helper;

        public ResourceProvider(AnnotationModelHelper helper) {
            this.helper = helper;
        }

        public List<ResourceImpl> createInitialObjects() {
            final List<ResourceImpl> result = new ArrayList<ResourceImpl>();
            TypeElement departmentType = helper.getCompilationController().getElements().getTypeElement("foo.Department");
            for (Element element : departmentType.getEnclosedElements()) {
                if (isResource(element)) {
                    result.add(new ResourceImpl(helper, departmentType, element));
                }
            }
            return result;
        }

        public List<ResourceImpl> createObjects(TypeElement type) {
            throw new UnsupportedOperationException();
        }

        public boolean modifyObjects(TypeElement type, List<ResourceImpl> objects) {
            boolean modified = false;
            Set<Element> elements = new HashSet<Element>();
            for (Iterator<ResourceImpl> it = objects.iterator(); it.hasNext();) {
                ResourceImpl resource = it.next();
                if (!resource.refresh()) {
                    it.remove();
                    modified = true;
                } else {
                    elements.add(resource.getAnnotatedElement());
                }
            }
            for (Element element : type.getEnclosedElements()) {
                if (isResource(element) && !elements.contains(element)) {
                    objects.add(new ResourceImpl(helper, type, element));
                    modified = true;
                }
            }
            return modified;
        }

        private boolean isResource(Element element) {
            String elementName = element.getSimpleName().toString();
            return "foo".equals(elementName) || "bar".equals(elementName) || "baz".equals(elementName);
        }
    }

    private static final class ResourceImpl extends PersistentObject {

        private final ElementHandle<Element> resourceElement;
        private String name;

        public ResourceImpl(AnnotationModelHelper helper, TypeElement typeElement, Element element) {
            super(helper, typeElement);
            resourceElement = ElementHandle.create(element);
        }

        protected boolean refresh() {
            Element element = resourceElement.resolve(getHelper().getCompilationController());
            if (element == null) {
                return false;
            }
            List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
            if (annotations.size() == 0) {
                return false;
            }
            AnnotationParser parser = AnnotationParser.create(getHelper());
            parser.expectString("name", parser.defaultValue(element.getSimpleName()));
            name = parser.parse(annotations.get(0)).get("name", String.class);
            return true;
        }

        public String getName() {
            return name;
        }

        protected Element getAnnotatedElement() {
            return resourceElement.resolve(getHelper().getCompilationController());
        }
    }
}
