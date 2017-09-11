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

package org.netbeans.modules.languages.features;

import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.openide.ErrorManager;


/**
 *
 * @author Jan Jancura
 */
public class FormatAction extends BaseAction {
    
    public FormatAction () {
        super("Format");
    }
    
    public void actionPerformed (ActionEvent e, JTextComponent component) {
        try {
            NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
            ASTNode root = ParserManagerImpl.getImpl (doc).getAST ();
            if (root == null) return;
            StringBuilder sb = new StringBuilder ();
            Map<String,String> indents = new HashMap<String,String> ();
            indents.put ("i", ""); // NOI18N
            indents.put ("ii", "    "); // NOI18N
            indent (
                root,
                new ArrayList<ASTItem> (),
                sb,
                indents,
                null,
                false,
                doc
            );
            doc.remove (0, doc.getLength ());
            doc.insertString (0, sb.toString (), null);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    // uncomment to disable the action for languages without grammar definition
    /*
    public boolean isEnabled() {
        JTextComponent component = getTextComponent(null);
        if (component == null)
            return false;
        NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
        String mimeType = (String) doc.getProperty ("mimeType"); // NOI18N
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            return !language.getAnalyser().getRules().isEmpty();
        } catch (ParseException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
     */
    
    private static void indent (
        ASTItem          item,
        List<ASTItem>    path,
        StringBuilder    sb,
        Map<String,String> indents,
        ASTToken         whitespace,
        boolean          firstIndented,
        NbEditorDocument document
    ) {
        Language language = (Language) item.getLanguage ();
        path.add (item);
        ASTPath path2 = ASTPath.create (path);
        Iterator<ASTItem> it = item.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem e = it.next ();

            // compute indent
            String indent = null;
            if (e instanceof ASTToken) {
                ASTToken token = (ASTToken) e;
                if (language.getAnalyser ().getSkipTokenTypes ().contains (token.getTypeID ())) { // NOI18N
                    whitespace = (ASTToken) e;
                    firstIndented = false;
                    continue;
                }
            }
            Feature format = language.getFeatureList ().getFeature ("FORMAT", path2);
            if (format != null)
                indent = (String) format.getValue ();

            // indent
//                if (e instanceof ASTNode)
//                    S ystem.out.println("indent " + indent + " " + firstIndented + " : " + ((ASTNode) e).getNT () + " wh:" + whitespace);
//                else
//                    S ystem.out.println("indent " + indent + " " + firstIndented + " : " + e + " wh:" + whitespace);

            if (indent != null) {
                if (indent.equals ("NewLine"))
                    sb.append ("\n");
            }

            // add child
            if (e instanceof ASTToken)
                sb.append (((ASTToken) e).getIdentifier ());
            else
                indent (
                    (ASTNode) e,
                    path,
                    sb, 
                    indents,
                    whitespace,
                    firstIndented || indent != null,
                    null
                );
        }// for children
        path.remove (path.size () - 1);
    }
    
    private static String chars (int length) {
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < length; i++) sb.append (' ');
        return sb.toString ();
    }
}
