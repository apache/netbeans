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
