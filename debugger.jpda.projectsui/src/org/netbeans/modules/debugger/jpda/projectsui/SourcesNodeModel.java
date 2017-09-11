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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.ModelEvent;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/SourcesView",
                                 types=NodeModel.class),
    @DebuggerServiceRegistration(path="SourcesView/netbeans-JPDASession/Current",
                                 types=NodeModel.class),
    @DebuggerServiceRegistration(path="SourcesView/netbeans-JPDASession/Remote",
                                 types=NodeModel.class)
})
public class SourcesNodeModel implements NodeModel {

    public static final String SOURCE_ROOT =
        "org/netbeans/modules/debugger/jpda/resources/root";
    public static final String FILTER =
        "org/netbeans/modules/debugger/jpda/resources/Filter";
    
    private final Map<Object, String> pathWithProject = new HashMap<Object, String>();
    private final RequestProcessor rp = new RequestProcessor(SourcesNodeModel.class.getName());
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    
    @Override
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getMessage(SourcesNodeModel.class, "CTL_SourcesModel_Column_Name_Name");
        } else
        if (o instanceof String) {
            String dn;
            synchronized (pathWithProject) {
                dn = pathWithProject.get(o);
            }
            if (dn == null) {
                computePathWithProject(o);
                return (String) o;
            } else {
                return dn;
            }
        } else {
            throw new UnknownTypeException (o);
        }
    }
    
    private void computePathWithProject(final Object o) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                String dn;
                File f = new File ((String) o);
                if (f.exists ()) {
                    FileObject fo = FileUtil.toFileObject (f);
                    Project p = FileOwnerQuery.getOwner (fo);
                    if (p != null) {
                        ProjectInformation pi = (ProjectInformation) ProjectUtils.getInformation(p);
                        if (pi != null) {
                            dn = NbBundle.getMessage(SourcesNodeModel.class, "CTL_SourcesModel_Column_Name_ProjectSources",
                                                     f.getPath(), pi.getDisplayName());
                        } else {
                            dn = NbBundle.getMessage(SourcesNodeModel.class, "CTL_SourcesModel_Column_Name_LibrarySources",
                                                     f.getPath());
                        }
                    } else {
                        dn = NbBundle.getMessage(SourcesNodeModel.class, "CTL_SourcesModel_Column_Name_LibrarySources",
                                                 f.getPath());
                    }
                } else {
                    dn = (String) o;
                }
                synchronized (pathWithProject) {
                    pathWithProject.put(o, dn);
                }
                fireNodeChanged(o);
            }
        });
    }
    
    @Override
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getMessage(SourcesNodeModel.class, "CTL_SourcesModel_Column_Name_Desc");
        }
        if (o instanceof String) {
            if (((String) o).startsWith ("D")) {
                return NbBundle.getMessage(SourcesNodeModel.class, "CTL_SourcesModel_Column_Name_DescExclusion");
            } else {
                return (String) o;
            }//NbBundle.getBundle(SourcesNodeModel.class).getString("CTL_SourcesModel_Column_Name_DescRoot");
        } else {
            throw new UnknownTypeException (o);
        }
    }
    
    @Override
    public String getIconBase (Object o) throws UnknownTypeException {
        return null;
        /*if (o instanceof String) {
            if (((String) o).startsWith ("D"))
                return FILTER;
            else
                return SOURCE_ROOT;
        } else
        throw new UnknownTypeException (o);*/
    }
    
    private void fireNodeChanged(Object node) {
        ModelEvent me = new ModelEvent.NodeChanged(this, node, ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        for (ModelListener l : listeners) {
            l.modelChanged(me);
        }
    }

    @Override
    public void addModelListener (ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener (ModelListener l) {
        listeners.remove(l);
    }
}
