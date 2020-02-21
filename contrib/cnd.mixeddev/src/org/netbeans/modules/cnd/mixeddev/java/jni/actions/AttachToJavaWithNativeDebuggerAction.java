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

import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.Triple;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.cnd.mixeddev.java.model.jni.JNIClass;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class AttachToJavaWithNativeDebuggerAction extends AbstractJNIAction {
    
    public AttachToJavaWithNativeDebuggerAction(Lookup context) {
        super(context);
        putValue(NAME, NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.attach_to_java_with_native_debugger")); // NOI18N
        //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, Boolean.TRUE);        
    }

    @Override
    protected boolean isEnabledAtPosition(Document doc, int caret) {
        return false;
    }

    @Override
    protected boolean isEnabled(Triple<DataObject, Document, Integer> context) {
        if (context == null) {
            return false;
        }
        final FileObject javaFile = context.first.getPrimaryFile();
        final Document doc = context.second;
        final int caret = context.third;
        JNIClass cls = JNISupport.getJNIClass(doc, caret);  
        if (cls == null) {
            return false;
        }
        //find debug session??
        Project javaProject = FileOwnerQuery.getOwner(javaFile);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (Session session : sessions) {
            Project sessionProject = session.lookupFirst(null, Project.class);
            if (javaProject == sessionProject) {
                return true;
            }
        }
        return false;
    }
    

    @Override
    protected void actionPerformedImpl(Node[] activatedNodes) {    
        final Triple<DataObject, Document, Integer> context = extractContext(activatedNodes);
        if (context != null) {
            final FileObject javaFile = context.first.getPrimaryFile();
            final Document doc = context.second;
            final int caret = context.third;
            JNIClass cls = JNISupport.getJNIClass(doc, caret);      
            if (cls == null) {
                return;
            }
            //find debug session??
            Project javaProject = FileOwnerQuery.getOwner(javaFile);
            Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
            for (Session session : sessions) {
                Project sessionProject = session.lookupFirst(null, Project.class);
                if (javaProject == sessionProject) {
                    //this is our session we will attach
                    Session nativeSession = MixedDevUtils.attachToJavaProcess(session.getName(), 
                            cls.getClassInfo().getQualified(),
                            cls.getJNIMethods().get(0).getName().toString(), 
                            true);
                    DebuggerManager.getDebuggerManager().setCurrentSession(session);
                }
                //System.out.println("session=" + session.getName() + " locationName=" + session.getLocationName());
            }
        }

    }   
}
