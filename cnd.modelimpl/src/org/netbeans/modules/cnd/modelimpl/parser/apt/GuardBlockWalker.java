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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTIfndef;
import org.netbeans.modules.cnd.apt.structure.APTPragma;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
public class GuardBlockWalker extends APTWalker {
    
    private APT guardCheck;
    private Boolean hasGuard = null;
            
            /** Creates a new instance of GuardBlockWalker */
    public GuardBlockWalker(APTFile apt) {
        super(apt, null);
    }

    public Token getGuard(){
        if (hasGuard == Boolean.TRUE && guardCheck != null){
            if (guardCheck instanceof APTIfndef) {
                return  ((APTIfndef)guardCheck).getMacroName();
            } else if (guardCheck instanceof APTPragma) {
                return  ((APTPragma)guardCheck).getName();
            }else {
                APTDefine fileOnce = APTUtils.createAPTDefine(getCurFile().getPath().toString());
                fileOnce.getName();
            }

        }
        return null;
    }
    
    @Override
    protected void onDefine(APT apt) {
        hasGuard = Boolean.FALSE;
    }
    
    @Override
    protected void onUndef(APT apt) {
        hasGuard = Boolean.FALSE;
    }
    
    @Override
    protected boolean onIf(APT apt) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    @Override
    protected boolean onIfdef(APT apt) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    @Override
    protected boolean onIfndef(APT apt) {
        guardCheck = (APTIfndef)apt;
        hasGuard = (hasGuard == null) ? Boolean.TRUE : Boolean.FALSE;
        return false;
    }
    
    @Override
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    @Override
    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        hasGuard = Boolean.FALSE;
        return false;
    }
    
    @Override
    protected void onEndif(APT apt, boolean wasInBranch) {
        hasGuard = (hasGuard == Boolean.TRUE) ? Boolean.TRUE : Boolean.FALSE;
    }
    
    @Override
    protected void onInclude(APT apt) {
        hasGuard = Boolean.FALSE;
    }
    
    @Override
    protected void onIncludeNext(APT apt) {
        hasGuard = Boolean.FALSE;
    }

    @Override
    protected void onPragmaNode(APT apt) {
        APTPragma pragma = (APTPragma) apt;
        APTToken name = pragma.getName();
        if (name != null && APTPragma.PRAGMA_ONCE.contentEquals(name.getTextID())) {
            hasGuard = Boolean.TRUE;
            guardCheck = apt;
            super.stop();
        }
    }

    @Override
    protected void onOtherNode(APT apt) {
        hasGuard = Boolean.FALSE;
    }

    public void clearGuard() {
        hasGuard = Boolean.FALSE;
    }
    
}
