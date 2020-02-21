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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTExpandedStream;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;
import org.netbeans.modules.cnd.apt.utils.TokenBasedTokenStream;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * #include and #include_next base implementation
 */
public abstract class APTIncludeBaseNode extends APTTokenBasedNode
        implements APTNodeBuilder, Serializable {

    private static final long serialVersionUID = -2311241687965334550L;
    // support pp-tokens as include stream
    // expanded later based on macro map;
    // we support INCLUDE_STRING, SYS_INLUDE_STRING
    // and macro expansion like #include MACRO_EXPRESSION
    private APTToken includeFileToken = EMPTY_INCLUDE;
    private int endOffset = 0;

    /** Copy constructor */
    /**package*/
    APTIncludeBaseNode(APTIncludeBaseNode orig) {
        super(orig);
        this.includeFileToken = orig.includeFileToken;
    }

    /** Constructor for serialization */
    protected APTIncludeBaseNode() {
    }

    /**
     * Creates a new instance of APTIncludeBaseNode
     */
    protected APTIncludeBaseNode(APTToken token) {
        super(token);
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public APT getFirstChild() {
        return null;
    }

    @Override
    public void setFirstChild(APT child) {
        // do nothing
        assert (false) : "include doesn't support children"; // NOI18N
    }

    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        int ttype = token.getType();
        if (APTUtils.isEndDirectiveToken(ttype)) {
            endOffset = token.getOffset();
            return false;
        }
        // eat all till END_PREPROC_DIRECTIVE
        switch (ttype) {
            case APTTokenTypes.INCLUDE_STRING:
            case APTTokenTypes.SYS_INCLUDE_STRING:
                if (includeFileToken == EMPTY_INCLUDE) {
                    this.includeFileToken = token;
                } else {
                    // append new token
                    ((MultiTokenInclude) includeFileToken).addToken(token);
                }
                break;
            case APTTokenTypes.COMMENT:
            case APTTokenTypes.CPP_COMMENT:
            case APTTokenTypes.FORTRAN_COMMENT:
                // just skip comments, they are valid
                break;
            default:
                // token stream of macro expressions
                if (includeFileToken == EMPTY_INCLUDE) {
                    // the first token of expression
                    includeFileToken = new MultiTokenInclude(token);
                } else {
                    // not the first token
                    if (isSimpleIncludeToken()) {
                        // remember old token
                        includeFileToken = new MultiTokenInclude(includeFileToken);
                    }
                    // append new token
                    ((MultiTokenInclude) includeFileToken).addToken(token);
                }
        }
        return true;
    }
    
    @Override
    public APTBaseNode getNode() {
        return this;
    }

    @Override
    public String getText() {
        String ret = super.getText();
        if (isSimpleIncludeToken()) {
            ret += " INCLUDE{" + (isSystem(null) ? "<S> " : "<U> ") + getInclude() + "}"; // NOI18N
        } else if (includeFileToken == EMPTY_INCLUDE) {
            ret += " INCLUDE{ **EMPTY** }"; // NOI18N
        } else {
            ret += " INCLUDE{ <M> " + getInclude() + "}"; // NOI18N
        }
        return ret;
    }
    ////////////////////////////////////////////////////////////////////////////
    // impl of interfaces APTInclude and APTIncludeNext

    public TokenStream getInclude() {
        if (isSimpleIncludeToken()) {
            return new TokenBasedTokenStream(includeFileToken);
        } else if (includeFileToken == EMPTY_INCLUDE) {
            return APTUtils.EMPTY_STREAM;
        } else {
            return new ListBasedTokenStream(((MultiTokenInclude) includeFileToken).getTokenList());
        }
    }

    public String getFileName(APTMacroCallback callback) {
        CharSequence file = getIncludeString(callback);
        String out = ""; // NOI18N
        if (file != null) {
            if (file.length() > 2) {
                if (file.charAt(0) == '<') { // NOI18N
                    for (int i = 2; i < file.length(); i++) {
                        if (file.charAt(i) == '>') { // NOI18N
                            out = file.subSequence(1, i).toString();
                            break;
                        }
                    }
                } else if (file.charAt(0) == '"') { // NOI18N
                    for (int i = 2; i < file.length(); i++) {
                        if (file.charAt(i) == '\"') { // NOI18N
                            out = file.subSequence(1, i).toString();
                            break;
                        }
                    }
                }
            }
        }
        return CndPathUtilities.normalizeUnixPath(out);
    }

    public boolean isSystem(APTMacroCallback callback) {
        CharSequence file = getIncludeString(callback);
        return file.length() > 0 ? file.charAt(0) == '<' : false; // NOI18N
    }

    private CharSequence getIncludeString(APTMacroCallback callback) {
        assert (includeFileToken != null);
        CharSequence file;
        if (!isSimpleIncludeToken()) {
            file = stringize(((MultiTokenInclude) includeFileToken).getTokenList(), callback);
        } else {
            file = includeFileToken.getTextID();
        }
        return file;
    }

    private boolean isSimpleIncludeToken() {
        assert (includeFileToken != null);
        return includeFileToken.getType() == APTTokenTypes.INCLUDE_STRING ||
                includeFileToken.getType() == APTTokenTypes.SYS_INCLUDE_STRING;
    }
    private static final MultiTokenInclude EMPTY_INCLUDE = new MultiTokenInclude(null);

    //TODO: what about Serializable
    private static final class MultiTokenInclude extends APTTokenAbstact {

        private final List<APTToken> origTokens;

        public MultiTokenInclude(APTToken token) {
            if (token != null) {
                origTokens = new ArrayList<APTToken>(1);
                origTokens.add(token);
            } else {
                origTokens = Collections.emptyList();
            }
        }

        public void addToken(APTToken token) {
            assert origTokens != null;
            origTokens.add(token);
        }

        @Override
        public String getText() {
            if (origTokens.size() > 0) {
                return stringize(getTokenList(), null).toString();
            } else {
                return "{no include information}"; // NOI18N
            }
        }

        public List<APTToken> getTokenList() {
            return origTokens;
        }
    };

    private static CharSequence stringize(List<APTToken> tokens, APTMacroCallback callback) {
        TokenStream expanded;
        if (callback != null) {
            expanded = new APTExpandedStream(new ListBasedTokenStream(tokens), callback);
        } else {
            expanded = new ListBasedTokenStream(tokens);
        }
        return APTUtils.stringize(expanded, true);
    }
}
