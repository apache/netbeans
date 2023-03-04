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

package org.netbeans.modules.languages.features;

import org.netbeans.api.languages.ASTPath;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Daniel Prusa
 */
public class GoToDeclarationAction extends BaseAction {
    
    public GoToDeclarationAction () {
        super(NbBundle.getBundle(GoToDeclarationAction.class).getString("LBL_GoToDeclaration"));
    }
    
    public void actionPerformed (ActionEvent e, JTextComponent component) {
        JTextComponent comp = getTextComponent(null);
        if (comp == null) return;
        ASTNode node = getASTNode(comp);
        if (node == null) return;
        NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
        if (root == null) return;
        DatabaseItem item = root.getDatabaseItem (path.getLeaf ().getOffset ());
        if (item == null) return;
        if (item instanceof DatabaseUsage) {
            item = ((DatabaseUsage) item).getDefinition();
        }
        
        int offset = item.getOffset();
        DataObject dobj = null;
        StyledDocument docToGo = null;
        URL url = ((DatabaseDefinition) item).getSourceFileUrl();
        if (url == null) {
            dobj = NbEditorUtilities.getDataObject (doc);
            docToGo = doc;
        } else {
            File file = null;
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            
            if (file != null && file.exists()) {
                /** convert file to an uni absolute pathed file (../ etc will be coverted) */
                file = FileUtil.normalizeFile(file);
                FileObject fobj = FileUtil.toFileObject(file);
                try {
                    dobj = DataObject.find(fobj);
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
                if (dobj != null) {
                    Node nodeOfDobj = dobj.getNodeDelegate();
                    EditorCookie ec = nodeOfDobj.getCookie(EditorCookie.class);
                    try {
                        docToGo = ec.openDocument();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                
            }
        }
        
        if (dobj == null) {
            return;
        }
        
        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
        Line.Set lineSet = lc.getLineSet();
        Line line = lineSet.getCurrent(NbDocument.findLineNumber(docToGo, offset));
        int column = NbDocument.findLineColumn (docToGo, offset);
        line.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
    }
    
    public boolean isEnabled() {
        JTextComponent comp = getTextComponent(null);
        if (comp == null)
            return false;
        ASTNode node = getASTNode(comp);
        if (node == null)
            return false;
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        if (path == null)
            return false;
        DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
        if (root == null)
            return false;
        DatabaseItem item = root.getDatabaseItem (path.getLeaf ().getOffset ());
        return item != null;
    }
    
    private ASTNode getASTNode(JTextComponent comp) {
        return ParserManagerImpl.getImpl ((NbEditorDocument)comp.getDocument ()).getAST ();
    }
}
