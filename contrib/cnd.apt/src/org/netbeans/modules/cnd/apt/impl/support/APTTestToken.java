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

import org.netbeans.modules.cnd.antlr.TokenImpl;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * lightweigth Token implementation (to reduce memory used by APT)
 */
public final class APTTestToken extends TokenImpl implements APTToken {

    private int offset;


    
//    private int textID;

    public APTTestToken() {

    }

    public APTTestToken(APTToken token) {
        this(token, token.getType());
    }
    
    public APTTestToken(APTToken token, int ttype) {
        this.setColumn(token.getColumn());
        this.setFilename(token.getFilename());
        this.setLine(token.getLine());
        this.setText(token.getText());
        this.setType(ttype);
        this.setOffset(token.getOffset());
        this.setEndOffset(token.getEndOffset());
        this.setTextID(token.getTextID());
    }
    
    @Override
    public int getOffset() {
        return offset;
    }
      
    @Override
    public void setOffset(int o) {
        offset = o;
    }
    
    @Override
    public int getEndOffset() {
        return getOffset() + getText().length();
    }

    @Override
    public void setEndOffset(int end) {
        // do nothing
    }
    
    @Override
    public CharSequence getTextID() {
        CharSequence res = getText();
        return res;
    }
    
    @Override
    public void setTextID(CharSequence textID) {
        setText(textID == null ? null : textID.toString());
    }
  
    @Override
    public String getText() {
        // TODO: use shared string map
        String res = super.getText();
        return res;
    }
     
    @Override
    public String toString() {
        return "[\"" + getText() + "\",<" + APTUtils.getAPTTokenName(getType()) + ">,line=" + getLine() + ",col=" + getColumn() + "]"+",offset="+getOffset();//+",file="+getFilename(); // NOI18N
    }

    @Override
    public int getEndColumn() {
        return getColumn() + getText().length();
    }

    @Override
    public void setEndColumn(int c) {
        // do nothing
    }

    @Override
    public int getEndLine() {
        return getLine();
    }

    @Override
    public void setEndLine(int l) {
        // do nothin
    }
    
    @Override
    public Object getProperty(Object key) {
        return null;
    }
}
