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
package org.netbeans.modules.j2ee.common.dd;

import java.io.IOException;
import java.util.function.Consumer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Benjamin Asbach
 */
public class DDHelperTest extends NbTestCase {

    @Mock
    DDHelper.MakeFileCopy makeFileCopy;

    @Mock
    Consumer<DDHelper.MakeFileCopy> atomicActionRunner;

    @Mock
    FileObject destinationDir;

    @Mock
    DDHelper.MakeFileCopyFactory makeFileCopyFactory;

    @Mock
    FileObject actionRetunFileObject;

    public DDHelperTest(String name) {
        super(name);

        MockitoAnnotations.openMocks(this);
    }

    public void testCreateWebXml() throws IOException {
        setupMocks();

        verifyWebXmlFileCreation(Profile.J2EE_13, "web-2.3.xml");

        verifyWebXmlFileCreation(Profile.J2EE_14, "web-2.4.xml");

        verifyWebXmlFileCreation(Profile.JAVA_EE_5, "web-2.5.xml");

        verifyWebXmlFileCreation(Profile.JAVA_EE_6_FULL, "web-3.0.xml");
        verifyWebXmlFileCreation(Profile.JAVA_EE_6_WEB, "web-3.0.xml");

        verifyWebXmlFileCreation(Profile.JAVA_EE_7_FULL, "web-3.1.xml");
        verifyWebXmlFileCreation(Profile.JAVA_EE_7_WEB, "web-3.1.xml");

        verifyWebXmlFileCreation(Profile.JAVA_EE_8_FULL, "web-4.0.xml");
        verifyWebXmlFileCreation(Profile.JAVA_EE_8_WEB, "web-4.0.xml");
        verifyWebXmlFileCreation(Profile.JAKARTA_EE_8_FULL, "web-4.0.xml");
        verifyWebXmlFileCreation(Profile.JAKARTA_EE_8_WEB, "web-4.0.xml");

        verifyWebXmlFileCreation(Profile.JAKARTA_EE_9_FULL, "web-5.0.xml");
        verifyWebXmlFileCreation(Profile.JAKARTA_EE_9_WEB, "web-5.0.xml");
        verifyWebXmlFileCreation(Profile.JAKARTA_EE_9_1_FULL, "web-5.0.xml");
        verifyWebXmlFileCreation(Profile.JAKARTA_EE_9_1_WEB, "web-5.0.xml");

        verifyWebXmlFileCreation(Profile.JAKARTA_EE_10_FULL, "web-6.0.xml");
        verifyWebXmlFileCreation(Profile.JAKARTA_EE_10_WEB, "web-6.0.xml");
    }

    private void verifyWebXmlFileCreation(Profile profile, String resourceName) throws IOException {
        assertEquals(actionRetunFileObject, DDHelper.createWebXml(profile, destinationDir));
        verify(makeFileCopyFactory).build(eq(DDHelper.RESOURCE_FOLDER + resourceName), eq(destinationDir), eq("web.xml"));

        clearInvocations(makeFileCopyFactory);
    }

    public void testCreateWebFragmentXml() throws IOException {
        setupMocks();

        assertNull(DDHelper.createWebFragmentXml(Profile.J2EE_13, null));
        assertNull(DDHelper.createWebFragmentXml(Profile.J2EE_14, null));
        assertNull(DDHelper.createWebFragmentXml(Profile.JAVA_EE_5, null));

        verifyWebFragmentXmlFileCreation(Profile.JAVA_EE_6_FULL, "web-fragment-3.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAVA_EE_6_WEB, "web-fragment-3.0.xml");

        verifyWebFragmentXmlFileCreation(Profile.JAVA_EE_7_FULL, "web-fragment-3.1.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAVA_EE_7_WEB, "web-fragment-3.1.xml");

        verifyWebFragmentXmlFileCreation(Profile.JAVA_EE_8_FULL, "web-fragment-4.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAVA_EE_8_WEB, "web-fragment-4.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_8_FULL, "web-fragment-4.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_8_WEB, "web-fragment-4.0.xml");

        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_9_FULL, "web-fragment-5.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_9_WEB, "web-fragment-5.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_9_1_FULL, "web-fragment-5.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_9_1_WEB, "web-fragment-5.0.xml");

        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_10_FULL, "web-fragment-6.0.xml");
        verifyWebFragmentXmlFileCreation(Profile.JAKARTA_EE_10_WEB, "web-fragment-6.0.xml");
    }

    private void verifyWebFragmentXmlFileCreation(Profile profile, String resourceName) throws IOException {
        assertEquals(actionRetunFileObject, DDHelper.createWebFragmentXml(profile, destinationDir));
        verify(makeFileCopyFactory).build(eq(DDHelper.RESOURCE_FOLDER + resourceName), eq(destinationDir), eq("web-fragment.xml"));

        clearInvocations(makeFileCopyFactory);
    }

    public void testCreateBeansXml() throws IOException {
        setupMocks();

        assertNull(DDHelper.createBeansXml(Profile.J2EE_13, null));
        assertNull(DDHelper.createBeansXml(Profile.J2EE_14, null));
        assertNull(DDHelper.createBeansXml(Profile.JAVA_EE_5, null));

        verifyBeansXmlFileCreation(Profile.JAVA_EE_6_FULL, "beans-1.0.xml");
        verifyBeansXmlFileCreation(Profile.JAVA_EE_6_WEB, "beans-1.0.xml");

        verifyBeansXmlFileCreation(Profile.JAVA_EE_7_FULL, "beans-1.1.xml");
        verifyBeansXmlFileCreation(Profile.JAVA_EE_7_WEB, "beans-1.1.xml");

        verifyBeansXmlFileCreation(Profile.JAVA_EE_8_FULL, "beans-2.0.xml");
        verifyBeansXmlFileCreation(Profile.JAVA_EE_8_WEB, "beans-2.0.xml");
        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_8_FULL, "beans-2.0.xml");
        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_8_WEB, "beans-2.0.xml");

        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_9_FULL, "beans-3.0.xml");
        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_9_WEB, "beans-3.0.xml");
        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_9_1_FULL, "beans-3.0.xml");
        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_9_1_WEB, "beans-3.0.xml");

        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_10_FULL, "beans-4.0.xml");
        verifyBeansXmlFileCreation(Profile.JAKARTA_EE_10_WEB, "beans-4.0.xml");
    }

    private void verifyBeansXmlFileCreation(Profile profile, String resourceName) throws IOException {
        assertEquals(actionRetunFileObject, DDHelper.createBeansXml(profile, destinationDir));
        verify(makeFileCopyFactory).build(eq(DDHelper.RESOURCE_FOLDER + resourceName), eq(destinationDir), eq("beans.xml"));

        clearInvocations(makeFileCopyFactory);
    }

    public void testCreateValidationXml() throws IOException {
        setupMocks();

        assertNull(DDHelper.createValidationXml(Profile.J2EE_13, null));
        assertNull(DDHelper.createValidationXml(Profile.J2EE_14, null));
        assertNull(DDHelper.createValidationXml(Profile.JAVA_EE_5, null));

        verifyValidationXmlFileCreation(Profile.JAVA_EE_6_FULL, "validation-1.0.xml");
        verifyValidationXmlFileCreation(Profile.JAVA_EE_6_WEB, "validation-1.0.xml");

        verifyValidationXmlFileCreation(Profile.JAVA_EE_7_FULL, "validation-1.1.xml");
        verifyValidationXmlFileCreation(Profile.JAVA_EE_7_WEB, "validation-1.1.xml");

        verifyValidationXmlFileCreation(Profile.JAVA_EE_8_FULL, "validation-2.0.xml");
        verifyValidationXmlFileCreation(Profile.JAVA_EE_8_WEB, "validation-2.0.xml");
        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_8_FULL, "validation-2.0.xml");
        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_8_WEB, "validation-2.0.xml");

        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_9_FULL, "validation-3.0.xml");
        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_9_WEB, "validation-3.0.xml");
        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_9_1_FULL, "validation-3.0.xml");
        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_9_1_WEB, "validation-3.0.xml");

        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_10_FULL, "validation-3.0.xml");
        verifyValidationXmlFileCreation(Profile.JAKARTA_EE_10_WEB, "validation-3.0.xml");
    }

    private void verifyValidationXmlFileCreation(Profile profile, String resourceName) throws IOException {
        assertEquals(actionRetunFileObject, DDHelper.createValidationXml(profile, destinationDir));
        verify(makeFileCopyFactory).build(eq(DDHelper.RESOURCE_FOLDER + resourceName), eq(destinationDir), eq("validation.xml"));

        clearInvocations(makeFileCopyFactory);
    }

    public void testConstraintXml() throws IOException {
        setupMocks();

        assertNull(DDHelper.createConstraintXml(Profile.J2EE_13, null));
        assertNull(DDHelper.createConstraintXml(Profile.J2EE_14, null));
        assertNull(DDHelper.createConstraintXml(Profile.JAVA_EE_5, null));

        verifyConstraintXmlFileCreation(Profile.JAVA_EE_6_FULL, "constraint-1.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAVA_EE_6_WEB, "constraint-1.0.xml");

        verifyConstraintXmlFileCreation(Profile.JAVA_EE_7_FULL, "constraint-1.1.xml");
        verifyConstraintXmlFileCreation(Profile.JAVA_EE_7_WEB, "constraint-1.1.xml");

        verifyConstraintXmlFileCreation(Profile.JAVA_EE_8_FULL, "constraint-2.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAVA_EE_8_WEB, "constraint-2.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_8_FULL, "constraint-2.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_8_WEB, "constraint-2.0.xml");

        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_9_FULL, "constraint-3.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_9_WEB, "constraint-3.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_9_1_FULL, "constraint-3.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_9_1_WEB, "constraint-3.0.xml");

        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_10_FULL, "constraint-3.0.xml");
        verifyConstraintXmlFileCreation(Profile.JAKARTA_EE_10_WEB, "constraint-3.0.xml");
    }

    private void verifyConstraintXmlFileCreation(Profile profile, String resourceName) throws IOException {
        assertEquals(actionRetunFileObject, DDHelper.createConstraintXml(profile, destinationDir, "constraint"));
        verify(makeFileCopyFactory).build(eq(DDHelper.RESOURCE_FOLDER + resourceName), eq(destinationDir), eq("constraint.xml"));

        clearInvocations(makeFileCopyFactory);
    }

    public void testApplicationXml() throws IOException {
        setupMocks();

        assertNull(DDHelper.createApplicationXml(Profile.JAVA_EE_6_WEB, null, true));
        assertNull(DDHelper.createApplicationXml(Profile.JAVA_EE_7_WEB, null, true));
        assertNull(DDHelper.createApplicationXml(Profile.JAVA_EE_8_WEB, null, true));
        assertNull(DDHelper.createApplicationXml(Profile.JAKARTA_EE_8_WEB, null, true));
        assertNull(DDHelper.createApplicationXml(Profile.JAKARTA_EE_9_WEB, null, true));
        assertNull(DDHelper.createApplicationXml(Profile.JAKARTA_EE_9_1_WEB, null, true));
        assertNull(DDHelper.createApplicationXml(Profile.JAKARTA_EE_10_WEB, null, true));

        verifyApplicationXmlFileCreation(Profile.J2EE_13, "ear-1.4.xml");
        verifyApplicationXmlFileCreation(Profile.J2EE_14, "ear-1.4.xml");

        verifyApplicationXmlFileCreation(Profile.JAVA_EE_5, "ear-5.xml");

        verifyApplicationXmlFileCreation(Profile.JAVA_EE_6_FULL, "ear-6.xml");

        verifyApplicationXmlFileCreation(Profile.JAVA_EE_7_FULL, "ear-7.xml");

        verifyApplicationXmlFileCreation(Profile.JAVA_EE_8_FULL, "ear-8.xml");
        verifyApplicationXmlFileCreation(Profile.JAKARTA_EE_8_FULL, "ear-8.xml");

        verifyApplicationXmlFileCreation(Profile.JAKARTA_EE_9_FULL, "ear-9.xml");
        verifyApplicationXmlFileCreation(Profile.JAKARTA_EE_9_1_FULL, "ear-9.xml");

        verifyApplicationXmlFileCreation(Profile.JAKARTA_EE_10_FULL, "ear-10.xml");
    }

    private void verifyApplicationXmlFileCreation(Profile profile, String resourceName) throws IOException {
        assertEquals(actionRetunFileObject, DDHelper.createApplicationXml(profile, destinationDir, true));
        verify(makeFileCopyFactory).build(eq(DDHelper.RESOURCE_FOLDER + resourceName), eq(destinationDir), eq("application.xml"));

        clearInvocations(makeFileCopyFactory);
    }

    private void setupMocks() {
        DDHelper.atomicActionRunner = atomicActionRunner;
        DDHelper.makeFileCopyFactory = makeFileCopyFactory;
        
        when(makeFileCopyFactory.build(anyString(), any(FileObject.class), anyString())).thenReturn(makeFileCopy);
        when(makeFileCopy.getResult()).thenReturn(actionRetunFileObject);
    }
}
