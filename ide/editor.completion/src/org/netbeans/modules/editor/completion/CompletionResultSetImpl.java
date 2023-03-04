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

package org.netbeans.modules.editor.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JToolTip;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;

/**
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionResultSetImpl {
    
    static {
        // Ensure the SPI accessor gets assigned
        try {
            Class.forName(CompletionResultSet.class.getName(), true, CompletionResultSetImpl.class.getClassLoader());
        } catch (ClassNotFoundException ex) {}
    }
    
    private static final CompletionSpiPackageAccessor spi
            = CompletionSpiPackageAccessor.get();

    private final CompletionImpl completionImpl;
    
    private final Object resultId;
    
    private final CompletionTask task;
    
    private final int queryType;
    
    private CompletionResultSet resultSet;
    
    private boolean active;

    private String title;
    
    private String waitText;
    
    private int anchorOffset;
    
    private List<CompletionItem> items;
    
    private boolean hasAdditionalItems;
    
    private String hasAdditionalItemsText;
    
    private boolean finished;
    
    private CompletionDocumentation documentation;
    
    private JToolTip toolTip;
    
    private int estimatedItemCount;
    
    private int estimatedItemWidth;
    
    CompletionResultSetImpl(CompletionImpl completionImpl,
    Object resultId, CompletionTask task, int queryType) {
        assert (completionImpl != null);
        assert (resultId != null);
        assert (task != null);
        this.completionImpl = completionImpl;
        this.resultId = resultId;
        this.task = task;
        this.queryType = queryType;
        this.anchorOffset = -1; // not set
        this.estimatedItemCount = -1; // not estimated
        this.active = true;
        
        spi.createCompletionResultSet(this);
    }
    
    /**
     * Get the result set instance associated with this implementation.
     */
    public synchronized CompletionResultSet getResultSet() {
        return resultSet;
    }
    
    public synchronized void setResultSet(CompletionResultSet resultSet) {
        assert (resultSet != null);
        assert (this.resultSet == null);
        this.resultSet = resultSet;
    }
    
    /**
     * Get the task associated with this result.
     */
    public CompletionTask getTask() {
        return task;
    }
    
    /**
     * Get the query type to which this result set belongs.
     * The results of other query types will be ignored when
     * being set into this result set.
     */
    public int getQueryType() {
        return queryType;
    }
    
    /**
     * Mark that results from this result set should no longer
     * be taken into account.
     */
    public synchronized void markInactive() {
        this.active = false;
    }

    public synchronized String getTitle() {
        return title;
    }

    public synchronized void setTitle(String title) {
        checkNotFinished();
        this.title = title;
    }
    
    public synchronized int getAnchorOffset() {
        return anchorOffset;
    }
    
    public synchronized void setAnchorOffset(int anchorOffset) {
        checkNotFinished();
        this.anchorOffset = anchorOffset;
    }
    
    public synchronized boolean addItem(CompletionItem item) {
        assert (item != null) : "Added item cannot be null";
        checkNotFinished();
        if (!active || (queryType & CompletionProvider.COMPLETION_QUERY_TYPE) == 0) {
            return false; // signal do not add any further results
        }

        if (items == null) {
            int estSize = (estimatedItemCount == -1) ? 10 : estimatedItemCount;
            items = new ArrayList<CompletionItem>(estSize);
        }
        items.add(item);
        return items.size() < 1000;
    }
    
    public boolean addAllItems(Collection<? extends CompletionItem> items) {
        boolean cont = true;
        for (Iterator<? extends CompletionItem> it = items.iterator(); it.hasNext();) {
            cont = addItem(it.next());
        }
        return cont;
    }
    
    /**
     * @return non-null list of items.
     */
    public synchronized List<? extends CompletionItem> getItems() {
        assert isFinished() : "Adding not finished";
        return (items != null) ? items : Collections.<CompletionItem>emptyList();
    }
    
    
    public synchronized void setHasAdditionalItems(boolean value) {
        checkNotFinished();
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return;
        }
        this.hasAdditionalItems = value;
    }

    public synchronized boolean hasAdditionalItems() {
        return hasAdditionalItems;
    }    
    
    public synchronized void setHasAdditionalItemsText(String text) {
        checkNotFinished();
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return;
        }
        this.hasAdditionalItemsText = text;
    }
    
    public synchronized String getHasAdditionalItemsText() {
        return hasAdditionalItemsText;
    }

    public synchronized void setDocumentation(CompletionDocumentation documentation) {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.DOCUMENTATION_QUERY_TYPE) {
            return;
        }
        this.documentation = documentation;
    }
    
    public synchronized CompletionDocumentation getDocumentation() {
        return documentation;
    }
    
    public synchronized JToolTip getToolTip() {
        return toolTip;
    }
    
    public synchronized void setToolTip(JToolTip toolTip) {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.TOOLTIP_QUERY_TYPE) {
            return;
        }
        this.toolTip = toolTip;
    }
    
    public synchronized boolean isFinished() {
        return finished;
    }
    
    public void finish() {
        synchronized (this) {
            if (finished) {
                throw new IllegalStateException("finish() already called"); // NOI18N
            }
            finished = true;
        }

        completionImpl.finishNotify(this);
    }
    
    public int getSortType() {
        return completionImpl.getSortType();
    }
    
    public synchronized void estimateItems(int estimatedItemCount, int estimatedItemWidth) {
        this.estimatedItemCount = estimatedItemCount;
        this.estimatedItemWidth = estimatedItemWidth;
    }
    
    CompletionImpl getCompletionImpl() {
        return completionImpl;
    }
    
    Object getResultId() {
        return resultId;
    }
    
    private void checkNotFinished() {
        if (isFinished()) {
            throw new IllegalStateException("Result set already finished"); // NOI18N
        }
    }

    public synchronized String getWaitText() {
        return waitText;
    }

    public synchronized void setWaitText(String waitText) {
        this.waitText = waitText;
    }

}
