/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

}
