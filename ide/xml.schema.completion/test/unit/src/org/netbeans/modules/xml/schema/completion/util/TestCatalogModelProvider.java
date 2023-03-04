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
package org.netbeans.modules.xml.schema.completion.util;

import org.netbeans.modules.xml.schema.completion.util.CatalogModelProvider;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 * Helps in getting the model for code completion.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.schema.completion.util.CatalogModelProvider.class)
public class TestCatalogModelProvider extends CatalogModelProvider {
    
    private TestCatalogModel catalogModel;
    
    public TestCatalogModelProvider() {
        catalogModel = TestCatalogModel.getDefault();
    }
    
    CatalogModel getCatalogModel() {
        return catalogModel;
    }

    ModelSource getModelSource(FileObject fo, boolean editable) throws CatalogModelException {
        return catalogModel.createModelSource(fo, editable);
    }
    
}
