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
package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectEvent;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectListener;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles source dir list for a freeform project.
 * XXX will not correctly unregister released external source roots
 */
public final class MakeSources implements Sources, MakeProjectListener {

    public static final String GENERIC = "generic"; // NOI18N
    private final MakeProject project;
    private final MakeProjectHelper helper;

    public MakeSources(MakeProject project, MakeProjectHelper helper) {
        this.project = project;        
        this.helper = helper;
        helper.addMakeProjectListener(MakeSources.this);
        changeSupport = new ChangeSupport(this);
    }
    private Sources delegate;
    private final ChangeSupport changeSupport;
    private long eventID = 0;

    @Override
    public SourceGroup[] getSourceGroups(String str) {
        if (!str.equals(GENERIC)) { // NOI18N
            return new SourceGroup[0];
        }
        Sources srcs;
        long curEvent;
        synchronized (this) {
            srcs = delegate;
            curEvent = ++eventID;
        }

        AtomicBoolean completeSouces = new AtomicBoolean(true);
        if (srcs == null) {
            srcs = initSources(completeSouces);
        }

        synchronized (this) {
            if (curEvent == eventID && completeSouces.get()) {
                delegate = srcs;
            }
        }
        SourceGroup[] sg = srcs.getSourceGroups(str);
        return sg;
    }

    private List<String> getSourceRootsFromProjectXML() {
        Element data = helper.getPrimaryConfigurationData(true);
        if (data.getElementsByTagName(MakeProjectTypeImpl.SOURCE_ROOT_LIST_ELEMENT).getLength() > 0) {
            List<String> list = new ArrayList<>();
            NodeList nl4 = data.getElementsByTagName(MakeProjectTypeImpl.SOURCE_ROOT_ELEMENT);
            if (nl4.getLength() > 0) {
                for (int i = 0; i < nl4.getLength(); i++) {
                    Node node = nl4.item(i);
                    NodeList nl2 = node.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        String typeTxt = nl2.item(j).getNodeValue();
                        String sRoot = typeTxt;
                        list.add(sRoot);
                    }
                }
            }
            return list;
        }
        return null;
    }

    private List<String> getAbsoluteSourceRootsFromProjectXML() {
        RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
        List<String> sourceRoots = getSourceRootsFromProjectXML();
        List<String> absSourceRoots = null;
        if (sourceRoots != null) {
            absSourceRoots = new ArrayList<>();
            for (String sRoot : sourceRoots) {
                absSourceRoots.add(sRoot);
            }
        }
        return absSourceRoots;
    }

    private Sources initSources(AtomicBoolean completeSouces) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        Set<String> sourceRootList = null;//new LinkedHashSet<String>();        

        // Try project.xml first if project not already read (this is cheap)
        if (!pdp.gotDescriptor()) {
            completeSouces.set(false);
            List<String> absSourceRoots = getAbsoluteSourceRootsFromProjectXML();
            if (absSourceRoots != null) {
                // mark source info as valid (got it from project.xml directly)
                completeSouces.set(true);
                sourceRootList = new LinkedHashSet<>();
                sourceRootList.addAll(absSourceRoots);
            }
        }
        if (sourceRootList == null) {
            sourceRootList = new LinkedHashSet<>();
            MakeConfigurationDescriptor pd = pdp.getConfigurationDescriptor();
            if (pd != null) {
                // mark source info as valid
                completeSouces.set(true);
                // Add external folders to sources.
                if (pd.getVersion() < 41) {
                    Item[] projectItems = pd.getProjectItems();
                    if (projectItems != null) {
                        for (int i = 0; i < projectItems.length; i++) {
                            Item item = projectItems[i];
                            String name = item.getPath();
                            if (!CndPathUtilities.isPathAbsolute(name)) {
                                continue;
                            }
                            File file = new File(name);
                            if (!file.exists()) {
                                continue;
                            }
                            if (!file.isDirectory()) {
                                file = file.getParentFile();
                            }
                            name = file.getPath();
                            sourceRootList.add(name);
                            pd.addSourceRootRaw(CndPathUtilities.toRelativePath(pd.getBaseDir(), name));
                        }
                    }
                }
                // Add source roots to set (>= V41)
                List<String> list = pd.getAbsoluteSourceRoots();
                for (String sr : list) {
                    sourceRootList.add(sr);
                }

                // Add buildfolder from makefile projects to sources. See IZ 90190.
                if (pd.getVersion() < 41) {
                    Configuration[] confs = pd.getConfs().toArray();
                    for (int i = 0; i < confs.length; i++) {
                        MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
                        if (makeConfiguration.isMakefileConfiguration()) {
                            MakefileConfiguration makefileConfiguration = makeConfiguration.getMakefileConfiguration();
                            String path = makefileConfiguration.getAbsBuildCommandWorkingDir();
                            sourceRootList.add(path);
                            pd.addSourceRootRaw(CndPathUtilities.toRelativePath(pd.getBaseDir(), path));
                        }
                    }
                }

            } else {
                completeSouces.set(false);
            }
        }
        ExecutionEnvironment fsEnv = project.getFileSystemHost();                
        FileObjectBasedSources sources = new FileObjectBasedSources();
        Set<FileObject> added = new HashSet<>();
        sourceRootList.add(project.getProjectDirectory().getPath()); // add remote project itself to the tail
        for (String name : sourceRootList) {
            String path = CndPathUtilities.toAbsolutePath(project.getProjectDirectory(), name);
            path = RemoteFileUtil.normalizeAbsolutePath(path, fsEnv);
            String displayName = (fsEnv.isLocal() ? "" : fsEnv.getDisplayName() + ":") + path; //NOI18N
            FileObject fo = RemoteFileUtil.getFileObject(path, fsEnv);
            if (!added.contains(fo)) {
                if (fo == null) {
                    new NullPointerException("Null file object for " + fsEnv + ':' + path).printStackTrace(); //NOI18N
                } else {
                    sources.addGroup(project, GENERIC, fo, displayName);
                    added.add(fo);
                }
            }
        }
        return sources;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private void fireChange() {
        synchronized(this) {
            delegate = null;
        }
        changeSupport.fireChange();
    }

    @Override
    public void configurationXmlChanged(MakeProjectEvent ev) {
        // fireChange(); // ignore - cnd projects don't keep source file info in project.xml
    }

    @Override
    public void propertiesChanged(MakeProjectEvent ev) {
        // ignore
    }

    public void sourceRootsChanged() {
        fireChange();
    }
}
