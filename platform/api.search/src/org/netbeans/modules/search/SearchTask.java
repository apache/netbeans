/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     * @param searchComposition search composition
     * @param replacing replace mode
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
            Mutex.EVENT.writeAccess((Runnable) resultViewPanel::requestFocusInWindow);
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
        Mutex.EVENT.writeAccess(() -> ResultView.getInstance().makeBusy(busy));
    }
}
