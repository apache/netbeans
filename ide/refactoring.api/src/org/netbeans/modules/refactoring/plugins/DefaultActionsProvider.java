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

package org.netbeans.modules.refactoring.plugins;

import java.util.Collection;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.impl.SafeDeleteUI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class)
public class DefaultActionsProvider extends ActionsImplementationProvider {
    
    /** Creates a new instance of DefaultActionsProvider */
    public DefaultActionsProvider() {
    }
    @Override
    public boolean canDelete(Lookup lookup) {
        return false;
//        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
//        boolean result = false;
//        for (Node node:nodes) {
//            DataObject dob = (DataObject) node.getCookie(DataObject.class);
//            if (dob==null) {
//                return false;
//            } else {
//                if (dob.getPrimaryFile().isFolder()) {
//                    return false;
//                }
//            }
//            result=true;
//        }
//        return result;
    }

    @Override
    public void doDelete(final Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        FileObject[] fobs = new FileObject[nodes.size()];
        int i = 0;
        for (Node node:nodes) {
            DataObject dob = (DataObject) node.getCookie(DataObject.class);
            if (dob!=null) {
                fobs[i++] = dob.getPrimaryFile();
            }
        }
        UI.openRefactoringUI(new SafeDeleteUI(fobs));
    }
}
