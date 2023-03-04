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

package org.netbeans.modules.websvc.metro.model.ui;

import com.sun.xml.ws.runtime.config.MetroConfig;
import com.sun.xml.ws.runtime.config.ObjectFactory;
import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import com.sun.xml.ws.runtime.config.TubelineDefinition;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.metro.model.MetroConfigLoader;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class TubelinesCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String WEBSERVICES = "WebServices"; //NOI18N
    private static final String TUBELINES = "Tubelines"; //NOI18N

    private String name;

    private boolean override = true;
    
    private MetroConfig cfg = null;
    private MetroConfigLoader cfgLoader = new MetroConfigLoader();

    private TubesProjectConfigPanel panel = null;

    private ObjectFactory objFact = new ObjectFactory();
    private Project project = null;

    /** Creates a new instance of TubelinesCompositePanelProvider */
    public TubelinesCompositePanelProvider(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        project = context.lookup(Project.class);
        if (!cfgLoader.isMetroConfigSupported(project)) return null;
        
        ResourceBundle bundle = NbBundle.getBundle( TubelinesCompositePanelProvider.class );
        
        if (WEBSERVICES.equals(name)) {
            final ProjectCustomizer.Category toReturn = ProjectCustomizer.Category.create(WEBSERVICES,
                            bundle.getString("LBL_WebServices" ), null, // NOI18N
                                    ProjectCustomizer.Category.create(TUBELINES,
                                        bundle.getString("LBL_Tubelines" ), null, null));
            toReturn.setStoreListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if ((panel != null) && (panel.isChanged())) {
                        cfgLoader.saveMetroConfig(cfg, project);
                    }
                }
            });

            override = true;
            cfg = cfgLoader.loadMetroConfig(project);
            if ((cfg == null) || (cfg.getTubelines() == null)) {
                override = false;
                cfg = cfgLoader.loadDefaultMetroConfig(project);
                if (cfg == null) {
                    cfg = cfgLoader.createFreshMetroConfig();
                }
            } else {
                override = (cfgLoader.getDefaultTubeline(cfg) != null);
                MetroConfig defCfg = cfgLoader.loadDefaultMetroConfig(project);
                TubelineDefinition tDef = cfgLoader.getDefaultTubeline(defCfg);
                if (tDef != null) {
                    TubelineDefinition newDef = cfgLoader.createDefaultTubeline(cfg);
                    newDef.setClientSide(tDef.getClientSide());
                    newDef.setEndpointSide(tDef.getEndpointSide());
                }
            }

            return toReturn;
        }
        return null;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        
        if (TUBELINES.equals(nm)) {
            panel = new TubesProjectConfigPanel(project, cfg, override);
            category.setOkButtonListener(new OKActionListener(panel));
            return panel;
        }
        
        return new JPanel();
    }

    private class OKActionListener implements ActionListener {

        private TubesProjectConfigPanel panel;
        
        public OKActionListener(TubesProjectConfigPanel panel) {
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent e) {
            if ((panel != null) && (panel.isChanged())) {
                if (!panel.isOverride()) {
                    if (cfg != null) {
                        cfgLoader.removeDefaultTubeline(cfg);
                    }
                    return;
                } else {
                    if (cfg == null) cfg = cfgLoader.createFreshMetroConfig();
                    if (cfgLoader.getDefaultTubeline(cfg) == null) {
                        cfgLoader.createDefaultTubeline(cfg);
                    }
                }
                
                TubelineDefinition tDef = cfgLoader.getDefaultTubeline(cfg);
                TubeFactoryList tList = tDef.getClientSide();
                if (tList == null) {
                    tList = objFact.createTubeFactoryList();
                    tDef.setClientSide(tList);
                }
                List<TubeFactoryConfig> cList = tList.getTubeFactoryConfigs();
                cList.clear();
                cList.addAll(cfgLoader.createTubeFactoryConfigList(panel.getTubeList(true)));

                tList = tDef.getEndpointSide();
                if (tList == null) {
                    tList = objFact.createTubeFactoryList();
                    tDef.setEndpointSide(tList);
                }
                cList = tList.getTubeFactoryConfigs();
                cList.clear();
                cList.addAll(cfgLoader.createTubeFactoryConfigList(panel.getTubeList(false)));
            }

        }
    }

    public static TubelinesCompositePanelProvider createTubelines() {
        return new TubelinesCompositePanelProvider(WEBSERVICES);
    }

}
