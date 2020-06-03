/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.apt.impl.structure;

import org.netbeans.modules.cnd.antlr.TokenStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * base class for #if, #elif directives
 */
public abstract class APTIfConditionBaseNode extends APTTokenAndChildBasedNode
                                            implements Serializable {
    private static final long serialVersionUID = 1068728941146083839L;
    private List<APTToken> condition;
    private int endOffset;

    /** Copy constructor */
    /**package*/APTIfConditionBaseNode(APTIfConditionBaseNode orig) {
        super(orig);
        this.condition = orig.condition;
        this.endOffset = orig.getEndOffset();
    }

    /** Constructor for serialization */
    protected APTIfConditionBaseNode() {
    }

    /**
     * Creates a new instance of APTIfConditionBaseNode
     */
    protected APTIfConditionBaseNode(APTToken token) {
        super(token);
    }

    @Override
    public String getText() {
        String text = super.getText();
        String condStr;
        if (condition != null) {
            condStr = APTUtils.debugString(getCondition()).toString();
        } else {
            assert(true):"is it ok to have #if/#elif without condition?"; // NOI18N
            condStr = "<no condition>"; // NOI18N
        }
        return text + " CONDITION{" + condStr + "}"; // NOI18N
    }

    /** provides APTIf and APTElif interfaces support */
    public TokenStream getCondition() {
        return condition != null ? new ListBasedTokenStream(condition) : APTUtils.EMPTY_STREAM;
    }

    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        assert (token != null);
        int ttype = token.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE
        if (APTUtils.isEndDirectiveToken(ttype)) {
            endOffset = token.getOffset();
            if (condition == null) {
                if (DebugUtils.STANDALONE) {
                    System.err.printf("%s, line %d: %s with no expression%n", // NOI18N
                        APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim()); // NOI18N
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: {2} with no expression", // NOI18N
                            new Object[] {APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim()} );  // NOI18N
                }
            }
            if (condition != null){
                ((ArrayList)condition).trimToSize();
            }
            return false;
        } else if (!APTUtils.isCommentToken(ttype)) {
            if (condition == null) {
                condition = new ArrayList<APTToken>();
            }
            condition.add(token);
        }
        return true;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }
}
