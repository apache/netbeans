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
package org.netbeans.modules.css.prep.editor;

import org.netbeans.modules.css.editor.module.spi.SemanticAnalyzer;
import org.netbeans.modules.css.editor.module.spi.SemanticAnalyzerResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;

/**
 *
 * @author marekfukala
 */
public class CPSemanticAnalyzer extends SemanticAnalyzer {

    //TODO implement real error checking for the declarations with less syntax,
    //right now it is used just to ignore the less syntax by the "pure css analyzer"
    @Override
    public SemanticAnalyzerResult analyzeDeclaration(Node declarationNode) {
        if(!NodeUtil.getChildrenRecursivelyByType(declarationNode, NodeType.cp_variable).isEmpty()) {
            //declaration contains some CP code => mark as valid so the plain analyzer won't attemp to check the value
            return SemanticAnalyzerResult.VALID;
        }
        
        if(NodeUtil.getAncestorByType(declarationNode, NodeType.sass_nested_properties) != null) {
            //the declaration lies in a nested properties block, so the property name ca be just a suffix of the real name:
            //font: {
            //  size: 10px;
            //}
            return SemanticAnalyzerResult.VALID;
        }
        
        return SemanticAnalyzerResult.UNKNOWN;
    }
    
}
