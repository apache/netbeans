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
     * @return a state of parser
     */
    public abstract State getState ();
    
    /**
     * Returns AST tree root node.
     * 
     * @throws in the case of errors in document
     * @return AST tree root node
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



