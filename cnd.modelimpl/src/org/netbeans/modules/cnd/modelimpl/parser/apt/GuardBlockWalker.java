/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
