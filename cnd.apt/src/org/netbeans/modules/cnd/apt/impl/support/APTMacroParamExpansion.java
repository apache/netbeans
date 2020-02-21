/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * Wrapper for tokens expanded from parameters of macros
 *
 */
public class APTMacroParamExpansion implements APTToken {

    private static final int NOT_INITED_TYPE = -5;
    private final APTToken param;
    private final APTToken original;
    private int type = NOT_INITED_TYPE;
    private CharSequence id;

    public APTToken getOriginal() {
        return original;
    }

    public APTMacroParamExpansion(APTToken token, APTToken param) {
        this.param = param;
        this.original = token;
    }

    @Override
    public int getOffset() {
        return original.getOffset();
    }

    @Override
    public void setOffset(int o) {
        original.setOffset(o);
    }

    @Override
    public int getEndOffset() {
        return original.getEndOffset();
    }

    @Override
    public void setEndOffset(int o) {
        original.setEndOffset(o);
    }

    @Override
    public int getEndColumn() {
        return original.getEndColumn();
    }

    @Override
    public void setEndColumn(int c) {
        original.setEndColumn(c);
    }

    @Override
    public int getEndLine() {
        return original.getEndLine();
    }

    @Override
    public void setEndLine(int l) {
        original.setEndLine(l);
    }

    @Override
    public String getText() {
        return original.getText();
    }

    @Override
    public CharSequence getTextID() {
        if (id == null) {
            id = original.getTextID();
        }
        return id;
    }

    @Override
    public void setTextID(CharSequence newId) {
        id = null;
        original.setTextID(newId);
    }

    @Override
    public int getColumn() {
        return original.getColumn();
    }

    @Override
    public void setColumn(int c) {
        original.setColumn(c);
    }

    @Override
    public int getLine() {
        return original.getLine();
    }

    @Override
    public void setLine(int l) {
        original.setLine(l);
    }

    @Override
    public String getFilename() {
        return original.getFilename();
    }

    @Override
    public void setFilename(String name) {
        original.setFilename(name);
    }

    @Override
    public void setText(String t) {
        id = null;
        original.setText(t);
    }

    @Override
    public int getType() {
        if (type == NOT_INITED_TYPE) {
            type = original.getType();
        }
        return type;
    }

    @Override
    public void setType(int t) {
        type = NOT_INITED_TYPE;
        original.setType(t);
    }

    @Override
    public String toString() {
        return param+ "->" + original; //NOI18N
    }

    @Override
    public Object getProperty(Object key) {
        return null;
    }
}
