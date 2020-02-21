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
package org.netbeans.modules.cnd.mixeddev.java.jni.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.Triple;
import org.netbeans.modules.cnd.mixeddev.java.*;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaClassInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaFieldInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class CopyJNISignatureAction extends AbstractJNIAction {
    
    public CopyJNISignatureAction(Lookup context) {
        super(context);
        putValue(NAME, NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.copy_jni_signature")); // NOI18N
    }

    @Override
    protected boolean isEnabledAtPosition(Document doc, int caret) {
        JavaEntityInfo entity = resolveJavaEntity(doc, caret);
        if (entity != null) {
            if (entity instanceof JavaMethodInfo) {
                return !((JavaMethodInfo) entity).isNative();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void actionPerformedImpl(Node[] activatedNodes) {
        Triple<DataObject, Document, Integer> context = extractContext(activatedNodes);
        if (context != null) {
            final Document doc = context.second;
            final int caret = context.third;
            String jniSignature = null;
            JavaEntityInfo entity = resolveJavaEntity(doc, caret);
            if (entity instanceof JavaClassInfo) {
                jniSignature = JNISupport.getJNISignature((JavaClassInfo) entity);
            } else if (entity instanceof JavaMethodInfo) {
                jniSignature = JNISupport.getJNISignature((JavaMethodInfo) entity);
            } else if (entity instanceof JavaFieldInfo) {
                jniSignature = JNISupport.getJNISignature((JavaFieldInfo) entity);
            }
            if (jniSignature != null) {
                StatusDisplayer.getDefault().setStatusText(jniSignature);
                StringSelection ss = new StringSelection(jniSignature);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(ss, null);
            }
        }
    }
}
