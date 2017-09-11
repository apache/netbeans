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
package org.openide.explorer.propertysheet.editors;

import java.awt.Component;


/** Enhances standard property editor to support in-place custom editors and tagged values.
 * <strong>Use of this class is strongly discouraged</strong> - the original
 * NetBeans property sheet did not specify a strong contract for when the
 * property should be updated from an editor such as the custom inplace editor
 * this interface allows you to provide.  The new (NetBeans 4.0 and later)
 * property sheet does specify such a contract, and the InplaceEditor
 * interface exists to allow it to be fulfilled.
 * @author Jan Jancura, Ian Formanek
 * @deprecated Instead of this class, implement ExPropertyEditor and InplaceEditor.Factory.  Also
 * create an implementation of InplaceEditor for the custom inline editor.
 * In the <code>attachEnv()</code> method of your ExPropertyEditor, call
 * <code>PropertyEnv.registerInplaceEditorFactory(this)</code>.  <p><strong>
 * Before you do any of this</strong> read the prose documentation on the
 * Explorer API and be sure you cannot do what you need with an existing
 * property editor - it is very rare to actually need to provide a custom
 * editor component.
 * @see org.openide.explorer.propertysheet.InplaceEditor
 * @see org.openide.explorer.propertysheet.InplaceEditor.Factory
 * @see org.openide.explorer.propertysheet.PropertyEnv
 */
public @Deprecated interface EnhancedPropertyEditor extends java.beans.PropertyEditor {
    /** Get an in-place editor.
    * @return a custom property editor to be shown inside the property
    *         sheet
    */
    public Component getInPlaceCustomEditor();

    /** Test for support of in-place custom editors.
    * @return <code>true</code> if supported
    */
    public boolean hasInPlaceCustomEditor();

    /** Test for support of editing of tagged values.
    * Must also accept custom strings, otherwise you may may specify a standard property editor accepting only tagged values.
    * @return <code>true</code> if supported
    */
    public boolean supportsEditingTaggedValues();
}
