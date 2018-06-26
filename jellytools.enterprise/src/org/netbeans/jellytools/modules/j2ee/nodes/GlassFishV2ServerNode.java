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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.jellytools.modules.j2ee.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author shura
 */
public class GlassFishV2ServerNode extends J2eeServerNode {

    public GlassFishV2ServerNode() {
        super(Bundle.getString("org.netbeans.modules.j2ee.sun.ide.j2ee.Bundle",
                "LBL_GLASSFISH_V2"));
    }
    
    public static GlassFishV2ServerNode invoke() {
        RuntimeTabOperator.invoke();
        return new GlassFishV2ServerNode();
    }
    
    public static GlassFishV2ServerNode checkServerShown() {
        JTreeOperator tree = new RuntimeTabOperator().getRootNode().tree();
        long oldValue = tree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 1);
        GlassFishV2ServerNode result = null;
        try {
            result = GlassFishV2ServerNode.invoke();
        } catch(TimeoutExpiredException e) {
        } finally {
            tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldValue);
        }
        return result;
    }
    
    
    /**
     * Adds GlassFish V2 using path from com.sun.aas.installRoot property
     */
    public static GlassFishV2ServerNode getGlassFishV2Node(String appServerPath) {
        /*
        GlassFishV2ServerNode result = checkServerShown();
        
        if(result != null) {
            return result;
        }
         */
        
        if (appServerPath == null) {
            throw new Error("Can't add application server. com.sun.aas.installRoot property is not set.");
        }

        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"
        String glassFishV2ListItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.Bundle", "LBL_GlassFishV2");
        String nextButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
        String finishButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();        
        JTreeOperator runtimeTree = rto.tree();
        
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 6000);
        
        TreePath path = runtimeTree.findPath("Servers");
        runtimeTree.selectPath(path);
        
        try {
            //log("Let's check whether GlassFish V2 is already added");
            runtimeTree.findPath("Servers|GlassFish V2");
        } catch (TimeoutExpiredException tee) {
            //log("There is no GlassFish V2 node so we'll add it");
            
            new JPopupMenuOperator(runtimeTree.callPopupOnPath(path)).pushMenuNoBlock(addServerMenuItem);

            NbDialogOperator addServerInstanceDialog = new NbDialogOperator(addServerInstanceDialogTitle);

            new JListOperator(addServerInstanceDialog, 1).selectItem(glassFishV2ListItem);

            new JButtonOperator(addServerInstanceDialog,nextButtonCaption).push();

            new JTextFieldOperator(addServerInstanceDialog).enterText(appServerPath);

            new JButtonOperator(addServerInstanceDialog,finishButtonCaption).push();
        }
        
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);
        
        return GlassFishV2ServerNode.invoke();
    }

    
}
