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

package org.netbeans.core.windows.persistence;

import java.util.logging.Level;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handle loading/saving of WindowManager configuration data.
 *
 * @author Marek Slama
 */

public class WindowManagerParser {
    
    public static final String INSTANCE_DTD_ID_1_0
        = "-//NetBeans//DTD Window Manager Properties 1.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_1_1
        = "-//NetBeans//DTD Window Manager Properties 1.1//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_0
        = "-//NetBeans//DTD Window Manager Properties 2.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_1
        = "-//NetBeans//DTD Window Manager Properties 2.1//EN"; // NOI18N
    
    private static final boolean DEBUG = Debug.isLoggable(WindowManagerParser.class);
    
    /** Unique wm name */
    private String wmName;
    
    private PersistenceManager pm;
    
    private InternalConfig internalConfig;
    
    private Map<String, ModeParser> modeParserMap = new HashMap<String, ModeParser>(19);
    
    private Map<String, GroupParser> groupParserMap = new HashMap<String, GroupParser>(19);
    
    //Used to collect names of all localy stored wstcref files.
    private Set<String> tcRefNameLocalSet = new HashSet<String>(101);
    
    private static final Object SAVING_LOCK = new Object();
    
    public WindowManagerParser(PersistenceManager pm, String wmName) {
        this.pm = pm;
        this.wmName = wmName;
    }

    /** Load window manager configuration including all modes and tcrefs. */
    WindowManagerConfig load() throws IOException {
        synchronized (SAVING_LOCK) {
            WindowManagerConfig wmc = new WindowManagerConfig();
            readProperties(wmc);
            readModes(wmc);
            readGroups(wmc);
            return wmc;
        }
    }
    
    /** Save window manager configuration including all modes and tcrefs. */
    void save (WindowManagerConfig wmc) throws IOException {
        synchronized (SAVING_LOCK) {
            writeProperties(wmc);
            writeModes(wmc);
            writeGroups(wmc);
        }
    }
    
    /** Extract all ModeConfigs in XML.
     *
     * @param wmc where all knowledge of the ModeConfigs is kept
     * @return All the known modes in the XML configuration that they would
     * be saved to disk as.
     * @throws IOException
     */
    List<String> modeConfigXmls(WindowManagerConfig wmc) throws IOException {
        synchronized (SAVING_LOCK) {
            Map<String, ModeConfig> modeConfigMap = gatherModeConfigs(wmc);
            return xmlModeConfigs(modeConfigMap);
        }
    }
    
    /** Get a ModeConfig as XML.
     * 
     * @param modeName the name of the Mode for which we want the XML
     * @param wmc where all knowledge of the ModeConfigs is kept
     * @return XML String of the Mode with modeName
     * @throws IOException 
     */
    public String modeConfigXml(String modeName, WindowManagerConfig wmc) throws IOException {
        synchronized (SAVING_LOCK) {
            Map<String, ModeConfig> modeConfigMap = gatherModeConfigs(wmc);
            ModeConfig modeConfig = modeConfigMap.get(modeName);
            ModeParser modeParser = modeParserMap.get(modeName);
            String xml = modeParser.modeConfigXml(modeConfig);
            return xml;
        }
    }

    /** Called from ModuleChangeHandler when wsmode file is deleted from module folder.
     * Do not remove ModeParser. Only set that it is not present in module folder.
     * @param modeName unique name of mode
     */
    void removeMode (String modeName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeMode" + " mo:" + modeName);
            ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
            if (modeParser != null) {
                modeParser.setInModuleFolder(false);
            }
            //deleteLocalMode(modeName);
        }
    }
    
    /** Called from ModuleChangeHandler when wsmode file is added to module folder.
     * Adds ModeParser.
     * @param modeName unique name of mode
     */
    ModeConfig addMode (String modeName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "addMode ENTER" + " mo:" + modeName);
            ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
            if (modeParser == null) {
                //Create new ModeParser if it does not exist.
                modeParser = ModeParser.parseFromFileObject(modeName,tcRefNameLocalSet);
                modeParserMap.put(modeName, modeParser);
            }
            FileObject modesModuleFolder = null;
            try {
                modesModuleFolder = pm.getModesModuleFolder();
            } catch (IOException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.addMode]" // NOI18N
                    + " Cannot get modes folder", exc); // NOI18N
                return null;
            }
            modeParser.setModuleParentFolder(modesModuleFolder);
            modeParser.setInModuleFolder(true);
            ModeConfig modeConfig = null;
            try {
                modeConfig = modeParser.load();
            } catch (IOException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.addMode]" // NOI18N
                + " Warning: Cannot load mode " + modeName, exc); // NOI18N
            }
            return modeConfig;
        }
    }
    
    /** Called from ModuleChangeHandler when wsgrp file is deleted from module folder.
     * Removes GroupParser and cleans wsgrp file from local folder
     * @param groupName unique name of group
     */
    void removeGroup (String groupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeGroup" + " group:" + groupName);
            groupParserMap.remove(groupName);
            deleteLocalGroup(groupName);
        }
    }
    
    /** Called from ModuleChangeHandler when wsgrp file is added to module folder.
     * Adds GroupParser.
     * @param groupName unique name of group
     */
    GroupConfig addGroup (String groupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addGroup ENTER" + " group:" + groupName);
            GroupParser groupParser = (GroupParser) groupParserMap.get(groupName);
            if (groupParser != null) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.addGroup]" // NOI18N
                + " Warning: GroupParser " + groupName // NOI18N
                + " exists but it should not."); // NOI18N
                groupParserMap.remove(groupName);
            }
            groupParser = new GroupParser(groupName);
            FileObject groupsModuleFolder = null;
            try {
                groupsModuleFolder = pm.getGroupsModuleFolder();
            } catch (IOException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.addGroup]" // NOI18N
                    + " Cannot get groups folder", exc); // NOI18N
                return null;
            }
            groupParser.setModuleParentFolder(groupsModuleFolder);
            groupParser.setInModuleFolder(true);
            //FileObject setsLocalFolder = pm.getGroupsLocalFolder();
            //groupParser.setLocalParentFolder(groupsLocalFolder);
            groupParserMap.put(groupName, groupParser);
            GroupConfig groupConfig = null;
            try {
                groupConfig = groupParser.load();
            } catch (IOException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.addGroup]" // NOI18N
                + " Warning: Cannot load group " + groupName, exc); // NOI18N
            }
            return groupConfig;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcref file is deleted from module folder
     * or from package convert when some imported TCRef is deleted from imported module folder.
     * Removes TCRefParser from ModeParser and cleans wstcref file from local folder
     * @param tcRefName unique name of tcRef
     */
    public boolean removeTCRef (String tcRefName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef ENTER" + " tcRef:" + tcRefName);
            ModeParser modeParser = findModeParser(tcRefName);
            if (modeParser == null) {
                //modeParser was already removed -> its local folder was cleaned
                if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef LEAVE 1" + " tcRef:" + tcRefName);
                return false;
            }
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef REMOVING tcRef:" + tcRefName
            + " FROM mo:" + modeParser.getName());  // NOI18N
            modeParser.removeTCRef(tcRefName);
            if (DEBUG) Debug.log(WindowManagerParser.class, "removeTCRef LEAVE 2" + " tcRef:" + tcRefName);
            return true;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcref file is added to module folder.
     * Adds TCRefParser to ModeParser.     
     * @param tcRefName unique name of tcRef
     */
    TCRefConfig addTCRef (String modeName, String tcRefName, List<String> tcRefNameList) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCRef ENTER" + " mo:" + modeName
            + " tcRef:" + tcRefName);  // NOI18N
            ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
            if (modeParser == null) {
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCRef LEAVE 1" + " mo:" + modeName
                + " tcRef:" + tcRefName);
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.addTCRef]" // NOI18N
                + " Warning: Cannot add tcRef " + tcRefName + ". ModeParser " + modeName + " not found."); // NOI18N
                return null;
            }
            TCRefConfig tcRefConfig = modeParser.addTCRef(tcRefName, tcRefNameList);
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCRef LEAVE 2" + " mo:" + modeName
            + " tcRef:" + tcRefName);  // NOI18N
            return tcRefConfig;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcgrp file is deleted from module folder.
     * Removes TCGroupParser from GroupParser and cleans wstcgrp file from local folder
     * @param tcGroupName unique name of tcGroup
     */
    boolean removeTCGroup (String groupName, String tcGroupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeTCGroup ENTER" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            GroupParser groupParser = (GroupParser) groupParserMap.get(groupName);
            if (groupParser == null) {
                //groupParser was already removed -> its local folder was cleaned
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeTCGroup LEAVE 1" + " group:" + groupName
                + " tcGroup:" + tcGroupName);  // NOI18N
                return false;
            }
            groupParser.removeTCGroup(tcGroupName);
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.removeTCGroup LEAVE 2" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            return true;
        }
    }
    
    /** Called from ModuleChangeHandler when wstcgrp file is added to module folder.
     * Adds TCGroupParser to GroupParser.     
     * @param tcGroupName unique name of tcGroup
     */
    TCGroupConfig addTCGroup (String groupName, String tcGroupName) {
        synchronized (SAVING_LOCK) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCGroup ENTER" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            GroupParser groupParser = (GroupParser) groupParserMap.get(groupName);
            if (groupParser == null) {
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCGroup LEAVE 1" + " group:" + groupName
                + " tcGroup:" + tcGroupName);  // NOI18N
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.addTCGroup]" // NOI18N
                + " Warning: Cannot add tcGroup " + tcGroupName + ". GroupParser " + groupName + " not found."); // NOI18N
                return null;
            }
            TCGroupConfig tcGroupConfig = groupParser.addTCGroup(tcGroupName);
            if (DEBUG) Debug.log(WindowManagerParser.class, "WMParser.addTCGroup LEAVE 2" + " group:" + groupName
            + " tcGroup:" + tcGroupName);  // NOI18N
            return tcGroupConfig;
        }
    }
    
    /** Called from import to pass module info to new parser.
     * Adds TCRefParser to ModeParser.     
     * @param tcRefName unique name of tcRef
     */
    public void addTCRefImport (String modeName, String tcRefName, InternalConfig internalCfg) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "addTCRefImport ENTER" + " mo:" + modeName
        + " tcRef:" + tcRefName);  // NOI18N
        ModeParser modeParser = (ModeParser) modeParserMap.get(modeName);
        if (modeParser == null) {
            if (DEBUG) Debug.log(WindowManagerParser.class, "addTCRefImport LEAVE 1" + " mo:" + modeName
            + " tcRef:" + tcRefName);  // NOI18N
            PersistenceManager.LOG.log(Level.INFO,
            "[WinSys.WindowManagerParser.addTCRef]" // NOI18N
            + " Warning: Cannot add tcRef " + tcRefName // NOI18N
            + ". ModeParser " + modeName + " not found."); // NOI18N
            return;
        }
        modeParser.addTCRefImport(tcRefName, internalCfg);
        if (DEBUG) Debug.log(WindowManagerParser.class, "addTCRefImport LEAVE 2" + " mo:" + modeName
        + " tcRef:" + tcRefName);  // NOI18N
    }
    
    /** Finds ModeParser containing TCRef with given ID. Returns null if such ModeParser
     * is not found.
     * @param tcRefName unique name of tcRef
     */
    ModeParser findModeParser (String tcRefName) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "findModeParser ENTER" + " tcRef:" + tcRefName);
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            TCRefParser tcRefParser = modeParser.findTCRefParser(tcRefName);
            if (tcRefParser != null) {
                return modeParser;
            }
        }
        return null;
    }
    
    private void readProperties (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "readProperties ENTER");
        PropertyHandler propertyHandler = new PropertyHandler();
        internalConfig = new InternalConfig();
        propertyHandler.readData(wmc, internalConfig);
        if (DEBUG) Debug.log(WindowManagerParser.class, "readProperties LEAVE");
    }
    
    private void readModes (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "readModes ENTER");
        
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            modeParser.setInModuleFolder(false);
            modeParser.setInLocalFolder(false);
        }
        
        FileObject modesModuleFolder = pm.getRootModuleFolder().getFileObject(PersistenceManager.MODES_FOLDER);
        //if (DEBUG) Debug.log(WindowManagerParser.class, "modesModuleFolder: " + modesModuleFolder);
        if (modesModuleFolder != null) {
            FileObject [] files = modesModuleFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.MODE_EXT.equals(files[i].getExt())) {
                    //wsmode file
                    ModeParser modeParser = (ModeParser) modeParserMap.get(files[i].getName());
                    if (modeParser == null) {
                        modeParser = ModeParser.parseFromFileObject(files[i].getName(),tcRefNameLocalSet);
                        modeParserMap.put(files[i].getName(), modeParser);
                    }
                    modeParser.setInModuleFolder(true);
                    modeParser.setModuleParentFolder(modesModuleFolder);
                }
            }
        }
        
        FileObject modesLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.MODES_FOLDER);
        //if (DEBUG) Debug.log(WindowManagerParser.class, " modesLocalFolder: " + modesLocalFolder);
        tcRefNameLocalSet.clear();
        if (modesLocalFolder != null) {
            FileObject [] files = modesLocalFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.MODE_EXT.equals(files[i].getExt())) {
                    //wsmode file
                    ModeParser modeParser;
                    if (modeParserMap.containsKey(files[i].getName())) {
                        modeParser = (ModeParser) modeParserMap.get(files[i].getName());
                    } else {
                        modeParser = ModeParser.parseFromFileObject(files[i].getName(),tcRefNameLocalSet);
                        modeParserMap.put(files[i].getName(), modeParser);
                    }
                    modeParser.setInLocalFolder(true);
                    modeParser.setLocalParentFolder(modesLocalFolder);
                }
                //Look for wstcref file in local folder
                if (files[i].isFolder()) {
                    FileObject [] subFiles = files[i].getChildren();
                    for (int j = 0; j < subFiles.length; j++) {
                        if (!subFiles[j].isFolder() && PersistenceManager.TCREF_EXT.equals(subFiles[j].getExt())) {
                            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- name: [" + files[i].getName() + "][" + subFiles[j].getName() + "]");
                            tcRefNameLocalSet.add(subFiles[j].getName());
                        }
                    }
                }
            }
        }
        
        /*for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            if (DEBUG) Debug.log(WindowManagerParser.class, "modeParser: " + modeParser.getName()
            + " isInModuleFolder:" + modeParser.isInModuleFolder()
            + " isInLocalFolder:" + modeParser.isInLocalFolder());
        }*/
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List<ModeConfig> modeCfgList = new ArrayList<ModeConfig>(modeParserMap.size());
        List<ModeParser> toRemove = new ArrayList<ModeParser>(modeParserMap.size());
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext(); ) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            ModeConfig modeCfg;
            try {
                modeCfg = modeParser.load();
            } catch (IOException exc) {
                //If reading of one Mode fails we want to log message
                //and continue.
                Logger.getLogger(WindowManagerParser.class.getName()).log(Level.INFO, null, exc);
                continue;
            }
            boolean modeAccepted = acceptMode(modeParser, modeCfg);
            if (modeAccepted) {
                modeCfgList.add(modeCfg);
            } else {
                toRemove.add(modeParser);
                deleteLocalMode(modeParser.getName());
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            ModeParser modeParser = (ModeParser) toRemove.get(i);
            modeParserMap.remove(modeParser.getName());
        }
        
        //remove modes still defined in Windows2 folder if they were merged into some other mode
        mergeModes( modeCfgList );
        
        wmc.modes = modeCfgList.toArray(new ModeConfig[0]);
        
        if (DEBUG) Debug.log(WindowManagerParser.class, "readModes LEAVE");
    }
    
    private void mergeModes( List<ModeConfig> modeCfgList ) {
        Set<String> mergedModes = new HashSet<String>( 20 );
        for( ModeConfig modeConfig : modeCfgList ) {
            if( modeConfig.otherNames == null )
                continue;
            mergedModes.addAll( modeConfig.otherNames );
            ModeParser parser = modeParserMap.get( modeConfig.name );
            if( null != parser ) {
                for( String otherName : modeConfig.otherNames ) {
                    modeParserMap.put( otherName, parser );
                }
            }
        }
        for( String name : mergedModes ) {
            ModeConfig merged = null;
            for( ModeConfig mc : modeCfgList ) {
                if( name.equals( mc.name ) ) {
                    modeCfgList.remove( mc );
                    merged = mc;
                    break;
                }
            }

            if( null != merged && merged.tcRefConfigs.length > 0 ) {
                for( ModeConfig mc : modeCfgList ) {
                    if( null != mc.otherNames && mc.otherNames.contains( merged.name ) ) {
                        List<TCRefConfig> refs = new ArrayList<TCRefConfig>( Arrays.asList( mc.tcRefConfigs ) );
                        for( TCRefConfig tcrf : merged.tcRefConfigs ) {
                            if( !refs.contains( tcrf ) ) {
                                refs.add( tcrf );
                            }
                        }
                        mc.tcRefConfigs = refs.toArray(new TCRefConfig[0] );
                        break;
                    }
                }
            }
            
            ModeParser parser = modeParserMap.remove( name );
        }
    }
    
    /** Checks if module for given mode exists.
     * @return true if mode is valid - its module exists
     */
    private boolean acceptMode (ModeParser modeParser, ModeConfig config) {
        InternalConfig cfg = modeParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
            (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
             cfg.moduleSpecificationVersion);
            if (curModuleInfo == null) {
                PersistenceManager.LOG.info("Cannot find module \'" +
                          cfg.moduleCodeNameBase + " " + cfg.moduleCodeNameRelease + " " + 
                          cfg.moduleSpecificationVersion + "\' for wsmode with name \'" + config.name + "\'"); // NOI18N
            }
            if ((curModuleInfo != null) && curModuleInfo.isEnabled()) {
                //Module is present and is enabled
                return true;
            } else {
                //Module is NOT present (it could be deleted offline)
                //or is NOT enabled
                return false;
            }
        } else {
            //No module info
            return true;
        }
    }
    
    private void readGroups (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups ENTER");
        
        for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            groupParser.setInModuleFolder(false);
            groupParser.setInLocalFolder(false);
        }
        
        FileObject groupsModuleFolder = pm.getRootModuleFolder().getFileObject(PersistenceManager.GROUPS_FOLDER);
        //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups groupsModuleFolder: " + groupsModuleFolder);
        
        if (groupsModuleFolder != null) {
            FileObject [] files;
            files = groupsModuleFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.GROUP_EXT.equals(files[i].getExt())) {
                    GroupParser groupParser;
                    //wsgrp file
                    if (groupParserMap.containsKey(files[i].getName())) {
                        groupParser = (GroupParser) groupParserMap.get(files[i].getName());
                    } else {
                        groupParser = new GroupParser(files[i].getName());
                        groupParserMap.put(files[i].getName(), groupParser);
                    }
                    groupParser.setInModuleFolder(true);
                    groupParser.setModuleParentFolder(groupsModuleFolder);
                }
            }
        }
        
        FileObject groupsLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.GROUPS_FOLDER);
        if (groupsLocalFolder != null) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups groupsLocalFolder: " + groupsLocalFolder);
            FileObject [] files = groupsLocalFolder.getChildren();
            for (int i = 0; i < files.length; i++) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups fo[" + i + "]: " + files[i]);
                if (!files[i].isFolder() && PersistenceManager.GROUP_EXT.equals(files[i].getExt())) {
                    //wsgrp file
                    GroupParser groupParser;
                    if (groupParserMap.containsKey(files[i].getName())) {
                        groupParser = (GroupParser) groupParserMap.get(files[i].getName());
                    } else {
                        groupParser = new GroupParser(files[i].getName());
                        groupParserMap.put(files[i].getName(), groupParser);
                    }
                    groupParser.setInLocalFolder(true);
                    groupParser.setLocalParentFolder(groupsLocalFolder);
                }
            }
        }
        
        /*for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups groupParser: " + groupParser.getName()
            + " isInModuleFolder:" + groupParser.isInModuleFolder()
            + " isInLocalFolder:" + groupParser.isInLocalFolder());
        }*/
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List<GroupConfig> groupCfgList = new ArrayList<GroupConfig>(groupParserMap.size());
        List<GroupParser> toRemove = new ArrayList<GroupParser>(groupParserMap.size());
        for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            GroupConfig groupCfg;
            try {
                groupCfg = groupParser.load();
            } catch (IOException exc) {
                //If reading of one group fails we want to log message
                //and continue.
                Logger.getLogger(WindowManagerParser.class.getName()).log(Level.INFO, null, exc);
                continue;
            }
            boolean groupAccepted = acceptGroup(groupParser, groupCfg);
            if (groupAccepted) {
                groupCfgList.add(groupCfg);
            } else {
                toRemove.add(groupParser);
                deleteLocalGroup(groupParser.getName());
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            GroupParser groupParser = (GroupParser) toRemove.get(i);
            groupParserMap.remove(groupParser.getName());
        }
        
        wmc.groups = groupCfgList.toArray(new GroupConfig[0]);
        
        if (DEBUG) Debug.log(WindowManagerParser.class, "readGroups LEAVE");
    }
    
    /** Checks if module for given group exists.
     * @return true if group is valid - its module exists
     */
    private boolean acceptGroup (GroupParser groupParser, GroupConfig config) {
        InternalConfig cfg = groupParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
                                        (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
                                         cfg.moduleSpecificationVersion);
            if (curModuleInfo == null) {
                
                PersistenceManager.LOG.log(Level.FINE, "Cannot find module \'" +
                          cfg.moduleCodeNameBase + " " + cfg.moduleCodeNameRelease + " " + 
                          cfg.moduleSpecificationVersion + "\' for group with name \'" + config.name + "\'"); // NOI18N
                
            }
            if ((curModuleInfo != null) && curModuleInfo.isEnabled()) {
                //Module is present and is enabled
                return true;
            } else {
                return false;
            }
        } else {
            //No module info
            return true;
        }
    }
    
    private void writeProperties (WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeProperties ENTER");
        PropertyHandler propertyHandler = new PropertyHandler();
        propertyHandler.writeData(wmc);
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeProperties LEAVE");
    }
    
    private void writeModes(WindowManagerConfig wmc) throws IOException {
        if (DEBUG) {
            Debug.log(WindowManagerParser.class, "writeModes ENTER");
        }

        Map<String, ModeConfig> modeConfigMap = gatherModeConfigs(wmc);

        saveModeConfigs(modeConfigMap);

        if (DEBUG) {
            Debug.log(WindowManagerParser.class, "writeModes LEAVE");
        }
    }

    private void saveModeConfigs(Map<String, ModeConfig> modeConfigMap) throws IOException {
        FileObject modesLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.MODES_FOLDER);
        if ((modesLocalFolder == null) && (modeParserMap.size() > 0)) {
            modesLocalFolder = pm.getModesLocalFolder();
        }
        //Step 3: Save all modes
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext();) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            modeParser.setLocalParentFolder(modesLocalFolder);
            modeParser.setInLocalFolder(true);
            modeParser.save((ModeConfig) modeConfigMap.get(modeParser.getName()));
        }
    }

    private List<String> xmlModeConfigs(Map<String, ModeConfig> modeConfigMap) throws IOException {
        // Convert ModeConfigs into xml.
        List<String> modeConfigXmls = new ArrayList<>();
        for (Iterator it = modeParserMap.keySet().iterator(); it.hasNext();) {
            ModeParser modeParser = (ModeParser) modeParserMap.get(it.next());
            String xml = modeParser.modeConfigXml((ModeConfig) modeConfigMap.get(modeParser.getName()));
            modeConfigXmls.add(xml);
        }
        return modeConfigXmls;
    }

    /**
     * Cleans obsolete ModeParsers and creates missing ModeParsers, populating
     * modeParserMap
     *
     * @param wmc where all knowledge of the ModeConfigs is kept
     * @return named index of Mode configurations
     */
    private Map<String, ModeConfig> gatherModeConfigs(WindowManagerConfig wmc) {
        //Step 1: Clean obsolete mode parsers
        Map<String, ModeConfig> modeConfigMap = new HashMap<String, ModeConfig>();
        for (int i = 0; i < wmc.modes.length; i++) {
            modeConfigMap.put(wmc.modes[i].name, wmc.modes[i]);
        }
        List<String> toDelete = new ArrayList<String>(10);
        for (ModeParser modeParser: modeParserMap.values()) {
            if (!modeConfigMap.containsKey(modeParser.getName())) {
                toDelete.add(modeParser.getName());
            }
        }
        for (int i = 0; i < toDelete.size(); i++) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeModes ** REMOVE FROM MAP modeParser: " + toDelete.get(i));
            modeParserMap.remove(toDelete.get(i));
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeModes ** DELETE modeParser: " + toDelete.get(i));
            deleteLocalMode(toDelete.get(i));
        }
        
        //Step 2: Create missing mode parsers
        for (int i = 0; i < wmc.modes.length; i++) {
            if (!modeParserMap.containsKey(wmc.modes[i].name)) {
                ModeParser modeParser = ModeParser.parseFromFileObject(wmc.modes[i].name,tcRefNameLocalSet);
                modeParserMap.put(wmc.modes[i].name, modeParser);
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeModes ** CREATE modeParser:" + modeParser.getName());
            }
        }
        return modeConfigMap;
    }

    private void writeGroups(WindowManagerConfig wmc) throws IOException {
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups ENTER");
        //Step 1: Clean obsolete group parsers
        Map<String, GroupConfig> groupConfigMap = new HashMap<String, GroupConfig>();
        //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups List of groups to be saved:");
        for (int i = 0; i < wmc.groups.length; i++) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups group[" + i + "]: " + wmc.groups[i].name);
            groupConfigMap.put(wmc.groups[i].name, wmc.groups[i]);
        }
        List<String> toDelete = new ArrayList<String>(10);
        for (GroupParser groupParser: groupParserMap.values()) {
            if (!groupConfigMap.containsKey(groupParser.getName())) {
                toDelete.add(groupParser.getName());
            }
        }
        for (int i = 0; i < toDelete.size(); i++) {
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeGroups ** REMOVE FROM MAP groupParser: " + toDelete.get(i));
            groupParserMap.remove(toDelete.get(i));
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeGroups ** DELETE groupParser: " + toDelete.get(i));
            deleteLocalGroup(toDelete.get(i));
        }
        //Step 2: Create missing group parsers
        for (int i = 0; i < wmc.groups.length; i++) {
            if (!groupParserMap.containsKey(wmc.groups[i].name)) {
                GroupParser groupParser = new GroupParser(wmc.groups[i].name);
                groupParserMap.put(wmc.groups[i].name, groupParser);
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.writeGroups ** CREATE groupParser:" + groupParser.getName());
            }
        }
        //Step 3: Save all groups
        FileObject groupsLocalFolder = pm.getRootLocalFolder().getFileObject(PersistenceManager.GROUPS_FOLDER);
        if ((groupsLocalFolder == null) && (groupParserMap.size() > 0)) {
            groupsLocalFolder = pm.getGroupsLocalFolder();
        }
        //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups groupsLocalFolder:" + groupsLocalFolder);
        for (Iterator it = groupParserMap.keySet().iterator(); it.hasNext(); ) {
            GroupParser groupParser = (GroupParser) groupParserMap.get(it.next());
            groupParser.setLocalParentFolder(groupsLocalFolder);
            groupParser.setInLocalFolder(true);
            //if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups save group:" + groupParser.getName());
            groupParser.save((GroupConfig) groupConfigMap.get(groupParser.getName()));
        }
        
        if (DEBUG) Debug.log(WindowManagerParser.class, "writeGroups LEAVE");
    }
    
    private void deleteLocalMode (String modeName) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "deleteLocalMode" + " mo:" + modeName);
        FileObject rootFO = null;
        try {
            rootFO = pm.getRootLocalFolder();
        } catch (IOException exc) {
            PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.deleteLocalMode]" // NOI18N
                + " Cannot get root local folder", exc); // NOI18N
            return;
        }
        FileObject modesLocalFolder = rootFO.getFileObject(PersistenceManager.MODES_FOLDER);
        if (modesLocalFolder == null) {
            return;
        }
        FileObject modeFO;
        modeFO = modesLocalFolder.getFileObject(modeName);
        if (modeFO != null) {
            PersistenceManager.deleteOneFO(modeFO);
        }
        modeFO = modesLocalFolder.getFileObject(modeName, PersistenceManager.MODE_EXT);
        if (modeFO != null) {
            PersistenceManager.deleteOneFO(modeFO);
        }
    }
    
    private void deleteLocalGroup (String groupName) {
        if (DEBUG) Debug.log(WindowManagerParser.class, "deleteLocalGroup" + " groupName:" + groupName);
        FileObject rootFO = null;
        try {
            rootFO = pm.getRootLocalFolder();
        } catch (IOException exc) {
            PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.deleteLocalGroup]" // NOI18N
                + " Cannot get root local folder", exc); // NOI18N
            return;
        }
        FileObject groupsLocalFolder = rootFO.getFileObject(PersistenceManager.GROUPS_FOLDER);
        if (groupsLocalFolder == null) {
            return;
        }
        FileObject groupFO;
        groupFO = groupsLocalFolder.getFileObject(groupName);
        if (groupFO != null) {
            PersistenceManager.deleteOneFO(groupFO);
        }
        groupFO = groupsLocalFolder.getFileObject(groupName, PersistenceManager.GROUP_EXT);
        if (groupFO != null) {
            PersistenceManager.deleteOneFO(groupFO);
        }
    }
    
    String getName () {
        return wmName;
    }
    
    void log (String s) {
        if (DEBUG) {
            Debug.log(WindowManagerParser.class, s);
        }
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** WindowManager configuration data */
        private WindowManagerConfig winMgrConfig = null;
        
        /** Internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** List to store parsed path items */
        private List<SplitConstraint> itemList = new ArrayList<SplitConstraint>(10);
        
        /** List to store parsed tc-ids */
        private List<String> tcIdList = new ArrayList<String>(10);
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () throws IOException {
            FileObject rootFolder;

            rootFolder = pm.getRootLocalFolder();

            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " rootFolder:" + rootFolder);

            FileObject wmConfigFO;
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " looking for LOCAL");
            wmConfigFO = rootFolder.getFileObject
            (WindowManagerParser.this.getName(), PersistenceManager.WINDOWMANAGER_EXT);
            if (wmConfigFO != null) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " wmConfigFO LOCAL:" + wmConfigFO);
                return wmConfigFO;
            } else {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " LOCAL not found");
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " looking for MODULE");
                //Local data not found, try module
                rootFolder = pm.getRootModuleFolder();
                wmConfigFO = rootFolder.getFileObject
                (WindowManagerParser.this.getName(), PersistenceManager.WINDOWMANAGER_EXT);

                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOInput" + " wmConfigFO MODULE:" + wmConfigFO);

                return wmConfigFO;
            }
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject rootFolder;
            rootFolder = pm.getRootLocalFolder();
            
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " rootFolder:" + rootFolder);
            
            FileObject wmConfigFO;
            //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " looking for LOCAL");
            wmConfigFO = rootFolder.getFileObject
            (WindowManagerParser.this.getName(), PersistenceManager.WINDOWMANAGER_EXT);
            if (wmConfigFO != null) {
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " wmConfigFO LOCAL:" + wmConfigFO);
                return wmConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(WindowManagerParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.WINDOWMANAGER_EXT);
                wmConfigFO = FileUtil.createData(rootFolder, buffer.toString());
                //if (DEBUG) Debug.log(WindowManagerParser.class, "-- WMParser.getConfigFOOutput" + " LOCAL not found CREATE");
                return wmConfigFO;
            }
        }
        
        /** 
         Reads window manager configuration data from XML file. 
         Data are returned in output params.
         */
        void readData (WindowManagerConfig winMgrCfg, InternalConfig internalCfg)
        throws IOException {
            winMgrConfig = winMgrCfg;
            internalConfig = internalCfg;
            itemList.clear();
            tcIdList.clear();
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing Window Manager configuration file");
            }
            InputStream is = null;
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*InputStream is = cfgFOInput.getInputStream();
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    if (DEBUG) Debug.log(WindowManagerParser.class, "DUMP WindowManager:");
                    String s = new String(arr);
                    if (DEBUG) Debug.log(WindowManagerParser.class, s);*/
                    //DUMP END
//                    long time = System.currentTimeMillis();
                    is = cfgFOInput.getInputStream();
                    PersistenceManager.getDefault().getXMLParser(this).parse(new InputSource(is));
//                    System.out.println("WindowManagerParser.readData "+(System.currentTimeMillis()-time));
                }
            } catch (SAXException exc) {
                // Turn into annotated IOException
                String msg = NbBundle.getMessage(WindowManagerParser.class,
                                                 "EXC_WindowManagerParse",
                                                 cfgFOInput);

                throw (IOException) new IOException(msg).initCause(exc);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException exc) {
                    Logger.getLogger(WindowManagerParser.class.getName()).log(Level.INFO, null, exc);
                }
            }
            
            winMgrConfig.editorAreaConstraints =
                itemList.toArray(new SplitConstraint[0]);
            winMgrConfig.tcIdViewList = 
                tcIdList.toArray(new String[0]);
            winMgrCfg = winMgrConfig;
            internalCfg = internalConfig;
            
            winMgrConfig = null;
            internalConfig = null;
        }

        @Override
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) throws SAXException {
            if ("windowmanager".equals(qname)) { // NOI18N
                handleWindowManager(attrs);
            } else if (internalConfig.specVersion != null
                    && internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) >= 0) { //NOI18N
                //Parse version 2.0 and 2.1
                if ("main-window".equals(qname)) { // NOI18N
                    handleMainWindow(attrs);
                } else if ("joined-properties".equals(qname)) { // NOI18N
                    handleJoinedProperties(attrs);
                } else if ("separated-properties".equals(qname)) { // NOI18N
                    handleSeparatedProperties(attrs);
                } else if ("editor-area".equals(qname)) { // NOI18N
                    handleEditorArea(attrs);
                } else if ("constraints".equals(qname)) { // NOI18N
                    handleConstraints(attrs);
                } else if ("path".equals(qname)) { // NOI18N
                    handlePath(attrs);
                } else if ("bounds".equals(qname)) { // NOI18N
                    handleEditorAreaBounds(attrs);
                } else if ("relative-bounds".equals(qname)) { // NOI18N
                    handleEditorAreaRelativeBounds(attrs);
                } else if ("screen".equals(qname)) { // NOI18N
                    handleScreen(attrs);
                } else if ("active-mode".equals(qname)) { // NOI18N
                    handleActiveMode(attrs);
                } else if ("maximized-mode".equals(qname)) { // NOI18N
                    handleMaximizedMode(attrs);
                } else if ("toolbar".equals(qname)) { // NOI18N
                    handleToolbar(attrs);
                } else if ("tc-id".equals(qname)) { // NOI18N
                    handleTcId(attrs);
                } else if ("tcref-item".equals(qname)) { // NOI18N
                    handleTCRefItem(attrs);
                }
            } else {
                if (DEBUG) Debug.log(WindowManagerParser.class, "WMP.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }

        @Override
        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }

        /** Reads element "windowmanager" */
        private void handleWindowManager (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleWindowManager]" // NOI18N
                + " Missing attribute \"version\" of element \"windowmanager\"."); // NOI18N
                internalConfig.specVersion = new SpecificationVersion("2.0"); // NOI18N
            }
        }
        
        /** Reads element "main-window" and updates window manager config content */
        private void handleMainWindow (Attributes attrs) {
        }
        
        /** Reads element "joined-properties" and updates window manager config content */
        private void handleJoinedProperties (Attributes attrs) {
            String s;
            try {
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    winMgrConfig.xJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.xJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"x\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.xJoined = -1;
            }
            
            try {
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    winMgrConfig.yJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.yJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"y\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.yJoined = -1;
            }
            
            try {
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    winMgrConfig.widthJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.widthJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"width\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.widthJoined = -1;
            }
            
            try {
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    winMgrConfig.heightJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.heightJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"height\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.heightJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-x"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeXJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeXJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-x\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.relativeXJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-y"); // NOI18N
                
                if (s != null) {
                    winMgrConfig.relativeYJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeYJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-y\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.relativeYJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-width"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeWidthJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeWidthJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-width\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.relativeWidthJoined = -1;
            }
            
            try {
                s = attrs.getValue("relative-height"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeHeightJoined = floatParse(s);
                } else {
                    winMgrConfig.relativeHeightJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-height\"" // NOI18N
                + " of element \"joined-properties\".",exc); // NOI18N
                winMgrConfig.relativeHeightJoined = -1;
            }
            
            s = attrs.getValue("centered-horizontally"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallyJoined = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallyJoined = false;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-horizontally\"" // NOI18N
                    + " of element \"joined-properties\"."); // NOI18N
                    winMgrConfig.centeredHorizontallyJoined = false;
                }
            } else {
                winMgrConfig.centeredHorizontallyJoined = false;
            }
            
            s = attrs.getValue("centered-vertically"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallyJoined = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallyJoined = false;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-vertically\"" // NOI18N
                    + " of element \"joined-properties\"."); // NOI18N
                    winMgrConfig.centeredVerticallyJoined = false;
                }
            } else {
                winMgrConfig.centeredVerticallyJoined = false;
            }
            
            try {
                s = attrs.getValue("maximize-if-width-below"); // NOI18N
                if (s != null) {
                    winMgrConfig.maximizeIfWidthBelowJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.maximizeIfWidthBelowJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"maximize-if-width-below\"" // NOI18N
                + " of element \"joined-properties\".", exc); // NOI18N
                winMgrConfig.maximizeIfWidthBelowJoined = -1;
            }
            
            try {
                s = attrs.getValue("maximize-if-height-below"); // NOI18N
                if (s != null) {
                    winMgrConfig.maximizeIfHeightBelowJoined = Integer.parseInt(s);
                } else {
                    winMgrConfig.maximizeIfHeightBelowJoined = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"maximize-if-height-below\"" // NOI18N
                + " of element \"joined-properties\".", exc);
                winMgrConfig.maximizeIfHeightBelowJoined = -1;
            }
            
            String frameState = attrs.getValue("frame-state"); // NOI18N
            if (frameState != null) {
                try {
                    winMgrConfig.mainWindowFrameStateJoined = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleJoinedProperties]" // NOI18N
                    + " Warning: Cannot read attribute \"frame-state\"" // NOI18N
                    + " of element \"joined-properties\".", exc); // NOI18N
                    winMgrConfig.mainWindowFrameStateJoined = Frame.NORMAL;
                }
            } else {
                winMgrConfig.mainWindowFrameStateJoined = Frame.NORMAL;
            }
        }
        
        /** Reads element "separated-properties" and updates window manager config content */
        private void handleSeparatedProperties (Attributes attrs) {
            String s;
            try {
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    winMgrConfig.xSeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.xSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"x\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.xSeparated = -1;
            }
            
            try {
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    winMgrConfig.ySeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.ySeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"y\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.ySeparated = -1;
            }
            
            try {
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    winMgrConfig.widthSeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.widthSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"width\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.widthSeparated = -1;
            }
            
            try {
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    winMgrConfig.heightSeparated = Integer.parseInt(s);
                } else {
                    winMgrConfig.heightSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"height\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.heightSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-x"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeXSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeXSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-x\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.relativeXSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-y"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeYSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeYSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-y\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.relativeYSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-width"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeWidthSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeWidthSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-width\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.relativeWidthSeparated = -1;
            }
            
            try {
                s = attrs.getValue("relative-height"); // NOI18N
                if (s != null) {
                    winMgrConfig.relativeHeightSeparated = floatParse(s);
                } else {
                    winMgrConfig.relativeHeightSeparated = -1;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                + " Warning: Cannot read attribute \"relative-height\"" // NOI18N
                + " of element \"separated-properties\".", exc); // NOI18N
                winMgrConfig.relativeHeightSeparated = -1;
            }
            
            s = attrs.getValue("centered-horizontally"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallySeparated = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredHorizontallySeparated = false;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-horizontally\"" // NOI18N
                    + " of element \"separated-properties\"."); // NOI18N
                    winMgrConfig.centeredHorizontallySeparated = false;
                }
            } else {
                winMgrConfig.centeredHorizontallySeparated = false;
            }
            
            s = attrs.getValue("centered-vertically"); // NOI18N
            if (s != null) {
                if ("true".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallySeparated = true;
                } else if ("false".equals(s)) { // NOI18N
                    winMgrConfig.centeredVerticallySeparated = false;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                    + " Warning: Invalid value of attribute \"centered-vertically\"" // NOI18N
                    + " of element \"separated-properties\"."); // NOI18N
                    winMgrConfig.centeredVerticallySeparated = false;
                }
            } else {
                winMgrConfig.centeredVerticallySeparated = false;
            }
            
            String frameState = attrs.getValue("frame-state"); // NOI18N
            if (frameState != null) {
                try {
                    winMgrConfig.mainWindowFrameStateSeparated = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleSeparatedProperties]" // NOI18N
                    + " Warning: Cannot read attribute \"frame-state\"" // NOI18N
                    + " of element \"separated-properties\".", exc); // NOI18N
                    winMgrConfig.mainWindowFrameStateSeparated = Frame.NORMAL;
                }
            } else {
                winMgrConfig.mainWindowFrameStateSeparated = Frame.NORMAL;
            }
        }
        
        /** Reads element "editor-area" */
        private void handleEditorArea (Attributes attrs) {
            String state = attrs.getValue("state"); // NOI18N
            if (state != null) {
                if ("joined".equals(state)) {
                    winMgrConfig.editorAreaState = Constants.EDITOR_AREA_JOINED;
                } else if ("separated".equals(state)) {
                    winMgrConfig.editorAreaState = Constants.EDITOR_AREA_SEPARATED;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorArea]" // NOI18N
                    + " Warning: Invalid value of attribute \"state\"" // NOI18N
                    + " of element \"editor-area\"."); // NOI18N
                    winMgrConfig.editorAreaState = Constants.EDITOR_AREA_JOINED;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleEditorArea]" // NOI18N
                + " Warning: Missing value of attribute \"state\"" // NOI18N
                + " of element \"editor-area\"."); // NOI18N
                winMgrConfig.editorAreaState = Constants.EDITOR_AREA_JOINED;
            }
            String frameState = attrs.getValue("frame-state"); // NOI18N
            if (frameState != null) {
                try {
                    winMgrConfig.editorAreaFrameState = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorArea]" // NOI18N
                    + " Warning: Cannot read attribute \"frame-state\"" // NOI18N
                    + " of element \"editor-area\".", exc); // NOI18N
                    winMgrConfig.editorAreaFrameState = Frame.NORMAL;
                }
            } else {
                winMgrConfig.editorAreaFrameState = Frame.NORMAL;
            }
        }
        
        /** Reads element "constraints" */
        private void handleConstraints (Attributes attrs) {
        }
        
        /** Reads element "path" */
        private void handlePath (Attributes attrs) {
            String s = attrs.getValue("orientation"); // NOI18N
            int orientation;
            if ("horizontal".equals(s)) { // NOI18N
                orientation = Constants.HORIZONTAL;
            } else if ("vertical".equals(s)) { // NOI18N
                orientation = Constants.VERTICAL;
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                + " Invalid or missing value of attribute \"orientation\"."); // NOI18N
                orientation = Constants.VERTICAL;
            }
            
            int number;
            try {
                s = attrs.getValue("number"); // NOI18N
                if (s != null) {
                    number = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                    + " Missing value of attribute \"number\"."); // NOI18N
                    number = 0;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                + " Cannot read element \"path\", attribute \"number\"", exc); // NOI18N
                number = 0;
            }
            
            double weight;
            try {
                s = attrs.getValue("weight"); // NOI18N
                if (s != null) {
                    weight = Double.parseDouble(s);
                } else {
                    //Not required attribute, provide default value
                    weight = 0.5;
                }
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handlePath]" // NOI18N
                + " Warning: Cannot read element \"path\", attribute \"weight\".", exc); // NOI18N
                weight = 0.5;
            }
            SplitConstraint item = new SplitConstraint(orientation, number, weight);
            itemList.add(item);
        }
        
        /** Reads element "screen" and updates window manager config content */
        private void handleScreen (Attributes attrs) {
            try {
                String s;
                winMgrConfig.screenSize = null;
                int width, height;
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleScreen]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"screen\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                        "[WinSys.WindowManagerParser.handleScreen]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"screen\"."); // NOI18N
                    return;
                }
                winMgrConfig.screenSize = new Dimension(width, height);
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleScreen]" // NOI18N
                + " Warning: Cannot read element \"screen\".", exc); // NOI18N
            }
        }
        
        /** Reads element "bounds" of editor area and updates window manager config content */
        private void handleEditorAreaBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                winMgrConfig.editorAreaBounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"x\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"bounds\"."); // NOI18N
                    return;
                }
                winMgrConfig.editorAreaBounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleEditorAreaBounds]" // NOI18N
                + " Warning: Cannot read element \"bounds\".", exc); // NOI18N
            }
        }
        
        /** Reads element "relative-bounds" of editor area and updates window manager config content */
        private void handleEditorAreaRelativeBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                winMgrConfig.editorAreaRelativeBounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"x\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                winMgrConfig.editorAreaRelativeBounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleEditorAreaRelativeBounds]" // NOI18N
                + " Warning: Cannot read element \"relative-bounds\".", exc); // NOI18N
            }
        }
        
        /** Reads element "active-mode" and updates window manager config content */
        private void handleActiveMode (Attributes attrs) {
            String name = attrs.getValue("name"); // NOI18N
            if (name != null) {
                winMgrConfig.activeModeName = name;
            } else {
                winMgrConfig.activeModeName = ""; // NOI18N
            }
        }
        
        /** Reads element "maximized-mode" and updates window manager config content */
        private void handleMaximizedMode (Attributes attrs) {
            String name = attrs.getValue("editor"); // NOI18N
            if (name != null) {
                winMgrConfig.editorMaximizedModeName = name;
            } else {
                winMgrConfig.editorMaximizedModeName = ""; // NOI18N
            }
            
            name = attrs.getValue("view"); // NOI18N
            if (name != null) {
                winMgrConfig.viewMaximizedModeName = name;
            } else {
                winMgrConfig.viewMaximizedModeName = ""; // NOI18N
            }
        }
        
        /** Reads element "toolbar" and updates window manager config content */
        private void handleToolbar (Attributes attrs) {
            String configuration = attrs.getValue("configuration"); // NOI18N
            if (configuration != null) {
                winMgrConfig.toolbarConfiguration = configuration;
            } else {
                winMgrConfig.toolbarConfiguration = "";  // NOI18N
            }
            String prefIconSize = attrs.getValue("preferred-icon-size"); // NOI18N
            if (prefIconSize != null) {
                try {
                    winMgrConfig.preferredToolbarIconSize = Integer.parseInt(prefIconSize);
                    if ((winMgrConfig.preferredToolbarIconSize != 16) &&
                        (winMgrConfig.preferredToolbarIconSize != 24)) {
                        
                        PersistenceManager.LOG.log(Level.INFO,
                        "[WinSys.WindowManagerParser.handleToolbar]" // NOI18N
                        + " Warning: Invalid value of attribute \"preferred-icon-size\"" //NOI18N
                        + " of element \"toolbar\": " + winMgrConfig.preferredToolbarIconSize  //NOI18N
                        + ". Fixed to default value 24."); // NOI18N
                        winMgrConfig.preferredToolbarIconSize = 24;
                    }
                } catch (NumberFormatException exc) {
                    
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleToolbar]" // NOI18N
                    + " Warning: Cannot read attribute \"preferred-icon-size\"" //NOI18N
                    + " of element \"toolbar\"." // NOI18N
                    + " Fixed to default value 24.", exc); // NOI18N
                    winMgrConfig.preferredToolbarIconSize = 24;
                }
            } else {
                winMgrConfig.preferredToolbarIconSize = 24;
            }
        }
        
        /** Reads element "tc-id" and updates window manager config content */
        private void handleTcId (Attributes attrs) {
            String id = attrs.getValue("id"); // NOI18N
            if (id != null) {
                if (!"".equals(id)) {
                    tcIdList.add(id);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleTcId]" // NOI18N
                    + " Warning: Empty required attribute \"id\" of element \"tc-id\"."); // NOI18N
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleTcId]" // NOI18N
                + " Warning: Missing required attribute \"id\" of element \"tc-id\"."); // NOI18N
            }
        }
        
        /** Reads element "tcref-item" */
        private void handleTCRefItem (Attributes attrs) {
            String workspaceName = attrs.getValue("workspace"); // NOI18N
            String modeName = attrs.getValue("mode"); // NOI18N
            String tc_id = attrs.getValue("id"); // NOI18N
            
            if (workspaceName != null) {
                if ("".equals(workspaceName)) {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                    + " Warning: Empty required attribute \"workspace\" of element \"tcref-item\"."); // NOI18N
                    return;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                + " Warning: Missing required attribute \"workspace\" of element \"tcref-item\"."); // NOI18N
                return;
            }
            if (modeName != null) {
                if ("".equals(modeName)) {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                    + " Warning: Empty required attribute \"mode\" of element \"tcref-item\"."); // NOI18N
                    return;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                + " Warning: Missing required attribute \"mode\" of element \"tcref-item\"."); // NOI18N
                return;
            }
            if (tc_id != null) {
                if ("".equals(tc_id)) {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                    + " Warning: Empty required attribute \"id\" of element \"tcref-item\"."); // NOI18N
                    return;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.WindowManagerParser.handleTCRefItem]" // NOI18N
                + " Warning: Missing required attribute \"id\" of element \"tcref-item\"."); // NOI18N
                return;
            }
        }
        
        /** Writes data from asociated window manager to the xml representation */
        void writeData (WindowManagerConfig wmc) throws IOException {
            final StringBuffer buff = fillBuffer(wmc);
            synchronized (RW_LOCK) {
                FileObject cfgFOOutput = getConfigFOOutput();
                FileLock lock = null;
                OutputStream os = null;
                OutputStreamWriter osw = null;
                try {
                    lock = cfgFOOutput.lock();
                    os = cfgFOOutput.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    osw.write(buff.toString());
                    //if (DEBUG) Debug.log(WindowManagerParser.class, "-- DUMP WindowManager:");
                    //if (DEBUG) Debug.log(WindowManagerParser.class, buff.toString());
                } finally {
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                    } catch (IOException exc) {
                        Logger.getLogger(WindowManagerParser.class.getName()).log(Level.INFO, null, exc);
                    }
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (WindowManagerConfig wmc) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            String curValue = null;
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"). // NOI18N
            /*buff.append("<!DOCTYPE windowmanager PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Window Manager Properties 2.0//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/windowmanager-properties2_0.dtd\">\n\n"); // NOI18N*/
                append("<windowmanager version=\"2.1\">\n"); // NOI18N
            
            appendMainWindow(wmc, buff);
            appendEditorArea(wmc, buff);
            appendScreen(wmc, buff);
            appendActiveMode(wmc, buff);
            appendMaximizedMode(wmc, buff);
            appendToolbar(wmc, buff);
            appendRecentViewList(wmc, buff);
            
            buff.append("</windowmanager>\n"); // NOI18N
            return buff;
        }
        

        private void appendMainWindow (WindowManagerConfig wmc, StringBuffer buff) {
            buff.append("    <main-window>\n  <joined-properties\n"). // NOI18N
                append("   x=\"").append(wmc.xJoined).append("\"\n"). // NOI18N
                append("   y=\"").append(wmc.yJoined).append("\"\n"). // NOI18N
                append("   width=\"").append(wmc.widthJoined).append("\"\n"). // NOI18N
                append("   height=\"").append(wmc.heightJoined).append("\"\n"). // NOI18N
                append("   relative-x=\"").append(wmc.relativeXJoined).append("\"\n"). // NOI18N
                append("   relative-y=\"").append(wmc.relativeYJoined).append("\"\n"). // NOI18N
                append("   relative-width=\"").append(wmc.relativeWidthJoined).append("\"\n"). // NOI18N
                append("   relative-height=\"").append(wmc.relativeHeightJoined).append("\"\n"). // NOI18N
                append("   centered-horizontally=\"").append(wmc.centeredHorizontallyJoined).append("\"\n"). // NOI18N
                append("   centered-vertically=\"").append(wmc.centeredVerticallyJoined).append("\"\n"). // NOI18N
                append("   maximize-if-width-below=\"").append(wmc.maximizeIfWidthBelowJoined).append("\"\n"). // NOI18N
                append("   maximize-if-height-below=\"").append(wmc.maximizeIfHeightBelowJoined).append("\"\n"). // NOI18N
                append("   frame-state=\"").append(wmc.mainWindowFrameStateJoined).append("\"\n/>\n"). // NOI18N
            
            //SEPARATED
                append("  <separated-properties\n").  // NOI18N
                append("   x=\"").append(wmc.xSeparated).append("\"\n"). // NOI18N
                append("   y=\"").append(wmc.ySeparated).append("\"\n"). // NOI18N
                append("   width=\"").append(wmc.widthSeparated).append("\"\n"). // NOI18N
                append("   height=\"").append(wmc.heightSeparated).append("\"\n"). // NOI18N
                append("   relative-x=\"").append(wmc.relativeXSeparated).append("\"\n"). // NOI18N
                append("   relative-y=\"").append(wmc.relativeYSeparated).append("\"\n"). // NOI18N
                append("   relative-width=\"").append(wmc.relativeWidthSeparated).append("\"\n"). // NOI18N
                append("   relative-height=\"").append(wmc.relativeHeightSeparated).append("\"\n"). // NOI18N
                append("   centered-horizontally=\"").append(wmc.centeredHorizontallySeparated).append("\"\n"). // NOI18N
                append("   centered-vertically=\"").append(wmc.centeredVerticallySeparated).append("\"\n"). // NOI18N
                append("   frame-state=\"").append(wmc.mainWindowFrameStateSeparated).append("\"\n"). // NOI18N
                append("/>\n  </main-window>\n"); // NOI18N
        }
        
        private void appendEditorArea (WindowManagerConfig wmc, StringBuffer buff) {
            buff.append("    <editor-area state=\""); // NOI18N
            if (wmc.editorAreaState == Constants.EDITOR_AREA_JOINED) {
                buff.append("joined"); // NOI18N
            } else {
                buff.append("separated"); // NOI18N
            }
            buff.append("\" frame-state=\"").append(wmc.editorAreaFrameState).append("\">\n"); // NOI18N
            
            //BEGIN Write constraints
            buff.append("  <constraints>\n"); // NOI18N
            for (int i = 0; i < wmc.editorAreaConstraints.length; i++) {
                SplitConstraint item = wmc.editorAreaConstraints[i];
                buff.append("  <path orientation=\""); // NOI18N
                if (item.orientation == Constants.HORIZONTAL) {
                    buff.append("horizontal"); // NOI18N
                } else {
                    buff.append("vertical"); // NOI18N
                }
                buff.append("\" number=\"").append(item.index).append("\" weight=\"").append(item.splitWeight).append("\" />\n"); // NOI18N
            }
            buff.append("  </constraints>\n"); // NOI18N
            //END Write constraints
            //BEGIN bounds or relative bounds
            if (wmc.editorAreaBounds != null) {
                buff.append("  <bounds x=\"").append(wmc.editorAreaBounds.x).
                    append("\" y=\"").append(wmc.editorAreaBounds.y).
                    append("\" width=\"").append(wmc.editorAreaBounds.width).append("\" height=\""); // NOI18N
                buff.append(wmc.editorAreaBounds.height).append("\" />\n"); // NOI18N
            } else if (wmc.editorAreaRelativeBounds != null) {
                buff.append("  <relative-bounds x=\"").append(wmc.editorAreaRelativeBounds.x).
                    append("\" y=\"").append(wmc.editorAreaRelativeBounds.y).
                    append("\" width=\"").append(wmc.editorAreaRelativeBounds.width).
                    append("\" height=\"").append(wmc.editorAreaRelativeBounds.height).append("\"/>\n"); // NOI18N
            }
            //END
            buff.append("    </editor-area>\n"); // NOI18N
        }
        
        private void appendScreen (WindowManagerConfig wmc, StringBuffer buff) {
            buff.append("    <screen width=\"").append(wmc.screenSize.width).  // NOI18N
                append("\" height=\"").append(wmc.screenSize.height).append("\"/>\n"); // NOI18N
        }
        
        private void appendActiveMode (WindowManagerConfig wmc, StringBuffer buff) {
            if ((wmc.activeModeName != null) && !"".equals(wmc.activeModeName)) {
                buff.append("    <active-mode name=\"").append(wmc.activeModeName).append("\"/>\n"); // NOI18N
            }
        }
        
        private void appendMaximizedMode (WindowManagerConfig wmc, StringBuffer buff) {
            if ((wmc.editorMaximizedModeName != null && !"".equals(wmc.editorMaximizedModeName))
                || (wmc.viewMaximizedModeName != null && !"".equals(wmc.viewMaximizedModeName))) {
                buff.append("    <maximized-mode");
                if (wmc.editorMaximizedModeName != null && !"".equals(wmc.editorMaximizedModeName))
                    buff.append( " editor=\"").append(wmc.editorMaximizedModeName).append("\""); // NOI18N
                if (wmc.viewMaximizedModeName != null && !"".equals(wmc.viewMaximizedModeName))
                    buff.append( " view=\"").append(wmc.viewMaximizedModeName).append("\""); // NOI18N
                buff.append("/>\n"); // NOI18N
            }
        }
        
        private void appendToolbar (WindowManagerConfig wmc, StringBuffer buff) {
            buff.append("    <toolbar"); //NOI18N
            if ((wmc.toolbarConfiguration != null) && !"".equals(wmc.toolbarConfiguration)) { //NOI18N
                buff.append(" configuration=\"").append(wmc.toolbarConfiguration).append("\""); // NOI18N
            }
            buff.append(" preferred-icon-size=\"").append(wmc.preferredToolbarIconSize).append("\"/>\n"); //NOI18N
        }
        
        private void appendRecentViewList (WindowManagerConfig wmc, StringBuffer buff) {
            if (wmc.tcIdViewList.length == 0) {
                return;
            }
            buff.append("    <tc-list>\n"); // NOI18N
            for (int i = 0; i < wmc.tcIdViewList.length; i++) {
                buff.append("  <tc-id id=\"").append(wmc.tcIdViewList[i]).append("\"/>\n"); // NOI18N
            }
            buff.append("    </tc-list>\n"); // NOI18N
        }
        
    }
    
    /** Float.parseFloat() shows up as a startup hotspot (classloading?), so this uses a 
     * lookup table for common values */
    private static final float floatParse (String s) throws NumberFormatException {
        int i = Arrays.binarySearch(floatStrings, s);
        if (i >= 0) {
            return floatVals[i];
        }
        return Float.parseFloat(s);
    }
    
    private static final String[] floatStrings = new String[] {
        "0", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", // NOI18N
        "0.9","1","1.0" // NOI18N
    };
    
    private static final float[] floatVals = new float[] {
        0f, 0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f,
        1f, 1f
    };

    /**
     * Loads Mode configuration from the given file.
     * @param fo
     * @return
     * @throws IOException 
     * @since 2.70
     */
    public static ModeConfig loadModeConfigFrom( FileObject fo ) throws IOException {
        String modeName = fo.getName();
        ModeParser parser = ModeParser.parseFromFileObject(modeName, new HashSet(1));
        parser.setInLocalFolder(true);
        parser.setLocalParentFolder(fo.getParent());
        return parser.load();
    }
}
