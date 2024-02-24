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

package org.netbeans.modules.apisupport.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.ErrorManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.util.ChangeSupport;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;

// XXX this class is more or less copy-pasted from j2seproject.
// Get rid of it as soon as "some" Libraries Node API is provided.

/**
 * PlatformNode represents Java platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * It displays the content of boot classpath.
 * @see JavaPlatform
 * @author Tomas Zezula
 */
final class PlatformNode extends AbstractNode implements ChangeListener {
    
    private static final String PLATFORM_ICON = "org/netbeans/modules/apisupport/project/ui/resources/platform.gif"; //NOI18N
    private static final String ARCHIVE_ICON = "org/netbeans/modules/apisupport/project/ui/resources/jar.gif"; //NOI18N
    
    private final PlatformProvider pp;
    private static final RequestProcessor RP = new RequestProcessor(PlatformNode.class);
    
    private PlatformNode(Project project, PlatformProvider pp) {
        super(new PlatformContentChildren(), Lookups.fixed(new JavadocProvider(pp), project));
        this.pp = pp;
        this.pp.addChangeListener(this);
        setIconBaseWithExtension(PLATFORM_ICON);
    }
    
    @Override
    public String getName() {
        return this.getDisplayName();
    }
    
    @Override
    public String getDisplayName() {
        JavaPlatform plat = pp.getPlatform();
        String name;
        if (plat != null) {
            name = plat.getDisplayName();
        } else {
            String platformId = pp.getPlatformID();
            if (platformId == null) {
                name = NbBundle.getMessage(PlatformNode.class,"TXT_BrokenPlatform");
            } else {
                name = MessageFormat.format(NbBundle.getMessage(PlatformNode.class,"FMT_BrokenPlatform"), new Object[] {platformId});
            }
        }
        return name;
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (pp.getPlatform() == null) {
            String displayName = this.getDisplayName();
            try {
                displayName = XMLUtil.toElementContent(displayName);
            } catch (CharConversionException ex) {
                // OK, no annotation in this case
                return null;
            }
            return "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
        } else {
            return null;
        }
    }
    
    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(ShowJavadocAction.class)
        };
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        //The caller holds ProjectManager.mutex() read lock
        RP.post(new Runnable() {
            @Override
            public void run() {
                PlatformNode.this.fireNameChange(null, null);
                PlatformNode.this.fireDisplayNameChange(null, null);
                ((PlatformContentChildren) getChildren()).addNotify();
            }
        });
    }
    
    /**
     * Creates new PlatformNode
     * @param eval the PropertyEvaluator used for obtaining the active platform name
     * and listening on the active platform change
     * @param platformPropName the name of ant property holding the platform name
     */
    static PlatformNode create(Project project, PropertyEvaluator eval, String platformPropName) {
        PlatformProvider pp = new PlatformProvider(eval, platformPropName);
        return new PlatformNode(project, pp);
    }
    
    private static class PlatformContentChildren extends Children.Keys<SourceGroup> {
        
        PlatformContentChildren() {
        }
        
        @Override
        protected void addNotify() {
            this.setKeys(this.getKeys());
        }
        
        @Override
        protected void removeNotify() {
            this.setKeys(Collections.<SourceGroup>emptySet());
        }
        
        @Override
        protected Node[] createNodes(SourceGroup sg) {
            return new Node[] { ActionFilterNode.create(PackageView.createPackageView(sg)) };
        }
        
        private List<SourceGroup> getKeys() {
            JavaPlatform platform = ((PlatformNode)this.getNode()).pp.getPlatform();
            if (platform == null) {
                return Collections.emptyList();
            }
            //Todo: Should listen on returned classpath, but now the bootstrap libraries are read only
            FileObject[] roots = platform.getBootstrapLibraries().getRoots();
            List<SourceGroup> result = new ArrayList<SourceGroup>(roots.length);
            for (int i=0; i<roots.length; i++) {
                    FileObject file;
                    Icon icon;
                    if ("jar".equals(roots[i].toURL().getProtocol())) { //NOI18N
                        file = FileUtil.getArchiveFile(roots[i]);
                        icon = ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
                    } else {
                        file = roots[i];
                        icon = null;
                    }
                    if (file.isValid()) {
                        result.add(new LibrariesSourceGroup(roots[i], file.getNameExt(), icon, icon));
                    }
            }
            return result;
        }
    }
    
    private static class PlatformProvider implements PropertyChangeListener {
        
        private final PropertyEvaluator evaluator;
        private final String platformPropName;
        private JavaPlatform platformCache;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        
        public PlatformProvider(PropertyEvaluator evaluator, String platformPropName) {
            this.evaluator = evaluator;
            this.platformPropName = platformPropName;
            this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this,evaluator));
        }
        
        public String getPlatformID() {
            return this.evaluator.getProperty(this.platformPropName);
        }
        
        public JavaPlatform getPlatform() {
            if (platformCache == null) {
                final String platformPropName = getPlatformID();
                platformCache = ModuleProperties.findJavaPlatformByLocation(platformPropName);
                if (platformCache != null && platformCache.getInstallFolders().size() == 0) {
                    //Deleted platform
                    platformCache = null;
                }
                //Issue: #57840: Broken platform 'default_platform'
                if (ErrorManager.getDefault().isLoggable(ErrorManager.INFORMATIONAL) && platformCache == null) {
                    StringBuilder message = new StringBuilder("RequestedPlatform: " + platformPropName + " not found.\nInstalled Platforms:\n"); // NOI18N
                    JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
                    for (int i=0; i<platforms.length; i++) {
                        message.append("Name: "+platforms[i].getProperties().get("platform.ant.name")+ // NOI18N
                                " Broken: "+ (platforms[i].getInstallFolders().size() == 0) + "\n");  // NOI18N
                    }
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, message.toString());
                }
            }
            return platformCache;
        }
        
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (platformPropName.equals(evt.getPropertyName())) {
                platformCache = null;
                this.changeSupport.fireChange();
            }
        }
        
    }
    
    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {
        
        PlatformProvider platformProvider;
        
        private JavadocProvider(PlatformProvider platformProvider) {
            this.platformProvider = platformProvider;
        }
        
        @Override
        public boolean hasJavadoc() {
            JavaPlatform platform = platformProvider.getPlatform();
            if (platform == null) {
                return false;
            }
            URL[] javadocRoots = getJavadocRoots(platform);
            return javadocRoots.length > 0;
        }
        
        @Override
        public void showJavadoc() {
            JavaPlatform platform = platformProvider.getPlatform();
            if (platform != null) {
                URL[] javadocRoots = getJavadocRoots(platform);
                URL pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",javadocRoots);
                if (pageURL == null) {
                    pageURL = ShowJavadocAction.findJavadoc("index.html",javadocRoots);
                }
                ShowJavadocAction.showJavaDoc(pageURL, platform.getDisplayName());
            }
        }
        
        private static URL[]  getJavadocRoots(JavaPlatform platform) {
            Set<URL> result = new HashSet<URL>();
            List<ClassPath.Entry> l = platform.getBootstrapLibraries().entries();
            for (Iterator<ClassPath.Entry> it = l.iterator(); it.hasNext();) {
                ClassPath.Entry e = it.next();
                result.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc(e.getURL()).getRoots()));
            }
            return result.toArray(new URL[0]);
        }
        
    }
    
}


