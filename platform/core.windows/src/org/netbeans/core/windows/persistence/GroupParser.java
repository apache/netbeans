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
import org.netbeans.core.windows.Debug;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Handle loading/saving of Group configuration data.
 *
 * @author Marek Slama
 */

class GroupParser {
    
    public static final String INSTANCE_DTD_ID_2_0
        = "-//NetBeans//DTD Group Properties 2.0//EN"; // NOI18N
    
    private static final boolean DEBUG = Debug.isLoggable(GroupParser.class);
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
    private InternalConfig internalConfig;
    
    private Map<String, TCGroupParser> tcGroupParserMap = new HashMap<String, TCGroupParser>(19);
    
    /** Unique group name from file name */
    private String groupName;
    
    /** true if wsgrp file is present in module folder */
    private boolean inModuleFolder;
    /** true if wsgrp file is present in local folder */
    private boolean inLocalFolder;
    
    public GroupParser(String name) {
        this.groupName = name;
    }
    
    /** Load group configuration including all tcgrp's. */
    GroupConfig load () throws IOException {
        //if (DEBUG) Debug.log(GroupParser.class, "");
        //if (DEBUG) Debug.log(GroupParser.class, "++ GroupParser.load ENTER" + " group:" + name);
        GroupConfig sc = new GroupConfig();
        readProperties(sc);
        readTCGroups(sc);
        //if (DEBUG) Debug.log(GroupParser.class, "++ GroupParser.load LEAVE" + " group:" + name);
        //if (DEBUG) Debug.log(GroupParser.class, "");
        return sc;
    }
    
    /** Save group configuration including all tcgrp's. */
    void save (GroupConfig sc) throws IOException {
        //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.save ENTER" + " group:" + name);
        writeProperties(sc);
        writeTCGroups(sc);
        //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.save LEAVE" + " group:" + name);
    }
    
    private void readProperties (GroupConfig sc) throws IOException {
        if (DEBUG) Debug.log(GroupParser.class, "readProperties ENTER" + " group:" + getName());
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(sc, internalCfg);
        
        /*if (DEBUG) Debug.log(GroupParser.class, "               specVersion: " + internalCfg.specVersion);
        if (DEBUG) Debug.log(GroupParser.class, "        moduleCodeNameBase: " + internalCfg.moduleCodeNameBase);
        if (DEBUG) Debug.log(GroupParser.class, "     moduleCodeNameRelease: " + internalCfg.moduleCodeNameRelease);
        if (DEBUG) Debug.log(GroupParser.class, "moduleSpecificationVersion: " + internalCfg.moduleSpecificationVersion);*/
        if (DEBUG) Debug.log(GroupParser.class, "readProperties LEAVE" + " group:" + getName());
    }
    
    private void readTCGroups (GroupConfig sc) throws IOException {
        if (DEBUG) Debug.log(GroupParser.class, "readTCGroups ENTER" + " group:" + getName());
        
        for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            tcGroupParser.setInModuleFolder(false);
            tcGroupParser.setInLocalFolder(false);
        }
        
        /*if (DEBUG) Debug.log(GroupParser.class, "moduleParentFolder: " + moduleParentFolder);
        if (DEBUG) Debug.log(GroupParser.class, " localParentFolder: " + localParentFolder);
        if (DEBUG) Debug.log(GroupParser.class, "   moduleGroupFolder: " + moduleGroupFolder);
        if (DEBUG) Debug.log(GroupParser.class, "    localGroupFolder: " + localGroupFolder);*/
        
        if (isInModuleFolder()) {
            FileObject moduleGroupFolder = moduleParentFolder.getFileObject(groupName);
            if (moduleGroupFolder != null) {
                FileObject [] files = moduleGroupFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //if (DEBUG) Debug.log(GroupParser.class, "-- MODULE fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCGROUP_EXT.equals(files[i].getExt())) {
                        //wstcgrp file
                        TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(files[i].getName());
                        if (tcGroupParser == null) {
                            tcGroupParser = new TCGroupParser(files[i].getName());
                            tcGroupParserMap.put(files[i].getName(), tcGroupParser);
                        }
                        tcGroupParser.setInModuleFolder(true);
                        tcGroupParser.setModuleParentFolder(moduleGroupFolder);
                    }
                }
            }
        }

        if (isInLocalFolder()) {
            FileObject localGroupFolder = localParentFolder.getFileObject(groupName);
            if (localGroupFolder != null) {
                FileObject [] files = localGroupFolder.getChildren();
                for (int i = 0; i < files.length; i++) {
                    //if (DEBUG) Debug.log(GroupParser.class, "-- LOCAL fo[" + i + "]: " + files[i]);
                    if (!files[i].isFolder() && PersistenceManager.TCGROUP_EXT.equals(files[i].getExt())) {
                        //wstcgrp file
                        TCGroupParser tcGroupParser;
                        if (tcGroupParserMap.containsKey(files[i].getName())) {
                            tcGroupParser = (TCGroupParser) tcGroupParserMap.get(files[i].getName());
                        } else {
                            tcGroupParser = new TCGroupParser(files[i].getName());
                            tcGroupParserMap.put(files[i].getName(), tcGroupParser);
                        }
                        tcGroupParser.setInLocalFolder(true);
                        tcGroupParser.setLocalParentFolder(localGroupFolder);
                    }
                }
            }
        }
        
        /*for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            if (DEBUG) Debug.log(GroupParser.class, "tcGroupParser: " + tcGroupParser.getName()
            + " isInModuleFolder:" + tcGroupParser.isInModuleFolder()
            + " isInLocalFolder:" + tcGroupParser.isInLocalFolder());
        }*/
        
        //Check if corresponding module is present and enabled.
        //We must load configuration data first because module info is stored in XML.
        List<TCGroupConfig> tcGroupCfgList = new ArrayList<TCGroupConfig>(tcGroupParserMap.size());
        List<TCGroupParser> toRemove = new ArrayList<TCGroupParser>(tcGroupParserMap.size());
        for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            TCGroupConfig tcGroupCfg;
            try {
                tcGroupCfg = tcGroupParser.load();
            } catch (IOException exc) {
                //If reading of one tcGroup fails we want to log message
                //and continue.
                // see #45497 - if something fails to load, remove it from local config..
                toRemove.add(tcGroupParser);
                deleteLocalTCGroup(tcGroupParser.getName());
                Logger.getLogger(GroupParser.class.getName()).log(Level.INFO, null, exc);
                continue;
            }
            boolean tcGroupAccepted = acceptTCGroup(tcGroupParser, tcGroupCfg);
            if (tcGroupAccepted) {
                tcGroupCfgList.add(tcGroupCfg);
            } else {
                toRemove.add(tcGroupParser);
                deleteLocalTCGroup(tcGroupParser.getName());
            }
        }
        for (int i = 0; i < toRemove.size(); i++) {
            TCGroupParser tcGroupParser = (TCGroupParser) toRemove.get(i);
            tcGroupParserMap.remove(tcGroupParser.getName());
        }
        
        sc.tcGroupConfigs = (TCGroupConfig []) 
            // safer array initialization, making sure the size of array matches size of list
            // see #45497
            tcGroupCfgList.toArray(new TCGroupConfig[0]);
        
        PersistenceManager pm = PersistenceManager.getDefault();
        for (int i = 0; i < sc.tcGroupConfigs.length; i++) {
            pm.addUsedTCId(sc.tcGroupConfigs[i].tc_id);
        }
        
        if (DEBUG) Debug.log(GroupParser.class, "readTCGroups LEAVE" + " group:" + getName());
    }
    
    /** Checks if module for given tcGroup exists.
     * @return true if tcGroup is valid - its module exists
     */
    private boolean acceptTCGroup (TCGroupParser tcGroupParser, TCGroupConfig config) {
        InternalConfig cfg = tcGroupParser.getInternalConfig();
        //Check module info
        if (cfg.moduleCodeNameBase != null) {
            ModuleInfo curModuleInfo = PersistenceManager.findModule
                                        (cfg.moduleCodeNameBase, cfg.moduleCodeNameRelease,
                                         cfg.moduleSpecificationVersion);
            if (curModuleInfo == null) {
                PersistenceManager.LOG.fine("Cannot find module \'" +
                          cfg.moduleCodeNameBase + " " + cfg.moduleCodeNameRelease + " " + 
                          cfg.moduleSpecificationVersion + "\' for tcgrp with name \'" + config.tc_id + "\'"); // NOI18N
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
    
    private void writeProperties (GroupConfig sc) throws IOException {
        if (DEBUG) Debug.log(GroupParser.class, "writeProperties ENTER" + " group:" + getName());
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(sc, internalCfg);
        if (DEBUG) Debug.log(GroupParser.class, "writeProperties LEAVE" + " group:" + getName());
    }
    
    private void writeTCGroups (GroupConfig sc) throws IOException {
        if (DEBUG) Debug.log(GroupParser.class, "writeTCGroups ENTER" + " group:" + getName());
        //Step 1: Clean obsolete tcGroup parsers
        Map<String, TCGroupConfig> tcGroupConfigMap = new HashMap<String, TCGroupConfig>(19);
        for (int i = 0; i < sc.tcGroupConfigs.length; i++) {
            tcGroupConfigMap.put(sc.tcGroupConfigs[i].tc_id, sc.tcGroupConfigs[i]);
        }
        List<String> toDelete = new ArrayList<String>(10);
        for (TCGroupParser tcGroupParser: tcGroupParserMap.values()) {
            if (!tcGroupConfigMap.containsKey(tcGroupParser.getName())) {
                toDelete.add(tcGroupParser.getName());
            }
        }
        for (int i = 0; i < toDelete.size(); i++) {
            /*if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.writeTCGroups"
            + " ** REMOVE FROM MAP tcGroupParser: " + toDelete.get(i));*/
            tcGroupParserMap.remove(toDelete.get(i));
            /*if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.writeTCGroups"
            + " ** DELETE tcGroupParser: " + toDelete.get(i));*/
            deleteLocalTCGroup(toDelete.get(i));
        }
        
        //Step 2: Create missing tcGoup parsers
        for (int i = 0; i < sc.tcGroupConfigs.length; i++) {
            if (!tcGroupParserMap.containsKey(sc.tcGroupConfigs[i].tc_id)) {
                TCGroupParser tcGroupParser = new TCGroupParser(sc.tcGroupConfigs[i].tc_id);
                tcGroupParserMap.put(sc.tcGroupConfigs[i].tc_id, tcGroupParser);
            }
        }
        
        //Step 3: Save all groups
        FileObject localFolder = localParentFolder.getFileObject(getName());
        if ((localFolder == null) && (tcGroupParserMap.size() > 0)) {
            //Create local group folder
            //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.writeTCGroups" + " CREATE LOCAL FOLDER");
            localFolder = FileUtil.createFolder(localParentFolder, getName());
        }
        //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.writeTCGroups" + " localFolder:" + localFolder);
        
        for (Iterator it = tcGroupParserMap.keySet().iterator(); it.hasNext(); ) {
            TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(it.next());
            tcGroupParser.setLocalParentFolder(localFolder);
            tcGroupParser.setInLocalFolder(true);
            tcGroupParser.save((TCGroupConfig) tcGroupConfigMap.get(tcGroupParser.getName()));
        }
        
        if (DEBUG) Debug.log(GroupParser.class, "writeTCGroups LEAVE" + " group:" + getName());
    }
    
    private void deleteLocalTCGroup (String tcGroupName) {
        if (DEBUG) Debug.log(GroupParser.class, "deleteLocalTCGroup" + " group:" + tcGroupName);
        if (localParentFolder == null) {
            return;
        }
        FileObject localGroupFolder = localParentFolder.getFileObject(groupName);
        if (localGroupFolder == null) {
            return;
        }
        FileObject tcGroupFO = localGroupFolder.getFileObject(tcGroupName, PersistenceManager.TCGROUP_EXT);
        if (tcGroupFO != null) {
            PersistenceManager.deleteOneFO(tcGroupFO);
        }
    }
    
    /** Removes TCGroupParser from GroupParser and cleans wstcgrp file from local folder.
     * @param tcGroupName unique name of tcgroup
     */
    void removeTCGroup (String tcGroupName) {
        //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.removeTCGroup" + " group:" + getName() + " tcGroup:" + tcGroupName);
        tcGroupParserMap.remove(tcGroupName);
        deleteLocalTCGroup(tcGroupName);
    }
    
    /** Adds TCGroupParser to GroupParser.
     * @param tcGroupName unique name of tcGroup
     */
    TCGroupConfig addTCGroup (String tcGroupName) {
        //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.addTCGroup" + " group:" + getName() + " tcGroup:" + tcGroupName);
        //Check consistency. TCGroupParser instance should not exist.
        TCGroupParser tcGroupParser = (TCGroupParser) tcGroupParserMap.get(tcGroupName);
        if (tcGroupParser != null) {
            PersistenceManager.LOG.log(Level.INFO,
            "[WinSys.GroupParser.addTCGroup]" // NOI18N
            + " Warning: GroupParser " + getName() + ". TCGroupParser " // NOI18N
            + tcGroupName + " exists but it should not."); // NOI18N
            tcGroupParserMap.remove(tcGroupName);
        }
        tcGroupParser = new TCGroupParser(tcGroupName);
        FileObject moduleFolder = moduleParentFolder.getFileObject(groupName);
        tcGroupParser.setModuleParentFolder(moduleFolder);
        tcGroupParser.setInModuleFolder(true);
        tcGroupParserMap.put(tcGroupName, tcGroupParser);
        TCGroupConfig tcGroupConfig = null;
        try {
            tcGroupConfig = tcGroupParser.load();
        } catch (IOException exc) {
            PersistenceManager.LOG.log(Level.INFO,
            "[WinSys.GroupParser.addTCGroup]" // NOI18N
            + " Warning: GroupParser " + getName() + ". Cannot load tcGroup " +  tcGroupName, exc); // NOI18N
        }
        return tcGroupConfig;
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
        return groupName;
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
        
        /** Group configuration data */
        private GroupConfig groupConfig = null;
        
        /** Internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject groupConfigFO;
            if (isInLocalFolder()) {
                //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.getConfigFOInput" + " looking for LOCAL");
                groupConfigFO = localParentFolder.getFileObject
                (GroupParser.this.getName(), PersistenceManager.GROUP_EXT);
            } else if (isInModuleFolder()) {
                //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.getConfigFOInput" + " looking for MODULE");
                groupConfigFO = moduleParentFolder.getFileObject
                (GroupParser.this.getName(), PersistenceManager.GROUP_EXT);
            } else {
                //XXX should not happen
                groupConfigFO = null;
            }
            //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.getConfigFOInput" + " groupConfigFO:" + groupConfigFO);
            return groupConfigFO;
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject groupConfigFO;
            groupConfigFO = localParentFolder.getFileObject
            (GroupParser.this.getName(), PersistenceManager.GROUP_EXT);
            if (groupConfigFO != null) {
                //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.getConfigFOOutput" + " groupConfigFO LOCAL:" + groupConfigFO);
                return groupConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(GroupParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.GROUP_EXT);
                //XXX should be improved localParentFolder can be null
                groupConfigFO = FileUtil.createData(localParentFolder, buffer.toString());
                //if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.getConfigFOOutput" + " LOCAL not found CREATE");

                return groupConfigFO;
            }
        }
        
        /**
         Reads group configuration data from XML file.
         Data are returned in output params.
         */
        void readData (GroupConfig groupCfg, InternalConfig internalCfg)
        throws IOException {
            groupConfig = groupCfg;
            internalConfig = internalCfg;
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing Group configuration file:" // NOI18N
                + GroupParser.this.getName());
            }
            InputStream is = null;
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*InputStream is = cfgFOInput.getInputStream();
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    if (DEBUG) Debug.log(GroupParser.class, "DUMP Group: " + GroupParser.this.getName());
                    String s = new String(arr);
                    if (DEBUG) Debug.log(GroupParser.class, s);*/
                    //DUMP END
                    is = cfgFOInput.getInputStream();
                    PersistenceManager.getDefault().getXMLParser(this).parse(new InputSource(is));
                }
            } catch (SAXException exc) {
                // Turn into annotated IOException
                String msg = NbBundle.getMessage(GroupParser.class,
                                                 "EXC_GroupParse", cfgFOInput);

                throw (IOException) new IOException(msg).initCause(exc);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException exc) {
                    Logger.getLogger(GroupParser.class.getName()).log(Level.INFO, null, exc);
                }
            }
            
            groupCfg = groupConfig;
            internalCfg = internalConfig;
            
            groupConfig = null;
            internalConfig = null;
        }

        @Override
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) throws SAXException {
            if ("group".equals(qname)) { // NOI18N
                handleGroup(attrs);
            } else if (internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) == 0) { // NOI18N
                //Parse version 2.0
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("name".equals(qname)) { // NOI18N
                    handleName(attrs);
                } else if ("state".equals(qname)) { // NOI18N
                    handleState(attrs);
                }
            } else {
                if (DEBUG) Debug.log(GroupParser.class, "-- GroupParser.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }

        @Override
        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }
        
        /** Reads element "group" */
        private void handleGroup (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.GroupParser.handleGroup]" // NOI18N
                + " Warning: Missing attribute \"version\" of element \"group\"."); // NOI18N
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
                Logger.getLogger(GroupParser.class.getName()).log(Level.INFO, null,
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
                groupConfig.name = name;
                if (!name.equals(GroupParser.this.getName())) {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.GroupParser.handleName]" // NOI18N
                    + " Error: Value of attribute \"unique\" of element \"name\"" // NOI18N
                    + " and configuration file name must be the same."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.GroupParser.handleName]" // NOI18N
                + " Error: Missing required attribute \"unique\" of element \"name\"."); // NOI18N
                throw new SAXException("Missing required attribute"); // NOI18N
            }
        }
        
        /** Reads element "state" */
        private void handleState (Attributes attrs) throws SAXException {
            String opened = attrs.getValue("opened"); // NOI18N
            if (opened != null) {
                if ("true".equals(opened)) { // NOI18N
                    groupConfig.opened = true;
                } else if ("false".equals(opened)) { // NOI18N
                    groupConfig.opened = false;
                } else {
                    PersistenceManager.LOG.log(Level.INFO,
                    "[WinSys.GroupParser.handleState]" // NOI18N
                    + " Warning: Invalid value of attribute \"opened\" of element \"state\"."); // NOI18N
                    groupConfig.opened = false;
                }
            } else {
                 PersistenceManager.LOG.log(Level.INFO,
                "[WinSys.GroupParser.handleState]" // NOI18N
                + " Error: Missing required attribute \"opened\" of element \"state\"."); // NOI18N
                groupConfig.opened = false;
            }
        }
        
        /** Writes data from asociated group to the xml representation */
        void writeData (GroupConfig sc, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(sc, ic);
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
                    //if (DEBUG) Debug.log(GroupParser.class, "-- DUMP Group: " + GroupParser.this.getName());
                    //if (DEBUG) Debug.log(GroupParser.class, buff.toString());
                } finally {
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                    } catch (IOException exc) {
                        Logger.getLogger(GroupParser.class.getName()).log(Level.INFO, null, exc);
                    }
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (GroupConfig gc, InternalConfig ic) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"); // NOI18N
            /*buff.append("<!DOCTYPE group PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Group Properties 2.0//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/group-properties2_0.dtd\">\n\n"); // NOI18N*/
            buff.append("<group version=\"2.0\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendName(gc, buff);
            appendState(gc, buff);
            
            buff.append("</group>\n"); // NOI18N
            return buff;
        }
        
        private void appendModule (InternalConfig ic, StringBuffer buff) {
            if (ic == null) {
                return;
            }
            if (ic.moduleCodeNameBase != null) {
                buff.append("    <module"); // NOI18N
                buff.append(" name=\""); // NOI18N
                buff.append(ic.moduleCodeNameBase);
                if (ic.moduleCodeNameRelease != null) {
                    buff.append("/" + ic.moduleCodeNameRelease); // NOI18N
                }
                if (ic.moduleSpecificationVersion != null) { 
                    buff.append("\" spec=\""); // NOI18N
                    buff.append(ic.moduleSpecificationVersion);
                }
                buff.append("\" />\n"); // NOI18N
            }
        }

        private void appendName (GroupConfig gc, StringBuffer buff) {
            buff.append("    <name"); // NOI18N
            buff.append(" unique=\""); // NOI18N
            buff.append(gc.name);
            buff.append("\""); // NOI18N
            buff.append(" />\n"); // NOI18N
        }
        
        private void appendState (GroupConfig gc, StringBuffer buff) {
            buff.append("    <state"); // NOI18N
            buff.append(" opened=\""); // NOI18N
            if (gc.opened) {
                buff.append("true"); // NOI18N
            } else {
                buff.append("false"); // NOI18N
            }
            buff.append("\""); // NOI18N
            buff.append(" />\n"); // NOI18N
        }
        
        /** Implementation of entity resolver. Points to the local DTD
         * for our public ID */
        @Override
        public InputSource resolveEntity (String publicId, String systemId)
        throws SAXException {
            if (INSTANCE_DTD_ID_2_0.equals(publicId)) {
                InputStream is = new ByteArrayInputStream(new byte[0]);
                //getClass().getResourceAsStream(INSTANCE_DTD_LOCAL);
//                if (is == null) {
//                    throw new IllegalStateException ("Entity cannot be resolved."); // NOI18N
//                }
                return new InputSource(is);
            }
            return null; // i.e. follow advice of systemID
        }
    }
    
}

