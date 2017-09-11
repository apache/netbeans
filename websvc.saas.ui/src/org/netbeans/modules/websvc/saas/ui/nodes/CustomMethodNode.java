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
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.util.SaasTransferable;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class CustomMethodNode extends AbstractNode {
    
    private CustomSaasMethod method;
    private Transferable transferable;
    
    public CustomMethodNode(CustomSaasMethod method) {
        this(method, new InstanceContent());
    }

    public CustomMethodNode(CustomSaasMethod method, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        this.method = method;
        content.add(method);
        transferable = ExTransferable.create(
            new SaasTransferable<SaasMethod>(method, SaasTransferable.CUSTOM_METHOD_FLAVORS));
    }

    @Override
    public String getDisplayName() {
        return method.getName();
    }
    
    @Override
    public String getShortDescription() {
        return method.getDocumentation();
    }
    
    private static final java.awt.Image ICON =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/method.png" ); //NOI18N
    
    @Override
    public java.awt.Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        //TODO maybe ???
        //actions.add(SystemAction.get(TestMethodAction.class));
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        if (method.getSaas().getState() != Saas.State.READY) {
            method.getSaas().toStateReady(false);
            return super.clipboardCopy();
        }
        return SaasTransferable.addFlavors(transferable);
    }
    
}
