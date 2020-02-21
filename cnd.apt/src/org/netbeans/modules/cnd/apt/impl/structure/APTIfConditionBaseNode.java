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
