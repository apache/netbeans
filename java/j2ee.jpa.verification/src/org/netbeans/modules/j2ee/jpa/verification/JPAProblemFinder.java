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

package org.netbeans.modules.j2ee.jpa.verification;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class JPAProblemFinder {
    private boolean cancelled = false;
    private FileObject file = null;
    private Object cancellationLock = new Object();
    private JPAProblemContext context = null;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
    
    public static final Logger LOG = Logger.getLogger(JPAProblemFinder.class.getName());
    private static final String PERSISTENCE_SCOPES_LISTENER = "jpa.verification.scopes_listener"; //NOI18N
    private static final Object singleInstanceLock = new Object();
    private static JPAProblemFinder runningInstance = null;
    private static boolean usgLogged;
    
    public JPAProblemFinder(FileObject file){
        assert file != null;
        this.file = file;
    }
    
    public void run(final CompilationInfo info) throws Exception{
        if (!"text/x-java".equals(file.getMIMEType())){ //NOI18N
            return;
        }
        
        if (runningInstance != null){
            runningInstance.cancel();
        }
        
        synchronized(singleInstanceLock){
            runningInstance = this;
            // the 'cancelled' flag must be reset as the instance of JPAProblemFinder is reused
            cancelled = false;
            problemsFound.clear();
            createPersistenceScopesListener(file, info.getDocument());
            MetadataModel<EntityMappingsMetadata> emModel = ModelUtils.getModel(file);
            
            if (emModel == null){
                return; // File doesn't belong to any project or project doesn't support JPA
            }
            
            runningInstance = null;
        }
    }
    
    public void cancel(){
        LOG.fine("Cancelling JPAProblemFinder task");
        cancelled = true;
        
        synchronized(cancellationLock){
            if (context != null){
                context.setCancelled(true);
            }
        }
    }
    
    public boolean isCancelled(){
        return cancelled;
    }
    
    public List<? extends ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    private void createPersistenceScopesListener(FileObject file, Document doc){
        if (doc == null){
            return;
        }
        
        LOG.fine("Creating PersistenceScopesListener on " + file.getName());
        Project project = FileOwnerQuery.getOwner(file);
        
        if (project != null){
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            PersistenceScopes scopes = PersistenceScopes.getPersistenceScopes(project, cp != null ? cp.findOwnerRoot(file) : null);
            
            if (scopes != null){
                PersistenceScopesListener listener = (PersistenceScopesListener) doc.getProperty(PERSISTENCE_SCOPES_LISTENER);
                
                if (listener == null){
                    listener = new PersistenceScopesListener(file);
                    PropertyChangeListener weakListener = WeakListeners.create(PropertyChangeListener.class, listener, null);
                    scopes.addPropertyChangeListener(weakListener);
                    
                    // scopes listener should live as long as the document
                    doc.putProperty(PERSISTENCE_SCOPES_LISTENER, listener);
                }
                
                ArrayList<PersistenceXMLListener> pxmlListeners = new ArrayList<PersistenceXMLListener>();
                
                for (PersistenceScope scope : scopes.getPersistenceScopes()){
                    FileObject persistenceXML = scope.getPersistenceXml();
                    if(persistenceXML!=null){//persistence xml may be deleted/renamed
                        PersistenceXMLListener pxmlListener = new PersistenceXMLListener(file);
                        FileChangeListener weakPXMLListener = WeakListeners.create(FileChangeListener.class, pxmlListener, null);
                        persistenceXML.addFileChangeListener(weakPXMLListener);
                        pxmlListeners.add(pxmlListener);
                        LOG.fine("Added PersistenceXMLListener to " + persistenceXML.getName());
                    }
                }
                
                // persistence.xml listeners should live as long as the scopes listener
                listener.setPXMLListeners(pxmlListeners);
            }
        }
    }
    
    private abstract class RescanTrigger{
        private FileObject file;
        
        RescanTrigger(FileObject file){
            this.file = file;
        }
        
        void rescan(){
            JavaSource javaSrc = JavaSource.forFileObject(file);
            
            if (javaSrc != null){
                try{
                    javaSrc.runUserActionTask(new ProblemFinderCompControl(file), true);
                } catch (IOException e){
                    // IOE can happen legitimatelly, see #103453
                    LOG.log(Level.FINE, e.getMessage(), e);
                }
            }
        }
    }
    
    private class PersistenceScopesListener extends RescanTrigger implements PropertyChangeListener{
        List<PersistenceXMLListener> pxmlListeners;
        
        PersistenceScopesListener(FileObject file){
            super(file);
        }
        
        public void propertyChange(PropertyChangeEvent evt){
            LOG.fine("Received a change event from PersistenceScopes");
            rescan();
        }
        
        void setPXMLListeners(List<PersistenceXMLListener> pxmlListeners){
            this.pxmlListeners = pxmlListeners;
        }
    }
    
    private class PersistenceXMLListener extends RescanTrigger implements FileChangeListener{
        PersistenceXMLListener(FileObject file){
            super(file);
        }
        
        public void fileChanged(FileEvent fe){
            LOG.fine("Received a change event from persistence.xml");
            rescan();
        }
        
        public void fileFolderCreated(FileEvent fe){}
        public void fileDataCreated(FileEvent fe){}
        public void fileDeleted(FileEvent fe){}
        public void fileRenamed(FileRenameEvent fe){}
        public void fileAttributeChanged(FileAttributeEvent fe){}
    }
    
    public static class ProblemFinderCompInfo extends JPAProblemFinder implements CancellableTask<CompilationInfo>{
        public ProblemFinderCompInfo(FileObject file){
            super(file);
        }
    }
    
    public static class ProblemFinderCompControl extends JPAProblemFinder implements CancellableTask<CompilationController>{
        public ProblemFinderCompControl(FileObject file){
            super(file);
        }
        
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            super.run(controller);
        }
    }
}
