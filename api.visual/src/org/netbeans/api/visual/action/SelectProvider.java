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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface controls a select action.
 *
 * @author David Kaspar
 */
public interface SelectProvider {

    /**
     * Called to check whether aiming is allowed
     * @param widget the aimed widget
     * @param localLocation the local location of a mouse cursor while aiming is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     * @return true, if aiming is allowed and widget is set to aimed state while mouse button is pressed;
     *         false, if aiming is disallowed and widget is not set to aimed state at any time.
     */
    boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection);

    /**
     * Called to check whether the selection is allowed.
     * @param widget the selected widget
     * @param localLocation the local location of a mouse cursor while selection is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     * @return true, if selection is allowed; false, if selection is disallowed
     */
    boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection);

    /**
     * Called to perform the selection.
     * @param widget the selected widget
     * @param localLocation the local location of a mouse cursor while selection is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     */
    void select (Widget widget, Point localLocation, boolean invertSelection);

}
