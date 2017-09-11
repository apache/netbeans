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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.projectsui;

import org.netbeans.modules.debugger.jpda.projectsui.MainProjectManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin Entlicher
 */
public class SourcesTabs extends JTabbedPane {

    private PropertyChangeListener  mainProjectListener;
    private DebuggerManagerListener debuggerListener;

    public SourcesTabs() {
        initComponent();
        mainProjectListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Project p = MainProjectManager.getDefault().getMainProject();
                setProjectTitle(p);
            }
        };
        MainProjectManager.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(mainProjectListener, MainProjectManager.getDefault()));

        debuggerListener = new DebuggerManagerAdapter() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                    Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
                    setSessionTitle(s);
                }
            }
        };
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                WeakListeners.create(DebuggerManagerListener.class, debuggerListener,
                                             DebuggerManager.getDebuggerManager()));
        setSessionTitle(DebuggerManager.getDebuggerManager().getCurrentSession());
    }

    private void initComponent() {
        // Will have two tabbs - one with the current sources, one for remote connection
        addTab(NbBundle.getMessage(SourcesTabs.class, "LBL_SourcesTabbs.Current"), new JLabel("Current"));
        addTab(NbBundle.getMessage(SourcesTabs.class, "LBL_SourcesTabbs.Remote"), new JLabel("Remote"));

    }

    private void setProjectTitle(Project p) {
        if (p == null) {
            setTitleAt(0, NbBundle.getMessage(SourcesTabs.class, "LBL_SourcesTabbs.Current"));
        } else {
            ProjectInformation pi = ProjectUtils.getInformation(p);
            setTitleAt(0, pi.getDisplayName());
        }
    }

    private void setSessionTitle(Session s) {
        if (s != null) {
            //getTabComponentAt(1).setVisible(false);
            setTitleAt(0, s.getName());
            setEnabledAt(1, false);
        } else {
            setProjectTitle(MainProjectManager.getDefault().getMainProject());
            setEnabledAt(1, true);
        }
    }

}
