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

package org.netbeans.modules.form.editors2;

import java.beans.PropertyEditor;
import java.io.IOException;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.ResourceValue;
import org.netbeans.modules.form.ResourceWrapperEditor;
import org.netbeans.modules.form.editors.IconEditor.NbImageIcon;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Resource wrapper for IconEditor from editors package.
 */
public class IconEditor extends ResourceWrapperEditor implements NamedPropertyEditor, XMLPropertyEditor {

    public IconEditor() {
        super(new org.netbeans.modules.form.editors.IconEditor());
    }

    @Override
    public PropertyEditor getDelegatedPropertyEditor() {
        // hack for saving: for compatibility we want this editor to be used
        // for saving (its class name to be written to .form file);
        // so the delegate editor is not exposed
        return this;
    }

    @Override
    protected void setValueToDelegate(Object value) {
        if (value instanceof ResourceValue) {
            ResourceValue resVal = (ResourceValue) value;
            value = resVal.getValue();
            if (value instanceof NbImageIcon)
                delegateEditor.setValue(value);
            else
                delegateEditor.setAsText(resVal.getClassPathResourceName());
        }
        else delegateEditor.setValue(value);
    }

    @Override
    protected void setValueToResourcePanel() {
        Object value = delegateEditor.getValue();
        if (value instanceof NbImageIcon || value == null) {
            NbImageIcon nbIcon = (NbImageIcon) value;
            String stringValue = nbIcon != null ? nbIcon.getName() : "${null}"; // NOI18N
            String resName = (nbIcon != null && nbIcon.getType() == org.netbeans.modules.form.editors.IconEditor.TYPE_CLASSPATH)
                    ? nbIcon.getName() : null;
            resourcePanel.setValue(nbIcon, stringValue, resName);
        } else {
            resourcePanel.setValue(null, "${null}", null); // NOI18N
        }
    }

    // NamedPropertyEditor implementation
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(IconEditor.class, "IconEditor_DisplayName"); // NOI18N
    }

    // XMLPropertyEditor implementation
    @Override
    public void readFromXML(Node element) throws IOException {
        ((org.netbeans.modules.form.editors.IconEditor)delegateEditor).readFromXML(element);
    }

    // XMLPropertyEditor implementation
    @Override
    public Node storeToXML(Document doc) {
        return ((org.netbeans.modules.form.editors.IconEditor)delegateEditor).storeToXML(doc);
    }
}
