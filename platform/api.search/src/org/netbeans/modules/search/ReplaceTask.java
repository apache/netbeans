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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.search.MatchingObject.InvalidityStatus;
import org.netbeans.modules.search.ui.BasicReplaceResultsPanel;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * Task that checks validity of found files and then
 * (if everything is valid) replaces the matching substrings
 * with the replacement string/pattern.
 * 
 * @author  Tim Boudreau
 * @author  Marian Petras
 */
public final class ReplaceTask implements Runnable {
    
    /**
     * maximum number of errors detected before replacing
     * and displayed to the user
     */
    private static final int MAX_ERRORS_CHECKED = 20;
    
    private final List<MatchingObject> matchingObjects;
    private final ProgressHandle progressHandle;
    private final List<String> problems;
    private final BasicReplaceResultsPanel panel;
    
    /** */
    private ResultStatus resultStatus = null;
    
    enum ResultStatus {
        SUCCESS,
        PRE_CHECK_FAILED,
        PROBLEMS_ENCOUNTERED
    }
    
    /**
     */
    public ReplaceTask(List<MatchingObject> matchingObjects,
            BasicReplaceResultsPanel panel) {
        this.matchingObjects = matchingObjects;
        this.panel = panel;
        
        problems = new ArrayList<>(4);
        progressHandle = ProgressHandle.createHandle(
                NbBundle.getMessage(getClass(), "LBL_Replacing"), //NOI18N
                null, null);
    }
    
    /**
     */
    @Override
    public void run() {
        assert !EventQueue.isDispatchThread();
        
        progressHandle.start(matchingObjects.size() * 2);
        try {
            replace();
            assert resultStatus != null;
        } finally {
            progressHandle.finish();
        }
    }
    
    /**
     */
    private void replace() {
        assert !EventQueue.isDispatchThread();
        
        checkForErrors();
        if (resultStatus == null) {       //the check passed
            FileUtil.runAtomicAction((Runnable) this::doReplace);
        }
    }
    
    /**
     */
    private void checkForErrors() {
        assert !EventQueue.isDispatchThread();
        
        int errorsCount = 0;
        
        for(MatchingObject mo: matchingObjects) {
            InvalidityStatus status = mo.checkValidity();
            if (status != null) {
                problems.add(status.getDescription(mo.getFileObject().getPath()));
                if (++errorsCount > MAX_ERRORS_CHECKED) {
                    break;
                }
            }
        }
        if (!problems.isEmpty()) {
            resultStatus = ResultStatus.PRE_CHECK_FAILED;
        }
    }

    /**
     * Fill internal list of strings describing problems that happened during
     * the replace and set resultStatus variable
     */
    private void doReplace() {
        assert !EventQueue.isDispatchThread();

        int i = 0;
        int moSize = matchingObjects.size();
        for(final MatchingObject obj: matchingObjects) {
            int workunit = moSize + i++;
            progressHandle.progress(obj.getName(), workunit);
            if (!obj.isSelected() || !obj.isValid()) {
                continue;
            }
            
            String invDescription = obj.getInvalidityDescription();
            if (invDescription != null) {
                problems.add(invDescription);
                continue;
            }
            
            String errMessage = null;
            FileLock fileLock = null;
            try {
                fileLock = obj.lock();
                MatchingObject.InvalidityStatus status = obj.replace();
                if (status == null) {
                    obj.write(fileLock);
                } else {
                    errMessage = status.getDescription(obj.getFileObject().getPath());
                }
            } catch (FileAlreadyLockedException |
                    UserQuestionException ex) {
                errMessage = createMsgFileLocked(obj);
            } catch (IOException ex) {
                ex.printStackTrace();      //PENDING - ex.printStackTrace()?
                errMessage = ex.getLocalizedMessage();
                if (errMessage == null) {
                    errMessage = ex.getMessage();
                }
            } finally {
                if (fileLock != null) {
                    fileLock.releaseLock();
                }
            }
            if (errMessage != null) {
                problems.add(errMessage);
            }
        }
        resultStatus = problems.isEmpty() ? ResultStatus.SUCCESS
                                          : ResultStatus.PROBLEMS_ENCOUNTERED;
    }

    private static String createMsgFileLocked(MatchingObject matchingObj) {
        return NbBundle.getMessage(
                ReplaceTask.class,
                "MSG_cannot_access_file_already_locked",                //NOI18N
                matchingObj.getName());
    }
    
    /**
     * 
     * @see  #getProblems()
     */
    ResultStatus getResultStatus() {
        return resultStatus;
    }
    
    /**
     * Returns a list of problems encountered during the pre-check or 
     * during replacing. The type of problems (pre-check or replacing)
     * can be determined from the results status returned by method
     * {@link #getResultStatus()}.
     * 
     * @return  array of problems, or {@code null} if no problems have been
     *          encountered
     * @see  #getResultStatus()
     */
    String[] getProblems() {
        return problems.isEmpty()
               ? null
               : problems.toArray(new String[0]);
    }

    BasicReplaceResultsPanel getPanel() {
        return panel;
    }
}
