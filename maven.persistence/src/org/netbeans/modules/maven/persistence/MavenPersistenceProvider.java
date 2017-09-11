/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
