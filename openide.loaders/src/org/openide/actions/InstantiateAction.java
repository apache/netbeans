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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
