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

package org.netbeans.modules.web.jsf.editor.index;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;

/**
 *
 * @author marekfukala
 */
public abstract class JsfPageModelFactory {

    private static final Collection<JsfPageModelFactory> FACTORIES = new ArrayList<>();
    static {
        FACTORIES.add(new CompositeComponentModel.Factory());
        FACTORIES.add(new ResourcesMappingModel.Factory());
    }

    public static Collection<JsfPageModel> getModels(HtmlParserResult result) {
        Collection<JsfPageModel> models = new ArrayList<>();
        for(JsfPageModelFactory factory : FACTORIES) {
            JsfPageModel model = factory.getModel(result);
            if(model != null) {
                models.add(model);
            }
        }
        return models;
    }

    public static JsfPageModelFactory getFactory(Class modelFactoryClass) {
        for(JsfPageModelFactory factory : FACTORIES) {
            if(factory.getClass().equals(modelFactoryClass)) {
                return factory;
            }
        }
        return null;
    }

    public abstract JsfPageModel getModel(HtmlParserResult result);

    public abstract JsfPageModel loadFromIndex(IndexResult result);
    
}
