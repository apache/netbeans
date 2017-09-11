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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.search;

import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.LifecycleManager;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;

/**
 * Task performing search.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @author  kaktus
 */
final class SearchTask implements Runnable, Cancellable {

    /** attribute used by class <code>Manager</code> */
    private boolean notifyWhenFinished = true;
    /** */
    private volatile boolean interrupted = false;
    /** */
    private volatile boolean finished = false;
    /** Search composition */
    private final SearchComposition<?> searchComposition;
    /** Replace mode */
    private final boolean replacing;
    /** */
    private ResultViewPanel resultViewPanel = null;    

    /**
     * Creates a new <code>SearchTask</code>.
     *
     * @param  searchScope  defines scope of the search task
     * @param  basicSearchCriteria  basic search criteria
     * @param  customizedSearchTypes  search types
     */
    public SearchTask(SearchComposition<?> searchComposition,
            boolean replacing) {

        this.searchComposition = searchComposition;
        this.replacing = replacing;
    }

    /**
     */
    private boolean isSearchAndReplace() {
        return replacing;
    }
    
    /** Runs the search task. */
    @Override
    public void run() {
        if (interrupted) {
            return;
        }
        if (isSearchAndReplace()) {
            LifecycleManager.getDefault().saveAll();
        }
        if (this.resultViewPanel == null) {
            this.resultViewPanel = ResultView.getInstance().addTab(this);
        }
        GraphicalSearchListener searchListener =
                this.resultViewPanel.createListener();
        try {
            makeResultViewBusy(true);
            searchListener.searchStarted();
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    resultViewPanel.requestFocusInWindow();
                }
            });
            searchComposition.start(searchListener);
        } catch (RuntimeException e) {
            searchListener.generalError(e);
        } finally {
            finished = true;
            searchListener.searchFinished();
            makeResultViewBusy(false);
        }
    }

    /**
     * Stops this search task.
     * This method also sets a value of attribute
     * <code>notifyWhenFinished</code>. This method may be called multiple
     * times (even if this task is already stopped) to change the value
     * of the attribute.
     *
     * @param  notifyWhenFinished  new value of attribute
     *                             <code>notifyWhenFinished</code>
     */
    void stop(boolean notifyWhenFinished) {
        if (notifyWhenFinished == false) {     //allow only change true -> false
            this.notifyWhenFinished = notifyWhenFinished;
        }
        stop();
    }
    
    /**
     * Stops this search task.
     *
     * @see  #stop(boolean)
     */
    void stop() {
        if (!finished) {
            interrupted = true;
        }
        if (searchComposition != null) {
            searchComposition.terminate();
        }
    }
   
    /** 
     * Cancel processing of the task. 
     *
     * @return true if the task was succesfully cancelled, false if job
     *         can't be cancelled for some reason
     * @see org.openide.util.Cancellable#cancel
     */
    @Override
    public boolean cancel() {
        stop();
        return true;
    }

    /**
     * Returns value of attribute <code>notifyWhenFinished</code>.
     *
     * @return  current value of the attribute
     */
    boolean notifyWhenFinished() {
        return notifyWhenFinished;
    }
    
    /**
     * Was this search task interrupted?
     *
     * @return  <code>true</code> if this method has been interrupted
     *          by calling {@link #stop()} or {@link #stop(boolean)}
     *          during the search; <code>false</code> otherwise
     */
    boolean wasInterrupted() {
        return interrupted;
    }

    SearchResultsDisplayer<?> getDisplayer() {
        return searchComposition.getSearchResultsDisplayer();
    }

    SearchComposition<?> getComposition() {
        return this.searchComposition;
    }

    void setResultViewPanel(ResultViewPanel resultViewPanel) {
        this.resultViewPanel = resultViewPanel;
    }

    private void makeResultViewBusy(final boolean busy) {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                ResultView.getInstance().makeBusy(busy);
            }
        });
    }
}
