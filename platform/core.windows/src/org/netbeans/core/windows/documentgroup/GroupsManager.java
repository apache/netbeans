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
package org.netbeans.core.windows.documentgroup;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.core.WindowSystem;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.PersistenceHandler;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.persistence.ModeConfig;
import org.netbeans.core.windows.persistence.PersistenceManager;
import org.netbeans.core.windows.persistence.WindowManagerParser;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author stan
 */
public class GroupsManager {

    private static GroupsManager theInstance;

    private static final String SEL_GROUP = "selectedDocumentGroup"; //NOI18N
    private static final String DEFAULT_GROUP_NAME = "group"; //NOI18N
    private static final String DISPLAY_NAME = "displayName"; //NOI18N
    private static final String CONFIG = "config"; //NOI18N
    private static final String SETTINGS = "settings"; //NOI18N
    private static final String WSMODE = "wsmode"; //NOI18N
    private static final String WINDOWS2_LOCAL = "Windows2Local"; //NOI18N
    private static final String COMPONENTS = "Components"; //NOI18N
    private static final String MODES = "Modes"; //NOI18N
    private static final String DOCUMENT_GROUPS = "DocumentGroups"; //NOI18N
    private static final String ID_SEPARATOR = ":"; //NOI18N
    private static final String SELECTED_ID = "_selectedId"; //NOI18N

    private static final Logger LOG = Logger.getLogger(GroupsManager.class.getName());

    private GroupsManager() {
    }

    public static GroupsManager getDefault() {
        synchronized( GroupsManager.class ) {
            if( null == theInstance ) {
                theInstance = new GroupsManager();
            }
            return theInstance;
        }
    }

    void addGroup(String displayName) {
        Preferences prefs = getPreferences();
        int groupIndex = getGroups().size();
        String groupName = DEFAULT_GROUP_NAME + groupIndex;
        try {
            do {
                groupIndex++;
                groupName = DEFAULT_GROUP_NAME + groupIndex;
            } while( prefs.nodeExists(groupName) );
        } catch( BackingStoreException ex ) {
            LOG.log(Level.INFO, null, ex);
        }
        prefs.put(SEL_GROUP, groupName);

        prefs.node(groupName).put(DISPLAY_NAME, displayName);
    }

    void removeAllGroups() {
        List<DocumentGroupImpl> groups = getGroups();
        for( DocumentGroupImpl group : groups ) {
            removeGroup(group);
        }
    }

    DocumentGroupImpl getCurrentGroup() {
        String selGroupName = getPreferences().get(SEL_GROUP, ""); //NOI18N
        return selGroupName.isEmpty() ? null : createGroup(selGroupName);
    }

    private DocumentGroupImpl createGroup(String groupName) {
        Preferences prefs = getPreferences().node(groupName);
        String displayName = prefs.get(DISPLAY_NAME, groupName);
        return new DocumentGroupImpl(groupName, displayName);
    }

    List<DocumentGroupImpl> getGroups() {
        Preferences prefs = getPreferences();
        try {
            String[] names = prefs.childrenNames();
            ArrayList<DocumentGroupImpl> res = new ArrayList<>(names.length);
            for( String name : names ) {
                res.add(createGroup(name));
            }
            Collections.sort(res);
            return res;
        } catch( BackingStoreException e ) {
            LOG.log(Level.INFO, null, e);
        }
        return Collections.emptyList();
    }

    static Preferences getPreferences() {
        return NbPreferences.forModule(DocumentGroupImpl.class).node(DOCUMENT_GROUPS); //NOI18N
    }

    boolean openGroup(DocumentGroupImpl group) {
        DocumentGroupImpl current = getCurrentGroup();
        if( null != current && !current.close() ) {
            return false;
        }
        //close documents not in our list
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        for( TopComponent tc : TopComponent.getRegistry().getOpened() ) {
            if( wmi.isEditorTopComponent(tc) ) {
                if( !tc.close() ) {
                    return false;
                }
            }
        }
        
        //prune empty modes
        ArrayList<ModeImpl> emptyModes = new ArrayList<>(10);
        for( ModeImpl mode : wmi.getModes() ) {
            if( mode.isPermanent() || mode.getKind() != Constants.MODE_KIND_EDITOR )
                continue;
            if( mode.getOpenedTopComponentsIDs().isEmpty() ) {
                emptyModes.add(mode);
            }
        }
        for( ModeImpl mode : emptyModes ) {
            wmi.removeMode(mode);
        }

        String name = group.getName();
        Preferences prefs = getPreferences().node(name);

        File userDir = Places.getUserDirectory();
        File root = new File(new File(userDir, "config"), DOCUMENT_GROUPS); //NOI18N
        File groupDir = new File(root, name);
        FileObject groupFO = FileUtil.toFileObject(groupDir);

        if( null != groupFO ) {
            //load mode configs
            Map<ModeImpl, ModeConfig> mode2config = new HashMap<>(10);
            Map<String, ModeImpl> tcid2mode = new HashMap<>(50);
            for( FileObject fo : groupFO.getChildren() ) {
                if( fo.isData() && WSMODE.equals(fo.getExt()) ) {
                    try {

                        ModeConfig config = WindowManagerParser.loadModeConfigFrom(fo);
                        String idList = prefs.get(config.name, ""); //NOI18N
                        if( idList.isEmpty() )
                            continue; //ignore empty modes
                        ModeImpl mode = (ModeImpl) wmi.findMode(config.name);
                        if( null == mode ) {
                            mode = createMode(config);
                        } else {
                            mode.setConstraints(config.constraints);
                        }
                        mode2config.put(mode, config);
                        String[] ids = idList.split(ID_SEPARATOR);
                        for( String id : ids ) {
                            tcid2mode.put(id, mode);
                            mode.addUnloadedTopComponent(id);
                        }
                    } catch( IOException ex ) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }

            DataLoader settingsLoader = null;
            Enumeration<DataLoader> loaders = DataLoaderPool.getDefault().producersOf(InstanceDataObject.class);
            while( loaders.hasMoreElements() ) {
                settingsLoader = loaders.nextElement();
            }
            //restore TCs
            Map<String, TopComponent> id2tc = new HashMap<>(50);
            for( FileObject fo : groupFO.getChildren() ) {
                if( fo.isData() && SETTINGS.equals(fo.getExt()) ) {
                    DataObject dob;
                    try {
                        //for some reason the DataLoader infrastructure insists that the first FileObject is an XMLDataObject
                        //using preferred loader ensures we always get the expected InstanceDataObject with proper instance cookies
                        DataLoaderPool.setPreferredLoader(fo, settingsLoader);
                        dob = DataObject.find(fo);
                        DataLoaderPool.setPreferredLoader(fo, null);
                        InstanceCookie ic = dob.getCookie(InstanceCookie.class);
                        if( null != ic ) {
                            TopComponent tc = (TopComponent) ic.instanceCreate();
                            id2tc.put(fo.getName(), tc);
                        } else {
                            // no instance cookie, which means that module which owned top
                            // component is gone or versions of data and module are incompatible
                            String excAnnotation = NbBundle.getMessage(
                                    PersistenceManager.class, "EXC_BrokenTCSetting",  //NOI18N
                                    fo.getName());
                            //resultExc = new SafeException(new IOException(excAnnotation));
                            LOG.log(Level.INFO,
                                "[PersistenceManager.getTopComponentForID]" // NOI18N
                                + " Problem when deserializing TopComponent for tcID:'" + fo.getName() + "'. Reason: " // NOI18N //NOI18N
                                + excAnnotation/*, resultExc*/);
                            
                        }
                    } catch( Exception ex ) {
                        LOG.log( Level.INFO, null, ex );
                    }
                }
            }

            Map<String, String> oldId2newId = new HashMap<>(id2tc.size());
            for( Map.Entry<String, TopComponent> entry : id2tc.entrySet() ) {
                String oldId = entry.getKey();
                TopComponent tc = entry.getValue();
                ModeImpl mode = tcid2mode.get(oldId);
                if( null != mode ) {
                    mode.dockInto(tc);
                }
                tc.open();
                oldId2newId.put(oldId, wmi.findTopComponentID(tc));
            }
            
            //restore selection in all modes
            for( ModeImpl mode : wmi.getModes() ) {
                ModeConfig config = mode2config.get(mode);
                if( null == config )
                    continue;
                String selectedId = config.selectedTopComponentID;
                if( null != selectedId )
                    selectedId = oldId2newId.get(selectedId);
                if( null != selectedId ) {
                    if( mode.getOpenedTopComponentsIDs().contains(selectedId) ) {
                        TopComponent tc = wmi.findTopComponent(selectedId);
                        if( null != tc )
                            mode.setSelectedTopComponent(tc);
                    }
                }
            }
        }

        //TODO restore recent view list
        
        getPreferences().put(SEL_GROUP, group.getName());
        
        return true;
    }

    boolean closeGroup(DocumentGroupImpl group) {
        TopComponent welcomeTc = WindowManager.getDefault().findTopComponent("Welcome"); // NOI18N
        boolean welcomeWasOpened = null != welcomeTc && welcomeTc.isOpened();
        //save the window system first
        Lookup.getDefault().lookup(WindowSystem.class).save();

        //collect documents
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        ArrayList<TopComponent> documents = new ArrayList<>(TopComponent.getRegistry().getOpened().size());
        for( TopComponent tc : TopComponent.getRegistry().getOpened() ) {
            if( wmi.isEditorTopComponent(tc) ) {
                documents.add(tc);
            }
        }

        String name = group.getName();
        File userDir = Places.getUserDirectory();
        File root = new File(new File(userDir, "config"), DOCUMENT_GROUPS); //NOI18N
        File groupDir = new File(root, name);
        //remove old data (if any)
        deleteAll(groupDir);
        groupDir.mkdirs();
        FileObject groupFO = FileUtil.toFileObject(groupDir);

        Preferences prefs = getPreferences().node(name);
        try {
            prefs.clear();
        } catch( BackingStoreException ex ) {
            LOG.log(Level.INFO, null, ex);
        }
        prefs.put(DISPLAY_NAME, group.toString());

        File configRoot = new File(new File(Places.getUserDirectory(), CONFIG), WINDOWS2_LOCAL);
        File modesRoot = new File(configRoot, MODES);
        for( ModeImpl mode : wmi.getModes() ) {
            if( mode.getKind() == Constants.MODE_KIND_EDITOR ) {
                String modeName = mode.getName();
                FileObject modeFO = FileUtil.toFileObject(new File(modesRoot, modeName + "." + WSMODE)); //NOI18N //NOI18N
                if( null == modeFO ) {
                    continue;
                }
                try {
                    modeFO.copy(groupFO, modeName, WSMODE);
                } catch( IOException ex ) {
                    LOG.log( Level.INFO, null, ex );
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                for( String id : mode.getOpenedTopComponentsIDs() ) {
                    sb.append(id);
                    sb.append(ID_SEPARATOR);
                }
                prefs.put(modeName, sb.toString());
            }
        }
        
        //copy TopComponents
        File componentRoot = new File(configRoot, COMPONENTS);
        for( TopComponent tc : documents ) {
            String id = wmi.findTopComponentID(tc);
            if( tc.equals(welcomeTc) && !welcomeWasOpened )
                continue;
            if (tc.getPersistenceType() == TopComponent.PERSISTENCE_NEVER) {
                continue;
            }
            FileObject tcFO = FileUtil.toFileObject(new File(componentRoot, id + "." + SETTINGS)); //NOI18N //NOI18N
            if (tcFO == null) {
                continue;
            }
            try {
                tcFO.copy(groupFO, id, SETTINGS);
            } catch( IOException ex ) {
                LOG.log( Level.INFO, null, ex );
            }
        }

        //TODO save recent view list
        
        getPreferences().put(SEL_GROUP, ""); //NOI18N

        return true;

    }

    private ModeImpl createMode(ModeConfig config) {
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();

        ModeImpl res = wmi.createMode(config.name, config.kind, config.state, false, config.constraints);
        Rectangle absBounds = config.bounds == null ? new Rectangle() : config.bounds;
        Rectangle relBounds = config.relativeBounds == null ? new Rectangle() : config.relativeBounds;
        Rectangle bounds = PersistenceHandler.computeBounds(false, false,
                absBounds.x,
                absBounds.y,
                absBounds.width,
                absBounds.height,
                relBounds.x / 100.0F,
                relBounds.y / 100.0F,
                relBounds.width / 100.0F,
                relBounds.height / 100.0F);
        res.setBounds(bounds);
        res.setFrameState(config.frameState);
        res.setMinimized(config.minimized);
        return res;
    }

    void removeGroup(DocumentGroupImpl group) {
        try {

            Preferences prefs = getPreferences();
            if( group.getName().equals(prefs.get(SEL_GROUP, "")) ) { //NOI18N
                prefs.put(SEL_GROUP, ""); //NOI18N
            }
            prefs = prefs.node(group.getName());
            prefs.removeNode();
            //delete all cached files

        } catch( Exception e ) {
            LOG.log(Level.INFO, "Failed to remove document group '" + group.toString() + "'", e); //NOI18N
        }
    }

    private static void deleteAll(File dir) {
        File[] dirContents = dir.listFiles();
        if( null == dirContents ) {
            return;
        }
        for( File f : dirContents ) {
            if( f.isDirectory() ) {
                deleteAll(f);
            }
            f.delete();
        }
    }

    static boolean closeAllDocuments() {
        TopComponent[] tcs = WindowManagerImpl.getInstance().getEditorTopComponents();
        for( TopComponent tc : tcs ) {
            if( !Switches.isClosingEnabled(tc) ) {
                continue;
            }
            tc.putClientProperty("inCloseAll", Boolean.TRUE); //NOI18N
            if( !tc.close() ) {
                return false;
            }
        }

        return true;
    }
}
