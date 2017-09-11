/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.core;

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
public class FileProxyBasedVCSProvider extends VCSSystemProvider implements LookupListener {

    /**
     * Result of Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
     */
    private final Lookup.Result<org.netbeans.modules.versioning.core.spi.VersioningSystem> systemsLookupResult;
    
    /**
     * Holds all registered versioning systems.
     */
    private final Collection<VersioningSystem> versioningSystems = new ArrayList<VersioningSystem>(5);

    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    public FileProxyBasedVCSProvider() {
        systemsLookupResult = Lookup.getDefault().lookup(new Lookup.Template<org.netbeans.modules.versioning.core.spi.VersioningSystem>(org.netbeans.modules.versioning.core.spi.VersioningSystem.class));
        systemsLookupResult.addLookupListener(this);
    }
    
    private int refreshSerial;
    @Override
    public Collection<VersioningSystem> getVersioningSystems() {
        int rs = ++refreshSerial;
        if (rs != refreshSerial) {
            // TODO: Workaround for Lookup bug #132145, we have to abort here to keep the freshest list of versioning systems
            return versioningSystems;
        }
        Collection<? extends org.netbeans.modules.versioning.core.spi.VersioningSystem> systems = systemsLookupResult.allInstances();
        synchronized(versioningSystems) {
            versioningSystems.clear();
            for (org.netbeans.modules.versioning.core.spi.VersioningSystem vs : systems) {
                if(vs instanceof DelegatingVCS) {
                    versioningSystems.add((DelegatingVCS) vs);
                } else if (!"org.netbeans.modules.versioning.DelegatingVCS".equals(vs.getClass().getName())){
                    versioningSystems.add(DelegatingVCS.create(vs));
                }
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
