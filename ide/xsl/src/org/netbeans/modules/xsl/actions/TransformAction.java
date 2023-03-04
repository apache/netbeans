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
package org.netbeans.modules.xsl.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.netbeans.api.xml.cookies.TransformableCookie;
import org.netbeans.modules.xml.actions.CollectXMLAction;
import org.netbeans.modules.xsl.XSLDataObject;
import org.netbeans.modules.xsl.transform.TransformPerformer;
import org.openide.util.NbBundle;

/**
 * Perform Transform action on XML document.
 * <p>
 * It should be cancellable in future.
 *
 * @author  Libor Kramolis
 */
public class TransformAction extends CookieAction implements CollectXMLAction.XMLAction {
    /** Serial Version UID */
    private static final long serialVersionUID = -640535981015250507L;

    private static TransformPerformer recentPerfomer;

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return super.enable(activatedNodes) && ready();
    }

    /**
     * Avoid spawing next transformatio until recent one is finished.
     * This check should be replaced by cancellable actions in future.
     */
    private boolean ready() {
        if (recentPerfomer == null) {
            return true;
        } else {
            if (recentPerfomer.isActive()) {
                return false;
            } else {
                recentPerfomer = null;
                return true;
            }
        }
    }

    /** */
    @Override
    protected Class[] cookieClasses () {
        return new Class[] { TransformableCookie.class, XSLDataObject.class };
    }

    /** All selected nodes must be XML one to allow this action. */
    @Override
    protected int mode () {
        return MODE_ALL;
    }


    /** Human presentable name. */
    @Override
    public String getName() {
        return NbBundle.getMessage(TransformAction.class, "NAME_transform_action");
    }

    /** Do not slow by any icon. */
    @Override
    protected String iconResource () {
        return "org/netbeans/modules/xsl/resources/xsl_transformation.png"; // NOI18N
    }

    /** Provide accurate help. */
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx (TransformAction.class);
    }


    /** Check all selected nodes. */
    @Override
    protected void performAction (Node[] nodes) {
        recentPerfomer = new TransformPerformer (nodes);
        recentPerfomer.perform();
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
