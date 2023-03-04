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
package org.netbeans.modules.apisupport.project.layers;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Dummy representation of a 'hidden' file from XML layer. Its Node delegate
 * provides an action to restore the deleted file and an action to open appropriate
 * layer file(s).
 *
 * @author S. Aubrecht
 */
public class HiddenDataObject extends MultiDataObject {

    public HiddenDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
    }

    @Override
    protected Node createNodeDelegate() {
        return new HiddenNode(this, getLookup());
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private static class HiddenNode extends DataNode {
        public HiddenNode( DataObject dob, Lookup lkp ) {
            super( dob, Children.LEAF, lkp );
        }

        @Override
        public Action[] getActions(boolean context) {
            Action[] res = new Action[2];
            res[0] = new AbstractAction(NbBundle.getMessage(HiddenDataObject.class, "LBL_restore")) { //NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        destroy();
                    } catch( IOException ex ) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            res[0].setEnabled(canDestroy());
            res[1] = SystemAction.get(OpenLayerFilesAction.class);
            return res;
        }
    }
}
