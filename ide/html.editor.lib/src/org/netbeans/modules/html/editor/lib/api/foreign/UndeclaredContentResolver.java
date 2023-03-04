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
package org.netbeans.modules.html.editor.lib.api.foreign;

import java.util.List;
import java.util.Map;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Named;

/**
 * Allows to resolve undeclared content in html sources.
 * 
 * @author marekfukala
 */
public interface UndeclaredContentResolver {
    
     /**
     * This method allows to bind some prefixed html source 
     * elements and attributes to a physically undeclared namespace.
     * 
     * @param the html source which is being processed
     * @return a map of namespace to prefix collection
     */
    public Map<String, List<String>> getUndeclaredNamespaces(HtmlSource source);
    
    /**
     * Returns true if the given element is a custom tag known to this resolver.
     * @param element
     * @return 
     */
    public boolean isCustomTag(Named element, HtmlSource source);
   
    /**
     * Returns true if the given element's attribute is a custom attribute known to this resolver.
     * 
     * @param attribute
     * @return 
     */
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source);

}
