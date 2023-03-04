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
package org.netbeans.modules.css.lib;

import org.antlr.runtime.CommonToken;
import org.netbeans.modules.css.lib.api.NodeType;

/**
 *
 * @author marekfukala
 */
public class RuleNode extends AbstractParseTreeNode {
    
    private NodeType rule;
    int from = -1, to = -1;
    
    RuleNode(NodeType rule, CharSequence source) {
        super(source);
        this.rule = rule;
    }
    
    //used by NbParseTreeBuilder
    void setFirstToken(CommonToken token) {
        assert token != null : "Attempting to set null first token in rule " + name();
        this.from = CommonTokenUtil.getCommonTokenOffsetRange(token)[0];
    }
    
    void setLastToken(CommonToken token) {
        assert token != null : "Attempting to set null last token in rule " + name();
        this.to = CommonTokenUtil.getCommonTokenOffsetRange(token)[1];
    }

    @Override
    public int from() {
        return from;
    }

    @Override
    public int to() {
        return to;
    }

    @Override
    public NodeType type() {
        return rule;
    }

    @Override
    public String name() {
        return type().name();
    }

}
