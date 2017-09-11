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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.Saas.State;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class WadlSaasNodeChildren extends SaasNodeChildren<Object> {
    
    public WadlSaasNodeChildren(WadlSaas wadlSaas) {
        super(wadlSaas);
    }
    
    @Override
    public WadlSaas getSaas() {
        return (WadlSaas) super.getSaas();
    }
    
    @Override
    protected void updateKeys() {
        State state = getSaas().getState();
        if (state == Saas.State.READY) {
            ArrayList<Object> keys = new ArrayList<Object>();
            List<WadlSaasResource> resources = getSaas().getResources();
            Collections.sort(resources);
            keys.addAll(resources);
            
            List<SaasMethod> methods = getSaas().getMethods();
            Collections.sort(methods);
            keys.addAll(methods);
            
            setKeys(keys);
        } else if (state == Saas.State.INITIALIZING) {
            setKeys(WAIT_HOLDER);
        } else {
            setKeys(Collections.EMPTY_LIST);
        }
    }
    
    @Override
    protected Node[] createNodes(Object key) {
        if (key == WAIT_HOLDER[0]) {
            return getWaitNode();
        }
        try {
            if (key instanceof WadlSaasMethod) {
                WadlSaasMethod wsm = (WadlSaasMethod) key;
                return new Node[] { new WadlMethodNode(wsm) };
            } else if (key instanceof WadlSaasResource) {
                return new Node[] { new ResourceNode((WadlSaasResource)key) };
            }
        } catch(Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return new Node[0];
    }

}
