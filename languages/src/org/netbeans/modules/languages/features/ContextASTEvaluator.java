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
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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
import org.netbeans.modules.languages.Feature;


/**
 *
 * @author hanz
 */
public class ContextASTEvaluator extends ASTEvaluator {

    private static Map<Document,WeakReference<ContextASTEvaluator>> cache = new WeakHashMap<Document,WeakReference<ContextASTEvaluator>> ();
    
    static void register (Document document) {
        if (get (document) != null) return;
        cache.put (document, new WeakReference<ContextASTEvaluator> (new ContextASTEvaluator (document)));
    }
    
    static void unregister (Document document) {
        ContextASTEvaluator evaluator = get (document);
        if (evaluator != null)
            ParserManager.get (document).removeASTEvaluator (evaluator);
        cache.remove (document);
    }
    
    static ContextASTEvaluator get (Document document) {
        WeakReference<ContextASTEvaluator> weakReference = cache.get (document);
        if (weakReference == null) return null;
        return weakReference.get ();
    }
    
    static DatabaseContext getRootContext (Document document) {
        ContextASTEvaluator evaluator = get (document);
        if (evaluator == null) return null;
        return evaluator.rootContext;
    }
    
    static DatabaseContext getCurrentContext (Document document, int offset) {
        ContextASTEvaluator evaluator = get (document);
        try {
            if (evaluator == null) return null;
            DatabaseContext context = evaluator.currentContext.peek ();
            while (offset < context.getOffset () || context.getEndOffset () <= offset) {
                evaluator.currentContext.pop ();
                context = evaluator.currentContext.peek ();
            }
            return context;
        } catch (EmptyStackException ex) {
            return evaluator.rootContext;
        }
    }
    
    static void setEvaluated(Document document, boolean evaluated) {
        ContextASTEvaluator evaluator = get (document);
        if (evaluator == null) return;
        evaluator.evaluated = evaluated;
    }
    
    private Document                    document;
    private DatabaseContext             rootContext;
    private Stack<DatabaseContext>      currentContext;
    private boolean evaluated;
    
    
    ContextASTEvaluator (Document document) {
        this.document = document;
        ParserManager.get (document).addASTEvaluator (this);
    }

    public void beforeEvaluation (State state, ASTNode root) {
        rootContext = new DatabaseContext (null, null, root.getOffset (), root.getEndOffset ());
        currentContext = new Stack<DatabaseContext> ();
        currentContext.push (rootContext);
        evaluated = false;
    }

    public void afterEvaluation (State state, ASTNode root) {
        if (evaluated) {
            DatabaseManager.setRoot (root, rootContext);
        }
    }

    public void evaluate (State state, List<ASTItem> path, Feature feature) {
        SyntaxContext sc = SyntaxContext.create (document, ASTPath.create (path));
        if (!feature.getBoolean ("condition", sc, true)) return;
        String type = (String) feature.getValue ("type");
        ASTItem leaf = path.get (path.size () - 1);
        DatabaseContext context = getCurrentContext (document, leaf.getOffset ());
        DatabaseContext newContext = new DatabaseContext (context, type, leaf.getOffset (), leaf.getEndOffset ());
        context.addContext (leaf, newContext);
        currentContext.push (newContext);
        evaluated = true;
    }

    public String getFeatureName () {
        return "SEMANTIC_CONTEXT";
    }
}
