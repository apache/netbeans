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

package org.netbeans.modules.cnd.apt.support;

import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.openide.util.CharSequences;

/**
 * token to be used in APT infrastructure
 */
public class APTBaseToken implements APTToken {
    private static final long serialVersionUID = 2834353662691067170L;
    // most tokens will want line and text information
    protected int line;
    protected CharSequence text = null;
    protected short col;
    protected short type;
    private int offset;
    /**
     * Creates a new instance of APTBaseToken
     */
    public APTBaseToken() {
    }

    protected APTBaseToken(APTToken token) {
        this(token, token.getType());
    }

    public APTBaseToken(APTToken token, int ttype) {
        this.setColumn(token.getColumn());
        this.setFilename(token.getFilename());
        this.setLine(token.getLine());

        // This constructor is used with the existing tokens so do not use setText here,
        // because we do not need to go through APTStringManager once again
        text = token.getTextID();
        assert CharSequences.isCompact(text);

        this.setType(ttype);
        this.setOffset(token.getOffset());
        this.setEndOffset(token.getEndOffset());
        this.setEndColumn(token.getEndColumn());
        this.setEndLine(token.getEndLine());
        this.setTextID(token.getTextID());
    }

    public APTBaseToken(String text) {
        this.setText(text);
    }
    
    @Override
    public final int getType() {
        return type;
    }

    @Override
    public final void setType(int t) {
        assert t <= Short.MAX_VALUE;
        type = (short) t;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public void setFilename(String name) {
    }
    
    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final void setOffset(int o) {
        this.offset = o;
    }

    @Override
    public int getEndOffset() {
        return getOffset() + (getTextID() != null ? getTextID().length() : 0);
    }

    @Override
    public void setEndOffset(int end) {
        // do nothing
    }

    @Override
    public final CharSequence getTextID() {
        return this.text;
    }

    @Override
    public final void setTextID(CharSequence textID) {
        this.text = TextCache.getManager().getString(textID);
    }

    @Override
    public final String getText() {
        if(this.text != null) {
            return text.toString();
        } else {
            return "";
        }
    }

    @Override
    public final void setText(String t) {
        text = TextCache.getManager().getString(t);
    }

    @Override
    public final int getLine() {
        return line;
    }

    @Override
    public final void setLine(int l) {
        line = l;
    }

    /** Return token's start column */
    @Override
    public final int getColumn() {
        return col;
    }

    @Override
    public final void setColumn(int c) {
        if (c > Short.MAX_VALUE) {
            // Column line used for messages, so set in max value in case too long line.
            c = Short.MAX_VALUE;
        } else {
            col = (short) c;
        }
    }

    @Override
    public String toString() {
        return "[\"" + getTextID() + "\",<" + APTUtils.getAPTTokenName(getType()) + ">,line=" + getLine() + ",col=" + getColumn() + "]" + ",offset="+getOffset()+",file="+getFilename(); // NOI18N
    }

    @Override
    public int getEndColumn() {
        return getColumn() + (getTextID() != null ? getTextID().length() : 0);
    }

    @Override
    public void setEndColumn(int c) {
        // do nothing
    }

    @Override
    public int getEndLine() {
        return getLine();
    }

    @Override
    public void setEndLine(int l) {
        // do nothing
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final APTBaseToken other = (APTBaseToken) obj;
        if (this.getType() != other.getType()) {
            return false;
        }
        if (this.getOffset() != other.getOffset()) {
            return false;
        }
        if (this.text == null) {
            return other.text == null;
        }
        if (!this.text.equals(other.text)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.getType();
        hash = 23 * hash + this.offset;
        hash = 23 * hash + (this.text != null ? this.text.hashCode() : 0);
        return hash;
    }
    
    @Override
    public Object getProperty(Object key) {
        return null;
    }    
}
