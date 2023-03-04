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

package org.netbeans.modules.xml.text.completion;

import java.util.Enumeration;

import org.w3c.dom.*;

import org.netbeans.modules.xml.api.model.*;

/**
 * This query always returns an empty result from all its query methods.
 *
 * @author  Petr Kuzel
 */
public class EmptyQuery implements GrammarQuery {

    /**
     * Shared instance.
     */
    public static final GrammarQuery INSTANCE = new EmptyQuery();

    // inherit JavaDoc from interface description

    public Enumeration queryEntities(String prefix) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryAttributes(HintContext ctx) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryElements(HintContext ctx) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryNotations(String prefix) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public Enumeration queryValues(HintContext ctx) {
        return org.openide.util.Enumerations.empty(); 
    }
    
    public boolean isAllowed(Enumeration en) {
        return false;
    }
    
    public GrammarResult queryDefault(HintContext virtualNodeCtx) {
        return null;
    }
    
    public java.awt.Component getCustomizer(HintContext ctx) {
        return null;
    }
    
    public boolean hasCustomizer(HintContext ctx) {
        return false;
    }

    public org.openide.nodes.Node.Property[] getProperties(HintContext ctx) {
        return null;
    }
    
    
}
