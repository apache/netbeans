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
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 *
 */
public final class APTLiteConstTextToken  extends APTTokenAbstact implements APTTokenTypes {

    private static final int COL_BITS  = 10;
    private static final int MAX_COL   = (1<<COL_BITS) - 1;
    private static final int LINE_BITS = 16;
    private static final int MAX_LINE  = (1<<LINE_BITS) - 1;
    private static final int TYPE_BITS = 6;
    private static final int MAX_TYPE  = (1<<TYPE_BITS) - 1;

    private final int offset;
    private final int columnLineType;

    public static String toText(int type) {
        assert isLiteConstTextType(type);
        return APTConstTextToken.constText[type];
    }

    public static CharSequence toTextID(int type) {
        assert isLiteConstTextType(type);
        return APTConstTextToken.constTextID[type];
    }

    public static boolean isLiteConstTextType(int type) {
        return type > APTTokenTypes.NULL_TREE_LOOKAHEAD && type < APTTokenTypes.LAST_CONST_TEXT_TOKEN;
    }
    public static boolean isApplicable(int type, int offset, int column, int line) {
        if (isLiteConstTextType(type) && type <= APTLiteConstTextToken.MAX_TYPE) {
            if (line <= APTLiteConstTextToken.MAX_LINE && column <= APTLiteConstTextToken.MAX_COL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new instance of APTConstTextToken
     */
    public APTLiteConstTextToken(int type, int offset, int column, int line) {
        this.offset = offset;
        columnLineType = ((((column & MAX_COL)<<LINE_BITS) + (line & MAX_LINE))<<TYPE_BITS) + (type & MAX_TYPE);
        assert type == getType();
        assert column == getColumn();
        assert line == getLine();
    }

    @Override
    public String getText() {
        return APTConstTextToken.constText[columnLineType & MAX_TYPE];
    }

    @Override
    public void setText(String t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CharSequence getTextID() {
        return APTConstTextToken.constTextID[columnLineType & MAX_TYPE];
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getType() {
        return columnLineType & MAX_TYPE;
    }

    @Override
    public void setType(int t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumn() {
        return (columnLineType>>(LINE_BITS+TYPE_BITS)) & MAX_COL;
    }

    @Override
    public void setColumn(int c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLine() {
        return (columnLineType>>TYPE_BITS) & MAX_LINE;
    }

    @Override
    public void setLine(int l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getEndOffset() {
        return getOffset() + getTextID().length();
    }

    @Override
    public int getEndLine() {
        return getLine();
    }

    @Override
    public int getEndColumn() {
        return getColumn() + getTextID().length();
    }
}
