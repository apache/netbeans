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
public final class APTLiteLiteralToken extends APTTokenAbstact {
    private static final int COL_BITS  = 10;
    private static final int MAX_COL   = (1<<COL_BITS) - 1;
    private static final int LINE_BITS = 14;
    private static final int MAX_LINE  = (1<<LINE_BITS) - 1;
    private static final int TYPE_BITS = 8;
    private static final int MAX_TYPE  = (1<<TYPE_BITS) - 1;
    private final int offset;
    private final int columnLineType;
    
    static {
        // check that MX_TYPE is enough for all literals
        assert APTTokenTypes.LAST_LITERAL_TOKEN - APTTokenTypes.FIRST_LITERAL_TOKEN < MAX_TYPE
                : TYPE_BITS + " bits is not enough for " + (APTTokenTypes.LAST_LITERAL_TOKEN - APTTokenTypes.FIRST_LITERAL_TOKEN) + " literals"; //NOI18N
    }

    public static boolean isApplicable(int type, int offset, int column, int line, int literalType) {
        if (type != APTTokenTypes.IDENT || line > MAX_LINE || column > MAX_COL) {
            return false;
        }
        if (literalType > APTTokenTypes.FIRST_LITERAL_TOKEN && literalType < APTTokenTypes.LAST_LITERAL_TOKEN) {
            return true;
        }
        return false;
    }

    /**
     * Creates a new instance of APTConstTextToken
     */
    public APTLiteLiteralToken(int offset, int column, int line, int literalType) {
        this.offset = offset;
        assert APTConstTextToken.constText[literalType] != null : "no text for literal type " + literalType;
        int type = literalType - APTTokenTypes.FIRST_LITERAL_TOKEN;
        assert type > 0 && type < MAX_TYPE;
        columnLineType = ((((column & MAX_COL)<<LINE_BITS) 
                       + (line & MAX_LINE))<<TYPE_BITS) 
                       + (type & MAX_TYPE);
        assert column == getColumn() : column + " vs. " + getColumn();
        assert line == getLine() : line + " vs. " + getLine();
        assert literalType == getLiteralType() : literalType + " vs. " + getLiteralType();
    }
    
    @Override
    public String getText() {
        return APTConstTextToken.constText[getLiteralType()];
    }

    @Override
    public void setText(String t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTextID(CharSequence id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CharSequence getTextID() {
        return APTConstTextToken.constTextID[getLiteralType()];
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
        return APTTokenTypes.IDENT;
    }
    
    /**
     * @return token type as defined by the text
     */
    public int getLiteralType() {
        return (columnLineType & MAX_TYPE) + APTTokenTypes.FIRST_LITERAL_TOKEN;
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
