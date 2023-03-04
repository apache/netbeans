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

package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.web.browser.spi.PageInspectorCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class PageInspectorCustomizerImpl implements PageInspectorCustomizer {

    private ClientSideProject project;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private static RequestProcessor RP = new RequestProcessor(PropertyChangeSupport.class);

    public PageInspectorCustomizerImpl(ClientSideProject project) {
        this.project = project;
        this.project.getEvaluator().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().startsWith(ClientSideProjectConstants.PROJECT_HIGHLIGHT_SELECTION) ||
                        ClientSideProjectConstants.PROJECT_SELECTED_BROWSER.equals(evt.getPropertyName())) {
                    // #232273 - page inspector takes long time to handle this event
                    //           so fire it in separate thread:
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            support.firePropertyChange(PageInspectorCustomizer.PROPERTY_HIGHLIGHT_SELECTION, null, null);
                        }
                    });
                }
            }
        });
    }
    
    @Override
    public boolean isHighlightSelectionEnabled() {
        ClientProjectEnhancedBrowserImplementation cfg = project.getEnhancedBrowserImpl();
        return cfg == null ? true : cfg.isHighlightSelectionEnabled();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

}
