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
