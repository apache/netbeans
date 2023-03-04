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

package org.openide.actions;


import java.io.IOException;
import java.util.Set;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

/** Instantiate a template.
* Enabled only when there is one selected node and
* it represents a data object satisfying {@link DataObject#isTemplate}.
*
* @author   Jaroslav Tulach
*
* @deprecated Deprecated since 3.42. The use of this action should be avoided.
*/
@Deprecated
public class InstantiateAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1482795804240508824L;

    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes.length != 1) return false;
        DataObject obj = activatedNodes[0].getCookie(DataObject.class);
        return obj != null && obj.isTemplate ();
    }

    protected void performAction (Node[] activatedNodes) {
        DataObject obj = activatedNodes[0].getCookie(DataObject.class);
        if (obj != null && obj.isTemplate ()) {
            try {
                instantiateTemplate (obj);
            } catch (UserCancelException ex) {
                // canceled by user
                // do not notify the exception
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /* @return the name of the action
    */
    public String getName() {
        return NbBundle.getMessage(org.openide.loaders.DataObject.class, "Instantiate");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (InstantiateAction.class);
    }

    /** Instantiate a template object.
    * Asks user for the target file's folder and creates the file.
    * Then runs the node delegate's {@link org.openide.nodes.NodeOperation#customize customizer} (if there is one).
    * Also the node's {@link Node#getDefaultAction default action}, if any, is run.
    * @param obj the template to use
    * @return set of created objects or null if user canceled the action
    * @exception IOException on I/O error
    * @see DataObject#createFromTemplate
    */
    public static Set<DataObject> instantiateTemplate(DataObject obj)
    throws IOException {
        // Create component for for file name input
        return NewTemplateAction.getWizard (null).instantiate (obj);
    }
}
