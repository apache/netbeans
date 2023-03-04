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
package org.netbeans.modules.versioning.core.util;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.spi.queries.CollocationQueryImplementation2;

/**
 * Warning: VCS internal use only. Not to be implemented by clients.
 * 
 * Provides implementations for {@link org.netbeans.modules.versioning.spi.VersioningSystem} 
 * and {@link org.netbeans.modules.versioning.fileproxy.spi.VersioningSystem}
 * 
 * @author Tomas Stupka
 */
public abstract class VCSSystemProvider {

    /**
     * Add a listener to changes in registered versioning systems
     * @param l 
     */
    public abstract void addChangeListener(ChangeListener l);
    
    /**
     * Stop listening to changes in registered versioning systems
     * @param l 
     */
    public abstract void removeChangeListener(ChangeListener l);
    
    /**
     * Provides all registered versioning systems 
     * 
     * @return a collections of all registered versioning systems 
     */
    public abstract Collection<VersioningSystem> getVersioningSystems();
    
    /**
     * Provides abstraction either over a {@link org.netbeans.modules.versioning.fileproxy.spi.VersioningSystem}
     * or a {@link org.netbeans.modules.versioning.spi.VersioningSystem}
     * @param <S> 
     */
    public interface VersioningSystem<S> {
        
        S getDelegate();
        
        public String getDisplayName();
        
        public String getMenuLabel();
        
        public boolean isLocalHistory();
        
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file);

        public VCSAnnotator getVCSAnnotator();

        public VCSInterceptor getVCSInterceptor();

        public VCSHistoryProvider getVCSHistoryProvider();
        
        public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile);

        public CollocationQueryImplementation2 getCollocationQueryImplementation();

        public VCSVisibilityQuery getVisibilityQuery();

        public void addPropertyCL(PropertyChangeListener listener);

        public void removePropertyCL(PropertyChangeListener listener);

        public boolean isExcluded(VCSFileProxy file);
        
        public boolean accept(VCSContext ctx);
        
        public boolean isMetadataFile(VCSFileProxy file);
        
    }
}
