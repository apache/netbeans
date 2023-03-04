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
