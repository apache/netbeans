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
