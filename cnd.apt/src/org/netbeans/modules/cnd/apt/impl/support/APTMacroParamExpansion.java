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

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * Wrapper for tokens expanded from parameters of macros
 *
 */
public class APTMacroParamExpansion implements APTToken {

    private static final int NOT_INITED_TYPE = -5;
    private final APTToken param;
    private final APTToken original;
    private int type = NOT_INITED_TYPE;
    private CharSequence id;

    public APTToken getOriginal() {
        return original;
    }

    public APTMacroParamExpansion(APTToken token, APTToken param) {
        this.param = param;
        this.original = token;
    }

    @Override
    public int getOffset() {
        return original.getOffset();
    }

    @Override
    public void setOffset(int o) {
        original.setOffset(o);
    }

    @Override
    public int getEndOffset() {
        return original.getEndOffset();
    }

    @Override
    public void setEndOffset(int o) {
        original.setEndOffset(o);
    }

    @Override
    public int getEndColumn() {
        return original.getEndColumn();
    }

    @Override
    public void setEndColumn(int c) {
        original.setEndColumn(c);
    }

    @Override
    public int getEndLine() {
        return original.getEndLine();
    }

    @Override
    public void setEndLine(int l) {
        original.setEndLine(l);
    }

    @Override
    public String getText() {
        return original.getText();
    }

    @Override
    public CharSequence getTextID() {
        if (id == null) {
            id = original.getTextID();
        }
        return id;
    }

    @Override
    public void setTextID(CharSequence newId) {
        id = null;
        original.setTextID(newId);
    }

    @Override
    public int getColumn() {
        return original.getColumn();
    }

    @Override
    public void setColumn(int c) {
        original.setColumn(c);
    }

    @Override
    public int getLine() {
        return original.getLine();
    }

    @Override
    public void setLine(int l) {
        original.setLine(l);
    }

    @Override
    public String getFilename() {
        return original.getFilename();
    }

    @Override
    public void setFilename(String name) {
        original.setFilename(name);
    }

    @Override
    public void setText(String t) {
        id = null;
        original.setText(t);
    }

    @Override
    public int getType() {
        if (type == NOT_INITED_TYPE) {
            type = original.getType();
        }
        return type;
    }

    @Override
    public void setType(int t) {
        type = NOT_INITED_TYPE;
        original.setType(t);
    }

    @Override
    public String toString() {
        return param+ "->" + original; //NOI18N
    }

    @Override
    public Object getProperty(Object key) {
        return null;
    }
}
