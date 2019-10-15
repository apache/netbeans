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
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.lib2.typinghooks.TypingHooksSpiAccessor;

/**
 * An interceptor which is called when text is typed into a document. You should
 * implement this interface if you want to hook in the keyboard input
 * processing done in the editor infrastructure that would normally result in inserting text
 * into a document. Typically, implementations of this interface will be called
 * when processing <code>KeyEvent</code>s that reach a default keymap action (ie.
 * there is no entry in the editor's keymap for this particular <code>KeyEvent</code>).
 *
 * <p><b>Registration</b>: <code>TypedTextInterceptor</code>s can be plugged in the editor infrastructure
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
 * <p><b>Processing rules</b>: If there are multiple instances of <code>TypedTextInterceptor</code> registered
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
 * <li>{@link #beforeInsert(Context)} - It's called before any text is inserted
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
 *   inserted by calling {@link MutableContext#setText(java.lang.String, int)} method.
 *   The text insertion is strictly controlled by the infrastructure and has to obey some
 *   additional rules (eg. correctly replacing selected text, handling insert vs override
 *   modes of the caret, etc). The first interceptor that modifies the insertion text
 *   will win and no other interceptor's <code>insert</code> method will be called.
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
public interface TypedTextInterceptor {

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
     * This method is called immediately before the text is inserted into a document.
     * Implementors can use special <code>MutableContext</code> to modify the text
     * that will be inserted into a document. The first interceptor that mutates
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
     * This method is called after text is inserted into a document and its editor's
     * caret is adjusted.
     *
     * <p><b>Locking</b>: When this method is called the document is not locked
     * by the infrastructure.
     *
     * @param context The context object providing information necessary for processing
     *   the event. The {@link Context#getText()} method will return text that was
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
     * editor pane, insertion offset and text.
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
         * Gets the edited document. It's the document that will receive the insertion
         * text (ie the text typed by a user or its modification provided by an
         * interceptor).
         *
         * @return The edited document.
         */
        public Document getDocument() {
            return document;
        }

        /**
         * Gets the insertion offset. This is the offset in the document where
         * user typed the text (ie. where the currently processed <code>KeyEvent</code>
         * happened). This is also the offset where the insertion text will end up.
         *
         * @return The offset in the edited document.
         */
        public int getOffset() {
            return offset.getOffset();
        }

        /**
         * Gets the insertion text. This is the text that was typed
         * by the user or its modification provided by one of the interceptors.
         *
         * <p>It is guaranteed that the text will have length equal to 1 for contexts
         * that are passed to <code>beforeInsert</code> and <code>insert</code>
         * methods. In these methods <code>getText</code> returns exactly what
         * a user typed in the editor.
         *
         * <p>In the <code>afterInsert</code> method the text returned from <code>getText</code>
         * method can have any length and will correspond to either the originally typed
         * text or to text supplied by one of the interceptors participating in
         * the key typed event processing.
         * 
         * @return The insertion text.
         */
        public String getText() {
            return originallyTypedText;
        }

        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private final JTextComponent component;
        private final Document document;
        private final Position offset;
        private final String originallyTypedText;

        /* package */ Context(JTextComponent component, Position offset, String typedText) {
            this.component = component;
            this.document = component.getDocument();
            this.offset = offset;
            this.originallyTypedText = typedText;
        }
        
    } // End of Context class

    /**
     * This context class allows to modify the insertion text and the caret position
     * after the text is inserted into a document. Apart from that it provides exactly the same
     * information as its superclass <code>Context</code>.
     */
    public static final class MutableContext extends Context {

        public @Override String getText() {
            return insertionText != null ? insertionText : super.getText();
        }

        /**
         * Sets the insertion text and adjusted caret position. This method can
         * be used for modifying text typed by a user that would normally be
         * inserted into a document.
         *
         * <p>There is no restriction on the new text
         * set by this method, except that it must not be <code>null</code>. It can
         * be of any length (including an empty string) and can even span  multiple lines.
         *
         * <p>It is important to remember that the adjusted caret position is
         * relative to the new text. Therefore valid values for the <code>caretPosition</code>
         * parameter are <code>&lt;0, text.getLength()&gt;</code>! The adjusted position
         * is <b>not</b> a document offset.
         * 
         * @param text The new text that will be inserted to a document.
         * @param caretPosition The adjusted caret position <b>inside</b> the new text.
         *   This position is relative to the new text. Valid values for this parameter
         *   are <code>&lt;0, text.getLength()&gt;</code>.
         */
        public void setText(String text, int caretPosition) {
            setText(text, caretPosition, false);
        }

        /**
         * Sets the insertion text and adjusted caret position. This method can
         * be used for modifying text typed by a user that would normally be
         * inserted into a document.
         *
         * <p>There is no restriction on the new text
         * set by this method, except that it must not be <code>null</code>. It can
         * be of any length (including an empty string) and can even span  multiple lines.
         *
         * <p>It is important to remember that the adjusted caret position is
         * relative to the new text. Therefore valid values for the <code>caretPosition</code>
         * parameter are <code>&lt;0, text.getLength()&gt;</code>! The adjusted position
         * is <b>not</b> a document offset.
         * 
         * @param text The new text that will be inserted to a document.
         * @param caretPosition The adjusted caret position <b>inside</b> the new text.
         *   This position is relative to the new text. Valid values for this parameter
         *   are <code>&lt;0, text.getLength()&gt;</code>.
         * @param formatNewLines true if new lines in the provided text should be indented.
         * @since 2.26
         */
        public void setText(String text, int caretPosition, boolean formatNewLines) {
            assert text != null : "Invalid text, it must not be null."; //NOI18N
            assert caretPosition >= 0 && caretPosition <= text.length() : "Invalid caretPostion=" + caretPosition + ", text.length=" + text.length(); //NOI18N

            this.insertionText = text;
            this.caretPosition = caretPosition;
            this.formatNewLines = formatNewLines;
        }

        /**
         * Gets the replaced text. This is the text that was selected
         * by the user and it is replaced by inserted text.
         *
         * <p>The selected text is removed from document before <code>insert</code> method.
         *
         * @return The replaced text.
         */
        public String getReplacedText() {
            return replacedText;
        }

        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private String insertionText = null;
        private String replacedText = null;
        private int caretPosition = -1;
        private boolean formatNewLines = false;
        
        private MutableContext(JTextComponent c, Position offset, String typedText, String replacedText) {
            super(c, offset, typedText);
            this.replacedText = replacedText;
        }

        private static final class Accessor extends TypingHooksSpiAccessor {

            @Override
            public MutableContext createTtiContext(JTextComponent c, Position offset, String typedText, String replacedText) {
                return new MutableContext(c, offset, typedText, replacedText);
            }

            @Override
            public Object[] getTtiContextData(MutableContext context) {
                return context.insertionText != null ?
                    new Object [] { context.insertionText, context.caretPosition, context.formatNewLines } :
                    null;
            }

            @Override
            public void resetTtiContextData(MutableContext context) {
                context.insertionText = null;
                context.caretPosition = -1;
                context.formatNewLines = false;
            }

            @Override
            public DeletedTextInterceptor.Context createDtiContext(JTextComponent c, int offset, String removedText, boolean backwardDelete) {
                return new DeletedTextInterceptor.Context(c, offset, removedText, backwardDelete);
            }
            
            @Override
            public CamelCaseInterceptor.MutableContext createDwiContext(JTextComponent c, int offset, boolean backwardDelete) {
                return new CamelCaseInterceptor.MutableContext(c, offset, backwardDelete);
            }
            
            @Override
            public TypedBreakInterceptor.MutableContext createTbiContext(JTextComponent c, int caretOffset, int insertBreakOffset) {
                return new TypedBreakInterceptor.MutableContext(c, caretOffset, insertBreakOffset);
            }
            
            @Override
            public Object [] getTbiContextData(TypedBreakInterceptor.MutableContext context) {
                return context.getData();
            }
            
            @Override
            public void resetTbiContextData(TypedBreakInterceptor.MutableContext context) {
                context.resetData();
            }

            @Override
            public Object[] getDwiContextData(CamelCaseInterceptor.MutableContext context) {
                return context.getData();
            }
        } // End of Accessor class

        static {
            TypingHooksSpiAccessor.register(new Accessor());
        }

    } // End of MutableContext class

    /**
     * The factory interface for registering <code>TypedTextInterceptor</code>s
     * in <code>MimeLookup</code>. An example registration in an XML layer shown
     * below registers <code>Factory</code> implementation under <code>text/x-something</code>
     * mime type in <code>MimeLookup</code>.
     *
     * <pre>
     * &lt;folder name="Editors"&gt;
     *  &lt;folder name="text"&gt;
     *   &lt;folder name="x-something"&gt;
     *    &lt;file name="org-some-module-TTIFactory.instance" /&gt;
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
        TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath);
    } // End of Factory interface

}
