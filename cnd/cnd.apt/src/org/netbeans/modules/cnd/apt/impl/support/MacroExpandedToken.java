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
