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


package org.netbeans.core.windows;



import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.windows.persistence.*;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.windows.*;


/**
 * Class which handles loading, saving of window system and persistence events.
 *
 * @author  Peter Zavadsky, Marek Slama
 */
public final class PersistenceHandler implements PersistenceObserver {

    // Persistence data
    /** Maps mode config name to mode instance. */
    private final Map<String, ModeImpl> name2mode = new WeakHashMap<String, ModeImpl>(10);
    /** Maps group config name to group instance. */
    private final Map<String, TopComponentGroupImpl> name2group = new WeakHashMap<String, TopComponentGroupImpl>(10);

    private static PersistenceHandler defaultInstance;

    private boolean loaded = false;

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(PersistenceHandler.class);

    private final LazyLoader lazyLoader = new LazyLoader();


    /** Creates a new instance of PersistenceHanlder */
    private PersistenceHandler() {
    }

    /**
     * Clears cached data.
     */
    public void clear() {
        name2mode.clear();
        name2group.clear();
        TopComponentTracker.getDefault().clear();
    }

    public static synchronized PersistenceHandler getDefault() {
        if(defaultInstance == null) {
            defaultInstance = new PersistenceHandler();
        }

        return defaultInstance;
    }

    // XXX helper method
    public static boolean isTopComponentPersistentWhenClosed(TopComponent tc) {
        return PersistenceManager.isTopComponentPersistentWhenClosed(tc);
    }

    boolean isLoaded() {
        return loaded;
    }

    public void load() {
        if(DEBUG) {
            debugLog("## PersistenceHandler.load"); // NOI18N
        }

        TopComponentTracker.getDefault().load();

        WindowManagerConfig wmc = null;
        try {
            wmc = PersistenceManager.getDefault().loadWindowSystem();
        } catch (IOException exc) {
            // Serious persistence problem -> try to reset
            Exceptions.attachLocalizedMessage(exc, "Cannot load window system persistent data, user directory content is broken. Resetting to default layout..."); //NOI18N
            Logger.getLogger(PersistenceHandler.class.getName()).log(Level.WARNING, null, exc); // NOI18N
            // try to delete local winsys data and try once more
            try {
                FileObject rootFolder = PersistenceManager.getDefault().getRootLocalFolder();
                if (null != rootFolder) {
                    rootFolder.delete();
                    wmc = PersistenceManager.getDefault().loadWindowSystem();
                } else {
                    Logger.getLogger(PersistenceHandler.class.getName()).log(Level.WARNING, "Cannot even get access to local winsys configuration, using internally predefined configuration."); // NOI18N
                    wmc = ConfigFactory.createDefaultConfig();
                }
            } catch (IOException ioE) {
                Exceptions.attachLocalizedMessage(ioE, "Cannot load even default layout, using internally predefined configuration."); //NOI18N
                Logger.getLogger(PersistenceHandler.class.getName()).log(Level.WARNING, null, ioE);
                wmc = ConfigFactory.createDefaultConfig();
            }
        }

        ToolbarPool.getDefault().setPreferredIconSize(wmc.preferredToolbarIconSize);

        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        if (wmc.tcIdViewList.length > 0) {
            wm.setRecentViewList(wmc.tcIdViewList);
        } else {
            //No recent view list is saved, fill it by opened TopComponents
            List<String> idList = new ArrayList<>();
            for (int i = 0; i < wmc.modes.length; i++) {
                ModeConfig mc = wmc.modes[i];
                for (int j = 0; j < mc.tcRefConfigs.length; j++) {
                    //Only opened
                    if (mc.tcRefConfigs[j].opened) {
                        idList.add(mc.tcRefConfigs[j].tc_id);
                    }
                }
            }
            wm.setRecentViewList(idList.toArray(new String[0]));
        }

        wm.setEditorAreaConstraints(wmc.editorAreaConstraints);
        wm.setEditorAreaState(wmc.editorAreaState);

        ModeImpl activeMode    = null;
        ModeImpl editorMaximizedMode = null;
        ModeImpl viewMaximizedMode = null;

        // First create empty modes.
        Map<ModeImpl, ModeConfig> mode2config = new HashMap<ModeImpl, ModeConfig>();

        for (int i = 0; i < wmc.modes.length; i++) {
            ModeConfig mc = (ModeConfig) wmc.modes[i];
            ModeImpl mode = getModeFromConfig(mc);

            mode2config.put(mode, mc);

            if(mc.name.equals(wmc.activeModeName)) {
                activeMode = mode;
            }
            if(mc.name.equals(wmc.editorMaximizedModeName)) {
                editorMaximizedMode = mode;
            } else if(mc.name.equals(wmc.viewMaximizedModeName)) {
                viewMaximizedMode = mode;
            }
        }

        TopComponentTracker tcTracker = TopComponentTracker.getDefault();
        TopComponent activeTopComponentOverride = null;
        // Then fill them with TopComponents.
        for(Iterator it = mode2config.keySet().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            ModeConfig mc = (ModeConfig)mode2config.get(mode);
            initModeFromConfig(mode, mc, false);
            initPreviousModes(mode, mc, mode2config);

            // Set selected TopComponent.
            if(mc.selectedTopComponentID != null) {
                mode.setUnloadedSelectedTopComponent(mc.selectedTopComponentID);
            }
            if(mc.previousSelectedTopComponentID != null) {
                mode.setUnloadedPreviousSelectedTopComponent(mc.previousSelectedTopComponentID);
            }
            for( String tcId : mode.getTopComponentsIDs() ) {
                tcTracker.add( tcId, mode );
            }
            //some TopComponents want to be always active when the window system starts (e.g. welcome screen)
            for( TopComponent tc : mode.getOpenedTopComponents() ) {
                Object val = tc.getClientProperty( Constants.ACTIVATE_AT_STARTUP );
                if(val instanceof Boolean && ((Boolean) val).booleanValue()) {
                    activeTopComponentOverride = tc;
                    break;
                }
            }
        }

        //Initialize top component groups
        for (int i = 0; i < wmc.groups.length; i++) {
            GroupConfig groupCfg = wmc.groups[i];
            createTopComponentGroupFromConfig(groupCfg);
        }

        // XXX #37188, 40237 Hot fix for the problem with initing active
        // component (even it is not on screen yet).
        if(activeMode != null) {
            TopComponent active = activeMode.getSelectedTopComponent();
            if(active != null) {
                WindowManagerImpl.getInstance().specialPersistenceCompShow(active);
            }
        }
        // active mode can be null, Active mode info is stored in winsys config (system layer) and modes in
        // project layer, that can cause out of synch state when switching projects...
        // setting null is however considered a valid state.
        wm.setActiveMode(activeMode);
        wm.setEditorMaximizedMode(editorMaximizedMode);
        wm.setViewMaximizedMode(viewMaximizedMode);

        Rectangle joinedBounds = computeBounds(
            wmc.centeredHorizontallyJoined,
            wmc.centeredVerticallyJoined,
            wmc.xJoined,
            wmc.yJoined,
            wmc.widthJoined,
            wmc.heightJoined,
            wmc.relativeXJoined,
            wmc.relativeYJoined,
            wmc.relativeWidthJoined,
            wmc.relativeHeightJoined);
        if(joinedBounds != null) {
            wm.setMainWindowBoundsJoined(joinedBounds);
        }
        // PENDING else { ...some default value?

        Rectangle separatedBounds = computeBounds(
            wmc.centeredHorizontallySeparated,
            wmc.centeredVerticallySeparated,
            wmc.xSeparated,
            wmc.ySeparated,
            wmc.widthSeparated,
            wmc.heightSeparated,
            wmc.relativeXSeparated,
            wmc.relativeYSeparated,
            wmc.relativeWidthSeparated,
            wmc.relativeHeightSeparated);
        if(separatedBounds != null) {
            wm.setMainWindowBoundsSeparated(separatedBounds);
        }
        // PENDING else { ...some default value?

        wm.setMainWindowFrameStateJoined(wmc.mainWindowFrameStateJoined);
        wm.setMainWindowFrameStateSeparated(wmc.mainWindowFrameStateSeparated);

        Rectangle absBounds = wmc.editorAreaBounds == null ? new Rectangle() : wmc.editorAreaBounds;
        Rectangle relBounds = wmc.editorAreaRelativeBounds == null ? new Rectangle() : wmc.editorAreaRelativeBounds;
        Rectangle bounds = computeBounds(false, false,
            absBounds.x,
            absBounds.y,
            absBounds.width,
            absBounds.height,
            relBounds.x / 100.0F,
            relBounds.y / 100.0F,
            relBounds.width / 100.0F,
            relBounds.height / 100.0F);
        wm.setEditorAreaBounds(bounds);
        wm.setEditorAreaFrameState(wmc.editorAreaFrameState);
        wm.setToolbarConfigName(wmc.toolbarConfiguration);
        if( null != activeTopComponentOverride )
            activeTopComponentOverride.requestActive();

        loaded = true;
    }


    /** Implements <code>WindowSystem</code> interface method. */
    public synchronized void save() {
        if( !loaded )
            return; //nothing to save
        if(DEBUG) {
            debugLog("## PersistenceHandler.save"); // NOI18N
        }
        TopComponentTracker.getDefault().save();

        ToolbarPool.getDefault().waitFinished();
        WindowManagerConfig wmc = getConfig();
        PersistenceManager.getDefault().saveWindowSystem(wmc);
    }

    private ModeImpl getModeFromConfig(ModeConfig mc) {
        if(DEBUG) {
            debugLog("Getting mode name=" + mc.name);
        }

        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(mc.name);
        if(mode == null) {
            mode = createModeFromConfig(mc);
        }
        return mode;
    }

    private ModeImpl createModeFromConfig(ModeConfig mc) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Creating mode name=\"" + mc.name + "\""); // NOI18N
        }

        ModeImpl mode;
        if (mc.kind == Constants.MODE_KIND_SLIDING) {
            mode = WindowManagerImpl.getInstance().createSlidingMode(
                    mc.name, mc.permanent, mc.side, mc.slideInSizes);
        } else {
             mode = WindowManagerImpl.getInstance().createMode(
                mc.name, mc.kind, mc.state, mc.permanent, mc.constraints);
        }
        name2mode.put(mc.name, mode);
        if( mc.minimized )
            mode.setMinimized( mc.minimized );
        if( null != mc.otherNames ) {
            for( String s : mc.otherNames ) {
                mode.addOtherName( s );
            }
        }

        return mode;
    }

    /**
     * find the the previous mode for tc if exists and set it in the model..
     */
    private void initPreviousModes(ModeImpl mode, ModeConfig mc, Map modes) {
        for (int j = 0; j < mc.tcRefConfigs.length; j++) {
            TCRefConfig tcRefConfig = mc.tcRefConfigs[j];
            if(DEBUG) {
                debugLog("\tTopComponent[" + j + "] id=\"" // NOI18N
                    + tcRefConfig.tc_id + "\", \topened=" + tcRefConfig.opened); // NOI18N
            }
            if (tcRefConfig.previousMode != null) {
                Iterator it = modes.keySet().iterator();
                ModeImpl previous = null;
                while (it.hasNext()) {
                    ModeImpl md = (ModeImpl)it.next();

                    if (tcRefConfig.previousMode.equals(md.getName())
                            || md.getOtherNames().contains( tcRefConfig.previousMode ) ) {
                        previous = md;
                        break;
                    }
                }
                if (previous != null) {
                    WindowManagerImpl.getInstance().setPreviousModeForTopComponent(tcRefConfig.tc_id, mode, previous, tcRefConfig.previousIndex);
                } else {
                    Logger.getLogger(PersistenceHandler.class.getName()).log(Level.INFO, null,
                                      new java.lang.NullPointerException("Cannot find previous mode named \'" +
                                                                         tcRefConfig.previousMode +
                                                                         "\'"));

                }
            }
        }
    }

    /**
     * find the the previous mode for tc if exists and set it in the model..
     */
    private void initPreviousMode (ModeImpl mode, TCRefConfig tcRefConfig) {
        if(DEBUG) {
            debugLog("\tTopComponent id=\"" // NOI18N
                + tcRefConfig.tc_id + "\", \topened=" + tcRefConfig.opened); // NOI18N
        }
        if (tcRefConfig.previousMode == null) {
            return;
        }
        Set<? extends ModeImpl> modes = WindowManagerImpl.getInstance().getModes();
        Iterator it = modes.iterator();
        ModeImpl previous = null;
        while (it.hasNext()) {
            ModeImpl md = (ModeImpl) it.next();

            if (tcRefConfig.previousMode.equals(md.getName())
                    || md.getOtherNames().contains( tcRefConfig.previousMode ) ) {
                previous = md;
                break;
            }
        }
        if (previous != null) {
            WindowManagerImpl.getInstance().setPreviousModeForTopComponent
            (tcRefConfig.tc_id, mode, previous, tcRefConfig.previousIndex);
        } else {
            Logger.getLogger(PersistenceHandler.class.getName()).log(Level.INFO, null,
                              new java.lang.NullPointerException("Cannot find previous mode named \'" +
                                                                 tcRefConfig.previousMode +
                                                                 "\'"));
        }
    }

    private ModeImpl initModeFromConfig(ModeImpl mode, ModeConfig mc, boolean initPrevModes) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        if( null != mc.otherNames ) {
            for( String s : mc.otherNames ) {
                mode.addOtherName( s );
            }
        }
        for (int j = 0; j < mc.tcRefConfigs.length; j++) {
            TCRefConfig tcRefConfig = mc.tcRefConfigs[j];
            if(DEBUG) {
                debugLog("\tTopComponent[" + j + "] id=\"" // NOI18N
                    + tcRefConfig.tc_id + "\", \topened=" + tcRefConfig.opened); // NOI18N
            }

            // PENDING
            if (tcRefConfig.opened) {
                lazyLoader.lazyLoad(mode, mc.selectedTopComponentID, tcRefConfig, j );
            } else {
                mode.addUnloadedTopComponent(tcRefConfig.tc_id);
            }
            wm.setTopComponentDockedInMaximizedMode( tcRefConfig.tc_id, tcRefConfig.dockedInMaximizedMode );
            wm.setTopComponentSlidedInDefaultMode( tcRefConfig.tc_id, !tcRefConfig.dockedInDefaultMode );
            wm.setTopComponentMaximizedWhenSlidedIn( tcRefConfig.tc_id, tcRefConfig.slidedInMaximized );
            if (initPrevModes) {
                initPreviousMode(mode, tcRefConfig);
            }
        }

        // PENDING Refine the unneded computing.
        Rectangle absBounds = mc.bounds == null ? new Rectangle() : mc.bounds;
        Rectangle relBounds = mc.relativeBounds == null ? new Rectangle() : mc.relativeBounds;
        Rectangle bounds = computeBounds(false, false,
            absBounds.x,
            absBounds.y,
            absBounds.width,
            absBounds.height,
            relBounds.x / 100.0F,
            relBounds.y / 100.0F,
            relBounds.width / 100.0F,
            relBounds.height / 100.0F);
        mode.setBounds(bounds);
        mode.setFrameState(mc.frameState);
        mode.setMinimized( mc.minimized );

        return mode;
    }

    TopComponent getTopComponentForID(String tc_id, boolean deserialize) {
        if(tc_id == null || "".equals(tc_id)) {
            return null;
        }

//        long start = System.currentTimeMillis();
        TopComponent tc = PersistenceManager.getDefault().getTopComponentForID(tc_id, deserialize);
//        if(DEBUG) {
//            debugLog("***Getting TopComponent for ID=" + tc_id + " in " + (System.currentTimeMillis() - start) + " ms"); // NOI18N
//        }

        return tc;
    }

    private TopComponentGroupImpl createTopComponentGroupFromConfig(GroupConfig groupCfg) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Creating group name=\"" + groupCfg.name + "\" \t[opened=" + groupCfg.opened + "]"); // NOI18N
        }

        TopComponentGroupImpl tcGroup = new TopComponentGroupImpl(groupCfg.name, groupCfg.opened);

        name2group.put(groupCfg.name, tcGroup);

        for (int j = 0; j < groupCfg.tcGroupConfigs.length; j++) {
            TCGroupConfig tcGroupCfg = groupCfg.tcGroupConfigs[j];
            if(DEBUG) {
                debugLog("\tTopComponent[" + j + "] id=\"" // NOI18N
                    + tcGroupCfg.tc_id + "\", \topen=" + tcGroupCfg.open + ", \tclose=" + tcGroupCfg.close
                    + ", \twasOpened=" + tcGroupCfg.wasOpened); // NOI18N
            }

            tcGroup.addUnloadedTopComponent(tcGroupCfg.tc_id);

            if (tcGroupCfg.open) {
                tcGroup.addUnloadedOpeningTopComponent(tcGroupCfg.tc_id);
            }
            if (tcGroupCfg.close) {
                tcGroup.addUnloadedClosingTopComponent(tcGroupCfg.tc_id);
            }

            // Handle also wasOpened flag.
            if(groupCfg.opened && tcGroupCfg.wasOpened) {
                tcGroup.addGroupUnloadedOpenedTopComponent(tcGroupCfg.tc_id);
            }
        }

        WindowManagerImpl.getInstance().addTopComponentGroup(tcGroup);

        return tcGroup;
    }



    private WindowManagerConfig getConfig() {
        WindowManagerConfig wmc = new WindowManagerConfig();

        wmc.preferredToolbarIconSize = ToolbarPool.getDefault().getPreferredIconSize();

        WindowManagerImpl wmi = WindowManagerImpl.getInstance();

        Rectangle joinedBounds = wmi.getMainWindowBoundsJoined();
        if(DEBUG) {
            debugLog("joinedBouds=" + joinedBounds); // NOI18N
        }
        wmc.xJoined      = joinedBounds.x;
        wmc.yJoined      = joinedBounds.y;
        wmc.widthJoined  = joinedBounds.width;
        wmc.heightJoined = joinedBounds.height;
        Rectangle separatedBounds = wmi.getMainWindowBoundsSeparated();
        if(DEBUG) {
            debugLog("separatedBounds=" + separatedBounds); // NOI18N
        }
        wmc.xSeparated      = separatedBounds.x;
        wmc.ySeparated      = separatedBounds.y;
        wmc.widthSeparated  = separatedBounds.width;
        wmc.heightSeparated = separatedBounds.height;

        if( Utilities.isMac() ) {
            //125881 - mac doesn't fire events when maximized window is resized by user
            wmc.mainWindowFrameStateJoined = wmi.getMainWindow().getExtendedState();
        } else {
            wmc.mainWindowFrameStateJoined = wmi.getMainWindowFrameStateJoined();
        }
        if (wmc.mainWindowFrameStateJoined == Frame.ICONIFIED) {
            // #46646 - don't save iconified state
            //mkleint - actually shoudn't we ignore the maximized states as well?
            wmc.mainWindowFrameStateJoined = Frame.NORMAL;
        }
        if(DEBUG) {
            debugLog("mainWindowFrameStateJoined=" + wmc.mainWindowFrameStateJoined); // NOI18N
        }
        wmc.mainWindowFrameStateSeparated = wmi.getMainWindowFrameStateSeparated();
        if (wmc.mainWindowFrameStateSeparated == Frame.ICONIFIED) {
            // #46646 - don't save iconified state
            wmc.mainWindowFrameStateSeparated = Frame.NORMAL;
        }
        if(DEBUG) {
            debugLog("mainWindowFrameStateSeparated=" + wmc.mainWindowFrameStateSeparated); // NOI18N
        }

        wmc.editorAreaState = wmi.getEditorAreaState();
        if(DEBUG) {
            debugLog("editorAreaState=" + wmc.editorAreaState); // NOI18N
        }
        wmc.editorAreaBounds = wmi.getEditorAreaBounds();
        if(DEBUG) {
            debugLog("editorAreaBounds=" + wmc.editorAreaBounds); // NOI18N
        }
        wmc.editorAreaConstraints = wmi.getEditorAreaConstraints();
        if(DEBUG) {
            debugLog("editorAreaConstraints=" + Arrays.toString(wmc.editorAreaConstraints)); // NOI18N
        }
        wmc.editorAreaFrameState = wmi.getEditorAreaFrameState();
        if(DEBUG) {
            debugLog("editorAreaFrameState=" + wmc.editorAreaFrameState); // NOI18N
        }
        wmc.screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        ModeImpl mo = wmi.getActiveMode();
        if(DEBUG) {
            debugLog("active mode=" + mo); // NOI18N
        }
        if (mo != null) {
            wmc.activeModeName = mo.getName();
        }

        mo = wmi.getEditorMaximizedMode();
        if(DEBUG) {
            debugLog("editor maximized mode=" + mo); // NOI18N
        }
        if (mo != null) {
            wmc.editorMaximizedModeName = mo.getName();
        }

        mo = wmi.getViewMaximizedMode();
        if(DEBUG) {
            debugLog("view maximized mode=" + mo); // NOI18N
        }
        if (mo != null) {
            wmc.viewMaximizedModeName = mo.getName();
        }

        wmc.toolbarConfiguration = wmi.getToolbarConfigName();
        if(DEBUG) {
            debugLog("toolbarConfiguration=" + wmc.toolbarConfiguration); // NOI18N
        }

        // Modes.
        Set<? extends Mode> modeSet = wmi.getModes();
        List<ModeConfig> modeConfigs = new ArrayList<ModeConfig>(modeSet.size());
        for (Iterator<? extends Mode> it = modeSet.iterator(); it.hasNext(); ) {
            ModeImpl modeImpl = (ModeImpl)it.next();
            //Do not save empty non permanent mode
            ModeConfig mc = getConfigFromMode(modeImpl);
            if ((mc.tcRefConfigs.length == 0) && (!mc.permanent) && wmi.getCentral().doCheckSlidingModes(modeImpl)) {
                continue;
            }
            modeConfigs.add(mc);
        }
        wmc.modes = modeConfigs.toArray(new ModeConfig[0]);

        // TopComponent groups.
        Set<TopComponentGroupImpl> tcGroups = wmi.getTopComponentGroups();
        List<GroupConfig> groupConfigs = new ArrayList<GroupConfig>(tcGroups.size());
        for (Iterator<TopComponentGroupImpl> it = tcGroups.iterator(); it.hasNext(); ) {
            groupConfigs.add(getConfigFromGroup(it.next()));
        }
        wmc.groups = groupConfigs.toArray(new GroupConfig[0]);

        PersistenceManager pm = PersistenceManager.getDefault();
        //RecentViewList
        TopComponent [] tcs = wmi.getRecentViewList();
        List<String> tcIdList = new ArrayList<String>(tcs.length);
        for (int i = 0; i < tcs.length; i++) {
            if (pm.isTopComponentPersistent(tcs[i])) {
                String tc_id = WindowManager.getDefault().findTopComponentID(tcs[i]);
                tc_id = PersistenceManager.escapeTcId4XmlContent(tc_id);
                tcIdList.add(tc_id);
            }
        }
        wmc.tcIdViewList = tcIdList.toArray(new String [0]);

        return wmc;
    }

    public ModeConfig getConfigFromMode(ModeImpl mode) {
        PersistenceManager pm = PersistenceManager.getDefault();
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeConfig modeCfg = new ModeConfig();
        modeCfg.name = mode.getName();
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("mode name=" + modeCfg.name); // NOI18N
        }
        modeCfg.state = mode.getState();
        if(DEBUG) {
            debugLog("mode state=" + modeCfg.state); // NOI18N
        }

        modeCfg.kind = mode.getKind();
        if(DEBUG) {
            debugLog("mode kind=" + modeCfg.kind); // NOI18N
        }
        if (wm instanceof WindowManagerImpl) {
            modeCfg.side = wm.getCentral().getModeSide(mode);
            if( null != modeCfg.side ) {
                modeCfg.slideInSizes = wm.getCentral().getSlideInSizes( modeCfg.side );
            }
        }
        if(DEBUG) {
            debugLog("mode side=" + modeCfg.side); // NOI18N
        }

        modeCfg.constraints = mode.getConstraints();
        if(DEBUG) {
            debugLog("mode constraints=" + Arrays.toString(modeCfg.constraints)); // NOI18N
        }
        // PENDING Whether to save relative or absolute bounds.
        // In case of relative, they would need to be computed.
        Rectangle relBounds = null;
        if (relBounds != null) {
            modeCfg.relativeBounds = relBounds;
        } else {
            modeCfg.bounds = mode.getBounds();
            if(DEBUG) {
                debugLog("mode bounds=" + modeCfg.bounds); // NOI18N
            }
        }
        modeCfg.frameState = mode.getFrameState();
        if(DEBUG) {
            debugLog("mode frame state=" + modeCfg.frameState); // NOI18N
        }
        if( modeCfg.frameState == Frame.MAXIMIZED_BOTH && Utilities.isWindows() ) {
            //#200199 - when a window is maximized on MS Windows its bounds origin
            //are shifted -4 pixels horizontally and vertically for some reason
            //so the window location may seem to be out of screen on some multi-monitor setups
            if( modeCfg.bounds.x < 0 ) {
                modeCfg.bounds.x += 4;
                modeCfg.bounds.width -= 4;
            }
            if( modeCfg.bounds.y < 0 ) {
                modeCfg.bounds.y += 4;
                modeCfg.bounds.height -= 4;
            }

        }

        TopComponent selectedTC = mode.getSelectedTopComponent();
        if(selectedTC != null) {
            if (pm.isTopComponentPersistent(selectedTC)) {
                String tc_id = wm.findTopComponentID(selectedTC);
                if(DEBUG) {
                    debugLog("selected tc=" + selectedTC.getName()); // NOI18N
                }
                modeCfg.selectedTopComponentID = tc_id;
            }
        }
        modeCfg.permanent = mode.isPermanent();
        if(DEBUG) {
            debugLog("mode permanent=" + modeCfg.permanent); // NOI18N
        }

        String prevSelectedTCID = mode.getPreviousSelectedTopComponentID();
        if(prevSelectedTCID != null) {
            if(DEBUG) {
                debugLog("previous selected tc id=" + prevSelectedTCID); // NOI18N
            }
            modeCfg.previousSelectedTopComponentID = prevSelectedTCID;
        }

        modeCfg.minimized = mode.isMinimized();

        modeCfg.otherNames = mode.getOtherNames();

        // TopComponents:
        List<TCRefConfig> tcRefCfgList = new ArrayList<TCRefConfig>();
        List<String> openedTcIDs = mode.getOpenedTopComponentsIDs();
        for(Iterator it = mode.getTopComponentsIDs().iterator(); it.hasNext(); ) {
            String tcID = (String)it.next();
            //Filter tc we do not want to save
            boolean opened = openedTcIDs.contains(tcID);
            if (opened) {
                if (pm.isTopComponentNonPersistentForID(tcID)) {
                    continue;
                }
            } else {
                if (pm.isTopComponentNonPersistentForID(tcID)) {
                    continue;
                } else if (pm.isTopComponentPersistentOnlyOpenedForID(tcID)) {
                    continue;
                }
            }

            // #45981: save previous mode even for closed tcs
            String modeName = null;
            int prevIndex = -1;
            ModeImpl prev = wm.getPreviousModeForTopComponent(tcID, mode);
            if (prev != null) {
                modeName = prev.getName();
                prevIndex = wm.getPreviousIndexForTopComponent(tcID, mode);
            }

            if(DEBUG) {
                debugLog("tc ID=" + tcID + " opened=" + opened); // NOI18N
            }
            TCRefConfig tcRefCfg = new TCRefConfig();
            tcRefCfg.tc_id = tcID;
            tcRefCfg.opened = opened;
            tcRefCfg.previousMode = modeName;
            tcRefCfg.previousIndex = prevIndex;
            tcRefCfg.dockedInMaximizedMode = wm.isTopComponentDockedInMaximizedMode( tcID );
            tcRefCfg.dockedInDefaultMode = !wm.isTopComponentSlidedInDefaultMode( tcID );
            tcRefCfg.slidedInMaximized = wm.isTopComponentMaximizedWhenSlidedIn( tcID );
            tcRefCfgList.add(tcRefCfg);
        }

        modeCfg.tcRefConfigs = tcRefCfgList.toArray(new TCRefConfig[0]);
        return modeCfg;
    }

    private GroupConfig getConfigFromGroup(TopComponentGroupImpl tcGroup) {
        GroupConfig groupCfg = new GroupConfig();
        groupCfg.name = tcGroup.getName();
        groupCfg.opened = tcGroup.isOpened();
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("group name=" + groupCfg.name); // NOI18N
        }
        Set<String> openSet = tcGroup.getOpeningSetIDs();
        Set<String> closeSet = tcGroup.getClosingSetIDs();
        Set<String> wasOpenedSet = tcGroup.getGroupOpenedTopComponentsIDs();

        Map<String, TCGroupConfig> tcGroupCfgMap = new HashMap<String, TCGroupConfig>();

        for (String tcID: tcGroup.getTopComponentsIDs()) {

            TCGroupConfig tcGroupCfg;
            if (tcGroupCfgMap.containsKey(tcID)) {
                tcGroupCfg = tcGroupCfgMap.get(tcID);
            } else {
                tcGroupCfg = new TCGroupConfig();
                tcGroupCfg.tc_id = tcID;
                tcGroupCfgMap.put(tcID, tcGroupCfg);
            }

            tcGroupCfg.open  = openSet.contains(tcID);
            tcGroupCfg.close = closeSet.contains(tcID);
            if(groupCfg.opened) {
                tcGroupCfg.wasOpened = wasOpenedSet.contains(tcID);
            }
            if(DEBUG) {
                debugLog("tc id=" + tcGroupCfg.tc_id // NOI18N
                    + ", open=" + tcGroupCfg.open // NOI18N
                    + ", close=" + tcGroupCfg.close // NOI18N
                    + ", wasOpened=" + tcGroupCfg.wasOpened); // NOI18N
            }
        }

        groupCfg.tcGroupConfigs = tcGroupCfgMap.values().toArray(new TCGroupConfig[0]);
        return groupCfg;
    }


    /** Handles adding mode to model.
     * @param modeConfig configuration data of added mode
     */
    @Override
    public synchronized void modeConfigAdded(ModeConfig modeConfig) {
        if(DEBUG) {
            debugLog("WMI.modeConfigAdded mo:" + modeConfig.name); // NOI18N
        }
        ModeImpl mode = getModeFromConfig(modeConfig);
        initModeFromConfig(mode, modeConfig, true);
    }

    /** Handles removing mode from model.
     * @param modeName unique name of removed mode
     */
    @Override
    public synchronized void modeConfigRemoved(String modeName) {
        if(DEBUG) {
            debugLog("WMI.modeConfigRemoved mo:" + modeName); // NOI18N
        }
        ModeImpl mode = (ModeImpl)name2mode.remove(modeName);
        if(mode != null) {
            WindowManagerImpl.getInstance().removeMode(mode);
        } else {
            Logger.getLogger(PersistenceHandler.class.getName()).log(Level.WARNING, null,
                              new java.lang.NullPointerException("Mode for name=" +
                                                                 modeName +
                                                                 " was not created")); // NOI18N
        }
    }

    /** Handles adding tcRef to model.
     * @param modeName unique name of parent mode.
     * @param tcRefConfig configuration data of added tcRef
     * @param tcRefNames array of tcIds to pass ordering of new tcRef,
     * if there is no ordering defined tcRef is appended to end of array
     */
    @Override
    public synchronized void topComponentRefConfigAdded
    (String modeName, TCRefConfig tcRefConfig, String [] tcRefNames) {
        if(DEBUG) {
            debugLog("WMI.topComponentRefConfigAdded mo:" + modeName + " tcRef:" + tcRefConfig.tc_id); // NOI18N
        }

        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        wm.setTopComponentDockedInMaximizedMode( tcRefConfig.tc_id, tcRefConfig.dockedInMaximizedMode );
        wm.setTopComponentSlidedInDefaultMode( tcRefConfig.tc_id, !tcRefConfig.dockedInDefaultMode );
        wm.setTopComponentMaximizedWhenSlidedIn( tcRefConfig.tc_id, tcRefConfig.slidedInMaximized );
        ModeImpl mode = (ModeImpl) name2mode.get(modeName);
        if( null == mode ) {
            for( ModeImpl m : name2mode.values() ) {
                if( m.getOtherNames().contains( modeName ) ) {
                    mode = m;
                    break;
                }
            }
        }
        if (mode != null) {
            initPreviousMode(mode, tcRefConfig);
        }

        TopComponent tc = getTopComponentForID(tcRefConfig.tc_id,true);
        if (tc != null) {
            if (mode != null) {
                if (tcRefConfig.opened) {
                    mode.addOpenedTopComponent(tc);
                } else {
                    mode.addClosedTopComponent(tc);
                }
            }
        }
    }

    /** Handles removing tcRef from model.
     * @param tc_id unique id of removed tcRef
     */
    @Override
    public synchronized void topComponentRefConfigRemoved(String tc_id) {
        if(DEBUG) {
            debugLog("WMI.topComponentRefConfigRemoved tcRef:" + tc_id); // NOI18N
        }

        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeImpl mode = wm.findModeForOpenedID(tc_id);
        if(mode != null) {
            TopComponent tc = getTopComponentForID(tc_id,true);
            if(tc != null) {
                mode.removeTopComponent(tc);
            }
        } else {
            mode = wm.findModeForClosedID(tc_id);
            if(mode != null) {
                mode.removeClosedTopComponentID(tc_id);
            }
        }
    }

    /** Handles adding group to model.
     * @param groupConfig configuration data of added group
     */
    @Override
    public synchronized void groupConfigAdded(GroupConfig groupConfig) {
        if(DEBUG) {
            debugLog("WMI.groupConfigAdded group:" + groupConfig.name); // NOI18N
        }
        createTopComponentGroupFromConfig(groupConfig);
    }

    /** Handles removing group from model.
     * @param groupName unique name of removed group
     */
    @Override
    public synchronized void groupConfigRemoved(String groupName) {
        if(DEBUG) {
            debugLog("WMI.groupConfigRemoved group:" + groupName); // NOI18N
        }
        TopComponentGroupImpl group = (TopComponentGroupImpl)name2group.remove(groupName);
        if(group != null) {
            WindowManagerImpl.getInstance().removeTopComponentGroup(group);
        } else {
            Logger.getLogger(PersistenceHandler.class.getName()).log(Level.WARNING, null,
                              new java.lang.NullPointerException("Null group for name=" +
                                                                 groupName));
        }
    }

    /** Handles adding tcGroup to model.
     * @param groupName unique name of parent group
     * @param tcGroupConfig configuration data of added tcGroup
     */
    @Override
    public synchronized void topComponentGroupConfigAdded(String groupName, TCGroupConfig tcGroupConfig) {
        if(DEBUG) {
            debugLog("WMI.topComponentGroupConfigAdded group:" + groupName + " tcGroup:" + tcGroupConfig.tc_id); // NOI18N
        }

        TopComponentGroupImpl group = (TopComponentGroupImpl)name2group.get(groupName);
        if(group != null) {
            group.addUnloadedTopComponent(tcGroupConfig.tc_id);
            if(tcGroupConfig.open) {
                group.addUnloadedOpeningTopComponent(tcGroupConfig.tc_id);
            }

            if(tcGroupConfig.close) {
                group.addUnloadedClosingTopComponent(tcGroupConfig.tc_id);
            }
        }
    }

    /** Handles removing tcGroup from model.
     * @param groupName unique name of parent group
     * @param tc_id unique id of removed tcGroup
     */
    @Override
    public synchronized void topComponentGroupConfigRemoved(String groupName, String tc_id) {
        if(DEBUG) {
            debugLog("WMI.topComponentGroupConfigRemoved group:" + groupName + " tcGroup:" + tc_id); // NOI18N
        }

        TopComponentGroupImpl group = (TopComponentGroupImpl)name2group.get(groupName);
        if(group != null) {
            group.removeUnloadedTopComponent(tc_id);
        }
    }

    /** Dump window manager configuration data to standard output. */
    private static String dumpConfig (WindowManagerConfig wmc) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n-- wmc: [" + Integer.toHexString(System.identityHashCode(wmc)) + "]");
        buffer.append("\n-- JOINED --");
        buffer.append("\n-- x: " + wmc.xJoined);
        buffer.append("\n-- y: " + wmc.yJoined);
        buffer.append("\n--  width: " + wmc.widthJoined);
        buffer.append("\n-- height: " + wmc.heightJoined);
        buffer.append("\n--  relativeX: " + wmc.relativeXJoined);
        buffer.append("\n--  relativeY: " + wmc.relativeYJoined);
        buffer.append("\n--  relativeWidth: " + wmc.relativeWidthJoined);
        buffer.append("\n-- relativeHeight: " + wmc.relativeHeightJoined);
        buffer.append("\n-- centeredHorizontally: " + wmc.centeredHorizontallyJoined);
        buffer.append("\n--   centeredVertically: " + wmc.centeredVerticallyJoined);
        buffer.append("\n--    maximizeIfWidthBelowJoined: " + wmc.maximizeIfWidthBelowJoined);
        buffer.append("\n--   maximizeIfHeightBelowJoined: " + wmc.maximizeIfHeightBelowJoined);

        buffer.append("\n-- SEPARATED --");
        buffer.append("\n-- x: " + wmc.xSeparated);
        buffer.append("\n-- y: " + wmc.ySeparated);
        buffer.append("\n--  width: " + wmc.widthSeparated);
        buffer.append("\n-- height: " + wmc.heightSeparated);
        buffer.append("\n--  relativeX: " + wmc.relativeXSeparated);
        buffer.append("\n--  relativeY: " + wmc.relativeYSeparated);
        buffer.append("\n--  relativeWidth: " + wmc.relativeWidthSeparated);
        buffer.append("\n-- relativeHeight: " + wmc.relativeHeightSeparated);
        buffer.append("\n-- centeredHorizontally: " + wmc.centeredHorizontallySeparated);
        buffer.append("\n--   centeredVertically: " + wmc.centeredVerticallySeparated);

        buffer.append("\n-- editorAreaState: " + wmc.editorAreaState);
        if(wmc.editorAreaConstraints != null) {
            for (int i = 0; i < wmc.editorAreaConstraints.length; i++) {
                buffer.append("\n-- co[" + i + "]: " + wmc.editorAreaConstraints[i]);
            }
        }
        buffer.append("\n--         editorAreaBounds: " + wmc.editorAreaBounds);
        buffer.append("\n-- editorAreaRelativeBounds: " + wmc.editorAreaRelativeBounds);

        buffer.append("\n--     screenSize: " + wmc.screenSize);
        buffer.append("\n--    activeModeName: " + wmc.activeModeName);
        buffer.append("\n-- editorMaximizedModeName: " + wmc.editorMaximizedModeName);
        buffer.append("\n-- viewMaximizedModeName: " + wmc.viewMaximizedModeName);
        buffer.append("\n--     toolbarconfig: " + wmc.toolbarConfiguration);
        buffer.append("\n-- modes: " + Arrays.toString(wmc.modes) + " size " + (wmc.modes == null ? -1 : wmc.modes.length));
        for (int i = 0; i < wmc.modes.length; i++) {
            ModeConfig mc = wmc.modes[i];
            buffer.append("\n-- --");
            buffer.append("\n-- -- mode[" + i + "]: " + mc.name);
            buffer.append("\n-- -- state: " + mc.state + " "
            + ((mc.state == Constants.MODE_STATE_JOINED) ? "joined" : "separated"));
            if (mc.constraints != null) {
                buffer.append("\n-- -- constraints.sz: " + mc.constraints.length);
                for (int j = 0; j < mc.constraints.length; j++) {
                    buffer.append("\n-- -- co[" + j + "]: " + mc.constraints[j]);
                }
            }
            buffer.append("\n-- -- kind: " + mc.kind + " ");
            if (mc.kind == Constants.MODE_KIND_EDITOR) {
                buffer.append("editor");
            } else if (mc.kind == Constants.MODE_KIND_VIEW) {
                buffer.append("view");
            } else if (mc.kind == Constants.MODE_KIND_SLIDING) {
                buffer.append("sliding");
            } else {
                buffer.append("unknown");
            }
            buffer.append("\n-- --         bounds: " + mc.bounds);
            buffer.append("\n-- -- relativeBounds: " + mc.relativeBounds);
            buffer.append("\n-- --          state: " + mc.frameState);
            buffer.append("\n-- -- active-tc: " + mc.selectedTopComponentID);
            buffer.append("\n-- -- permanent: " + mc.permanent);
            if (mc.tcRefConfigs != null) {
                for (int k = 0; k < mc.tcRefConfigs.length; k++) {
                    TCRefConfig tcRefCfg = mc.tcRefConfigs[k];
                    buffer.append("\n++ ++ ++ tcRef[" + k + "]: " + tcRefCfg.tc_id);
                    buffer.append("\n++ ++ ++   opened: " + tcRefCfg.opened);
                }
            }
        }
        buffer.append("\n-- groups: " + Arrays.toString(wmc.groups) + " size " + (wmc.groups == null ? -1 : wmc.groups.length));
        for (int i = 0; i < wmc.groups.length; i++) {
            GroupConfig sc = wmc.groups[i];
            buffer.append("\n-- --");
            buffer.append("\n-- -- group[" + i + "]: " + sc.name);
            if (sc.tcGroupConfigs != null) {
                for (int k = 0; k < sc.tcGroupConfigs.length; k++) {
                    TCGroupConfig tcGroupCfg = sc.tcGroupConfigs[k];
                    buffer.append("\n++ ++ ++ tcGroup[" + k + "]: " + tcGroupCfg.tc_id);
                    buffer.append("\n++ ++ ++   open: " + tcGroupCfg.open);
                    buffer.append("\n++ ++ ++  close: " + tcGroupCfg.close);
                }
            }
        }

        return buffer.toString();
    }

    private static void debugLog(String message) {
        Debug.log(PersistenceHandler.class, message);
    }

    /**
     * @return False if the given point is not inside any screen device that are currently available.
     */
    private static boolean isOutOfScreen( int x, int y ) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for( int j=0; j<gs.length; j++ ) {
            GraphicsDevice gd = gs[j];
            if( gd.getType() != GraphicsDevice.TYPE_RASTER_SCREEN )
                continue;
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            if( bounds.contains( x, y ) )
                return false;
        }
        return true;
    }

    public static Rectangle computeBounds(boolean centeredHorizontaly, boolean centeredVerticaly,
    int x, int y, int width, int height, float relativeX, float relativeY, float relativeWidth, float relativeHeight) {
        Rectangle bounds;
        if(width > 0 && height > 0) {
            // From absoute values.
            bounds = new Rectangle(x, y, width, height);
            // #33288 fix start- when screen resolution changes, some windows may get completely out of the screen.
            Rectangle screen = Utilities.getUsableScreenBounds();
            int xlimit = screen.x + screen.width - 20; // 20 = let's have some buffer area..
            int ylimit = screen.y + screen.height - 20; // 20 = let's have some buffer area..
            // will make sure that the out-of-screen windows get thrown in.
            if( isOutOfScreen( bounds.x, bounds.y ) ) {
                while (bounds.x > xlimit) {
                    bounds.x = Math.max(bounds.x - screen.width, screen.x);
                }
                while (bounds.y > ylimit) {
                    bounds.y = Math.max(bounds.y - ylimit, screen.y);
                }
            }
            // #33288 fix end
        } else if(relativeWidth > 0F && relativeHeight > 0F) {
            // From relative values.
            Rectangle screen = Utilities.getUsableScreenBounds();
            bounds = new Rectangle((int)(screen.width * relativeX), (int)(screen.height * relativeY),
                        (int)(screen.width * relativeWidth), (int)(screen.height * relativeHeight));
        } else {
            return null;
        }
        // Center the bounds if necessary.
        if(centeredHorizontaly || centeredVerticaly) {
            Rectangle centered = Utilities.findCenterBounds(new Dimension(bounds.width, bounds.height));
            if(centeredHorizontaly) {
                bounds.x = centered.x;
            }
            if(centeredVerticaly) {
                bounds.y = centered.y;
            }
        }

        return bounds;
    }

    void finishLazyLoading() {
        lazyLoader.loadAllNow();
    }

}
