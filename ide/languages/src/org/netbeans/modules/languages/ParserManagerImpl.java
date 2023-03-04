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
package org.netbeans.modules.languages;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.languages.lexer.SLanguageHierarchy;
import org.netbeans.modules.languages.lexer.SLexer;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.SyntaxError;
import org.netbeans.modules.languages.parser.TokenInputUtils;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class ParserManagerImpl extends ParserManager {

    private Document                        document;
    private TokenHierarchy                  tokenHierarchy;
    private ASTNode                         ast = ASTNode.create (null, "Root", 0);
    private State                           state = State.NOT_PARSED;
    private List<SyntaxError>               syntaxErrors = Collections.<SyntaxError>emptyList ();
    private boolean[]                       cancel = new boolean[] {false};
    private Set<ParserManagerListener>      listeners;
    private Map<String,Set<ASTEvaluator>>   evaluatorsMap;
    private static RequestProcessor         rp = new RequestProcessor ("Parser");
    
    
    public ParserManagerImpl (Document doc) {
        this.document = doc;
        tokenHierarchy = TokenHierarchy.get (doc);
        String mimeType = (String) doc.getProperty ("mimeType");        
        if (tokenHierarchy == null) {
            // for tests only....
            if (mimeType != null) {
                try {
                    Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
                    if (language.getParser () != null) {
                        doc.putProperty (
                            org.netbeans.api.lexer.Language.class, 
                            new SLanguageHierarchy (language).language ()
                        );
                        tokenHierarchy = TokenHierarchy.get (doc);
                    }
                } catch (LanguageDefinitionNotFoundException ex) {
                }
            }
        }
        if (tokenHierarchy != null) {
            new DocListener (this, tokenHierarchy);
            if (mimeType != null && state == State.NOT_PARSED) {
                try {
                    LanguagesManager.getDefault().getLanguage(mimeType);
                    startParsing();
                } catch (LanguageDefinitionNotFoundException e) {
                    //not supported language
                }
            }
        }
        
        managers.put (doc, new WeakReference<ParserManager> (this));
    }
    
    public static ParserManagerImpl getImpl (Document document) {
        return (ParserManagerImpl) get (document);
    }
    
    public State getState () {
        return state;
    }
    
    public List<SyntaxError> getSyntaxErrors () {
        return syntaxErrors;
    }

    @Override
    public boolean hasSyntaxErrors() {
        return !getSyntaxErrors().isEmpty();
    }
    
    public ASTNode getAST () {
        return ast;
    }
    
    public void addListener (ParserManagerListener l) {
        if (listeners == null) listeners = new HashSet<ParserManagerListener> ();
        listeners.add (l);
    }
    
    public void removeListener (ParserManagerListener l) {
        if (listeners == null) return;
        listeners.remove (l);
    }
    
    public void addASTEvaluator (ASTEvaluator e) {
        if (evaluatorsMap == null)
            evaluatorsMap = new HashMap<String,Set<ASTEvaluator>> ();
        Set<ASTEvaluator> evaluatorsSet = evaluatorsMap.get (e.getFeatureName ());
        if (evaluatorsSet == null) {
            evaluatorsSet = new HashSet<ASTEvaluator> ();
            evaluatorsMap.put (e.getFeatureName (), evaluatorsSet);
        }
        evaluatorsSet.add (e);
    }
    
    public void removeASTEvaluator (ASTEvaluator e) {
        if (evaluatorsMap != null) {
            Set<ASTEvaluator> evaluatorsSet = evaluatorsMap.get (e.getFeatureName ());
            if (evaluatorsSet != null) 
                evaluatorsSet.remove (e);
        }
    }
    
    public void fire (
        final State                           state, 
        final List<ParserManagerListener>     listeners,
        final Map<String,Set<ASTEvaluator>>   evaluators,
        final ASTNode                         root
    ) {
        if (root == null) throw new NullPointerException ();
        parsingTask = rp.post (new Runnable () {
            public void run () {
                cancel [0] = false;
                fire2 (
                    state,
                    listeners,
                    evaluators,
                    root
                );
            }
        });
    }

    
    // private methods .........................................................
    
    private RequestProcessor.Task parsingTask;
    
    private synchronized void startParsing () {
        setChange (State.PARSING, ast);
        cancel [0] = true;
        if (parsingTask != null) {
            parsingTask.cancel ();
        }
        parsingTask = rp.post (new Runnable () {
            public void run () {
                cancel [0] = false;
                parse ();
            }
        }, 1000);
    }
    
    private void setChange (State state, ASTNode root) {
        if (state == this.state) return;
        this.state = state;
        this.ast = root;
        List<ParserManagerListener> listeners = this.listeners == null ?
            null : new ArrayList<ParserManagerListener> (this.listeners);
        Map<String,Set<ASTEvaluator>> evaluatorsMap = this.evaluatorsMap == null ?
            null : new HashMap<String,Set<ASTEvaluator>> (this.evaluatorsMap);
        fire2 (state, listeners, evaluatorsMap, root);
    }
    
    private void fire2 (
        State                           state, 
        List<ParserManagerListener>     listeners,
        Map<String,Set<ASTEvaluator>>   evaluators,
        ASTNode                         root
    ) {

        if (state == State.PARSING) return;
        if (evaluators != null) {
            if (!evaluators.isEmpty ()) {
                Iterator<Set<ASTEvaluator>> it = evaluators.values ().iterator ();
                while (it.hasNext ()) {
                    Iterator<ASTEvaluator> it2 = it.next ().iterator ();
                    while (it2.hasNext ()) {
                        ASTEvaluator e = it2.next ();
                        e.beforeEvaluation (state, root);
                        if (cancel [0]) return;
                    }
                }
                                                                                //times = new HashMap<Object,Long> ();
                evaluate (
                    state, 
                    root, 
                    new ArrayList<ASTItem> (), 
                    evaluators                                                  //, times
                );                                                              //iit = times.keySet ().iterator ();while (iit.hasNext()) {Object object = iit.next();S ystem.out.println("  Evaluator " + object + " : " + times.get (object));}
                if (cancel [0]) return;
                it = evaluators.values ().iterator ();
                while (it.hasNext ()) {
                    Iterator<ASTEvaluator> it2 = it.next ().iterator ();
                    while (it2.hasNext ()) {
                        ASTEvaluator e = it2.next ();
                        e.afterEvaluation (state, root);
                        if (cancel [0]) return;
                    }
                }
            }
        }
        
        if (listeners != null) {
            Iterator<ParserManagerListener> it = listeners.iterator ();
            while (it.hasNext ()) {
                ParserManagerListener l = it.next ();                           //long start = System.currentTimeMillis ();
                l.parsed (state, ast);
                                                                                //Long t = times.get (l);if (t == null) t = new Long (0);times.put (l, t.longValue () + S ystem.currentTimeMillis () - start);
                if (cancel [0]) return;
            }
        }                                                                       //Iterator iit = times.keySet ().iterator ();while (iit.hasNext()) {Object object = iit.next();S ystem.out.println("  Listener " + object + " : " + times.get (object));}
    }
    
    private void evaluate (
        State state, 
        ASTItem item, 
        List<ASTItem> path,
        Map<String,Set<ASTEvaluator>> evaluatorsMap2                            //, Map<Object,Long> times                                         
    ) {
        path.add (item);
        Language language = (Language) item.getLanguage ();
        if (language != null)
            language.getFeatureList ().evaluate (
                 state, 
                 path, 
                 evaluatorsMap2                                                 //, times
            );
        Iterator<ASTItem> it2 = item.getChildren ().iterator ();
        while (it2.hasNext ()) {
            if (cancel [0]) return;
            evaluate (
                state, 
                it2.next (), 
                path, 
                evaluatorsMap2                                                  //, times
            );
        }
        path.remove (path.size () - 1);
    }
    
    private void parse () {
        setChange (State.PARSING, ast);
        String mimeType = (String) document.getProperty ("mimeType");
        Language language = getLanguage (mimeType);
        LLSyntaxAnalyser analyser = language.getAnalyser ();                           //long start = System.currentTimeMillis ();
        TokenInput input = createTokenInput ();
        if (cancel [0]) return;                                                 //S ystem.out.println ("lex " + (System.currentTimeMillis () - start));start = System.currentTimeMillis ();
        List<SyntaxError> newSyntaxErrors = new ArrayList<SyntaxError> ();
        try {
            ast = analyser.read (
                input, 
                true, 
                newSyntaxErrors,
                cancel
            );                                                                  //S ystem.out.println ("syntax " + (System.currentTimeMillis () - start));
            syntaxErrors = newSyntaxErrors;
        } catch (ParseException ex) {
            // should not be called - read (skipErrors == true)
            Utils.notify (ex);
            ast = ASTNode.create (language, "Root", 0);
            setChange (State.OK, ast);                                          //S ystem.out.println ("fire " + (System.currentTimeMillis () - start));
            return;
        }
        if (cancel [0]) return;                                                 //long start = System.currentTimeMillis ();
        try {
            Feature astProperties = language.getFeatureList ().getFeature ("AST");
            if (astProperties != null) {
                ASTNode processedAst = (ASTNode) astProperties.getValue (
                    "process", 
                    SyntaxContext.create (document, ASTPath.create (ast))
                );
                if (processedAst != null) {
                    ast = processedAst;
                }
            }
        } catch (Exception ex) {
            Utils.notify (ex);
            ast = ASTNode.create (language, "Root", 0);
        }                                                                       //start = System.currentTimeMillis () - start;if (start > 100)S ystem.out.println ("postprocess " + start);
        if (ast == null) {
            Utils.notify (new NullPointerException ());
            ast = ASTNode.create (language, "Root", 0);
        }                                                                   //start = System.currentTimeMillis ();
        setChange (State.OK, ast);                                          //S ystem.out.println ("fire " + (System.currentTimeMillis () - start));
    }
    
    private TokenInput createTokenInput () {
        final TokenInput[] result = new TokenInput [1];
        document.render (new Runnable () {
            public void run () {
                if (tokenHierarchy == null) {
                    result [0] = TokenInputUtils.create (Collections.<ASTToken>emptyList ());
                    return;
                }
                TokenSequence ts = tokenHierarchy.tokenSequence ();
                List<ASTToken> tokens = getTokens (ts);
                if (cancel [0]) {
                    // Leave null in ret[0]
                    return;
                }
                result [0] = TokenInputUtils.create (tokens);
            }
        });
        return result [0];
    }
    
    private List<ASTToken> getTokens (TokenSequence ts) {
        List<ASTToken> tokens = new ArrayList<ASTToken> ();
        if (ts == null) return tokens;
        Language language = null;
        try {
            language = LanguagesManager.getDefault ().getLanguage (ts.language ().mimeType ());
        } catch (LanguageDefinitionNotFoundException ex) {
        }
        if (!ts.moveNext ()) return tokens;
        Token t = ts.token ();
        int type = t.id ().ordinal ();
        int offset = ts.offset ();
        String ttype = (String) t.getProperty ("type");
        List<ASTToken> firstInjection = null;
        if (ttype == SLexer.INJECTED_CODE) {
            // first token can be injected 
            TokenSequence ts2 = ts.embedded ();
            firstInjection = getTokens (ts2);
            if (!ts.moveNext ()) {
                tokens.add (ASTToken.create (
                    language,
                    0, 
                    "", 
                    offset,
                    0,
                    firstInjection
                ));
                return tokens;
            }
            t = ts.token ();
            type = t.id ().ordinal ();
            offset = ts.offset ();
            ttype = (String) t.getProperty ("type");
        }
        for (;;) {
            if (cancel [0]) return null;
            if (ttype == null) {
                List<ASTToken> children = getTokens (ts.embedded ());
                if (firstInjection != null) {
                    if (children != null)
                        children.addAll (firstInjection);
                    else
                        children = firstInjection;
                    firstInjection = null;
                }
                tokens.add (ASTToken.create (
                    language,
                    type, 
                    t.text ().toString (), 
                    offset,
                    t.length (),
                    children
                ));
                children = null;
            } else
            if (ttype == SLexer.CONTINUOUS_TOKEN_START) {
                StringBuilder sb = new StringBuilder (t.text ());
                List<ASTToken> children = new ArrayList<ASTToken> ();
                TokenSequence ts2 = ts.embedded ();
                while (ts.moveNext ()) {
                    if (cancel [0]) return null;
                    t = ts.token ();
                    ttype = (String) t.getProperty ("type");
                    if (ttype == null) {
                        ts.movePrevious ();
                        break;
                    }
                    if (ttype == SLexer.INJECTED_CODE) {
                        ts2 = ts.embedded ();
                        if (ts2 != null) {
                            List<ASTToken> tokens2 = getTokens (ts2);
                            if (cancel [0]) return null;
                            children.addAll (tokens2);
                        }
                        continue;
                    }
                    if (ttype == SLexer.CONTINUOUS_TOKEN_START) {
                        ts.movePrevious ();
                        break;
                    }
                    if (ttype != SLexer.CONTINUOUS_TOKEN)
                        throw new IllegalArgumentException ();
                    if (type != t.id ().ordinal ())
                        throw new IllegalArgumentException ();
                    sb.append (t.text ());
                }
                int no = ts.offset () + ts.token ().length ();
                tokens.add (ASTToken.create (
                    language,
                    type, 
                    sb.toString (), 
                    offset,
                    no - offset,
                    children
                ));
            } else
                throw new IllegalArgumentException ();
            if (!ts.moveNext ()) return tokens;
            t = ts.token ();
            type = t.id ().ordinal ();
            offset = ts.offset ();
            ttype = (String) t.getProperty ("type");
        }
    }
    
    private Language getLanguage (String mimeType) {
        try {
            return LanguagesManager.getDefault ().getLanguage (mimeType);
        } catch (LanguageDefinitionNotFoundException ex) {
            return Language.create (LanguagesManager.normalizeMimeType(mimeType));
        }
    }
    
    private static Map<Document,WeakReference<ParserManager>> managers = 
        new WeakHashMap<Document,WeakReference<ParserManager>> ();

    // HACK
    static void refreshHack () {
        Iterator<Document> it = managers.keySet ().iterator ();
        while (it.hasNext ()) {
            AbstractDocument document = (AbstractDocument) it.next ();
            document.readLock ();
            try {
                MutableTextInput mti = (MutableTextInput) document.getProperty (MutableTextInput.class);
                mti.tokenHierarchyControl ().rebuild ();
            } finally {
                document.readUnlock ();
            }
//            final StyledDocument document = (StyledDocument) it.next ();
//            NbDocument.runAtomic (document, new Runnable () {
//                public void run() {
//                    MutableTextInput mti = (MutableTextInput) document.getProperty (MutableTextInput.class);
//                    mti.tokenHierarchyControl ().rebuild ();
//                }
//            });
        }
    }
    
    // innerclasses ............................................................
    
    private static class DocListener implements TokenHierarchyListener {
        
        private WeakReference<ParserManagerImpl> pmwr;
        
        DocListener (ParserManagerImpl pm, TokenHierarchy hierarchy) {
            pmwr = new WeakReference<ParserManagerImpl> (pm);
            hierarchy.addTokenHierarchyListener (this);
        }
        
        private ParserManagerImpl getPM () {
            ParserManagerImpl pm = pmwr.get ();
            if (pm != null) return pm;
            return null;
        }
    
        public void tokenHierarchyChanged (TokenHierarchyEvent evt) {
            ParserManagerImpl pm = getPM ();
            if (pm == null) return;
            pm.startParsing ();
        }
    }
}
