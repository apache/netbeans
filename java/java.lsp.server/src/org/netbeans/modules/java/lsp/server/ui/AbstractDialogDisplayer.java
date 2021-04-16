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
package org.netbeans.modules.java.lsp.server.ui;

import java.awt.Dialog;
import java.awt.HeadlessException;
import org.netbeans.modules.java.lsp.server.LspServerUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;

/**
 * Remoting implementation of {@link DialogDisplayer}. The implementation will refuse to 
 * display dialogs that block the message processing thread ({@link IllegalStateException} will be thrown.
 * 
 * @author sdedic
 */
public class AbstractDialogDisplayer extends DialogDisplayer {
    private final Lookup context;
    
    public AbstractDialogDisplayer() {
        this(Lookup.getDefault());
    }

    AbstractDialogDisplayer(Lookup context) {
        this.context = context;
    }
    
    @Override
    public Object notify(NotifyDescriptor descriptor) {
        LspServerUtils.avoidClientMessageThread(context);
        UIContext ctx = UIContext.find(context);
        NotifyDescriptorAdapter adapter = new NotifyDescriptorAdapter(descriptor, ctx);
        return adapter.clientNotify();
    }
    
    @Override
    public void notifyLater(final NotifyDescriptor descriptor) {
        UIContext ctx = context.lookup(UIContext.class);
        if (ctx == null) {
            ctx = UIContext.find();
        }
        NotifyDescriptorAdapter adapter = new NotifyDescriptorAdapter(descriptor, ctx);
        adapter.clientNotifyLater();
    }

    @Override
    public Dialog createDialog(DialogDescriptor descriptor) {
        throw new HeadlessException();
    }
}
