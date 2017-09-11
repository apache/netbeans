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


package org.netbeans.modules.i18n;

import java.util.concurrent.Future;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;


/**
 * Internationalize action. Runs "i18n session" over specified source. Finds
 * non-i19n-ized hard coded strings and offers them i18n-ize to user in step-by-step
 * manner.
 *
 * @author   Petr Jiricka
 * @see I18nManager
 */
public class I18nAction extends NodeAction {

    /** Generated sreial version UID. */
    static final long serialVersionUID = 3322896507302889271L;

    public I18nAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }    
    
    /** 
     * Actually performs the action. Implements superclass abstract method.
     * @param activatedNodes Currently activated nodes.
     */
    protected void performAction(final Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }

        final Node node = activatedNodes[0];
        DataObject dataObject = node.getCookie(DataObject.class);
        if (dataObject == null) {
            return;
        }

        if (FileOwnerQuery.getOwner(dataObject.getPrimaryFile()) == null) {
            return;
        }

        EditorCookie editorCookie = node.getCookie(EditorCookie.class);
        if (editorCookie == null) {
            editorCookie = dataObject.getCookie(EditorCookie.class);
            if (editorCookie == null) {
                return;
            }
        }

        editorCookie.open(); 
        I18nManager.getDefault().internationalize(dataObject);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /** Overrides superclass method. Adds additional test if i18n module has registered factory
     * for this data object to be able to perform i18n action. */
    protected boolean enable(Node[] activatedNodes) {    
        if (activatedNodes.length != 1) {
            return false;
        }

        final Node node = activatedNodes[0];
        DataObject dataObject = node.getCookie(DataObject.class);
        if ((dataObject == null) || (dataObject.getPrimaryFile() == null)) {
            return false;
        }

        EditorCookie editorCookie = node.getCookie(EditorCookie.class);
        if (editorCookie == null) {
            editorCookie = dataObject.getCookie(EditorCookie.class);
            if (editorCookie == null) {
                return false;
            }
        }

        if (!FactoryRegistry.hasFactory(dataObject.getClass())) {
            return false;
        }

	// check that the node has project
        Future<Project[]> openProjects = OpenProjects.getDefault().openProjects();
        if(!openProjects.isDone()) {
            return true;
        }
	if (FileOwnerQuery.getOwner(dataObject.getPrimaryFile()) == null) {
            return false;
        }

	return true;
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return I18nUtil.getBundle().getString("CTL_I18nAction");
    }

    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_AUTOINSERT);
    }
}
