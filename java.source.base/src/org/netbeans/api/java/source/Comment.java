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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
     * @param indent indentation of comment
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
