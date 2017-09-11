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

package org.openide.explorer.propertysheet;

import java.beans.PropertyEditor;

/**
 * An extension interface for property editors that hides
 * all the necessary communication with the property sheet. ExPropertyEditor
 * is able to accept an instance of PropertyEnv class - this
 * environment passes additional information to the editor.
 * The <code>PropertyEnv</code> instance is typically used
 * to set the valid/invalid state of the property, and to
 * retrieve a reference to the Node.Property or PropertyDescriptor
 * for the property being edited.
 * @author  dstrupl
 */
public interface ExPropertyEditor extends PropertyEditor {
    /**
     * If you want to enable/disable the OK button on the custom
     * property editor panel you can fire a property change event
     * with boolean value. You don't have to implement the ExPropertyEditor
     * interface for this feature to be turned on.
     * When firing property change event PROP_VALUE_VALID is the name
     * and an instance of java.lang.Boolean should be passed as a value.
     */
    public static final String PROP_VALUE_VALID = "propertyValueValid"; // NOI18N

    /**
     * If you want to add custom help ID on the custom property
     * editor panel you can store its value in PROPERTY_HELP_ID property.
     */
    public static final String PROPERTY_HELP_ID = "helpID"; // NOI18N

    /**
     * This method is called by the property sheet to pass
     * the environment to the property editor.  The typical use case is for
     * the ExPropertyEditor to call <code>
     * env.getFeatureDescriptor().getValue (String key)</code> to retrieve
     * any hints the Property object may supply regarding how the property
     * editor should behave (such as providing alternate text representations
     * of &quot;true&quot; and &quot;false&quot; for a Boolean property
     * editor).<P>Property editors that support an invalid state (typically
     * used to disable the OK button in custom editor dialogs) should cache
     * the env object and update the env's state on calls to <code>setValue()</code>.
     * <P><strong>Note:</strong> This method may be called more than once
     * during the lifetime of a property editor.  In particular, custom
     * property editors which want to change the state of the OK button by
     * calling <code>PropertyEnv.setState(PropertyEnv.STATE_VALID)</code>
     * should not assume that the instance of PropertyEnv most recently
     * passed to attachEnv() on the underlying property editor is the same
     * instance that controls the dialog they are displayed in.  Custom
     * editors which wish to control the state of the OK button should cache
     * the last-set PropertyEnv at the time they are instantiated.
     */
    public void attachEnv(PropertyEnv env);
}
