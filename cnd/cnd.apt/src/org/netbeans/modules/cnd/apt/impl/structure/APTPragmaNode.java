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
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTPragma;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
public class APTPragmaNode extends APTStreamBaseNode implements APTPragma, Serializable {

    private static final long serialVersionUID = 1L;
    private APTToken name;

    /** Copy constructor */
    /**package*/ APTPragmaNode(APTPragmaNode orig) {
        super(orig);
        name = orig.name;
    }

    /** constructor for serialization **/
    protected APTPragmaNode() {
    }

    /**
     * Creates a new instance of APTPragmaNode
     */
    public APTPragmaNode(APTToken token) {
        super(token);
    }

    @Override
    public final int getType() {
        return APT.Type.PRAGMA;
    }

    @Override
    protected boolean validToken(APTToken t) {
        assert (t != null);
        int ttype = t.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers"; // NOI18N
        // eat all till END_PREPROC_DIRECTIVE
        return !APTUtils.isEndDirectiveToken(ttype);
    }

    @Override
    public boolean accept(APTFile curFile, APTToken token) {
        if (name == null) {
            name = token;
            return true;
        } else {
            return super.accept(curFile, token);
        }
    }

    @Override
    public APTToken getName() {
        return this.name;
    }

    @Override
    public TokenStream getTokenStream() {
        return super.getTokenStream();
    }
}