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

package org.netbeans.spi.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Parameters;

/**
 * An interceptor which is called when a line break is typed into a document. You should
 * implement this interface if you want to hook in the keyboard input
 * processing done in the editor infrastructure that would normally result in inserting a line break
 * into a document. This is in fact a specialized version of {@link TypedTextInterceptor} interface,
 * which works the same way, but handles inserting a line break rather than regular characters.
 * The {@link TypedTextInterceptor} implementations are never called to handle the line break insertion.
 *
 * <p><b>Registration</b>: <code>TypedBreakInterceptor</code>s can be plugged in the editor infrastructure
 * by implementing the {@link Factory} interface and registering it in <code>MimeLookup</code>
 * under the appropriate mimetype (ie <code>MimePath</code>).
 *
 * <p>The instances created from the <code>Factory</code> will be reused for processing
 * all keyboard input received by all documents of the same mime type, which the interceptor
 * instances were registered for (including documents that contain embedded sections
 * of that mime type). As described in the general concepts of Typing Hooks SPI
 * the interceptors are guaranteed to be called in AWT thread only, which means that
 * they should not need any internal synchronization model.
 *
 * <p><b>Processing rules</b>: If there are multiple instances of <code>TypedBreakInterceptor</code> registered
 * for the same mime type the infrastructure will queue them up in their registration
 * order and when processing an event it will call them all until the processing is done
 * or terminated. 
 *
 * <p>The interceptor has several methods that are called at different stages of
 * the key typed event processing. When processing an event the infrastructure will call
 * the methods in the order as they are listed below. Moreover, if there are multiple
 * interceptors queued up for processing an event each method is first called on
 * all the queued interceptors before moving on to the next stage and calling next
 * method.
 *
 * <ul>
 * <li>{@link #beforeInsert(Context)} - It's called before a line break is inserted
 *   into a document. No document lock is held when this method is called. The method
 *   can't modify the text that will be inserted (and it's not supposed to do any tricks to
 *   break this rule). An interceptor can stop further processing of the event by returning
 *   <code>true</code> from this method. If it does so, no other interceptors'
 *   <code>beforeInsert</code> method will be called and the processing will be terminated
 *   without inserting any text.
 *
 * <li>{@link #insert(MutableContext)} - This method is called during the text
 *   insertion stage immediately before the text is inserted into a document. At this
 *   time the document is already write locked, but the interceptors are not expected
 *   to modify its content directly. Instead they can change the text that will be
 *   inserted by calling {@link MutableContext#setText(java.lang.String, int, int, int...)} method.
 *   The text insertion is strictly controlled by the infrastructure and has to obey some
 *   additional rules (eg. correctly replacing selected text, handling insert vs override
 *   modes of the caret, etc). The first interceptor that modifies the insertion text
 *   will win and no other interceptor's <code>insert</code> method will be called.
 *   <br>
 *   The interceptors are allowed to insert more than just a line break, but the text
 *   they insert has to contain at least one line break. They can also request the
 *   inserted text to be reindented. Please see {@link MutableContext#setText(java.lang.String, int, int, int...)}
 *   for details. 
 *
 * <li>{@link #afterInsert(Context)} - This is the last method in the processing
 *   chain and it will be called when the text is already inserted in the document.
 *   Similarly as in <code>beforeInsert</code> the document is not locked when
 *   this method is called.
 * 
 * <li>{@link #cancelled(Context)} - This is an additional method that will be called
 *   when the processing is terminated in the before-insertion stage (ie. by an interceptor
 *   returning <code>true</code> from its <code>beforeInsert</code> method).
 *   The infrastructure will only call this method on interceptors that have already
 *   had their <code>beforeInsert</code> method called, but not on those that
 *   have not yet been called at all.
 * </ul>
 *
 * <p><b>Errors recovery</b>: If an exception is thrown from any of the methods
 * when calling an interceptor the infrastructure will catch it and log it,
 * but it will not stop further processing. The infrastructure may blacklist the offending
 * interceptor and exclude it from processing future events.
 *
 * @author Vita Stejskal
 * @since 1.31
 */
public interface TypedBreakInterceptor {

    /**
     * This method is called before any text is inserted into a document. The context object
     * passed to the method provides access to the editor pane and the edited document,
     * but does not allow to modify the text that will be inserted.
     *
     * <p>This method can be used for stopping further processing of the current
     * key typed event. If this method returns <code>true</code> the processing will
     * be terminated and {@link #cancelled(Context)} will be called for all the intercetors
     * that have already had their <code>beforeInsert</code> method called (including
     * the one that terminated the processing). The rest of the interceptors waiting
     * in the queue will not be called at all.
     *
     * <p><b>Locking</b>: When this method is called the document is not locked
     * by the infrastructure.
     * 
     * @param context The context object providing information necessary for processing
     *   the event.
     *
     * @return If <code>true</code> the further processing will be stopped. Normally
     *   the method should return <code>false</code>.
     * @throws BadLocationException Since the document is not locked prior calling this
     *   method the processing may fail when working with stale context data.
     */
    boolean beforeInsert(Context context) throws BadLocationException;
    
    /**
     * This method is called immediately before a line break is inserted into a document.
     * Implementors can use special <code>MutableContext</code> to modify the text
     * that will be inserted into a document. The first interceptor that sets
     * the insertion text will win and the method will not be called on the rest
     * of the queued interceptors. The interceptors are not supposed to modify the
     * document directly.
     *
     * <p><b>Locking</b>: When this method is called the infrastructure has already
     * write locked the document.
     *
     * @param context The context object providing information necessary for processing
     *   the event and allowing to modify the insertion text.
     *
     * @throws BadLocationException If the processing fails.
     */
    void insert(MutableContext context) throws BadLocationException;
    
    /**
     * This method is called after the text is inserted into a document and its editor's
     * caret is adjusted.
     *
     * <p><b>Locking</b>: When this method is called the document is not locked
     * by the infrastructure.
     *
     * @param context The context object providing information necessary for processing
     *   the event. The {@code Context.getText()} method will return text that was
     *   inserted into the document at the end of the text-insertion stage.
     * 
     * @throws BadLocationException Since the document is not locked prior calling this
     *   method the processing may fail when working with stale context data.
     */
    void afterInsert(Context context) throws BadLocationException;
    
    /**
     * This method is called when the normal processing is terminated by some
     * interceptor's <code>beforeInsert</code> method. Please note that this
     * method will not be called if the <code>beforeInsert</code> method was not
     * called.
     * 
     * @param context The context object used for calling the <code>beforeInsert</code>
     *   method.
     */
    void cancelled(Context context);
    
    /**
     * The context class providing information about the edited document, its
     * editor pane, caret offset, line break insertion offset and text.
     */
    public static class Context {
        
        /**
         * Gets the editor component where the currently processed key typed event
         * occurred.
         *
         * @return The editor pane that contains the edited <code>Document</code>.
         */
        public JTextComponent getComponent() {
            return component;
        }

        /**
         * Gets the edited document. It's the document where the line break is going to
         * be inserted.
         *
         * @return The edited document.
         */
        public Document getDocument() {
            return document;
        }

        /**
         * Gets the caret offset. This is the offset in the document where
         * the caret is at the time when a user performed an action resulting in
         * the insertion of a line break (ie. where the currently processed <code>KeyEvent</code>
         * happened). This may or may not be the same offset, where the line break
         * will be inserted.
         *
         * @return The offset in the edited document.
         */
        public int getCaretOffset() {
            return caretOffset;
        }
        
        /**
         * Gets the line break insertion offset. This is the offset in the document where
         * the line break will be inserted.
         *
         * @return The offset in the edited document.
         */
        public int getBreakInsertOffset() {
            return breakInsertOffset;
        }
        
        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------
        
        private final JTextComponent component;
        private final Document document;
        private final int caretOffset;
        private final int breakInsertOffset;
        
        private Context(JTextComponent component, int caretOffset, int breakInsertOffset) {
            this.component = component;
            this.document = component.getDocument();
            this.caretOffset = caretOffset;
            this.breakInsertOffset = breakInsertOffset;
        }
        
    } // End of Context class
    
    /**
     * This context class allows to modify the insertion text and the caret position
     * after the text is inserted into a document. Apart from that it provides exactly the same
     * information as its superclass <code>Context</code>.
     */
    public static final class MutableContext extends Context {
        
        /**
         * Sets the insertion text and adjusted caret position. This method can
         * be used for inserting additional text that accompanies the line break typed
         * in by a user.
         *
         * <p>There is no restriction on the new text
         * set by this method, except that it must not be <code>null</code> and must contain at least
         * one line break. It can be of any length and can even span multiple lines.
         *
         * <p>It is important to remember that the values of the position parameters are
         * relative to the new text. Therefore valid values for the <code>caretPosition</code>
         * parameter, for example, are <code>&lt;0, text.getLength()&gt;</code>! The position parameters
         * are <b>not</b> document offsets.
         * 
         * <p>The following rules have to be obeyed otherwise an <code>IllegalArgumentException</code>
         * will be thrown:
         * 
         * <ul>
         * <li>The <code>text</code> has to contain at least one line break '\n' character.
         * <li>The <code>breakInsertPosition</code>, if specified, has to point to the most
         *   important line break character in the <code>text</code> (eg. an interceptor can actually insert
         *   several lines, but one of them is always considered the most significant and its
         *   line break character position is where <code>breakInsertPosition</code> should point at.
         * <li>The <code>caretPosition</code>, if specified, has to point somewhere within the <code>text</code>.
         * <li>The <code>reindentBlocks</code>, if specified, are pairs of positions within the
         *   <code>text</code>. In each pair the first number denotes the starting position of a
         *   region that will be reindented. The ending position of that region is denoted by the
         *   second number in the pair. Therfore the first number has to be lower or equal to the
         *   second number.
         * </ul>
         * 
         * @param text The new text that will be inserted to a document. It must contain at least
         *   one line break '\n' character.
         * @param breakInsertPosition The position within the <code>text</code> where the most significant
         *   line break character is. If <code>-1</code>, the position of the first line break character
         *   in the <code>text</code> will be used.
         * @param caretPosition The position within the <code>text</code> where the caret will be placed
         *   after the text is inserted in the document. If <code>-1</code>, the <code>breakInsertPosition + 1</code>
         *   will be used.
         * @param reindentBlocks The list of position pairs that determine areas within the <code>text</code>
         *   that will be reindented after the <code>text</code> is inserted in the document. Can be <code>null</code>
         *   or zero length array in which case only the line containing the <code>caretPosition</code> will
         *   be reindented. If specified, it must contain an even number of elements.
         * 
         * @throws IllegalArgumentException If the parameters passed in violate the rules specified above.
         */
        public void setText(String text, int breakInsertPosition, int caretPosition, int... reindentBlocks) {
            Parameters.notNull("text", text); //NOI18N
            
            if (text.indexOf('\n') == -1) {
                throw new IllegalArgumentException("The text must contain a new line (\\n) character."); //NOI18N
            }
            
            if (breakInsertPosition != -1) {
                if (breakInsertPosition < 0 || breakInsertPosition >= text.length()) {
                    throw new IllegalArgumentException("The breakInsertPosition=" + breakInsertPosition + " must point in the text=<0, " + text.length() + ")."); //NOI18N
                }
                if (text.charAt(breakInsertPosition) != '\n') {
                    throw new IllegalArgumentException("The character at breakInsertPosition=" + breakInsertPosition + " must be the new line (\\n) character."); //NOI18N
                }
            }
            
            if (caretPosition != -1) {
                if (caretPosition < 0 || caretPosition > text.length()) {
                    throw new IllegalArgumentException("The caretPosition=" + caretPosition + " must point in the text=<0, " + text.length() + ">."); //NOI18N
                }
            }

            if (reindentBlocks != null && reindentBlocks.length > 0) {
                if (reindentBlocks.length % 2 != 0) {
                    throw new IllegalArgumentException("The reindentBlocks must contain even number of positions within the text: " + reindentBlocks.length); //NOI18N
                }
                for(int i = 0; i < reindentBlocks.length / 2; i++) {
                    int s = reindentBlocks[2 * i];
                    if (s < 0 || s > text.length()) {
                        throw new IllegalArgumentException("The reindentBlocks[" + (2 * i) + "]=" + s + " must point in the text=<0, " + text.length() + ")."); //NOI18N
                    }
                    int e = reindentBlocks[2 * i + 1];
                    if (e < 0 || e > text.length()) {
                        throw new IllegalArgumentException("The reindentBlocks[" + (2 * i + 1) + "]=" + e + " must point in the text=<0, " + text.length() + ")."); //NOI18N
                    }
                    if (s > e) {
                        throw new IllegalArgumentException("The reindentBlocks[" + (2 * i) + "]=" + s + " must be smaller than reindentBlocks[" + (2 * i + 1) + "]=" + e); //NOI18N
                    }
                }
                
            }
            
            this.insertionText = text;
            this.breakInsertPosition = breakInsertPosition;
            this.caretPosition = caretPosition;
            this.reindentBlocks = reindentBlocks;
        }
        
        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------
        
        private String insertionText = null;
        private int breakInsertPosition = -1;
        private int caretPosition = -1;
        private int [] reindentBlocks = null;
        
        /* package */ MutableContext(JTextComponent component, int caretOffset, int insertBreakOffset) {
            super(component, caretOffset, insertBreakOffset);
        }
        
        /* package */ Object [] getData() {
            return insertionText != null ?
                new Object [] { insertionText, breakInsertPosition, caretPosition, reindentBlocks } :
                null;
        }
        
        /* package */ void resetData() {
            insertionText = null;
            breakInsertPosition = -1;
            caretPosition = -1;
            reindentBlocks = null;
        }
    } // End of MutableContext class

    /**
     * The factory interface for registering <code>TypedBreakInterceptor</code>s
     * in <code>MimeLookup</code>. An example registration in an XML layer shown
     * below registers <code>Factory</code> implementation under <code>text/x-something</code>
     * mime type in <code>MimeLookup</code>.
     *
     * <pre>
     * &lt;folder name="Editors"&gt;
     *  &lt;folder name="text"&gt;
     *   &lt;folder name="x-something"&gt;
     *    &lt;file name="org-some-module-TBIFactory.instance" /&gt;
     *   &lt;/folder&gt;
     *  &lt;/folder&gt;
     * &lt;/folder&gt;
     * </pre>
     */
    public interface Factory {

        /**
         * Creates a new interceptor for the given <code>MimePath</code>.
         * 
         * @param mimePath The <code>MimePath</code> for which the infrastructure
         *   needs the new interceptor. Typically this is the same <code>MimePath</code>
         *   where this <code>Factory</code> was registered, but in embedded scenarios
         *   this can be a different <code>MimePath</code>.
         *
         * @return The new interceptor.
         */
        TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath);
    } // End of Factory interface
}
