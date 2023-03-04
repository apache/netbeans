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
package org.netbeans.modules.html.angular.model;

import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * Angular directive attribute form convention.
 *
 * @author marekfukala
 */
public enum DirectiveConvention {
    
    /**
     *  data-ng-app
     */
    data_dash("data", '-'), //NOI18N
    
    /**
     *  data-ng_app
     */
    data_underscore("data", '_'), //NOI18N
    
    /**
     *  data-ng:app //is it possible?
     */
    data_colon("data", ':'), //NOI18N
    
    /**
     *  data-ng-app
     */
    x_dash("x", '-'), //NOI18N
    
    /**
     *  data-ng_app
     */
    x_underscore("x", '_'), //NOI18N
    
    /**
     *  data-ng:app //is it possible?
     */
    x_colon("x", ':'), //NOI18N
    
    //note: base_* members needs to be last - see the getConvention() logic
    
    /**
     * ng-app
     */
    base_dash(null, '-'),
    
    /**
     * ng_app
     */
    base_underscore(null, '_'),
    
    /**
     * ng:app
     */
    base_colon(null, ':');
      
    private static final String NG_PREFIX = "ng"; //NOI18N
    
    private final char delimiter;
    private final String fullPrefix;
    
    private DirectiveConvention(String prefix, char delimiter) {
        this.delimiter = delimiter;
        this.fullPrefix = prefix == null ? NG_PREFIX : prefix + '-' + NG_PREFIX; //XXX is this correct? data-ng_bind? or data_ng_bind???
    }

    String createFQN(Directive directive) {
        StringBuilder sb = new StringBuilder();
        sb.append(fullPrefix);
        sb.append(delimiter);
        sb.append(directive.getAttributeCoreName(delimiter));
        
        return sb.toString();
    }

    /**
     * Checks whether the attribute name fits to one of the AJS conventions.
     * @param attributeName
     * @return the convention or null
     */
    public static DirectiveConvention getConvention(CharSequence attributeName) {
        for(DirectiveConvention dc : values()) {
            //data-ng:bind
            if(LexerUtils.startsWith(attributeName, dc.fullPrefix, true, false)) {
                if(attributeName.length() > dc.fullPrefix.length()) {
                    char delimiterChar = attributeName.charAt(dc.fullPrefix.length());
                    if(dc.delimiter == delimiterChar) {
                        return dc;
                    }
                }
            }
        }
        return null;
    }
    
}
