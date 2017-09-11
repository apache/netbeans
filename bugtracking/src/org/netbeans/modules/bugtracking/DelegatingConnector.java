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
