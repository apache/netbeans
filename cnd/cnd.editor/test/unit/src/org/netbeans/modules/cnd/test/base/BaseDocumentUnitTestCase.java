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
package org.netbeans.modules.cnd.test.base;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.TestCase;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 * Vladimir Voskresensky copied this class to prevent dependency on editor tests
 *
 * Support for creation of unit tests working with document.
 *
 */
public abstract class BaseDocumentUnitTestCase extends CndBaseTestCase {
    
    private EditorKit editorKit;
    
    private String loadDocumentText;
    
    private BaseDocument doc;
    
    private Caret caret;
    
    private int loadCaretOffset = -1;

    protected final static class Context {
        private JEditorPane pane;

        public Context(final EditorKit kit, final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        pane = new JEditorPane();
                        pane.setEditorKit(kit);
                        Document doc = pane.getDocument();
                        // do we need language for default key typed?
//                        doc.putProperty(Language.class, CppTokenId.language());
//                        doc.putProperty("mimeType", mimeType);
                        int caretOffset = textWithPipe.indexOf('|');
                        String text;
                        if (caretOffset != -1) {
                            text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
                        } else {
                            text = textWithPipe;
                        }
                        pane.setText(text);
                        pane.setCaretPosition((caretOffset != -1) ? caretOffset : doc.getLength());
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public JEditorPane pane() {
            return pane;
        }

        public Document document() {
            return pane.getDocument();
        }

        private void typeChar(final char ch) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        final KeyEvent keyEvent;
                        switch (ch) {
                            case '\n':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Enter
                                break;
                            case '\b':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of BackSpace
                                break;
                            case '\f':
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Delete
                                break;
                            default:
                                keyEvent = new KeyEvent(pane, KeyEvent.KEY_TYPED,
                                        EventQueue.getMostRecentEventTime(),
                                        0, KeyEvent.VK_UNDEFINED, ch);
                        }
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                SwingUtilities.processKeyBindings(keyEvent);
                            }
                        };
                        if (document() instanceof BaseDocument) {
                            ((BaseDocument)document()).runAtomic(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public void typeText(final String text) {
            for (int i = 0; i < text.length(); i++) {
                typeChar(text.charAt(i));
            }
        }

        public void assertDocumentTextEquals(final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        int caretOffset = textWithPipe.indexOf('|');
                        String text;
                        if (caretOffset != -1) {
                            text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
                        } else {
                            text = textWithPipe;
                        }
                        try {
                            // Use debug text to prefix special chars for easier readability
                            text = CharSequenceUtilities.debugText(text);
                            String docText = document().getText(0, document().getLength());
                            docText = CharSequenceUtilities.debugText(docText);
                            if (!text.equals(docText)) {
                                int diffIndex = 0;
                                int minLen = Math.min(docText.length(), text.length());
                                while (diffIndex < minLen) {
                                    if (text.charAt(diffIndex) != docText.charAt(diffIndex)) {
                                        break;
                                    }
                                    diffIndex++;
                                }
                                TestCase.fail("Invalid document text - diff at index " + diffIndex
                                        + "\nExpected: \"" + text
                                        + "\"\n  Actual: \"" + docText + "\"");
                            }
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e);
                        }
                        if (caretOffset != -1) {
                            TestCase.assertEquals("Invalid caret offset", caretOffset, pane.getCaretPosition());
                        }
                    }
                });
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public BaseDocumentUnitTestCase(String testMethodName) {
        super(testMethodName);
        
    }
    
    /**
     * Set the text that the document obtained by {@link #getDocument()}
     * would be loaded with.
     *
     * <p>
     * The text is parsed and the first occurrence of "|" sets
     * the caret offset which is available by {@link #getLoadCaretOffset()}.
     * <br>
     * The "|" itself is removed from the document text and subsequent
     * calls to {@link #getLoadDocumentText()} do not contain it.
     */
    protected void setLoadDocumentText(String loadDocumentText) {
        // [TODO] a more elaborated support could be done e.g. "||" to a real "|" etc.
        loadCaretOffset = loadDocumentText.indexOf('|');
        if (loadCaretOffset != -1) {
            loadDocumentText = loadDocumentText.substring(0, loadCaretOffset)
                + loadDocumentText.substring(loadCaretOffset + 1);
        }
        
        this.loadDocumentText = loadDocumentText;
    }
    
    /**
     * Get the text that the document obtained by {@link #getDocument()}
     * would be loaded with.
     *
     * @return text to be loaded into the document or null if nothing
     *  should be loaded.
     */
    protected final String getLoadDocumentText() {
        return loadDocumentText;
    }

    /**
     * Return caret offset based on the scanning of the text passed
     * to {@link #setLoadDocumentText(String)}.
     *
     * @return valid caret offset or -1 if no caret offset was determined
     *  in the document text.
     */
    protected final int getLoadCaretOffset() {
        return loadCaretOffset;
    }
    
    /**
     * Return the caret instance that can be used for testing.
     * <br>
     * The caret listens on document changes and its initial
     * position is set to {@link #getLoadCaretOffset()}.
     *
     * @return caret instance.
     */
    private synchronized Caret getCaret () {
        if (caret == null) {
            caret = new CaretImpl(getDocument(), loadCaretOffset);
        }
        return caret;
    }
    
    /**
     * Get the offset of where the caret resides in the document.
     */
    protected final int getCaretOffset() {
        return getCaret().getDot();
    }
    
    /**
     * Get the document that the test should work with.
     * <br>
     * If the document does not exist yet it will be created
     * and loaded with the text from {@link #getLoadDocumentText()}.
     */
    protected synchronized BaseDocument getDocument() {
        if (doc == null) {
            doc = createAndLoadDocument();
        }
        return doc;
    }
    
    /**
     * Return text of the whole document.
     * <br>
     * The document is retrieved by {@link #getDocument()}.
     */
    protected String getDocumentText() {
        try {
            Document d = getDocument();
            return d.getText(0, d.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace(getLog());
            fail(ex.getMessage());
            return null; // should never be reached
        }
    }
    
    /**
     * Assert whether the document available through {@link #getDocument()}
     * has a content equal to <code>expectedText</code>.
     */
    protected void assertDocumentText(String msg, String expectedText) {
        String docText = getDocumentText();
        if (!docText.equals(expectedText)) {
            //CndUtils.threadsDump();
            StringBuffer sb = new StringBuffer();
            sb.append(msg);
            sb.append("\n----- expected text: -----\n");
            appendDebugText(sb, expectedText);
            sb.append("\n----- document text: -----\n");
            appendDebugText(sb, docText);
            sb.append("\n-----\n");
            int startLine = 1;
            for (int i = 0; i < docText.length() && i < expectedText.length(); i++){
                if (expectedText.charAt(i) == '\n') {
                    startLine++;
                }
                if (expectedText.charAt(i) != docText.charAt(i)){
                    sb.append("Diff starts in line ").append(startLine).append("\n");
                    String context = expectedText.substring(i);
                    if (context.length()>40){
                        context = context.substring(0, 40);
                    }
                    sb.append("Expected:").append(context).append("\n");
                    context = docText.substring(i);
                    if (context.length()>40){
                        context = context.substring(0, 40);
                    }
                    sb.append("   Found:").append(context).append("\n");
                    break;
                }
            }
            System.err.println(sb.toString());
            fail(sb.toString());
        }
    }
    
    protected final void appendDebugChar(StringBuffer sb, char ch) {
        switch (ch) {
            case '\n':
                sb.append("\\n\n");
                break;
            case '\t':
                sb.append("\\t");
                break;

            default:
                sb.append(ch);
                break;
        }
    }
    
    protected final void appendDebugText(StringBuffer sb, String text) {
        for (int i = 0; i < text.length(); i++) {
            appendDebugChar(sb, text.charAt(i));
        }
    }
    
    protected final String debugText(String text) {
        StringBuffer sb = new StringBuffer();
        appendDebugText(sb, text);
        return sb.toString();
    }

    /**
     * Assert whether the document available through {@link #getDocument()}
     * has a content equal to <code>expectedText</code> and whether the caret
     * position {@link #getCaretOffset()}
     * indicated by "|" in the passed text is at the right place.
     */
    protected void assertDocumentTextAndCaret(String msg, String expectedTextAndCaretPipe) {
        // [TODO] a more elaborated support could be done e.g. "||" to a real "|" etc.
        int expectedCaretOffset = expectedTextAndCaretPipe.indexOf('|');
        if (expectedCaretOffset == -1) { // caret position not indicated
            fail("Caret position not indicated in '" + expectedTextAndCaretPipe + "'");
        }

        String expectedDocumentText= expectedTextAndCaretPipe.substring(0, expectedCaretOffset)
            + expectedTextAndCaretPipe.substring(expectedCaretOffset + 1);

        assertDocumentText(msg, expectedDocumentText);
        if (expectedCaretOffset != getCaretOffset()) {
            fail("caretOffset=" + getCaretOffset()
                + " but expectedCaretOffset=" + expectedCaretOffset
                + " in '" + expectedTextAndCaretPipe + "'"
            );
        }
    }

    /**
     * Create editor kit instance to be returned
     * by {@link #getEditorKit()}.
     * <br>
     * The returned editor kit should return
     * <code>BaseDocument</code> instances
     * from its {@link javax.swing.text.EditorKit.createDefaultDocument()}.
     */
    abstract protected EditorKit createEditorKit();
    
    /**
     * Get the kit that should be used
     * when creating the <code>BaseDocument</code>
     * instance.
     * <br>
     * The editor kit instance is created in {@link #createEditorKit()}.
     *
     * @return editor kit instance.
     */
    public final EditorKit getEditorKit() {
        if (editorKit == null) {
            editorKit = createEditorKit();
        }
        return editorKit;
    }
    
    private BaseDocument createAndLoadDocument() {
        BaseDocument bd = (BaseDocument)getEditorKit().createDefaultDocument();

        if (loadDocumentText != null) {
            try {
                bd.insertString(0, loadDocumentText, null);
            } catch (BadLocationException e) {
                e.printStackTrace(getLog());
                fail();
            }
        }
        return bd;
    }

    private static final class CaretImpl implements Caret, DocumentListener {
        
        private final Document doc;
        
        private int dot;
        
        private int mark;
        
        private boolean visible = true;
        
        private boolean selectionVisible;
        
        private int blinkRate = 300;
        
        private final EventListenerList listenerList = new EventListenerList();
        
        private ChangeEvent changeEvent;
        
        CaretImpl(Document doc, int dot) {
            this.doc = doc;
            doc.addDocumentListener(this);
            setDot(dot);
        }

        @Override
        public void deinstall (javax.swing.text.JTextComponent c) {
            fail("Not yet implemented");
        }
        
        @Override
        public void install (javax.swing.text.JTextComponent c) {
            fail("Not yet implemented");
        }
        
        @Override
        public java.awt.Point getMagicCaretPosition () {
            fail("Not yet implemented");
            return null;
        }
        
        @Override
        public void setMagicCaretPosition (java.awt.Point p) {
            fail("Not yet implemented");
        }
        
        @Override
        public int getDot () {
            return dot;
        }
        
        @Override
        public int getMark () {
            return mark;
        }
        
        @Override
       public void setDot (int dot) {
            this.mark = this.dot;
            changeCaretPosition(dot);
        }
        
        @Override
        public void moveDot (int dot) {
            changeCaretPosition(dot);
        }
        
        @Override
        public int getBlinkRate () {
            return blinkRate;
        }
        
        @Override
        public void setBlinkRate (int rate) {
            this.blinkRate = rate;
        }
        
        @Override
        public boolean isVisible () {
            return visible;
        }
        
        @Override
        public void setVisible (boolean v) {
            this.visible = v;
        }
        
        @Override
        public boolean isSelectionVisible () {
            return selectionVisible;
        }
        
        @Override
        public void setSelectionVisible (boolean v) {
            this.selectionVisible = v;
        }
        
        @Override
        public void addChangeListener (ChangeListener l) {
            listenerList.add(ChangeListener.class, l);
        }
        
        @Override
        public void removeChangeListener (ChangeListener l) {
            listenerList.remove(ChangeListener.class, l);
        }
        
        public void fireChangeListener() {
            // Lazily create the event
            if (changeEvent == null) {
                changeEvent = new ChangeEvent(this);
            }

            Object[] listeners = listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
            }
        }
        
        @Override
        public void paint (java.awt.Graphics g) {
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            int offset = e.getOffset();
            int length = e.getLength();
            int newDot = dot;
            short changed = 0;
            if (newDot >= offset) {
                newDot += length;
                changed |= 1;
            }
            int newMark = mark;
            if (newMark >= offset) {
                newMark += length;
                changed |= 2;
            }

            if (changed != 0) {
                if (newMark == newDot) {
                    setDot(newDot);
                    ensureValidPosition();
                } else {
                    setDot(newMark);
                    if (getDot() == newMark) {
                        moveDot(newDot);
                    }
                    ensureValidPosition();
                }

            }
        }
        
        @Override
        public void removeUpdate(DocumentEvent e) {
            int offs0 = e.getOffset();
            int offs1 = offs0 + e.getLength();
            int newDot = dot;
            if (newDot >= offs1) {
                newDot -= (offs1 - offs0);
            } else if (newDot >= offs0) {
                newDot = offs0;
            }
            int newMark = mark;
            if (newMark >= offs1) {
                newMark -= (offs1 - offs0);
            } else if (newMark >= offs0) {
                newMark = offs0;
            }
            if (newMark == newDot) {
                setDot(newDot);
                ensureValidPosition();
            } else {
                setDot(newMark);
                if (getDot() == newMark) {
                    moveDot(newDot);
                }
                ensureValidPosition();
            }
        }
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            
        }

        private void changeCaretPosition(int dot) {
            if (this.dot != dot) {
                this.dot = dot;
                fireChangeListener();
            }
        }
        
       private void ensureValidPosition() {
            int length = doc.getLength();
            if (dot > length || mark > length) {
                setDot(length);
            }
        }

    }
    
}
