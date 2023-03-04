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
