/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
