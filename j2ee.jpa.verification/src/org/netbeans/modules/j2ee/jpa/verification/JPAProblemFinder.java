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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    public final static Logger LOG = Logger.getLogger(JPAProblemFinder.class.getName());
    private final static String PERSISTENCE_SCOPES_LISTENER = "jpa.verification.scopes_listener"; //NOI18N
    private final static Object singleInstanceLock = new Object();
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
