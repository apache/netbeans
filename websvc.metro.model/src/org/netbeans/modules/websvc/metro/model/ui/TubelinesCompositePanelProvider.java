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
