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
package org.netbeans.modules.spring.api.beans.model;

import java.io.IOException;
import java.net.MalformedURLException;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
//import org.netbeans.modules.spring.beans.AnnotationSupportTestCase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SpringModelFactoryTest extends CommonAnnotationTestCase {

    public SpringModelFactoryTest(String name) {
        super(name);
    }

    public void testSpringModelFactoryCaching() throws MalformedURLException, IOException, NoSuchFieldException, ClassNotFoundException {
        ModelUnit originalUnit = createNewModelUnitForTestProject();
        MetadataModel<SpringModel> originalMetaModel = SpringModelFactory.getMetaModel(originalUnit);
        assertEquals("Caching mechanism in SpringModelFactory failed", 1, SpringModelFactory.MODELS.size());
        
        // second getter should return already existing MetadataModel from cache
        ModelUnit secondUnit = createNewModelUnitForTestProject();
        MetadataModel<SpringModel> secondMetaModel = SpringModelFactory.getMetaModel(secondUnit);
        assertEquals("Caching mechanism in SpringModelFactory failed", 1, SpringModelFactory.MODELS.size());
        
        // both obtained meta models should be the same
        assertEquals(originalMetaModel, secondMetaModel);
        
        // created new model on demand should not be equivalent
        assertFalse(originalMetaModel.equals(SpringModelFactory.createMetaModel(secondUnit)));
    }
    
}
