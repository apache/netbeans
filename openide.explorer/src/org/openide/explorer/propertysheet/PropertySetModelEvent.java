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
 * PropertySetModelEvent.java
 *
 * Created on December 30, 2002, 11:56 AM
 */
package org.openide.explorer.propertysheet;

import java.awt.event.*;


/** Event type that carries information about changes in the property
 *  set model, such as changes in the number of rows that should be
 *  shown in the table due to expanding or closing categories.  In
 *  particular it is used to maintain the current selection across a
 *  change which can alter the selected index.
 * @author  Tim Boudreau
 */
class PropertySetModelEvent extends java.util.EventObject {
    public static final int TYPE_INSERT = 0;
    public static final int TYPE_REMOVE = 1;
    public static final int TYPE_WHOLESALE_CHANGE = 2;
    int type = 2;
    int start = -1;
    int end = -1;
    boolean reordering = false;

    /**  Create a new model event of <code>TYPE_WHOLESALE_CHANGE</code>. */
    public PropertySetModelEvent(Object source) {
        super(source);
    }

    /** Create a new model event with the specified parameters. */
    public PropertySetModelEvent(Object source, int type, int start, int end, boolean reordering) {
        super(source);
        this.type = type;
        this.start = start;
        this.end = end;
        this.reordering = reordering;
    }

    /** Get the type of event.  This will be one of
    * TYPE_INSERT,
    * TYPE_REMOVE, or
    * TYPE_WHOLESALE_CHANGE,
    * depending on the type of change (expansion of a category,
    * de-expansion of a category, or a wholesale change like changing
    * the node displayed, which completely invalidates the displayed
    * data.  */
    public int getType() {
        return type;
    }

    /** Get the first row affected by this change.  */
    public int getStartRow() {
        return start;
    }

    /** Get the last row affected by this change.  This should be the
     *  affected row <strong>prior</strong> to the change;  that is, if
     *  a category is de-expanded, removing properties 20-30, this value
     *  should be 30. */
    public int getEndRow() {
        return end;
    }

    public boolean isReordering() {
        return reordering;
    }
}
