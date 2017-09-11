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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.modules.languages.Feature;

/**
 *
 * @author hanz
 */
public class DeclarationASTEvaluator extends ASTEvaluator {

    private static Map<Document,WeakReference<DeclarationASTEvaluator>> cache = new WeakHashMap<Document,WeakReference<DeclarationASTEvaluator>> ();
    
    static void register (Document document) {
        if (get (document) != null) return;
        cache.put (document, new WeakReference<DeclarationASTEvaluator> (new DeclarationASTEvaluator (document)));
    }
    
    static void unregister (Document document) {
        DeclarationASTEvaluator evaluator = get (document);
        if (evaluator != null)
            ParserManager.get (document).removeASTEvaluator (evaluator);
        cache.remove (document);
    }
    
    static DeclarationASTEvaluator get (Document document) {
        WeakReference<DeclarationASTEvaluator> weakReference = cache.get (document);
        if (weakReference == null) return null;
        return weakReference.get ();
    }
    
    
    private Document            document;
    
    DeclarationASTEvaluator (Document document) {
        this.document = document;
        ParserManager.get (document).addASTEvaluator (this);
    }

    public void beforeEvaluation (State state, ASTNode root) {
    }

    public void afterEvaluation (State state, ASTNode root) {
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        SyntaxContext sc = SyntaxContext.create (document, ASTPath.create (path));
        if (!feature.getBoolean ("condition", sc, true)) return;
        String name = ((String) feature.getValue ("name", sc)).trim ();
        String type = (String) feature.getValue ("type", sc);
        if (name != null && name.length() > 0) {
            String local = (String) feature.getValue ("local", sc);
            ASTItem leaf = path.get (path.size () - 1);
            DatabaseContext context = ContextASTEvaluator.getCurrentContext (document, leaf.getOffset ());
            if (local != null) {
                DatabaseContext c = context;
                while (c != null && !local.equals (c.getType ()))
                    c = c.getParent ();
                if (c != null) 
                    type = "local";
            }
            DatabaseContext con = context;
            if ("method".equals (type)) { // NOI18N
                con = con.getParent();
                if (con == null) {
                    con = context;
                }
            }
            DatabaseDefinition original = con.getDefinition (name, leaf.getOffset ());
            if (original != null) {
                original.addUsage (new DatabaseUsage(name, leaf.getOffset (), leaf.getEndOffset ()));
            } else {
                DatabaseDefinition definition = new DatabaseDefinition (name, type, leaf.getOffset (), leaf.getEndOffset ());
                con.addDefinition (definition);
                //S ystem.out.println("add " + definition + " to " + con);
                UsagesASTEvaluator.addDatabaseDefinition (document, definition);
            }
            ContextASTEvaluator.setEvaluated(document, true);
        }
    }

    public String getFeatureName () {
        return "SEMANTIC_DECLARATION";
    }
}

