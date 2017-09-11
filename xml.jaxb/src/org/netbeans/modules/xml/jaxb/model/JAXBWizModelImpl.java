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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.jaxb.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.api.model.JAXBWizModel;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEvent;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListener;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.model.events.JAXBWizEventImpl;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author gpatil
 */
@ProjectServiceProvider(service=JAXBWizModel.class, projectType={
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject"
})
public class JAXBWizModelImpl implements JAXBWizModel {
    private List<JAXBWizEventListener> listeners = null;
    private Project project;
    // Do not need to listen to Dir see #110406
    //private JaxbCfgChangeListener jaxbListener = new JaxbCfgChangeListener();
    private boolean swallowCfgFileEditEvent = false;
        
    public JAXBWizModelImpl(Project prj){
        this.project = prj;
         // Do not need to listen to Dir see #110406
        //ProjectHelper.addCfgFileChangeListener(project, jaxbListener);
    }
    
    public synchronized void addJAXBWizEventListener(JAXBWizEventListener listener) {
        if (this.listeners == null){
            this.listeners = new ArrayList<JAXBWizEventListener>();
        }
        this.listeners.add(listener);
    }

    public synchronized void removeJAXBWizEventListener(JAXBWizEventListener listener) {
        if (this.listeners != null){
            this.listeners.remove(listener);
        }
    }
    
    public void fireSchemaAddedEvent(Schemas ss, Schema schema){
        JAXBWizEvent event = new JAXBWizEventImpl(ss, null, schema, 
                JAXBWizEvent.JAXBWizEventType.EVENT_BINDING_ADDED);
        dispatchEvent(event);
    }
    
    public void fireSchemaChangedEvent(Schemas ss, Schema oS, Schema nS){
        JAXBWizEvent event = new JAXBWizEventImpl(ss, oS, nS,
                JAXBWizEvent.JAXBWizEventType.EVENT_BINDING_CHANGED);
        dispatchEvent(event);
    }

    public void fireSchemaDeletedEvent(Schemas ss, Schema oS){
        JAXBWizEvent event = new JAXBWizEventImpl(ss, oS, null,
                JAXBWizEvent.JAXBWizEventType.EVENT_BINDING_DELETED);
        dispatchEvent(event);
    }

    public void fireCfgFileEditedEvent(Schemas ss){
        JAXBWizEvent event = new JAXBWizEventImpl(ss, null, null,
                JAXBWizEvent.JAXBWizEventType.EVENT_CFG_FILE_EDITED);
        dispatchEvent(event);
    }
    
    private void dispatchEvent(JAXBWizEvent event){
        List<JAXBWizEventListener> lss = new ArrayList<JAXBWizEventListener>();
        synchronized (this){
            if (this.listeners != null){
                lss.addAll(this.listeners);
            }
        }
        JAXBWizEvent.JAXBWizEventType  eventType = event.getEventType();
        
        for (JAXBWizEventListener ls: lss){
            try {                
                switch (eventType){
                    case EVENT_BINDING_ADDED: 
                        ls.bindingAdded(event);
                        break;
                    case EVENT_BINDING_CHANGED: 
                        ls.bindingChanged(event);
                        break;
                    case EVENT_BINDING_DELETED: 
                        ls.bindingDeleted(event);
                        break;
                    case EVENT_CFG_FILE_EDITED:
                        ls.configFileEdited(event);
                        break;                        
                }
            } catch (Exception ex){
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public void addSchema(Schema schema){
        Schemas scs = ProjectHelper.getXMLBindingSchemas(project);
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }
            ProjectHelper.addSchema(project, scs, schema);                    
            this.fireSchemaAddedEvent(scs, schema);
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }            
        }
    }
    
    public void changeSchema(Schema os, Schema ns){        
        Schemas scs = ProjectHelper.getXMLBindingSchemas(project);        
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }

            ProjectHelper.removeSchema(project, scs, os);
            ProjectHelper.addSchema(project, scs, ns);
            this.fireSchemaChangedEvent(scs, os, ns);        
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }            
        }        
    }    
    
    public void deleteSchema(Schema schema){
        Schemas scs = ProjectHelper.getXMLBindingSchemas(project);
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }
        
            ProjectHelper.removeSchema(project, scs, schema);
            this.fireSchemaDeletedEvent(scs, schema);
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }            
        }        
    }    
    
    private final class JaxbCfgChangeListener extends FileChangeAdapter {
        private void refreshNodes() {
            boolean skipEvent = false;
            synchronized (JAXBWizModelImpl.this){
                skipEvent = JAXBWizModelImpl.this.swallowCfgFileEditEvent;
            }

            if (!skipEvent){
                Schemas scs = ProjectHelper.getXMLBindingSchemas(project);
                JAXBWizModelImpl.this.fireCfgFileEditedEvent(scs);
            }
            
//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    try {
//                        JAXBRootNodeList.this.rootKeys.clear();
//                        fireChange();
//                    } catch (Exception ex) {
//                        logger.log(Level.WARNING, "refreshing root nodes.", ex);
//                    }
//
//                    try {
//                        updateKeys();
//                    } catch (Exception ex) {
//                        logger.log(Level.WARNING, "refreshing root nodes.", ex);
//                    }
//                    fireChange();
//                }
//            });
        }

        public void fileChanged(FileEvent fe) {
            refreshNodes();
        }

        public void fileRenamed(FileEvent fe) {
            refreshNodes();
        }

        public void fileDataCreated(FileEvent fe) {
            // New file is created, check if config file is created.
            FileObject fo = ProjectHelper.getFOForBindingConfigFile(project);
            if ((fo != null) && (fo.isValid())) {
                // Remove listening on folder, add for the file
                
                // Do not need to listen to Dir see #110406
                //ProjectHelper.removeModelListner(project, jaxbListener);
                //ProjectHelper.addCfgFileChangeListener(project, jaxbListener);
                refreshNodes();
            } 
        }

        public void fileDeleted(FileEvent fe) {
            refreshNodes();
        }
    }
}
