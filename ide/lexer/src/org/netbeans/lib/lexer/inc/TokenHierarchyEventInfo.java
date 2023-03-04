/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.TokenHierarchyOperation;

/**
 * Shared information for all the token list changes
 * for a single token hierarchy event.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyEventInfo {

    // True to log real source chars
    // -J-Dorg.netbeans.editor.log.source.text=true
    static final boolean LOG_SOURCE_TEXT = Boolean.getBoolean("org.netbeans.editor.log.source.text"); // NOI18N

    private final TokenHierarchyOperation<?,?> tokenHierarchyOperation;

    private final TokenHierarchyEventType type;
    
    private TokenChange<?> tokenChange;

    private final int modOffset;

    private final int removedLength;

    private final CharSequence removedText;

    private final int insertedLength;
    
    private final int diffLengthOrZero;

    private OriginalText originalText;
    
    private int affectedStartOffset;
    
    private int affectedEndOffset;

    public TokenHierarchyEventInfo(TokenHierarchyOperation<?,?> tokenHierarchyOperation,
    TokenHierarchyEventType type, int modificationOffset, int removedLength, CharSequence removedText, int insertedLength) {
        // Initial checks
        if (modificationOffset < 0) {
            throw new IllegalArgumentException("modificationOffset=" + modificationOffset + " < 0"); // NOI18N
        }
        if (removedLength < 0) {
            throw new IllegalArgumentException("removedLength=" + removedLength + " < 0"); // NOI18N
        }
        if (insertedLength < 0) {
            throw new IllegalArgumentException("insertedLength=" + insertedLength + " < 0"); // NOI18N
        }

        this.tokenHierarchyOperation = tokenHierarchyOperation;
        this.type = type;
        this.modOffset = modificationOffset;
        this.removedLength = removedLength;
        this.removedText = removedText;
        this.insertedLength = insertedLength;
        this.diffLengthOrZero = Math.max(0, insertedLength - removedLength);
        this.affectedStartOffset = modificationOffset;
        this.affectedEndOffset = modificationOffset + diffLengthOrZero;
    }

    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }

    public TokenHierarchyEventType type() {
        return type;
    }
    
    public TokenChange<?> tokenChange() {
        return tokenChange;
    }
    
    public void setTokenChangeInfo(TokenChangeInfo<?> info) {
        this.tokenChange = LexerApiPackageAccessor.get().createTokenChange(info);
    }
    
    public int affectedStartOffset() {
        return affectedStartOffset;
    }
    
    public void setMinAffectedStartOffset(int affectedStartOffset) {
        if (affectedStartOffset < this.affectedStartOffset) {
            this.affectedStartOffset = affectedStartOffset;
        }
    }

    public int affectedEndOffset() {
        return affectedEndOffset;
    }
    
    public void setMaxAffectedEndOffset(int affectedEndOffset) {
        if (affectedEndOffset > this.affectedEndOffset) {
            this.affectedEndOffset = affectedEndOffset;
        }
    }

    public int modOffset() {
        return modOffset;
    }

    public int removedLength() {
        return removedLength;
    }

    public CharSequence removedText() {
        return removedText;
    }

    public int insertedLength() {
        return insertedLength;
    }
    
    public CharSequence insertedText() {
        return currentText().subSequence(modOffset(), modOffset() + insertedLength());
    }
    
    public int diffLength() {
        return insertedLength - removedLength;
    }
    
    /**
     * Get <code>Math.max(0, insertedLength() - removedLength())</code>.
     */
    public int diffLengthOrZero() {
        return diffLengthOrZero;
    }

    public OriginalText originalText() {
        if (originalText == null) {
            if (removedLength != 0 && removedText == null) {
                throw new IllegalStateException("Cannot obtain removed text for " // NOI18N
                        + tokenHierarchyOperation.inputSource()
                        + " which breaks token snapshots operation and" // NOI18N
                        + " token text retaining after token's removal." // NOI18N
                        + " Valid removedText in TokenHierarchyControl.textModified()" // NOI18N
                        + " should be provided." // NOI18N
                        );
            }
            originalText = new OriginalText(currentText(),
                    modOffset, removedText, insertedLength);
        }
        return originalText;
    }
    
    public CharSequence currentText() {
        return tokenHierarchyOperation.text();
    }
    
    public String modificationDescription(boolean detail) {
        StringBuilder sb = new StringBuilder(originalText().length() + 300);
        if (removedLength() > 0) {
            sb.append("TEXT REMOVED at ").append(modOffset()).append(" len="). // NOI18N
                    append(removedLength());
            if (removedText() != null) {
                sb.append(" \"");
                CharSequenceUtilities.debugText(sb, removedText());
                sb.append('"');
            }
            sb.append('\n');
        }
        if (insertedLength() > 0) {
            sb.append("TEXT INSERTED at ").append(modOffset()).append(" len="). // NOI18N
                    append(insertedLength()).append(" \""); // NOI18N
            CharSequenceUtilities.debugText(sb, insertedText());
            sb.append("\"\n");
        }
        if (LOG_SOURCE_TEXT && detail) {
            sb.append("\n\n----------------- ORIGINAL TEXT -----------------\n" + // NOI18N
                originalText() +
                "\n----------------- BEFORE-CARET TEXT -----------------\n" + // NOI18N
                originalText().subSequence(0, modOffset()) +
                "|<--CARET\n" // NOI18N
            );
        }
        return sb.toString();
    }
    
    public String dumpAffected() {
        StringBuilder sb = new StringBuilder("Affected(");
        sb.append(affectedStartOffset());
        sb.append(",");
        sb.append(affectedEndOffset());
        sb.append(')');
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("modOffset="); // NOI18N
        sb.append(modOffset());
        if (removedLength() > 0) {
            sb.append(", removedLength=");
            sb.append(removedLength());
        }
        if (insertedLength() > 0) {
            sb.append(", insertedLength=");
            sb.append(insertedLength()); // NOI18N
        }
        sb.append(", ").append(dumpAffected());
        return sb.toString();
    }

}
