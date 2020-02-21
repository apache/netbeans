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

package org.netbeans.modules.cnd.editor.fortran.reformat;

import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 */
public class FortranReformatter implements ReformatTask {
    private Context context;
    private final Document doc;
    private FortranCodeStyle codeStyle;
    private boolean expandTabToSpaces = true;
    private int tabSize = 8;

    public FortranReformatter(Context context) {
        this.context = context;
        this.doc = context.document();
    }

    public FortranReformatter(Document doc, FortranCodeStyle codeStyle) {
        this.doc = doc;
        this.codeStyle = codeStyle;
    }

    @Override
    public void reformat() throws BadLocationException {
        if (codeStyle == null){
            codeStyle = FortranCodeStyle.get(doc);
        }
        codeStyle.setupLexerAttributes(doc);
        expandTabToSpaces = codeStyle.expandTabToSpaces();
        tabSize = codeStyle.getTabSize();
        if (tabSize <= 1) {
            tabSize = 8;
        }
        if (Boolean.TRUE.equals(doc.getProperty("code-template-insert-handler"))) { // NOI18N
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                if (fo != null) {
                    String lf = (String)fo.getAttribute(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
                    if (lf != null) {
                        doc.putProperty(FileObject.DEFAULT_LINE_SEPARATOR_ATTR, lf);
                        doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, lf);
                    }
                    doc.insertString(0, "", null); // NOI18N
                }
            }
        }
        if (context != null) {
            if (MIMENames.FORTRAN_MIME_TYPE.equals(context.mimePath())) {
                for (Context.Region region : context.indentRegions()) {
                    TokenHierarchy<?> hierarchy = TokenHierarchy.create(doc.getText(0, doc.getLength()), FortranTokenId.languageFortran());
                    TokenSequence<FortranTokenId> ts = (TokenSequence<FortranTokenId>)hierarchy.tokenSequence();
                    ts.move(0);
                    reformatImpl(ts, region.getStartOffset(), region.getEndOffset());
                    break;
                }
            }
        } else {
            int endOffset = doc.getLength();
            TokenSequence<FortranTokenId> ts = CndLexerUtilities.getFortranTokenSequence(doc, 0);
            reformatImpl(ts, 0, endOffset);
        }
    }

    @Override
    public ExtraLock reformatLock() {
        return null;
    }

    private void reformatImpl(TokenSequence<FortranTokenId> ts, int startOffset, int endOffset) throws BadLocationException {
        int prevStart = -1;
        int prevEnd = -1;
        String prevText = null;
        for (Diff diff : new FortranReformatterImpl(ts, startOffset, endOffset, codeStyle).reformat()) {
            int curStart = diff.getStartOffset();
            int curEnd = diff.getEndOffset();
            if (startOffset > curEnd || endOffset < curStart) {
                continue;
            }
            String curText = diff.getText(expandTabToSpaces, tabSize);
            if (endOffset < curEnd) {
                if (curText != null && curText.length() > 0) {
                    curText = curEnd - endOffset >= curText.length() ? null :
                           curText.substring(0, curText.length() - curEnd + endOffset);
                }
                curEnd = endOffset;
            }
            if (prevStart == curEnd) {
                prevStart = curStart;
                prevText = curText+prevText;
                continue;
            } else {
                if (!applyDiff(prevStart, prevEnd, prevText)){
                    return;
                }
                prevStart = curStart;
                prevEnd = curEnd;
                prevText = curText;
            }
        }
        if (prevStart > -1) {
            applyDiff(prevStart, prevEnd, prevText);
        }
    }

    private boolean applyDiff(int start, int end, String text) throws BadLocationException{
        if (end - start > 0) {
            String what = doc.getText(start, end - start);
            if (text != null && text.equals(what)) {
                // optimization
                return true;
            }
            if (!checkRemoved(what)){
                // Reformat failed
                Logger log = Logger.getLogger("org.netbeans.modules.cnd.editor"); // NOI18N
                String error = NbBundle.getMessage(FortranReformatter.class, "REFORMATTING_FAILED", // NOI18N
                        doc.getText(start, end - start), text);
                log.severe(error);
                return false;
            }
            doc.remove(start, end - start);
        }
        if (text != null && text.length() > 0) {
            doc.insertString(start, text, null);
        }
        return true;
    }

    private boolean checkRemoved(String whatRemoved){
        for(int i = 0; i < whatRemoved.length(); i++){
            char c = whatRemoved.charAt(i);
            switch(c){
                case ' ':
                case '\n':
                case '\t':
                case '\r':
                case '\f':
                case 0x0b:
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static class Factory implements ReformatTask.Factory {

        @Override
        public ReformatTask createTask(Context context) {
            return new FortranReformatter(context);
        }
    }

    static class Diff {

        private final int start;
        private final int end;
        private int newLines;
        private int spaces;
        private boolean isIndent;

        Diff(int start, int end, int newLines, int spaces, boolean isIndent) {
            this.start = start;
            this.end = end;
            this.spaces = spaces;
            this.newLines = newLines;
            this.isIndent = isIndent;
        }

        public int getStartOffset() {
            return start;
        }

        public int getEndOffset() {
            return end;
        }

        public String getText(boolean expandTabToSpaces, int tabSize) {
            return repeatChar(newLines, '\n', false, expandTabToSpaces, tabSize) + repeatChar(spaces, ' ', isIndent, expandTabToSpaces, tabSize); // NOI18N
        }

        public void setText(int newLines, int spaces, boolean isIndent) {
            this.newLines = newLines;
            this.spaces = spaces;
            this.isIndent = isIndent;
        }

        public void replaceSpaces(int spaces, boolean isIndent) {
            this.spaces = spaces;
            this.isIndent = isIndent;
        }

        public boolean hasNewLine() {
            return newLines > 0;
        }

        public int spaceLength() {
            return spaces;
        }

        @Override
        public String toString() {
            return "Diff<" + start + "," + end + ">: newLines=" + newLines + " spaces=" + spaces; //NOI18N
        }

        private static String repeatChar(int length, char c, boolean indent, boolean expandTabToSpaces, int tabSize) {
            if (length == 0) {
                return ""; //NOI18N
            } else if (length == 1) {
                if (c == ' ') {
                    return " "; //NOI18N
                } else {
                    return "\n"; //NOI18N
                }
            }
            StringBuilder buf = new StringBuilder(length);
            if (c == ' ' && indent && !expandTabToSpaces && tabSize > 1) {
                while (length >= tabSize) {
                    buf.append('\t'); //NOI18N
                    length -= tabSize;
                }
            }
            for (int i = 0; i < length; i++) {
                buf.append(c);
            }
            return buf.toString();
        }

        public static boolean equals(String text, int newLines, int spaces, boolean isIndent, boolean expandTabToSpaces, int tabSize) {
            String space = repeatChar(newLines, '\n', false, expandTabToSpaces, tabSize) + repeatChar(spaces, ' ', isIndent, expandTabToSpaces, tabSize); // NOI18N
            return text.equals(space);
        }
    }
}
