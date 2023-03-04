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
package org.netbeans.modules.analysis.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.analysis.DescriptionReader;
import org.netbeans.modules.analysis.SPIAccessor;
import org.netbeans.modules.analysis.Utils;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class RequiredPluginsNode extends AbstractNode {

    private final Collection<? extends MissingPlugin> requiredPlugins;
    
    @Messages({"DN_MissingPlugins=Required Plugins Missing",
               "#{0}: the list of missing plugins",
               "DESC_MissingPlugins=<html>The following plugins that are required to fully analyze the selected scope were missing:<ul>{0}</ul>"})
    public RequiredPluginsNode(final Collection<? extends MissingPlugin> requiredPlugins) {
        super(Children.LEAF, Lookups.fixed(new DescriptionReader() {
            @Override public CharSequence getDescription() {
                StringBuilder missingPlugins = new StringBuilder();
                
                for (MissingPlugin p : requiredPlugins) {
                    missingPlugins.append("<li>").append(SPIAccessor.ACCESSOR.getDisplayName(p)).append("</li>");
                }
                
                return Bundle.DESC_MissingPlugins(missingPlugins.toString());
            }
        }));
        this.requiredPlugins = new ArrayList<MissingPlugin>(requiredPlugins);
        setDisplayName(Bundle.DN_MissingPlugins());
        setIconBaseWithExtension("org/netbeans/modules/analysis/ui/resources/warning.gif");
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new ActionImpl(requiredPlugins)
        };
    }
    
    private static final class ActionImpl extends AbstractAction {

        private Collection<? extends MissingPlugin> requiredPlugins;
        @Messages("LBL_InstallPlugins=Install Missing Plugins...")
        public ActionImpl(Collection<? extends MissingPlugin> requiredPlugins) {
            this.putValue(NAME, Bundle.LBL_InstallPlugins());
            this.requiredPlugins = requiredPlugins;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Utils.installMissingPlugins(requiredPlugins);
        }
        
    }
    
}
