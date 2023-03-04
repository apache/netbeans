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

package org.netbeans.modules.web.core.syntax;


import java.util.Map;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.web.core.syntax.deprecated.Jsp11Syntax;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.jsp.lexer.JspParseData;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.web.core.syntax.deprecated.HtmlSyntax;
import org.netbeans.modules.web.core.api.JspColoringData;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.editor.BaseKit.InsertBreakAction;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.modules.csl.api.*;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 * Editor kit implementation for JSP content type
 *
 * @author Miloslav Metelka, Petr Jiricka, Yury Kamen
 * @author Marek.Fukala@Sun.COM
 * @version 1.5
 */
//@MimeRegistration(mimeType="text/x-jsp", service=EditorKit.class, position=1)
public class JspKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider{

    //hack for Bug 212105 - JspKit.createSyntax slow - LowPerformance took 9988 ms. 
    public static final ThreadLocal<Boolean> ATTACH_COLORING_LISTENER_TO_SYNTAX = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {
            return true;
        }
        
    };
    
    public static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N

    /** serialVersionUID */
    private static final long serialVersionUID = 8933974837050367142L;

    private final String mimeType;

    public JspKit() {
        this(JSP_MIME_TYPE);
    }

    // called from the XML layer
    private static JspKit createKitForJsp() {
        return new JspKit(JSP_MIME_TYPE);
    }

    // called from the XML layer
    private static JspKit createKitForTag() {
        return new JspKit(TAG_MIME_TYPE);
    }

    /** Default constructor */
    public JspKit(String mimeType) {
        super();
        this.mimeType = mimeType;
    }

    @Override
    public String getContentType() {
        return mimeType;
    }

    @Override
    public Object clone() {
        return new JspKit(mimeType);
    }

    /** Creates a new instance of the syntax coloring parser */
    @Override
    public Syntax createSyntax(Document doc) {
        final Jsp11Syntax newSyntax = new Jsp11Syntax(new HtmlSyntax(), new JavaSyntax(null, true));

        if(ATTACH_COLORING_LISTENER_TO_SYNTAX.get()) {
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;

            // tag library coloring data stuff
            JspColoringData data = JspUtils.getJSPColoringData(fobj);
            // construct the listener
            PropertyChangeListener pList = new ColoringListener(doc, data, newSyntax);
            // attach the listener
            // PENDING - listen on the language
            //jspdo.addPropertyChangeListener(WeakListeners.propertyChange(pList, jspdo));
            if (data != null) {
                data.addPropertyChangeListener(WeakListeners.propertyChange(pList, data));
            }
        }
        return newSyntax;
    }

    @Override
    public Document createDefaultDocument() {
        final Document doc = super.createDefaultDocument();
        //#174763 workaround - there isn't any elegant place where to place
        //a code which needs to be run after document's COMPLETE initialization.
        //DataEditorSupport.createStyledDocument() creates the document via the
        //EditorKit.createDefaultDocument(), but some of the important properties
        //like Document.StreamDescriptionProperty or mimetype are set as the
        //document properties later.
        //A hacky solution is that a Runnable can be set to the postInitRunnable property
        //in the EditorKit.createDefaultDocument() and the runnable is run
        //once the document is completely initialized.
        //The code responsible for running the runnable is in BaseJspEditorSupport.createStyledDocument()
        doc.putProperty("postInitRunnable", new Runnable() { //NOI18N
            public void run() {
                initLexerColoringListener(doc);
            }
        });
        return doc;
    }

    @Override
    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
            new JspDefaultKeyTypedAction(),
            new SelectCodeElementAction(SelectCodeElementAction.selectNextElementAction, true),
            new SelectCodeElementAction(SelectCodeElementAction.selectPreviousElementAction, false),
            new InstantRenameAction(),
            new ToggleCommentAction("//"), // NOI18N
            new ExtKit.CommentAction(""), //NOI18N
            new ExtKit.UncommentAction("") //NOI18N
        };

        return TextAction.augmentList(super.createActions(), javaActions);
    }

    private static class ColoringListener implements PropertyChangeListener {
        private Document doc;
        private Object parsedDataRef; // NOPMD: hold a reference to the data we are listening on
        // so it does not get garbage collected
        private Jsp11Syntax syntax;
        //private JspDataObject jspdo;

        public ColoringListener(Document doc, JspColoringData data, Jsp11Syntax syntax) {
            this.doc = doc;
            // we must keep the reference to the structure we are listening on so it's not gc'ed
            this.parsedDataRef = data;
            this.syntax = syntax;
            // syntax must keep a reference to this object so it's not gc'ed
            syntax.listenerReference = this;
            syntax.data = data;
            /* jspdo = (JspDataObject)NbEditorUtilities.getDataObject(doc);*/
        }

        private void recolor() {
            if (doc instanceof BaseDocument)
                ((BaseDocument)doc).invalidateSyntaxMarks();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (syntax == null)
                return;
            if (syntax.listenerReference != this) {
                syntax = null; // should help garbage collection
                return;
            }
            if (JspColoringData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NbEditorDocument nbdoc = (NbEditorDocument)doc;
                        nbdoc.extWriteLock();
                        try {
                            recolor();
                        } finally {
                            nbdoc.extWriteUnlock();
                        }
                    }
                });
            }
        }
    }

    public static class ToggleCommentAction extends ExtKit.ToggleCommentAction {

        static final long serialVersionUID = -1L;

        public ToggleCommentAction(String lineCommentString) {
            super("//");
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            try {
                boolean toggled = false;
                BaseDocument doc = (BaseDocument) target.getDocument();

                // inside scriptlet
                int startPos = org.netbeans.editor.Utilities.getRowStart(doc, target.getSelectionStart());
                TokenHierarchy th = TokenHierarchy.create(target.getText(), JspTokenId.language());
                List<TokenSequence> ets = th.embeddedTokenSequences(startPos, false);
                if (!ets.isEmpty()
                        && ets.get(ets.size() - 1).languagePath().mimePath().endsWith("text/x-java")) { //NOI18N
                    super.actionPerformed(evt, target);
                    toggled = true;
                }

                // inside one line scriptlet
                if (!toggled) {
                    List<TokenSequence> ets2 = th.embeddedTokenSequences(target.getCaretPosition(), false);
                    if (!ets.isEmpty()
                            && ets.get(ets.size() - 1).languagePath().mimePath().endsWith("text/html") //NOI18N
                            && !ets2.isEmpty()
                            && ets2.get(ets2.size() - 1).languagePath().mimePath().endsWith("text/x-java")) { //NOI18N
                        commentUncomment(th, evt, target);
                    }
                }

                // try common comment
                if (!toggled) {
                    (new ToggleBlockCommentAction()).actionPerformed(evt, target);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void commentUncomment(TokenHierarchy th, ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled() || !(target.getDocument() instanceof BaseDocument)) {
                target.getToolkit().beep();
                return;
            }
            int caretOffset = org.netbeans.editor.Utilities.isSelectionShowing(target) ? target.getSelectionStart() : target.getCaretPosition();
            final BaseDocument doc = (BaseDocument) target.getDocument();
            TokenSequence<JspTokenId> ts = th.tokenSequence();
            if (ts != null) {
                ts.move(caretOffset);
                ts.moveNext();
                boolean newLine = false;
                if (isNewLineBeforeCaretOffset(ts, caretOffset)) {
                    newLine = true;
                }
                while (!newLine && ts.movePrevious() && ts.token().id() != JspTokenId.SYMBOL2) {
                    if(isNewLineBeforeCaretOffset(ts, caretOffset)) {
                        newLine = true;
                    }
                }
                if (!newLine && ts.token().id() == JspTokenId.SYMBOL2) {
                    final int changeOffset = ts.offset() + ts.token().length();
                    ts.moveNext();
                    // application.getAttribute("  ") %>
                    String scriptlet = ts.token().text().toString();
                    final boolean lineComment = scriptlet.matches("^(\\s)*//.*"); //NOI18N
                    final int length = lineComment ? scriptlet.indexOf("//") + 2 : 0; //NOI18N
                    doc.runAtomic(new Runnable() {

                        public @Override
                        void run() {
                            try {
                                if (!lineComment) {
                                    doc.insertString(changeOffset, " //", null);
                                } else {
                                    doc.remove(changeOffset, length);
                                }

                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
            }
        }
    }

    private static boolean isNewLineBeforeCaretOffset(final TokenSequence<JspTokenId> ts, final int caretOffset) {
        boolean result = false;
        int indexOfNewLine = ts.token().text().toString().indexOf("\n"); //NOI18N
        if (indexOfNewLine != -1) {
            int absoluteIndexOfNewLine = ts.offset() + indexOfNewLine;
            result = caretOffset > absoluteIndexOfNewLine;
        }
        return result;
    }

    private static class LexerColoringListener implements PropertyChangeListener {

        private Document doc;
        private JspColoringData data;
        private JspParseData jspParseData;

        private LexerColoringListener(Document doc, JspColoringData data, JspParseData jspParseData) {
            this.doc = doc;
            this.data = data; //hold ref to JspColoringData so LCL is not GC'ed
            this.jspParseData = jspParseData;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (JspColoringData.PROP_PARSING_SUCCESSFUL.equals(evt.getPropertyName())) {
                if(!jspParseData.initialized()) {
                    SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NbEditorDocument nbdoc = (NbEditorDocument)doc;
                        nbdoc.extWriteLock();
                        try {
                            recolor();
                        } finally {
                            nbdoc.extWriteUnlock();
                        }
                    }
                });
                }
            } else if (JspColoringData.PROP_COLORING_CHANGE.equals(evt.getPropertyName())) {
                //THC.rebuild() must run under document write lock. Since it is not guaranteed that the
                //event from the JspColoringData is not fired under document read lock, synchronous call
                //to write lock could deadlock. So the rebuild is better called asynchronously.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NbEditorDocument nbdoc = (NbEditorDocument)doc;
                        nbdoc.extWriteLock();
                        try {
                            recolor();
                        } finally {
                            nbdoc.extWriteUnlock();
                        }
                    }
                });
            }
        }
        private void recolor() {
            jspParseData.updateParseData((Map<String,String>)data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax());

            MutableTextInput mti = (MutableTextInput)doc.getProperty(MutableTextInput.class);
            if(mti != null) {
                mti.tokenHierarchyControl().rebuild();
            }
        }

    }

    private void initLexerColoringListener(Document doc) {
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        FileObject fobj = (dobj != null) ? dobj.getPrimaryFile() : null;
        JspColoringData data = JspUtils.getJSPColoringData(fobj);

        if(data == null) {
            return ;
        }

        JspParseData jspParseData = new JspParseData((Map<String,String>)data.getPrefixMapper(), data.isELIgnored(), data.isXMLSyntax(), data.isInitialized());
        PropertyChangeListener lexerColoringListener = new LexerColoringListener(doc, data, jspParseData);

        data.addPropertyChangeListener(WeakListeners.propertyChange(lexerColoringListener, data));
        //reference LCL from document to prevent LCL to be GC'ed
        doc.putProperty(LexerColoringListener.class, lexerColoringListener);

        //add an instance of InputAttributes to the document property,
        //lexer will use it to read coloring information
        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(JspTokenId.language(), JspParseData.class, jspParseData, false);
        doc.putProperty(InputAttributes.class, inputAttributes);
    }

    // <RAVE> #62993
    // Implement HelpCtx.Provider to provide help for CloneableEditor
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(JspKit.class);
    }

    public static class JspDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        /** called under document atomic lock */
        @Override
        protected void insertString(BaseDocument doc, int dotPos,
                Caret caret, String str,
                boolean overwrite) throws BadLocationException {
            // see issue #211036 - inserted string can be empty since #204450
            if (str.isEmpty()) {
                return;
            }

            // handle EL expression brackets completion - must be done here before HtmlKeystrokeHandler
            if (handledELBracketsCompletion(doc, dotPos, caret, str, overwrite)) {
                return;
            }

            super.insertString(doc, dotPos, caret, str, overwrite);
        }

        private boolean handledELBracketsCompletion(BaseDocument doc, int dotPos, Caret caret, String str, boolean overwrite) throws BadLocationException {
            // EL expression completion - #234702
            if (dotPos > 0) {
                String charPrefix = doc.getText(dotPos - 1, 1);
                if ("{".equals(str) && ("#".equals(charPrefix) || "$".equals(charPrefix))) { //NOI18N
                    super.insertString(doc, dotPos, caret, "{}", overwrite);                 //NOI18N
                    caret.setDot(dotPos + 1);

                    //open completion
                    Completion.get().showCompletion();

                    return true;
                }
            }
            return false;
        }

    }

}

