/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.core.windows.model;



import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.core.windows.Constants;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.options.TabsPanel.EditorSortType;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
final class DefaultModeModel implements ModeModel {


    /** Programatic name of mode. */
    private String name;
    
    private final Set<String> otherNames = new HashSet<String>(3);

    private final Rectangle bounds = new Rectangle();

    private final Rectangle boundsSeparetedHelp = new Rectangle();

    /** State of mode: split or separate. */
    private /*final*/ int state;
    /** Kind of mode: editor or view. */
    private final int kind;
    
    /** Frame state. */
    private int frameState;

    /** Permanent property. */
    private boolean permanent;
    
    private boolean minimized;

    /** Sub model which manages TopComponents stuff. */
    private final TopComponentSubModel topComponentSubModel;
    
    /** Context of tcx. Lazy initialization, because this will be used only by
     * sliding kind of modes */
    private TopComponentContextSubModel topComponentContextSubModel = null;

    // Locks>>
    /** */
    private final Object LOCK_STATE = new Object();
    /** */
    private final Object LOCK_BOUNDS = new Object();
    /** */
    private final Object LOCK_BOUNDS_SEPARATED_HELP = new Object();
    /** Locks frameState. */
    private final Object LOCK_FRAMESTATE = new Object();
    /** Locks top components. */
    private final Object LOCK_TOPCOMPONENTS = new Object();
    /** Locks tc contexts */
    private final Object LOCK_TC_CONTEXTS = new Object();
    
    
    public DefaultModeModel(String name, int state, int kind, boolean permanent) {
        this.name = name;
        this.state = state;
        this.kind = kind;
        this.permanent = permanent;
        this.topComponentSubModel = new TopComponentSubModel(kind);
    }
    
    /////////////////////////////////////
    // Mutator methods >>
    /////////////////////////////////////
    @Override
    public void setState(int state) {
        synchronized(LOCK_STATE) {
            this.state = state;
        }
    }
    
    @Override
    public void removeTopComponent(TopComponent tc, TopComponent recentTc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeTopComponent(tc, recentTc);
        }
    }
    
    // XXX
    @Override
    public void removeClosedTopComponentID(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeClosedTopComponentID(tcID);
        }
    }
    
    /** Adds opened TopComponent. */
    @Override
    public void addOpenedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addOpenedTopComponent(tc);
            sortOpenedTopComponents();
        }
    }
    
    @Override
    public void insertOpenedTopComponent(TopComponent tc, int index) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.insertOpenedTopComponent(tc, index);
            sortOpenedTopComponents();
        }
    }

    private void sortOpenedTopComponents() {
        if (getKind() == Constants.MODE_KIND_EDITOR) {
            sortByFile(getEditorSortType());
        } else if( getKind() == Constants.MODE_KIND_SLIDING && Switches.isModeSlidingEnabled()) {
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            List<TopComponent> opened = topComponentSubModel.getOpenedTopComponents();
            final List<String> prevModes = new ArrayList<>(opened.size());
            final Map<TopComponent, String> tc2modeName = new HashMap<>(opened.size());
            for (TopComponent tc : opened) {
                String tcId = wm.findTopComponentID(tc);
                if (null == tcId) {
                    continue;
                }
                ModeImpl prevMode = getTopComponentPreviousMode(tcId);
                if (null == prevMode) {
                    continue;
                }
                if (!prevModes.contains(prevMode.getName())) {
                    prevModes.add(prevMode.getName());
                }
                tc2modeName.put(tc, prevMode.getName());
            }

            if (prevModes.isEmpty()) {
                return; //nothing to sort by (shouldn't really happen)
            }
            opened.sort(new Comparator<TopComponent>() {
                @Override
                public int compare(TopComponent o1, TopComponent o2) {
                    String mode1 = tc2modeName.get(o1);
                    String mode2 = tc2modeName.get(o2);
                    if (null == mode1 && null != mode2) {
                        return 1;
                    } else if (null != mode1 && null == mode2) {
                        return -1;
                    }
                    return prevModes.indexOf(mode1) - prevModes.indexOf(mode2);
                }
            });
            topComponentSubModel.setOpenedTopComponents(opened);
        }
    }

    private EditorSortType getEditorSortType() {
        EditorSortType sortType = EditorSortType.None;
        try {
            sortType = EditorSortType.valueOf(WinSysPrefs.HANDLER.get(WinSysPrefs.EDITOR_SORT_TABS, EditorSortType.None.name()));
        } catch (IllegalArgumentException ex) {
            // no-op
        }
        return sortType;
    }

    private void sortByFile(final EditorSortType sortType) {
        if (sortType == EditorSortType.None) {
            return;
        }
        List<TopComponent> openedComponents = topComponentSubModel.getOpenedTopComponents();
        openedComponents.sort(new Comparator<TopComponent>() {
            @Override
            public int compare(TopComponent tc1, TopComponent tc2) {
                FileObject f1 = tc1.getLookup().lookup(FileObject.class);
                FileObject f2 = tc2.getLookup().lookup(FileObject.class);
                if (f1 == null && f2 == null) {
                    return 0;
                } else if (f1 != null && f2 == null) {
                    return 1;
                } else if (f1 == null && f2 != null) {
                    return -1;
                } else {
                    switch (sortType) {
                        case FullFilePath:
                            return compareFullFilePath(f1, f2);
                        case FileName:
                            return compareFileName(f1, f2);
                        case FileNameWithParent:
                            return compareFileNameWithParent(f1, f2);
                        default:
                            throw new AssertionError();
                    }
                }
            }
        });
        topComponentSubModel.setOpenedTopComponents(openedComponents);
    }

    private int compareFullFilePath(FileObject f1, FileObject f2) {
        return toFullFilePath(f1).compareToIgnoreCase(toFullFilePath(f2));
    }

    private String toFullFilePath(FileObject fo) {
        File f = FileUtil.toFile(fo);

        if (f != null) {
            return f.getAbsolutePath();
        }

        FileObject rootFO = FileUtil.getArchiveFile(fo);

        if (rootFO != null) {
            return toFullFilePath(rootFO) + "/" + fo.getPath();
        }

        return fo.toURL().getPath();
    }

    private int compareFileName(FileObject f1, FileObject f2) {
        return f1.getName().compareToIgnoreCase(f2.getName());
    }

    private int compareFileNameWithParent(FileObject f1, FileObject f2) {
        FileObject p1 = f1.getParent();
        FileObject p2 = f2.getParent();
        if (p1 == null && p2 == null) {
            return 0;
        } else if (p1 != null && p2 == null) {
            return 1;
        } else if (p1 == null && p2 != null) {
            return -1;
        } else {
            if (p1.getName().equals(p2.getName())) {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
            return p1.getName().compareToIgnoreCase(p2.getName());
        }
    }

    @Override
    public void addClosedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addClosedTopComponent(tc);
        }
    }
    
    @Override
    public void addUnloadedTopComponent(String tcID, int index) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addUnloadedTopComponent(tcID, index);
        }
    }
    
    @Override
    public void setUnloadedSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedSelectedTopComponent(tcID);
        }
    }
    
    @Override
    public void setUnloadedPreviousSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedPreviousSelectedTopComponent(tcID);
        }
    }
    
    /** Sets seleted TopComponent. */
    @Override
    public void setSelectedTopComponent(TopComponent selected) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setSelectedTopComponent(selected);
        }
    }
    
    @Override
    public void setPreviousSelectedTopComponentID(String prevSelectedId) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setPreviousSelectedTopComponentID(prevSelectedId);
        }
    }

    /** Sets frame state */
    @Override
    public void setFrameState(int frameState) {
        synchronized(LOCK_FRAMESTATE) {
            this.frameState = frameState;
        }
    }

    @Override
    public void setBounds(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS) {
            this.bounds.setBounds(bounds);
        }
    }
    
    @Override
    public void setBoundsSeparatedHelp(Rectangle boundsSeparatedHelp) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            this.boundsSeparetedHelp.setBounds(boundsSeparatedHelp);
        }
    }
    /////////////////////////////////////
    // Mutator methods <<
    /////////////////////////////////////


    /////////////////////////////////////
    // Accessor methods >>
    /////////////////////////////////////
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Rectangle getBounds() {
        synchronized(LOCK_BOUNDS) {
            return (Rectangle)this.bounds.clone();
        }
    }
    
    @Override
    public Rectangle getBoundsSeparatedHelp() {
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            return (Rectangle)this.boundsSeparetedHelp.clone();
        }
    }
    
    @Override
    public int getState() {
        synchronized(LOCK_STATE) {
            return this.state;
        }
    }
    
    @Override
    public int getKind() {
        return this.kind;
    }
    
    /** Gets frame state. */
    @Override
    public int getFrameState() {
        synchronized(LOCK_FRAMESTATE) {
            return this.frameState;
        }
    }
    
    @Override
    public boolean isPermanent() {
        return this.permanent;
    }
    
    @Override
    public void makePermanent() {
        this.permanent = true;
    }
    
    @Override
    public boolean isEmpty() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.isEmpty();
        }
    }
    
    @Override
    public boolean containsTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.containsTopComponent(tc);
        }
    }

    /** Gets list of top components in this workspace. */
    @Override
    public List<TopComponent> getTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponents();
        }
    }


    /** Gets selected TopComponent. */
    @Override
    public TopComponent getSelectedTopComponent() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getSelectedTopComponent();
        }
    }
    /** Gets the ID of top component that was selected before switching to/from maximized mode */
    @Override
    public String getPreviousSelectedTopComponentID() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getPreviousSelectedTopComponentID();
        }
    }

    /** Gets list of top components. */
    @Override
    public List<TopComponent> getOpenedTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponents();
        }
    }

    @Override
    public final void setName(String name) {
        this.name = name;
    }
    
    // XXX
    @Override
    public List<String> getOpenedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponentsIDs();
        }
    }
    
    @Override
    public List<String> getClosedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getClosedTopComponentsIDs();
        }
    }
    
    @Override
    public List<String> getTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponentsIDs();
        }
    }
    
    @Override
    public int getOpenedTopComponentTabPosition (TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponentTabPosition(tc);
        }
    }
    
    @Override
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousConstraints(tcID);
        }
    }
    
    @Override
    public ModeImpl getTopComponentPreviousMode(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousMode(tcID);
        }
    }
    /** Gets the tab index of the top component in its previous mode */
    @Override
    public int getTopComponentPreviousIndex(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousIndex(tcID);
        }
    }
    
    @Override
    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousConstraints(tcID, constraints);
        }
    }
    
    @Override
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int prevIndex) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousMode(tcID, mode, prevIndex);
            sortOpenedTopComponents();
        }
    }
    
    /////////////////////////////////////
    // Accessor methods <<
    /////////////////////////////////////
    
    private TopComponentContextSubModel getContextSubModel() {
        if (topComponentContextSubModel == null) {
            topComponentContextSubModel = new TopComponentContextSubModel();
        }
        return topComponentContextSubModel;
    }

    @Override
    public boolean isMinimized() {
        return minimized;
    }

    @Override
    public void setMinimized( boolean minimized ) {
        this.minimized = minimized;
    }

    @Override
    public Collection<String> getOtherNames() {
        return Collections.unmodifiableSet( otherNames );
    }

    @Override
    public void addOtherName( String otherModeName ) {
        otherNames.add( otherModeName );
    }
}

