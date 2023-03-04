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
package org.netbeans.modules.css.model.impl;

import org.netbeans.modules.css.lib.api.NodeType;

/**
 *
 * @author marekfukala
 */
public class Utils {
        
    private static final String IMPLEMENTATIONS_PACKAGE = StyleSheetI.class.getPackage().getName();
    private static final char IMPLEMENTATIONS_SUFFIX = 'I'; //NOI18N
    
    
    
    //rule: grammar element name, first char in upper case + "I" postfix
    static String getImplementingClassNameForNodeType(NodeType nodeType) {
        return getImplementingClassNameForNodeType(nodeType.name());
    }
    
    static String getImplementingClassNameForNodeType(String typeName) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(IMPLEMENTATIONS_PACKAGE);
        sb.append('.');
        sb.append(getInterfaceForNodeType(typeName));
        sb.append(IMPLEMENTATIONS_SUFFIX);
        
        return sb.toString();
    }
    
    static String getInterfaceForNodeType(String typeName) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(typeName.charAt(0)));
        
        //underscores in element names conversion
        //
        //generic_at_rule --- should generate class name --- GenericAtRuleI
        for(int i = 1; i < typeName.length(); i++) {
            char c = typeName.charAt(i);
            if(c == '_') {
                //eat and convert next char to uppercase
                assert i < typeName.length() - 1 :
                    String.format("NodeType name %s cannot end with underscore!", typeName);
                i++;
                c = typeName.charAt(i);
                assert c != '_' : 
                        String.format("No two underscores in row can be preset in the NodeType %s name!", typeName);

                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(c);
            }
            
        }
        return sb.toString();
    }
    
    
}
