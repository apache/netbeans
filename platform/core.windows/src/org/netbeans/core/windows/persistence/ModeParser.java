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
import org.openide.util.io.ReaderInputStream;


/**
 * Handle loading/saving of Mode configuration data.
 *
 * @author Marek Slama
 */

class ModeParser {
    
    public static final String INSTANCE_DTD_ID_1_0
        = "-//NetBeans//DTD Mode Properties 1.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_1_1
        = "-//NetBeans//DTD Mode Properties 1.1//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_1_2
        = "-//NetBeans//DTD Mode Properties 1.2//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_0
        = "-//NetBeans//DTD Mode Properties 2.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_1
        = "-//NetBeans//DTD Mode Properties 2.1//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_2
        = "-//NetBeans//DTD Mode Properties 2.2//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_3
        = "-//NetBeans//DTD Mode Properties 2.3//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_4
        = "-//NetBeans//DTD Mode Properties 2.4//EN"; // NOI18N
    
    /** Name of extended attribute for order of children */
    private static final String EA_ORDER = "WinSys-TCRef-Order"; // NOI18N
    
    private static final boolean DEBUG = Debug.isLoggable(ModeParser.class);
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
    private InternalConfig internalConfig;
    
    /** Map of TCRefParser instances. Used for fast access. */
    private Map<String, TCRefParser> tcRefParserMap = new HashMap<>(19);
    
    /** map of names of tcRefs to their index or null */
    private Map<String,Integer> tcRefOrder;
    
    /** Unique mode name from file name */
    private String modeName;
    
    /** true if wsmode file is present in module folder */
    private boolean inModuleFolder;
    /** true if wsmode file is present in local folder */
    private boolean inLocalFolder;
    
    /** Contains names of all tcRefs placed in local folder <String> */
    private Set maskSet;
    
    private final Object LOCK = new Object();
    
    private final boolean fileObjectNameMustMatchModeName;

    public static ModeParser parseFromFileObject(String name, Set maskSet) {
        return new ModeParser(name, maskSet, true);
    }
    
    public static ModeParser parseFromString(String name, Set maskSet) {
        return new ModeParser(name, maskSet, false);
    }
    
    private ModeParser (String name, Set maskSet, boolean fileObjectNameMustMatchModeName) {
        this.modeName = name;
        this.maskSet = maskSet;
        this.fileObjectNameMustMatchModeName = fileObjectNameMustMatchModeName;
    }
    
    /** Load mode configuration including all tcrefs from an XML file. */
    ModeConfig load () throws IOException {
        synchronized( LOCK ) {
            //if (DEBUG) Debug.log(ModeParser.class, "load ENTER" + " mo:" + name);
            ModeConfig mc = new ModeConfig();
            readProperties(mc);
            if (mc.kind == Constants.MODE_KIND_SLIDING && mc.side != null && !mc.permanent) {
                // now we have the 4.0 anonymous mode for the slide bar. replace with the 
                // predefined ones..
                mc.permanent = true;
                // well, the names are defined in core/ui.
                // shall we at all care about the name? or is making it permanent just fine?
    //            if (mc.side.equals(Constants.BOTTOM)) {
    //                mc.name = "bottomSlidingSide"; //NOI18N
    //            } else if (mc.side.equals(Constants.LEFT)) {
    //                mc.name = "leftSlidingSide"; //NOI18N
    //            } else if (mc.side.equals(Constants.RIGHT)) {
    //                mc.name = "rightSlidingSide"; //NOI18N
    //            }
            }
            readTCRefs(mc);
            //if (DEBUG) Debug.log(ModeParser.class, "load LEAVE" + " mo:" + name);
            return mc;
        }
    }
    
    /** Load mode configuration from an XML String. */
    ModeConfig load (String xml) throws IOException {
        synchronized( LOCK ) {
            //if (DEBUG) Debug.log(ModeParser.class, "load ENTER" + " mo:" + name);
            ModeConfig mc = new ModeConfig();
            readProperties(mc, xml);
            if (mc.kind == Constants.MODE_KIND_SLIDING && mc.side != null && !mc.permanent) {
                // now we have the 4.0 anonymous mode for the slide bar. replace with the 
                // predefined ones..
                mc.permanent = true;
                // well, the names are defined in core/ui.
                // shall we at all care about the name? or is making it permanent just fine?
    //            if (mc.side.equals(Constants.BOTTOM)) {
    //                mc.name = "bottomSlidingSide"; //NOI18N
    //            } else if (mc.side.equals(Constants.LEFT)) {
    //                mc.name = "leftSlidingSide"; //NOI18N
    //            } else if (mc.side.equals(Constants.RIGHT)) {
    //                mc.name = "rightSlidingSide"; //NOI18N
    //            }
            }
            return mc;
        }
    }
    
    /** Save mode configuration including all tcrefs. */
    void save (ModeConfig mc) throws IOException {
        synchronized( LOCK ) {
            //if (DEBUG) Debug.log(ModeParser.class, "save ENTER" + " mo:" + name);
            writeProperties(mc);
            writeTCRefs(mc);
            //if (DEBUG) Debug.log(ModeParser.class, "save LEAVE" + " mo:" + name);
        }
    }
    
    String modeConfigXml(ModeConfig mc) throws IOException {
        synchronized( LOCK ) {
            PropertyHandler propertyHandler = new PropertyHandler();
            InternalConfig internalCfg = getInternalConfig();
            StringBuffer buff = propertyHandler.generateData(mc, internalCfg);
            return buff.toString();
        }
    }
    
    private void readProperties (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "readProperties ENTER" + " mo:" + getName());
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(mc, internalCfg);
        
        /*if (DEBUG) Debug.log(ModeParser.class, "               specVersion: " + internalCfg.specVersion);
        if (DEBUG) Debug.log(ModeParser.class, "        moduleCodeNameBase: " + internalCfg.moduleCodeNameBase);
        if (DEBUG) Debug.log(ModeParser.class, "     moduleCodeNameRelease: " + internalCfg.moduleCodeNameRelease);
        if (DEBUG) Debug.log(ModeParser.class, "moduleSpecificationVersion: " + internalCfg.moduleSpecificationVersion);*/
        
        if (DEBUG) Debug.log(ModeParser.class, "readProperties LEAVE" + " mo:" + getName());
    }
    
    private void readProperties (ModeConfig mc, String xml) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "readProperties ENTER" + " mo:" + getName());
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(mc, internalCfg, xml);
        
        /*if (DEBUG) Debug.log(ModeParser.class, "               specVersion: " + internalCfg.specVersion);
        if (DEBUG) Debug.log(ModeParser.class, "        moduleCodeNameBase: " + internalCfg.moduleCodeNameBase);
        if (DEBUG) Debug.log(ModeParser.class, "     moduleCodeNameRelease: " + internalCfg.moduleCodeNameRelease);
        if (DEBUG) Debug.log(ModeParser.class, "moduleSpecificationVersion: " + internalCfg.moduleSpecificationVersion);*/
        
        if (DEBUG) Debug.log(ModeParser.class, "readProperties LEAVE" + " mo:" + getName());
    }
    
    private void readTCRefs (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "readTCRefs ENTER" + " mo:" + getName());
        
        for (Iterator<String> it = tcRefParserMap.keySet().iterator(); it.hasNext(); ) {
            TCRefParser tcRefParser = tcRefParserMap.get(it.next());
            tcRefParser.setInModuleFolder(false);
            tcRefParser.setInLocalFolder(false);
        }
        
        //if (DEBUG) Debug.log(ModeParser.class, "moduleParentFolder: " + moduleParentFolder);
        //if (DEBUG) Debug.log(ModeParser.class, " localParentFolder: " + localParentFolder);
        //if (DEBUG) Debug.log(ModeParser.class, "  moduleModeFolder: " + moduleModeFolder);
        //if (DEBUG) Debug.log(ModeParser.class, "   localModeFolder: " + localModeFolder);
        
        if (isInModuleFolder()) {
            FileObject moduleModeFolder = moduleParentFolder.getFileObject(modeName);
            if (moduleModeFolder != null) {
                FileObject [] files = moduleModeFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //if (DEBUG) Debug.log(ModeParser.class, "-- -- MODULE fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCREF_EXT.equals(files[i].getExt())) {
                        //wstcref file
                        TCRefParser tcRefParser;
                        if (tcRefParserMap.containsKey(files[i].getName())) {
                            tcRefParser = tcRefParserMap.get(files[i].getName());
                        } else {
                            tcRefParser = new TCRefParser(files[i].getName());
                            tcRefParserMap.put(files[i].getName(), tcRefParser);
                        }
                        tcRefParser.setInModuleFolder(true);
                        tcRefParser.setModuleParentFolder(moduleModeFolder);
                    }
                }
            }
        }

        if (isInLocalFolder()) {
            FileObject localModeFolder = localParentFolder.getFileObject(modeName);
            if (localModeFolder != null) {
                FileObject [] files = localModeFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //if (DEBUG) Debug.log(ModeParser.class, "-- -- LOCAL fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCREF_EXT.equals(files[i].getExt())) {
                        //wstcref file
                        TCRefParser tcRefParser = tcRefParserMap.get(files[i].getName());
                        if (tcRefParser== null) {
                            tcRefParser = new TCRefParser(files[i].getName());
                            tcRefParserMap.put(files[i].getName(), tcRefParser);
                        }
                        tcRefParser.setInLocalFolder(true);
                        tcRefParser.setLocalParentFolder(localModeFolder);
                    }
                }
            }
        }
        
        /*for (Iterator it = tcRefParserMap.keySet().iterator(); it.hasNext(); ) {
            TCRefParser tcRefParser = (TCRefParser) tcRefParserMap.get(it.next());
            if (DEBUG) Debug.log(ModeParser.class, "tcRefParser: " + tcRefParser.getName()
            + " isInModuleFolder:" + tcRefParser.isInModuleFolder()
            + " isInLocalFolder:" + tcRefParser.isInLocalFolder());
        }*/

        //Read order
        readOrder();
        
        List<TCRefParser> localList = new ArrayList<TCRefParser>(10);
        Map<String, TCRefParser> localMap = new HashMap<String, TCRefParser>( tcRefParserMap );
        
        if (tcRefOrder != null) {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- ORDER IS DEFINED");
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- map.size:" + localMap.size());
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- order.size:" + tcRefOrder.size());
            TCRefParser [] tcRefParserArray = new TCRefParser[tcRefOrder.size()];
            for (Iterator it = tcRefOrder.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry en = (Map.Entry) it.next();
                String tcRefName = (String) en.getKey();
                int index = ((Integer) en.getValue()).intValue();
                TCRefParser tcRefParser = (TCRefParser) localMap.remove(tcRefName);
                //Put instances to array according to defined order
                //Order should be defined from 0 to N-1
                //if (DEBUG) Debug.log(ModeParser.class, "-- -- ADD [" + index + "]: " + tcRefParser.getName());
                tcRefParserArray[index] = tcRefParser;
            }
            for (int i = 0; i < tcRefParserArray.length; i++) {
                if (tcRefParserArray[i] != null) {
                    localList.add(tcRefParserArray[i]);
                }
            }
            //Append remaining instances if any
            for (TCRefParser tcRefParser: localMap.values()) {
                localList.add(tcRefParser);
            }
        } else {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- NO ORDER, USING PARTIAL ORDERING");
            for (TCRefParser tcRefParser: localMap.values()) {
                localList.add(tcRefParser);
            }
            
            /*if (DEBUG) Debug.log(ModeParser.class, "LIST BEFORE SORT");
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = (TCRefParser) localList.get(i);
                if (DEBUG) Debug.log(ModeParser.class, " p[" + i + "]: " + tcRefParser.getName());
            }*/
            
            //Sort using partial ordering
            localList = carefullySort(localList);
            
            /*if (DEBUG) Debug.log(ModeParser.class, "LIST AFTER SORT");
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = (TCRefParser) localList.get(i);
                if (DEBUG) Debug.log(ModeParser.class, " p[" + i + "]: " + tcRefParser.getName());
            }*/
            
            if (tcRefOrder == null) {
                tcRefOrder = new HashMap<String,Integer>(19);
            }
            tcRefOrder.clear();
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = localList.get(i);
                tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
            }
            writeOrder();
        }
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List<TCRefConfig> tcRefCfgList = new ArrayList<TCRefConfig>(localList.size());
        List<TCRefParser> toRemove = new ArrayList<TCRefParser>(localList.size());
        for (int i = 0; i < localList.size(); i++) {
            TCRefParser tcRefParser = localList.get(i);
            //Special masking: Ignore tcRef which is present in module folder and
            //is present in local folder in DIFFERENT mode. (ie. when TopComponent defined
            //by module was moved to another module) It is to avoid creating _hidden file =>
            //trouble when disable/enable module.
            if (maskSet.contains(tcRefParser.getName())) {
                if (tcRefParser.isInModuleFolder() && !tcRefParser.isInLocalFolder()) {
                    toRemove.add(tcRefParser);
                    continue;
                }
            }
            TCRefConfig tcRefCfg;
            try {
                tcRefCfg = tcRefParser.load();
            } catch (IOException exc) {
                //If reading of one tcRef fails we want to log message
                //and continue.
                Logger.getLogger(ModeParser.class.getName()).log(Level.INFO, null, exc);
                continue;
            }
            boolean tcRefAccepted = acceptTCRef(tcRefParser, tcRefCfg);
            if (tcRefAccepted) {
                tcRefCfgList.add(tcRefCfg);
            } else {
                toRemove.add(tcRefParser);
                deleteLocalTCRef(tcRefParser.getName());
            }
        }
        
        for (int i = 0; i < toRemove.size(); i++) {
            TCRefParser tcRefParser = toRemove.get(i);
            localList.remove(tcRefParser);
            tcRefParserMap.remove(tcRefParser.getName());
        }
        
        //Update order if any tcRef was removed
        if (toRemove.size() > 0) {
            if (tcRefOrder == null) {
                tcRefOrder = new HashMap<String,Integer>(19);
            }
            tcRefOrder.clear();
            for (int i = 0; i < localList.size(); i++) {
                TCRefParser tcRefParser = (TCRefParser) localList.get(i);
                tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
            }
            writeOrder();
        }
        
        mc.tcRefConfigs = 
            tcRefCfgList.toArray(new TCRefConfig[0]);
        
        PersistenceManager pm = PersistenceManager.getDefault();
        for (int i = 0; i < mc.tcRefConfigs.length; i++) {
            pm.addUsedTCId(mc.tcRefConfigs[i].tc_id);
        }
        
        if (DEBUG) Debug.log(ModeParser.class, "readTCRefs LEAVE" + " mo:" + getName());
    }
    
    /** Checks if module for given tcRef exists.
     * @return true if tcRef is valid - its module exists
     */
    private boolean acceptTCRef (TCRefParser tcRefParser, TCRefConfig config) {
        InternalConfig cfg = tcRefParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
                                            (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
                                             cfg.moduleSpecificationVersion);
            if (curModuleInfo == null) {
                PersistenceManager.LOG.fine("Cannot find module \'" +
                          cfg.moduleCodeNameBase + " " + cfg.moduleCodeNameRelease + " " + 
                          cfg.moduleSpecificationVersion + "\' for tcref with id \'" + config.tc_id + "\'"); // NOI18N
                
            }
            return (curModuleInfo != null) && curModuleInfo.isEnabled();
        } else {
            //No module info
            return true;
        }
    }
    
    private void writeProperties (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "writeProperties ENTER" + " mo:" + getName());
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(mc, internalCfg);
        if (DEBUG) Debug.log(ModeParser.class, "writeProperties LEAVE" + " mo:" + getName());
    }
    
    private void writeTCRefs (ModeConfig mc) throws IOException {
        if (DEBUG) Debug.log(ModeParser.class, "writeTCRefs ENTER" + " mo:" + getName());
        //Step 0: Create order
        if (mc.tcRefConfigs.length > 0) {
            if (tcRefOrder == null) {
                tcRefOrder = new HashMap<String,Integer>(19);
            }
            tcRefOrder.clear();
            for (int i = 0; i < mc.tcRefConfigs.length; i++) {
                tcRefOrder.put(mc.tcRefConfigs[i].tc_id, Integer.valueOf(i));
            }
        } else {
            tcRefOrder = null;
        }
        writeOrder();
        //Step 1: Clean obsolete tcRef parsers
        Map<String, TCRefConfig> tcRefConfigMap = new HashMap<String, TCRefConfig>(19);
        for (int i = 0; i < mc.tcRefConfigs.length; i++) {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- tcRefCfg[" + i + "]: " + mc.tcRefConfigs[i].tc_id);
            tcRefConfigMap.put(mc.tcRefConfigs[i].tc_id, mc.tcRefConfigs[i]);
        }

        List<String> toDelete = new ArrayList<String>(10);
        for (TCRefParser tcRefParser : tcRefParserMap.values()) {
            if (!tcRefConfigMap.containsKey(tcRefParser.getName())) {
                toDelete.add(tcRefParser.getName());
            }
        }

        for (int i = 0; i < toDelete.size(); i++) {
            //if (DEBUG) Debug.log(ModeParser.class, " ** REMOVE FROM MAP tcRefParser: " + toDelete.get(i));
            tcRefParserMap.remove(toDelete.get(i));
            //if (DEBUG) Debug.log(ModeParser.class, " ** DELETE tcRefParser: " + toDelete.get(i));
            deleteLocalTCRef(toDelete.get(i));
        }
        
        //Step 2: Create missing tcRefs parsers
        //if (DEBUG) Debug.log(ModeParser.class, "-- -- mc.tcRefConfigs.length:" + mc.tcRefConfigs.length);
        for (int i = 0; i < mc.tcRefConfigs.length; i++) {
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- tcRefCfg[" + i + "]: " + mc.tcRefConfigs[i].tc_id);
            if (!tcRefParserMap.containsKey(mc.tcRefConfigs[i].tc_id)) {
                TCRefParser tcRefParser = new TCRefParser(mc.tcRefConfigs[i].tc_id);
                //if (DEBUG) Debug.log(ModeParser.class, " ** CREATE tcRefParser:" + tcRefParser.getName());
                tcRefParserMap.put(mc.tcRefConfigs[i].tc_id, tcRefParser);
            }
        }
        
        //Step 3: Save all tcRefs
        FileObject localFolder = localParentFolder.getFileObject(getName());
        if ((localFolder == null) && (tcRefParserMap.size() > 0)) {
            //Create local mode folder
            //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.writeTCRefs" + " CREATE LOCAL FOLDER");
            localFolder = FileUtil.createFolder(localParentFolder, getName());
        }
        //if (DEBUG) Debug.log(ModeParser.class, "writeTCRefs" + " localFolder:" + localFolder);
        
        for (Iterator<String> it = tcRefParserMap.keySet().iterator(); it.hasNext(); ) {
            TCRefParser tcRefParser = tcRefParserMap.get(it.next());
            tcRefParser.setLocalParentFolder(localFolder);
            tcRefParser.setInLocalFolder(true);
            tcRefParser.save((TCRefConfig) tcRefConfigMap.get(tcRefParser.getName()));
        }
        
        if (DEBUG) Debug.log(ModeParser.class, "writeTCRefs LEAVE" + " mo:" + getName());
    }
    
    private void deleteLocalTCRef (String tcRefName) {
        if (DEBUG) Debug.log(ModeParser.class, "deleteLocalTCRef" + " tcRefName:" + tcRefName);
        if (localParentFolder == null) {
            return;
        }
        FileObject localModeFolder = localParentFolder.getFileObject(modeName);
        if (localModeFolder == null) {
            return;
        }
        FileObject tcRefFO = localModeFolder.getFileObject(tcRefName, PersistenceManager.TCREF_EXT);
        if (tcRefFO != null) {
            PersistenceManager.deleteOneFO(tcRefFO);
        }
    }
    
    //////////////////////////////////////////////////////////////////
    // BEGIN Code to keep order of TopComponents in Mode.
    //
    // It is taken from FolderOrder and FolderList where it is used
    // to keep order of DataObjects.
    //////////////////////////////////////////////////////////////////
    
    /** Reads the order of tcRefs from disk.
     */
    private void readOrder () {
        if (localParentFolder == null) {
            try {
                localParentFolder = PersistenceManager.getDefault().getModesLocalFolder();
            }
            catch (IOException ex) {
                Logger.getLogger(ModeParser.class.getName()).log(
                        Level.INFO, "Cannot get access to lcoal modes folder", ex); // NOI18N
                return;
            }
        }
        FileObject localModeFolder = localParentFolder.getFileObject(modeName);
        if (localModeFolder == null) {
            tcRefOrder = null;
            return;
        }
        Object o = localModeFolder.getAttribute(EA_ORDER);
        
        if (o == null) {
            tcRefOrder = null;
            return;
        } else if (o instanceof String) {
            String sepNames = (String) o;
            Map<String,Integer> map = new HashMap<String,Integer>(19);
            StringTokenizer tok = new StringTokenizer(sepNames, "/"); // NOI18N
            int i = 0;
            while (tok.hasMoreTokens()) {
                String tcRefName = tok.nextToken();
                map.put(tcRefName, Integer.valueOf(i));
                i++;
            }
            tcRefOrder = map;
            return;
        } else {
            // Unknown format:
            tcRefOrder = null;
            return;
        }
    }
    
    /** Stores the order of tcRefs to disk.
    */
    private void writeOrder () throws IOException {
        //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.writeOrder ENTER" + " mo:" + getName());
        if (localParentFolder == null) {
            localParentFolder = PersistenceManager.getDefault().getModesLocalFolder();
        }
        
        FileObject localModeFolder = localParentFolder.getFileObject(modeName);
        if (localModeFolder == null) {
            //Create local mode folder
            localModeFolder = FileUtil.createFolder(localParentFolder, modeName);
        }
        if (tcRefOrder == null) {
            //Clear the order
            localModeFolder.setAttribute(EA_ORDER, null);
        } else {
            // Stores list of file names separated by /
            String[] tcRefNames = new String[tcRefOrder.size()];
            for (Map.Entry<String, Integer> en: tcRefOrder.entrySet()) {
                String tcRefName = en.getKey();
                int index = en.getValue().intValue();
                tcRefNames[index] = tcRefName;
            }
            StringBuilder buf = new StringBuilder(255);
            for (int i = 0; i < tcRefNames.length; i++) {
                if (i > 0) {
                    buf.append('/');
                }
                buf.append(tcRefNames[i]);
            }
            //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.writeOrder buf:" + buf);
            localModeFolder.setAttribute(EA_ORDER, buf.toString ());
        }
    }
    
    /** Sort a list of TCRefParsers carefully.
     * If the partial ordering is self-contradictory,
     * it will be ignored and a warning issued.
     * @param l the list to sort
     * @return the sorted list (may or may not be the same)
     */
    private List<TCRefParser> carefullySort (List<TCRefParser> l) {
        if (tcRefOrder != null && !tcRefOrder.isEmpty()) {
            // XXX could perhaps be more precise, but this is likely close enough
            return l;
        }
        if (moduleParentFolder == null) {
            return l;
        }
        FileObject moduleModeFolder = moduleParentFolder.getFileObject(modeName);
        if (moduleModeFolder == null) {
            //if (DEBUG) Debug.log(ModeParser.class, "++ ModeParser.readPartials LEAVE 1");
            return l;
        }
        Map<FileObject,TCRefParser> m = new LinkedHashMap<FileObject,TCRefParser>();
        for (TCRefParser p : l) {
            FileObject f = moduleModeFolder.getFileObject(p.getName() + '.' + PersistenceManager.TCREF_EXT);
            if (f == null) {
                // ???
                return l;
            }
            m.put(f, p);
        }
        List<FileObject> files = FileUtil.getOrder(m.keySet(), true);
        List<TCRefParser> tcs = new ArrayList<TCRefParser>(m.size());
        for (FileObject f : files) {
            tcs.add(m.get(f));
        }
        return tcs;
    }
    //////////////////////////////////////////////////////////////////
    // END Code to keep order of TopComponents in Mode.
    //////////////////////////////////////////////////////////////////
    
    /** Removes TCRefParser from ModeParser and cleans wstcref file from local folder.
     * @param tcRefName unique name of tcRef
     */
    void removeTCRef (String tcRefName) {
        if (DEBUG) Debug.log(ModeParser.class, "removeTCRef ENTER" + " tcRef:" + tcRefName);
        //Update order
        List<TCRefParser> localList = new ArrayList<TCRefParser>(10);
        Map localMap = (Map) ((HashMap) tcRefParserMap).clone();
        
        tcRefParserMap.remove(tcRefName);
        
        TCRefParser [] tcRefParserArray = new TCRefParser[tcRefOrder.size()];
        for (Iterator it = tcRefOrder.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry en = (Map.Entry) it.next();
            String name = (String) en.getKey();
            int index = ((Integer) en.getValue()).intValue();
            TCRefParser tcRefParser = (TCRefParser) localMap.remove(name);
            //Put instances to array according to defined order
            //Order should be defined from 0 to N-1
            //if (DEBUG) Debug.log(ModeParser.class, "-- -- ADD [" + index + "]: " + tcRefParser.getName());
            tcRefParserArray[index] = tcRefParser;
        }
        for (int i = 0; i < tcRefParserArray.length; i++) {
            localList.add(tcRefParserArray[i]);
        }
        //Append remaining instances if any
        for (Iterator it = localMap.keySet().iterator(); it.hasNext(); ) {
            TCRefParser tcRefParser = (TCRefParser) localMap.get(it.next());
            localList.add(tcRefParser);
        }

        //Remove tcRef
        for (int i = 0; i < localList.size(); i++) {
            TCRefParser tcRefParser = localList.get(i);
            if (tcRefName.equals(tcRefParser.getName())) {
                localList.remove(i);
                break;
            }
        }

        //Create updated order
        if( null == tcRefOrder )
            tcRefOrder = new HashMap<String,Integer>(19);
        tcRefOrder.clear();
        for (int i = 0; i < localList.size(); i++) {
            TCRefParser tcRefParser = localList.get(i);
            tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
        }
        try {
            writeOrder();
        } catch (IOException exc) {
            PersistenceManager.LOG.log(Level.INFO,
            "[WinSys.ModeParser.removeTCRef]" // NOI18N
            + " Warning: Cannot write order of mode: " + getName(), exc); // NOI18N
        }
        
        deleteLocalTCRef(tcRefName);
        if (DEBUG) Debug.log(ModeParser.class, "removeTCRef LEAVE" + " tcRef:" + tcRefName);
    }
    
    /** Adds TCRefParser to ModeParser.
     * @param tcRefName unique name of tcRef
     */
    TCRefConfig addTCRef (String tcRefName, List<String> tcRefNameList) {
        synchronized( LOCK ) {
            if (DEBUG) Debug.log(ModeParser.class, "addTCRef ENTER" + " mo:" + getName()
            + " tcRef:" + tcRefName);
            //Check consistency. TCRefParser instance should not exist.
            TCRefParser tcRefParser = tcRefParserMap.get(tcRefName);
            if (tcRefParser != null) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.addTCRef]" // NOI18N
                + " Warning: ModeParser " + getName() + ". TCRefParser " // NOI18N
                + tcRefName + " exists but it should not."); // NOI18N
                tcRefParserMap.remove(tcRefName);
            }
            tcRefParser = new TCRefParser(tcRefName);
            FileObject moduleFolder = moduleParentFolder.getFileObject(modeName);
            tcRefParser.setModuleParentFolder(moduleFolder);
            tcRefParser.setInModuleFolder(true);
            tcRefParserMap.put(tcRefName, tcRefParser);
            TCRefConfig tcRefConfig = null;
            try {
                tcRefConfig = tcRefParser.load();
            } catch (IOException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.addTCRef]" // NOI18N
                + " Warning: ModeParser " + getName() + ". Cannot load tcRef " +  tcRefName, exc); // NOI18N
            }

            // Update order
            List<TCRefParser> localList = new ArrayList<TCRefParser>(10);
            Map<String, TCRefParser> localMap = new HashMap<>(tcRefParserMap);

            if( null == tcRefOrder ) {
                //#232307
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.addTCRef]" // NOI18N
                + " Warning: ModeParser " + getName() + ". TCRefParser " // NOI18N
                + tcRefName + " is missing TC order."); // NOI18N
                tcRefParserMap.remove(tcRefName);
                readOrder();
            }
            TCRefParser [] tcRefParserArray = new TCRefParser[tcRefOrder.size()];
            for (Iterator it = tcRefOrder.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry en = (Map.Entry) it.next();
                String name = (String) en.getKey();
                int index = ((Integer) en.getValue()).intValue();
                tcRefParser = (TCRefParser) localMap.remove(name);
                //Put instances to array according to defined order
                //Order should be defined from 0 to N-1
                //log("-- -- ADD [" + index + "]: " + tcRefParser.getName());
                tcRefParserArray[index] = tcRefParser;
            }
            for (int i = 0; i < tcRefParserArray.length; i++) {
                if(  null != tcRefParserArray[i] ) {
                    //#233078 - when enabling modules that add more than one TC
                    //the file system sends one notification per file however the order
                    //attribute in Modes folder contains all new files already so
                    //the parser is missing for files that were notified yet
                    localList.add(tcRefParserArray[i]);
                }
            }
            //Append remaining instances if any
            for (Iterator<String> it = localMap.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                tcRefParser = localMap.get(key);
                assert tcRefParser != null : "No parser for " + key;
                localList.add(tcRefParser);
            }

            /*if (DEBUG) Debug.log(ModeParser.class, "LIST BEFORE SORT");
            for (int i = 0; i < localList.size(); i++) {
                tcRefParser = (TCRefParser) localList.get(i);
                if (DEBUG) Debug.log(ModeParser.class, "p[" + i + "]: " + tcRefParser.getName());
            }*/

            localList = carefullySort(localList);

            /*if (DEBUG) Debug.log(ModeParser.class, "LIST AFTER SORT");
            for (int i = 0; i < localList.size(); i++) {
                tcRefParser = (TCRefParser) localList.get(i);
                if (DEBUG) Debug.log(ModeParser.class, "p[" + i + "]: " + tcRefParser.getName());
            }*/

            //Create updated order
            if( null == tcRefOrder )
                tcRefOrder = new HashMap<String,Integer>(19);
            tcRefOrder.clear();
            for (int i = 0; i < localList.size(); i++) {
                tcRefParser = (TCRefParser) localList.get(i);
                tcRefOrder.put(tcRefParser.getName(), Integer.valueOf(i));
            }
            try {
                writeOrder();
            } catch (IOException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.addTCRef]" // NOI18N
                + " Warning: Cannot write order of mode: " + getName(), exc); // NOI18N
            }

            //Fill output order
            tcRefNameList.clear();
            for (int i = 0; i < localList.size(); i++) {
                tcRefParser = (TCRefParser) localList.get(i);
                tcRefNameList.add(tcRefParser.getName());
            }

            if (DEBUG) Debug.log(ModeParser.class, "addTCRef LEAVE" + " mo:" + getName()
            + " tcRef:" + tcRefName);

            return tcRefConfig;
        }
    }
    
    /** Adds TCRefParser to ModeParser. Called from import to pass module info
     * to new parser.
     * @param tcRefName unique name of tcRef
     */
    void addTCRefImport (String tcRefName, InternalConfig internalCfg) {
        synchronized( LOCK ) {
            if (DEBUG) Debug.log(ModeParser.class, "addTCRefImport ENTER" + " mo:" + getName()
            + " tcRef:" + tcRefName);
            //Check consistency. TCRefParser instance should not exist.
            TCRefParser tcRefParser = tcRefParserMap.get(tcRefName);
            if (tcRefParser != null) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.addTCRef]" // NOI18N
                + " Warning: ModeParser " + getName() + ". TCRefParser " // NOI18N
                + tcRefName + " exists but it should not."); // NOI18N
                tcRefParserMap.remove(tcRefName);
            }
            tcRefParser = new TCRefParser(tcRefName);
            //FileObject moduleFolder = moduleParentFolder.getFileObject(modeName);
            //tcRefParser.setModuleParentFolder(moduleFolder);
            //tcRefParser.setInModuleFolder(false);
            FileObject localFolder = localParentFolder.getFileObject(modeName);
            tcRefParser.setLocalParentFolder(localFolder);
            tcRefParser.setInternalConfig(internalCfg);

            //if (DEBUG) Debug.log(ModeParser.class, "CodeNameBase:" + internalCfg.moduleCodeNameBase);
            //if (DEBUG) Debug.log(ModeParser.class, "CodeNameRelease:" + internalCfg.moduleCodeNameRelease);
            //if (DEBUG) Debug.log(ModeParser.class, "SpecificationVersion:" + internalCfg.moduleSpecificationVersion);
            //if (DEBUG) Debug.log(ModeParser.class, "specVersion:" + internalCfg.specVersion);

            tcRefParserMap.put(tcRefName, tcRefParser);

            if (DEBUG) Debug.log(ModeParser.class, "addTCRefImport LEAVE" + " mo:" + getName()
            + " tcRef:" + tcRefName);
        }
    }
    
    /** Finds TCRefParser with given ID. Returns null if such TCRefParser
     * is not found.
     * @param tcRefName unique name of tcRef
     */
    TCRefParser findTCRefParser (String tcRefName) {
        synchronized( LOCK ) {
            //if (DEBUG) Debug.log(ModeParser.class, "findTCRefParser ENTER" + " tcRef:" + tcRefName);
            return tcRefParserMap.get(tcRefName);
        }
    }
    
    /** Getter for internal configuration data.
     * @return instance of internal configuration data
     */
    InternalConfig getInternalConfig () {
        if (internalConfig == null) {
            internalConfig = new InternalConfig();
        }
        return internalConfig;
    }
    
    void setModuleParentFolder (FileObject moduleParentFolder) {
        this.moduleParentFolder = moduleParentFolder;
    }
    
    void setLocalParentFolder (FileObject localParentFolder) {
        this.localParentFolder = localParentFolder;
    }
    
    String getName () {
        return modeName;
    }
    
    boolean isInModuleFolder () {
        return inModuleFolder;
    }
    
    void setInModuleFolder (boolean inModuleFolder) {
        this.inModuleFolder = inModuleFolder;
    }
    
    boolean isInLocalFolder () {
        return inLocalFolder;
    }
    
    void setInLocalFolder (boolean inLocalFolder) {
        this.inLocalFolder = inLocalFolder;
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** Mode configuration data */
        private ModeConfig modeConfig = null;
        
        /** Internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** List to store parsed path items */
        private List<SplitConstraint> itemList = new ArrayList<SplitConstraint>(10);
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject modeConfigFO;
            if (isInLocalFolder()) {
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOInput" + " looking for LOCAL");
                modeConfigFO = localParentFolder.getFileObject
                (ModeParser.this.getName(), PersistenceManager.MODE_EXT);
            } else if (isInModuleFolder()) {
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOInput" + " looking for MODULE");
                modeConfigFO = moduleParentFolder.getFileObject
                (ModeParser.this.getName(), PersistenceManager.MODE_EXT);
            } else {
                //XXX should not happen
                modeConfigFO = null;
            }
            //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOInput" + " modeConfigFO:" + modeConfigFO);
            return modeConfigFO;
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject modeConfigFO;
            modeConfigFO = localParentFolder.getFileObject
            (ModeParser.this.getName(), PersistenceManager.MODE_EXT);
            if (modeConfigFO != null) {
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOOutput" + " modeConfigFO LOCAL:" + modeConfigFO);
                return modeConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(ModeParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.MODE_EXT);
                //XXX should be improved localParentFolder can be null
                modeConfigFO = FileUtil.createData(localParentFolder, buffer.toString());
                //if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.getConfigFOOutput" + " LOCAL not found CREATE");

                return modeConfigFO;
            }
        }
        
        /**
         * Reads mode configuration data from XML file. Data are returned in
         * output params.
         */
        void readData(ModeConfig modeCfg, InternalConfig internalCfg)
                throws IOException {
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing Mode configuration file:" // NOI18N
                        + ModeParser.this.getName());
            }
            InputStream is = cfgFOInput.getInputStream();
            readData(modeCfg, internalCfg, is, cfgFOInput);
        }
        
        /**
         * Reads mode configuration data from XML String. Data are returned in
         * output params.
         */
        void readData(ModeConfig modeCfg, InternalConfig internalCfg, String xml)
                throws IOException {
            InputStream is = new BufferedInputStream( new ReaderInputStream( new StringReader(xml)));
            readData(modeCfg, internalCfg, is, xml);
        }
        
        /**
         * Reads mode configuration data from an InputStream. Data are returned
         * in output params.
         */
        private void readData(ModeConfig modeCfg, InternalConfig internalCfg, InputStream is, Object source)
                throws IOException {
            modeConfig = modeCfg;
            internalConfig = internalCfg;
            itemList.clear();
            
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*if ("explorer".equals(ModeParser.this.getName())) {
                        InputStream is = cfgFOInput.getInputStream();
                        byte [] arr = new byte [is.available()];
                        is.read(arr);
                        if (DEBUG) Debug.log(ModeParser.class, "DUMP Mode:");
                        String s = new String(arr);
                        if (DEBUG) Debug.log(ModeParser.class, s);
                    }*/
                    //DUMP END
                    PersistenceManager.getDefault().getXMLParser(this).parse(new InputSource(is));
                }
            } catch (SAXException exc) {
                // Turn into annotated IOException
                String msg = NbBundle.getMessage(ModeParser.class,
                                                 "EXC_ModeParse", source);

                throw (IOException) new IOException(msg).initCause(exc);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException exc) {
                    Logger.getLogger(ModeParser.class.getName()).log(Level.INFO, null, exc);
                }
            }
            
            modeConfig.constraints =
                itemList.toArray(new SplitConstraint[0]);
            
            modeCfg = modeConfig;
            internalCfg = internalConfig;
            
            modeConfig = null;
            internalConfig = null;
        }

        @Override
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) throws SAXException {
            if ("mode".equals(qname)) { // NOI18N
                handleMode(attrs);
            } else if (internalConfig.specVersion != null && 
            // check for null because of #45599 - no idea how it can happen other than having a broken file which doesn't
            // declade 'mode' element. (see handleMode() - after this is called NPE is impossible
                       internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) >= 0) { // NOI18N
                //Parse version 2.0 and beyond
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("name".equals(qname)) { // NOI18N
                    handleName(attrs);
                } else if ("kind".equals(qname)) { // NOI18N
                    handleKind(attrs);
                } else if ("slidingSide".equals(qname)) { // NOI18N
                    handleSlidingSide(attrs);
                } else if ("slide-in-size".equals(qname)) { // NOI18N
                    handleSlideInSize(attrs);
                } else if ("state".equals(qname)) { // NOI18N
                    handleState(attrs);
                } else if ("constraints".equals(qname)) { // NOI18N
                    handleConstraints(attrs);
                } else if ("path".equals(qname)) { // NOI18N
                    handlePath(attrs);
                } else if ("bounds".equals(qname)) { // NOI18N
                    handleBounds(attrs);
                } else if ("relative-bounds".equals(qname)) { // NOI18N
                    handleRelativeBounds(attrs);
                } else if ("frame".equals(qname)) { // NOI18N
                    handleFrame(attrs);
                } else if ("active-tc".equals(qname)) { // NOI18N
                    handleActiveTC(attrs);
                } else if ("empty-behavior".equals(qname)) { // NOI18N
                    handleEmptyBehavior(attrs);
                }
            } else {
                if (DEBUG) Debug.log(ModeParser.class, "-- ModeParser.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }

        @Override
        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }
        
        /** Reads element "mode" */
        private void handleMode (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleMode]" // NOI18N
                + " Warning: Missing attribute \"version\" of element \"mode\"."); // NOI18N
                internalConfig.specVersion = new SpecificationVersion("2.0"); // NOI18N
            }
        }
        
        /** Reads element "module" and updates mode config content */
        private void handleModule (Attributes attrs) {
            String moduleCodeName = attrs.getValue("name"); // NOI18N
            //Parse code name
            internalConfig.moduleCodeNameBase = null;
            internalConfig.moduleCodeNameRelease = null;
            internalConfig.moduleSpecificationVersion = null;
            if (moduleCodeName != null) {
                int i = moduleCodeName.indexOf('/');
                if (i != -1) {
                    internalConfig.moduleCodeNameBase = moduleCodeName.substring(0, i);
                    internalConfig.moduleCodeNameRelease = moduleCodeName.substring(i + 1);
                    checkReleaseCode(internalConfig);
                } else {
                    internalConfig.moduleCodeNameBase = moduleCodeName;
                }
                internalConfig.moduleSpecificationVersion = attrs.getValue("spec"); // NOI18N
            }
        }

        /** Checks validity of <code>moduleCodeNameRelease</code> field. 
         * Helper method. */
        private void checkReleaseCode (InternalConfig internalConfig) {
            // #24844. Repair the wrongly saved "null" string
            // as release number.
            if("null".equals(internalConfig.moduleCodeNameRelease)) { // NOI18N
                Logger.getLogger(ModeParser.class.getName()).log(Level.INFO, null,
                                  new IllegalStateException("Module release code was saved as null string" +
                                                            " for module " +
                                                            internalConfig.moduleCodeNameBase +
                                                            "! Repairing."));
                internalConfig.moduleCodeNameRelease = null;
            }
        }
        
        /** Reads element "name" */
        private void handleName (Attributes attrs) throws SAXException {
            String name = attrs.getValue("unique"); // NOI18N
            if (name != null) {
                modeConfig.name = name;
                if (fileObjectNameMustMatchModeName && !name.equals(ModeParser.this.getName())) {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleName]" // NOI18N
                    + " Error: Value of attribute \"unique\" of element \"name\"" // NOI18N
                    + " and configuration file name must be the same: " + name + " != " + ModeParser.this.getName() + "."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleName]" // NOI18N
                + " Error: Missing required attribute \"unique\" of element \"name\"."); // NOI18N
                throw new SAXException("Missing required attribute"); // NOI18N
            }
            String includes = attrs.getValue("includes"); // NOI18N
            if (includes != null) {
                String[] split = includes.split( "," ); //NOI18N
                Set<String> otherNames = new HashSet<String>( split.length );
                for( String s : split ) {
                    s = s.trim();
                    if( s.isEmpty() )
                        continue;
                    otherNames.add( s );
                }
                if( otherNames.isEmpty() ) {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleName]" // NOI18N
                    + " Error: Attribute \"includes\" of element \"name\"" // NOI18N
                    + " is present but does not contain any valid mode names."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
                modeConfig.otherNames = otherNames;
            }
        }

        /** Reads element "kind" */
        private void handleKind (Attributes attrs) throws SAXException {
            String type = attrs.getValue("type"); // NOI18N
            if (type != null) {
                if ("editor".equals(type)) {
                    modeConfig.kind = Constants.MODE_KIND_EDITOR;
                } else if ("view".equals(type)) {
                    modeConfig.kind = Constants.MODE_KIND_VIEW;
                } else if ("sliding".equals(type)) {
                    modeConfig.kind = Constants.MODE_KIND_SLIDING;
                    if( null != modeConfig.otherNames && !modeConfig.otherNames.isEmpty() ) {
                        PersistenceManager.LOG.log(Level.INFO,
                        "[WinSys.ModeParser.handleName]" // NOI18N
                        + " Error: Sliding modes are not allowed to have additional names: " + modeConfig.otherNames + "."); // NOI18N
                        throw new SAXException("Invalid attribute value"); // NOI18N
                    }
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleKind]" // NOI18N
                    + " Warning: Invalid value of attribute \"type\": " + type + "."); // NOI18N
                    modeConfig.kind = Constants.MODE_KIND_VIEW;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleKind]" // NOI18N
                + " Error: Missing required attribute \"type\" of element \"kind\"."); // NOI18N
                modeConfig.kind = Constants.MODE_KIND_VIEW;
            }
        }
        
        /** Reads element "kind" */
        private void handleSlidingSide(Attributes attrs) {
            String side = attrs.getValue("side");
            if (side != null) {
                if (Constants.LEFT.equals(side) ||
                    Constants.RIGHT.equals(side) ||
                    Constants.TOP.equals(side) ||
                    Constants.BOTTOM.equals(side)) 
                {
                    modeConfig.side = side;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleSlidingSide]" // NOI18N
                    + " Warning: Wrong value \"" + side + "\" of attribute \"side\" for sliding mode"); // NOI18N
                    modeConfig.side = Constants.LEFT;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleSlidingSide]" // NOI18N
                + " Warning: Missing value of attribute \"side\" for sliding mode."); // NOI18N
                modeConfig.side = Constants.LEFT;
            }
        }      
        
        /** Reads element "slideInSize" */
        private void handleSlideInSize(Attributes attrs) {
            String tcId = attrs.getValue("tc-id");
            String size = attrs.getValue("size");
            if (tcId != null && size != null) {
                try {
                    Integer intSize = Integer.valueOf( size );
                    if( null == modeConfig.slideInSizes )
                        modeConfig.slideInSizes = new HashMap<String, Integer>(5);
                    modeConfig.slideInSizes.put( tcId, intSize );
                    return;
                } catch( NumberFormatException nfE ) {
                    //fall through
                }
            } 
            PersistenceManager.LOG.log(Level.INFO,
            "[WinSys.ModeParser.handleSlideInSize]" // NOI18N
            + " Warning: Invalid attributes for preferred slide-in size: tc-id=" + tcId + ", size=" + size + "."); // NOI18N
        }      
        
        private void handleState(Attributes attrs) throws SAXException {
            String type = attrs.getValue("type"); // NOI18N
            if (type != null) {
                if ("joined".equals(type)) {
                    modeConfig.state = Constants.MODE_STATE_JOINED;
                } else if ("separated".equals(type)) {
                    modeConfig.state = Constants.MODE_STATE_SEPARATED;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleState]" // NOI18N
                    + " Warning: Invalid value " + type + " of attribute \"type\"" // NOI18N
                    + " of element \"state\"."); // NOI18N
                    modeConfig.state = Constants.MODE_STATE_JOINED;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleState]" // NOI18N
                + " Error: Missing required attribute \"type\""
                + " of element \"state\"."); // NOI18N
                modeConfig.state = Constants.MODE_STATE_JOINED;
            }
            String minimized = attrs.getValue("minimized"); // NOI18N
            if (minimized != null) {
                if ("true".equals(minimized)) {
                    if( modeConfig.kind == Constants.MODE_KIND_SLIDING ) {
                        PersistenceManager.LOG.log(Level.INFO,
                        "[WinSys.ModeParser.handleState]" // NOI18N
                        + " Error: Sliding mode cannot be minimized."); // NOI18N
                        throw new SAXException("Invalid attribute value"); // NOI18N
                    }
                    modeConfig.minimized = true;
                } else if ("false".equals(minimized)) {
                    modeConfig.minimized = false;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleState]" // NOI18N
                    + " Warning: Invalid value " + minimized + " of attribute \"minimized\"" // NOI18N
                    + " of element \"state\"."); // NOI18N
                    modeConfig.minimized = false;
                }
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
                "[WinSys.ModeParser.handlePath]" // NOI18N
                + " Warning: Invalid or missing value " + s + " of attribute \"orientation\"."); // NOI18N
                orientation = Constants.VERTICAL;
            }
            
            int number;
            try {
                s = attrs.getValue("number"); // NOI18N
                if (s != null) {
                    number = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handlePath]" // NOI18N
                    + " Warning: Missing value of attribute \"number\"."); // NOI18N
                    number = 0;
                }
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handlePath]" // NOI18N
                + " Warning: Cannot read element \"path\", attribute \"number\"", exc); // NOI18N
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
                "[WinSys.ModeParser.handlePath]" // NOI18N
                + " Warning: Cannot read element \"path\", attribute \"weight\".", exc); // NOI18N
                weight = 0.5;
            }
            SplitConstraint item = new SplitConstraint(orientation, number, weight);
            itemList.add(item);
        }
        
        /** Reads element "bounds" */
        private void handleBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                modeConfig.bounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                        "[WinSys.ModeParser.handleBounds]" // NOI18N
                        + " Warning: Missing attribute \"x\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"bounds\"."); // NOI18N
                    return;
                }
                modeConfig.bounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleBounds]" // NOI18N
                + " Warning: Cannot read element \"bounds\".", exc); // NOI18N
            }
        }
        
        /** Reads element "relative-bounds" */
        private void handleRelativeBounds (Attributes attrs) {
            try {
                String s;
                int x, y, width, height;
                
                modeConfig.relativeBounds = null;
                s = attrs.getValue("x"); // NOI18N
                if (s != null) {
                    x = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"x\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("y"); // NOI18N
                if (s != null) {
                    y = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"y\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("width"); // NOI18N
                if (s != null) {
                    width = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"width\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                s = attrs.getValue("height"); // NOI18N
                if (s != null) {
                    height = Integer.parseInt(s);
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                    + " Warning: Missing attribute \"height\" of element \"relative-bounds\"."); // NOI18N
                    return;
                }
                modeConfig.relativeBounds = new Rectangle(x, y, width, height);
            } catch (NumberFormatException exc) {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleRelativeBounds]" // NOI18N
                + " Warning: Cannot read element \"relative-bounds\".", exc); // NOI18N
            }
        }
        
        /** Reads element "frame" */
        private void handleFrame (Attributes attrs) {
            String frameState = attrs.getValue("state"); // NOI18N
            if (frameState != null) {
                try {
                    modeConfig.frameState = Integer.parseInt(frameState);
                } catch (NumberFormatException exc) {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.ModeParser.handleFrame]" // NOI18N
                    + " Warning: Cannot read value " + frameState + " for attribute \"state\"" // NOI18N
                    + " of element \"frame\".", exc); // NOI18N
                    modeConfig.frameState = Frame.NORMAL;
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleFrame]" // NOI18N
                + " Warning: Missing value of attribute \"state\"" // NOI18N
                + " of element \"frame\"."); // NOI18N
                modeConfig.frameState = Frame.NORMAL;
            }
        }
        
        /** Reads element "active-tc" */
        private void handleActiveTC (Attributes attrs) {
            String id = attrs.getValue("id"); // NOI18N
            if (id != null) {
                modeConfig.selectedTopComponentID = id;
            } else {
                modeConfig.selectedTopComponentID = ""; // NOI18N
            }
            String prevId = attrs.getValue("prev-id"); // NOI18N
            if (prevId != null) {
                modeConfig.previousSelectedTopComponentID = prevId;
            } else {
                modeConfig.previousSelectedTopComponentID = ""; // NOI18N
            }
        }
        
        /** Reads element "empty-behavior" */
        private void handleEmptyBehavior (Attributes attrs) {
            String value = attrs.getValue("permanent"); // NOI18N
            if ("true".equals(value)) { // NOI18N
                modeConfig.permanent = true;
            } else if ("false".equals(value)) { // NOI18N
                modeConfig.permanent = false;
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.ModeParser.handleEmptyBehavior]" // NOI18N
                + " Warning: Invalid value " + value + " of attribute \"permanent\"."); // NOI18N
                modeConfig.permanent = false;
            }
        }
        
        StringBuffer generateData(ModeConfig mc, InternalConfig ic) throws IOException {
            return fillBuffer(mc, ic);
        }
        
        /** Writes data from asociated mode to the xml representation */
        void writeData (ModeConfig mc, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(mc, ic);
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
                    /*log("-- DUMP Mode:");
                    log(buff.toString());*/
                } finally {
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                    } catch (IOException exc) {
                        Logger.getLogger(ModeParser.class.getName()).log(Level.INFO, null, exc);
                    }
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (ModeConfig mc, InternalConfig ic) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"). // NOI18N
            /*buff.append("<!DOCTYPE mode PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Mode Properties 2.4//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/mode-properties2_4.dtd\">\n\n"); // NOI18N*/
                append("<mode version=\"2.4\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendName(mc, buff);
            appendKind(mc, buff);
            if (mc.kind == Constants.MODE_KIND_SLIDING) {
                appendSlidingSide(mc, buff);
                if( null != mc.slideInSizes )
                    appendSlideInSize(mc, buff);
            }
            appendState(mc, buff);
            appendConstraints(mc, buff);
            if (mc.bounds != null) {
                appendBounds(mc, buff);
            } else if (mc.relativeBounds != null) {
                appendRelativeBounds(mc, buff);
            }
            appendFrame(mc, buff);
            appendActiveTC(mc, buff);
            appendEmptyBehavior(mc, buff);
            
            buff.append("</mode>\n"); // NOI18N
            return buff;
        }
        
        private void appendModule (InternalConfig ic, StringBuffer buff) {
            if (ic == null) {
                return;
            }
            if (ic.moduleCodeNameBase != null) {
                buff.append("    <module name=\""); // NOI18N
                buff.append(ic.moduleCodeNameBase);
                if (ic.moduleCodeNameRelease != null) {
                    buff.append("/").append(ic.moduleCodeNameRelease); // NOI18N
                }
                if (ic.moduleSpecificationVersion != null) { 
                    buff.append("\" spec=\""); // NOI18N
                    buff.append(ic.moduleSpecificationVersion);
                }
                buff.append("\" />\n"); // NOI18N
            }
        }

        private void appendName (ModeConfig mc, StringBuffer buff) {
            buff.append("    <name unique=\""); // NOI18N
            buff.append(mc.name);
            buff.append("\" ");
            if( null != mc.otherNames && !mc.otherNames.isEmpty() ) {
                buff.append( " includes=\"" );
                boolean comma = false;
                for( String s : mc.otherNames ) {
                    if( comma )
                        buff.append( ',' );
                    buff.append( s );
                    comma = true;
                }
                buff.append("\" ");
            }
            buff.append( " />\n"); // NOI18N
        }

        private void appendKind (ModeConfig mc, StringBuffer buff) {
            buff.append("  <kind type=\""); // NOI18N
            if (mc.kind == Constants.MODE_KIND_EDITOR) {
                buff.append("editor"); // NOI18N
            } else if (mc.kind == Constants.MODE_KIND_VIEW) {
                buff.append("view"); // NOI18N
            } else if (mc.kind == Constants.MODE_KIND_SLIDING) {
                buff.append("sliding"); // NOI18N
            }
            buff.append("\" />\n"); // NOI18N
        }
        
        private void appendSlidingSide(ModeConfig mc, StringBuffer buff) {
            buff.append("  <slidingSide side=\"");
            buff.append(mc.side);
            buff.append("\" ");
            buff.append("/>\n"); // NOI18N
        }
        
        private void appendSlideInSize(ModeConfig mc, StringBuffer buff) {
            if( null != mc.slideInSizes ) {
                for( Iterator<String> i=mc.slideInSizes.keySet().iterator(); i.hasNext(); ) {
                    String tcId = i.next();
                    Integer size = mc.slideInSizes.get(tcId);
                    
                    buff.append("  <slide-in-size tc-id=\"");
                    buff.append(tcId);
                    buff.append("\" size=\"");
                    buff.append(size.intValue());
                    buff.append("\" />\n"); // NOI18N
                }
            }
        }

        private void appendState (ModeConfig mc, StringBuffer buff) {
            buff.append("  <state type=\""); // NOI18N
            if (mc.state == Constants.MODE_STATE_JOINED) {
                buff.append("joined"); // NOI18N
            } else if (mc.state == Constants.MODE_STATE_SEPARATED) {
                buff.append("separated"); // NOI18N
            }
            buff.append("\" ");
            if( mc.minimized ) {
                buff.append( " minimized=\"true\" " );// NOI18N
            }
            buff.append(" />\n"); // NOI18N
        }
        
        private void appendConstraints (ModeConfig mc, StringBuffer buff) {
            if (mc.constraints.length == 0) {
                return;
            }
            buff.append("  <constraints>\n"); // NOI18N
            for (int i = 0; i < mc.constraints.length; i++) {
                SplitConstraint item = mc.constraints[i];
                buff.append("    <path orientation=\""); // NOI18N
                if (item.orientation == Constants.HORIZONTAL) {
                    buff.append("horizontal"); // NOI18N
                } else {
                    buff.append("vertical"); // NOI18N
                }
                buff.append("\" number=\"").append(item.index).append("\" weight=\"").append(item.splitWeight).append("\"/>\n"); // NOI18N
            }
            buff.append("  </constraints>\n"); // NOI18N
        }
        
        private void appendBounds (ModeConfig mc, StringBuffer buff) {
            if (mc.bounds == null) {
                return;
            }
            buff.append("  <bounds x=\"").append(mc.bounds.x).
                append("\" y=\"").append(mc.bounds.y).
                append("\" width=\"").append(mc.bounds.width).
                append("\" height=\"").append(mc.bounds.height).append("\" />\n"); // NOI18N
        }
        
        private void appendRelativeBounds (ModeConfig mc, StringBuffer buff) {
            if (mc.relativeBounds == null) {
                return;
            }
            buff.append("  <relative-bounds x=\"").append(mc.relativeBounds.x).
                append("\" y=\"").append(mc.relativeBounds.y).
                append("\" width=\"").append(mc.relativeBounds.width).
                append("\" height=\"").append(mc.relativeBounds.height).append("\" />\n"); // NOI18N
        }
        
        private void appendFrame (ModeConfig mc, StringBuffer buff) {
            buff.append("  <frame state=\"").append(mc.frameState).append("\"/>\n"); // NOI18N
        }
        
        private void appendActiveTC (ModeConfig mc, StringBuffer buff) {
            if ((mc.selectedTopComponentID != null && !"".equals(mc.selectedTopComponentID)) 
                || (mc.previousSelectedTopComponentID != null && !"".equals(mc.previousSelectedTopComponentID)) ) {
                buff.append("    <active-tc ");
                
                if (mc.selectedTopComponentID != null && !"".equals(mc.selectedTopComponentID)) {
                    String tcName = PersistenceManager.escapeTcId4XmlContent(mc.selectedTopComponentID);
                    buff.append( " id=\"").append(tcName).append("\" "); // NOI18N
                }
                
                if (mc.previousSelectedTopComponentID != null && !"".equals(mc.previousSelectedTopComponentID)) {
                    String tcName = PersistenceManager.escapeTcId4XmlContent(mc.previousSelectedTopComponentID);
                    buff.append( " prev-id=\"").append(tcName).append("\" "); // NOI18N
                }
                buff.append("/>\n"); // NOI18N
            }
        }
        
        private void appendEmptyBehavior (ModeConfig mc, StringBuffer buff) {
            buff.append("    <empty-behavior permanent=\"").append(mc.permanent).append("\"/>\n"); // NOI18N
        }
    }
    
}
