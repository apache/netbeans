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
