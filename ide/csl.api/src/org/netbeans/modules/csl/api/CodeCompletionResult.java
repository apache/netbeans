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
package org.netbeans.modules.csl.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/** 
 * The CompletionResult object returns a list of proposals along with some
 * information about the result. You should subclass this class
 * yourself (or use the default implementation, 
 * {@link org.netbeans.modules.gsf.spi.DefaultCompletionResult}
 * and return an instance of it from the {@link CodeCompletionHandler#complete} method.
 * object is provided by the language implementation
 * 
 * @author Tor Norbye
 */
public abstract class CodeCompletionResult {
    /**
     * Special code completion result which means that there are no proposals
     */
    public static final CodeCompletionResult NONE = new CodeCompletionResult() {

        @Override
        public List<CompletionProposal> getItems() {
            return Collections.emptyList();
        }

        @Override
        public boolean isTruncated() {
            return false;
        }

        @Override
        public boolean isFilterable() {
            return false;
        }

    };

    /**
     * Return the list of completion proposals that should be presented to
     * the user.
     * 
     * @return A list of items to show the user
     */
    @NonNull
    public abstract List<CompletionProposal> getItems();

    /**
     * This method is called when the user has chosen to insert a code completion item.
     * The method is called BEFORE the actual item is inserted into the document.
     * 
     * @param item The proposal that is about to be inserted into the document.
     */
    public void beforeInsert(@NonNull CompletionProposal item) {
    }

    /**
     * <p>Insert the given item into the document. (The document and other context
     * was passed in as part of the {@link CodeCompletionContext} passed to the completion
     * handler.)
     * </p>
     * <p><b>NOTE</b>: Most implementation should just return false from this
     * method. False means that you have not handled the insertion, and the framework
     * will do it for you. Return true if you want to have custom handling here.
     * In that case, the infrastructure will not do anything else.
     * </p>
     * 
     * @param item The item to be inserted into the document.
     * @return true if you want to handle the insertion yourself, or false to get the
     *   infrastructure to do it on your behalf.
     */
    public boolean insert(@NonNull CompletionProposal item) {
        return false;
    }

    /**
     * This method is called when the user has chosen to insert a code completion item.
     * The method is called AFTER the actual item is inserted into the document.
     * 
     * @param item The proposal that has been inserted into the document.
     */
    public void afterInsert(@NonNull CompletionProposal item) {
    }

    /**
     * Return true if you have truncated the items that are returned. For example,
     * it is probably pointless to return a list of 5,000 methods to the user.
     * This just means a slow response time, so implementations may choose to abort
     * when the set is probably too large to be used without further filtering.
     * In this case, you should return "true" from this method, which will cause
     * the infrastructure to (a) insert an item at the bottom of the list stating
     * that the list has been truncated, and (b) it will NOT do its normal optimization
     * or simply filtering the first result set as the user types additional characters;
     * it will repeat the full search whenever the list has been truncated.
     * @return true if and only if the {@link #getItems()} method returned a truncated
     * result.
     */
    public abstract boolean isTruncated();

    /**
     * Return true iff the code completion result can be "filtered" (narrowed down)
     * by the infrastructure without repeating the search. In other words,
     * if the prefix was "f", and your search returned {"foo", "fuu"}, then
     * if your result is filterable (by default true), and the user types "u"
     * then the infrastructure will not repeat the search it will just filter
     * your original result down from {"foo", "fuu"} to just {"fuu"}.
     *
     * @return true iff the result can be filtered (by default, true).
     */
    public abstract boolean isFilterable();
    

}
