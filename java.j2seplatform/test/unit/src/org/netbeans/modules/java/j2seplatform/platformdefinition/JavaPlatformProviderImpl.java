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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;

/**
 *
 * @author  tom
 */
public final class JavaPlatformProviderImpl implements JavaPlatformProvider {


    private PropertyChangeSupport support;
    private List<JavaPlatform> platforms;
    private JavaPlatform defaultPlatform;

    /** Creates a new instance of JavaPlatformProviderImpl */
    public JavaPlatformProviderImpl() {
        this.support = new PropertyChangeSupport (this);
        this.platforms = new ArrayList<JavaPlatform>();
        this.addPlatform (this.createDefaultPlatform());
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }    
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }    
    
    public void addPlatform (JavaPlatform platform) {
        this.platforms.add (platform);
        this.support.firePropertyChange(PROP_INSTALLED_PLATFORMS, null, null);
    }
    
    public void removePlatform (JavaPlatform platform) {
        this.platforms.remove(platform);
        this.support.firePropertyChange(PROP_INSTALLED_PLATFORMS, null, null);
    }
        
    @Override
    public JavaPlatform[] getInstalledPlatforms() {
        return this.platforms.toArray(new JavaPlatform[this.platforms.size()]);
    }

    @Override
    public JavaPlatform getDefaultPlatform() {
        return createDefaultPlatform ();
    }
    
    private synchronized JavaPlatform createDefaultPlatform () {
        if (this.defaultPlatform == null) {
            System.getProperties().put("jdk.home",System.getProperty("java.home"));     //NOI18N
            this.defaultPlatform = DefaultPlatformImpl.create (null,null,null);
        }
        return defaultPlatform;
    }

}
