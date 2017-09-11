/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
