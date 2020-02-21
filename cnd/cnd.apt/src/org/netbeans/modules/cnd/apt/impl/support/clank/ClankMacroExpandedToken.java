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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import org.netbeans.modules.cnd.apt.impl.support.MacroExpandedToken;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 */
public class ClankMacroExpandedToken implements APTToken {

    private final APTToken to;
    private final APTToken endOffsetToken;
    private final int macroIndex;

    public ClankMacroExpandedToken(APTToken to, APTToken endOffsetToken, int macroIndex) {
        assert !(endOffsetToken instanceof ClankMacroExpandedToken || endOffsetToken instanceof MacroExpandedToken);
        this.to = to;
        this.endOffsetToken = endOffsetToken;
        this.macroIndex = macroIndex;
    }

    @Override
    public int getOffset() {
        return to.getOffset();
    }

    @Override
    public void setOffset(int o) {
        throw new UnsupportedOperationException("setOffset must not be used"); // NOI18N
    }

    @Override
    public int getColumn() {
        return to.getColumn();
    }

    @Override
    public void setColumn(int c) {
        throw new UnsupportedOperationException("setColumn must not be used"); // NOI18N
    }

    @Override
    public int getLine() {
        return to.getLine();
    }

    @Override
    public void setLine(int l) {
        throw new UnsupportedOperationException("setLine must not be used"); // NOI18N
    }

    @Override
    public String getFilename() {
        return to.getFilename();
    }

    @Override
    public void setFilename(String name) {
        throw new UnsupportedOperationException("setFilename must not be used"); // NOI18N
    }

    @Override
    public String getText() {
        return to.getText();
    }

    @Override
    public void setText(String t) {
        throw new UnsupportedOperationException("setText must not be used"); // NOI18N
    }

    @Override
    public CharSequence getTextID() {
        return to.getTextID();
    }

    @Override
    public void setTextID(CharSequence id) {
        throw new UnsupportedOperationException("setTextID must not be used"); // NOI18N
    }

    @Override
    public int getType() {
        return to.getType();
    }

    @Override
    public void setType(int t) {
        throw new UnsupportedOperationException("setType must not be used"); // NOI18N
    }

    @Override
    public int getEndOffset() {
        return endOffsetToken.getEndOffset();
    }

    @Override
    public void setEndOffset(int o) {
        throw new UnsupportedOperationException("setEndOffset must not be used"); // NOI18N
    }

    @Override
    public int getEndColumn() {
        return endOffsetToken.getEndColumn();
    }

    @Override
    public void setEndColumn(int c) {
        throw new UnsupportedOperationException("setEndColumn must not be used"); // NOI18N
    }

    @Override
    public int getEndLine() {
        return endOffsetToken.getEndLine();
    }

    @Override
    public void setEndLine(int l) {
        throw new UnsupportedOperationException("setEndLine must not be used"); // NOI18N
    }

    public APTToken getTo() {
        return to;
    }

    public int getMacroIndex() {
        return macroIndex;
    }

    @Override
    public String toString() {
        String retValue;
        retValue = super.toString();
        retValue += "\n\tEXPANDING OF {" + to + "}\n\tTO {" + to + "}"; // NOI18N
        return retValue;
    }

    @Override
    public Object getProperty(Object key) {
        return null;
    }    
}
