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
package org.netbeans.modules.cnd.lsp.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * LSPServerSupport listens for changes of status in ClangdProcess. An instance
 * of this class is included in MakeProject Lookup.
 *
 * @author antonio
 */
public class LSPServerSupport
        implements PropertyChangeListener {

    /**
     * We can't make LSPServerSupport extends ProjectOpenedHook directly,
     * because classes that extend ProjectOpenedHook are filtered out of a
     * MakeProject lookup.
     */
    public static final class LSPServerSupportOpenedHook
            extends ProjectOpenedHook {

        private final LSPServerSupport support;

        public LSPServerSupportOpenedHook(LSPServerSupport support) {
            this.support = support;
        }

        @Override
        protected void projectOpened() {
            ClangdProcess.getInstance().addPropertyChangeListener(support);
        }

        @Override
        protected void projectClosed() {
            ClangdProcess.getInstance().removePropertyChangeListener(support);
        }

    }

    /**
     * The property name that indicates that clangd LSP server state changed.
     */
    public static final String PROP_CLANGD_PROCESS_STATE = "clangd.state"; // NOI18N

    private final SwingPropertyChangeSupport propertyChangeSupport;
    private final LSPServerSupportOpenedHook openedHook;

    public LSPServerSupport(MakeProject owner) {
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this, true);
        this.openedHook = new LSPServerSupportOpenedHook(this);
    }

    public ProjectOpenedHook getOpenedHook() {
        return openedHook;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (ClangdProcess.getInstance().equals(evt.getSource())) {
            propertyChangeSupport.firePropertyChange(PROP_CLANGD_PROCESS_STATE,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
    }

}
