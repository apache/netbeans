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

package org.netbeans.modules.editor.indent.spi;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.editor.indent.IndentImpl;
import org.netbeans.modules.editor.indent.IndentSpiPackageAccessor;
import org.netbeans.modules.editor.indent.TaskHandler;
import org.openide.util.Lookup;


/**
 * Context information for both indentation and reformatting.
 * <br/>
 * A common class allows to conveniently share code between indentation and reformatting.
 * {@link #isIndent()} allows to check whether the actual processing
 * is indentation or reformatting.
 *
 * @author Miloslav Metelka
 */

public final class Context {
   
    static {
        IndentSpiPackageAccessor.register(new PackageAccessor());
    }

    private TaskHandler.MimeItem mimeItem;

    Context(TaskHandler.MimeItem mimeItem) {
        this.mimeItem = mimeItem;
    }

    /**
     * Returns lookup which is merge of lookups provided by individual
     * formatters involved in formatting given document. This method returns
     * a valid non-null value only after formatting was started, that is in and after
     * {@link IndentTask#reindent()} or {@link ReformatTask#reformat()}.
     * 
     * @return merge lookup provided by individual formatters; will be null
     *  before formatting starts
     *
     * @since org.netbeans.modules.editor.indent/2 1.12
     */
    public Lookup getLookup() {
        assert mimeItem.handler().getLookup() != null : "you are calling getLookup() too early - re-read Javadoc";
        return mimeItem.handler().getLookup();
    }

    /**
     * Document for which the reformatting or indenting is being done.
     */
    public Document document() {
        return mimeItem.handler().document();
    }
    
    /**
     * Get mimePath of this context as string.
     * <br/>
     * The indent or reformat task should only care about indentation
     * of the code that belongs to this mime path.
     * 
     * @return non-null mime path string.
     */
    public String mimePath() {
        return mimeItem.mimePath().getPath();
    }

    /**
     * Starting offset of the area to be reformatted or reindented.
     * <br/>
     * The value gets updated accordingly when the reformatter performs modifications
     * in the affected area.
     */
    public int startOffset() {
        Position startPos = mimeItem.handler().startPos();
        return (startPos != null) ? startPos.getOffset() : -1;
    }

    /**
     * Starting offset of the area to be reformatted or reindented.
     * <br/>
     * The value gets updated accordingly when the reformatter performs modifications
     * in the affected area.
     */
    public int endOffset() {
        Position endPos = mimeItem.handler().endPos();
        return (endPos != null) ? endPos.getOffset() : -1;
    }
    
    /**
     * Determine start of the line for the given offset.
     * 
     * @param offset offset in a document being formatted.
     * @return offset of start of the line.
     * @throws javax.swing.text.BadLocationException if the given offset is not within
     *  corresponding document's bounds.
     */
    public int lineStartOffset(int offset) throws BadLocationException {
        return IndentUtils.lineStartOffset(mimeItem.handler().document(), offset);
    }
    
    /**
     * Determine indent on the given line as a number of spaces.
     * 
     * @param lineStartOffset start offset of a line where the indent is being determined.
     * @return indent on the given line. Possible tabs are translated into space count accordingly.
     * @throws javax.swing.text.BadLocationException if the given lineStartOffset is not within
     *  corresponding document's bounds.
     */
    public int lineIndent(int lineStartOffset) throws BadLocationException {
        return IndentUtils.lineIndent(mimeItem.handler().document(), lineStartOffset);
    }

    /**
     * Modify indent of the line at the offset passed as the parameter.
     *
     * @param lineStartOffset start offset of a line where the indent is being modified.
     * @param newIndent new indent as a number of spaces. The method will possibly use tabs
     *  according to the indentation settings for the given document.
     * @throws javax.swing.text.BadLocationException if the given lineStartOffset is not within
     *  corresponding document's bounds.
     */
    public void modifyIndent(int lineStartOffset, int newIndent) throws BadLocationException {
        Document doc = document();
        IndentImpl.checkOffsetInDocument(doc, lineStartOffset);
        // Determine old indent first together with oldIndentEndOffset
        int indent = 0;
        int tabSize = -1;
        CharSequence docText = DocumentUtilities.getText(doc);
        int oldIndentEndOffset = lineStartOffset;
        while (oldIndentEndOffset < docText.length()) {
            char ch = docText.charAt(oldIndentEndOffset);
            if (ch == '\n') {
                break;
            } else if (ch == '\t') {
                if (tabSize == -1)
                    tabSize = IndentUtils.tabSize(doc);
                // Round to next tab stop
                indent = (indent + tabSize) / tabSize * tabSize;
            } else if (Character.isWhitespace(ch)) {
                indent++;
            } else { // non-whitespace
                break;
            }
            oldIndentEndOffset++;
        }

        String newIndentString = IndentUtils.createIndentString(doc, newIndent);
        // Attempt to match the begining characters
        int offset = lineStartOffset;
        for (int i = 0; i < newIndentString.length() && lineStartOffset + i < oldIndentEndOffset; i++) {
            if (newIndentString.charAt(i) != docText.charAt(lineStartOffset + i)) {
                offset = lineStartOffset + i;
                newIndentString = newIndentString.substring(i);
                break;
            }
        }
        
        // Replace the old indent
        if (!doc.getText(offset, oldIndentEndOffset - offset).equals(newIndentString)) {
            if (offset < oldIndentEndOffset) {
                doc.remove(offset, oldIndentEndOffset - offset);
            }
            if (newIndentString.length() > 0) {
                doc.insertString(offset, newIndentString, null);
            }
        }
    }
    
    /**
     * Return offset of the caret passed to the indentation
     * infrastructure.
     * <br/>
     * Since it's maintained as a swing position the offset will increase
     * by subsequent modifications by the underlying indent task(s).
     * <br/>
     * If a particular task wishes to modify the offset explicitly
     * it can do so by {@link #setCaretOffset(int)}.
     * 
     * @return current caret offset.
     */
    public int caretOffset() {
        return mimeItem.handler().caretOffset();
    }
    
    /**
     * Override the offset at which the caret should be placed after the indentation
     * is finished.
     * <br/>
     *  This is only relevant for indentation not for reformatting.
     *
     * @param offset new offset where the caret should be placed.
     * @throws javax.swing.text.BadLocationException if the given offset is outside
     *  of underlying document bounds.
     * @see #caretOffset()
     */
    public void setCaretOffset(int offset) throws BadLocationException {
        mimeItem.handler().setCaretOffset(offset);
    }
    
    /**
     * Get list of regions for the given mime-path
     * where the indent or reformat task should operate.
     * <br/>
     * The region boundaries are held as positions so they should update
     * by subsequent inserts/removals.
     * 
     * @return non-null list of regions.
     */
    public List<Region> indentRegions() {
        return mimeItem.indentRegions();
    }

    /*
     * Check whether the actual processing
     * is indentation or reformatting.
     * 
     * <br/>
     * Indent tasks may be used for reformatting in case a reformat task
     * is not available (for the given mimepath) but the indent task is available.
     * 
     * @return true if indentation is being done or false for reformatting.
     */
    public boolean isIndent() {
        return mimeItem.handler().isIndent();
    }
    
    /**
     * Description of the region where the indentation/reformatting should operate.
     */
    public static final class Region {

        MutablePositionRegion region;
        
        Region(MutablePositionRegion region) {
            this.region = region;
        }

        /**
         * Get start offset of the region.
         * 
         * @return start offset of the region.
         */
        public int getStartOffset() {
            return region.getStartOffset();
        }

        /**
         * Get end offset of the region.
         * 
         * @return end offset of the region.
         */
        public int getEndOffset() {
            return region.getEndOffset();
        }
        
        MutablePositionRegion positionRegion() {
            return region;
        }

    }

    private static final class PackageAccessor extends IndentSpiPackageAccessor {

        public Context createContext(TaskHandler.MimeItem mimeItem) {
            return new Context(mimeItem);
        }
        
        public Context.Region createContextRegion(MutablePositionRegion region) {
            return new Region(region);
        }
    
        public MutablePositionRegion positionRegion(Context.Region region) {
            return region.positionRegion();
        }
    


    }

}
