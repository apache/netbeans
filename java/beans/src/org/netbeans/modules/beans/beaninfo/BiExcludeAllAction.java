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

package org.netbeans.modules.beans.beaninfo;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
* Toggles selection.
*
* @author   Petr Hrebejk
*/
public class BiExcludeAllAction extends NodeAction  {

    static final long serialVersionUID =300378897958545485L;
    /** generated Serialized Version UID */
    //static final long serialVersionUID = 1391479985940417455L;

    //private static final Class[] cookieClasses = new Class[] { BiFeatureNode.class };



    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle (GenerateBeanInfoAction.class).getString ("CTL_EXCLALL_MenuItem");
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return null;
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (BiToggleAction.class);
    }

    protected boolean enable( Node[] activatedNodes ) {
        activatedNodes = BiPanel.getSelectedNodes();

        if ( activatedNodes.length < 1 )
            return false;

        for(int i = 0; i < activatedNodes.length; i++ ) {
            BiNode.SubNode bis = activatedNodes[i].getCookie( BiNode.SubNode.class );
            if ( bis == null )
                return false;
            Node[] nodes = bis.getChildren().getNodes();
            if ( nodes == null || nodes.length == 0 )
                return false;
            BiFeature biFeature = ((BiFeatureNode)nodes[0]).getBiFeature();
            BiAnalyser biAnalyser = ((BiFeatureNode)nodes[0]).getBiAnalyser();
            if( ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty ) && biAnalyser.isNullProperties() )
                return false;
            if( biFeature instanceof BiFeature.EventSet && biAnalyser.isNullEventSets() )
                return false;
            if( biFeature instanceof BiFeature.Method && biAnalyser.isNullMethods() )
                return false;
        }
            
        return true;
    }


    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( Node[] nodes ) {

        nodes = BiPanel.getSelectedNodes();

        if ( nodes.length < 1 )
            return;

        for(int i = 0; i < nodes.length; i++ ) {
            if( nodes[i].getCookie( BiNode.SubNode.class ) != null )
                nodes[i].getCookie( BiNode.SubNode.class ).includeAll( false );
        }

    }

}
