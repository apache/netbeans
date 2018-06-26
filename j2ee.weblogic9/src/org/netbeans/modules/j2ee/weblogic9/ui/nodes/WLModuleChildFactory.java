/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
