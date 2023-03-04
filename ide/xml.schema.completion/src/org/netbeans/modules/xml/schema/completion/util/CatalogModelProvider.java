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

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 * We need to write unit tests for this module, however there is a
 * challenge. This class is purely needed to solve that challenge.
 * 
 * In order to get a model source or a model, we must first have a
 * CatalogModel. In general you can get a CatalogModel from the
 * CatalogModelFactory. But for unit tests, we need a special TestCatalogModel.
 *
 * In this module, we do not directly deal with CatalogModel and hence it is
 * very difficult to use a TestCatalogModel from the unit tests.
 *
 * So the code uses lookup to find all CatalogModelProvider. If found uses it,
 * else uses the real CatalogModel. The way it will work is, unit test code will
 * create CatalogModelProvider which is going to return a TestCatalogModel.
 *
 * For all other use-cases, no CatalogModelProvider will be found and the module
 * will use the project based CatalogModel.
 *
 * @see DefaultModelProvider#getCatalogModelProvider
 *
 * @author Samaresh
 */
public abstract class CatalogModelProvider {
    abstract CatalogModel getCatalogModel();
    
    abstract ModelSource getModelSource(FileObject fo, boolean editable)
    throws CatalogModelException ;
}
