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
package org.netbeans.modules.java.api.common.project.ui;

import java.awt.event.ActionEvent;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.nodes.FilterNode;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * Adjusts some display characteristics of source group root node in logical the view.
 * @author Tomas Zezula
 */
final class PackageViewFilterNode extends FilterNode {

    private final Project project;
    private final String customizerNodeName;
    private final String customizerPanelName;
    private final boolean trueSource;

    PackageViewFilterNode(
            @NonNull final SourceGroup sourceGroup,
            @NonNull final Project project,
            final boolean generated) {
        this(sourceGroup, project, "Sources", null, generated); //NOI18N
    }

    PackageViewFilterNode(
            @NonNull final SourceGroup sourceGroup,
            @NonNull final Project project,
            @NonNull final String customizerNodeName,
            @NullAllowed final String customizerPanelName,
            final boolean generated) {
        super(PackageView.createPackageView(sourceGroup));
        this.project = project;
        this.customizerNodeName = customizerNodeName;
        this.customizerPanelName = customizerPanelName;
        this.trueSource = !generated;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>();
        Collections.addAll(actions, super.getActions(context));
        if (trueSource) {
            actions.add(null);
            actions.add(new PreselectPropertiesAction(project, customizerNodeName, customizerPanelName));
        } else {
            // Just take out "New File..." as this would be misleading.
            Iterator<Action> scan = actions.iterator();
            while (scan.hasNext()) {
                Action a = scan.next();
                if (a != null && a.getClass().getName().equals("org.netbeans.modules.project.ui.actions.NewFile$WithSubMenu")) { // NOI18N
                    scan.remove();
                }
            }
        }
        return actions.toArray(new Action[0]);
    }

    @Override
    public String getHtmlDisplayName() {
        if (trueSource) {
            return super.getHtmlDisplayName();
        }
        String htmlName = getOriginal().getHtmlDisplayName();
        if (htmlName == null) {
            try {
                htmlName = XMLUtil.toElementContent(super.getDisplayName());
            } catch (CharConversionException x) {
                return null; // never mind
            }
        }
        return "<font color='!controlShadow'>" + htmlName + "</font>"; // NOI18N
    }
}
