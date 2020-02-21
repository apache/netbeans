/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.mixeddev.ui;

import javax.swing.Action;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.AttachToJavaWithNativeDebuggerAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNICallMethodCodeAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNIGetFieldCodeAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNISetFieldCodeAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.CopyJNISignatureAction;
import org.netbeans.modules.cnd.mixeddev.java.jni.actions.GenerateHeaderForJNIClassAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.cnd.mixeddev.ui.MixedDevContextMetaAction", category = "MixedDevelopment")
@ActionRegistration(displayName = "unused-name", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Editors/text/x-java/Popup", position = 1950)
})
public class MixedDevContextMetaAction extends AbstactDynamicMenuAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(MixedDevContextMetaAction.class.getName(), 1);

    public MixedDevContextMetaAction() {
        super(RP, NbBundle.getMessage(MixedDevUtils.class, "Editors/text/x-java/Popup/MixedDevelopment")); // NOI18N
    }

    @Override
    protected Action[] createActions(Lookup actionContext) {
        return new Action[] {
            new GenerateHeaderForJNIClassAction(actionContext),
            new CopyJNISignatureAction(actionContext),
            new CopyJNICallMethodCodeAction(actionContext),
            new CopyJNIGetFieldCodeAction(actionContext),
            new CopyJNISetFieldCodeAction(actionContext),
            new AttachToJavaWithNativeDebuggerAction(actionContext)
            
//            GenerateHeaderForJNIClassAction.INSTANCE,
//            CopyJNISignatureAction.INSTANCE,
//            CopyJNICallMethodCodeAction.INSTANCE,
//            CopyJNIGetFieldCodeAction.INSTANCE,
//            CopyJNISetFieldCodeAction.INSTANCE
        };
    }
}
