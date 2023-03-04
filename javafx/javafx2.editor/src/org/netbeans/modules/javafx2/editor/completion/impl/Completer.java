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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Completer provides CompletionItems for a certain feature or a language
 * construct. Its {@link Factory} is called when the user invokes the CC,
 * and may create a Completer instance if it decides the context is interesting.
 * The Completer may produce zero to many CompletionItems.
 * <p/>
 * The Completer may hold state, a new instance should be created for each Completion
 * invocation by the {@code Factory.createCompleter} method.
 * @author sdedic
 */
public interface Completer {
    /**
     * Processes the CompletionContext and provides the completion items.
     * The method is called <b>OUTSIDE</b> the Document's read lock.
     * 
     * @return null, or any number (incl. zero) CompletionItems
     */
    @CheckForNull
    public List<? extends CompletionItem> complete();
    
    public boolean hasMoreItems();
     
    /**
     * Factory interface should be registered into MIME lookup using {@link MimeRegistration}
     * annotation. The {@link #createCompleter} will be called with an initialized
     * CompletionContext to decide whether the Factory can provide an appropriate Completer.
     * This allows to decompose completion code into pieces and extend it over time.
     */
    @MimeLocation(subfolderName="completion")
    public interface Factory {
        
        /**
         * Called by the infrastructure when completion items are to be produced.
         * The method should check the {@link CompletionContext}, whether its state
         * is applicable to this Completer and if so, the {@link Completer} instance 
         * should be returned.
         * <p/>
         * New Completer instance should be allocated and initialized
         * with CompletionContext by this method.
         * 
         * @param ctx contextual information to create completion items
         * @return 
         */
        @CheckForNull
        public Completer    createCompleter(@NonNull CompletionContext ctx);
    }
}
