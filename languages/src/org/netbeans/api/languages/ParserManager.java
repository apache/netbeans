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
package org.netbeans.api.languages;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.languages.ParserManagerImpl;


/**
 * Represents parser implementation.
 * 
 * @author Jan Jancura
 */
public abstract class ParserManager {
    
    /**
     * State of parser.
     */
    public static enum State {
        /** Parser is running. */
        PARSING, 
        /** Parsed without errors. */
        OK, 
        /** Parser with errors. */
        ERROR, 
        /** Parser has not been started yet. */
        NOT_PARSED
    }
    
    
    private static Map<Document,WeakReference<ParserManager>> managers = 
        new WeakHashMap<Document,WeakReference<ParserManager>> ();
    
    /**
     * Returns parser for given {@link javax.swing.text.Document}.
     * 
     * @return parser for given {@link javax.swing.text.Document}
     */
    public static synchronized ParserManager get (Document doc) {
        WeakReference<ParserManager> wr = managers.get (doc);
        ParserManager pm = wr != null ? wr.get () : null;
        if (pm == null) {
            pm = new ParserManagerImpl (doc);
            managers.put (doc, new WeakReference<ParserManager> (pm));
            //Utils.startTest ("ParserManager.managers", managers);
//            printManagers ();
        }
        return pm;
    }

    /**
     * Returns state of parser.
     * 
     * @retrun a state of parser
     */
    public abstract State getState ();
    
    /**
     * Returns AST tree root node.
     * 
     * @throws in the case of errors in document
     * @retrun AST tree root node
     */
    public abstract ASTNode getAST () throws ParseException;
    
    /**
     * Registers ParserManagerListener.
     * 
     * @param l ParserManagerListener to be registerred
     */
    public abstract void addListener (ParserManagerListener l);
    
    /**
     * Unregisters ParserManagerListener.
     * 
     * @param l ParserManagerListener to be unregisterred
     */
    public abstract void removeListener (ParserManagerListener l);
    
    /**
     * Registers ASTEvaluator.
     * 
     * @param l ASTEvaluator to be unregisterred
     */
    public abstract void addASTEvaluator (ASTEvaluator e);
    
    /**
     * Unregisters ASTEvaluator.
     * 
     * @param l ASTEvaluator to be unregisterred
     */
    public abstract void removeASTEvaluator (ASTEvaluator e);
    
    /**
     * Checks whether the parsed document has any syntax errors
     * @return Returns TRUE if there are any syntax errors according to the defined grammar
     */
    public abstract boolean hasSyntaxErrors();


    private static void printManagers () {
        System.out.println("\nParserManagers:");
        Iterator<Document> it = managers.keySet ().iterator ();
        while (it.hasNext ()) {
            Document document =  it.next ();
            String title = (String) document.getProperty("title");
            if (title == null)
                title = document.toString();
            WeakReference wr = managers.get (document);
            if (wr.get () != null)
                System.out.println("  " + title);
        }
    }
}



