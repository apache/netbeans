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

package org.netbeans.modules.i18n.form;

import org.netbeans.modules.form.FormPropertyEditorManager;
import org.openide.modules.OnStart;

/**
 * Installation class for i18n to form cross dependency module.
 * It registers <code>FormI18nStringEditor</code> to form property editors.
 *
 * @author Peter Zavadsky
 */
@OnStart
public class I18nFormCrossModule implements Runnable {
    /** Registers property editor in form module and factory in i18n module. */
    @Override
    public void run() {
        Class newEditorClass = FormI18nStringEditor.class;
        Class newEditorClassInteger = FormI18nIntegerEditor.class;
        Class newEditorClassMnemonic = FormI18nMnemonicEditor.class;
              
        // Register new property editor.
        FormPropertyEditorManager.registerEditor (String.class, newEditorClass);
        FormPropertyEditorManager.registerEditor (int.class, newEditorClassInteger);
        FormPropertyEditorManager.registerEditor (int.class, newEditorClassMnemonic);
    }

}
