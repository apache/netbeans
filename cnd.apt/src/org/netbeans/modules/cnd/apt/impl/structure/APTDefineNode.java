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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * #define directive implementation
 */
public class APTDefineNode extends APTMacroBaseNode implements APTDefine, Serializable {
    private static final long serialVersionUID = -99267816578145490L;

    private APTToken[] params = null;
    private APTToken[] bodyTokens = null;

    private volatile int stateAndHashCode = BEFORE_MACRO_NAME;

    private static final byte BEFORE_MACRO_NAME = 0;
    private static final byte AFTER_MACRO_NAME = 1;
    private static final byte IN_PARAMS = 2;
    private static final byte IN_PARAMS_AFTER_ID = 3;
    private static final byte IN_PARAMS_AFTER_ELLIPSIS = 4;
    private static final byte IN_BODY = 5;
    private static final byte IN_BODY_AFTER_SHARP = 6;
    private static final byte IN_BODY_AFTER_LPAREN_AND_SHARP = 7;
    private static final byte ERROR = 8;

    /** Copy constructor */
    /**package*/APTDefineNode(APTDefineNode orig) {
        super(orig);
        this.params = orig.params;
        this.bodyTokens = orig.bodyTokens;
        this.stateAndHashCode = orig.stateAndHashCode;
    }

    /** Constructor for serialization */
    protected APTDefineNode() {
    }

    /** Creates a new instance of APTDefineNode */
    public APTDefineNode(APTToken token) {
        super(token);
    }

    /** Creates a new instance of APTDefineNode for pragma once*/
    public APTDefineNode(APTToken token, APTToken fileName) {
        super(token, fileName);
    }

    @Override
    public final int getType() {
        return APT.Type.DEFINE;
    }

    @Override
    public Collection<APTToken> getParams() {
        if (params == null) {
            return null;
        } else {
            return Collections.<APTToken>unmodifiableList(Arrays.asList(params));// != null ? (APTToken[]) params.toArray(new APTToken[params.size()]) : null;
        }
    }

    @Override
    public boolean isFunctionLike() {
        return params != null;
    }

    /**
     * returns List of Tokens of macro body
     */
    @Override
    public List<APTToken> getBody() {
        if (bodyTokens == null) {
            return Collections.<APTToken>emptyList();
        } else {
            return Arrays.asList(bodyTokens);
        }
    }

    /**
     * returns true if #define directive is valid
     */
    @Override
    public boolean isValid() {
        return stateAndHashCode != ERROR;
    }

    @Override
    public boolean accept(APTFile curFile, APTToken token) {
        throw new IllegalStateException("Do not call accept on APTDefineNode directly, use APTDefineNode.Builder"); //NOI18N
    }

    private boolean superAccept(APTFile curFile, APTToken token) {
        return super.accept(curFile, token);
    }

    public static class Builder implements APTNodeBuilder {
        private List<APTToken> params = null;
        private List<APTToken> bodyTokens = null;

        private final APTDefineNode node;

        public Builder(APTToken token) {
            node = new APTDefineNode(token);
        }

        @Override
        public APTDefineNode getNode() {
            if (params != null) {
                node.params = params.toArray(new APTToken[params.size()]);
            }
            if (bodyTokens != null) {
                node.bodyTokens = bodyTokens.toArray(new APTToken[bodyTokens.size()]);
            }
            return node;
        }

        @Override
        public boolean accept(APTFile curFile, APTToken token) {
            assert node.stateAndHashCode >= 0 && node.stateAndHashCode <= ERROR;
            int ttype = token.getType();
            if (APTUtils.isEndDirectiveToken(ttype)) {
                if (bodyTokens != null){
                    ((ArrayList<?>)bodyTokens).trimToSize();
                }
                if (params != null){
                    if (params.isEmpty()) {
                        params = Collections.<APTToken>emptyList();
                    } else {
                        ((ArrayList<?>)params).trimToSize();
                        if (bodyTokens != null && !bodyTokens.isEmpty()) {
                            int index = params.size()-1;
                            // check if any named variadic parameter to replace by VA_ARGS_TOKEN
                            APTToken lastParam = params.get(index);
                            if (lastParam.getType() == APTTokenTypes.ELLIPSIS) {
                                assert lastParam instanceof APTBaseLanguageFilter.FilterToken : "it must be filtered ellipsis token " + lastParam;
                                APTToken originalToken = ((APTBaseLanguageFilter.FilterToken)lastParam).getOriginalToken();
                                assert originalToken != null;
                                assert originalToken.getType() == APTTokenTypes.IDENT : "original token must be ID " + originalToken;
                                CharSequence name = originalToken.getTextID();
                                params.set(index, APTUtils.VA_ARGS_TOKEN);
                                for (int i = 0; i < bodyTokens.size(); i++) {
                                    APTToken cur = bodyTokens.get(i);
                                    if (cur.getType() == APTTokenTypes.IDENT && cur.getTextID().equals(name)) {
                                        bodyTokens.set(i, APTUtils.VA_ARGS_TOKEN);
                                    }
                                }
                            }
                        }
                    }
                }
                if (node.stateAndHashCode == BEFORE_MACRO_NAME) {
                    // macro without name
                    node.stateAndHashCode = ERROR;
                }
                return false;
            } else {
                switch (node.stateAndHashCode) {
                    case BEFORE_MACRO_NAME:
                    {
                        if (APTUtils.isCommentToken(ttype)) {
                            return true;
                        }
                        // allow base class to remember macro nam
                        boolean accepted = node.superAccept(curFile, token);
                        if (!accepted) {
                            node.stateAndHashCode = ERROR;
                        } else {
                            node.stateAndHashCode = AFTER_MACRO_NAME;
                        }
                        break;
                    }
                    case AFTER_MACRO_NAME:
                    {
                        if (token.getType() == APTTokenTypes.FUN_LIKE_MACRO_LPAREN) {
                            params = new ArrayList<APTToken>();
                            node.stateAndHashCode = IN_PARAMS;
                        } else {
                            if (bodyTokens == null) {
                                bodyTokens = new ArrayList<APTToken>();
                            }
                            bodyTokens.add(token);
                            node.stateAndHashCode = IN_BODY;
                        }
                        break;
                    }
                    case IN_PARAMS:
                    {
                        switch (token.getType()) {
                            case APTTokenTypes.IDENT:
                                params.add(token);
                                node.stateAndHashCode = IN_PARAMS_AFTER_ID;
                                break;
                            case APTTokenTypes.RPAREN:
                                node.stateAndHashCode = IN_BODY;
                                break;
                            case APTTokenTypes.ELLIPSIS:
                                // support ELLIPSIS for IZ#83949
                                params.add(APTUtils.VA_ARGS_TOKEN);
                                node.stateAndHashCode = IN_PARAMS_AFTER_ELLIPSIS;
                                break;
                            default:
                                // eat comma and comments and leave state
                                if (!APTUtils.isCommentToken(token.getType())) {
                                    node.logError(curFile, token);
                                    node.stateAndHashCode = ERROR;
                                }
                                break;
                        }
                        break;
                    }
                    case IN_PARAMS_AFTER_ELLIPSIS:
                    {
                        switch (token.getType()) {
                            case APTTokenTypes.RPAREN:
                                node.stateAndHashCode = IN_BODY;
                                break;
                            default:
                                // eat comments and leave state
                                if (!APTUtils.isCommentToken(token.getType())) {
                                    node.logError(curFile, token);
                                    node.stateAndHashCode = ERROR;
                                }
                                break;
                        }
                        break;
                    }
                    case IN_PARAMS_AFTER_ID:
                    {
                        switch (token.getType()) {
                            case APTTokenTypes.RPAREN:
                                node.stateAndHashCode = IN_BODY;
                                break;
                            case APTTokenTypes.ELLIPSIS:
                                //previous parameter is variadic named token
                                // #195560 - more support for variadic variables in macro
                                if (params.isEmpty()) {
                                    node.logError(curFile, token);
                                    node.stateAndHashCode = ERROR;
                                } else {
                                    int index = params.size() - 1;
                                    APTToken last = params.get(index);
                                    token = new APTBaseLanguageFilter.FilterToken(last, APTTokenTypes.ELLIPSIS);
                                    params.set(index, token);
                                    node.stateAndHashCode = IN_PARAMS_AFTER_ELLIPSIS;
                                }
                                break;
                            case APTTokenTypes.COMMA:
                                node.stateAndHashCode = IN_PARAMS;
                                break;
                            default:
                                // eat comma and comments and leave state
                                if (!APTUtils.isCommentToken(token.getType())) {
                                    node.logError(curFile, token);
                                    node.stateAndHashCode = ERROR;
                                }
                                break;
                        }
                        break;
                    }
                    case IN_BODY:
                    {
                        // init body list if necessary
                        if (bodyTokens == null) {
                            bodyTokens = new ArrayList<APTToken>();
                        }
                        // check for errors with sharp for function like macro:
                        if (token.getType() == APTTokenTypes.SHARP && params != null) {
                            node.stateAndHashCode = IN_BODY_AFTER_SHARP;
                            // there is a special case of escaping sharp by putting it between parens like '(#)'
                            if (bodyTokens.size() > 0 && bodyTokens.get(bodyTokens.size() - 1).getType() == APTTokenTypes.LPAREN) {
                                node.stateAndHashCode = IN_BODY_AFTER_LPAREN_AND_SHARP;
                            }
                        }
                        bodyTokens.add(token);
                        break;
                    }
                    case IN_BODY_AFTER_LPAREN_AND_SHARP:
                    case IN_BODY_AFTER_SHARP:
                    {
                        bodyTokens.add(token);
                        // skip comments
                        if (APTUtils.isCommentToken(token.getType())) {
                            // stay in the current state
                        } else if (token.getType() == APTTokenTypes.IDENT) {
                            // error check: token after # must be parameter or it must be # with leading LPAREN
                            node.stateAndHashCode = (node.stateAndHashCode == IN_BODY_AFTER_LPAREN_AND_SHARP) || isInParamList(token) ? IN_BODY : ERROR;
                        } else {
                            // special case is '(#)' - sharp between parens
                            if (node.stateAndHashCode == IN_BODY_AFTER_LPAREN_AND_SHARP && token.getType() == APTTokenTypes.RPAREN) {
                                node.stateAndHashCode = IN_BODY;
                            } else {
                                // only id is accepted after #
                                node.stateAndHashCode = ERROR;
                            }
                        }
                        if (node.stateAndHashCode == ERROR) {
                            if (DebugUtils.STANDALONE) {
                                System.err.printf("%s, line %d: '#' is not followed by a macro parameter%n", // NOI18N
                                        APTTraceUtils.toFileString(curFile), node.getToken().getLine());
                            } else {
                                APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: '#' is not followed by a macro parameter", // NOI18N
                                        new Object[] {APTTraceUtils.toFileString(curFile), node.getToken().getLine()} );
                            }
                        }
                        break;
                    }
                    case ERROR:
                    {
                        // eat all after error
                        break;
                    }
                    default:
                        assert(false) : "unexpected state"; // NOI18N
                }
                return true;
            }
        }

        private boolean isInParamList(APTToken id) {
            assert id != null;
            if (params == null) {
                return false;
            }
            for (APTToken param : params) {
                if (param.getTextID().equals(id.getTextID())) {
                    return true;
                }
            }
            return false;
        }
    }

    private void logError(APTFile curFile, APTToken token) {
        // error report
        if (DebugUtils.STANDALONE) {
            System.err.printf("%s, line %d: \"%s\" may not appear in macro parameter list%n", // NOI18N
                    APTTraceUtils.toFileString(curFile), getToken().getLine(), token.getText()); // NOI18N
        } else {
            APTUtils.LOG.log(Level.SEVERE, "{0} line {1}: {2} may not appear in macro parameter list", // NOI18N
                    new Object[]{APTTraceUtils.toFileString(curFile), getToken().getLine(), token.getText()}); // NOI18N
        }
    }

    @Override
    public String getText() {
        String ret = super.getText();
        String paramStr = ""; // NOI18N
        if (params != null) {
            paramStr = "PARAMS{" + APTUtils.toString(new ListBasedTokenStream(Arrays.asList(this.params))) + "}"; // NOI18N
        }
        String bodyStr;
        if (bodyTokens != null) {
            bodyStr = "BODY{" + APTUtils.toString(new ListBasedTokenStream(getBody())) + "}"; // NOI18N
        } else {
            bodyStr = "{NO BODY}"; // NOI18N
        }
        return ret + paramStr + bodyStr;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final APTDefineNode other = (APTDefineNode) obj;
        if (!Arrays.equals(this.params, other.params)) {
            return false;
        }
        if (!Arrays.equals(this.bodyTokens, other.bodyTokens)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = stateAndHashCode;
        // if state was ERROR, leave it as hashcode as well, otherwise calc and cache hashcode
        if (0 <= hash && hash < ERROR) {
            hash = super.hashCode();
            hash = 37 * hash + Arrays.hashCode(this.params);
            hash = 37 * hash + Arrays.hashCode(this.bodyTokens);
            hash = APTUtils.hash(hash);
            if (0 <= hash && hash <= ERROR) {
                hash += ERROR + ERROR;
            }
            stateAndHashCode = hash;
        }
        return hash;
    }
}
