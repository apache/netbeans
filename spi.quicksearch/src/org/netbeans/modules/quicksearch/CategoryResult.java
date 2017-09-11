/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
        return obsolete;
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
