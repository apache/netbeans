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

package org.netbeans.lib.profiler.ui.monitor;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager;
import org.netbeans.lib.profiler.ui.graphs.CPUGraphPanel;
import org.netbeans.lib.profiler.ui.graphs.GraphPanel;
import org.netbeans.lib.profiler.ui.graphs.MemoryGraphPanel;
import org.netbeans.lib.profiler.ui.graphs.SurvivingGenerationsGraphPanel;
import org.netbeans.lib.profiler.ui.graphs.ThreadsGraphPanel;

/**
 *
 * @author Jiri Sedlacek
 */
public class MonitorView extends JPanel {
    
    private VMTelemetryModels models;
    
    private GraphPanel cpuPanel;
    private GraphPanel memoryPanel;
    private GraphPanel gcPanel;
    private GraphPanel threadsPanel;
    
    
    public MonitorView(VMTelemetryDataManager dataManager) {
        initUI(dataManager);
    }
    
    
    public void setupCPUView(boolean visible) {
        cpuPanel.setVisible(visible);
    }
    
    public void setupMemoryView(boolean visible) {
        memoryPanel.setVisible(visible);
    }
    
    public void setupGCView(boolean visible) {
        gcPanel.setVisible(visible);
    }
    
    public void setupThreadsView(boolean visible) {
        threadsPanel.setVisible(visible);
    }
    
    
    public void cleanup() {
        cpuPanel.cleanup();
        memoryPanel.cleanup();
        gcPanel.cleanup();
        threadsPanel.cleanup();
        
        models.cleanup();
    }
    
    
    private void initUI(VMTelemetryDataManager dataManager) {
        setLayout(new GraphsLayout());
        
        models = new VMTelemetryModels(dataManager);
        
        cpuPanel = CPUGraphPanel.createBigPanel(models);
        add(cpuPanel);
        
        memoryPanel = MemoryGraphPanel.createBigPanel(models);
        add(memoryPanel);
        
        gcPanel = SurvivingGenerationsGraphPanel.createBigPanel(models);
        add(gcPanel);
        
        threadsPanel = ThreadsGraphPanel.createBigPanel(models);
        add(threadsPanel);
    }
    
    
    private class GraphsLayout implements LayoutManager {

        public void addLayoutComponent(String name, Component comp) {}

        public void removeLayoutComponent(Component comp) {}

        public Dimension preferredLayoutSize(Container parent) { return new Dimension(); }

        public Dimension minimumLayoutSize(Container parent) { return new Dimension(); }

        public void layoutContainer(Container parent) {
            int w = getWidth();
            int h = getHeight();
            
            boolean c = cpuPanel.isVisible();
            boolean m = memoryPanel.isVisible();
            boolean g = gcPanel.isVisible();
            boolean t = threadsPanel.isVisible();
            
            int h1 = c || m ? (g || t ? h / 2 : h) : 0;
            int h2 = h - h1;
            
            if (h1 > 0) {
                if (c && m) {
                    int w1 = w / 2;
                    int w2 = w - w1;
                    cpuPanel.setBounds(0, 0, w1, h1);
                    memoryPanel.setBounds(w1, 0, w2, h1);
                } else if (c) {
                    cpuPanel.setBounds(0, 0, w, h1);
                } else {
                    memoryPanel.setBounds(0, 0, w, h1);
                }
            }
            
            if (h2 > 0) {
                if (g && t) {
                    int w1 = w / 2;
                    int w2 = w - w1;
                    gcPanel.setBounds(0, h1, w1, h2);
                    threadsPanel.setBounds(w1, h1, w2, h2);
                } else if (g) {
                    gcPanel.setBounds(0, h1, w, h2);
                } else {
                    threadsPanel.setBounds(0, h1, w, h2);
                }
            }
        }
        
    }
    
}
