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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Node representing WS operation
 *
 * @author mkuchtiak
 */
public class OperationNode extends AbstractNode {
    WsdlOperation operation;
    FileObject srcRoot;
    OperationEditorDrop editorDrop;
    
    public OperationNode(WsdlOperation operation) {
        this(operation, new InstanceContent());
    }
    
    private OperationNode(WsdlOperation operation, InstanceContent content) {
        super(Children.LEAF,new AbstractLookup(content));
        this.operation=operation;
        setName(operation.getName());
        setDisplayName(operation.getName());
        content.add(this);
        content.add(operation);
        editorDrop = new OperationEditorDrop(this);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{SystemAction.get(PropertiesAction.class)};
    }
    
    @Override
    public Image getIcon(int type){
        return ImageUtilities.loadImage("org/netbeans/modules/websvc/core/webservices/ui/resources/wsoperation.png"); //NOI18N
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(editorDrop);
        t.put(s);

        return t;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
}
