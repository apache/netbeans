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

package org.netbeans.api.java.source;

import static org.netbeans.modules.java.source.save.PositionEstimator.*;

/**
 * An individual comment, consisting of a style, begin and end source
 * file position, the indention (column) of its first character, and its text.
 *
 * @since 0.43
 */
public final class Comment {
    private Style style;
    private int pos;
    private int endPos;
    private int indent;
    private String text;

    /**
     * The set of different comment types.
     */
    public enum Style {
        /**
         * A line (double-slash) comment.
         */
        LINE,
        
        /**
         * A block comment.
         */
        BLOCK,
        
        /**
         * A JavaDoc comment.
         */
        JAVADOC,
        
        /**
         * Whitespace
         * TODO: not comment, but requested by another teams to preserve
         * empty lines etc.
         */
        WHITESPACE;
    }

    /**
     * Define a new block comment from a string.  This comment does not
     * have source file positions.
     * @param s textual content of comment. With or without proper escaping
     * @return new comment
     */
    public static Comment create(String s) {
        return new Comment(Style.BLOCK, NOPOS, NOPOS, NOPOS, s);
    }

    /**
     * Define a comment, using source file positions. 
     * @param style the style of comment
     * @param pos start position within source file
     * @param endPos end position within source file
     * @param indent indentation of comment
     * @param text textual content of comment. With or without proper escaping
     * @return new comment
     */
    public static Comment create(Style style, int pos, int endPos, int indent, String text) {
        return new Comment(style, pos, endPos, indent, text);
    }
    
    /**
     * Define a comment, using specified style.
     * @param style the style of comment
     * @param text textual content of comment. With or without proper escaping
     * @return new comment
     */
    public static Comment create(Style style, String text) {
        return new Comment(style, NOPOS, NOPOS, NOPOS, text);
    }

    /**
     * Define a comment, using source file positions.
     * @param style the style of comment
     * @param pos start position within source file
     * @param endPos end position within source file
     * @param indent indentation of comment
     * @param text textual content of comment. With or without proper escaping
     */
    private Comment(Style style, int pos, int endPos, int indent, String text) {
        this.style = style;
        this.pos = pos;
        this.endPos = endPos;
        this.indent = indent;
        this.text = text;
    }
    
    public Style style() {
        return style;
    }

    /**
     * The start position in the source file, or NOPOS if the
     * comment was added by a translation operation.
     */
    public int pos() {
        return pos;
    }

    /**
     * The end position in the source file, or NOPOS if the
     * comment was added by a translation operation.
     */
    public int endPos() {
        return endPos;
    }

    /**
     * Returns the line indention for this comment, or NOPOS if the
     * comment was added by a translation operation.
     */
    public int indent() {
        return indent;
    }
    
    /** Returns true if this is a JavaDoc comment. */
    public boolean isDocComment() {
        return style == Style.JAVADOC;
    }

    /**
     * Returns the comment text.
     */
    public String getText() {
        return text;
    }
    
    public boolean isNew() {
        return pos == NOPOS;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(style.toString());
        sb.append(" pos=");
        sb.append(pos);
        sb.append(" endPos=");
        sb.append(endPos);
        sb.append(" indent=");
        sb.append(indent);
        sb.append(' ');
        sb.append(text);
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Comment))
            return false;
        Comment c = (Comment)obj;
        return c.style == style && c.pos == pos && c.endPos == endPos &&
            c.indent == indent && c.text.equals(text);
    }

    public int hashCode() {
        return style.hashCode() + pos + endPos + indent + text.hashCode();
    }
}
