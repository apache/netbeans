/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerRootNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.PrioritizedCustomizerNode;
import org.netbeans.modules.cnd.makeproject.spi.DebuggerChooserProvider;
import org.openide.util.Lookup;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=DebuggerChooserProvider.class)
public class DebuggerChooserProviderImpl extends DebuggerChooserProvider {
    private static List<CustomizerNode> nodes;
    private static String[] names;
    private static int def;

    public DebuggerChooserProviderImpl() {
        Lookup lookup = Lookup.EMPTY;
        if (nodes == null) {
            nodes = CustomizerRootNodeProvider.getInstance().getCustomizerNodes("Debug", lookup); // NOI18N
            String[] defnames = new String[] { "" };

            if (nodes.size() >= 1) {
                int priority = PrioritizedCustomizerNode.DEFAULT_PRIORITY;
                int idx = 0;
                List<String> n = new ArrayList<>();
                for (CustomizerNode node : nodes) {
                    if (node instanceof PrioritizedCustomizerNode) {
                        if (((PrioritizedCustomizerNode) node).getPriority() > priority) {
                            priority = ((PrioritizedCustomizerNode) node).getPriority();
                            idx = n.size();
                        }
                    } else if (node.getClass().getName().toLowerCase(Locale.getDefault()).contains("dbx")) { // NOI18N
                        priority = PrioritizedCustomizerNode.MAX_PRIORITY;
                        idx = n.size();
                    }
                    n.add(node.getDisplayName());
                }
                names = n.toArray(defnames);
                def = idx;
            } else {
                names = defnames;
                def = 0;
            }
        }
    }

    @Override
    public String getName(int i) {
        if (i < names.length) {
            return names[i];
        } else {
            return "???"; // FIXUP // NOI18N
        }
    }

    @Override
    public ProjectActionHandlerFactory getNode(int i) {
        if (i < nodes.size()) {
            CustomizerNode node = nodes.get(i);
            if (node instanceof ProjectActionHandlerFactory) {
                return (ProjectActionHandlerFactory) node;
            }
        }
        return null;
    }

    @Override
    public String[] getNames() {
        return names;
    }

    @Override
    public int getDefault() {
        return def;
    }

    @Override
    public int getNodesSize() {
        return nodes.size();
    }
}
