/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
        return actions.toArray(new Action[actions.size()]);
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
