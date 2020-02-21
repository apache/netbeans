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
