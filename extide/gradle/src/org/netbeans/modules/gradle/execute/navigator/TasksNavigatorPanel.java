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
package org.netbeans.modules.gradle.execute.navigator;

import org.netbeans.modules.gradle.GradleDataObject;
import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
@NbBundle.Messages("TASKS_NAME=Related Tasks")
@NavigatorPanel.Registrations({
    @NavigatorPanel.Registration(mimeType = GradleDataObject.KOTLIN_MIME_TYPE, position = 250, displayName = "#TASKS_NAME"),
    @NavigatorPanel.Registration(mimeType = GradleDataObject.MIME_TYPE, position = 250, displayName = "#TASKS_NAME"),
    @NavigatorPanel.Registration(mimeType = "application/gradle-project", position = 250, displayName = "#TASKS_NAME")
})
public class TasksNavigatorPanel implements NavigatorPanel {

    private TasksPanel component;

    protected Lookup.Result<DataObject> selection;

    protected final LookupListener selectionListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            if (selection == null) {
                return;
            }
            navigate(selection.allInstances());
        }
    };

    @Override
    public String getDisplayName() {
        return Bundle.TASKS_NAME();
    }

    @Override
    @NbBundle.Messages("TASKS_HINT=View what tasks are available based on current Gradle project")
    public String getDisplayHint() {
        return Bundle.TASKS_HINT();
    }

    @Override
    public JComponent getComponent() {
        return getNavigatorUI();
    }

    private TasksPanel getNavigatorUI() {
        if (component == null) {
            component = new TasksPanel();
        }
        return component;
    }

    @Override
    public void panelActivated(Lookup context) {
        selection = context.lookupResult(DataObject.class);
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }

    @Override
    public void panelDeactivated() {
        if (selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }
        getNavigatorUI().release();
    }

    @Override
    public Lookup getLookup() {
        return null;
    }

    /**
     *
     * @param selectedFiles
     */
    public void navigate(Collection<? extends DataObject> selectedFiles) {
        if (selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            getNavigatorUI().navigate(d);
        } else {
            getNavigatorUI().release();
        }
    }

}
