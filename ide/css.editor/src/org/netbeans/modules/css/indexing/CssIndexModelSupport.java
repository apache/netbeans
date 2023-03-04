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
package org.netbeans.modules.css.indexing;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.css.indexing.api.CssIndexModel;
import org.netbeans.modules.css.indexing.api.CssIndexModelFactory;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class CssIndexModelSupport {

    public static Collection<CssIndexModel> getModels(CssParserResult result) {
        Collection<CssIndexModel> models = new ArrayList<>();
        Collection<? extends CssIndexModelFactory> factories = Lookup.getDefault().lookupAll(CssIndexModelFactory.class);

        for (CssIndexModelFactory factory : factories) {
            CssIndexModel model = factory.getModel(result);
            if (model != null) {
                models.add(model);
            }
        }
        return models;
    }

    public static CssIndexModelFactory getFactory(Class modelFactoryClass) {
        Collection<? extends CssIndexModelFactory> factories = Lookup.getDefault().lookupAll(CssIndexModelFactory.class);
        for (CssIndexModelFactory factory : factories) {
            if (factory.getClass().equals(modelFactoryClass)) {
                return factory;
            }
        }
        return null;
    }
    
}
