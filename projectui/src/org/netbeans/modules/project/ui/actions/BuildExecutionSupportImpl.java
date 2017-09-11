/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
                    if (list.contains(ai)) {
                        list.remove(ai);
                    }
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
