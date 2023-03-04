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
package org.netbeans.modules.jshell.navigation;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages("LBL_PanelRegisteredName=Snippets")
@NavigatorPanel.Registration(displayName = "#LBL_PanelRegisteredName", mimeType = "text/x-repl")
public class SnippetNavigationPanel implements NavigatorPanel {
    private SnippetPanelUI ui;
    
    private static SnippetNavigationPanel INSTANCE = null;
    private Lookup context;
    private Lookup.Result<FileObject> lkp;
    
    private LookupListener lookupL = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            navigateToFile(context.lookup(FileObject.class));
        }
    };
    
    public static void navigate(SnippetHandle h) {
        if (INSTANCE != null) {
            INSTANCE.getUI().navigateToHandle(h);
        }
    }
    
    @NbBundle.Messages("LBL_NavigatorPanel=Snippets")
    @Override
    public String getDisplayName() {
        return Bundle.LBL_NavigatorPanel();
    }

    @NbBundle.Messages("HINT_NavigatorPanel=Navigates among the snippets in the current editor window")
    @Override
    public String getDisplayHint() {
        return Bundle.HINT_NavigatorPanel();
    }

    @Override
    public JComponent getComponent() {
        return getUI();
    }
    
    private void navigateToFile(FileObject file) {
        JShellEnvironment env = ShellRegistry.get().get(file);
        if (env == null) {
            return;
        }
        getUI().navigate(env);
    }

    @Override
    public void panelActivated(Lookup context) {
        lkp = context.lookupResult(FileObject.class);
        lkp.addLookupListener(lookupL);
        Collection<? extends FileObject> l = lkp.allInstances();
        FileObject file = l.isEmpty()? null : l.iterator().next();
        navigateToFile(file);
        this.context = context;
        INSTANCE = this;
    }
    
    private SnippetPanelUI getUI() {
        if (ui == null) {
            ui = new SnippetPanelUI();
        }
        return ui;
    }

    @Override
    public void panelDeactivated() {
        if (lkp != null) {
            lkp.removeLookupListener(lookupL);
        }
        INSTANCE = null;
        getUI().unselectAll();
        context = null;
    }

    @Override
    public Lookup getLookup() {
        return getUI().getLookup();
    }
    
}
