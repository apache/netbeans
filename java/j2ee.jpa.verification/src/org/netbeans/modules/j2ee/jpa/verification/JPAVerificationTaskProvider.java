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
package org.netbeans.modules.j2ee.jpa.verification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder.ProblemFinderCompControl;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
/**
 * TODO: This code is temporarily disabled, see issue #163916
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAVerificationTaskProvider extends FileTaskScanner {
    private static final String TASKLIST_ERROR = "nb-tasklist-error"; //NOI18N
    private static final String TASKLIST_WARNING = "nb-tasklist-warning"; //NOI18N
    
    public JPAVerificationTaskProvider() {
        super(NbBundle.getMessage(JPAVerificationTaskProvider.class, "LBL_TaskList_DisplayName"),
                NbBundle.getMessage(JPAVerificationTaskProvider.class, "LBL_TaskList_Desc"),
                null);
    }
    
    public List<? extends Task> scan(FileObject file) {
        JavaSource javaSrc = null;
        try {
            javaSrc = JavaSource.forFileObject(file);
        } catch (IllegalArgumentException ie) {
            // Do not try to process an invalid FileObject
            JPAProblemFinder.LOG.log(Level.WARNING, ie.getMessage(), ie);
            return null;
        }
        
        ProblemFinderCompControl compControl = new ProblemFinderCompControl(file);
        
        if (javaSrc != null){
            try{
                javaSrc.runUserActionTask(compControl, true);
            } catch (IOException e){
                JPAProblemFinder.LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
        
        List<Task> tasks = new ArrayList<Task>();
        
        for (ErrorDescription error : compControl.getProblemsFound()){
            try{
                Task task = Task.create(file,
                        severityToTaskListString(error.getSeverity()),
                        error.getDescription(),
                        error.getRange().getBegin().getLine());
                
                tasks.add(task);
            } catch (IOException e){
                JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        return tasks;
    }
    
    public void attach(Callback callback) {
    }
    
    private static String severityToTaskListString(Severity severity){
        if (severity == Severity.ERROR){
            return TASKLIST_ERROR;
        }
        
        return TASKLIST_WARNING;
    }
}
