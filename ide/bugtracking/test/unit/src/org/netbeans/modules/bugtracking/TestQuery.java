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
package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.util.HelpCtx;

/**
 *
 * @author tomas
 */
public abstract class TestQuery {

    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTooltip() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private QueryController controller;
    public QueryController getController() {
        if(controller == null) {
            controller = new QueryController() {
                private final PropertyChangeSupport support = new PropertyChangeSupport(TestQuery.this);
                
                @Override
                public boolean providesMode(QueryController.QueryMode mode) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public JComponent getComponent(QueryController.QueryMode mode) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public HelpCtx getHelpCtx() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void opened() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void closed() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public boolean saveChanges(String name) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public boolean discardUnsavedChanges() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {
                    support.addPropertyChangeListener(l);
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {
                    support.removePropertyChangeListener(l);
                }

                @Override
                public boolean isChanged() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
                
            };
        }
        return controller;
    }

    public boolean isSaved() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void refresh() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean canRename() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void rename(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    boolean canRemove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void setIssueContainer(QueryProvider.IssueContainer<TestIssue> c) {
        
    }
    
}
