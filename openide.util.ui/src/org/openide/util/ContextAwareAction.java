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

package org.openide.util;

import javax.swing.Action;

/**
 * Interface to be implemented by an action whose behavior
 * is dependent on some context.
 * The action created by {@link #createContextAwareInstance}
 * is bound to the provided context: {@link Action#isEnabled},
 * {@link Action#actionPerformed}, etc. may be specific to that context.
 * <p class="nonnormative">For example, the action representing a context menu item will usually implement
 * this interface. When the actual context menu is created, rather than making a
 * presenter for the generic action, the menu will contain a presenter for the
 * context-aware instance. The context will then be taken from the GUI
 * environment where the context menu was shown; for example it may be a
 * <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent's context</a>,
 * often taken from an activated node selection. The context action might be
 * enabled only if a certain "cookie" is present in that selection. When invoked,
 * the action need not search for an object to act on, since it can use the context.
 * <p>
 *
 * @author Jaroslav Tulach, Peter Zavadsky
 *
 * @see <a href="http://wiki.netbeans.org/DevFaqActionContextSensitive">NetBeans FAQ</a>
 * @see org.openide.util.Utilities#actionsToPopup
 * @see org.openide.util.Utilities#actionsGlobalContext
 * @since 3.29
 */
public interface ContextAwareAction extends Action {

    /**
     * Creates action instance for provided context.
     * @param actionContext an arbitrary context (e.g. "cookies" from a node selection)
     * @return a transient action whose behavior applies only to that context
     */
    public Action createContextAwareInstance(Lookup actionContext);

}
