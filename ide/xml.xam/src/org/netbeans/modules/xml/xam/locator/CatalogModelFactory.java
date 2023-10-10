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

package org.netbeans.modules.xml.xam.locator;

import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.Lookup;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Returns a CatalogModel for a project
 * @author girix
 */
public abstract class CatalogModelFactory {
    
    /**
     * Given a ModelSource this method will return a Locator object specific to the it.
     * If there are initialization errors, CatalogModelException will be thrown.
     * @param modelSource a not null model source for which catalog model is requested.
     * @throws CatalogModelException
     */
    public abstract CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException;
    
    public abstract LSResourceResolver getLSResourceResolver();
    
    private static CatalogModelFactory implObj = null;
    
    public static CatalogModelFactory getDefault(){
        if(implObj == null) {
            implObj = (CatalogModelFactory) Lookup.getDefault().lookup(CatalogModelFactory.class);
        }
        if (implObj == null) {
            implObj = new Default();
        }
        return implObj;
    }
    
    public static class Default extends CatalogModelFactory {
        public CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException {
            return (CatalogModel) modelSource.getLookup().lookup(CatalogModel.class);
        }

        public LSResourceResolver getLSResourceResolver() {
            return null;
        }
    }
}
