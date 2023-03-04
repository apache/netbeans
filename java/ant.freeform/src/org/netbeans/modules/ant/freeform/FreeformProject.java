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

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.ant.freeform.ui.ProjectCustomizerProvider;
import org.netbeans.modules.ant.freeform.ui.View;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * One freeform project.
 * @author Jesse Glick
 */
@AntBasedProjectRegistration(
    type=FreeformProjectType.TYPE,
    iconResource="org/netbeans/modules/ant/freeform/resources/freeform-project.png",
    sharedName=FreeformProjectType.NAME_SHARED,
    privateName=FreeformProjectType.NAME_SHARED,
    sharedNamespace=FreeformProjectType.NS_GENERAL_1,
    privateNamespace=FreeformProjectType.NS_GENERAL_PRIVATE
)
public final class FreeformProject implements Project {
    
    public static final Lookup.Result<ProjectNature> PROJECT_NATURES = Lookup.getDefault().lookupResult(ProjectNature.class);
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private AuxiliaryConfiguration aux;
    
    public FreeformProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = new FreeformEvaluator(this);
        lookup = initLookup();
        Logger.getLogger(FreeformProject.class.getName()).log(Level.FINER, "Initializing project in {0} with {1}", new Object[] {helper, lookup});
    }
    
    public AntProjectHelper helper() {
        return helper;
    }

    /**
     * @see Util#getPrimaryConfigurationData
     */
    public Element getPrimaryConfigurationData() {
        return Util.getPrimaryConfigurationData(helper);
    }

    /**
     * @see Util#putPrimaryConfigurationData
     */
    public void putPrimaryConfigurationData(Element data) {
        Util.putPrimaryConfigurationData(helper, data);
    }

    private Lookup initLookup() throws IOException {
        aux = helper().createAuxiliaryConfiguration(); // AuxiliaryConfiguration
        FreeformFileEncodingQueryImpl FEQImpl = new FreeformFileEncodingQueryImpl(helper(), evaluator());
        helper().addAntProjectListener(FEQImpl);
        Lookup baseLookup = Lookups.fixed(
            this,
            new Info(), // ProjectInformation
            new FreeformSources(this), // Sources
            new Actions(this), // ActionProvider
            new View(this), // LogicalViewProvider
            new ProjectCustomizerProvider(this), // CustomizerProvider
            aux, // AuxiliaryConfiguration
            helper().createAuxiliaryProperties(),
            helper().createCacheDirectoryProvider(), // CacheDirectoryProvider
            new Subprojects(this), // SubprojectProvider
            new ArtifactProvider(this), // AntArtifactProvider
            new LookupMergerImpl(), // LookupMerger or ActionProvider
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            new FreeformProjectOperations(this),
	    new FreeformSharabilityQuery(this), //SharabilityQueryImplementation
            Accessor.DEFAULT.createProjectAccessor(this), //Access to AntProjectHelper and PropertyEvaluator
            FEQImpl, // FileEncodingQueryImplementation
            new FreeformTemplateAttributesProvider(helper(), eval, FEQImpl)
        );
        return LookupProviderSupport.createCompositeLookup(baseLookup, "Projects/org-netbeans-modules-ant-freeform/Lookup"); //NOI18N
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }

    @Override
    public String toString() {
        return "FreeformProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = getPrimaryConfigurationData();
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(FreeformProjectType.NS_GENERAL, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(FreeformProjectType.NS_GENERAL, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                putPrimaryConfigurationData(data);
                return null;
            }
        });
    }

    //Todo: replace with api.java.common #110886
    private final class Info implements ProjectInformation, AntProjectListener {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private volatile String nameCache;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public Info() {
            helper.addAntProjectListener(this);
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        public String getDisplayName() {
            String res = nameCache;
            if (res != null) {
                return res;
            }
            return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
                public String run() {
                    Element genldata = getPrimaryConfigurationData();
                    Element nameEl = XMLUtil.findElement(genldata, "name", FreeformProjectType.NS_GENERAL); // NOI18N
                    final String name = nameEl == null ? "???" : XMLUtil.findText(nameEl); //NOI18N  Corrupt. Cf. #48267 (cause unknown).
                    nameCache = name;
                    return name;
                }
            });
        }
        
        public Icon getIcon() {
            if (usesAntScripting()) {
                return ImageUtilities.loadImageIcon("org/netbeans/modules/ant/freeform/resources/freeform-project.png", true); // NOI18N
            } else {
                return ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/projectTab.png", true); // NOI18N
            }
        }
        
        public Project getProject() {
            return FreeformProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }


        public @Override void configurationXmlChanged(AntProjectEvent ev) {
            nameCache = null;
            pcs.firePropertyChange(null, null, null);
        }

        public @Override void propertiesChanged(AntProjectEvent ev) {}
        
    }
    
    /**
     * Utility method to decide if the project actually uses Ant scripting.
     * It does if at least one of these hold:
     * <ol>
     * <li>There is a <code>build.xml</code> at top level.
     * <li>The property <code>ant.script</code> is defined.
     * <li>There are any <code>&lt;action&gt;</code>s bound.
     * </ol>
     */
    public boolean usesAntScripting() {
        return getProjectDirectory().getFileObject("build.xml") != null || // NOI18N
                evaluator().getProperty("ant.script") != null || // NOI18N
                Util.getPrimaryConfigurationData(helper).getElementsByTagName("action").getLength() > 0; // NOI18N
    }

}
