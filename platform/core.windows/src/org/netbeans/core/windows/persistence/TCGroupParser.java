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
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Handle loading/saving of TopComponent reference in Group configuration data.
 *
 * @author Marek Slama
 */

class TCGroupParser {
    
    public static final String INSTANCE_DTD_ID_2_0
    = "-//NetBeans//DTD Top Component in Group Properties 2.0//EN"; // NOI18N
    
    private static final boolean DEBUG = Debug.isLoggable(TCGroupParser.class);
    
    /** Unique id from file name */
    private String tc_id;
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
    private InternalConfig internalConfig;
    
    /** true if wstcgrp file is present in module folder */
    private boolean inModuleFolder;
    /** true if wstcgrp file is present in local folder */
    private boolean inLocalFolder;
    
    public TCGroupParser(String tc_id) {
        this.tc_id = tc_id;
    }
    
    /** Load tcgroup configuration. */
    TCGroupConfig load () throws IOException {
        if (DEBUG) Debug.log(TCGroupParser.class, "load ENTER" + " tcGrp:" + tc_id);
        TCGroupConfig tcGroupCfg = new TCGroupConfig();
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(tcGroupCfg, internalCfg);
        if (DEBUG) Debug.log(TCGroupParser.class, "load LEAVE" + " tcGrp:" + tc_id);
        return tcGroupCfg;
    }
    
    /** Save tcGroup configuration. */
    void save (TCGroupConfig tcGroupCfg) throws IOException {
        if (DEBUG) Debug.log(TCGroupParser.class, "save ENTER" + " tcGrp:" + tc_id);
        
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(tcGroupCfg, internalCfg);
        if (DEBUG) Debug.log(TCGroupParser.class, "save LEAVE" + " tcGrp:" + tc_id);
    }
    
    String getName () {
        return tc_id;
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
    
    void setModuleParentFolder (FileObject moduleParentFolder) {
        this.moduleParentFolder = moduleParentFolder;
    }
    
    void setLocalParentFolder (FileObject localParentFolder) {
        this.localParentFolder = localParentFolder;
    }
    
    void log (String s) {
        Debug.log(TCGroupParser.class, s);
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** tcRef manager configuration data */
        private TCGroupConfig tcGroupConfig = null;
        
        /** internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject tcGroupConfigFO;
            if (isInLocalFolder()) {
                //log("-- TCGroupParser.getConfigFOInput" + " looking for LOCAL");
                tcGroupConfigFO = localParentFolder.getFileObject
                (TCGroupParser.this.getName(), PersistenceManager.TCGROUP_EXT);
            } else if (isInModuleFolder()) {
                //log("-- TCGroupParser.getConfigFOInput" + " looking for MODULE");
                tcGroupConfigFO = moduleParentFolder.getFileObject
                (TCGroupParser.this.getName(), PersistenceManager.TCGROUP_EXT);
            } else {
                //XXX should not happen
                tcGroupConfigFO = null;
            }
            //log("-- TCGroupParser.getConfigFOInput" + " tcGroupConfigFO:" + tcGroupConfigFO);
            return tcGroupConfigFO;
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject tcGroupConfigFO;
            tcGroupConfigFO = localParentFolder.getFileObject
            (TCGroupParser.this.getName(), PersistenceManager.TCGROUP_EXT);
            if (tcGroupConfigFO != null) {
                //log("-- TCGroupParser.getConfigFOOutput" + " tcGroupConfigFO LOCAL:" + tcGroupConfigFO);
                return tcGroupConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(TCGroupParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.TCGROUP_EXT);
                //XXX should be improved localParentFolder can be null
                tcGroupConfigFO = FileUtil.createData(localParentFolder, buffer.toString());
                //log("-- TCGroupParser.getConfigFOOutput" + " LOCAL not found CREATE");

                return tcGroupConfigFO;
            }
        }
        /** 
         Reads tcRef configuration data from XML file. 
         Data are returned in output params.
         */
        void readData (TCGroupConfig tcGroupCfg, InternalConfig internalCfg)
        throws IOException {
            tcGroupConfig = tcGroupCfg;
            internalConfig = internalCfg;
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing TCGroup configuration file:" // NOI18N
                + TCGroupParser.this.getName());
            }
            InputStream is = null;
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*InputStream is = cfgFOInput.getInputStream();
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    log("DUMP TCGroup: " + TCGroupParser.this.getName());
                    String s = new String(arr);
                    log(s);*/
                    //DUMP END
                    is = cfgFOInput.getInputStream();
                    PersistenceManager.getDefault().getXMLParser(this).parse(new InputSource(is));
                }
            } catch (SAXException exc) {
                // Turn into annotated IOException
                String msg = NbBundle.getMessage(TCGroupParser.class,
                                                 "EXC_TCGroupParse", cfgFOInput);

                throw (IOException) new IOException(msg).initCause(exc);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException exc) {
                    Logger.getLogger(TCGroupParser.class.getName()).log(Level.WARNING, null, exc);
                }
            }
                        
            tcGroupCfg = tcGroupConfig;
            internalCfg = internalConfig;
            
            tcGroupConfig = null;
            internalConfig = null;
        }

        @Override
        public void startElement (String nameSpace, String name, String qname, Attributes attrs)
        throws SAXException {
            if ("tc-group".equals(qname)) { // NOI18N
                handleTCGroup(attrs);
                // #125235: null check added
            } else if (internalConfig.specVersion != null && 
                    internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) == 0) { // NOI18N
                //Parse version 2.0
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("tc-id".equals(qname)) { // NOI18N
                    handleTcId(attrs);
                } else if ("open-close-behavior".equals(qname)) { // NOI18N
                    handleOpenCloseBehavior(attrs);
                }
            } else {
                log("-- TCGroupParser.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }

        @Override
        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }

        /** Reads element "tc-group" */
        private void handleTCGroup (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.TCGroupParser.handleTCGroup]" // NOI18N
                + " Warning: Missing attribute \"version\" of element \"tc-group\"."); // NOI18N
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
                Logger.getLogger(TCGroupParser.class.getName()).log(Level.WARNING, null,
                                  new IllegalStateException("Module release code was saved as null string" +
                                                            " for module " +
                                                            internalConfig.moduleCodeNameBase +
                                                            "! Repairing."));
                internalConfig.moduleCodeNameRelease = null;
            }
        }
        
        /** Reads element "tc-id" */
        private void handleTcId (Attributes attrs) throws SAXException {
            String tc_id = attrs.getValue("id"); // NOI18N
            if (tc_id != null) {
                tcGroupConfig.tc_id = tc_id;
                if (!tc_id.equals(TCGroupParser.this.getName())) {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.TCGroupParser.handleTcId]" // NOI18N
                    + " Error: Value of attribute \"id\" of element \"tc-id\"" // NOI18N
                    + " and configuration file name must be the same."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.TCGroupParser.handleTcId]" // NOI18N
                + " Error: Missing required attribute \"id\" of element \"tc-id\"."); // NOI18N
                throw new SAXException("Missing required attribute"); // NOI18N
            }
        }
        
        /** Reads element "open-close-behavior" */
        private void handleOpenCloseBehavior (Attributes attrs) throws SAXException {
            String open = attrs.getValue("open"); // NOI18N;
            if (open != null) {
                if ("true".equals(open)) { // NOI18N
                    tcGroupConfig.open = true;
                } else if ("false".equals(open)) { // NOI18N
                    tcGroupConfig.open = false;
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                    + " Warning: Invalid value of attribute \"open\"" // NOI18N
                    + " of element \"open-close-behavior\"."); // NOI18N
                    tcGroupConfig.open = false;
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                + " Warning: Missing required attribute \"open\"" // NOI18N
                + " of element \"open-close-behavior\"."); // NOI18N
                tcGroupConfig.open = false;
            }
            
            String close = attrs.getValue("close"); // NOI18N;
            if (close != null) {
                if ("true".equals(close)) { // NOI18N
                    tcGroupConfig.close = true;
                } else if ("false".equals(close)) { // NOI18N
                    tcGroupConfig.close = false;
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                    + " Warning: Invalid value of attribute \"close\"" // NOI18N
                    + " of element \"open-close-behavior\"."); // NOI18N
                    tcGroupConfig.close = false;
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                + " Warning: Missing required attribute \"close\"" // NOI18N
                + " of element \"open-close-behavior\"."); // NOI18N
                tcGroupConfig.close = false;
            }
            
            String wasOpened = attrs.getValue("was-opened"); // NOI18N;
            if (wasOpened != null) {
                if ("true".equals(wasOpened)) { // NOI18N
                    tcGroupConfig.wasOpened = true;
                } else if ("false".equals(wasOpened)) { // NOI18N
                    tcGroupConfig.wasOpened = false;
                } else {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                    + " Warning: Invalid value of attribute \"was-opened\"" // NOI18N
                    + " of element \"open-close-behavior\"."); // NOI18N
                    tcGroupConfig.wasOpened = false;
                }
            } else {
                tcGroupConfig.wasOpened = false;
            }
        }
        
        /** Writes data from asociated tcRef to the xml representation */
        void writeData (TCGroupConfig tcGroupCfg, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(tcGroupCfg, ic);
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
                    //log("DUMP TCGroup: " + TCGroupParser.this.getName());
                    //log(buff.toString());
                } finally {
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                    } catch (IOException exc) {
                        Logger.getLogger(TCGroupParser.class.getName()).log(Level.WARNING, null, exc);
                    }
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (TCGroupConfig tcGroupCfg, InternalConfig ic) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            String curValue = null;
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"). // NOI18N
            /*buff.append("<!DOCTYPE tc-group PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Top Component in Group Properties 2.0//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/tc-group2_0.dtd\">\n\n"); // NOI18N*/
                append("<tc-group version=\"2.0\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendTcId(tcGroupCfg, buff);
            appendOpenCloseBehavior(tcGroupCfg, buff);
            
            buff.append("</tc-group>\n"); // NOI18N
            return buff;
        }
        
        private void appendModule (InternalConfig ic, StringBuffer buff) {
            if (ic == null) {
                return;
            }
            if (ic.moduleCodeNameBase != null) {
                buff.append(" <module name=\""); // NOI18N
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

        private void appendTcId (TCGroupConfig tcGroupCfg, StringBuffer buff) {
            buff.append(" <tc-id id=\"").append(
                    PersistenceManager.escapeTcId4XmlContent(tcGroupCfg.tc_id)).
                    append("\"/>\n"); // NOI18N
        }
        
        private void appendOpenCloseBehavior (TCGroupConfig tcGroupCfg, StringBuffer buff) {
            buff.append(" <open-close-behavior open=\"").append(tcGroupCfg.open). // NOI18N
                append("\" close=\"").append(tcGroupCfg.close). // NOI18N
                append("\" was-opened=\"").append(tcGroupCfg.wasOpened).append("\"/>\n"); // NOI18N
        }
        
    }
    
}
