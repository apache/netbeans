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
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.openide.util.CharSequences;

/**
 * Fake AST managing type
 */
public class FakeAST extends BaseAST implements Serializable {
    private static final long serialVersionUID = -1975495157952844447L;
    
    private final static CharSequence[] tokenText = new CharSequence[CPPTokenTypes.CSM_END + 1];

    static {
        int flags = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        for (Field field : CPPTokenTypes.class.getDeclaredFields()) {
            if ((field.getModifiers() & flags) == flags &&
                    int.class.isAssignableFrom(field.getType())) {
                try {
                    int value = field.getInt(null);
                    String name = field.getName();
                    tokenText[value]=CharSequences.create(name);
                } catch (Exception ex) {
                    DiagnosticExceptoins.register(ex);
                }
            }
        }
    }
    
    int ttype = Token.INVALID_TYPE;
    
    String text = null;
    
    public FakeAST() {
    }
    
    /** Get the token type for this node */
    @Override
    public int getType() {
        return ttype;
    }
    
    @Override
    public void initialize(int t, String txt) {
        setType(t);
        setText(txt);
    }
    
    @Override
    public void initialize(AST t) {
        setText(t.getText());
        setType(t.getType());
    }
    
    @Override
    public void initialize(Token tok) {
        setText(tok.getText());
        setType(tok.getType());
    }
    
    /** Set the token type for this node */
    @Override
    public void setType(int ttype_) {
        ttype = ttype_;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text != null ? text : tokenText[getType()].toString();
    }

    public CharSequence getTextID() {
        return text != null ? text : tokenText[getType()].toString();
    }
}
