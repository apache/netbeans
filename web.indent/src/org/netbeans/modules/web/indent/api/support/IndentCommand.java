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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.indent.api.support;

/**
 * Indentation command descriptor.
 *
 * @since org.netbeans.modules.css.editor/1 1.3
 */
public final class IndentCommand {

    /**
     * Enumeration of indentation command types.
     */
    public static enum Type {

        /**
         * This line should be indented.
         */
        INDENT,

        /**
         * This line should be un-indented.
         */
        RETURN,

        /**
         * There was no change, apply whatever indentation was set for
         * previous line.
         */
        NO_CHANGE,

        /**
         * This line is continuation of statement from previous line and first
         * (and only first) occurance of CONTINUE should be indented. Similar
         * to INDENT but does not need to be finished with RETURN - it
         * will end automatically on first occurance of command whic is not
         * CONITNUE.
         */
        CONTINUE,

        /**
         * Ignore line's formatting, for example content of HTML's <pre> tag
         * should not be touched. Any other line indentation commands are ignored
         * if presented together with DO_NOT_INDENT_THIS_LINE.
         */
        DO_NOT_INDENT_THIS_LINE,

        /**
         * Preserve indentation of this line relatively to previous 
         * non-PRESERVE_INDENTATION line. Difference from DO_NOT_INDENT_THIS_LINE 
         * is that DO_NOT_INDENT_THIS_LINE does not touch line's indentation but
         * PRESERVE_INDENTATION can shit line(s) indent. For example in case
         * of block comment in HTML whole block comment indent can be changed
         * but indentation within the block should be preserved.
         */
        PRESERVE_INDENTATION,

        /**
         * Foreign language block between lines carying BLOCK_START and BLOCK_END
         * will be automatically shifted. Blocks cannot overlap or be nested.
         * Useful for example in case of JSP's scriptlet tag which contains block
         * of Java code and which starts with &lt;% and ends with %&gt;.
         */
        BLOCK_START,
        BLOCK_END,

        /**
         * Checkpoint to be set whenever indentation code can deduce without
         * mistake certain indentation level. The idea is that with emebedded
         * languages it is possible to break indentation level if for example
         * closing of block or tag is generated dynamically. In such a case
         * stack of indentation state can get inconsistent. When indentation
         * process can reestablish correct indentation level it can express it via
         * INDENT_CHECKPOINT type of indent. For example in CSS this could be when
         * a new rule start - we can assert that indent level should be zero; in
         * Java example this could possibly be end of class definition. Indentation
         * commands processor will use INDENT_CHECKPOINT and compare it with existing
         * indent and (this is the idea) could walk back the lines and readjust
         * indentation automatically to match INDENT_CHECKPOINT. How far back
         * readjustment would work? Until previous INDENT_CHECKPOINT or embedded
         * language block.
         */
        //INDENT_CHECKPOINT,

        /**
         * Similar to INDENT but does not need to be finished with RETURN - it
         * will end automatically on next line which is not CONITNUE. The purpose
         * is to handle "hanging" indent of multiline statements.
         */
        //SINGLE_INDENT,

    }

    private Type type;
    private int fixedIndentSize;
    private int lineOffset;
    private int indentation;
    private int indentationSize;
    private boolean wasContinue;

    public IndentCommand(Type type, int lineOffset) {
        this(type, lineOffset, -1);
    }

    public IndentCommand(Type type, int lineOffset, int indentationSize) {
        this.type = type;
        this.lineOffset = lineOffset;
        this.fixedIndentSize  = -1;
        // #242649 - each language can have different indentation level:
        this.indentationSize = indentationSize;
    }

    public int getFixedIndentSize() {
        return fixedIndentSize;
    }

    public void setFixedIndentSize(int fixedIndentSize) {
        this.fixedIndentSize = fixedIndentSize;
    }

    public int getIndentationSize() {
        return indentationSize;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    public Type getType() {
        return type;
    }

    void setCalculatedIndentation(int indentation) {
        this.indentation = indentation;
    }

    int getCalculatedIndentation() {
        return indentation;
    }

    IndentCommand cloneMe() {
        IndentCommand ic = new IndentCommand(type, lineOffset);
        ic.fixedIndentSize = fixedIndentSize;
        ic.indentation = indentation;
        ic.wasContinue = wasContinue;
        return ic;
    }

    boolean wasContinue() {
        return wasContinue;
    }

    void setWasContinue() {
        this.wasContinue = true;
    }

    @Override
    public String toString() {
        return "IndentCommand[type="+type+
                (fixedIndentSize != -1 ? " fixedIndent="+fixedIndentSize : "")+
                " lineOffset="+lineOffset+"]"; // NOI18N
    }

    void updateOffset(int diff) {
        lineOffset += diff;
    }

}
