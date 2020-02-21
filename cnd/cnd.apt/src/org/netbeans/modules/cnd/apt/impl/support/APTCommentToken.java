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

import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
public final class APTCommentToken extends APTTokenAbstact {
    /** Creates a new instance of APTCommentToken */
    public APTCommentToken() {
    }
    
    private int type = INVALID_TYPE; // we have two kinds of comments :(
    private int offset = 0;
    private int length;
    private int line;
    private int column;
    private int endLine;

    @Override
    public String getText() {
        return "<comment text skipped>"; // NOI18N
    }
    
    @Override
    public int getEndOffset() {
        return offset + length;
    }

    @Override
    public void setOffset(int o) {
        offset = o;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public int getType() {
        assert APTUtils.isCommentToken(type) : "forgot to set comment kind type?";
        return type;
    }

    @Override
    public void setColumn(int c) {
        column = c;
    }

    @Override
    public void setLine(int l) {
        line = l;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public void setEndLine(int l) {
        this.endLine = l;
    }
    
    @Override
    public int getEndLine() {
        return endLine;
    }
    
    @Override
    public int getEndColumn() {
        return getColumn() + length;
    }

    @Override
    public void setText(String t) {
        length = t.length();
    }

    public void setTextLength(int len) {
        length = len;
    }
}
