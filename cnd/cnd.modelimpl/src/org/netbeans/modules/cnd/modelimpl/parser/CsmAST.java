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

package org.netbeans.modules.cnd.modelimpl.parser;

import org.netbeans.modules.cnd.antlr.BaseAST;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 */
public class CsmAST extends BaseAST implements TokenBasedAST, OffsetableAST, Serializable {

    private static final long serialVersionUID = -1975495157952833337L;
    private static final Token NIL;
    static {
        NIL = new APTBaseToken();
        NIL.setText("");
    }
    
    transient protected Token token = NIL;

    /** Creates a new instance of CsmAST */
    public CsmAST() {
    }

//    public CsmAST(Token tok) {
//        initialize(tok);
//    }


    @Override
    public void initialize(int t, String txt) {
        token = new APTBaseToken();
        token.setType(t);
        token.setText(txt);
    }

    @Override
    public void initialize(AST t) {

        if (t instanceof CsmAST) {
            token = ((CsmAST)t).token;
        } else {
            assert false;
//            token = new CsmToken();
//            token.setType(t.getType());
//            token.setText(t.getText());
        }
    }

    @Override
    public void initialize(Token tok) {
        token = tok;
    }

    /** Get the token text for this node */
    @Override
    public String getText() {
        return token.getText();
    }

    public CharSequence getTextID() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getTextID();
        }
        return token.getText();
    }

    /** Get the token type for this node */
    @Override
    public int getType() {
        return token.getType();
    }

    @Override
    public int getLine() {
        return token.getLine();
    }
    
    @Override
    public int getColumn() {
        return token.getColumn();
    }

    @Override
    public int getOffset() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getOffset();
        } else {
            return 0;
        }
    }

    @Override
    public int getEndOffset() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getEndOffset();
        } else {
            return 0;
        }        
    }
    
    public int getEndLine() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getEndLine();
        } else {
            return 0;
        }          
    }
    
    public int getEndColumn() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getEndColumn();
        } else {
            return 0;
        }          
    }
    
    public String getFilename() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getFilename();
        } else {
            return "<undef_file>"; // NOI18N
        }
    }
    
    @Override
    public String toString() {
        return token.toString();
    }

    public Token getToken() {
        return token;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(token);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        token = (Token) in.readObject();
    }    
}
