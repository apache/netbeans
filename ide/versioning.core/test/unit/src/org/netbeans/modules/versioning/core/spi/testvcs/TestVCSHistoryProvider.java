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
package org.netbeans.modules.versioning.core.spi.testvcs;

import java.awt.event.ActionEvent;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class TestVCSHistoryProvider implements VCSHistoryProvider, VCSHistoryProvider.RevisionProvider {
    public static final String FILE_PROVIDES_REVISIONS_SUFFIX = "providesRevisions";
    public static TestVCSHistoryProvider instance;
    
    public boolean revisionProvided = false;
    public static HistoryEntry[] history;
    
    public TestVCSHistoryProvider() {
        instance = this;
    }
    

    public static void reset() {
        instance.history = null;
        instance.revisionProvided = false;
    }
    
    @Override
    public HistoryEntry[] getHistory(VCSFileProxy[] files, Date fromDate) {
        if(files[0].getName().endsWith(FILE_PROVIDES_REVISIONS_SUFFIX)) {
            return new VCSHistoryProvider.HistoryEntry[] {
                new VCSHistoryProvider.HistoryEntry(
                    files, 
                    new Date(System.currentTimeMillis()), 
                    "msg", 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[] {new HistoryAwareAction()}, 
                    this)};
        }
        return history;
    }

    @Override
    public Action createShowHistoryAction(VCSFileProxy[] files) {
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
    public void getRevisionFile(VCSFileProxy originalFile, VCSFileProxy revisionFile) {
        revisionProvided = true;
    }
    
    private class HistoryAwareAction extends AbstractAction implements ContextAwareAction {
        private Lookup context;
        @Override
        public void actionPerformed(ActionEvent e) {}
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            this.context = actionContext;
            return this;
        }
    }
    
}
