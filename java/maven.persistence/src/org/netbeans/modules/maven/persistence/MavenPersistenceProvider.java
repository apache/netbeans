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
/*
 * Contributor(s): Daniel Mohni
 */
package org.netbeans.modules.maven.persistence;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URI;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author Daniel Mohni
 */
@ProjectServiceProvider(service={PersistenceLocationProvider.class, PersistenceScopeProvider.class, PersistenceScopesProvider.class},
projectType="org-netbeans-modules-maven")
public class MavenPersistenceProvider implements PersistenceLocationProvider, 
        PersistenceScopeProvider, PersistenceScopesProvider 
{
    public static final String PROP_PERSISTENCE = "MavenPersistence"; //NOI18N
    
    private PersistenceLocationProviderImpl  locProvider = null;
    private PersistenceScopesProviderImpl    scopesProvider   = null;
    private PersistenceScopeProviderImpl     scopeProvider    = null;
   
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    private ResourceListener res = new ResourceListener();
    /**
     * Creates a new instance of MavenPersistenceProvider
     */
    public MavenPersistenceProvider(Project proj, Lookup lkp)
    {
        NbMavenProject watcher = lkp.lookup(NbMavenProject.class);
        locProvider    = new PersistenceLocationProviderImpl(proj, watcher);
        scopeProvider  = new PersistenceScopeProviderImpl(locProvider, proj);
        scopesProvider = new PersistenceScopesProviderImpl(scopeProvider);
        
        propChangeSupport.addPropertyChangeListener(locProvider);
        propChangeSupport.addPropertyChangeListener(scopesProvider);
                
        //TODO add FileChangeListener on persistence.xml
        watcher.addWatchedPath(PersistenceLocationProviderImpl.DEF_PERSISTENCE);
        watcher.addWatchedPath(PersistenceLocationProviderImpl.ALT_PERSISTENCE);
        watcher.addPropertyChangeListener(WeakListeners.propertyChange(res, watcher));
    }

    /**************************************************************************
     * PersistenceLocationProvider methodes
     *************************************************************************/
    @Override
    public FileObject getLocation()
    {
        return locProvider.getLocation();
    }

    @Override
    public FileObject createLocation() throws IOException
    {
         return locProvider.createLocation();
    }

    /**************************************************************************
     * PersistenceScopeProvider methodes
     *************************************************************************/
    @Override
    public PersistenceScope findPersistenceScope(FileObject fileObject)
    {
        return scopeProvider.findPersistenceScope(fileObject);
    }

    /**************************************************************************
     * PersistenceScopesProvider methodes
     *************************************************************************/
    @Override
    public PersistenceScopes getPersistenceScopes()
    {
        return scopesProvider.getPersistenceScopes();
    }

    
    //TODO rewrite..
    private class ResourceListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (NbMavenProject.PROP_RESOURCE.equals(event.getPropertyName())) {
                URI newval = (URI)event.getNewValue();
                if (  newval.getPath().endsWith(PersistenceLocationProviderImpl.DEF_PERSISTENCE)
                   || newval.getPath().endsWith(PersistenceLocationProviderImpl.ALT_PERSISTENCE)) {
                   //TODO could be a bit too eager to fire. We might want to check if the URI is actually coming from the 
                   // current project.
                   propChangeSupport.firePropertyChange(PROP_PERSISTENCE, null, null);
                    
                }
            }
        }
    }
 }
