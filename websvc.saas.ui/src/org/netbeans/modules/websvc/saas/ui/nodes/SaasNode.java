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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.websvc.saas.ui.actions.DeleteServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.RefreshServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.ViewApiDocAction;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;

/**
 *
 * @author nam
 */
public abstract class SaasNode extends AbstractNode {
    protected Saas saas;

    public SaasNode(SaasNodeChildren nodeChildren, AbstractLookup lookup, Saas saas) {
        super(nodeChildren, lookup);
        this.saas = saas;
    }

    public Saas getSaas() {
        return saas;
    }

    @Override
    public String getDisplayName() {
        return saas.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return saas.getDescription();
    }

    @Override
    public Image getIcon(int type) {
        return getGenericIcon(type);
    }

    protected abstract Image getGenericIcon(int type);

    public static List<Action> getActions(Lookup lookup) {
        List<Action> actions = new ArrayList<Action>();
        for (SaasNodeActionsProvider ext : SaasUtil.getSaasNodeActionsProviders()) {
            actions.addAll(Arrays.asList(ext.getSaasActions(lookup)));
        }
        return actions;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = getActions(getLookup());
        actions.add(SystemAction.get(ViewApiDocAction.class));
        actions.add(SystemAction.get(DeleteServiceAction.class));
        actions.add(SystemAction.get(RefreshServiceAction.class));

        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public void destroy() throws IOException {
        SaasServicesModel.getInstance().removeService(getSaas());
        super.destroy();
    }

    @Override
    public boolean canDestroy() {
        return getSaas().isUserDefined();
    }
}
