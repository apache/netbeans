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
package org.netbeans.modules.print.ui;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.02.03
 */
final class Editor {

    Editor(Class clazz, String title, Object value) {
        myEditor = PropertyEditorManager.findEditor(clazz);
        myEditor.setValue(value);
        myDescriptor = new DialogDescriptor(myEditor.getCustomEditor(), title);
        DialogDisplayer.getDefault().createDialog(myDescriptor).setVisible(true);
    }

    Object getValue() {
        if (myDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            return myEditor.getValue();
        }
        return null;
    }

    private PropertyEditor myEditor;
    private DialogDescriptor myDescriptor;
}
