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
package org.netbeans.modules.cnd.actions;

import java.util.Collection;
import org.netbeans.modules.cnd.execution.CompileExecSupport;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=ActionProvider.class)
public class StandAloneFileActionProvider implements ActionProvider {

    @Override
    public String[] getSupportedActions() {
        return new String[] { ActionProvider.COMMAND_COMPILE_SINGLE };
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
        if (files.isEmpty()) {
            return;
        }
        for (DataObject d : files) {
            final FileObject fo = d.getPrimaryFile();
            if (fo == null) {
                continue;
            }
            String mimeType = fo.getMIMEType();
            if (mimeType == null) {
                continue;
            }
            if (MIMENames.isCppOrCOrFortran(mimeType)) {
                CompileAction.performAction(d.getNodeDelegate());
                return;
            }
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
        if (files.isEmpty()) {
            return false;
        }
        for (DataObject d : files) {
            final FileObject fo = d.getPrimaryFile();
            if (fo == null) {
                continue;
            }
            String mimeType = fo.getMIMEType();
            if (mimeType == null) {
                continue;
            }
            if (MIMENames.isCppOrCOrFortran(mimeType)) {
                return d.getLookup().lookup(CompileExecSupport.class) != null;
            }
        }
        return false;
    }
    
}
