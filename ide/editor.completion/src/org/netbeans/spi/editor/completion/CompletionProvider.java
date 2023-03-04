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

package org.netbeans.spi.editor.completion;

import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * The basic interface for providing code completion items. You should implement this interface
 * if you want to provide items that are available to users when they invoke code completion in
 * a text document. You should register your implementation on the system FileSystem under the
 * <code>Editors/&lt;mime-type&gt;</code> folder. The registered implementation will then be used
 * for documents that are of the specified <code>mime-type</code>.
 * <p><b>Related documentation</b></p>
 * <ul>
 *   <li><a href="http://platform.netbeans.org/tutorials/nbm-code-completion.html">NetBeans Code Completion Tutorial</a></li>
 * </ul>
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */
@MimeLocation(subfolderName="CompletionProviders")
public interface CompletionProvider {

    /**
     * The <code>int</code> value representing the query for a code completion.
     */
    public static final int COMPLETION_QUERY_TYPE = 1;

    /**
     * The <code>int</code> value representing the query for a documentation.
     */    
    public static final int DOCUMENTATION_QUERY_TYPE = 2;
    
    /**
     * The <code>int</code> value representing the query for a tooltip hint.
     */    
    public static final int TOOLTIP_QUERY_TYPE = 4;

    /**
     * The <code>int</code> value representing the query for an all code completion.
     */
    public static final int COMPLETION_ALL_QUERY_TYPE = 9;

    /**
     * Creates a task that performs a query of the given type on the given component.
     * 
     * <p>This method is invoked in AWT thread only and the returned task
     * may either be synchronous (if it's not complex)
     * or it may be asynchronous
     * (see {@link org.netbeans.spi.editor.completion.support.AsyncCompletionTask}).
     * 
     * <p class="nonnormative">The task usually inspects the component's document, the
     * text up to the caret position and returns the appropriate result.
     * 
     * @param queryType Type of the query. It can be one of the {@link #COMPLETION_QUERY_TYPE},
     *  {@link #COMPLETION_ALL_QUERY_TYPE}, {@link #DOCUMENTATION_QUERY_TYPE},
     *  or {@link #TOOLTIP_QUERY_TYPE} (but not their combination).          
     * @param component A text component where the query should be performed.
     *
     * @return A task performing the query.
     */
    public CompletionTask createTask(int queryType, JTextComponent component);

    /**
     * Determines whether text typed in a document should automatically pop up the code completion
     * window. This method is called by the code completion infrastructure only to check whether
     * text that has just been typed into a text component triggers an automatic query invocation.
     * 
     * <p>An implementation of this method can return any combination of the query type constants
     * available in this interface to tell the infrastructure that it should call {@link #createTask(int, JTextComponent)}
     * and show the code completion window. Or it can return zero if the typed text does not trigger
     * popping up the code completion window.
     *
     * <p class="nonnormative">Please note that there could be multiple <code>CompletionProvider</code>s registered for
     * the same mime type and this method is called for all of them. This means that even if a particular
     * implementation does not want the code completion window to pop up for the typed text (ie. returns zero
     * from this method) there could be others that recognize the text as a trigger and will return non-zero.
     * If at least one of the registered <code>CompletionProvider</code>s returns
     * non-zero from this method the infrastructure will call <code>createTask</code> in all the registered
     * implementations asking them to provide completion items for the requested query type.
     *
     * @param component A component in which the text was typed.
     * @param typedText Typed text.
     *
     * @return Any combination of the {@link #COMPLETION_QUERY_TYPE}, {@link #COMPLETION_ALL_QUERY_TYPE},
     *         {@link #DOCUMENTATION_QUERY_TYPE}, and {@link #TOOLTIP_QUERY_TYPE}
     *         values, or zero if no query should be automatically invoked.
     */
    public int getAutoQueryTypes(JTextComponent component, String typedText);

}
