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
/*
 * PropertyDisplayer.java
 *
 * Created on 17 October 2003, 15:31
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.*;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeListener;


/** A set of interfaces which define the contract for different types of
 * components that can display or edit properties.  There is the base interface
 * for any component that can display a property, and sub interfaces describing
 * aspects such as editability.  Eventually this interfaces and a factory
 * should become public as a replacement for PropertyPanel - they are much
 * more straightforward in terms of setting expectations correctly about
 * the behavior of the underlying component.
 * <p>
 * Note that to avoid making them public, the subinterfaces have been factored
 * out for the time being.
 *
 * @author  Tim Boudreau */
interface PropertyDisplayer {
    /**Update policy constant - update whenever an ActionEvent is received from
     * an editor component */
    public static final int UPDATE_ON_CONFIRMATION = 0;

    /**Update policy constant - update if the user tabs out of the editor
     * component or it otherwise loses focus */
    public static final int UPDATE_ON_FOCUS_LOST = 1;

    /**Update policy constant - fire an action event but do not actually
     * update the property */
    public static final int UPDATE_ON_EXPLICIT_REQUEST = 2;

    public Property getProperty();

    public void refresh();

    public Component getComponent();
}
