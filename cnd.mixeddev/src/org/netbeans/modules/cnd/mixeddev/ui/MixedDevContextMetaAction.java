/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
