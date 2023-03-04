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

package org.netbeans.modules.j2ee.core.support.java.method;

import org.netbeans.modules.j2ee.core.support.java.method.ValidatingPropertyChangeListener;
import org.netbeans.modules.j2ee.core.support.java.method.MethodCustomizerPanel;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.openide.DialogDescriptor;

/**
 *
 * @author Martin Adamek
 */
public class ValidatingPropertyChangeListenerTest extends NbTestCase {
    
    public ValidatingPropertyChangeListenerTest(String testName) {
        super(testName);
    }
    
    public void testValidate() {
        MethodModel methodModel = MethodModel.create(
                "m1",
                "void",
                null,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        MethodCustomizerPanel mcPanel = MethodCustomizerPanel.create(
                methodModel,
                null,
                false,
                false,
                false,
                false,
                true,
                null,
                false,
                true,
                false,
                false
                );
        DialogDescriptor dialogDescriptor = new DialogDescriptor("Test", "Test");
        ValidatingPropertyChangeListener validator = new ValidatingPropertyChangeListener(mcPanel, dialogDescriptor, Collections.<MethodModel>emptyList(), null);
        assertTrue(validator.validate());
        mcPanel = MethodCustomizerPanel.create(
                methodModel,
                null,
                false,
                false,
                false,
                false,
                true,
                null,
                false,
                true,
                true,
                false
                );
        validator = new ValidatingPropertyChangeListener(mcPanel, dialogDescriptor, Collections.<MethodModel>emptyList(), null);
        assertFalse(validator.validate());
    }
    
}
