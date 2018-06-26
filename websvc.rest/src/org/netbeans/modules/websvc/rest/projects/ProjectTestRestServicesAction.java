/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.websvc.rest.projects;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/** 
 * Action for Test RESTful Web Services
 * @author Nam Nguyen
 */
public class ProjectTestRestServicesAction extends AbstractAction implements Presenter.Menu, ContextAwareAction, LookupListener {

    private String command;
    private ProjectActionPerformer performer;
    private String namePattern;
    private String presenterName;
    private JMenuItem menuPresenter;
    private Lookup lookup;
    private Class<?>[] watch;
    private Lookup.Result results[];
    private boolean needsRefresh = true;
    private boolean initialized = false;
    private boolean refreshing = false;

    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public ProjectTestRestServicesAction() {
        this(null);
    }

    private ProjectTestRestServicesAction(Lookup lookup) {
        if (lookup == null) {
            lookup = Utilities.actionsGlobalContext();
        }
        this.lookup = lookup;
        watch = new Class[]{Project.class, DataObject.class};
        command = RestSupport.COMMAND_TEST_RESTBEANS;
        presenterName = NbBundle.getMessage(ProjectTestRestServicesAction.class, 
                "LBL_TestRestBeansAction_Name");
        setDisplayName(presenterName);
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }

    protected final void setDisplayName(String name) {
        putValue(NAME, name);
    }

    protected void actionPerformed(Lookup context) {
        Project[] projects = Utils.getProjectsFromLookup(context);
        if (projects.length == 1) {
            Utils.testRestWebService(projects[0]);
        }

    }

    protected void refresh(Lookup context) {
        Project[] projects = Utils.getProjectsFromLookup(context);

        if (projects.length == 1) {
            RestSupport restSupport = projects[0].getLookup().lookup(RestSupport.class);
            if (restSupport == null) {
                setEnabled(false);
            } else {
                setEnabled(restSupport.isRestSupportOn());
            }
        } else {
            setEnabled(false);
        }

        setLocalizedTextToMenuPresented(presenterName);
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }

    protected final void setLocalizedTextToMenuPresented(String presenterName) {
        if (menuPresenter != null) {
            Mnemonics.setLocalizedText(menuPresenter, presenterName);
        }
    }
    // Implementation of Presenter.Menu ----------------------------------------
    public JMenuItem getMenuPresenter() {
        if (menuPresenter == null) {
            menuPresenter = new JMenuItem(this);

            Icon icon = null;
            // ignore icon if noIconInMenu flag is set
            if (!Boolean.TRUE.equals(getValue("noIconInMenu"))) {
                icon = (Icon) getValue(Action.SMALL_ICON);
            }
            menuPresenter.setIcon(icon);
            Mnemonics.setLocalizedText(menuPresenter, presenterName);
        }

        return menuPresenter;
    }
    // Implementation of ContextAwareAction ------------------------------------
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ProjectTestRestServicesAction(actionContext);
    }

    private void init() {
        if (initialized) {
            return;
        }
        assert EventQueue.isDispatchThread() : "Cannot be called outside EQ!";
        this.results = new Lookup.Result[watch.length];
        // Needs to listen on changes in results
        for (int i = 0; i < watch.length; i++) {
            results[i] = lookup.lookupResult(watch[i]);
            results[i].allItems();
            LookupListener resultListener = WeakListeners.create(LookupListener.class, this, results[i]);
            results[i].addLookupListener(resultListener);
        }
        initialized = true;
    }

    /** Needs to override getValue in order to force refresh
     */
    public Object getValue(String key) {
        init();
        if (needsRefresh) {
            doRefresh();
        }
        return super.getValue(key);
    }

    /** Needs to override isEnabled in order to force refresh
     */
    public boolean isEnabled() {
        init();
        if (needsRefresh) {
            doRefresh();
        }
        return super.isEnabled();
    }

    public final void actionPerformed(ActionEvent e) {
        init();
        actionPerformed(lookup);
    }

    protected final Lookup getLookup() {
        return lookup;
    }

    private void doRefresh() {
        refreshing = true;
        try {
            refresh(lookup);
        } finally {
            refreshing = false;
        }
        needsRefresh = false;
    }

    public void resultChanged(LookupEvent e) {
        if (refreshing) {
            return;
        } else if (getPropertyChangeListeners().length == 0) {
            needsRefresh = true;
        } else {
            doRefresh();
        }
    }
}
