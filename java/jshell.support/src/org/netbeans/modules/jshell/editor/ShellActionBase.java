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
package org.netbeans.modules.jshell.editor;

import java.awt.event.ActionEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public abstract class ShellActionBase extends BaseAction {

    public ShellActionBase(String name) {
        super(name);
    }

    public ShellActionBase(String name, int updateMask) {
        super(name, updateMask);
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target == null) {
            return;
        }
        Document d = target.getDocument();
        if (d != null) {
            FileObject f = EditorDocumentUtils.getFileObject(d);
            if (f != null) {
                ShellSession s = ShellSession.get(d);
                if (s != null) {
                    doPerformAction(evt, target, s);
                }
            }
        }
    }
    
    protected abstract void doPerformAction(ActionEvent evt, JTextComponent target, ShellSession session);
}
