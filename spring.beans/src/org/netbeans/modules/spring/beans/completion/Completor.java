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
package org.netbeans.modules.spring.beans.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class Completor {

    private volatile List<SpringXMLConfigCompletionItem> cache = new ArrayList<SpringXMLConfigCompletionItem>();
    private volatile int anchorOffset = -1;
    private final int invocationOffset;
    private volatile boolean hasAdditionalItems = false;
    private volatile boolean cancelled = false;

    protected Completor(int invocationOffset) {
        this.invocationOffset = invocationOffset;
    }

    public final SpringCompletionResult complete(CompletionContext context) {
        try {
            anchorOffset = initAnchorOffset(context);
            compute(context);
            return SpringCompletionResult.create(cache, anchorOffset, hasAdditionalItems, getAdditionalItemsText());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return SpringCompletionResult.NONE;
    }

    protected abstract int initAnchorOffset(CompletionContext context);

    protected abstract void compute(CompletionContext context) throws IOException;

    public boolean canFilter(CompletionContext context) {
        return false;
    }

    public final SpringCompletionResult filter(CompletionContext context) {
        return SpringCompletionResult.create(doFilter(context), anchorOffset, hasAdditionalItems, getAdditionalItemsText());
    }

    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        throw new UnsupportedOperationException("No default implementation"); // NOI18N
    }

    protected final void addCacheItem(SpringXMLConfigCompletionItem item) {
        cache.add(item);
    }

    protected List<SpringXMLConfigCompletionItem> getCacheItems() {
        return cache;
    }

    protected final int getAnchorOffset() {
        return anchorOffset;
    }

    protected final boolean isCancelled() {
        return cancelled;
    }

    protected final void cancel() {
        cancelled = true;
    }

    protected final int getInvocationOffset() {
        return invocationOffset;
    }

    protected String getAdditionalItemsText() {
        return "";
    }

    protected void setAdditionalItems(boolean additionalItems) {
        hasAdditionalItems = additionalItems;
    }
}
