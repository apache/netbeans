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

import java.io.ObjectStreamException;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * token as wrapper to present macro expansion
 * on deserialization is substituted by presenter
 */
public class MacroExpandedToken implements APTToken, Serializable {

    private static final long serialVersionUID = -5975409234096997015L;
    private static final int NOT_INITED_OFFSET = -5;
    transient private final APTToken from;
    transient private final APTToken to;
    transient private final APTToken endOffsetToken;
    transient private int offset = NOT_INITED_OFFSET;
    private static final boolean OPTIMIZE_MACRO = CndUtils.getBoolean("apt.optimize.macro", false); // NOI18N

    /** constructor for serialization **/
    protected MacroExpandedToken() {
        from = null;
        to = null;
        endOffsetToken = null;
    }

    public MacroExpandedToken(APTToken from, APTToken to, APTToken endOffsetToken) {
        while (from instanceof MacroExpandedToken) {
            from = ((MacroExpandedToken) from).from;
        }
        if (from instanceof APTMacroParamExpansion) {
            CndUtils.assertTrueInConsole(false, "more optimization for:" + from, Thread.currentThread().getName());
        }
        if (from == null) {
            throw new IllegalArgumentException("why 'from' is not APTToken?"); // NOI18N
        }
        this.from = from;
        if (OPTIMIZE_MACRO) {
            while (true) {
                if (to instanceof APTMacroParamExpansion) {
                    to = ((APTMacroParamExpansion)to).getOriginal();
                } else if (to instanceof MacroExpandedToken) {
                    to = ((MacroExpandedToken)to).to;
                } else {
                    break;
                }
            }
        }
        if (to == null) {
            throw new IllegalArgumentException("why 'to' is not APTToken?"); // NOI18N
        }
        this.to = to;
        while (endOffsetToken instanceof MacroExpandedToken) {
            endOffsetToken = ((MacroExpandedToken) endOffsetToken).endOffsetToken;
        }
        if (endOffsetToken == null) {
            throw new IllegalArgumentException("why 'endOffsetToken' is not APTToken?"); // NOI18N
        }
        this.endOffsetToken = endOffsetToken;
    }

    ////////////////////////////////////////////////////////
    // delegate to original token (before expansion)

    @Override
    public int getOffset() {
        if (offset == NOT_INITED_OFFSET) {
            offset = from.getOffset();
        }
        return offset;
    }

    @Override
    public void setOffset(int o) {
        throw new UnsupportedOperationException("setOffset must not be used"); // NOI18N
    }

    @Override
    public int getColumn() {
        return from.getColumn();
    }

    @Override
    public void setColumn(int c) {
        throw new UnsupportedOperationException("setColumn must not be used"); // NOI18N
    }

    @Override
    public int getLine() {
        return from.getLine();
    }

    @Override
    public void setLine(int l) {
        throw new UnsupportedOperationException("setLine must not be used"); // NOI18N
    }

    @Override
    public String getFilename() {
        return from.getFilename();
    }

    @Override
    public void setFilename(String name) {
        throw new UnsupportedOperationException("setFilename must not be used"); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // delegate to expanded result

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

    @Override
    public String toString() {
        String retValue;

        retValue = super.toString();
        retValue += "\n\tEXPANDING OF {" + from + "}\n\tTO {" + to + "}"; // NOI18N
        return retValue;
    }

    @Override
    public Object getProperty(Object key) {
        return null;
    }    
    //////////////////////////////////////////////////////////////////////////////
    // serialization support

    protected Object writeReplace() throws ObjectStreamException {
        Object replacement = new SerializedMacroToken(this);
        return replacement;
    }

    // replacement class to prevent serialization of
    // "from", "to", "endOffset" tokens
    private static final class SerializedMacroToken extends APTBaseToken
                                                    implements APTToken, Serializable {
        private static final long serialVersionUID = -3616605756675245730L;
        private int endOffset;
        private int endLine;
        private int endColumn;

        public SerializedMacroToken(MacroExpandedToken orig) {
            super(orig);
        }

        @Override
        public void setEndOffset(int end) {
            endOffset = end;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public void setEndLine(int l) {
            this.endLine = l;
        }

        @Override
        public void setEndColumn(int c) {
            this.endColumn = c;
        }

        @Override
        public int getEndLine() {
            return endLine;
        }

        @Override
        public int getEndColumn() {
            return endColumn;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
        
    }
}
