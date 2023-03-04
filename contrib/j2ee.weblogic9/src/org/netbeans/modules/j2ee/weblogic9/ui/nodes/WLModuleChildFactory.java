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

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class WLModuleChildFactory
        extends org.openide.nodes.ChildFactory<WLModuleNode> implements RefreshModulesCookie {

    private static final Logger LOGGER = Logger.getLogger(WLModuleChildFactory.class.getName());

    private final Lookup lookup;

    private final ModuleType moduleType;

    public WLModuleChildFactory(Lookup lookup, ModuleType moduleType) {
        this.lookup = lookup;
        this.moduleType = moduleType;
    }

    public final void refresh() {
        refresh(false);
    }

    @Override
    protected Node createNodeForKey(WLModuleNode key) {
        return key;
    }

    @Override
    protected boolean createKeys(List<WLModuleNode> toPopulate) {
        WLDeploymentManager dm = lookup.lookup(WLDeploymentManager.class);
        try {
            TargetModuleID[] modules = dm.getAvailableModules(moduleType, dm.getTargets());
            TargetModuleID[] stopped = dm.getNonRunningModules(moduleType, dm.getTargets());
            Set<String> stoppedByName = new HashSet<String>();
            if (stopped != null) {
                for (TargetModuleID module : stopped) {
                    stoppedByName.add(module.getModuleID());
                }
            }

            if (modules != null) {
                Arrays.sort(modules, new Comparator<TargetModuleID>() {

                    @Override
                    public int compare(TargetModuleID o1, TargetModuleID o2) {
                        if (o1.getModuleID() == null) {
                            return o2.getModuleID() == null ? 0 : -1;
                        } else if (o2.getModuleID() == null){
                            return 1;
                        }
                        return o1.getModuleID().compareTo(o2.getModuleID());
                    }
                });
                Map<String, List<TargetModuleID>> byName = new HashMap<String, List<TargetModuleID>>();
                for (TargetModuleID module : modules) {
                    String name = module.getModuleID();
                    List<TargetModuleID> ids = byName.get(name);
                    if (ids == null) {
                        ids = new LinkedList<TargetModuleID>();
                        byName.put(name, ids);
                    }
                    ids.add(module);
                }
                for (Map.Entry<String, List<TargetModuleID>> e : byName.entrySet()) {
                    toPopulate.add(new WLModuleNode(e.getKey(), e.getValue(), lookup, moduleType,
                            stoppedByName.contains(e.getKey())));
                }
            }
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (TargetException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        // perhaps we should return false on exception, however it would most likely fail again
        return true;
    }
}
