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

package org.netbeans.spi.quicksearch;

import java.util.List;
import javax.swing.KeyStroke;
import org.netbeans.api.annotations.common.CheckReturnValue;
import org.netbeans.modules.quicksearch.CategoryResult;
import org.netbeans.modules.quicksearch.ResultsModel;
    
/**
 * Response object for collecting results of {@link SearchProvider#evaluate} search
 * operation. SearchProvider implementors are expected to fill SearchResponse
 * in steps by calling various {@link SearchResponse#addResult} methods.
 * 
 * @author Dafe Simonek
 */
public final class SearchResponse {

    private CategoryResult catResult;
    private SearchRequest sRequest;
   
    /** Package private creation, made available to other packages via
     * Accessor pattern.
     * @param catResult CategoryResult for storing response data 
     */
    SearchResponse (CategoryResult catResult, SearchRequest sRequest) {
        this.catResult = catResult;
        this.sRequest = sRequest;
    }

    /**
     * Adds new result of quick search operation.
     *  
     * @param action Runnable to invoke when this result item is chosen by user.
     * Providers are expected to signal unsuccessful invocation of <code>Runnable.run</code>
     * by writing into status line and producing beep. Invocation failures may happen,
     * as <code>Runnable.run</code> may be called later, when conditions or context
     * changed in a way that action can't be performed.
     * 
     * @param htmlDisplayName Localized display name of this result item.
     * May start with {@code <html>} in which case the text may use HTML tags;
     * then {@code <b>...</b>} tags should be used to emphasize part of the result.
     * Common provider implementations will use bold marking for found substring, so
     * resulting string should look like {@code <html>Item containing <b>searched</b> text}, where
     * {@code searched} is text returned from {@link SearchRequest#getText()}.
     * Use of other HTML tags is discouraged.
     * If plain text is passed, the first occurrence of the search string will be highlighted automatically.
     * 
     * @return true when result was accepted and more results are needed if available.
     * False when no further results are needed.
     * {@link SearchProvider} implementors should stop computing and leave
     * SearchProvider.evaluate(...) immediately if false is returned.
     */
    @CheckReturnValue public boolean addResult(Runnable action, String htmlDisplayName) {
        return addResult(action, htmlDisplayName, null, null);
    }
    
    /**
     * Adds new result of quick search operation.
     *  
     * @param action Runnable to invoke when this result item is chosen by user.
     * Providers are expected to signal unsuccessful invocation of <code>Runnable.run</code>
     * by writing into status line and producing beep. Invocation failures may happen,
     * as <code>Runnable.run</code> may be called later, when conditions or context
     * changed in a way that action can't be performed.
     * 
     * @param htmlDisplayName see {@link #addResult(Runnable,String)} for description
     * 
     * @param displayHint Localized display hint of this result item or null if not available
     * 
     * @param shortcut Shortcut of this result item or null if shortcut isn't available
     * 
     * @return true when result was accepted and more results are needed if available.
     * False when no further results are needed.
     * {@link SearchProvider} implementors should stop computing and leave
     * SearchProvider.evaluate(...) immediately if false is returned.
     */
    @CheckReturnValue public boolean addResult(Runnable action, String htmlDisplayName,
                            String displayHint, List <? extends KeyStroke> shortcut) {
        return catResult.addItem(
                new ResultsModel.ItemResult(catResult, sRequest, action,
                htmlDisplayName, shortcut, displayHint));
    }
    
    /**
     * Determines if the response is already obsolete. The result is the same as the 
     * return value from {@link #addResult(java.lang.Runnable, java.lang.String)}: if
     * false, the Provider should terminate the search immediately. Provider can query
     * this status in case it does not find any results so it does not waste time
     * searching further. 
     * 
     * @return true, if the result is obsolete.
     * @since 12.2
     */
    public boolean isObsolete() {
        return catResult.isObsolete();
    }

}
