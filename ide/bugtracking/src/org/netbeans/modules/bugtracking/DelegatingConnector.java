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
package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Tomas Stupka
 */
public class DelegatingConnector implements BugtrackingConnector {
    
    private static final boolean OVERRIDE_REPOSITORY_MANAGEMENT = Boolean.getBoolean("org.netbeans.modules.bugtracking.connector.overrrideRepositoryManagement"); // NOI18N
    
    private final Map<?, ?> map;
    private final String tooltip;
    private final Image image;
    private final String id;
    private final String displayName;
    private final boolean providesRepositoryManagement;
    
    private BugtrackingConnector delegate;
    
    public static BugtrackingConnector create(Map<?, ?> map) {
        return new DelegatingConnector(map);
    }

    public DelegatingConnector(BugtrackingConnector delegate, String id, String displayName, String tooltip, Image image) {
        this.tooltip = tooltip;
        this.image = image;
        this.id = id;
        this.displayName = displayName;
        this.delegate = delegate;
        this.providesRepositoryManagement = true;
        map = null;
    }
    
    private DelegatingConnector(Map<?, ?> map) {
        this.map = map;
        tooltip = (String) map.get("tooltip"); //NOI18N;
        String path = (String) map.get("iconPath"); //NOI18N
        image = path != null && !path.equals("") ? ImageUtilities.loadImage(path) : null; 
        id = (String) map.get("id"); //NOI18N
        displayName = (String) map.get("displayName"); //NOI18N
        providesRepositoryManagement = (Boolean) map.get("providesRepositoryManagement"); //NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "Created DelegatingConnector for : {0}", map.get("displayName")); // NOI18N
    }

    public BugtrackingConnector getDelegate() {
        if(delegate == null) {
            assert map != null;
            delegate = (BugtrackingConnector) map.get("delegate"); // NOI18N
            if(delegate == null) {
                BugtrackingManager.LOG.log(Level.WARNING, "Couldn't create delegate for : {0}", map.get("displayName")); // NOI18N
            } else {
                BugtrackingManager.LOG.log(Level.FINE, "Created delegate for : {0}", map.get("displayName")); // NOI18N
            }
        }
        return delegate;
    }

    public String getTooltip() {
        return tooltip;
    }

    public Image getIcon() {
        return image;
    }

    public String getID() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean providesRepositoryManagement() {
        return OVERRIDE_REPOSITORY_MANAGEMENT ? true : providesRepositoryManagement;
    }
    
    @Override
    public Repository createRepository(RepositoryInfo info) {
        BugtrackingConnector d = getDelegate();
        return d != null ? d.createRepository(info) : null;
    }

    @Override
    public Repository createRepository() {
        BugtrackingConnector d = getDelegate();
        return d != null ? d.createRepository() : null;
    }

}
