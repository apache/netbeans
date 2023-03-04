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

package org.netbeans.modules.project.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.uiapi.BuildExecutionSupportImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport.Item;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.project.uiapi.BuildExecutionSupportImplementation.class)
public class BuildExecutionSupportImpl implements BuildExecutionSupportImplementation {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    private final List<IndentityHashCodeWrapper> runningItems = new ArrayList<IndentityHashCodeWrapper>();
    
    private BuildExecutionSupport.Item lastFinishedItem;
    
    //constant for keeping the history list small.
    private static final int HISTORY_MAX = 7;
    private final Map<String, List<BuildExecutionSupport.ActionItem>> historyItems = new HashMap<String, List<BuildExecutionSupport.ActionItem>>();

    public static BuildExecutionSupportImplementation getInstance() {
        return Lookup.getDefault().lookup(BuildExecutionSupportImplementation.class);
    }

    public BuildExecutionSupportImpl() {
        OpenProjects.getDefault().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                    List<Project> opened = Arrays.asList(OpenProjects.getDefault().getOpenProjects());
                    Set<FileObject> fos = new HashSet<FileObject>();
                    for (Project p : opened) {
                        if (p != null) {
                            fos.add(p.getProjectDirectory());
                        }
                    }
                    synchronized (runningItems) {
                        for (List<BuildExecutionSupport.ActionItem> lst : historyItems.values()) {
                            Iterator<BuildExecutionSupport.ActionItem> it = lst.iterator();
                            while (it.hasNext()) {
                                BuildExecutionSupport.ActionItem item = it.next();
                                if (!fos.contains(item.getProjectDirectory())) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void registerFinishedItem(Item item) {
        synchronized (runningItems) {
            lastFinishedItem = item;
            runningItems.remove(new IndentityHashCodeWrapper(item));
            if (item instanceof BuildExecutionSupport.ActionItem) {
                BuildExecutionSupport.ActionItem ai = (BuildExecutionSupport.ActionItem) item;
                String action = ai.getAction();
                assert action != null;
                if (ActionProvider.COMMAND_RUN.equals(action)) { //performance optimization, only remember what we actually use.
                    List<BuildExecutionSupport.ActionItem> list = historyItems.get(action);
                    if (list == null) {
                        list = new ArrayList<BuildExecutionSupport.ActionItem>();
                        historyItems.put(action, list);
                    }
                    list.remove(ai);
                    list.add(ai);
                    if (list.size() > HISTORY_MAX) {
                        list.remove(0);
                    }
                }
            }
        }
        fireChange();
    }

    @Override
    public void registerRunningItem(Item item) {
        synchronized (runningItems) {
            final IndentityHashCodeWrapper indentityHashCodeWrapper = new IndentityHashCodeWrapper(item);
            if (!runningItems.contains(indentityHashCodeWrapper)) {
                runningItems.add(indentityHashCodeWrapper);
            }
        }
        fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public BuildExecutionSupport.Item getLastItem() {
        synchronized (runningItems) {
            return lastFinishedItem;
        }
    }

    @Override
    public List<BuildExecutionSupport.Item> getRunningItems() {
        List<BuildExecutionSupport.Item> items = new ArrayList<BuildExecutionSupport.Item>();
        synchronized (runningItems) {
            for (IndentityHashCodeWrapper wr : runningItems) {
                items.add(wr.item);
            }
        }
        return items;
    }
    /**
     * 
     * @param action
     * @return list of items, first item is the newest one 
     */
    public List<BuildExecutionSupport.ActionItem> getHistoryFor(String action) {
        List<BuildExecutionSupport.ActionItem> items = new ArrayList<BuildExecutionSupport.ActionItem>();
        synchronized (runningItems) {
            List<BuildExecutionSupport.ActionItem> itms = historyItems.get(action);
            if (itms != null) {
                items.addAll(itms);
                Collections.reverse(items);
            }
        }
        return items;
    }

    private void fireChange() {
        List<ChangeListener> lsts;
        synchronized (listeners) {
            lsts = new ArrayList<ChangeListener>(listeners);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener l : lsts) {
            l.stateChanged(event);
        }
    }

    private static class IndentityHashCodeWrapper {
        final BuildExecutionSupport.Item item;

        public IndentityHashCodeWrapper(Item item) {
            this.item = item;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.item != null ? System.identityHashCode(this.item) : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IndentityHashCodeWrapper other = (IndentityHashCodeWrapper) obj;
            if (this.item != other.item) {
                return false;
            }
            return true;
        }
        
    }
}
