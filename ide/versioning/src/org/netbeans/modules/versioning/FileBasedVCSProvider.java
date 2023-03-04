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
package org.netbeans.modules.versioning;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author tomas
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.core.util.VCSSystemProvider.class)
public class FileBasedVCSProvider extends VCSSystemProvider implements LookupListener {

    /**
     * Result of Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(org.netbeans.modules.versioning.spi.VersioningSystem.class.class));
     * applies to all lgacy VCS registrations via org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.spi.VersioningSystem.class)
     */
    private final Lookup.Result<org.netbeans.modules.versioning.spi.VersioningSystem> systemsLookupResult;
    
    /**
     * applies to all registrations via {@link org.netbeans.modules.versioning.spi.VersioningSystem.Registration}
     */
    private final Lookup.Result<org.netbeans.modules.versioning.core.spi.VersioningSystem> delegatesLookupResult;
    
    /**
     * Holds all registered versioning systems.
     */
    private final Collection<VersioningSystem> versioningSystems = new ArrayList<VersioningSystem>(5);

    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    public FileBasedVCSProvider() {
        systemsLookupResult = Lookup.getDefault().lookup(new Lookup.Template<org.netbeans.modules.versioning.spi.VersioningSystem>(org.netbeans.modules.versioning.spi.VersioningSystem.class));
        delegatesLookupResult = Lookup.getDefault().lookup(new Lookup.Template<org.netbeans.modules.versioning.core.spi.VersioningSystem>(org.netbeans.modules.versioning.core.spi.VersioningSystem.class));
        systemsLookupResult.addLookupListener(this);
        delegatesLookupResult.addLookupListener(this);
    }
    
    private int refreshSerial;
    @Override
    public Collection<VersioningSystem> getVersioningSystems() {
        int rs = ++refreshSerial;
        if (rs != refreshSerial) {
            // TODO: Workaround for Lookup bug #132145, we have to abort here to keep the freshest list of versioning systems
            return versioningSystems;
        }
        Collection<? extends org.netbeans.modules.versioning.spi.VersioningSystem> systems = systemsLookupResult.allInstances();
        Collection<? extends org.netbeans.modules.versioning.core.spi.VersioningSystem> delegates = delegatesLookupResult.allInstances();
        synchronized(versioningSystems) {
            versioningSystems.clear();
            
            for (org.netbeans.modules.versioning.core.spi.VersioningSystem vs : delegates) {
                if(vs instanceof DelegatingVCS) {
                    versioningSystems.add((DelegatingVCS) vs); 
                }
            }
            
            for (org.netbeans.modules.versioning.spi.VersioningSystem vs : systems) {
                versioningSystems.add(new DelegatingVCS(vs));
            }
            
            return versioningSystems;
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        changeSupport.fireChange();
    }
    
}
