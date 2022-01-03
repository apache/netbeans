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

import org.netbeans.modules.cnd.antlr.TokenStream;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.TokenBasedTokenStream;
import org.openide.util.CharSequences;

/**
 *
 */
public final class APTPredefinedMacroMap implements APTMacroMap {
       
    private static CharSequence []preMacro = new CharSequence [] {
         CharSequences.create("__FILE__"),  // NOI18N
         CharSequences.create("__LINE__"),  // NOI18N
         CharSequences.create("__DATE__"),  // NOI18N
         CharSequences.create("__TIME__"),  // NOI18N
         CharSequences.create("__FUNCTION__"),  // NOI18N
         CharSequences.create("_Pragma"),  // NOI18N
         CharSequences.create("__pragma")  // NOI18N
    };

    /*package*/ APTPredefinedMacroMap() {
    }

    @Override
    public APTMacroMap.State getState() {
        return null;
    }

    @Override
    public boolean isDefined(APTToken token) {
        return isDefined(token.getTextID());
    }
    
    @Override
    public boolean isDefined(CharSequence token) {
        int i;
        
        if (token.length() < 2 || token.charAt(0) != '_' || (token.charAt(1) != '_' && token.charAt(1) != 'P')) {
            return false;
        }
        CharSequence tokenText = CharSequences.create(token);
                    
        for (i = 0; i < preMacro.length; i++) {
            if(preMacro[i].equals(tokenText)) {
                return true;
            }                
        }        
        return false;
    }

    @Override
    public APTMacro getMacro(APTToken token) {
        if (isDefined(token.getTextID())) {
            return new APTPredefinedMacroImpl(token);
        }
        return null;
    }
    

    @Override
    public void setState(APTMacroMap.State state) {
        APTUtils.LOG.log(Level.SEVERE, "setState is not supported", new IllegalAccessException()); // NOI18N
    }

    @Override
    public void define(APTFile file, APTDefine define, Kind macroType) {
        APTUtils.LOG.log(Level.SEVERE, "define is not supported", new IllegalAccessException()); // NOI18N
    }

    @Override
    public void undef(APTFile file, APTToken name) {
        APTUtils.LOG.log(Level.SEVERE, "undef is not supported", new IllegalAccessException()); // NOI18N
    }

    @Override
    public boolean pushPPDefined() {
        APTUtils.LOG.log(Level.SEVERE, "pushPPDefined is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    @Override
    public boolean popPPDefined() {
        APTUtils.LOG.log(Level.SEVERE, "popPPDefined is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }
   
    @Override
    public boolean pushExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "pushExpanding is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    @Override
    public void popExpanding() {
        APTUtils.LOG.log(Level.SEVERE, "popExpanding is not supported", new IllegalAccessException()); // NOI18N
    }

    @Override
    public boolean isExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "isExpanding is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }     
    
    private static final class APTPredefinedMacroImpl implements APTMacro {
        private APTToken macro;
        
        public APTPredefinedMacroImpl(APTToken macro) {
            this.macro =  macro;           
        }

        @Override
        public CharSequence getFile() {
            return CharSequences.empty();
        }

        @Override
        public Kind getKind() {
            return Kind.POSITION_PREDEFINED;
        }

        @Override
        public boolean isFunctionLike() {
            if ("_Pragma".contentEquals(macro.getTextID()) || // NOI18N
                    "__pragma".contentEquals(macro.getTextID())) { // NOI18N
                return true;
            }
            return false;
        }

        @Override
        public APTToken getName() {
            return macro;
        }

        @Override
        public Collection<APTToken> getParams() {
            return Collections.<APTToken>emptyList();
        }

        @Override
        public APTDefine getDefineNode() {
            throw new UnsupportedOperationException("Not supported operation."); // NOI18N
        }

        @Override
        public TokenStream getBody() {
            APTToken tok = APTUtils.createAPTToken(macro, APTTokenTypes.STRING_LITERAL);
            
            if ("__LINE__".contentEquals(macro.getTextID())) { // NOI18N
                tok.setType(APTTokenTypes.DECIMALINT);
                tok.setText("" + macro.getLine()); // NOI18N
            } else if("_Pragma".contentEquals(macro.getTextID()) || // NOI18N
                    "__pragma".contentEquals(macro.getTextID())) { // NOI18N
                tok.setType(APTTokenTypes.COMMENT);
                tok.setText(""); // NOI18N
            } else {
                tok.setType(APTTokenTypes.STRING_LITERAL);
            }
                        
            return new TokenBasedTokenStream(tok);
        }    
        
        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("<P>"); // NOI18N     
            retValue.append(getName());
            return retValue.toString();
        }
    }
    
}
