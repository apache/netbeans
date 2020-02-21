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

import java.io.Serializable;
import java.util.logging.Level;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #ifndef/#ifdef directives base implementation
 */
public abstract class APTIfdefConditionBaseNode extends APTTokenAndChildBasedNode
                                                implements Serializable {
    private static final long serialVersionUID = -5900095440680811076L;
    private APTToken macroName;
    private int endOffset;

    /** Copy constructor */
    /**package*/ APTIfdefConditionBaseNode(APTIfdefConditionBaseNode orig) {
        super(orig);
        this.macroName = orig.macroName;
        this.endOffset = orig.endOffset;
    }

    /** Constructor for serialization */
    protected APTIfdefConditionBaseNode() {
    }

    /** Creates a new instance of APTIfdefConditionBaseNode */
    protected APTIfdefConditionBaseNode(APTToken token) {
        super(token);
    }

    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        /** base implementation of #ifdef/#ifndef */
        if (APTUtils.isID(token)) {
            if (macroName != null) {
                // init macro name only once
                if (DebugUtils.STANDALONE) {
                    System.err.printf("%s, line %d: extra tokens after %s at end of %s directive%n", // NOI18N
                            APTTraceUtils.toFileString(curFile), getToken().getLine(), macroName.getText(), getToken().getText().trim()); // NOI18N
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: extra tokens after {2} at end of {3} directive", // NOI18N
                            new Object[] {APTTraceUtils.toFileString(curFile), getToken().getLine(), macroName.getText(), getToken().getText().trim()} ); // NOI18N
                }
            } else {
                this.macroName = token;
            }
        } else if (token.getType() == APTTokenTypes.DEFINED) {
            // "defined" cannot be used as a macro name
            if (DebugUtils.STANDALONE) {
                System.err.printf("%s, line %d: \"defined\" cannot be used as a macro name%n", // NOI18N
                                    APTTraceUtils.toFileString(curFile), getToken().getLine()); // NOI18N
            } else {
                APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: \"defined\" cannot be used as a macro name", // NOI18N
                        new Object[] {APTTraceUtils.toFileString(curFile), getToken().getLine()} ); // NOI18N
            }
        }
        // eat all till END_PREPROC_DIRECTIVE
        if (APTUtils.isEndDirectiveToken(token.getType())) {
            endOffset = token.getOffset();
            if (macroName == null) {
                if (DebugUtils.STANDALONE) {
                    System.err.printf("%s, line %d: no macro name given in %s directive%n", // NOI18N
                        APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim());
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: no macro name given in {2} directive ", // NOI18N
                            new Object[] {APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim()} );
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public String getText() {
        assert (getToken() != null) : "must have valid preproc directive"; // NOI18N
        // macro name could be null for incorrect constructions
        // assert (getMacroName() != null) : "must have valid macro"; // NOI18N
        String retValue = super.getText();
        if (getMacroName() != null) {
            retValue += " MACRO{" + getMacroName() + "}"; // NOI18N
        }
        return retValue;
    }

    /** base implementation for #ifdef/#ifndef */
    public APTToken getMacroName() {
        return macroName;
    }

}
