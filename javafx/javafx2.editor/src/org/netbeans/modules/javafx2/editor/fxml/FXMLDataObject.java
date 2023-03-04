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

package org.netbeans.modules.javafx2.editor.fxml;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


@NbBundle.Messages({
    "LBL_FXML_loader_name=FXML"
})
@MIMEResolver.ExtensionRegistration(
       extension=JavaFXEditorUtils.FXML_FILE_EXTENSION,
       mimeType=JavaFXEditorUtils.FXML_MIME_TYPE,
       position=8739,
       displayName="#LBL_FXML_loader_name") // NOI18N
public class FXMLDataObject extends MultiDataObject {

   public FXMLDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
       super(pf, loader);
       registerEditor(JavaFXEditorUtils.FXML_MIME_TYPE, true);
   }

   @Override
   protected Node createNodeDelegate() {
       return new DataNode(this, Children.LEAF, getLookup()) {
           @Override
           public Action getPreferredAction() {
               for(Action a : getActions(true)) {
                   if (a == null) {
                       continue;
                   }
                   if (a.isEnabled()) {
                       if (a instanceof FXMLOpenAction) {
                           return a;
                       } else if (a instanceof FXMLEditAction) {
                           return a;
                       }
                   }
               }
               return super.getPreferredAction();
           }
       };
   }

   @Override
   protected int associateLookup() {
       return 1;
   }
}