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
package org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet;

//import com.sun.tools.analytics.utils.CndFileUtilBridge;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public final class CodeSnippet {

    private FileObject fo;
    private volatile AnnotatedCode code;
    private final String fileURI;
    private final int line;
//    private final int column;
    private final String path;
    private final int[][] startLineColumns;
    private final int[][] endLineColumns;
    static boolean COLORIZATION_ENABLED = CndUtils.getBoolean("cnd.clank.diagnostics.colorize.snippets", false); //NOI18N

    public CodeSnippet(FileObject fo, String path, int[][] startLineColumns, int[][] endLineColumns) {
        this(fo, null, path, startLineColumns, endLineColumns);//, descr);
    }

    public CodeSnippet(String fileURI, String path,  int[][] startLineColumns, int[][] endLineColumns) {
        this(null, fileURI, path, startLineColumns, endLineColumns);//, descr);
    }

    private CodeSnippet(FileObject fo, String fileURI, String path, int[][] startLineColumns, int[][] endLineColumns) {
        this.fo = fo;
        this.line = startLineColumns[0][0];
        this.path = path;
        this.fileURI = fileURI;
        this.startLineColumns = startLineColumns;
        this.endLineColumns = endLineColumns;     
        assert fileURI != null || fo != null : "fo or fileURI should be not null: fo = " + fo + "\n uri = " + fileURI; //NOI18N
    }

//    public Description getDescription() {
//        return descr;
//    }


    public String getFilePath() {
        return path;
    }

    public synchronized FileObject getFileObject() {
        if (fo == null && fileURI != null) {
            String uri = this.fileURI;
            //   fo = URLMapper.findFileObject(new URL(uri));
            fo = CndFileUtils.urlToFileObject(uri);
        }
        return fo;
    }

    public int getLine() {
        return line;
    }

//    public int getColumn() {
//        return column;
//    }

    public synchronized AnnotatedCode getCode() throws IOException {
        if (code == null) {
            code = AnnotatedCode.create(this);
        }
        return code;
    }

    @Override
    public String toString() {
        return "CodeSnippet{" + "path=" + path + ", fo=" + fo + '}';//NOI18N
    }

    public boolean isAnnotatedCodeReady() {
        return code != null;
    }

    public static final class AnnotatedCode {

        private static void calculateColors(LineInfo lineInfo, TokenSequence<? extends TokenId> ts) {
            FontColorSettings settings = null;
            LanguagePath languagePath = ts.languagePath();
            while ( languagePath != null && settings == null) {
                String mime = languagePath.mimePath();
                Lookup lookup = MimeLookup.getLookup(mime);
                settings = lookup.lookup(FontColorSettings.class);
            }
            int offset = 0;
            while (ts.moveNext()) {
                Token<?> token = ts.token();
                TokenSequence<?> es = ts.embedded();
                if (es != null && es.language() == CppTokenId.languagePreproc()) {
                    calculateColors(lineInfo, es);
                } else {
                    String category = token.id().primaryCategory();
                    if (category == null) {
                        category = CppTokenId.WHITESPACE_CATEGORY; //NOI18N
                    }
                    String text = token.text().toString();
                    AttributeSet set = null;
                    if (settings != null) {
                        set = settings.getTokenFontColors(category);
                    } 
                    if (set != null) {
                        lineInfo.attrs.add(new LineAttribute(offset, text.length(), set));
                    }
                    offset += text.length();
                }
            }
        }

        private final String mimeType;
        private final List<LineInfo> lines;        

        private AnnotatedCode(List<LineInfo> lines, String mimeType) {
            this.mimeType = mimeType;
            this.lines = lines;
        }

        public String getText() {
            StringBuilder out = new StringBuilder();
            for (LineInfo line : lines) {
                out.append(line.lineNumberPrefix).append(line.lineText).append('\n');
            }
            return out.toString();
        }

        public String getMimeType() {
            return mimeType;
        }

        public Collection<LineInfo> getAnnotations() {
            Collection<LineInfo> out = new ArrayList<LineInfo>(1);
            for (LineInfo line : lines) {
                if (line.type == LineType.ANNOTATION || line.type == LineType.ERROR) {
                    out.add(line);
                }
            }
            return out;
        }

        public List<LineInfo> getLines() {
            return Collections.unmodifiableList(lines);
        }

        private static final int CONTEXT_LINE_NUM = Integer.getInteger("diagnostic.code.snippet.lines", 1);//NOI18N

        private static AnnotatedCode create(CodeSnippet codeSnippet) throws IOException {
            FileObject fo = codeSnippet.getFileObject();
            CsmFile csmFile = CsmUtilities.getCsmFile(fo, true, false);
            // line is 1-based number
            int line = codeSnippet.getLine();
            Language<CppTokenId> lang = CndLexerUtilities.getLanguage(codeSnippet.getFileObject().getMIMEType());
            int currentLineNumber = 1;
            int lastLineNumber = codeSnippet.endLineColumns[codeSnippet.endLineColumns.length - 1][0];
            int context_lines_after = lastLineNumber - line > 0 ? lastLineNumber -line + 1 : CONTEXT_LINE_NUM;
            if (fo != null) {
                List<String> asLines = fo.asLines();
                if (asLines.size() >= line) {
                    List<LineInfo> lines = new ArrayList<LineInfo>(4);
                    String lineNumber = toLineNumber(line);
                    String space = String.format("%" + lineNumber.length() + "s", " ");//NOI18N
                    // add prev line if any
                    int prevLineIndex = line - 1;
                    int addedBeforeIssueLines = 0;
                    while (prevLineIndex > 0) {
                        String lineText = asLines.get(prevLineIndex - 1);
                        if (!lineText.isEmpty()) {
                            final LineInfo lineInfo = new LineInfo(LineType.SOURCE, lineText, toLineNumber(prevLineIndex), prevLineIndex);
                            if (COLORIZATION_ENABLED) {
                                TokenHierarchy<?> tokenH = TokenHierarchy.create(lineText, lang);
                                TokenSequence<?> tok = tokenH.tokenSequence();
                                calculateColors(lineInfo, tok);
                            }
                            
                            lines.add(0, lineInfo);
                            if (addedBeforeIssueLines++ > CONTEXT_LINE_NUM) {
                                break;
                            }
                        }
                        currentLineNumber++;
                        prevLineIndex--;
                    }
                    //and now add annotated lines the number of annotated lines = 
                    int curAnnotatedLine = line -1;
                    int countDown = lastLineNumber - line + 1;
                    while (countDown >0 ) {
                        String lineWithIssueText = asLines.get(curAnnotatedLine);
                        final String toLineNumber = toLineNumber(curAnnotatedLine + 1);
                        LineInfo lineInfo = new LineInfo(LineType.ANNOTATION, lineWithIssueText, toLineNumber, curAnnotatedLine + 1);
                        if (COLORIZATION_ENABLED) {
                            TokenHierarchy<?> tokenH = TokenHierarchy.create(lineWithIssueText, lang);
                            TokenSequence<?> tok = tokenH.tokenSequence();
                            calculateColors(lineInfo, tok);
                        }
                        //start column in this line
                        lines.add(lineInfo);
                        int size = codeSnippet.startLineColumns.length;
                        //it can be 22:23 - 24:7
                        //it can be 22:1-22:6 22:7-22:8
//                        lineInfo.startHLColumn = new int[1];
//                        lineInfo.endHLColumns = new int[1];
                        ArrayList<Integer> starts = new ArrayList<>();
                        ArrayList<Integer> ends = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            if (codeSnippet.startLineColumns[i][0] == curAnnotatedLine + 1){
                                starts.add(codeSnippet.startLineColumns[i][1]);
                                if (codeSnippet.endLineColumns[i][0] == curAnnotatedLine + 1) {
                                    ends.add(codeSnippet.endLineColumns[i][1]);
                                }
                                ends.add(-1);
                                continue;
                            }
                            //if the start line less and end line more - means the whole line
                            if (codeSnippet.startLineColumns[i][0] < curAnnotatedLine + 1) {
                                if (codeSnippet.endLineColumns[i][0] > curAnnotatedLine + 1) {
                                    //mark the whole line
                                    starts.add(0);
                                    ends.add(-1);
                                } else {
                                    starts.add(0);
                                    ends.add(codeSnippet.endLineColumns[i][1]);
                                }
                            }
                        }
                        lineInfo.startHLColumn = new int[starts.size()];
                        lineInfo.endHLColumns = new int[starts.size()];
                        for (int i = 0; i < starts.size(); i++) {
                            lineInfo.startHLColumn[i] = starts.get(i);
                            lineInfo.endHLColumns[i] = ends.get(i);
                        }
                        curAnnotatedLine++;
                        countDown--;
                    }

                    
                    int nextLineIndex = curAnnotatedLine + 1;
                    int addedAfterIssueLines = 0;
                    currentLineNumber++;
                    while (asLines.size() >= nextLineIndex) {
                        String lineText = asLines.get(nextLineIndex - 1);
                        if (!lineText.isEmpty()) {
                            LineInfo lineInfo = new LineInfo(LineType.SOURCE, lineText, toLineNumber(nextLineIndex), nextLineIndex);
                            lines.add(lineInfo);
                            if (COLORIZATION_ENABLED) {
                                TokenHierarchy<?> tokenH = TokenHierarchy.create(lineText, lang);
                                TokenSequence<?> tok = tokenH.tokenSequence();                    
                                calculateColors(lineInfo, tok);
                            }
                                //we will get BadLocationException if the range is wider 
                            if (++addedAfterIssueLines >= context_lines_after) {
                                break;
                            }
                        }
                        nextLineIndex++;
                        currentLineNumber++;
                    }
                    final AnnotatedCode annotatedCode = new AnnotatedCode(lines, fo.getMIMEType());
                    return annotatedCode;
                }
            }
            String msg = NbBundle.getMessage(CodeSnippet.class, "LBL_NoCodeSnippet", codeSnippet.getFilePath(), line);//NOI18N
            LineInfo lineInfo = new LineInfo(LineType.ERROR, msg, "", -1);//NOI18N
            return new AnnotatedCode(Collections.singletonList(lineInfo), "text/unknown");//NOI18N
        }


        public enum LineType {
            SOURCE,
            ANNOTATION,
            ERROR,
        }
        
        public static final class LineAttribute {
            final int column;
            final int length;
            final AttributeSet attribute;

            public LineAttribute(int column, int length, AttributeSet attribute) {
                this.column = column;
                this.length = length;
                this.attribute = attribute;
            }
            
            
        }

        public static final class LineInfo {

            private final String lineText;
            private final String lineNumberPrefix;
            private final int line;
            private final LineType type;
            private int[] startHLColumn;
            private int[] endHLColumns;
            //to colorize line
            final ArrayList<LineAttribute> attrs = new ArrayList<>();

            public LineInfo(LineType type, String lineText, String lineNumberPrefix, int line) {
                this.lineText = lineText;
                this.lineNumberPrefix = lineNumberPrefix;
                this.line = line;
                this.type = type;
            }

            @Override
            public String toString() {
                return "LineInfo{" + "lineText=" + lineText + ", lineNumberPrefix=" + lineNumberPrefix + ", line=" + line + '}';//NOI18N
            }

            public LineType getType() {
                return type;
            }

            public int getLine() {
                return line;
            }
            
            int[] getStartHLColumns() {
                return startHLColumn;
            }
            
            int[] getEndHLColumns() {
                return endHLColumns;
            }            

            public String getPrefix() {
                return lineNumberPrefix;
            }

            public String getText() {
                return lineText;
            }
        }
    }

    private static String toLineNumber(int line) {
        int digits = (int) Math.ceil(Math.log10(line + 1.0));
        String fmt = "%" + digits + "d:";//NOI18N
        return String.format(fmt, line);
    }
    
   

}
