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

package org.netbeans.modules.quicksearch;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.util.NbBundle;

/**
 * Thread safe model of provider results of asociated category.
 * 
 * @author  Jan Becicka, Dafe Simonek
 */
public final class CategoryResult implements Runnable {
    
    static final int MAX_RESULTS = 7;
    static final int ALL_MAX_RESULTS = 30;

    private final boolean allResults; 
    
    private final Object LOCK = new Object();
    
    private final ProviderModel.Category category;
    
    private final List<ItemResult> items;
    
    private boolean obsolete;
    
    private int previousSize;

    private boolean moreResults = false;

    public CategoryResult (ProviderModel.Category category, boolean allResults) {
        this.category = category;
        this.allResults = allResults;
        items = new ArrayList<ItemResult>(allResults ? ALL_MAX_RESULTS : MAX_RESULTS);
    }

    public boolean addItem (ItemResult item) {
        synchronized (LOCK) {
            if (obsolete) {
                return false;
            }
            if (items.size() >= (allResults ? ALL_MAX_RESULTS : MAX_RESULTS)) {
                if (!allResults) {
                    moreResults = true;
                }
                return false;
            }
            items.add(item);
        }
        
        if (EventQueue.isDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
        
        return true;
    }
    
    /**
     * Get the value of item
     *
     * @return the value of item
     */
    public List<ItemResult> getItems() {
        List<ItemResult> rItems = null;
        synchronized (LOCK) {
            rItems = new ArrayList<ItemResult>(items);
            if (moreResults) {
                rItems.add(new ItemResult(this, null, this,
                        NbBundle.getMessage(getClass(), "LBL_MoreResults")));
            }
        }
        return rItems;
    }
    
    public boolean isFirstItem (ItemResult ir) {
        synchronized (LOCK) {
            if (items.size() > 0 && items.get(0).equals(ir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the value of Category
     *
     * @return the value of Category
     */
    public ProviderModel.Category getCategory() {
        return category;
    }

    public void setObsolete(boolean obsolete) {
        synchronized (LOCK) {
            this.obsolete = obsolete;
        }
    }

    public boolean isObsolete() {
        synchronized (LOCK) {
            return obsolete;
        }
    }

    /** Sends notification about category change, always runs in EQ thread */
    public void run() {
        int curSize = 0;
        boolean shouldNotify = false;
        synchronized (LOCK) {
            curSize = items.size();
            shouldNotify = !obsolete && 
                    items.size() <= (allResults ? ALL_MAX_RESULTS : MAX_RESULTS);
        }
        
        if (!shouldNotify) {
            return;
        }

        // as this method is called later then data change occurred (invocation through SwingUtilities.invokeLater),
        // it happens that all data are already added when this code is executed,
        // especially when provider is fast calling addItem. In such situation,
        // notification changes are redundant. We can get rid of them by controlling
        // category size. Data are only added through addItem, never removed,
        // and so the same size also means the same content and change
        // notification may be dismissed.
        if (curSize > previousSize) {
            previousSize = curSize;
            ResultsModel.getInstance().categoryChanged(this);
        }
        
    }

}
