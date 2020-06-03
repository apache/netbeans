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

import java.lang.reflect.Field;
import java.util.Map;
import org.netbeans.modules.cnd.antlr.ANTLRHashString;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTLexer;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;

/**
 *
 */
public final class APTConstTextToken extends APTTokenAbstact implements APTTokenTypes {
    private static final int MAX_TEXT_ID = APTTokenTypes.LAST_LEXER_FAKE_RULE;
    final static String[] constText = new String[MAX_TEXT_ID];
    final static CharSequence[] constTextID = new CharSequence[MAX_TEXT_ID];

    private int type = INVALID_TYPE;
    private int column;
    private int offset;
    private int line;
    /**
     * Creates a new instance of APTConstTextToken
     */
    public APTConstTextToken() {
    }

    static {
        //setup const text values
        constText[EOF]                  =""; // NOI18N
        constText[END_PREPROC_DIRECTIVE]=""; // NOI18N

        // 1 symbol:
        constText[GRAVE_ACCENT]          ="`"; // NOI18N
        constText[FUN_LIKE_MACRO_LPAREN]="("; // NOI18N
        constText[ASSIGNEQUAL]          ="="; // NOI18N
        constText[DIVIDE]               ="/"; // NOI18N
        constText[STAR]                 ="*"; // NOI18N
        constText[MOD]                  ="%"; // NOI18N
        constText[NOT]                  ="!"; // NOI18N
        constText[AMPERSAND]            ="&"; // NOI18N
        constText[BITWISEOR]            ="|"; // NOI18N
        constText[BITWISEXOR]           ="^"; // NOI18N
        constText[COLON]                =":"; // NOI18N
        constText[LESSTHAN]             ="<"; // NOI18N
        constText[GREATERTHAN]          =">"; // NOI18N
        constText[MINUS]                ="-"; // NOI18N
        constText[PLUS]                 ="+"; // NOI18N
        constText[SHARP]                ="#"; // NOI18N
        constText[SEMICOLON]            =";"; // NOI18N
        constText[RPAREN]               =")"; // NOI18N
        constText[DOLLAR]               ="$"; // NOI18N
        constText[RCURLY]               ="}"; // NOI18N
        constText[AT]                   ="@"; // NOI18N
        constText[LPAREN]               ="("; // NOI18N
        constText[QUESTIONMARK]         ="?"; // NOI18N
        constText[LCURLY]               ="{"; // NOI18N
        constText[COMMA]                =","; // NOI18N
        constText[LSQUARE]              ="["; // NOI18N
        constText[RSQUARE]              ="]"; // NOI18N
        constText[TILDE]                ="~"; // NOI18N
        constText[DOT]                  ="."; // NOI18N
        constText[BACK_SLASH]           ="\\"; // NOI18N

        // 2 symbol:
        constText[BITWISEANDEQUAL]      ="&="; // NOI18N
        constText[AND]                  ="&&"; // NOI18N
        constText[NOTEQUAL]             ="!="; // NOI18N
        constText[MODEQUAL]             ="%="; // NOI18N
        constText[TIMESEQUAL]           ="*="; // NOI18N
        constText[DIVIDEEQUAL]          ="/="; // NOI18N
        constText[EQUAL]                ="=="; // NOI18N
        constText[BITWISEOREQUAL]       ="|="; // NOI18N
        constText[OR]                   ="||"; // NOI18N
        constText[BITWISEXOREQUAL]      ="^="; // NOI18N
        constText[SCOPE]                ="::"; // NOI18N
        constText[LESSTHANOREQUALTO]    ="<="; // NOI18N
        constText[SHIFTLEFT]            ="<<"; // NOI18N
        constText[GREATERTHANOREQUALTO] =">="; // NOI18N
        constText[SHIFTRIGHT]           =">>"; // NOI18N
        constText[MINUSEQUAL]           ="-="; // NOI18N
        constText[MINUSMINUS]           ="--"; // NOI18N
        constText[POINTERTO]            ="->"; // NOI18N
        constText[PLUSEQUAL]            ="+="; // NOI18N
        constText[PLUSPLUS]             ="++"; // NOI18N
        constText[DBL_SHARP]            ="##"; // NOI18N
        constText[DOTMBR]               =".*"; // NOI18N

        // 3 symbol:
        constText[SHIFTLEFTEQUAL]       ="<<="; // NOI18N
        constText[SHIFTRIGHTEQUAL]      =">>="; // NOI18N
        constText[POINTERTOMBR]         ="->*"; // NOI18N
        constText[ELLIPSIS]             ="..."; // NOI18N

        // more
        constText[DEFINED]              ="defined"; // NOI18N

        // add literals
        try {
            Field literalsField = APTLexer.class.getDeclaredField("LITERALS_TABLE"); //NOI18N
            literalsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<ANTLRHashString, Integer> map = (Map<ANTLRHashString, Integer>)literalsField.get(null);
            for (Map.Entry<ANTLRHashString, Integer> entry : map.entrySet()) {
                int idx = entry.getValue();
                String current = constText[idx];
                assert current == null;
                // get string value
                Field stringField = entry.getKey().getClass().getDeclaredField("s"); //NOI18N
                stringField.setAccessible(true);
                constText[idx] = (String)stringField.get(entry.getKey()); //NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        for (int i = 0; i < constText.length; i++) {
            String str = constText[i];
            constTextID[i] = CharSequences.create(str);
            if (str != null) {
                if (i > MAX_TEXT_ID) {
                    System.err.printf("APTConstTextToken: token %s [%d] is higher than MAX_TEXT_ID [%d]%n", str, i, MAX_TEXT_ID);
                }
            } else {
               // System.err.printf("APTConstTextToken: index [%d] does not have text \n", i);
            }
        }
//        assert TYPE_MASK >= LAST_CONST_TEXT_TOKEN;
//        System.err.printf("APTConstTextToken: %d\n", LAST_CONST_TEXT_TOKEN);
    }

//    private static void addConstText(String text, int id) {
//        assert constText[id] == null || constText[id].equals(text) : "Trying to redefine value " + text + " for already defined token type " + id + ", current value is " + constText[id];
//        constText[id] = text;
//    }

    @Override
    public String getText() {
        //assert(constText[getType()] != null) : "Not initialized ConstText for type " + getType(); // NOI18N
        return constText[getType()];
    }

    public static CharSequence getConstTextID(int type) {
        return constTextID[type];
    }
    
    @Override
    public void setText(String t) {
        //assert(true) : "setText should not be called for ConstText token"; // NOI18N
        /*String existingText = getText();
        if (existingText != null) {
            /*if (!existingText.equals(t)) {
                System.out.println(getType() + ", Old=" + existingText + ", New=" + t); // NOI18N
            }*/
            //assert(existingText.equals(t));
        /*} else {
            constText[getType()] = t;
        }*/
    }

    @Override
    public CharSequence getTextID() {
        return constTextID[getType()];
    }

    @Override
    public int getEndOffset() {
        return getOffset() + getTextID().length();
        //return endOffset;
    }

    @Override
    public int getEndLine() {
        return getLine();
    }

    @Override
    public int getEndColumn() {
        return getColumn() + getTextID().length();
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public void setLine(int l) {
        line = l;
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
        return type;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public void setColumn(int c) {
        column = c;
    }
}
