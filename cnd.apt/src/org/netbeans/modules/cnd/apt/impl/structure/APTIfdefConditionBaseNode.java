/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
