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

package org.netbeans.modules.web.core.syntax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Source;

import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

import static org.netbeans.api.jsp.lexer.JspTokenId.JavaCodeType;

import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.spi.GsfUtilities;

/**
 * Utility class for generating a simplified <em>JSP servlet</em> class from a JSP file.
 * Using a full featured JSP parser would be too resource demanding,
 * we need a lightweight solution to be used with code completion.
 *
 * Inputs: original JSP document, caret offset within the original document
 * Outputs: a body of a simplified JSP servlet class, offset of the corresponding
 *          position in the servlet class
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class SimplifiedJspServlet extends JSPProcessor {
    private static final String METHOD_HEADER = "\n\tvoid mergedScriptlets(\n"
            + "\t\tHttpServletRequest request,\n"
            + "\t\tHttpServletResponse response,\n"
            + "\t\tHttpSession session,\n"
            + "\t\tServletContext application,\n"
            + "\t\tJspWriter out,\n"
            + "\t\tServletConfig config,\n"
            + "\t\tJspContext jspContext,\n"
            + "\t\tObject page,\n"
            + "\t\tPageContext pageContext,\n"
            + "\t\tThrowable exception\n"
            + "\t) throws Throwable {"; //NOI18N
    private static final String CLASS_FOOTER = "\n\t}\n}"; //NOI18N
    private CharSequence charSequence;
    private final Snapshot snapshot;
    private final ArrayList<Embedding> codeBlocks = new ArrayList<>();
    private final List<String> localImportsFound = new ArrayList<>();
    private final List<String> localBeansFound = new ArrayList<>();

    private final List<Embedding> header = new LinkedList<>();
    private final List<Embedding> scriptlets = new LinkedList<>();
    private final List<Embedding> declarations = new LinkedList<>();
    private final List<Embedding> localImports = new LinkedList<>();
    // keep bean declarations separate to avoid duplicating the declaration, see #130745
    private final List<Embedding> beanDeclarations = new LinkedList<>();
    private final List<Embedding> implicitImports = new LinkedList<>();
    private Embedding pageExtends = null;

    private int expressionIndex = 1;


    public SimplifiedJspServlet(Snapshot snapshot, Document doc){
        this(snapshot, doc, null);
    }

    public SimplifiedJspServlet(Snapshot snapshot, Document doc, CharSequence charSequence) {
        this.doc = doc;

        if (charSequence == null) {
            this.charSequence = snapshot.getText();
        } else {
            this.charSequence = charSequence;
        }

        if (doc != null){
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile(): null;
        } else {
            logger.log(Level.SEVERE, "Unable to find FileObject for document");
            fobj = null;
        }

        this.snapshot = snapshot;
    }

    private boolean isUnfinishedScriptletInQueue(TokenSequence ts) {
        Token<JspTokenId> scriptletToken = ts.token();
        if (ts.moveNext()) {
            // scriptlet is unfinished
            if (ts.token().id() != JspTokenId.SYMBOL2
                    // or it contains scriptlet starting delimiter - like <% java HTML <% java %>
                    || CharSequenceUtilities.indexOf(scriptletToken.text(), "<%") != -1) { //NOI18N
                ts.movePrevious();
                return true;
            }
            ts.movePrevious();
            return false;
        } else {
            return true;
        }
    }

    /* process under document readlock */
    @Override
    protected void renderProcess() throws BadLocationException{
        //check servlet API on classpath
        if (!isServletAPIOnClasspath()){
            SwingUtilities.invokeLater(this::displayServletAPIMissingWarning);
            processingSuccessful = false;
            return;
        }

        processIncludes(true, null);

        //XXX The InputAttribute from the document are not copied to the following TokenHierarchy,
        //the JspLexer behaviour may seem to be inaccurate in some cases!
        TokenHierarchy<CharSequence> tokenHierarchy = TokenHierarchy.create(charSequence, JspTokenId.language());

        TokenSequence<JspTokenId> tokenSequence = tokenHierarchy.tokenSequence(JspTokenId.language()); //get top level token sequence
        if (!tokenSequence.moveNext()) {
            return; //no tokens in token sequence
        }

        /**
         * process java code blocks one by one
         * note: We count on the fact the scripting language in JSP is Java
         */
        do {
            Token<JspTokenId> token = tokenSequence.token();
            String tokenText = token.text() == null ? "" : CharSequenceUtilities.toString(token.text()).trim(); //NOI18N
            if (token.id() == JspTokenId.SCRIPTLET) {
                int blockStart = token.offset(tokenHierarchy);
                // workaround for #172594
                int blockLength = Math.min(token.length(), snapshot.getText().length() - blockStart);

                JavaCodeType blockType = (JavaCodeType) token.getProperty(JspTokenId.SCRIPTLET_TOKEN_TYPE_PROPERTY);
                List<Embedding> buff = blockType == JavaCodeType.DECLARATION ? declarations : scriptlets;

                if (blockType == JavaCodeType.EXPRESSION) {
                    // the "" + (...) construction is used to preserve compatibility with pre-autoboxing java
                    // see issue #116598
                    buff.add(snapshot.create(String.format("\n\t\tObject expr%1$d = \"\" + (", expressionIndex++), "text/x-java")); //NOI18N
                    buff.add(snapshot.create(blockStart, blockLength, "text/x-java"));
                    buff.add(snapshot.create(");", "text/x-java")); //NOI18N
                } else {
                    boolean unfinishedScriptlet = false;
                    if (isUnfinishedScriptletInQueue(tokenSequence)) {
                        // see issue #213963 - we are trying to cut rest of the tag after the caret position
                        int caretOffset = GsfUtilities.getLastKnownCaretOffset(snapshot, null);
                        if (caretOffset - blockStart > 0) {
                            blockLength = Math.min(blockLength, caretOffset - blockStart);
                            unfinishedScriptlet = true;
                        }
                    }
                    buff.add(snapshot.create(blockStart, blockLength, "text/x-java"));
                    //https://netbeans.org/bugzilla/show_bug.cgi?id=231452
                    if(unfinishedScriptlet) {
                        buff.add(snapshot.create(" ; ", "text/x-java"));
                    }
                }
            } else if (token.id() == JspTokenId.TAG && "include".equals(tokenText)) {
                processIncludes(false, getIncludedPath(tokenSequence));
            }
        } while (tokenSequence.moveNext());

        processJavaInTagValues(tokenSequence); //repositions the tokenSequence


        String extendsClass = null; //NOI18N
        PageInfo pageInfo = getPageInfo();

        if (pageInfo != null) {
            extendsClass = pageInfo.getExtends();
        }

        if (extendsClass == null ||
                // workaround for issue #116314
                "org.apache.jasper.runtime.HttpJspBase".equals(extendsClass)){ //NOI18N
            extendsClass = "HttpServlet"; //NOI18N
        }

        header.add(snapshot.create("\nclass SimplifiedJSPServlet extends ", "text/x-java")); //NOI18N

        if (pageExtends != null){
            header.add(pageExtends);
        } else {
            header.add(snapshot.create(extendsClass, "text/x-java")); //NOI18N
        }

        header.add(snapshot.create(" {\n\tprivate static final long serialVersionUID = 1L;\n", "text/x-java")); //NOI18N

        implicitImports.add(snapshot.create(createImplicitImportStatements(localImportsFound), "text/x-java"));
        beanDeclarations.add(snapshot.create("\n" + createBeanVarDeclarations(localBeansFound), "text/x-java"));
    }



    private boolean consumeWS(TokenSequence tokenSequence){
        if (tokenSequence.token().id() == JspTokenId.WHITESPACE){
            return tokenSequence.moveNext();
        }

        return true;
    }

    /**
     * The information about imports obtained from the JSP Parser
     * does not include data about offsets,
     * therefore it is necessary to some manual parsing.
     *
     * This method creates embeddings and stores them in the
     * <code>localImports</code>
     *
     * additionaly it returns a list of imports found
     */
    private void processJavaInTagValues(TokenSequence<JspTokenId> tokenSequence) {
        tokenSequence.moveStart();

        while (tokenSequence.moveNext()) {
            PieceOfCode pieceOfCode = extractCodeFromTagAttribute(tokenSequence,
                    Arrays.asList("page", "tag"), //NOI18N
                    Arrays.asList("import"));     //NOI18N

            if (pieceOfCode != null){
                String importContent = pieceOfCode.getContent();
                int startOffset = 0;
                boolean moreToProcess = true;

                int endOffset;

                do {
                    // the JSP directive can take a comma separated list of imports
                    endOffset = importContent.indexOf(',', startOffset);

                    if (endOffset == -1) {
                        endOffset = importContent.length();
                        moreToProcess = false;
                    }

                    localImports.add(snapshot.create("import ", "text/x-java")); //NOI18N

                    localImports.add(snapshot.create(pieceOfCode.getStartOffset() + startOffset,
                            endOffset - startOffset,
                            "text/x-java")); //NOI18N

                    localImports.add(snapshot.create(";\n", "text/x-java")); //NOI18N

                    String singleImport = importContent.substring(startOffset, endOffset).trim();
                    localImportsFound.add(singleImport);
                    startOffset = endOffset + 1;

                } while (moreToProcess);
            } else {
                pieceOfCode = extractCodeFromTagAttribute(tokenSequence,
                    Arrays.asList("jsp:useBean"), //NOI18N
                    Arrays.asList("class", "type"));     //NOI18N


                if (pieceOfCode != null){
                    PieceOfCode id = extractCodeFromTagAttribute(tokenSequence,
                        Arrays.asList("jsp:useBean"), //NOI18N
                        Arrays.asList("id"));     //NOI18N

                    // id may be null in broken (incomplete) code
                    if (id != null){
                        beanDeclarations.add(snapshot.create(
                                pieceOfCode.getStartOffset(),
                                pieceOfCode.getLength(), "text/x-java")); //NOI18N

                        beanDeclarations.add(snapshot.create(" ", "text/x-java")); //NOI18N

                        beanDeclarations.add(snapshot.create(
                                id.getStartOffset(),
                                id.getLength(), "text/x-java")); //NOI18N

                        beanDeclarations.add(snapshot.create(";\n", "text/x-java")); //NOI18N

                        String beanId = id.getContent();
                        localBeansFound.add(beanId);
                    }
                } else {
                    pieceOfCode = extractCodeFromTagAttribute(tokenSequence,
                        Arrays.asList("attribute"), //NOI18N
                        Arrays.asList("type"));     //NOI18N


                    if (pieceOfCode != null){
                        PieceOfCode name = extractCodeFromTagAttribute(tokenSequence,
                            Arrays.asList("attribute"), //NOI18N
                            Arrays.asList("name"));     //NOI18N

                        // id may be null in broken (incomplete) code
                        if (name != null){
                            beanDeclarations.add(snapshot.create(
                                    pieceOfCode.getStartOffset(),
                                    pieceOfCode.getLength(), "text/x-java")); //NOI18N

                            beanDeclarations.add(snapshot.create(" ", "text/x-java")); //NOI18N

                            beanDeclarations.add(snapshot.create(
                                    name.getContent(), "text/x-java")); //NOI18N

                            beanDeclarations.add(snapshot.create(";\n", "text/x-java")); //NOI18N

                            localBeansFound.add(name.getContent());
                        }
                    } else {
                        pieceOfCode = extractCodeFromTagAttribute(tokenSequence,
                            Arrays.asList("page"), //NOI18N
                            Arrays.asList("extends"));

                        if (pieceOfCode != null){
                            pageExtends = snapshot.create(pieceOfCode.startOffset,
                                    pieceOfCode.length,
                                    "text/x-java"); //NOI18N
                        }
                    }
                }
            }
        }
    }

    private PieceOfCode extractCodeFromTagAttribute(TokenSequence tokenSequence, List<String> tagName, List<String> attrName) {
        PieceOfCode pieceOfCode = null;

        if (tokenSequence.token().id() == JspTokenId.TAG && tagName.contains(tokenSequence.token().text().toString().trim())) { //NOI18N

            int startPos = tokenSequence.offset();

            while (tokenSequence.moveNext() && consumeWS(tokenSequence)
                    && !(tokenSequence.token().id() == JspTokenId.SYMBOL
                    && TokenUtilities.equals(tokenSequence.token().text(), "%>"))) {

                if (tokenSequence.token().id() == JspTokenId.ATTRIBUTE && attrName.contains(tokenSequence.token().text().toString())) { //NOI18N

                    if (tokenSequence.moveNext() && consumeWS(tokenSequence) && tokenSequence.token().id() == JspTokenId.SYMBOL && TokenUtilities.equals("=", tokenSequence.token().text())) {

                        if (tokenSequence.moveNext() && consumeWS(tokenSequence) && tokenSequence.token().id() == JspTokenId.ATTR_VALUE) {

                            String val = tokenSequence.token().text().toString();

                            // extract the content of quoted string
                            if (val.length() > 2
                                    // attr values can be specified using double or single quotes
                                    && (val.charAt(0) == '"' || val.charAt(0) == '\'')
                                    && val.charAt(val.length() - 1) == val.charAt(0)) {

                                int startOffset = tokenSequence.offset() + 1;
                                int len = val.length() - 1;
                                String imprt = val.substring(1, len);
                                pieceOfCode = new PieceOfCode(imprt, startOffset, imprt.length());
                                break;
                            }
                        }
                    }
                }
            }

            tokenSequence.move(startPos);
            tokenSequence.moveNext();
        }

        return pieceOfCode;
    }



    private boolean isServletAPIOnClasspath() {
        ClassPath cp = ClassPath.getClassPath(fobj, ClassPath.COMPILE);

        return cp != null
                && (cp.findResource("javax/servlet/http/HttpServlet.class") != null //NOI18N
                || cp.findResource("jakarta/servlet/http/HttpServlet.class") != null); //NOI18N
    }

    @Override
    protected void processIncludedFile(IncludedJSPFileProcessor includedJSPFileProcessor) {
        implicitImports.add(snapshot.create(includedJSPFileProcessor.getImports(), "text/x-java"));
        declarations.add(snapshot.create(includedJSPFileProcessor.getDeclarations(), "text/x-java"));
        scriptlets.add(snapshot.create(includedJSPFileProcessor.getScriptlets(), "text/x-java"));
    }

    private void displayServletAPIMissingWarning() {
        if (fobj == null){
            return; //issue #160889
        }

        try {
            DataObject doJsp = DataObject.find(fobj);
            EditorCookie editor = doJsp.getCookie(EditorCookie.class);

            if (editor != null && editor.getOpenedPanes() != null) {

                JTextComponent component = editor.getOpenedPanes()[0];
                if (component != null) {
                    org.netbeans.editor.Utilities.setStatusBoldText(component,
                            NbBundle.getMessage(SimplifiedJspServlet.class, "MSG_MissingServletAPI"));
                }
            }
        } catch (DataObjectNotFoundException e) {
            // ignore
        }
    }

    @Override
    protected Collection<String> processedIncludes() {
        return Collections.emptyList();
    }

    public Embedding getSimplifiedServlet() {
        assureProcessCalled();

        if (!processingSuccessful){
            return null;
        }

        List<Embedding> content = new LinkedList<>();

        // debug code to find the root cause of #169924
        assert !implicitImports.contains(null) : "implicitImports contains null";
        assert !localImports.contains(null) : "localImports contains null";
        assert !declarations.contains(null) : "declarations contains null";
        assert !beanDeclarations.contains(null) : "beanDeclarations contains null";
        assert !scriptlets.contains(null) : "scriptlets contains null";

        content.addAll(implicitImports);
        content.addAll(localImports);
        content.addAll(header);
        content.addAll(declarations);
        content.addAll(beanDeclarations);
        content.add(snapshot.create(METHOD_HEADER, "text/x-java"));
        content.addAll(scriptlets);
        content.add(snapshot.create(CLASS_FOOTER, "text/x-java"));

        Embedding embedding = Embedding.create(content);

        if (logger.isLoggable(Level.FINEST)){
            String msg = "---\n" + embedding.getSnapshot().getText() + "\n---";
            logger.finest(msg);
        }

        return embedding;
    }

    private String getIncludedPath(TokenSequence<JspTokenId> tokenSequence) {
        String filePath = null;
        boolean afterFile = false;
        while (tokenSequence.moveNext()) {
            Token token = tokenSequence.token();
            if (token.id() == JspTokenId.SYMBOL && "%>".equals(token.text())) { //NOI18N
                break;
            } else if (token.id() == JspTokenId.ATTRIBUTE) {
                afterFile = "file".equals(token.text()); //NOI18N
            } else if (afterFile && token.id() == JspTokenId.ATTR_VALUE) {
                filePath = CharSequenceUtilities.toString(token.text());
                filePath = filePath.replaceAll("[\"' ]", ""); //NOI18N
                break;
            }
        }
        return filePath;
    }

    @Deprecated
    public abstract static class VirtualJavaClass {

        public final void create(Document doc, String virtualClassBody) {
            try {
                FileSystem memFS = FileUtil.createMemoryFileSystem();
                FileObject fileDummyJava = memFS.getRoot().createData("SimplifiedJSPServlet", "java"); //NOI18N
                try (PrintWriter writer = new PrintWriter(fileDummyJava.getOutputStream())) {
                    writer.print(virtualClassBody);
                }

                Source source = Source.create(fileDummyJava);
                process(fileDummyJava, source);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        protected abstract void process(FileObject fileObject, Source javaEmbedding);
    }

    private class PieceOfCode{
        private final String content;
        private final int startOffset;
        private final int length;

        public PieceOfCode(String content, int startOffset, int length) {
            this.content = content;
            this.startOffset = startOffset;
            this.length = length;
        }

        public String getContent() {
            return content;
        }

        public int getLength() {
            return length;
        }

        public int getStartOffset() {
            return startOffset;
        }
    }
}
