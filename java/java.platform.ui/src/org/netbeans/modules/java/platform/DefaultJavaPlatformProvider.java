/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.platform;

import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.java.platform.implspi.JavaPlatformProvider.class)
public class DefaultJavaPlatformProvider implements JavaPlatformProvider, FileChangeListener {

    private static final String PLATFORM_STORAGE = "Services/Platforms/org-netbeans-api-java-Platform";  //NOI18N
    private static final String DEFAULT_PLATFORM_ATTR = "default-platform"; //NOI18N

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    //@GuardedBy("this")
    private FileObject storageCache;
    //@GuardedBy("this")
    private FileObject lastFound;
    //@GuardedBy("this")
    private FileChangeListener pathListener;
    private JavaPlatform defaultPlatform;

    private static final Logger LOG = Logger.getLogger(DefaultJavaPlatformProvider.class.getName());

    public DefaultJavaPlatformProvider () {
    }

    @Override
    public JavaPlatform[] getInstalledPlatforms() {
        final List<JavaPlatform> platforms = new ArrayList<JavaPlatform>();
        final FileObject storage = getStorage();
        if (storage != null) {
            for (FileObject platformDefinition : storage.getChildren()) {
                try {
                    final DataObject dobj = DataObject.find(platformDefinition);
                    final InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
                    if (ic == null) {
                        LOG.log(
                            Level.WARNING,
                            "The file: {0} has no InstanceCookie",  //NOI18N
                            platformDefinition.getNameExt());
                        continue;
                    } else  if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of)ic).instanceOf(JavaPlatform.class)) {
                            platforms.add((JavaPlatform) ic.instanceCreate());
                        } else {
                            LOG.log(
                                Level.WARNING,
                                "The file: {0} is not an instance of JavaPlatform", //NOI18N
                                platformDefinition.getNameExt());
                        }
                    } else {
                        Object instance = ic.instanceCreate();
                        if (instance instanceof JavaPlatform) {
                            platforms.add((JavaPlatform) instance);
                        } else {
                            LOG.log(
                                Level.WARNING,
                                "The file: {0} is not an instance of JavaPlatform", //NOI18N
                                platformDefinition.getNameExt());
                        }
                    }
                } catch (ClassNotFoundException | IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        return platforms.toArray(new JavaPlatform[0]);
    }

    @Override
    public JavaPlatform getDefaultPlatform() {
        if (this.defaultPlatform == null) {
            defaultPlatform = getDefaultPlatformByHint();
            if (defaultPlatform != null) {
                return defaultPlatform;
            }
            JavaPlatform[] allPlatforms = this.getInstalledPlatforms();
            for (int i=0; i< allPlatforms.length; i++) {
                if (isDefaultPlatform(allPlatforms[i])) {  //NOI18N
                    defaultPlatform = allPlatforms[i];
                    break;
                }
            }
        }
        return this.defaultPlatform;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }


    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        firePropertyChange ();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        firePropertyChange ();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        firePropertyChange ();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        firePropertyChange ();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private void firePropertyChange () {
        pcs.firePropertyChange(PROP_INSTALLED_PLATFORMS, null, null);
    }

    private boolean isDefaultPlatform(final JavaPlatform platform) {
        return "default_platform".equals(platform.getProperties().get("platform.ant.name"));    //NOI18N
    }

    private JavaPlatform getDefaultPlatformByHint() {
        final FileObject storage = getStorage();
        if (storage != null) {
            for (final FileObject defFile : storage.getChildren()) {
                if (defFile.getAttribute(DEFAULT_PLATFORM_ATTR) == Boolean.TRUE) {
                    try {
                        DataObject dobj = DataObject.find(defFile);
                        //xxx: Using old good DO.getCookie as Lookup does not work.
                        final InstanceCookie ic = dobj.getCookie(InstanceCookie.class);
                        if (ic != null) {
                            final Object instance = ic.instanceCreate();
                            if (instance instanceof JavaPlatform && isDefaultPlatform((JavaPlatform)instance)) {
                                return (JavaPlatform) instance;
                            }
                        }
                    } catch (IOException e) {
                        //pass -> return null
                    } catch (ClassNotFoundException e) {
                        //pass -> return null
                    }
                    return null;
                }
            }
        }
        return null;
    }

    private synchronized FileObject getStorage() {
        if (storageCache == null) {
            storageCache = FileUtil.getConfigFile(PLATFORM_STORAGE);
            if (storageCache != null) {
                storageCache.addFileChangeListener (this);
                removePathListener();
            } else {
                final String[] path = PLATFORM_STORAGE.split("/");  //NOI18N
                FileObject lastExist = FileUtil.getConfigRoot();
                String expected = null;
                for (String pathElement : path) {
                    expected = pathElement;
                    FileObject current = lastExist.getFileObject(expected);
                    if (current == null) {
                        break;
                    }
                    lastExist = current;
                }
                assert lastExist != null;
                assert expected != null;
                removePathListener();
                final String expectedFin = expected;
                pathListener = new FileChangeAdapter(){
                    @Override
                    public void fileFolderCreated(FileEvent fe) {
                        if (expectedFin.equals(fe.getFile().getName())) {
                            firePropertyChange();
                        }
                    }
                };
                lastFound = lastExist;
                lastFound.addFileChangeListener(pathListener);
            }
        }
        return storageCache;
    }

    /**
     * Removes pathListener from lastFound FileObject
     * threading: caller has to own DefaultJavaPlatformProvider monitor
     */
    private void removePathListener() {
        if (pathListener != null) {
            assert lastFound != null;
            lastFound.removeFileChangeListener(pathListener);
            pathListener = null;
            lastFound = null;
        }
    }
}
