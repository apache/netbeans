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
package org.netbeans.modules.php.project.annotations;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class UserAnnotationsTest extends NbTestCase {

    private static final UserAnnotationTag USER_ANNOTATION = new UserAnnotationTag(
            EnumSet.of(UserAnnotationTag.Type.FUNCTION, UserAnnotationTag.Type.METHOD), "@FuncMeth2", "@FuncMeth2", "FuncMeth2 description");
    private static final List<UserAnnotationTag> ANNOTATIONS = Arrays.asList(
            new UserAnnotationTag(EnumSet.of(UserAnnotationTag.Type.FUNCTION), "@Func1", "@Func1", "Func1 description"),
            USER_ANNOTATION,
            new UserAnnotationTag(EnumSet.of(UserAnnotationTag.Type.TYPE), "@Class1", "@Class1", "Class1 description"),
            new UserAnnotationTag(EnumSet.of(UserAnnotationTag.Type.TYPE), "@Iface2", "@Iface2", "Iface2 description"),
            new UserAnnotationTag(EnumSet.of(UserAnnotationTag.Type.FIELD), "@Field1", "@Field1", "Field1 description"),
            new UserAnnotationTag(EnumSet.of(UserAnnotationTag.Type.METHOD), "@Method1", "@Method1", "Method1 description"));


    public UserAnnotationsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        UserAnnotations.getGlobal().clearAnnotations();
    }

    public void testEmpty() {
        List<UserAnnotationTag> annotations = UserAnnotations.getGlobal().getAnnotations();
        assertTrue("Annotations should be empty but are not: " + annotations.size(), annotations.isEmpty());
    }

    public void testClear() {
        // save
        UserAnnotations.getGlobal().setAnnotations(ANNOTATIONS);
        // read
        List<UserAnnotationTag> annotations = UserAnnotations.getGlobal().getAnnotations();
        assertEquals(ANNOTATIONS.size(), annotations.size());
        // clear
        UserAnnotations.getGlobal().clearAnnotations();
        // read
        annotations = UserAnnotations.getGlobal().getAnnotations();
        assertTrue("Annotations should be empty but are not: " + annotations.size(), annotations.isEmpty());
    }

    public void testSaveAndRead() {
        // save
        UserAnnotations.getGlobal().setAnnotations(ANNOTATIONS);
        // read
        List<UserAnnotationTag> annotations = UserAnnotations.getGlobal().getAnnotations();
        assertEquals(ANNOTATIONS.size(), annotations.size());
        assertEquals(ANNOTATIONS, annotations);
    }

    public void testMoreSavesAndReads() {
        testSaveAndRead();
        testSaveAndRead();
        testSaveAndRead();
    }

    public void testMarshall() {
        assertEquals("FUNCTION,METHOD", UserAnnotations.getGlobal().marshallTypes(USER_ANNOTATION.getTypes()));
    }

    public void testUnmarshall() {
        assertEquals(USER_ANNOTATION.getTypes(), UserAnnotations.getGlobal().unmarshallTypes("FUNCTION,METHOD"));
    }

}
