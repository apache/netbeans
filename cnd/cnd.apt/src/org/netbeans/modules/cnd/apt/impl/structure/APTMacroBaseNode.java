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
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base class for #define/#undef impl
 */
public abstract class APTMacroBaseNode extends APTTokenBasedNode
                                        implements Serializable {
    private static final long serialVersionUID = 1315417078059538898L;
    private APTToken macroName = EMPTY_NAME;

    /** Copy constructor */
    /**package*/APTMacroBaseNode(APTMacroBaseNode orig) {
        super(orig);
        this.macroName = orig.macroName;
    }

    /** Constructor for serialization **/
    protected APTMacroBaseNode() {
    }

    /** Creates a new instance of APTMacroBaseNode */
    public APTMacroBaseNode(APTToken token) {
        super(token);
    }

    /** Creates a new instance of APTMacroBaseNode for pragma once */
    public APTMacroBaseNode(APTToken token, APTToken fileName) {
        super(token);
        macroName = fileName;
    }

    @Override
    public APT getFirstChild() {
        // #define/#undef doesn't have subtree
        return null;
    }

    @Override
    public void setFirstChild(APT child) {
        // do nothing
        assert (false) : "define/undef doesn't support children"; // NOI18N
    }

    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        if (APTUtils.isEndDirectiveToken(token.getType())) {
            return false;
        }
        if (APTUtils.isCommentToken(token)) {
            return true;
        }
        if (APTUtils.isID(token)) {
            if (macroName != EMPTY_NAME) {
                // init macro name only once
                if (DebugUtils.STANDALONE) {
                    System.err.printf("%s, line %d: warning: extra tokens at end of %s directive%n", // NOI18N
                            APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim()); // NOI18N
                } else {
                    APTUtils.LOG.log(Level.WARNING, "{0}, line {1}: warning: extra tokens at end of {2} directive", // NOI18N
                            new Object[] {APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim()} ); // NOI18N
                }
                return false;
            } else {
                this.macroName = token;
            }
        } else {
            // everything else is not expected here
            if (DebugUtils.STANDALONE) {
                System.err.printf("%s, line %d: warning: unexpected token %s%n", // NOI18N
                        APTTraceUtils.toFileString(curFile), getToken().getLine(), token.getText().trim()); // NOI18N
            } else {
                APTUtils.LOG.log(Level.WARNING, "{0}, line {1}: warning: unexpected token {2}", // NOI18N
                        new Object[]{APTTraceUtils.toFileString(curFile), getToken().getLine(), token.getText().trim()}); // NOI18N
            }
            return false;
        }
        // eat all till END_PREPROC_DIRECTIVE
        return true;
    }

    @Override
    public String getText() {
        assert (getToken() != null) : "must have valid preproc directive"; // NOI18N
        assert (getName() != null) : "must have valid macro"; // NOI18N
        String retValue = super.getText();
        if (getName() != null) {
            retValue += " MACRO{" + getName() + "}"; // NOI18N
        }
        return retValue;
    }

    public APTToken getName() {
        return macroName;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final APTMacroBaseNode other = (APTMacroBaseNode) obj;
        if (this.macroName != other.macroName && (this.macroName == null || !this.macroName.equals(other.macroName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 37 * hash + (this.macroName != null ? this.macroName.hashCode() : 0);
        return hash;
    }

    private static final NotHandledMacroName EMPTY_NAME = new NotHandledMacroName();

    //TODO: what about Serializable
    private static final class NotHandledMacroName extends APTTokenAbstact {
        public NotHandledMacroName() {
        }

        @Override
        public String getText() {
            return "<<DUMMY>>"; // NOI18N
        }

        @Override
        public int hashCode() {
            return -1;
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return this == obj;
        }

    };
}
