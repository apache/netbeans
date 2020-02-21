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
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base implementation of nodes with associated stream
 *
 */
public abstract class APTStreamBaseNode extends APTTokenBasedNode
                                        implements APTNodeBuilder, Serializable {
    private static final long serialVersionUID = -1498074871896804293L;
    private List<APTToken> tokens;
    
    /** Copy constructor */
    /**package*/ APTStreamBaseNode(APTStreamBaseNode orig) {
        super(orig);
        this.tokens = orig.tokens;
    }
    
    /** Constructor for serialization **/
    protected APTStreamBaseNode() {
    }
    
    /**
     * Creates a new instance of APTStreamBaseNode
     */
    public APTStreamBaseNode(APTToken token) {
        super(token);
        assert (validToken(token)) : "must init only from valid tokens"; // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation of abstract methods
    
    @Override
    public boolean accept(APTFile curFile,APTToken token) {
        boolean accepted = false;
        if (validToken(token)) {
            accepted = true;
            appendToken(token);
        }
        return accepted;
    }
    
    /**
     * APTStream node doesn't have children
     */
    @Override
    public APT getFirstChild() {
        return null;
    }
    
    /**
     * APTStream node doesn't have children
     */
    @Override
    public void setFirstChild(APT child) {
        assert(false) : "stream node doesn't support children"; // NOI18N
    }
    
    /** returns list of tokens */
    @Override
    public String getText() {
        StringBuilder retValue = new StringBuilder("TOKENS{"); // NOI18N
        try {
            TokenStream ts = getTokenStream();
            for (APTToken token = (APTToken) ts.nextToken(); !APTUtils.isEOF(token);) {
                assert(token != null) : "list of tokens must not have 'null' elements"; // NOI18N
                retValue.append(token.toString());
                token = (APTToken) ts.nextToken();
                if (!APTUtils.isEOF(token)) {
                    retValue.append("; "); // NOI18N
                }
            }
        } catch (TokenStreamException ex) {
            assert(false);
        }
        return retValue.append('}').toString(); // NOI18N
    }
    
    @Override
    public APTBaseNode getNode() {
        return this;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation of APTStream interface
    
    /**
     * returns reset token stream of the node;
     * use this method to get first access to token stream,
     * do not use this method as each time getter,
     * reset stream means, that token stream's iterator
     * moved to the begin of the stream
     */
    public TokenStream getTokenStream() {
        return new TokenStreamIterator(getToken(), this.tokens);
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // help implementation methods
    
    private void appendToken(APTToken token) {
        assert (validToken(token)) : "must append only valid tokens"; // NOI18N
        if (tokens == null) {
            tokens = new LinkedList<APTToken>();
        }
        tokens.add(token);
    }
    
    protected abstract boolean validToken(APTToken t);
    
    /** token stream iterator */
    private static class TokenStreamIterator implements TokenStream {
        private final APTToken firstToken;
        private final Iterator<APTToken> tokens;
        private boolean first = true;
        public TokenStreamIterator(APTToken firstToken, List<APTToken> tokens) {
            this.firstToken = firstToken;
            if (tokens != null) {
                this.tokens = tokens.iterator();
            } else {
                this.tokens = null;
            }
        }
        
        @Override
        public APTToken nextToken() throws TokenStreamException {
            if (first) {
                first =false;
                return firstToken;
            }
            if (tokens != null) {
                if (tokens.hasNext()) {
                    return tokens.next();
                }
            }
            return APTUtils.EOF_TOKEN;
        }
    }
}
