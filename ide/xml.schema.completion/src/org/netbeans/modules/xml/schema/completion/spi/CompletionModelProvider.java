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
package org.netbeans.modules.xml.schema.completion.spi;

import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * <p>
 * The Schema-Aware Code Completion feature works on the basis of the 
 * {@code schemaLocation} or {@code noNamespaceSchemaLocation} attribute of 
 * the root element of the XML document. If a matching schema is found, it's 
 * model is queried to generate completion items.
 * </p>
 * <p>
 * CompletionModelProvider is a hook for XML documents that do not directly
 * conform to a schema through either the {@code schemaLocation} or 
 * {@code noNamespaceSchemaLocation} attributes but still want to use the 
 * Schema-Aware Code Completion feature.
 * </p>
 * <p>
 * For example, if you want code completion in a 
 * <a href="http://www.w3.org/TR/wsdl">WSDL</a> document, all you need to do is 
 * implement a CompletionModelProvider and return the set of 
 * {@link CompletionModel model}s for WSDL's schema(s).
 * </p>
 * @author Samaresh (Samaresh@Netbeans.Org)
 */
public abstract class CompletionModelProvider {
    
    /**
     * Returns a list of CompletionModels at a given context. This method is 
     * queried by the Schema-Aware Code Completion framework whenever code
     * completion is requested from an XML document. The context should be
     * used in determining the list of applicable CompletionModels. For example,
     * it does not make sense to return WSDL models while working on a non-WSDL 
     * file.
     * @param context a description of the context from which completion was
     * requested.
     * @return all CompletionModels that apply to the specified context.
     */
    public abstract List<CompletionModel> getModels(CompletionContext context);
    
    /**
     * A model that describes a schema that applies to an XML document at a
     * point where code completion is requested.
     */
    public abstract static class CompletionModel {
        
        /**
         * Returns the suggested prefix to be used for completion.
         * @return the suggested prefix for this schema model.
         */
        public abstract String getSuggestedPrefix();
        
        /**
         * Returns the target namespace for this schema model.
         * @return the target namespace for this schema model.
         */
        public abstract String getTargetNamespace();
        
        /**
         * Returns the schema model.
         * @return the model defining a schema that applies to the document at
         * the completion point.
         */
        public abstract SchemaModel getSchemaModel();
    }
}
