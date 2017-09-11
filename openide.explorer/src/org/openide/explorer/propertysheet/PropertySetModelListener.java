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
 * PropertySetModelListener.java
 *
 * Created on December 30, 2002, 12:13 PM
 */
package org.openide.explorer.propertysheet;


/** Listener interface for PropertySetModel changes.
 *
 * @author  Tim Boudreau
 */
interface PropertySetModelListener extends java.util.EventListener {
    /* Indicates a change is about to occur, but the model data is still
     *  valid with its pre-change values.  */
    public void pendingChange(PropertySetModelEvent e);

    /** A change which has known constraints, such as the insertion or
     *  removal of rows due to expansion/de-expansion of a category in
     *  a property sheet.  The affected rows are available from the
     *  event object. */
    public void boundedChange(PropertySetModelEvent e);

    /** Called when a change occurs that is so far reaching that the
     *  entire model is invalidated.  In this case, the affected
     *  row properties of the event are irrelevant and should not
     *  be used.*/
    public void wholesaleChange(PropertySetModelEvent e);
}
