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

package org.netbeans.modules.editor.indent;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;

/**
 * Indentation and code reformatting services for a swing text document.
 *
 * @author Miloslav Metelka
 */
public final class IndentImpl {
    
    // -J-Dorg.netbeans.modules.editor.indent.IndentImpl=FINE
    private static final Logger LOG = Logger.getLogger(IndentImpl.class.getName());
    
    public static IndentImpl get(Document doc) {
        IndentImpl indentImpl = (IndentImpl)doc.getProperty(IndentImpl.class);
        if (indentImpl == null) {
            indentImpl = new IndentImpl(doc);
            doc.putProperty(IndentImpl.class, indentImpl);
        }
// XXX: formatting infra cleanup
//        indentImpl.refresh();
        return indentImpl;
    }
    
    private final Document doc;
    
    private final Preferences prefs;
    
    private Indent indent;
    
    private Reformat reformat;
    
    private TaskHandler indentHandler;
    
    private TaskHandler reformatHandler;

// XXX: formatting infra cleanup
//    private Formatter defaultFormatter;
    
    private Thread indentLockThread;
    private int indentLockExtraDepth;
    private final Object indentLock = new Object();
    private Thread reformatLockThread;
    private int reformatLockExtraDepth;
    private final Object reformatLock = new Object();
    
    public IndentImpl(Document doc) {
        this.doc = doc;
        String mimeType = (String)doc.getProperty("mimeType"); //NOI18N
        this.prefs = mimeType != null ? MimeLookup.getLookup(mimeType).lookup(Preferences.class) : null;
    }
    
    public Document document() {
        return doc;
    }
    
    public Indent getIndent() {
        return indent;
    }
    
    public void setIndent(Indent indent) {
        this.indent = indent;
    }

    public Reformat getReformat() {
        return reformat;
    }
    
    public void setReformat(Reformat reformat) {
        this.reformat = reformat;
    }
    
// XXX: formatting infra cleanup
//    void setDefaultFormatter(Formatter defaultFormatter) {
//        this.defaultFormatter = defaultFormatter;
//    }
//
//    void refresh() {
//        if (defaultFormatter == null) {
//            if (doc instanceof BaseDocument) {
//                defaultFormatter = ((BaseDocument)doc).getLegacyFormatter();
//            }
//        }
//    }
    
    public void indentLock() {
        synchronized(indentLock) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("indentLock() on " + this);
            }
            Thread currentThread = Thread.currentThread();
            while (indentLockThread != null) {
                if (currentThread == indentLockThread) {
                    indentLockExtraDepth++; // Extra inner lock
                    return;
                }
                try {
                    indentLock.wait();
                } catch (InterruptedException e) {
                    throw new Error("Interrupted at acquiring indent-lock");
                }
            }
            indentLockThread = currentThread;
            indentHandler = new TaskHandler(true, doc);
            try {
                if (indentHandler.collectTasks()) {
                    indentHandler.lock();
                }
            } catch (Exception e) {
                indentUnlock();
                throw e;
            }
        }
    }
    
    public void indentUnlock() {
        synchronized(indentLock) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("indentUnlock() on " + this);
            }
            Thread currentThread = Thread.currentThread();
            if (currentThread != indentLockThread) {
                throw new IllegalStateException("Invalid indentUnlock(): current-thread=" + // NOI18N
                        currentThread + ", lockThread=" + indentLockThread + ", lockExtraDepth=" + indentLockExtraDepth); // NOI18N
            }
            if (indentLockExtraDepth == 0) {
                indentHandler.unlock();
                indentHandler = null;
                indentLockThread = null;
                indentLock.notifyAll();
            } else {
                indentLockExtraDepth--;
            }
        }
    }
    
    public TaskHandler indentHandler() {
        return indentHandler;
    }
    
    public void reformatLock() {
        synchronized(reformatLock) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("reformatLock() on " + this);
            }
            Thread currentThread = Thread.currentThread();
            while (reformatLockThread != null) {
                if (currentThread == reformatLockThread) {
                    reformatLockExtraDepth++; // Extra inner lock
                    return;
                }
                try {
                    reformatLock.wait();
                } catch (InterruptedException e) {
                    throw new Error("Interrupted at acquiring reformat-lock");
                }
            }
            reformatLockThread = currentThread;
            reformatHandler = new TaskHandler(false, doc);
            try {
                if (reformatHandler.collectTasks()) {
                    reformatHandler.lock();
                }
            } catch (Exception e) {
                reformatUnlock();
                throw e;
            }
        }
    }
    
    public void reformatUnlock() {
        synchronized(reformatLock) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("reformatUnlock() on " + this);
            }
            Thread currentThread = Thread.currentThread();
            if (currentThread != reformatLockThread) {
                throw new IllegalStateException("Invalid reformatUnlock(): current-thread=" + // NOI18N
                        currentThread + ", lockThread=" + reformatLockThread + ", lockExtraDepth=" + reformatLockExtraDepth); // NOI18N
            }
            if (reformatLockExtraDepth == 0) {
                reformatHandler.unlock();
                reformatHandler = null;
                reformatLockThread = null;
                reformatLock.notifyAll();
            } else {
                reformatLockExtraDepth--;
            }
        }
    }
    
    public TaskHandler reformatHandler() {
        return reformatHandler;
    }

    public int reindent(int startOffset, int endOffset, int caretOffset, boolean indentNewLine) throws BadLocationException {
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " > endOffset=" + endOffset); // NOI18N
        }
        boolean runUnlocked = false;
        if (indentHandler == null) {
            LOG.log(Level.SEVERE, null, new Exception("Not locked. Use Indent.lock().")); // NOI18N
            runUnlocked = true;
            // Attempt to call the tasks unlocked since now it's too late to lock (doc's lock already taken).
            indentHandler = new TaskHandler(true, doc);
        }
        try {
            if (runUnlocked) {
                indentHandler.collectTasks();
            }
            indentHandler.setCaretOffset(caretOffset);
            // Find begining of line
            Element lineRootElem = lineRootElement(doc);
            // Correct the start offset to point to the begining of the start line
// XXX: formatting infra cleanup
//            boolean done = false;
            if (indentHandler.hasItems()) {
                // When indenting newline first insert a plain newline
                if (indentNewLine) {
                    doc.insertString(startOffset, "\n", null);
                    // Adjust start and end offsets after the inserted newline
                    startOffset++;
                    endOffset++;
                    // Fix for Enter on first line - if it would have offset 0 the position would stay
                    // at the begining of the document
                    if (indentHandler.caretOffset() == 0) {
                        indentHandler.setCaretOffset(1);
                    }
                }

                if (prefs == null || prefs.getBoolean(SimpleValueNames.ENABLE_INDENTATION, true)) {
                    int startLineIndex = lineRootElem.getElementIndex(startOffset);
                    Element lineElem = lineRootElem.getElement(startLineIndex);
                    int startLineOffset = lineElem.getStartOffset();
                    // Find ending line element - by default use the same as for start offset
                    if (endOffset > lineElem.getEndOffset()) { // need to get a different line element
                        int endLineIndex = lineRootElem.getElementIndex(endOffset);
                        lineElem = lineRootElem.getElement(endLineIndex);
                        // Check if the given endOffset ends right after line's newline (in fact at the begining of the next line)
                        if (endLineIndex > 0 && lineElem.getStartOffset() == endOffset) {
                            endLineIndex--;
                            lineElem = lineRootElem.getElement(endLineIndex);
                        }
                    }

                    // Create context from begining of the start line till the end of the end line.
                    indentHandler.setGlobalBounds(
                            doc.createPosition(startLineOffset),
                            doc.createPosition(lineElem.getEndOffset() - 1));

                    // Perform whole reindent on top and possibly embedded levels
                    indentHandler.runTasks();
                }
// XXX: formatting infra cleanup
//                done = true;
            }

// XXX: formatting infra cleanup
//            // Fallback to Formatter
//            if (!done && doc instanceof BaseDocument && defaultFormatter != null) {
//                if (indentNewLine) {
//                    if (LOG.isLoggable(Level.FINE)) {
//                        LOG.fine("Defaulting reindent() to indentNewLine() in legacy formatter " + // NOI18N
//                                defaultFormatter + '\n');
//                    }
//                    // Fallback to indentNewLine() will insert '\n'
//                    int newCaretOffset = defaultFormatter.indentNewLine(doc, caretOffset);
//                    indentHandler.setCaretOffset(newCaretOffset);
//                } else { // Indent line
//                    // Original formatter does not have reindentation of multiple lines
//                    // so reformat start line and continue for each line.
//                    Position endPos = doc.createPosition(endOffset);
//                    if (LOG.isLoggable(Level.FINE)) {
//                        LOG.fine("Defaulting reindent() to indentLine() in legacy formatter " + // NOI18N
//                                defaultFormatter + '\n');
//                    }
//                    do {
//                        startOffset = defaultFormatter.indentLine(doc, startOffset);
//                        int startLineIndex = lineRootElem.getElementIndex(startOffset) + 1;
//                        if (startLineIndex >= lineRootElem.getElementCount())
//                            break;
//                        Element lineElem = lineRootElem.getElement(startLineIndex);
//                        startOffset = lineElem.getStartOffset(); // Move to next line
//                    } while (startOffset < endPos.getOffset());
//                }
//            }

            return indentHandler.caretOffset();
        } finally {
            if (runUnlocked)
                indentHandler = null;
        }
    }

    public void reformat(int startOffset, int endOffset, int caretOffset) throws BadLocationException {
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " > endOffset=" + endOffset); // NOI18N
        }
        boolean runUnlocked = false;
        if (reformatHandler == null) { // 
            LOG.log(Level.SEVERE, "Not locked. Use Reformat.lock().", new Exception()); // NOI18N
            // Attempt to call the tasks unlocked since now it's too late to lock (doc's lock already taken).
            runUnlocked = true;
            reformatHandler = new TaskHandler(false, doc);
        }
        try {
            if (runUnlocked) {
                reformatHandler.collectTasks();
            }
            reformatHandler.setCaretOffset(caretOffset);
// XXX: formatting infra cleanup
//            boolean done = false;
            if (reformatHandler.hasItems()) {
                reformatHandler.setGlobalBounds(
                        doc.createPosition(startOffset),
                        doc.createPosition(endOffset));

                // Run top and embedded reformatting
                reformatHandler.runTasks();

                // Perform reformatting of the top section and possible embedded sections
// XXX: formatting infra cleanup
//                done = true;
            }

// XXX: formatting infra cleanup
//            // Fallback to Formatter
//            if (!done && doc instanceof BaseDocument && defaultFormatter != null) {
//                if (LOG.isLoggable(Level.FINE)) {
//                    LOG.fine("Defaulting reformat() to reformat() in legacy formatter " + // NOI18N
//                            defaultFormatter + '\n');
//                }
//                BaseDocument bdoc = (BaseDocument)doc;
//                defaultFormatter.reformat(bdoc, startOffset, endOffset);
//            }
        } finally {
            if (runUnlocked)
                reformatHandler = null;
        }
    }
    
    public static Element lineRootElement(Document doc) {
        return (doc instanceof StyledDocument)
            ? ((StyledDocument)doc).getParagraphElement(0).getParentElement()
            : doc.getDefaultRootElement();
    }

    public static void checkOffsetInDocument(Document doc, int offset) throws BadLocationException {
        if (offset < 0)
            throw new BadLocationException("offset=" + offset + " < 0", offset); // NOI18N
        if (offset > doc.getLength())
            throw new BadLocationException("offset=" + offset + " > doc.getLength()=" + doc.getLength(), offset);
    }

}
