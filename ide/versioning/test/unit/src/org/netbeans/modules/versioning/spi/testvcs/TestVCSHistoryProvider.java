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
package org.netbeans.modules.versioning.spi.testvcs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;

/**
 *
 * @author tomas
 */
public class TestVCSHistoryProvider implements VCSHistoryProvider, VCSHistoryProvider.RevisionProvider, VCSHistoryProvider.ParentProvider {
    public static final String FILE_PROVIDES_REVISIONS_SUFFIX = "providesRevisions";
    public static TestVCSHistoryProvider instance;
    
    public static String PARENT_MSG = "im.the.parent";
    
    public boolean revisionProvided = false;
    public boolean parentrevisionProvided = false;
    public static HistoryEntry[] history;
    
    public TestVCSHistoryProvider() {
        instance = this;
    }
    

    public static void reset() {
        instance.history = null;
        instance.revisionProvided = false;
    }
    
    @Override
    public HistoryEntry[] getHistory(File[] files, Date fromDate) {
        for (File file : files) {
            if(file.getName().endsWith(FILE_PROVIDES_REVISIONS_SUFFIX)) {
                return new VCSHistoryProvider.HistoryEntry[] {
                        new VCSHistoryProvider.HistoryEntry(
                            new File[] {file}, 
                            new Date(System.currentTimeMillis()), 
                            "msg", 
                            "user", 
                            "username", 
                            "12345", 
                            "1234567890", 
                            new Action[0], 
                            this,
                            null, 
                            this)};
                
            }
        }
        return history;
    }

    @Override
    public Action createShowHistoryAction(File[] files) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Override
    public void addHistoryChangeListener(HistoryChangeListener l) {
        
    }

    @Override
    public void removeHistoryChangeListener(HistoryChangeListener l) {
        
    }

    @Override
    public void getRevisionFile(File originalFile, File revisionFile) {
        revisionProvided = true;
    }

    @Override
    public HistoryEntry getParentEntry(File file) {
        return new VCSHistoryProvider.HistoryEntry(
                            new File[] {file}, 
                            new Date(System.currentTimeMillis()), 
                            PARENT_MSG, 
                            "user", 
                            "username", 
                            "12345", 
                            "1234567890", 
                            new Action[0], 
                            this);
}


}
