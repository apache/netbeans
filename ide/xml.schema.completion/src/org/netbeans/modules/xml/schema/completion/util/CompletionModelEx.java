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

import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionModelEx extends CompletionModel {
    private String prefix;
    private SchemaModel model;
    private CompletionContextImpl context;
    
    public CompletionModelEx(CompletionContextImpl context,
            String prefix, SchemaModel model) {
        this.prefix = prefix;
        this.model = model;
        this.context = context;
    }
    
    public String getSuggestedPrefix() {
//        if(prefix == null && getTargetNamespace() != null) {
//            this.prefix = context.suggestPrefix(getTargetNamespace()); //NOI18N
//        }
        return prefix;
    }
    
    public SchemaModel getSchemaModel() {
        return model;
    }
    
    public String getTargetNamespace() {
        Schema schema = model.getSchema();
        if (schema != null) {
            return schema.getTargetNamespace();
        }
        return null;
    }
}
