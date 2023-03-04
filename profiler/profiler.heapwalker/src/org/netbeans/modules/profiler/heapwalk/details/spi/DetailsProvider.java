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
package org.netbeans.modules.profiler.heapwalk.details.spi;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "BrowserUtils_Loading=<loading content...>"                                   // NOI18N
})
public abstract class DetailsProvider {
    
    // [Event Dispatch Thread / Worker Thread] List of supported classes, null for all
    public String[] getSupportedClasses() {
        return null;
    }
    
    // [Worker Thread] Short string representing the instance
    public String getDetailsString(String className, Instance instance, Heap heap) {
        return null;
    }
    
    // [Event Dispatch Thread] UI to visualize the selected instance
    public View getDetailsView(String className, Instance instance, Heap heap) {
        return null;
    }
    
    
    public abstract static class Basic extends DetailsProvider {
        
        private final String[] supportedClasses;
        
        // Use to register for all classes
        public Basic() {
            this((String[])null);
        }
        
        // Use to register for defined classes
        protected Basic(String... supportedClasses) {
            this.supportedClasses = supportedClasses;
        }
        
        public final String[] getSupportedClasses() {
            return supportedClasses;
        }
        
    }
    
    
    public abstract static class View extends JPanel {
        
        private RequestProcessor.Task workerTask;
        private Instance instance;
        private Heap heap;
        
        // [Event Dispatch Thread] Constructor for default initial UI ("<loading content...>")
        protected View(Instance instance, Heap heap) {
            this(instance, heap, initialView());
        }
        
        private static JComponent initialView() {
            JLabel loading = new JLabel(Bundle.BrowserUtils_Loading(), JLabel.CENTER);
            loading.setEnabled(false);
            
            JPanel loadingContainer = new JPanel(new BorderLayout());
            loadingContainer.setOpaque(true);
            loadingContainer.setBackground(UIUtils.getProfilerResultsBackground());
            loadingContainer.setEnabled(false);
            loadingContainer.add(loading, BorderLayout.CENTER);
            
            return loadingContainer;
        }
        
        // [Event Dispatch Thread] Constructor for custom initial UI
        protected View(Instance instance, Heap heap, Component initialView) {
            super(new BorderLayout());
            add(initialView, BorderLayout.CENTER);
            
            this.instance = instance;
            this.heap = heap;
        }
        
        // [Worker Thread] Compute the view here, check Thread.interrupted(),
        // use SwingUtilities.invokeLater() to display the result
        protected abstract void computeView(Instance instance, Heap heap);
        
        public final void addNotify() {
            super.addNotify();
            
            // #241316, this can't be called from constructor!
            workerTask = BrowserUtils.performTask(new Runnable() {
                public void run() {
                    if (!Thread.interrupted()) computeView(instance, heap);
                }
            });
        }
        
        // [Event Dispatch Thread] Do any cleanup here if needed
        protected void removed() {}
        
        public final void removeNotify() {
            workerTask.cancel();
            super.removeNotify();
            removed();
        }
        
    }
    
}
