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

package org.netbeans.modules.maven.execute.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.modules.maven.api.Constants;

import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.execute.navigator.Bundle.*;

/**
 *
 * @author mkleint
 */
@NavigatorPanel.Registrations({
    @NavigatorPanel.Registration(mimeType=Constants.POM_MIME_TYPE, position=250, displayName="#GOALS_NAME"),
    @NavigatorPanel.Registration(mimeType="application/maven-project", position=250, displayName="#GOALS_NAME")
})
public class GoalsNavigationPanel implements NavigatorPanel {
    private GoalsPanel component;
    
    protected Lookup.Result<DataObject> selection;

    protected final LookupListener selectionListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            if(selection == null)
                return;
            navigate(selection.allInstances());
        }
    };
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(GoalsNavigationPanel.class, "GOALS_NAME");
    }

    @Override
    @NbBundle.Messages("GOALS_HINT=View what goals are available based on content of the pom.xml")
    public String getDisplayHint() {
        return GOALS_HINT();
    }

    @Override
    public JComponent getComponent() {
        return getNavigatorUI();
    }
    
    private GoalsPanel getNavigatorUI() {
        if (component == null) {
            component = new GoalsPanel();
        }
        return component;
    }

    @Override
    public void panelActivated(Lookup context) {
        getNavigatorUI().showWaitNode();
        selection = context.lookupResult(DataObject.class);
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    @Override
    public void panelDeactivated() {
        getNavigatorUI().showWaitNode();
        if(selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }
        getNavigatorUI().release();
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
    /**
     * 
     * @param selectedFiles 
     */

    public void navigate(Collection<? extends DataObject> selectedFiles) {
        if(selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            getNavigatorUI().navigate(d);           
        } else {
            getNavigatorUI().release();
        }
    }
}
