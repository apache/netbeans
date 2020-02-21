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
