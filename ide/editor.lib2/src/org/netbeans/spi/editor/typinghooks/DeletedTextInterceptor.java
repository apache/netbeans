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

package org.netbeans.spi.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;

/**
 * An interceptor which is called when user deletes text from a document. You should
 * implement this interface if you want to hook in the keyboard input
 * processing done in the editor infrastructure that would normally result in removing text
 * from a document. Typically, implementations of this interface will be called
 * when processing <code>KeyEvent</code>s that reach the default editor actions bound to
 * <code>VK_DELETE</code> and <code>VK_BACK_SPACE</code> events.
 *
 * <p><b>Registration</b>: <code>DeletedTextInterceptor</code>s can be plugged in the editor infrastructure
 * by implementing the {@link Factory} interface and registering it in <code>MimeLookup</code>
 * under the appropriate mimetype (ie. <code>MimePath</code>).
 *
 * <p>The instances created from the <code>Factory</code> will be reused for processing
 * the relevant key events received by all documents of the same mime type, which the interceptor
 * instances were registered for (including documents that contain embedded sections
 * of that mime type). As described in the general concepts of Typing Hooks SPI
 * the interceptors are guaranteed to be called in AWT thread only, which means that
 * they should not need any internal synchronization model.
 *
 * <p><b>Processing rules</b>: If there are multiple instances of <code>DeletedTextInterceptor</code> registered
 * for the same mime type the infrastructure will queue them up in their registration
 * order and when processing an event it will call them all until the processing is done
 * or terminated. 
 *
 * <p>The interceptor has several methods that are called at different stages of
 * the key typed event processing. When processing an event the infrastructure will call
 * the methods in the order as they are listed below. Moreover if there are multiple
 * interceptors queued up for processing an event each method is first called on
 * all the queued interceptors before moving on to the next stage and calling next
 * method.
 *
 * <ul>
 * <li>{@link #beforeRemove(Context)} - It's called before any text is removed
 *   from a document. No document lock is held when this method is called. The method
 *   is not allowed to modify the document (and it's not supposed to do any tricks to
 *   break this rule). An interceptor can stop further processing of the event by returning
 *   <code>true</code> from this method. If it does so, no other interceptors'
 *   <code>beforeRemove</code> method will be called and the processing will be terminated
 *   without removing any text.
 *
 * <li>{@link #remove(Context)} - This method is called during the text
 *   removal stage immediately after the text was removed from a document. At this
 *   time the document is already write locked and the interceptors can modify it
 *   if they need to.
 *
 * <li>{@link #afterRemove(Context)} - This is the last method in the processing
 *   chain and it will be called when the text has already been removed from the document.
 *   Similarly as in <code>beforeRemove</code> the document is not locked when
 *   this method is called.
 * 
 * <li>{@link #cancelled(Context)} - This is an additional method that will be called
 *   when the processing is terminated in the before-removal stage (ie. by an interceptor
 *   returning <code>true</code> from its <code>beforeRemove</code> method).
 *   The infrastructure will only call this method on interceptors that have already
 *   had their <code>beforeRemove</code> method called, but not on those that
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
public interface DeletedTextInterceptor {

    /**
     * This method is called before any text is removed from a document. The context object
     * passed to the method provides access to the document and its editor pane. The interceptors
     * are not allowed to modify the document in this method.
     *
     * <p>This method can be used for stopping further processing of the current
     * key typed event. If this method returns <code>true</code> the processing will
     * be terminated and {@link #cancelled(Context)} will be called for all the interceptors
     * that have already had their <code>beforeRemove</code> method called (including
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
    boolean beforeRemove(Context context) throws BadLocationException;
    
    /**
     * This method is called immediately after the text is removed from a document.
     * Implementors can modify the document as they need. The document and all
     * the other useful information is accessible from the <code>Context</code> object
     * passed in this method. The document is write-locked.
     *
     * <p><b>Locking</b>: When this method is called the infrastructure has already
     * write locked the document.
     *
     * @param context The context object providing information necessary for processing
     *   the event and allowing to modify the edited document.
     *
     * @throws BadLocationException If the processing fails.
     */
    void remove(Context context) throws BadLocationException;

    /**
     * This method is called after text is removed from a document and its editor's
     * caret is adjusted.
     *
     * <p><b>Locking</b>: When this method is called the document is not locked
     * by the infrastructure.
     *
     * @param context The context object providing information necessary for processing
     *   the event. The {@link Context#getText()} method will return text that was
     *   removed from the document at the beginning of the text-removal stage.
     * 
     * @throws BadLocationException Since the document is not locked prior calling this
     *   method the processing may fail when working with stale context data.
     */
    void afterRemove(Context context) throws BadLocationException;

    /**
     * This method is called when the normal processing is terminated by some
     * interceptor's <code>beforeRemove</code> method. Please note that this
     * method will not be called if the <code>beforeRemove</code> method was not
     * called.
     * 
     * @param context The context object used for calling the <code>beforeRemove</code>
     *   method.
     */
    void cancelled(Context context);
    
    /**
     * The context class providing information about the edited document, its
     * editor pane and the offset where the delete key event occurred.
     */
    public static final class Context {

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
         * Gets the edited document. It's the document, where the text will be
         * removed.
         *
         * @return The edited document.
         */
        public Document getDocument() {
            return document;
        }
        
        /**
         * Gets the removal offset. This is the offset in the document where
         * a user performed the delete action (ie. where the currently processed <code>KeyEvent</code>
         * happened). This is also the offset with text, which will be removed.
         *
         * @return The offset in the edited document.
         */
        public int getOffset() {
            return offset;
        }

// XXX: since this is always one (character) it make no sense to have it
//        public int getLength() {
//            return lenght;
//        }
        
        /**
         * Determines the type of the character removal action performed by a user. The two
         * possible actions are called differently on different platforms,
         * but they are always defined by the position of a character, which they are
         * applied to. The <i>backspace</i> action deletes a character on the left hand
         * side of a caret, while the <i>delete</i> action deletes a character on
         * the right hand side of the caret.
         * 
         * <p>In other words one delete action removes characters backwards moving
         * the caret towards the beginning if a document and the other action leaves
         * the caret at the same position and removes characters towards the end
         * of the document.
         * 
         * @return <code>true</code> if the interceptor is called to handle the
         *   backspace action. <code>false</code> if the handled action is the
         *   delete action.
         */
        public boolean isBackwardDelete() {
            return backwardDelete;
        }
        
        /**
         * Gets the text being removed. In <code>beforeRemove</code> method this
         * text is still present in the document, while in the other methods this
         * text has already been removed from the document. Nevertheless this method
         * always returns a copy of the text.
         * 
         * @return The text being removed by the currently processed key typed event.
         */
        public String getText() {
            return removedText;
        }
        
        // -------------------------------------------------------------------
        // Private implementation
        // -------------------------------------------------------------------

        private final JTextComponent component;
        private final Document document;
        private final int offset;
        private final boolean backwardDelete;
        private final String removedText;

        /* package */ Context(JTextComponent component, int offset, String removedText, boolean backwardDelete) {
            this.component = component;
            this.document = component.getDocument();
            this.offset = offset;
            this.backwardDelete = backwardDelete;
            this.removedText = removedText;
        }
        
    } // End of Context class

    /**
     * The factory interface for registering <code>DeletedTextInterceptor</code>s
     * in <code>MimeLookup</code>. An example registration in an XML layer shown
     * below registers <code>Factory</code> implementation under <code>text/x-something</code>
     * mime type in <code>MimeLookup</code>.
     *
     * <pre>
     * &lt;folder name="Editors"&gt;
     *  &lt;folder name="text"&gt;
     *   &lt;folder name="x-something"&gt;
     *    &lt;file name="org-some-module-DTIFactory.instance" /&gt;
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
        DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath);
    } // End of Factory interface
}
