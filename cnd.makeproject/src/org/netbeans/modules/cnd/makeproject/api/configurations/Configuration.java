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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

public abstract class Configuration implements ProjectConfiguration {
    private FSPath fsPath;
    private String name;
    private boolean defaultConfiguration;

    private PropertyChangeSupport pcs = null;

    private final Map<String, ConfigurationAuxObject> auxObjectsMap =
            Collections.synchronizedSortedMap(new TreeMap<String, ConfigurationAuxObject>());

    private Configuration cloneOf;

    protected Configuration(String baseDir, String name) {
        this(new FSPath(CndFileUtils.getLocalFileSystem(), baseDir), name);
    }
    
    protected Configuration(FSPath fsPath, String name) {
        this.fsPath = fsPath;
        this.name = name;
        defaultConfiguration = false;

        // For change support
        pcs = new PropertyChangeSupport(this);
    }
    
    // extracted from constructor to avoid leaking "this"
    // MUST be called by descendant classes
    protected final void initAuxObjects() {
        // Create and initialize auxiliary objects
        ConfigurationAuxObjectProvider[] auxObjectProviders = ConfigurationDescriptorProvider.getAuxObjectProviders();        
        for (int i = 0; i < auxObjectProviders.length; i++) {
            ConfigurationAuxObject pao = auxObjectProviders[i].factoryCreate(fsPath.getPath(), pcs, this); //XXX:fullRemote:fileSystem
            pao.initialize();
            //auxObjects.add(pao);
            String id = pao.getId();
            if (auxObjectsMap.containsKey(id)) {
                System.err.println("Init duplicated ConfigurationAuxObject id="+id);
            }
            auxObjectsMap.put(id,pao);
        }
    }

    public void setCloneOf(Configuration profile) {
        this.cloneOf = profile;
    }

    public Configuration getCloneOf() {
        return cloneOf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseDir() {
        // this dir is possibly local directory (in remote mode)
        return fsPath.getPath();
    }
    
    public FileSystem getFileSystem() {
        return fsPath.getFileSystem();
    }
    
    //XXX:fullRemote:fileSystem - change with setFSPath
    public void setBaseDir(String baseDir) {
        CndUtils.assertTrue(CndFileUtils.isLocalFileSystem(fsPath.getFileSystem()), "Setting baseDir for non-local configuration?!"); //NOI18N
        this.fsPath = new FSPath(fsPath.getFileSystem(), baseDir);
    }

    public FSPath getBaseFSPath() {
        return fsPath;
    }

    public void setBaseFSPath(FSPath fsPath) {
        this.fsPath = fsPath;
    }
    
    @Override
    public String getDisplayName() {
            return getName();
    }

    public boolean isDefault() {
        return defaultConfiguration;
    }

    public void setDefault(boolean b) {
        defaultConfiguration = b;
    }

    @Override
    public String toString() {
        // Please note that toString() is used in some UIs
        // And I can not be sure I know all the places it is used in a UI :(
        if (isDefault()) {
            return getDisplayName() + " " + getString("ActiveTxt"); // NOI18N
        } else {
            return getDisplayName();
        }
    }

    public void addAuxObject(ConfigurationAuxObject pao) {
        String id = pao.getId();
        if (auxObjectsMap.containsKey(id)) {
            System.err.println("Add duplicated ConfigurationAuxObject id="+id);
        }
        auxObjectsMap.put(id,pao);
    }


    public ConfigurationAuxObject removeAuxObject(ConfigurationAuxObject pao) {
        return auxObjectsMap.remove(pao.getId());
    }


    public ConfigurationAuxObject removeAuxObject(String id) {
        return auxObjectsMap.remove(id);
    }

    public ConfigurationAuxObject getAuxObject(String id) {
        return auxObjectsMap.get(id);
    }

    public ConfigurationAuxObject[] getAuxObjects() {
        List<ConfigurationAuxObject> list;
        synchronized (auxObjectsMap){
            list = new ArrayList<>(auxObjectsMap.values());
        }
        return list.toArray(new ConfigurationAuxObject[list.size()]);
    }

    /** guarded by auxObjectsMap */
    private boolean valid = true;

    public void clear() {
        synchronized (auxObjectsMap) {
            auxObjectsMap.clear();
            valid = false;
        }
    }

    public boolean isValid() {
        synchronized (auxObjectsMap) {
            return valid;
        }
    }

    public void setAuxObjects(List<ConfigurationAuxObject> v) {
        synchronized (auxObjectsMap) {
            auxObjectsMap.clear();
            v.forEach((object) -> {
                auxObjectsMap.put(object.getId(),object);
            });
        }
    }

    public abstract Configuration cloneConf();

    public abstract void assign(Configuration conf);

    public abstract Configuration copy();

    public void cloneConf(Configuration clone) {
        // name is already cloned
        clone.setDefault(isDefault());
    }

    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public RunProfile getProfile() {
        return (RunProfile)getAuxObject(RunProfile.PROFILE_ID);
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(Configuration.class, s);
    }
}
