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

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.Serializable;
import org.netbeans.modules.cnd.utils.cache.TextCache;

/**
 * Fake AST managing text
 */
public final class NamedFakeAST extends FakeAST implements Serializable {
    private static final long serialVersionUID = 3949611279758335361L;
    
    private CharSequence text;
    
    public NamedFakeAST() {
    }

    /** Set the token text for this node */
    @Override
    public void setText(String text_) {
        text = TextCache.getManager().getString(text_);
    } 

    /** Get the token text for this node */
    @Override
    public String getText() {
        return text.toString();
    }

    @Override
    public CharSequence getTextID() {
        return text;
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
}
