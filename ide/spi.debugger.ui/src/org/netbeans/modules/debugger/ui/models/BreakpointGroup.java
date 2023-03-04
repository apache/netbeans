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

package org.netbeans.modules.debugger.ui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Entlicher
 */
public class BreakpointGroup {

    static enum Group { NO, CUSTOM, LANGUAGE, TYPE, PROJECT, FILE, ENGINE, NESTED }

    static final String PROP_FROM_OPEN_PROJECTS = "fromOpenProjects";       // NOI18N
    static final String PROP_FROM_CURRENT_SESSION_PROJECTS = "fromCurrentSessionProjects";  // NOI18N

    private BreakpointGroup parent;
    private String name;
    private Object id;
    private Group group;
    private List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
    private List<BreakpointGroup> groups = new ArrayList<BreakpointGroup>();

    BreakpointGroup(Object id, String name, Group group) {
        this.id = id;
        this.name = name;
        this.group = group;
    }
    
    Object getId() {
        return id;
    }

    String getName() {
        return name;
    }

    Group getGroup() {
        return group;
    }

    List<BreakpointGroup> getSubGroups() {
        if (groups == null || groups.size() == 0) {
            return Collections.emptyList();
        }
        return groups;
    }

    List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    private void addBreakpoint(Breakpoint b) {
        breakpoints.add(b);
    }

    private void addGroup(BreakpointGroup bg) {
        groups.add(bg);
        if (bg.parent != null) {
            throw new IllegalStateException("Group "+bg+" already has parent "+bg.parent);
        }
        bg.parent = this;
    }

    Object[] getGroupsAndBreakpoints() {
        List groupsAndBreakpoints = new ArrayList(groups.size() + breakpoints.size());
        groupsAndBreakpoints.addAll(groups);
        groupsAndBreakpoints.addAll(breakpoints);
        return groupsAndBreakpoints.toArray();
    }
    
    /*List<Breakpoint> getBreakpoints() {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
            ArrayList l = new ArrayList();
            int i, k = bs.length;
            for (i = 0; i < k; i++) {
                String gn = bs[i].getGroupName();
                if (gn.equals("")) {
                    l.add (bs [i]);
                } else {
                    if (!l.contains(gn)) {
                        l.add(gn);
                    }
                }
            }
    }*/

    static Object[] createGroups(Properties props, Set<Breakpoint> closedProjectsBreakpoints) {
        //props.addPropertyChangeListener(null);
        String[] groupNames = (String[]) props.getArray("Grouping", new String[] { Group.CUSTOM.name() });
        boolean openProjectsOnly = props.getBoolean(PROP_FROM_OPEN_PROJECTS, true);
        boolean sessionProjectsOnly = props.getBoolean(PROP_FROM_CURRENT_SESSION_PROJECTS, true);
        Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
        if (groupNames.length == 0 || groupNames[0].equals(Group.NO.name())) {
            return bs;
        }
        Group[] gs = new Group[groupNames.length];
        Map<Object, BreakpointGroup> gm = new HashMap<Object, BreakpointGroup>();
        //Map<Group, Set<BreakpointGroup>> groupSets = new HashMap<Group, Set<BreakpointGroup>>();
        for (int gi = 0; gi < groupNames.length; gi++) {
            //Group g = Group.valueOf(groupNames[gi]);
            gs[gi] = Group.valueOf(groupNames[gi]);
            //gm[gi] = new HashMap<Object, BreakpointGroup>();
            //groupSets.put(g, new HashSet<BreakpointGroup>());
        }
        //List groupsAndBreakpoints = new ArrayList();
        List<BreakpointGroup> groups = new ArrayList<BreakpointGroup>();
        List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
        List ids = new ArrayList();
        List<BreakpointGroup> parentGroups = new ArrayList<BreakpointGroup>();
        List<BreakpointGroup> rootGroups = new ArrayList<BreakpointGroup>();

        Set<Project> sessionProjects;
        if (sessionProjectsOnly) {
            // TODO: Perhaps, better, ask for the session breakpoints somehow directly
            sessionProjects = getCurrentSessionProjects();
        } else {
            sessionProjects = null;
        }
        
        Set<Project> openedProjectsCache = new HashSet<Project>(); // projects opened or subprojects of opened
        Set<Project> closedProjectsCache = new HashSet<Project>(); // projects closed and not subprojects of opened
        Map<Project, Set<? extends Project>> subProjects = new HashMap<Project, Set<? extends Project>>();
        for (int bi = 0; bi < bs.length; bi++) {
            Breakpoint b = bs[bi];
            Breakpoint.GroupProperties bprops = b.getGroupProperties();
            if (bprops != null) {
                if (bprops.isHidden()) {
                    continue;
                }
                if (openProjectsOnly && !isOpened(bprops.getProjects(), openedProjectsCache, closedProjectsCache, subProjects)) {
                    closedProjectsBreakpoints.add(b);
                    Breakpoint.VALIDITY validity = b.getValidity();
                    if (!Breakpoint.VALIDITY.VALID.equals(validity)) {
                        continue;
                    } // Include valid breakpoints anyway
                }
                if (sessionProjects != null && !contains(sessionProjects, bprops.getProjects())) {
                    continue;
                }
            }
            parentGroups.clear();
            rootGroups.clear();
            ids.clear();
            for (int gi = 0; gi < gs.length; gi++) {
                Group g = gs[gi];
                //Set gSet = groupSets.get(g);
                String propertyName = null;
                Object id = null;
                String[] propertyNames = null;
                Object[] idz = null;
                switch (g) {
                    case CUSTOM:
                        propertyName = b.getGroupName();
                        if (propertyName != null && propertyName.length() == 0) {
                            propertyName = null;
                        }
                        id = propertyName;
                        break;
                    case FILE:
                        if (bprops != null) {
                            FileObject[] files = bprops.getFiles();
                            if (files != null && files.length > 0) {
                                if (files.length == 1) {
                                    propertyName = files[0].getPath();
                                    id = files[0];
                                } else {
                                    propertyNames = new String[files.length];
                                    idz = files;
                                    for (int i = 0; i < files.length; i++) {
                                        propertyNames[i] = files[i].getPath();
                                    }
                                }
                            }
                        }
                        break;
                    case LANGUAGE:
                        if (bprops != null) {
                            id = propertyName = bprops.getLanguage();
                        }
                        break;
                    case PROJECT:
                        if (bprops != null) {
                            Project[] prjs = bprops.getProjects();
                            if (prjs != null && prjs.length > 0) {
                                if (prjs.length == 1) {
                                    if (prjs[0] != null) {
                                        propertyName = ProjectUtils.getInformation(prjs[0]).getDisplayName();
                                        id = prjs[0];
                                    }
                                } else {
                                    propertyNames = new String[prjs.length];
                                    idz = prjs;
                                    for (int i = 0; i < prjs.length; i++) {
                                        if (prjs[i] == null) {
                                            propertyNames = null;
                                            idz = null;
                                            break;
                                        }
                                        propertyNames[i] = ProjectUtils.getInformation(prjs[i]).getDisplayName();
                                    }
                                }
                            }
                        }
                        break;
                    case ENGINE:
                        if (bprops != null) {
                            DebuggerEngine[] es = bprops.getEngines();
                            if (es != null && es.length > 0) {
                                if (es.length == 1) {
                                    propertyName = getName(es[0]);
                                    id = es[0];
                                } else {
                                    propertyNames = new String[es.length];
                                    idz = es;
                                    for (int i = 0; i < es.length; i++) {
                                        propertyNames[i] = getName(es[i]);
                                    }
                                }
                            }
                        }
                        break;
                    case TYPE:
                        if (bprops != null) {
                            id = propertyName = bprops.getType();
                        }
                        break;
                }

                if (parentGroups.isEmpty()) {
                    if (id != null) {
                        BreakpointGroup bg = gm.get(id);
                        if (bg == null) {
                            bg = new BreakpointGroup(id, propertyName, g);
                            gm.put(id, bg);
                        }
                        parentGroups.add(bg);
                    } else if (idz != null) {
                        for (int i = 0; i < idz.length; i++) {
                            BreakpointGroup bg = gm.get(idz[i]);
                            if (bg == null) {
                                bg = new BreakpointGroup(idz[i], propertyNames[i], g);
                                gm.put(idz[i], bg);
                            }
                            parentGroups.add(bg);
                        }
                    }
                } else {    // Add them all under every parent group
                    if (id != null) {
                        for (int i = 0; i < parentGroups.size(); i++) {
                            BreakpointGroup pg = parentGroups.get(i);
                            Object key = new NestedGroupKey(pg, id);
                            BreakpointGroup bg = gm.get(key);
                            if (bg == null) {
                                bg = new BreakpointGroup(id, propertyName, g);
                                pg.addGroup(bg);
                                gm.put(key, bg);
                            }
                            parentGroups.set(i, bg);
                        }
                    } else if (idz != null) {
                        List<BreakpointGroup> newParentGroups = new ArrayList<BreakpointGroup>(parentGroups.size() * idz.length);
                        for (BreakpointGroup pg : parentGroups) {
                            for (int i = 0; i < idz.length; i++) {
                                Object key = new NestedGroupKey(pg, idz[i]);
                                BreakpointGroup bg = gm.get(key);
                                if (bg == null) {
                                    bg = new BreakpointGroup(idz[i], propertyNames[i], g);
                                    pg.addGroup(bg);
                                    gm.put(key, bg);
                                }
                                newParentGroups.add(bg);
                            }
                        }
                        parentGroups.clear();
                        parentGroups.addAll(newParentGroups);
                    }
                }

                /*
                BreakpointGroup bg = null;
                List<BreakpointGroup> bgs = null;
                if (id != null) {
                    ids.add(id);
                    Object key;
                    if (ids.size() > 1) {
                        key = new NestedGroupKey(ids.toArray());
                    } else {
                        key = id;
                    }
                    bg = gm.get(key);
                    if (bg == null) {
                        bg = new BreakpointGroup(id, propertyName, g);
                        gm.put(key, bg);
                    }
                } else if (idz != null) {
                    bgs = new ArrayList<BreakpointGroup>(idz.length);
                    
                }

                if (parentGroups.isEmpty()) {
                    if (bg != null) {
                        parentGroups.add(bg);
                    } else if (bgs != null) {
                        
                    }
                } else if (bg != null) {
                    parentGroup.addGroup(bg);
                    parentGroup = bg;
                }
                 */
                if (rootGroups.isEmpty()) {
                    rootGroups.addAll(parentGroups);
                }
            }
            if (parentGroups.isEmpty()) {
                breakpoints.add(b);
            } else {
                for (BreakpointGroup rg : rootGroups) {
                    if (!groups.contains(rg)) {
                        groups.add(rg);
                    }
                }
                for (BreakpointGroup pg : parentGroups) {
                    pg.addBreakpoint(b);
                }
            }
        }
        List groupsAndBreakpoints = new ArrayList(groups.size() + breakpoints.size());
        groupsAndBreakpoints.addAll(groups);
        groupsAndBreakpoints.addAll(breakpoints);
        return groupsAndBreakpoints.toArray();
    }

    private static boolean isOpened(Project[] projects,
                                    Set<Project> openedProjectsCache,
                                    Set<Project> closedProjectsCache,
                                    Map<Project, Set<? extends Project>> subProjects) {
        if (projects != null && projects.length > 0) {
            for (Project p : projects) {
                if (p == null) {
                    return true;
                }
                if (openedProjectsCache.contains(p)) {
                    return true;
                }
                if (closedProjectsCache.contains(p)) {
                    return false;
                }
                if (OpenProjects.getDefault().isProjectOpen(p) ||
                    isDependentOnAnOpened(p, openedProjectsCache, closedProjectsCache, subProjects)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private static boolean isDependentOnAnOpened(Project p,
                                                 Set<Project> openedProjectsCache,
                                                 Set<Project> closedProjectsCache,
                                                 Map<Project, Set<? extends Project>> subProjects) {
        for (Project op : OpenProjects.getDefault().getOpenProjects()) {
            if (isSubProjectOf(p, op, subProjects, new HashSet<Project>())) {
                openedProjectsCache.add(p);
                return true;
            }
        }
        closedProjectsCache.add(p);
        return false;
    }

    private static boolean isSubProjectOf(Project p, Project op,
                                          Map<Project, Set<? extends Project>> subProjects, // cache of sub-projects
                                          Set<Project> allSubProjects) {                    // all inspected sub-projects
        Set<? extends Project> sps = subProjects.get(op);
        if (sps == null) {
            //mkleint: see subprojectprovider for official contract, see #210465
            SubprojectProvider spp = op.getLookup().lookup(SubprojectProvider.class);
            if (spp == null) {
                return false;
            }
            sps = spp.getSubprojects();
            subProjects.put(op, sps);
        }
        for (Project sp : sps) {
            if (allSubProjects.contains(sp)) {
                continue;
            }
            allSubProjects.add(sp);
            if (p.equals(sp) || isSubProjectOf(p, sp, subProjects, allSubProjects)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(Set<Project> openProjects, Project[] projects) {
        if (projects != null && projects.length > 0) {
            boolean contains = false;
            for (Project p : projects) {
                if (p == null) {
                    return true;
                }
                if (openProjects.contains(p)) {
                    contains = true;
                    break;
                }
            }
            return contains;
        } else {
            return true;
        }
    }

    private static String getName(DebuggerEngine e) {
        Session s = e.lookupFirst(null, Session.class);
        String name = s.getName();
        String[] ls = s.getSupportedLanguages();
        if (ls.length > 1) {
            for (String l : ls) {
                DebuggerEngine en = s.getEngineForLanguage(l);
                if (en == e) {
                    name += "/"+l;
                    break;
                }
            }
        }
        return name;
    }

    private static Set<Project> getCurrentSessionProjects() {
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (currentSession == null) {
            return null;
        }
        List<? extends Project> sessionProjects = currentSession.lookup(null, Project.class);
        if (sessionProjects.size() == 0) {
            return null;
        }
        return new HashSet<Project>(sessionProjects);
    }

    private static final class NestedGroupKey {

        private Object[] ids;

        public NestedGroupKey(Object[] ids) {
            this.ids = ids;
        }

        private static Object[] createIDs(BreakpointGroup g, Object id) {
            List ids = new ArrayList();
            ids.add(id);
            while (g != null) {
                ids.add(0, g.id);
                g = g.parent;
            }
            return ids.toArray();
        }

        public NestedGroupKey(BreakpointGroup g, Object id) {
            this(createIDs(g, id));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NestedGroupKey)) {
                return false;
            }
            return Arrays.equals(((NestedGroupKey) obj).ids, ids);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(ids);
        }

    }


}
