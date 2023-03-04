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

package org.netbeans.editor;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.awt.Color;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.Style;
import javax.swing.text.Element;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
/**
* Extension to the guarded document that implements
* StyledDocument interface
*
* @author Miloslav Metelka
* @version 1.00
*/

public class GuardedDocument extends BaseDocument
    implements StyledDocument {

    /** Guarded attribute used for specifying that the inserted block
    * will be guarded.
    */
    public static final String GUARDED_ATTRIBUTE = "guarded"; // NOI18N

    /** AttributeSet with only guarded attribute */
    public static final SimpleAttributeSet guardedSet = new SimpleAttributeSet();

    /** AttributeSet with only break-guarded attribute */
    public static final SimpleAttributeSet unguardedSet = new SimpleAttributeSet();

    private static final boolean debugAtomic = Boolean.getBoolean("netbeans.debug.editor.atomic"); // NOI18N
    private static final boolean debugAtomicStack = Boolean.getBoolean("netbeans.debug.editor.atomic.stack"); // NOI18N

    // -J-Dorg.netbeans.editor.GuardedDocument.level=FINEST
    private static final Logger LOG = Logger.getLogger(GuardedDocument.class.getName());

    // Add the attributes to sets
    static {
        guardedSet.addAttribute(GUARDED_ATTRIBUTE, Boolean.TRUE);
        unguardedSet.addAttribute(GUARDED_ATTRIBUTE, Boolean.FALSE);
    }

    public static final String FMT_GUARDED_INSERT_LOCALE = "FMT_guarded_insert"; // NOI18N
    public static final String FMT_GUARDED_REMOVE_LOCALE = "FMT_guarded_remove"; // NOI18N

    MarkBlockChain guardedBlockChain;

    /** Break the guarded flag, so inserts/removals over guarded areas will work */
    boolean breakGuarded;

    boolean atomicAsUser;

    /** Style context to hold the styles */
    protected StyleContext styles;

    /** Name of the normal style. The normal style is used to reset the effect
    * of all styles applied to the line.
    */
    protected String normalStyleName;

    /**
     * Create a new guarded document.
     * 
     * @param kitClass The implementation class of the editor kit that
     *   should be used for this document.
     * 
     * @deprecated The use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
     */
    @Deprecated
    public GuardedDocument(Class kitClass) {
        this(kitClass, true, new StyleContext());
    }

    /**
     * Create a new guarded document.
     * 
     * @param mimeType The mime type for this document.
     * 
     * @since 1.26
     */
    public GuardedDocument(String mimeType) {
        this(mimeType, true, new StyleContext());
    }
    
    /** 
     * Creates base document with specified style context.
     * 
    * @param kitClass class used to initialize this document with proper settings
    *   category based on the editor kit for which this document is created
     * @param addToRegistry XXX
    * @param styles style context to use
     * 
     * @deprecated The use of editor kit's implementation classes is deprecated
     *   in favor of mime types.
    */
    @Deprecated
    public GuardedDocument(Class kitClass, boolean addToRegistry, StyleContext styles) {
        super(kitClass, addToRegistry);
        init(styles);
    }
    
    /**
     * Creates base document with specified style context.
     * 
     * @param mimeType The mime type for this document.
     * @param addToRegistry XXX
     * @param styles style context to use
     * 
     * @since 1.26
     */
    public GuardedDocument(String mimeType, boolean addToRegistry, StyleContext styles) {
        super(addToRegistry, mimeType);
        init(styles);
    }
    
    private void init(StyleContext styles) {
        this.styles = styles;
        guardedBlockChain = new MarkBlockChain(this) {
            protected @Override Mark createBlockStartMark() {
                MarkFactory.ContextMark startMark = new MarkFactory.ContextMark(Position.Bias.Forward, false);
                return startMark;
            }

            protected @Override Mark createBlockEndMark() {
                MarkFactory.ContextMark endMark = new MarkFactory.ContextMark(Position.Bias.Backward, false);
                return endMark;
            }
        };
    }
    
    /** Get the chain of the guarded blocks */
    public MarkBlockChain getGuardedBlockChain() {
        return guardedBlockChain;
    }

    public boolean isPosGuarded(int offset) {
        Object o = getProperty(EDITABLE_PROP);
        if (o != null && !(Boolean)o) {
            return true;
        }
        if (!modifiable) { // if whole doc is readonly due to CES modificationListener veto
            return true;
        }
        int rel = guardedBlockChain.compareBlock(offset, offset) & MarkBlock.IGNORE_EMPTY;
        // Return not guarded when inside line
        return (rel == MarkBlock.INNER || (rel == MarkBlock.INSIDE_BEGIN && // and at line begining
                (offset == 0 || org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(this).
                charAt(offset - 1) == '\n')));
    }

    /** This method is called automatically before the document
    * is updated as result of removal. This function can throw
    * BadLocationException or its descendants to stop the ongoing
    * insert from being actually done.
    * @param evt document event containing the change including array
    *  of characters that will be inserted
    */
    protected @Override void preInsertCheck(int offset, String text, AttributeSet a)
    throws BadLocationException {
        super.preInsertCheck(offset, text, a);

        int rel = guardedBlockChain.compareBlock(offset, offset) & MarkBlock.IGNORE_EMPTY;

        if (debugAtomic) {
            System.err.println("GuardedDocument.beforeInsertUpdate() atomicAsUser=" // NOI18N
                               + atomicAsUser + ", breakGuarded=" + breakGuarded // NOI18N
                               + ", inserting text='" + EditorDebug.debugString(text) // NOI18N
                               + "' at offset=" + Utilities.debugPosition(this, offset)); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        boolean guarded = (rel & MarkBlock.OVERLAP) != 0
                && rel != MarkBlock.INSIDE_END // guarded blocks have insertAfter endMark
                && !(text.charAt(text.length() - 1) == '\n' && rel == MarkBlock.INSIDE_BEGIN);
        if (guarded) {
            if (!breakGuarded || atomicAsUser) {
                CharSequence docText = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(this);
                // Allow mod when inserting "inside" line and right at the begining of the guarded block
                boolean insertAtLineBegin = (offset == 0 || docText.charAt(offset - 1) == '\n');
                guarded = (rel != MarkBlock.INSIDE_BEGIN) || insertAtLineBegin;
                if (guarded) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("GuardedDocument.preInsertCheck(): offset:" + Utilities.debugPosition(this, offset) +
                                ", relation: " + rel + "; guardedBlockChain:\n" + guardedBlockChain + "\n");
                    }

                    throw new GuardedException(
                        MessageFormat.format(
                            NbBundle.getBundle(BaseKit.class).getString(FMT_GUARDED_INSERT_LOCALE),
                            new Object [] {
                                Integer.valueOf(offset)
                            }
                        ),
                        offset
                    );
                }
            }
        }
    }

    /** This method is called automatically before the document
    * is updated as result of removal.
    */
    protected @Override void preRemoveCheck(int offset, int len)
    throws BadLocationException {
        int rel = guardedBlockChain.compareBlock(offset, offset + len);

        if (debugAtomic) {
            System.err.println("GuardedDocument.beforeRemoveUpdate() atomicAsUser=" // NOI18N
                               + atomicAsUser + ", breakGuarded=" + breakGuarded // NOI18N
                               + ", removing text='" + EditorDebug.debugChars(getChars(offset, len)) // NOI18N
                               + "'at offset=" + Utilities.debugPosition(this, offset)); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        // Check if removed block overlaps any guarded block(s)
        // Also check that if an area right before GB gets removed (including ending newline)
        // that the area does not start in the middle of the line (GB would then start in the middle of line
        // after the removal).
        // For GBs that already start in the middle of a line this does not apply
        boolean guarded = ((rel & MarkBlock.OVERLAP) != 0);
        if (!guarded && (rel == MarkBlock.CONTINUE_BEGIN)) { // GB starts right after removed area
            CharSequence docText = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(this);
            int gbStartOffset = offset + len;
            // To allow removal either GB starts in a middle of line
            // or (if starts at line begining) check that a character in front of first removed char is newline
            boolean gbStartsAtLineBegin = (gbStartOffset == 0 || docText.charAt(gbStartOffset - 1) == '\n');
            guarded = gbStartsAtLineBegin && (offset != 0 && docText.charAt(offset - 1) != '\n');
        }
        if (guarded) {
            if (!breakGuarded || atomicAsUser) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("GuardedDocument.preRemoveCheck(): offset:" + Utilities.debugPosition(this, offset) +
                            ", relation: " + rel + "; guardedBlockChain:\n" + guardedBlockChain + "\n");
                }

                // test whether the previous char before removed text is '\n'
                throw new GuardedException(
                    MessageFormat.format(
                        NbBundle.getBundle(BaseKit.class).getString(FMT_GUARDED_REMOVE_LOCALE),
                        new Object [] {
                            Integer.valueOf(offset)
                        }
                    ),
                    offset
                );
            }
        }
    }

    public @Override void runAtomic(Runnable r) {
        if (debugAtomic) {
            System.out.println("GuardedDocument.runAtomic() called"); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        atomicLockImpl ();
        boolean origBreakGuarded = breakGuarded;
        try {
            breakGuarded = true;
            r.run();
        // Only attempt to recover (undo the document modifications) from runtime exceptions.
        // Do not attempt to recover from java.lang.Error or other Throwable subclasses.
        } catch (RuntimeException ex) {
            boolean completed = false;
            try {
                breakAtomicLock();
                completed = true;
            } finally {
                if (completed) {
                    throw ex;
                } else {
                    // Log thrown exception in case breakAtomicLock() throws an exception by itself.
                    LOG.log(Level.INFO, "Runtime exception thrown in GuardedDocument.runAtomic() leading to breakAtomicLock():", ex);
                }
            }
        } finally {
            breakGuarded = origBreakGuarded;
            atomicUnlockImpl ();
            if (debugAtomic) {
                System.out.println("GuardedDocument.runAtomic() finished"); // NOI18N
            }
        }
    }

    public @Override void runAtomicAsUser(Runnable r) {
        if (debugAtomic) {
            System.out.println("GuardedDocument.runAtomicAsUser() called"); // NOI18N
            if (debugAtomicStack) {
                Thread.dumpStack();
            }
        }

        atomicLockImpl ();
        boolean origAtomicAsUser = atomicAsUser;
        try {
            atomicAsUser = true;
            r.run();
        // Only attempt to recover (undo the document modifications) from runtime exceptions.
        // Do not attempt to recover from java.lang.Error or other Throwable subclasses.
        } catch (RuntimeException ex) {
            boolean completed = false;
            try {
                breakAtomicLock();
                completed = true;
            } finally {
                if (completed) {
                    throw ex;
                } else {
                    // Log thrown exception in case breakAtomicLock() throws an exception by itself.
                    LOG.log(Level.INFO, "Runtime exception thrown in GuardedDocument.runAtomicAsUser() leading to breakAtomicLock():", ex);
                }
            }
        } finally {
            atomicAsUser = origAtomicAsUser;
            atomicUnlockImpl ();
            if (debugAtomic) {
                System.out.println("GuardedDocument.runAtomicAsUser() finished"); // NOI18N
            }
        }
    }

    protected @Override BaseDocumentEvent createDocumentEvent(int offset, int length,
            DocumentEvent.EventType type) {
        return new GuardedDocumentEvent(this, offset, length, type);
    }

    /** Set the name for normal style. Normal style is used to reset the effect
    * of all aplied styles.
    */
    public void setNormalStyleName(String normalStyleName) {
        this.normalStyleName = normalStyleName;
    }

    /** Fetches the list of style names */
    public Enumeration getStyleNames() {
        return styles.getStyleNames();
    }

    // ------------------------------------------------------------------------
    // StyleDocument implementation
    // ------------------------------------------------------------------------

    /** Adds style to the document */
    public Style addStyle(String styleName, Style parent) {
        Style style =  styles.addStyle(styleName, parent);
        return style;
    }

    /** Removes style from document */
    public void removeStyle(String styleName) {
        styles.removeStyle(styleName);
    }

    /** Fetches style previously added */
    public Style getStyle(String styleName) {
        return styles.getStyle(styleName);
    }

    public void setCharacterAttributes(int offset, int length, AttributeSet attribs, boolean replace) {
        if (((Boolean)attribs.getAttribute(GUARDED_ATTRIBUTE)).booleanValue() == true) {
            guardedBlockChain.addBlock(offset, offset + length, false); // no concat
            fireChangedUpdate(getDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE, attribs));
        }
        if (((Boolean)attribs.getAttribute(GUARDED_ATTRIBUTE)).booleanValue() == false) {
            guardedBlockChain.removeBlock(offset, offset + length);
            fireChangedUpdate(getDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE, attribs));
        }
    }

    /** Change attributes for part of the text.  */
    public void setParagraphAttributes(int offset, int length, AttributeSet s,
                                       boolean replace) {
        // !!! implement
    }

    /**
     * Sets the logical style to use for the paragraph at the
     * given position.  If attributes aren't explicitly set
     * for character and paragraph attributes they will resolve
     * through the logical style assigned to the paragraph, which
     * in turn may resolve through some hierarchy completely
     * independent of the element hierarchy in the document.
     *
     * @param pos the starting position >= 0
     * @param s the style to set
     */
    public void setLogicalStyle(int pos, Style s) {
    }

    /** Get logical style for position in paragraph */
    public Style getLogicalStyle(int pos) {
        return null;
    }

    /**
     * Gets the element that represents the character that
     * is at the given offset within the document.
     *
     * @param pos the offset >= 0
     * @return the element
     */
    public Element getCharacterElement(int pos) {
        return getParagraphElement(pos);
    }


    /**
     * Takes a set of attributes and turn it into a foreground color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    public Color getForeground(AttributeSet attr) {
        return null; // !!!
    }

    /**
     * Takes a set of attributes and turn it into a background color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    public Color getBackground(AttributeSet attr) {
        return null; // !!!
    }

    /**
     * Takes a set of attributes and turn it into a font
     * specification.  This can be used to turn things like
     * family, style, size, etc into a font that is available
     * on the system the document is currently being used on.
     *
     * @param attr the set of attributes
     * @return the font
     */
    public Font getFont(AttributeSet attr) {
        return new Font("Default",Font.BOLD,12); // NOI18N
    }

    public @Override String toStringDetail() {
        return super.toStringDetail()
               + ",\nGUARDED blocks:\n" + guardedBlockChain; // NOI18N
    }

}
