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
package org.netbeans.modules.css.lib.api.properties;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import static org.netbeans.modules.css.lib.api.properties.GrammarResolver.Log.*;
import org.netbeans.modules.css.lib.properties.GrammarParseTreeBuilder;
import org.openide.util.Pair;

/**
 * Resolves a css property value against its grammar.
 * 
 * @see PropertyDefinition for more information about the property grammar.
 *
 * @author marekfukala
 */
public class GrammarResolver {

    //logs types
    public static enum Log {

        DEFAULT,
        VALUES,
        ALTERNATIVES
        
    }
    
    static final Map<Log, AtomicBoolean> LOGGERS = new EnumMap<>(Log.class);

    static {
        for (Log log : Log.values()) {
            LOGGERS.put(log, new AtomicBoolean(false));
        }
    }

    public static void setLogging(Log log, boolean enable) {
        LOGGERS.get(log).set(enable);

        //set general LOG flag
        LOG = false;
        for (Log l : Log.values()) {
            if (isLoggingEnabled(l)) {
                LOG = true;
            }
        }
    }

    private static boolean isLoggingEnabled(Log log) {
        return LOGGERS.get(log).get();
    }
    private static boolean LOG = false;
    private static final Logger LOGGER = Logger.getLogger(GrammarResolver.class.getName());

    
    public static enum Feature {
        /**
         * The resulting parse tree WILL contain the anonymous grammar rules.
         * Default behavior is not to put such nodes in the parse tree.
         */
        keepAnonymousElementsInParseTree
    }
    
    public static GrammarResolverResult resolve(GroupGrammarElement grammar, CharSequence input) {
        return new GrammarResolver(grammar).resolve(input);
    }
    
    private List<ResolvedToken> resolvedTokens;
    private Tokenizer tokenizer;
    
    //keys are elements which matched the input, values are pairs of InputState 
    //in the time of the match and collection of possible values which may follow
    //the matched element
    private Map<GrammarElement, Pair<InputState, Collection<ValueGrammarElement>>> resolvedSomething;
    private GrammarElement lastResolved;


    private final Collection<GrammarResolverListener> LISTENERS  = new ArrayList<>();
    
    private final GroupGrammarElement grammar;
    
    private final Map<Feature, Object> FEATURES = new EnumMap<>(Feature.class);

    private ValueGrammarElement[] globalValues;

    public GrammarResolver(GroupGrammarElement grammar) {
        this.grammar = grammar;
        this.globalValues = new ValueGrammarElement[]{
            new FixedTextGrammarElement(this.grammar, "inherit", "inherit"),
            new FixedTextGrammarElement(this.grammar, "initial", "initial"),
            new FixedTextGrammarElement(this.grammar, "unset", "unset")
        };
    }
    
    //the grammar resolve has its internal state so this method needs to be synchronized
    //if the class is supposed to be thread-safe.
    public synchronized GrammarResolverResult resolve(CharSequence input) {
        //reset internal state
        resolvedTokens =  new ArrayList<>();
        resolvedSomething = new LinkedHashMap<>();
        lastResolved = null;
        tokenizer = new Tokenizer(input);
        
        GrammarParseTreeBuilder parseTreeBuilder = new GrammarParseTreeBuilder();
        addGrammarResolverListener(parseTreeBuilder);
        
        fireStarting();
        
        groupMemberResolved(grammar, grammar, createInputState(), true);

        boolean inputResolved = resolve(grammar);

        if (tokenizer.moveNext()) {
            //the main element resolved, but something left in the input -- fail
            inputResolved = false;
        }

        resolvingFinished();
        
        fireFinished();
        
        removeGrammarResolverListener(parseTreeBuilder);
        
        boolean skipAnonymousElements = !isFeatureEnabled(Feature.keepAnonymousElementsInParseTree);
        
        Node rootNode = skipAnonymousElements 
                ? GrammarParseTreeConvertor.createParseTreeWithOnlyNamedNodes(parseTreeBuilder.getParseTree())
                : parseTreeBuilder.getParseTree();
        
        return new GrammarResolverResult(tokenizer, inputResolved, resolvedTokens, getAlternatives(), rootNode);
        
    }
    
    public void setFeature(Feature feature, Object value) {
        FEATURES.put(feature, value);
    }
    
    public void enableFeature(Feature feature) {
        FEATURES.put(feature, Boolean.TRUE);
    }
    
    public void disableFeture(Feature feature) {
        FEATURES.put(feature, null);
    }
    
    public Object getFeature(Feature name) {
        return FEATURES.get(name);
    }
    
    private boolean isFeatureEnabled(Feature name) {
        //just presence of the key and non-null value means enabled
        return getFeature(name) != null;
    }
    
    public void addGrammarResolverListener(GrammarResolverListener listener) {
        LISTENERS.add(listener);
    }
    
    public void removeGrammarResolverListener(GrammarResolverListener listener) {
        LISTENERS.remove(listener);
    }
    
    private void fireEntering(GroupGrammarElement group) {
        for(GrammarResolverListener listener : LISTENERS) {
            listener.entering(group);
        }
    }
    
    private void fireEntering(ValueGrammarElement value) {
        for(GrammarResolverListener listener : LISTENERS) {
            listener.entering(value);
        }
    }
    
    private void fireExited(GroupGrammarElement group, boolean accepted) {
        for(GrammarResolverListener listener : LISTENERS) {
            if(accepted) {
                listener.accepted(group);
            } else {
                listener.rejected(group);
            }
        }
    }
    
    private void fireExited(ValueGrammarElement value, ResolvedToken rt) {
        for(GrammarResolverListener listener : LISTENERS) {
            if(rt == null) {
                listener.rejected(value);
            } else {
                listener.accepted(value, rt);
            }
        }
    }
    
    private void fireRuleChoosen(GroupGrammarElement base, GrammarElement value) {
        for(GrammarResolverListener listener : LISTENERS) {
            listener.ruleChoosen(base, value);
        }
    }
    
    private void fireStarting() {
        for(GrammarResolverListener listener : LISTENERS) {
            listener.starting();
        }
    }
    
    private void fireFinished() {
        for(GrammarResolverListener listener : LISTENERS) {
            listener.finished();
        }
    }

    private void resolvingFinished() {
        if (isLoggingEnabled(DEFAULT)) {
            if (LOG) {
                log("\nResolved tokens:");
            }
            for (ResolvedToken rt : resolvedTokens) {
                if (LOG) {
                    log(rt.toString());
                }
            }
        }

        if (isLoggingEnabled(ALTERNATIVES)) {
            if (LOG) {
                log(ALTERNATIVES, "\nAlternatives:");
            }
            for (ValueGrammarElement e : getAlternatives()) {
                if (LOG) {
                    log(ALTERNATIVES, e.path());
                }
            }
        }
    }

    private InputState createInputState() {
        return new InputState();
    }

    private boolean equalsToCurrentState(InputState state) {
        return tokenizer.tokenIndex() == state.tokenIndex && resolvedTokens.equals(state.consumed);
    }

    private void backupInputState(InputState state) {
        if (equalsToCurrentState(state)) {
            //no need to backup the same state
            return;
        }

        tokenizer.move(state.tokenIndex);
        resolvedTokens = new ArrayList<>(state.consumed);

        if (LOG) {
            log(String.format("  state backup to: %s", state));
        }
    }

    private boolean resolve(GrammarElement e) {
        if (LOG) {
            log(String.format("+ entering %s, %s", e.path(), createInputState()));
        }
        boolean resolves;
        if(e instanceof GroupGrammarElement) {
                GroupGrammarElement group = (GroupGrammarElement) e;
                fireEntering(group);
                resolves = processGroup(group);
                if (!resolves && (group == this.grammar && tokenizer.tokenIndex() == -1)) {
                    InputState beforeState = createInputState();
                    List<ValueGrammarElement> matching = new ArrayList<>(globalValues.length);
                    for(ValueGrammarElement vge: globalValues) {
                        if(processValue(vge)) {
                            matching.add(vge);
                        } else {
                            valueNotAccepted(vge);
                        }
                        backupInputState(beforeState);
                    }
                    if(! matching.isEmpty()) {
                        boolean matched = processValue(matching.get(0));
                        assert matched;
                        resolves = true;
                    }
                }
                fireExited(group, resolves);
        } else if(e instanceof ValueGrammarElement) {
                ValueGrammarElement value = (ValueGrammarElement) e;
                fireEntering(value);
                resolves = processValue(value);
                fireExited(value, resolves ? resolvedTokens.get(resolvedTokens.size() - 1) : null);
        } else {
            throw new IllegalStateException();
        }
        if (LOG) {
            log(String.format("- leaving %s, resolved: %s, %s", e.path(), resolves, createInputState()));
        }
        return resolves;

    }
    //alternatives computation >>>
    //

    private void valueNotAccepted(ValueGrammarElement valueGrammarElement) {
        if (resolvedTokens.size() < tokenizer.tokensCount()) {
            //ignore such alternatives, we need to find alts after all input tokens are resolved
            return;
        }

        if (LOG) {
            log(ALTERNATIVES, String.format("value not accepted %s, %s", valueGrammarElement.path(), createInputState()));
        }

        Pair<InputState, Collection<ValueGrammarElement>> pair = resolvedSomething.get(lastResolved);
        pair.second().add(valueGrammarElement);
    }

    private void groupMemberResolved(GrammarElement member, GroupGrammarElement group, InputState state, boolean root) {
        if (!root && (state.consumed.size() < tokenizer.tokensCount())) {
            //ignore such alternatives, we need to find alts after all input tokens are resolved
            return;
        }

        if (LOG) {
            log(ALTERNATIVES, String.format("input matched %s, %s", member.path(), state));
        }
        resolvedSomething.put(group, Pair.<InputState, Collection<ValueGrammarElement>>of(state, new LinkedList<ValueGrammarElement>()));
        lastResolved = group;
    }

    private Set<ValueGrammarElement> getAlternatives() {
        HashSet<ValueGrammarElement> alternatives = new HashSet<>();
        for (Pair<InputState, Collection<ValueGrammarElement>> tri : resolvedSomething.values()) {
            for (ValueGrammarElement value : tri.second()) {
                alternatives.add(value);
            }
        }
        return alternatives;
    }

    private boolean processGroup(GroupGrammarElement group) {
        //resolve all group members
        InputState successState = null;
        InputState enteringGroupState = createInputState();
        multiplicity:
        for (int i = 0; i < group.getMaximumOccurances(); i++) {
            InputState atMultiplicityLoopStartState = createInputState();
            
            if (LOG) {
                if(i > 0) {
                    log(String.format("  multiplicity loop %s, %s", i, atMultiplicityLoopStartState));
                }
            }
            
            Collection<GrammarElement> grammarElementsToProcess = new ArrayList<>(group.elements());
            
            //remember the grammar elements to process for the ALL and COLLECTION branch alternatives 
            Collection<GrammarElement> branchAlternativesGrammarElementsToProcess = null;

            //ALL and COLLECTIOn group: when multiple branches consumed similar input then we need to 
            //try to resolve the whole group using each of them
            Set<GrammarElement> alreadyTriedAlternativeBranches = new HashSet<>();
            
            Map<GrammarElement, InputState> branchesResults =
                    new HashMap<>();

            collection_loop:
            for (;;) { //try to loop until the LIST group is resolved fully (or not at all)
                InputState atCollectionLoopStartState = createInputState();
                members:
                for (Iterator<GrammarElement> membersIterator = grammarElementsToProcess.iterator(); membersIterator.hasNext();) {
                    GrammarElement member = membersIterator.next();
                    boolean resolved = resolve(member);

                    if (LOG) {
                        log(String.format("  back in %s", group.path()));
                    }

                    if (!resolved) {
                        if (member instanceof ValueGrammarElement) {
                            valueNotAccepted((ValueGrammarElement) member);
                        }
                    }

                    if (resolved) {
                        InputState state = createInputState();

                        groupMemberResolved(member, group, state, false);

                        //member resolved some input
                        switch (group.getType()) {
                            case SET:
                            case COLLECTION:
                            case ALL:
                                if (LOG) {
                                    log(String.format("  added %s branch result: %s, %s", group.getType().name(), member.path(), state));
                                }
                                branchesResults.put(member, state);
                                backupInputState(enteringGroupState);
                                break;
                            case LIST:
                                if (!membersIterator.hasNext()) {
                                    //the resolved element was the last one from the LIST so the group si resolved
                                    successState = state;
                                    break collection_loop;
                                }
                                break;
                        }

                    } else if (member.isOptional()) {
                        //the member hasn't resolved any input but it is optional
                        InputState state = createInputState();

                        if (LOG) {
                            log(String.format("  arbitrary member %s skipped", member.path()));
                        }

                        switch (group.getType()) {
                            case SET:
                            case COLLECTION:
                                if (LOG) {
                                    log(String.format(" added %s branch result: %s, %s", group.getType().name(), member, state));
                                }
                                branchesResults.put(member, state);
                                backupInputState(enteringGroupState);
                                break;
                            case ALL:
                                if (LOG) {
                                    log(String.format(" added %s branch result: %s, %s", group.getType().name(), member, state));
                                }
                                branchesResults.put(member, state);
                                backupInputState(atCollectionLoopStartState);
                                break;
                            case LIST:
                                if (!membersIterator.hasNext()) {
                                    //the resolved element was the last one from the LIST so the group si resolved
                                    successState = state;
                                    break collection_loop;
                                }
                                break;
                        }

                    } else {
                        //member doesn't resolve
                        switch (group.getType()) {
                            case LIST:
                                //failure, cannot resolve the member and it is mandatory
                                //so we are sure the group cannot be resolved
                                break multiplicity;

                            case ALL:
                            case COLLECTION:
//                                grammarElementsToProcess.remove(member);
                            case SET:
                                //the failure of resolving this member doesn't
                                //necessarily mean the group element cannot be resolved ... continue resolving
                                break;
                        }
                    }
                } //members

                switch (group.getType()) {
                    case SET:
                    case COLLECTION:
                    case ALL:
                        //process branches results - find longest match
                        if(branchesResults.isEmpty()) {
                            //no success branch result
                            break;
                        }
                        
                        //find best match length first
                        int inputLenBeforeEnteringGroupElement = enteringGroupState.consumed.size();
                        int bestMatchConsumed = inputLenBeforeEnteringGroupElement;
                        for (InputState state : branchesResults.values()) {
                            if (bestMatchConsumed < state.consumed.size()) {
                                bestMatchConsumed = state.consumed.size();
                            }
                        }
                        if (LOG) {
                            log(String.format("  resolving best branch (consumed %s tokens)", bestMatchConsumed - inputLenBeforeEnteringGroupElement));
                        }
                        if (bestMatchConsumed == inputLenBeforeEnteringGroupElement) {
                            //nothing resolved, but this still may mean that some of the branches
                            //matched since they are arbitrary. In such case we cannot resolve 
                            //which branch is the best so just take the first one
                            Entry<GrammarElement, InputState> entry = branchesResults.entrySet().iterator().next();
                            successState = entry.getValue();
                            //put the state of the best match back
                            backupInputState(successState);
                            if (LOG) {
                                log(String.format("  zero tokens consumed, but decided to use arbitraty branch %s, %s", entry.getKey().path(), successState));
                            }
                            
                            break;
                        }

                        //collect all branches which matched the bestMatchConsumed and compare the
                        //resolved tokens. If in one step one branch consumed keyword (static element name)
                        //and the other resolved a property acceptor then the keyword one has a precendence.
                        Map<GrammarElement, InputState> bestBranches = new LinkedHashMap<>();
                        for (GrammarElement member : group.elements()) {
                            InputState state = branchesResults.get(member);
                            if (state == null) {
                                //matched nothing
                                continue;
                            }
                            if (state.consumed.size() == bestMatchConsumed) {
                                bestBranches.put(member, state);
                            }
                        }

                        //now compare the branches
                        //compare just the parts consumed during this group element resolving
                        for (int j = inputLenBeforeEnteringGroupElement; j < bestMatchConsumed; j++) {
                            Collection<GrammarElement> consumedUnit = new LinkedList<>();
                            for (Entry<GrammarElement, InputState> entry : bestBranches.entrySet()) {
                                ResolvedToken token = entry.getValue().consumed.get(j);
                                if (token.getGrammarElement() instanceof UnitGrammarElement) {
                                    //unit value
                                    consumedUnit.add(entry.getKey());
                                }
                            }
                            if (consumedUnit.size() == bestBranches.size()) {
                                //all branches consumed units, go on with all of them
                            } else {
                                //some branch/es consumed keyword while other/s units,
                                //remove the unit ones from the bestBranches list
                                for (GrammarElement ge : consumedUnit) {
                                    bestBranches.remove(ge);
                                }
                            }

                        }

                        assert !bestBranches.isEmpty();

                        //set the success state to the best branch (consumed most input)
                        
                        if (bestBranches.size() > 1) {
                            //there're more branches consumed the same input length - we need to decide which one to use
                            if(LOG) {
                                log(String.format("! more branches (%s) which consumed same input lenght found!", bestBranches.size()));
                            }
                            
                            //try to continue in the group processing accepting each of the alternative branch
                            for (Entry<GrammarElement, InputState> entry : bestBranches.entrySet()) {
                                // <editor-fold defaultstate="collapsed" desc="Logging">  
                                if(LOG) {
                                    log(String.format("\t%s, %s %s",
                                            entry.getKey(),
                                            entry.getValue(),
                                            alreadyTriedAlternativeBranches.contains(entry.getKey()) ? "(tried)" : ""));
                                }
                                // </editor-fold>
                                if (branchAlternativesGrammarElementsToProcess == null) {
                                    //first alternative attempt - remember the set of unprocessed grammar elements 
                                    //so we can reset it back for the next alternative processing
                                    // <editor-fold defaultstate="collapsed" desc="Logging">  
                                    if(LOG) {
                                        StringBuilder b = new StringBuilder();
                                        b.append("  saving grammar elements to process: ");
                                        for (GrammarElement e : grammarElementsToProcess) {
                                            b.append(e);
                                            b.append(',');
                                        }
                                        log(b.toString());
                                    }
                                // </editor-fold>
                                    branchAlternativesGrammarElementsToProcess = new ArrayList<>(grammarElementsToProcess);
                                }
                            }
                        }
                        
                        GrammarElement bestMatchElement = null;
                        for (GrammarElement alternative : bestBranches.keySet()) {
                            if (!alreadyTriedAlternativeBranches.contains(alternative)) {
                                //not tried alternative yet - lets give it a try
                                bestMatchElement = alternative;
                                break;

                            } else {
                                //already tried alternative -- skip
                            }
                        }

                        //set the bestMatchElement if there's just one alternative EVEN IF the alternative has been already tried
                        //(the previous block of code will not set bestMatchElement)
                        if (bestBranches.size() == 1) {
                            bestMatchElement = bestBranches.keySet().iterator().next();
                        }

                        if (bestMatchElement == null) {
                            //all alternative tried, no success
                            if(LOG) {
                                log(String.format("! all %s alternative branches tried", bestBranches.size()));
                            }
                        } else {
                            
                            if (bestBranches.size() > 1) {
                                //if more alternatives and this is not the first attempt, reset the grammarElementsToProcess to the
                                //state before the first alternative try
                                if (branchAlternativesGrammarElementsToProcess != null) {
                                    // <editor-fold defaultstate="collapsed" desc="Logging">  
                                    if(LOG) {
                                        StringBuilder b = new StringBuilder();
                                        b.append("  restoring grammar elements to process: ");
                                        for (GrammarElement e : branchAlternativesGrammarElementsToProcess) {
                                            b.append(e);
                                            b.append(',');
                                        }
                                        log(b.toString());
                                    }
                                    // </editor-fold>
                                    grammarElementsToProcess = new ArrayList<>(branchAlternativesGrammarElementsToProcess);
                                }
                            }

                            //remember we have tried this alternative
                            alreadyTriedAlternativeBranches.add(bestMatchElement);

                            successState = branchesResults.get(bestMatchElement);
                            if(LOG) {
                                log(String.format("  decided to use best match %s, %s", bestMatchElement.path(), successState));
                            }

                            fireRuleChoosen(group, bestMatchElement);
                            
                            //put the state of the best match back
                            backupInputState(successState);

                            //if we are in a COLLECTION or ALL, we need to remove 
                            //the choosen member from the further collection processing
                            switch (group.getType()) {
                                case COLLECTION:
                                case ALL:
                                    grammarElementsToProcess.remove(bestMatchElement);
                            }
                        }

                        break;
                }

                if(successState == null) {
                    break collection_loop;
                }
                
                if(successState.equals(atCollectionLoopStartState)) {
                    //nothing resolved in the loop
                    break collection_loop;
                }

                //loop if the grammar element is a collection or all otherwise leave
                switch (group.getType()) {
                    case SET:
                    case LIST:
                        break collection_loop;
                    case COLLECTION:
                    case ALL:
                        //continue
                }

            } //:collection_loop

            switch(group.getType()) {
                case ALL:
                    //all members of the group must be resolved or optional                    
                    for(GrammarElement e : grammarElementsToProcess) {
                        if(!e.isOptional()) {
                            if(LOG) {
                                StringBuilder sb = new StringBuilder();
                                for(Iterator<GrammarElement> itr = grammarElementsToProcess.iterator(); itr.hasNext(); ) {
                                    sb.append(itr.next().path());
                                    if(itr.hasNext()) {
                                        sb.append(", ");
                                    }
                                }
                                log(String.format("  all group: exited collection_loop but there are some grammar element to process left: %s", sb.toString()));
                            }
                            backupInputState(atMultiplicityLoopStartState);
                            return false;
                        }
                    }
                    
                    break;
            }
            
            //we went through the first iteration of members (multiplicity 0 and more)
            //but nothing resolved so making another multiplicity loop makes no sense
            if (successState == null) {
                break multiplicity;
            }
            
            if(successState.equals(atMultiplicityLoopStartState)) {
                //nothing resolved in the loop
                break multiplicity;
            }

        } //multiplicity loop

        if (successState == null) {
            //nothing from the group resolved, backup the input before leaving
            backupInputState(enteringGroupState);
            return false;
        } else {
            //the group is resolved, backup the last successful state
            //the backup must be here since the multiplicity loop may fail, but
            //still the group is resolved
            backupInputState(successState);
            return true;
        }

    }

    private boolean processValue(ValueGrammarElement ve) {
        if(!tokenizer.moveNext()) {
            return false; //eof
        }
        
        Token token = tokenizer.token();
        if(ve.accepts(token)) {
            //consumed
            consumeValueGrammarElement(token, ve);
            if (LOG) {
                log(VALUES, String.format("eaten unit %s", token));
            }
            return true;
        } else {
            //backup the read token
            tokenizer.movePrevious();

            return false;
        }
    }

    private void consumeValueGrammarElement(Token token, ValueGrammarElement element) {
        resolvedTokens.add(new ResolvedToken(token, element));
    }

    private void log(String text) {
        log(DEFAULT, text);
    }

    private void log(Log log, String text) {
        if (isLoggingEnabled(log)) {
            System.out.println(text);
        }
    }

    private class InputState {

        private int tokenIndex;
        private final List<ResolvedToken> consumed;

        public InputState() {
            this.tokenIndex = tokenizer.tokenIndex();
            this.consumed = new ArrayList<>(GrammarResolver.this.resolvedTokens);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            //resolved part
            sb.append('[');
            for (Iterator<ResolvedToken> i = consumed.iterator(); i.hasNext();) {
                ResolvedToken rt = i.next();
                sb.append(rt.token());
                if (i.hasNext()) {
                    sb.append(' ');
                }
            }
            sb.append(']');
            sb.append(' ');

            //unresolved part
            List<Token> input = tokenizer.tokensList();
            for (int i = input.size() - 1; i >= 0; i--) {
                sb.append(input.get(i));
                if (i > 0) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InputState other = (InputState) obj;
            if (this.tokenIndex != other.tokenIndex) {
                return false;
            }
            if (this.consumed != other.consumed && (this.consumed == null || !this.consumed.equals(other.consumed))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.tokenIndex;
            hash = 79 * hash + (this.consumed != null ? this.consumed.hashCode() : 0);
            return hash;
        }
        
        
    }
}
