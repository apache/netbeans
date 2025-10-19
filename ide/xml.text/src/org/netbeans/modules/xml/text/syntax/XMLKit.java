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
package org.netbeans.modules.xml.text.syntax;

import java.awt.event.ActionEvent;
import java.awt.Panel;
import java.util.*;

// prevent ambiguous reference to Utilities
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.openide.awt.StatusDisplayer;

// we depend on NetBeans editor stuff
import org.netbeans.editor.*;
import org.netbeans.modules.csl.api.ToggleBlockCommentAction;
import org.netbeans.modules.editor.*;

import org.netbeans.modules.xml.text.XmlCommentHandler;
import org.netbeans.modules.xml.text.completion.NodeSelector;

import static org.netbeans.modules.xml.text.syntax.DTDKit.MIME_TYPE;

import org.netbeans.modules.xml.text.syntax.bridge.LegacySyntaxBridge;


/**
 * NetBeans editor kit implementation for xml content type.
 * <p>
 * It provides syntax coloring, code completion, actions, abbrevirations, ...
 *
 * @author Libor Kramolis
 * @author Petr Kuzel
 * @author Sandeep
 */
public class XMLKit extends NbEditorKit implements org.openide.util.HelpCtx.Provider {

    /** Serial Version UID */
    private static final long serialVersionUID =5326735092324267367L;
    
    /** Default XML Mime Type. */
    public static final String MIME_TYPE = "text/xml"; // NOI18N

    // comment action name
    public static final String xmlCommentAction = "xml-comment";
    
    // uncomment action name
    public static final String xmlUncommentAction = "xml-uncomment";

    // dump XML sysntax
    public static final String xmlTestAction = "xml-dump";
    
    // hack to be settings browseable //??? more info needed
    public static Map settings;
    
    //temporary - will be removed when lexer is stabilized
    private static final boolean J2EE_LEXER_COLORING = Boolean.getBoolean("j2ee_lexer_coloring"); //NOI18N
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(XMLKit.class);
    }
    
    /** Create new instance of syntax coloring parser */
    @Override
    public Syntax createSyntax(Document doc) {
        Syntax syn = null;
        LegacySyntaxBridge bridge = MimeLookup.getLookup(MIME_TYPE).lookup(LegacySyntaxBridge.class);
        if (bridge != null) {
            syn = bridge.createSyntax(this, doc, MIME_TYPE);
        }
        return syn != null ? syn : super.createSyntax(doc);
    }

    @Override
    public Document createDefaultDocument() {
        if(J2EE_LEXER_COLORING) {
            Document doc = new XMLEditorDocument(getContentType());
            doc.putProperty(Language.class, XMLTokenId.language());
            return doc;
        } else {
            return super.createDefaultDocument();
        }
    }

    /** Create syntax support */
    @Override
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        SyntaxSupport syn = null;
        LegacySyntaxBridge bridge = MimeLookup.getLookup(MIME_TYPE).lookup(LegacySyntaxBridge.class);
        if (bridge != null) {
            syn = bridge.createSyntaxSupport(this, doc, MIME_TYPE);
        }
        return syn != null ? syn : super.createSyntaxSupport(doc);
    }
    
    @Override
    public void install(JEditorPane c) {
        super.install(c);
        if (Boolean.getBoolean("netbeans.experimental.xml.nodeselectors")) {  // NOI18N
            new NodeSelector(c);
        }
    }

    // hack to be settings browseable //??? more info needed    
    public static void setMap(Map map) {
        settings = map;
    }

    // hack to be settings browseable //??? more info needed        
    public Map getMap() {
        return settings;
    }

    //??? +xml handling
    public @Override String getContentType() {
        return MIME_TYPE;
    }

    /**
     * Provide XML related actions.
     */
    protected @Override Action[] createActions() {
        Action[] actions = new Action[] {
            new XMLCommentAction(),
            new XMLUncommentAction(),
            new ToggleBlockCommentAction(new XmlCommentHandler()),
            new TestAction(),
        };
        return TextAction.augmentList(super.createActions(), actions);
    }
    
    
    public abstract static class XMLEditorAction extends BaseAction {
        
        public XMLEditorAction (String id) {
            super(id);
            String desc = org.openide.util.NbBundle.getMessage(XMLKit.class,id); // NOI18N
            if (desc != null) {
                putValue(SHORT_DESCRIPTION, desc);
            }
        }
        
        /**
         * Uniform way of reporting problem while action executing #15589
         */
        protected void problem(String reason) {
            if (reason != null) StatusDisplayer.getDefault().setStatusText("Cannot proceed: " + reason);
            new Panel().getToolkit().beep();
        }
    }
    
    /**
     * Comment out editor selection.
     */
    public static class XMLCommentAction extends XMLEditorAction {
        
        private static final long serialVersionUID =4004056745446061L;

        private static final String commentStartString = "<!--";  //NOI18N
        private static final String commentEndString = "-->";  //NOI18N
        
        public XMLCommentAction() {
            super( xmlCommentAction);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null) return;
            if (!target.isEditable() || !target.isEnabled()) {
                problem(null);
                return;
            }
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            try {
                if (caret.isSelectionVisible()) {
                    int startPos = LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart());
                    int endPos = target.getSelectionEnd();
                    doc.atomicLock();
                    try {

                        if (endPos > 0 && LineDocumentUtils.getLineStartOffset(doc, endPos) == endPos) {
                            endPos--;
                        }

                        int pos = startPos;
                        int lineCnt = Utilities.getRowCount(doc, startPos, endPos);                            

                        for (;lineCnt > 0; lineCnt--) {
                            doc.insertString(pos, commentStartString, null); 
                            doc.insertString(LineDocumentUtils.getLineEndOffset(doc,pos), commentEndString, null);
                            pos = Utilities.getRowStart(doc, pos, +1);
                        }

                    } finally {
                        doc.atomicUnlock();
                    }
                } else { // selection not visible
                    doc.insertString(LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart()),
                        commentStartString, null);
                    doc.insertString(LineDocumentUtils.getLineEndOffset(doc, target.getSelectionStart()),
                        commentEndString, null);
                }
            } catch (BadLocationException e) {
                problem(null);
            }
        }
        
    }

    /**
     * Uncomment selected text
     *
     */
    public static class XMLUncommentAction extends XMLEditorAction {
        private static final String commentStartString = "<!--";  //NOI18N
        private static final String commentEndString = "-->";  //NOI18N
        private static final char[] commentStart = {'<','!','-','-'};
        private static final char[] commentEnd = {'-','-','>'};
        
        static final long serialVersionUID = 40040567454546061L;
        
        public XMLUncommentAction() {
            super( xmlUncommentAction);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null) return;
            if (!target.isEditable() || !target.isEnabled()) {
                problem(null);
                return;
            }
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            try {
                if (caret.isSelectionVisible()) {
                    int startPos = LineDocumentUtils.getLineStartOffset(doc, target.getSelectionStart());
                    int endPos = target.getSelectionEnd();
                    doc.atomicLock();
                    try {

                        if (endPos > 0 && LineDocumentUtils.getLineStartOffset(doc, endPos) == endPos) {
                            endPos--;
                        }

                        int pos = startPos;
                        int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                        char[] startChars, endChars;

                        for (; lineCnt > 0; lineCnt-- ) {
                            startChars = doc.getChars(pos, 4 );
                            endChars = doc.getChars(LineDocumentUtils.getLineEndOffset(doc,pos)-3, 3 );

                            if(startChars[0] == commentStart[0] && startChars[1] == commentStart[1] &&
                                startChars[2] == commentStart[2] && startChars[3] == commentStart[3] &&
                                endChars[0] == commentEnd[0] && endChars[1] == commentEnd[1] && endChars[2] == commentEnd[2] ){

                                doc.remove(pos,4);
                                doc.remove(LineDocumentUtils.getLineEndOffset(doc,pos)-3,3);
                            }                                
                            pos = Utilities.getRowStart(doc, pos, +1);
                        }

                    } finally {
                        doc.atomicUnlock();
                    }
                } else { // selection not visible
                  char[] startChars = doc.getChars(target.getSelectionStart(), 4 );
                  char[] endChars = doc.getChars(LineDocumentUtils.getLineEndOffset(doc,target.getSelectionStart())-3, 3 );
                  if(startChars[0] == commentStart[0] && startChars[1] == commentStart[1] &&
                                startChars[2] == commentStart[2] && startChars[3] == commentStart[3] &&
                                endChars[0] == commentEnd[0] && endChars[1] == commentEnd[1] && endChars[2] == commentEnd[2] ){
                        doc.remove(target.getSelectionStart(),4);
                        doc.remove(LineDocumentUtils.getLineEndOffset(doc,target.getSelectionStart())-3,3);
                    }
                }
            } catch (BadLocationException e) {
                problem(null);
            }
        }
    }    

    
    /**
     * Dump it.
     */
    public static class TestAction extends XMLEditorAction {
        
        private static final long serialVersionUID =4004056745446099L;

        public TestAction() {
            super( xmlTestAction);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null) return;
            if (!target.isEditable() || !target.isEnabled()) {
                problem(null);
                return;
            }
            Caret caret = target.getCaret();
            BaseDocument doc = (BaseDocument)target.getDocument();
            try {
                doc.dump(System.out);    
                if (target == null)  throw new BadLocationException(null,0);  // folish compiler
            } catch (BadLocationException e) {
                problem(null);
            }
        }
        
    }
    
    public class XMLEditorDocument extends NbEditorDocument {
        public XMLEditorDocument(Class kitClass) {
            super(kitClass);
        }

        public XMLEditorDocument(String mimeType) {
            super(mimeType);
        }
    }
}
