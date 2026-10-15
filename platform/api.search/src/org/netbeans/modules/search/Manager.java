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

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.netbeans.modules.search.ReplaceTask.ResultStatus.PRE_CHECK_FAILED;
import static org.netbeans.modules.search.ReplaceTask.ResultStatus.SUCCESS;
import org.netbeans.spi.search.provider.SearchComposition;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.OutputWriter;

/**
 * Manager of the Search module's activities.
 * It knows which tasks are running and manages the module's actions so that
 * no two conflicting tasks are running at a moment.
 *
 * @see <a href="doc-files/manager-state-diagram.png">State diagram</a>
 * @author  Marian Petras
 * @author  kaktus
 */
public final class Manager {
    
    static final int SEARCHING        = 0x01;
/*
    static final int CLEANING_RESULT  = 0x02;

    static final int PRINTING_DETAILS = 0x04;
*/
    static final int REPLACING        = 0x08;

    static final int EVENT_SEARCH_STARTED = 1;
    
    static final int EVENT_SEARCH_FINISHED = 2;
    
    static final int EVENT_SEARCH_INTERRUPTED = 3;
    
    static final int EVENT_SEARCH_CANCELLED = 4;
    
    private static final Manager instance = new Manager();

    private final List<Runnable> pendingTasks = new LinkedList<>();
    
    private TaskListener taskListener;
    
    private final List<Runnable> currentTasks = new LinkedList<>();

    private final List<Runnable> stoppingTasks = new LinkedList<>();

    private boolean searchWindowOpen = false;
    
    private Reference<OutputWriter> outputWriterRef;

    private Map<Task, Runnable> tasksMap = new HashMap<>();

    private static final RequestProcessor RP =
            new RequestProcessor(Manager.class.getName()); // #186445


    /**
     */
    public static Manager getInstance() {
        return instance;
    }

    /**
     */
    private Manager() { }
    
    /*
     * INVARIANTS:
     * #1: If the Search Results window is open, its root node displays:
     *     - if the search task is in progress:
     *             - summary of results
     *     - if the Search module is inactive:
     *             - summary of current results (continuously updated)
     *     - if the search task in scheduled but another task is blocking it:
     *             - name of the current task blocking the search
     * #2: At most one result model exists at a single moment.
     */

    /**
     */
    synchronized void scheduleSearchTask(SearchTask task) {
        assert EventQueue.isDispatchThread();

        ResultView resultView = ResultView.getInstance();
        resultView.open();
        resultView.requestActive();
        task.setResultViewPanel(resultView.addTab(task));
        pendingTasks.add(task);
        processNextPendingTask();
    }

    public <R> void scheduleSearchTask(SearchComposition<R> searchComposition,
            boolean replacing) {
        scheduleSearchTask(new SearchTask(searchComposition, replacing));
    }

    /**
     */
    public synchronized void scheduleReplaceTask(ReplaceTask task) {
        assert EventQueue.isDispatchThread();
        
        pendingTasks.add(task);
        processNextPendingTask();
    }

    /**
     */
    public synchronized void schedulePrintTask(PrintDetailsTask task) {
        assert EventQueue.isDispatchThread();

        pendingTasks.add(task);
        processNextPendingTask();
    }

    /**
     */
    synchronized void scheduleCleanTask(CleanTask task) {
        assert EventQueue.isDispatchThread();

        pendingTasks.add(task);
        processNextPendingTask();
    }
    
    /**
     * Queries whether the user should be allowed to initiate a new search.
     * For example, the user should not be allowed to do so if the last
     * replace action has not finished yet.
     * 
     * @return  message to the user, describing the reason why a new search
     *          cannot be started, or {@code null} if there is no such reason
     *          (i.e. if a new search may be started)
     */
    synchronized String mayStartSearching() {
        String msgKey = haveRunningReplaceTask() ? "MSG_Cannot_start_search__replacing"//NOI18N
                                : null;
        return (msgKey != null) ? NbBundle.getMessage(getClass(), msgKey)
                                    : null;
    }
    
    /**
     */
    private void notifySearchStarted(SearchTask task) {
        notifySearchTaskStateChange(task, EVENT_SEARCH_STARTED);
    }
    
    /**
     */
    private void notifySearchFinished(SearchTask task) {
        notifySearchTaskStateChange(task, EVENT_SEARCH_FINISHED);
    }
    
    /**
     */
    private void notifySearchInterrupted(SearchTask task) {
        notifySearchTaskStateChange(task, EVENT_SEARCH_INTERRUPTED);
    }
    
    /**
     */
    private void notifySearchCancelled(SearchTask task) {
        notifySearchTaskStateChange(task, EVENT_SEARCH_CANCELLED);
    }
    
    /**
     * Notifies the result window of a search task's state change.
     *
     * @param task
     * @param changeType  constant describing what happened
     *                     - one of the EVENT_xxx constants
     */
    private void notifySearchTaskStateChange(final SearchTask task, final int changeType) {
        Method theMethod;
        try {
            theMethod = ResultView.class.getDeclaredMethod(
                                                "searchTaskStateChanged",  //NOI18N
                                                SearchTask.class,
                                                Integer.TYPE);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }

        callOnWindowFromAWT(theMethod, new Object[]{task, changeType});
    }

    /**
     */
    private void notifySearchPending(SearchTask sTask, final int blockingTask) {
        if (!searchWindowOpen) {
            return;
        }
        Method theMethod;
        try {
            theMethod = ResultView.class.getDeclaredMethod(
                                                "notifySearchPending",  //NOI18N
                                                SearchTask.class,
                                                Integer.TYPE);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        callOnWindowFromAWT(theMethod, new Object[]{sTask, blockingTask});
    }
    
    /**
     */
    private void notifyReplaceFinished(ReplaceTask task) {
        ReplaceTask.ResultStatus resultStatus = task.getResultStatus();
        if (resultStatus == SUCCESS) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(getClass(), "MSG_Success"));    //NOI18N
            if (searchWindowOpen) {
                task.getPanel().showFinishedInfo();
            }
        } else {
            String msgKey = (resultStatus == PRE_CHECK_FAILED)
                            ? "MSG_Issues_found_during_precheck"        //NOI18N
                            : "MSG_Issues_found_during_replace";        //NOI18N
            String title = NbBundle.getMessage(getClass(), msgKey);
            task.getPanel().displayIssuesToUser(task, title, task.getProblems(),
                    resultStatus != PRE_CHECK_FAILED);
            if (resultStatus == PRE_CHECK_FAILED) {
                offerRescanAfterIssuesFound(task);
            }
        }
    }
    
    /**
     */
    private void offerRescanAfterIssuesFound(final ReplaceTask task) {
        String msg = NbBundle.getMessage(getClass(),
                                         "MSG_IssuesFound_Rescan_");    //NOI18N
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                            msg,
                                            NotifyDescriptor.QUESTION_MESSAGE);
        String rerunOption = NbBundle.getMessage(getClass(),
                                                 "LBL_Rerun");          //NOI18N
        nd.setOptions(new Object[] {rerunOption,
                                    NotifyDescriptor.CANCEL_OPTION});
        Object dlgResult = DialogDisplayer.getDefault().notify(nd);
        if (rerunOption.equals(dlgResult)) {
            /*
             * The rescan method calls 'scheduleSearchTaskRerun()' on this.
             * But it will wait until 'taskFinished()' returns, which is
             * exactly what we need to keep consistency of the manager's fields
             * like 'currentReplaceTask', 'replaceTask' and 'state'.
             * Using this mechanism also requires that, when sending a method
             * to the EventQueue thread, we use invokeLater(...) and not
             * invokeAndWait(...).
             */
            Mutex.EVENT.writeAccess(task.getPanel()::rescan);
        }
    }
    
    /**
     */
    private void notifyPrintingDetailsFinished() {
        if (!searchWindowOpen) {
            return;
        }
        callOnWindowFromAWT("showAllDetailsFinished");                  //NOI18N
    }

    /**
     */
    private void activateResultWindow() {
        Method theMethod;
        try {
            theMethod = ResultView.class
                        .getMethod("requestActive", new Class<?>[0]);   //NOI18N
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException();
        }
        callOnWindowFromAWT(theMethod, null);
    }
    
    /**
     */
    private void displayIssuesFromAWT(ReplaceTask task,
                                      String title,
                                      boolean att) {
        Method theMethod;
        try {
            theMethod = ResultView.class.getDeclaredMethod(
                                                "displayIssuesToUser",  //NOI18N
                                                ReplaceTask.class,
                                                String.class,
                                                String[].class,
                                                Boolean.TYPE);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        callOnWindowFromAWT(theMethod,
                            new Object[] {task, title, task.getProblems(), att},
                            false);
    }
    
    /**
     * Calls a given method on the Search Results window, from the AWT thread.
     *
     * @param  methodName  name of the method to be called
     */
    private void callOnWindowFromAWT(final String methodName) {
        callOnWindowFromAWT(methodName, true);
    }
    
    /**
     */
    private void callOnWindowFromAWT(final String methodName,
                                     final boolean wait) {
        Method theMethod;
        try {
            theMethod = ResultView.class
                        .getDeclaredMethod(methodName, new Class<?>[0]);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException();
        }
        callOnWindowFromAWT(theMethod, null, wait);
    }
    
    /**
     */
    private void callOnWindowFromAWT(final Method method,
                                     final Object[] params) {
        callOnWindowFromAWT(method, params, true);
    }
    
    /**
     */
    private void callOnWindowFromAWT(final Method method,
                                     final Object[] params,
                                     final boolean wait) {
        Runnable runnable = () -> {
            final ResultView resultViewInstance = ResultView.getInstance();
            try {
                method.invoke(resultViewInstance, params);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        };
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            if (wait) {
                try {
                    EventQueue.invokeAndWait(runnable);
                } catch (InvocationTargetException ex1) {
                    ErrorManager.getDefault().notify(ex1);
                } catch (Exception ex2) {
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex2);
                }
            } else {
                EventQueue.invokeLater(runnable);
            }
        }
    }
    
    /**
     */
    synchronized void searchWindowOpened() {
        searchWindowOpen = true;
    }

    /**
     */
    synchronized void searchWindowClosed() {
        assert EventQueue.isDispatchThread();
        
        searchWindowOpen = false;
        Runnable[] tasks = currentTasks.toArray(new Runnable[0]);
        for (Runnable task : tasks) {
            if (task instanceof SearchTask) {
                SearchTask sTask = (SearchTask) task;
                sTask.stop(true);
                if (sTask.getDisplayer() instanceof ResultDisplayer) {
                    ResultDisplayer disp =
                            (ResultDisplayer) sTask.getDisplayer();
                    scheduleCleanTask(new CleanTask(disp.getResultModel()));
                }
            }
        }
    }
    
    /**
     */
    private synchronized void processNextPendingTask() {
        Runnable[] pTasks = pendingTasks.toArray(new Runnable[0]);
        for (Runnable pTask : pTasks) {
            boolean haveReplaceRunning = haveRunningReplaceTask();
            if (pTask instanceof SearchTask) {
                if (!stoppingTasks.isEmpty()) {
                    notifySearchPending((SearchTask) pTask, SEARCHING);
                } else if (haveReplaceRunning) {
                    notifySearchPending((SearchTask) pTask, REPLACING);
                } else {
                    if (pendingTasks.remove(pTask)) {
                        startSearching((SearchTask) pTask);
                    }
                }
            } else if (pTask instanceof ReplaceTask) {
                if (!haveReplaceRunning && !haveRunningSearchTask()) {
                    if (pendingTasks.remove(pTask)) {
                        startReplacing((ReplaceTask) pTask);
                    }
                }
            } else if (pTask instanceof PrintDetailsTask) {
                if (pendingTasks.remove(pTask)) {
                    startPrintingDetails((PrintDetailsTask) pTask);
                }
            } else if (pTask instanceof CleanTask) {
                if (pendingTasks.remove(pTask)) {
                    startCleaning((CleanTask) pTask);
                }
            } else {
                //only 4 task types described above can be here
                assert false : "Unexpected task: " + pTask; // #184603
            }
        }
    }

    private boolean haveRunningReplaceTask() {
        for (Runnable r : currentTasks) {
            if (r instanceof ReplaceTask) {
                return true;
            }
        }
        return false;
    }

    private boolean haveRunningSearchTask() {
        for (Runnable r : currentTasks) {
            if (r instanceof SearchTask) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    private void startSearching(SearchTask sTask) {
        notifySearchStarted(sTask);

        if (outputWriterRef != null) {
            SearchDisplayer.clearOldOutput(outputWriterRef);
            outputWriterRef = null;

            /*
             * The following is necessary because clearing the output window
             * activates the output window:
             */
            activateResultWindow();
        }
        runTask(sTask);
    }
    
    /**
     */
    private void startReplacing(ReplaceTask rTask) {
        runTask(rTask);
    }
    
    /**
     */
    private void startPrintingDetails(PrintDetailsTask pTask) {
        if (outputWriterRef != null) {
            SearchDisplayer.clearOldOutput(outputWriterRef);
            outputWriterRef = null;
        }
        runTask(pTask);
    }
    
    /**
     */
    private void startCleaning(CleanTask cTask) {
        runTask(cTask);
    }

    private void runTask(Runnable task){
        assert task != null;
        currentTasks.add(task);

        RequestProcessor.Task pTask;
        pTask = RP.create(task);
        tasksMap.put(pTask, task);
        pTask.addTaskListener(getTaskListener());
        pTask.schedule(0);
    }

    /**
     */
    void stopSearching(SearchTask sTask) {
        
        boolean stopTask = false;

        synchronized (this) {
            if (pendingTasks.remove(sTask)) {
                notifySearchCancelled(sTask);
            } else if (currentTasks.contains(sTask)) {
                if (!stoppingTasks.contains(sTask)) {
                    stoppingTasks.add(sTask);
                    stopTask = true;
                }
            }
        }
        if (stopTask) {
            sTask.stop();
        }
    }

    /**
     */
    private void taskFinished(Runnable task) {

        notifyTaskFinished(task);

        synchronized (this) {
            if (task instanceof SearchTask) {
                stoppingTasks.remove(task);
            } else if (task instanceof PrintDetailsTask) {
                PrintDetailsTask pTask = (PrintDetailsTask) task;
                outputWriterRef = pTask.getOutputWriterRef();
            }
            currentTasks.remove(task);
        }
        processNextPendingTask();
    }
    
    private void notifyTaskFinished(Runnable task) {
        if (task instanceof SearchTask) {
            SearchTask sTask = (SearchTask) task;
            if (sTask.notifyWhenFinished()) {
                if (sTask.wasInterrupted()) {
                    notifySearchInterrupted(sTask);
                } else {
                    notifySearchFinished(sTask);
                }
            }
        } else if (task instanceof ReplaceTask){
            ReplaceTask rTask = (ReplaceTask)task;
            notifyReplaceFinished(rTask);
        } else if (task instanceof PrintDetailsTask){
            PrintDetailsTask pTask = (PrintDetailsTask)task;
            notifyPrintingDetailsFinished();
        }
    }

    /**
     */
    private TaskListener getTaskListener() {
        if (taskListener == null) {
            taskListener = new MyTaskListener();
        }
        return taskListener;
    }


    /**
     */
    private class MyTaskListener implements TaskListener {

        /**
         */
        MyTaskListener() {
            super();
        }

        /**
         */
        @Override
        public void taskFinished(Task task) {
            Runnable rTask;
            synchronized (Manager.this) {
                rTask = Manager.this.tasksMap.remove(task);
            }
            if (rTask != null) {
                Manager.this.taskFinished(rTask);
            }
        }

    }
}
