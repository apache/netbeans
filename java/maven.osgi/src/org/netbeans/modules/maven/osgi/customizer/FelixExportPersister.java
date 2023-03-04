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

package org.netbeans.modules.maven.osgi.customizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginContainer;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.osgi.OSGiConstants;
import org.netbeans.modules.maven.spi.customizer.SelectedItemsTablePersister;

/**
 *
 * @author dafe
 */
public class FelixExportPersister implements SelectedItemsTablePersister {

    private final ModelHandle2 handle;
    private final Project project;
    private ModelOperation<POMModel> operation;
    private ModelOperation<POMModel> defaultOperation;
    
    private boolean isDefined = false;

    private SortedMap<String, Boolean> defaultValue;

    public FelixExportPersister (Project project, ModelHandle2 handle) {
        this.project = project;
        this.handle = handle;
        String[] exports = PluginPropertyUtils.getPluginPropertyList(project,
                OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN,
                OSGiConstants.PARAM_INSTRUCTIONS, OSGiConstants.EXPORT_PACKAGE,
                OSGiConstants.GOAL_MANIFEST);
        String exportInstruction = null;
        if (exports != null && exports.length == 1) {
            exportInstruction = exports[0];
            isDefined = true;
        } else {
            isDefined = false;
        }
        String[] privates = PluginPropertyUtils.getPluginPropertyList(project,
                OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN,
                OSGiConstants.PARAM_INSTRUCTIONS, OSGiConstants.PRIVATE_PACKAGE,
                OSGiConstants.GOAL_MANIFEST);
        String privateInstruction = null;
        if (privates != null && privates.length == 1) {
            privateInstruction = privates[0];
        }

        Map<Integer, String> instructions = new HashMap<Integer, String>(2);
        instructions.put(InstructionsConverter.EXPORT_PACKAGE, exportInstruction);
        instructions.put(InstructionsConverter.PRIVATE_PACKAGE, privateInstruction);

        defaultValue = InstructionsConverter.computeExportList(instructions, project);
    }
    
    public boolean isIsDefined() {
        return isDefined;
    }
    

    @Override
    public SortedMap<String, Boolean> read() {
        return defaultValue;
    }

    @Override
    public void write(SortedMap<String, Boolean> selItems) {
        if (operation != null) {
            handle.removePOMModification(operation);
        }
        
        final Map<Integer, String> exportIns = InstructionsConverter.computeExportInstructions(selItems, project);
        operation = new ModelOperation<POMModel>() {

            @Override
            public void performOperation(POMModel pomModel) {
        Build build = pomModel.getProject().getBuild();
        Plugin felixPlugin = null;
        if (build != null) {
            felixPlugin = build.findPluginById(OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN);
        } else {
            build = pomModel.getFactory().createBuild();
            pomModel.getProject().setBuild(build);
        }
        Configuration config = null;
        if (felixPlugin != null) {
            config = felixPlugin.getConfiguration();
        } else {
            felixPlugin = pomModel.getFactory().createPlugin();
            felixPlugin.setGroupId(OSGiConstants.GROUPID_FELIX);
            felixPlugin.setArtifactId(OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN);
            felixPlugin.setExtensions(Boolean.TRUE);
            build.addPlugin(felixPlugin);
        }
        if (config == null) {
            config = pomModel.getFactory().createConfiguration();
            felixPlugin.setConfiguration(config);
        }

        POMExtensibilityElement instructionsEl = null;
        List<POMExtensibilityElement> confEls = config.getConfigurationElements();
        for (POMExtensibilityElement el : confEls) {
            if (OSGiConstants.PARAM_INSTRUCTIONS.equals(el.getQName().getLocalPart())) {
                instructionsEl = el;
                break;
            }
        }
        if (instructionsEl == null) {
            instructionsEl = pomModel.getFactory().
                    createPOMExtensibilityElement(new QName(OSGiConstants.PARAM_INSTRUCTIONS));
            config.addExtensibilityElement(instructionsEl);
        }
        
        POMExtensibilityElement exportEl = ModelUtils.getOrCreateChild(instructionsEl, OSGiConstants.EXPORT_PACKAGE, pomModel);
        POMExtensibilityElement privateEl = ModelUtils.getOrCreateChild(instructionsEl, OSGiConstants.PRIVATE_PACKAGE, pomModel);

        exportEl.setElementText(exportIns.get(InstructionsConverter.EXPORT_PACKAGE));
        privateEl.setElementText(exportIns.get(InstructionsConverter.PRIVATE_PACKAGE));

    }
        };
        handle.addPOMModification(operation);
    }

    void setDefault(boolean def) {
        if (def) {
            if (operation != null) {
                handle.removePOMModification(operation);
            }
            if (defaultOperation == null) {
                defaultOperation = new ModelOperation<POMModel>() {
                    
                    @Override
                    public void performOperation(POMModel pomModel) {
                        Build build = pomModel.getProject().getBuild();
                        if (build != null) {
                            removeExportPrivate(findInstructions(build));
                            PluginManagement pm = build.getPluginManagement();
                            if (pm != null) {
                                removeExportPrivate(findInstructions(pm));
                            }
                        }
                       // we care about activated profiles?
                        List<String> profiles = handle.getActiveConfiguration().getActivatedProfiles();
                        if (profiles != null) {
                            for (String prof : profiles) {
                                Profile p = pomModel.getProject().findProfileById(prof);
                                if (p != null) {
                                    BuildBase bb = p.getBuildBase();
                                    if (bb != null) {
                                        removeExportPrivate(findInstructions(bb));
                                        PluginManagement pm = bb.getPluginManagement();
                                        if (pm != null) {
                                            removeExportPrivate(findInstructions(pm));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    private POMExtensibilityElement findInstructions(PluginContainer cont) {
                        Plugin felixPlugin = cont.findPluginById(OSGiConstants.GROUPID_FELIX, OSGiConstants.ARTIFACTID_BUNDLE_PLUGIN);
                        if (felixPlugin != null) {
                                Configuration config = felixPlugin.getConfiguration();
                                if (config != null) {
                                    List<POMExtensibilityElement> confEls = config.getConfigurationElements();
                                    for (POMExtensibilityElement el : confEls) {
                                        if (OSGiConstants.PARAM_INSTRUCTIONS.equals(el.getQName().getLocalPart())) {
                                            return el;
                                        }
                                    }
                                }
                        }
                        return null;
                    }                    

                    private void removeExportPrivate(POMExtensibilityElement instructionsEl) {
                        if (instructionsEl != null) {
                            for (POMExtensibilityElement el : instructionsEl.getAnyElements()) {
                                if (OSGiConstants.EXPORT_PACKAGE.equals(el.getQName().getLocalPart())) {
                                    instructionsEl.removeAnyElement(el);
                                }
                                if (OSGiConstants.PRIVATE_PACKAGE.equals(el.getQName().getLocalPart())) {
                                    instructionsEl.removeAnyElement(el);
                                }
                            }
                        }
                    }
                };
                
            }
            handle.addPOMModification(defaultOperation);
        } else {
            if (operation != null) {
                handle.addPOMModification(operation);
            }
            if (defaultOperation != null) {
                handle.removePOMModification(defaultOperation);
            }
        }
    }


}
