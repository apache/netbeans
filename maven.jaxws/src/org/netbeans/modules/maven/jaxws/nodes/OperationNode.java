/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.maven.jaxws.nodes;

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
        return ImageUtilities.loadImage("org/netbeans/modules/maven/jaxws/resources/wsoperation.png"); //NOI18N
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
               
        @Override
        public Object getData () {
            return drop;
        }
        
    }
}
