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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.saas.ui.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.CustomSaas;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

public class SaasGroupNodeChildren extends Children.Keys<Object> implements PropertyChangeListener {
    private SaasGroup group;

    public SaasGroupNodeChildren(SaasGroup group) {
        this.group = group;
        SaasServicesModel model = SaasServicesModel.getInstance();
        model.addPropertyChangeListener(WeakListeners.propertyChange(this, model));
    }

    protected void setGroup(SaasGroup g) {
        group = g;
    }

    @Override
    protected void addNotify() {
        updateKeys();
        super.addNotify();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == group) {
            updateKeys();
            if (evt.getNewValue() != null) {
                refreshKey(evt.getNewValue());
            } else if (evt.getOldValue() != null) {
                refreshKey(evt.getOldValue());
            }
        }
    }

    protected void updateKeys() {
        ArrayList<Object> keys = new ArrayList<Object>();
        List<SaasGroup> groups = group.getChildrenGroups();
        Collections.sort(groups);
        keys.addAll(groups);
        
        List<Saas> services = group.getServices();
        Collections.sort(services);
        keys.addAll(services);
        
        setKeys(keys);
    }

    @Override
    protected void removeNotify() {
        java.util.List<String> emptyList = Collections.emptyList();
        setKeys(emptyList);
        super.removeNotify();
    }

    @Override
    protected Node[] createNodes(Object key) {
        if (key instanceof SaasGroup) {
            SaasGroup g = (SaasGroup) key;
            SaasGroupNode node = new SaasGroupNode(g);
            return new Node[]{node};
        } else if (key instanceof WadlSaas) {
            return new Node[]{new WadlSaasNode((WadlSaas) key)};
        } else if (key instanceof WsdlSaas) {
            return new Node[]{new WsdlSaasNode((WsdlSaas) key)};
        } else if (key instanceof CustomSaas) {
            return new Node[]{new CustomSaasNode((CustomSaas) key)};
        }
        return new Node[0];
    }
}
