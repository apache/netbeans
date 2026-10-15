/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.hints.spiimpl.pm;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Name;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreeScanner;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.AdditionalQueryConstraints;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class NFABasedBulkSearch extends BulkSearch {

    public NFABasedBulkSearch() {
        super(false);
    }

    @Override
    public Map<String, Collection<TreePath>> match(CompilationInfo info, final AtomicBoolean cancel, TreePath tree, BulkPattern patternIn, Map<String, Long> timeLog) {
        BulkPatternImpl pattern = (BulkPatternImpl) patternIn;
        
        final Map<Res, Collection<TreePath>> occurringPatterns = new HashMap<>();
        final NFA<Input, Res> nfa = pattern.toNFA();
        final Set<String> identifiers = new HashSet<>();

        new CollectIdentifiers<Void, TreePath>(identifiers, cancel) {
            private NFA.State active = nfa.getStartingState();
            @Override
            public Void scan(Tree node, TreePath p) {
                if (node == null) {
                    return null;
                }

                TreePath currentPath = new TreePath(p, node);
                boolean[] goDeeper = new boolean[1];
                final NFA.State newActiveAfterVariable = nfa.transition(active, new Input(Kind.IDENTIFIER, "$", false));
                Input normalizedInput = normalizeInput(node, goDeeper, null);
                boolean ignoreKind = normalizedInput.kind == Kind.IDENTIFIER || normalizedInput.kind == Kind.MEMBER_SELECT;

                NFA.State newActiveBefore = nfa.transition(active, normalizedInput);

                if (normalizedInput.name != null && !ignoreKind) {
                    newActiveBefore = nfa.join(newActiveBefore, nfa.transition(active, new Input(normalizedInput.kind, "$", false)));
                }

                active = newActiveBefore;

                if (goDeeper[0]) {
                    super.scan(node, currentPath);
                } else {
                    new CollectIdentifiers<Void, Void>(identifiers, cancel).scan(node, null);
                }
                
                if (cancel.get()) return null;

                NFA.State newActiveAfter = nfa.transition(active, UP);

                active = nfa.join(newActiveAfter, nfa.transition(newActiveAfterVariable, UP));

                for (Res r : nfa.getResults(active)) {
                    addOccurrence(r, currentPath);
                }

                return null;
            }

            @Override
            public Void scan(Iterable<? extends Tree> nodes, TreePath p) {
                active = nfa.transition(active, new Input(Kind.IDENTIFIER, "(", false));
                try {
                    return super.scan(nodes, p);
                } finally {
                    active = nfa.transition(active, UP);
                }
            }
            
            private void addOccurrence(Res r, TreePath currentPath) {
                occurringPatterns.computeIfAbsent(r, k -> new LinkedList<>())
                                 .add(currentPath);
            }
        }.scan(tree, tree.getParentPath());

        if (cancel.get()) return null;
        
        Map<String, Collection<TreePath>> result = new HashMap<>();

        for (Entry<Res, Collection<TreePath>> e : occurringPatterns.entrySet()) {
            if (cancel.get()) return null;
            if (!identifiers.containsAll(pattern.getIdentifiers().get(e.getKey().patternIndex))) {
                continue;
            }

            result.put(e.getKey().pattern, e.getValue());
        }

        return result;
    }

    @Override
    public BulkPattern create(Collection<? extends String> code, Collection<? extends Tree> patterns, Collection<? extends AdditionalQueryConstraints> additionalConstraints, final AtomicBoolean cancel) {
        int startState = 0;
        final int[] nextState = new int[] {1};
        final Map<NFA.Key<Input>, NFA.State> transitionTable = new LinkedHashMap<>();
        Map<Integer, Res> finalStates = new HashMap<>();
        List<Set<? extends String>> identifiers = new LinkedList<>();
        List<List<List<String>>> requiredContent = new ArrayList<>();
        Iterator<? extends String> codeIt = code.iterator();
        int patternIndex = 0;

        for (final Tree pattern : patterns) {
            final int[] currentState = new int[] {startState};
            final Set<String> patternIdentifiers = new HashSet<>();
            final List<List<String>> content = new ArrayList<>();

            identifiers.add(patternIdentifiers);
            requiredContent.add(content);

            @SuppressWarnings("NestedAssignment")
            class Scanner extends CollectIdentifiers<Void, Void> {
                public Scanner() {
                    super(patternIdentifiers, cancel);
                }
                private boolean auxPath;
                private List<String> currentContent;
                {
                    content.add(currentContent = new ArrayList<>());
                }
                @Override
                public Void scan(Tree t, Void v) {
                    if (t == null) {
                        return null;
                    }

                    if (Utilities.isMultistatementWildcardTree(t) || multiModifiers(t)) {
                        int target = nextState[0]++;

                        setBit(transitionTable, new NFA.Key(currentState[0], new Input(Kind.IDENTIFIER, "$", false)), target);
                        setBit(transitionTable, new NFA.Key(target, UP), currentState[0]);

                        content.add(currentContent = new ArrayList<>());
                        
                        return null;
                    }

                    if (t.getKind() == Kind.BLOCK) {
                        StatementTree singletonStatement = null;
                        BlockTree bt = (BlockTree) t;

                        if (!bt.isStatic()) {
                            switch (bt.getStatements().size()) {
                                case 1 -> singletonStatement = bt.getStatements().get(0);
                                case 2 -> {
                                    if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(0))) {
                                        singletonStatement = bt.getStatements().get(1);
                                    } else {
                                        if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(1))) {
                                            singletonStatement = bt.getStatements().get(0);
                                        }
                                    }
                                }
                                case 3 -> {
                                    if (Utilities.isMultistatementWildcardTree(bt.getStatements().get(0)) && Utilities.isMultistatementWildcardTree(bt.getStatements().get(2))) {
                                        singletonStatement = bt.getStatements().get(1);
                                    }
                                }
                            }
                        }

                        if (singletonStatement != null) {
                            int backup = currentState[0];

                            boolean oldAuxPath = auxPath;

                            auxPath = true;

                            scan(singletonStatement, null);

                            auxPath = oldAuxPath;

                            int target = currentState[0];

                            setBit(transitionTable, new NFA.Key(backup, new Input(Kind.BLOCK, null, false)), currentState[0] = nextState[0]++);
                            setBit(transitionTable, new NFA.Key(currentState[0], new Input(Kind.IDENTIFIER, "(", false)), currentState[0] = nextState[0]++);

                            for (StatementTree st : bt.getStatements()) {
                                scan(st, null);
                            }

                            setBit(transitionTable, new NFA.Key(currentState[0], UP), currentState[0] = nextState[0]++);
                            setBit(transitionTable, new NFA.Key(currentState[0], UP), target);
                            currentState[0] = target;

                            return null;
                        }
                    }
                    
                    boolean[] goDeeper = new boolean[1];
                    Input[] bypass = new Input[1];
                    Input i = normalizeInput(t, goDeeper, bypass);

                    if (!TO_IGNORE.contains(i.kind) && !auxPath) {
                        currentContent.add(kind2EncodedString.get(i.kind));
                    }

                    if (i.name != null && !auxPath) {
                        if (!"$".equals(i.name)) {
                            if (isIdentifierAcceptable(i.name)) {
                                currentContent.add(i.name);
                            }
                            if (Utilities.isPureMemberSelect(t, false)) {
                                content.add(currentContent = new ArrayList<>());
                            }
                        } else {
                            content.add(currentContent = new ArrayList<>());
                        }
                    }

                    int backup = currentState[0];

                    handleTree(i, goDeeper, t, bypass);

                    boolean oldAuxPath = auxPath;

                    auxPath = true;

                    if (StatementTree.class.isAssignableFrom(t.getKind().asInterface()) && t != pattern) {
                        int target = currentState[0];

                        setBit(transitionTable, new NFA.Key(backup, new Input(Kind.BLOCK, null, false)), currentState[0] = nextState[0]++);
                        setBit(transitionTable, new NFA.Key(currentState[0], new Input(Kind.IDENTIFIER, "(", false)), currentState[0] = nextState[0]++);
                        handleTree(i, goDeeper, t, bypass);
                        setBit(transitionTable, new NFA.Key(currentState[0], UP), currentState[0] = nextState[0]++);
                        setBit(transitionTable, new NFA.Key(currentState[0], UP), target);
                        currentState[0] = target;
                    }

                    auxPath = oldAuxPath;

                    return null;
                }

                @Override
                public Void scan(Iterable<? extends Tree> nodes, Void p) {
                    setBit(transitionTable, new NFA.Key(currentState[0], new Input(Kind.IDENTIFIER, "(", false)), currentState[0] = nextState[0]++);
                    try {
                        return super.scan(nodes, p);
                    } finally {
                        setBit(transitionTable, new NFA.Key(currentState[0], UP), currentState[0] = nextState[0]++);
                    }
                }

                private void handleTree(Input i, boolean[] goDeeper, Tree t, Input[] bypass) {
                    int backup = currentState[0];
                    int target = nextState[0]++;

                    setBit(transitionTable, new NFA.Key(backup, i), currentState[0] = nextState[0]++);

                    if (goDeeper[0]) {
                        super.scan(t, null);
                    } else {
                        new CollectIdentifiers<Void, Void>(patternIdentifiers, cancel).scan(t, null);
                        int aux = nextState[0]++;
                        setBit(transitionTable, new NFA.Key(backup, new Input(Kind.MEMBER_SELECT, i.name, false)), aux);
                        setBit(transitionTable, new NFA.Key(aux, new Input(Kind.IDENTIFIER, "$", false)), aux = nextState[0]++);
                        setBit(transitionTable, new NFA.Key(aux, UP), aux = nextState[0]++);
                        setBit(transitionTable, new NFA.Key(aux, UP), target);
                    }

                    setBit(transitionTable, new NFA.Key(currentState[0], UP), target);
                    
                    if (bypass[0] != null) {
                        int intermediate = nextState[0]++;
                        
                        setBit(transitionTable, new NFA.Key(backup, bypass[0]), intermediate);
                        setBit(transitionTable, new NFA.Key(intermediate, UP), target);
                    }
                    
                    currentState[0] = target;
                }
            }

            Scanner s = new Scanner();

            s.scan(pattern, null);

            finalStates.put(currentState[0], new Res(codeIt.next(), patternIndex++));
        }

        if (cancel.get()) return null;
        
        NFA<Input, Res> nfa = NFA.<Input, Res>create(startState, nextState[0], null, transitionTable, finalStates);

        return new BulkPatternImpl(new LinkedList<String>(code), identifiers, requiredContent, new LinkedList<>(additionalConstraints), nfa);
    }

    private static void setBit(Map<NFA.Key<Input>, NFA.State> transitionTable, NFA.Key<Input> input, int state) {
        transitionTable.computeIfAbsent(input, k -> new NFA.State())
                       .mutableOr(state);
    }

    private static Input normalizeInput(Tree t, boolean[] goDeeper, Input[] bypass) {
        if (t.getKind() == Kind.IDENTIFIER && ((IdentifierTree) t).getName().toString().startsWith("$")) {
            goDeeper[0] = false;
            return new Input(Kind.IDENTIFIER, "$", false);
        }

        if (Utilities.getWildcardTreeName(t) != null) {
            goDeeper[0] = false;
            return new Input(Kind.IDENTIFIER, "$", false);
        }
        
        if (t.getKind() == Kind.IDENTIFIER) {
            goDeeper[0] = false;
            String name = ((IdentifierTree) t).getName().toString();
            return new Input(Kind.IDENTIFIER, name, false);
        }

        if (t.getKind() == Kind.MEMBER_SELECT) {
            String name = ((MemberSelectTree) t).getIdentifier().toString();
            if (name.startsWith("$")) {
                goDeeper[0] = false;//???
                return new Input(Kind.IDENTIFIER, "$", false);
            }
            if (bypass != null && Utilities.isPureMemberSelect(t, true)) {
                bypass[0] = new Input(Kind.IDENTIFIER, name, false);
            }
            goDeeper[0] = true;
            return new Input(Kind.MEMBER_SELECT, name, false);
        }

        goDeeper[0] = true;

        String name;

        name = switch (t.getKind()) {
            case CLASS -> ((ClassTree)t).getSimpleName().toString();
            case VARIABLE -> ((VariableTree)t).getName().toString();
            case METHOD -> ((MethodTree)t).getName().toString();
            case BOOLEAN_LITERAL -> ((LiteralTree) t).getValue().toString();
            default -> null;
        };

        if (name != null) {
            if (!name.isEmpty() && name.charAt(0) == '$') {
                name = "$";
            }
        }
        return new Input(t.getKind(), name, false);
    }
    
    private boolean multiModifiers(Tree t) {
        if (t.getKind() != Kind.MODIFIERS) return false;
        
        List<AnnotationTree> annotations = new ArrayList<>(((ModifiersTree) t).getAnnotations());

        return !annotations.isEmpty() && annotations.get(0).getAnnotationType().getKind() == Kind.IDENTIFIER;
    }

    @Override
    public boolean matches(CompilationInfo info, AtomicBoolean cancel, TreePath tree, BulkPattern pattern) {
        //XXX: performance
        return !match(info, cancel, tree, pattern).isEmpty();
    }

    private static final Set<Kind> TO_IGNORE = EnumSet.of(Kind.BLOCK, Kind.IDENTIFIER, Kind.MEMBER_SELECT);

    @Override
    public void encode(Tree tree, final EncodingContext ctx, AtomicBoolean cancel) {
        final Set<String> identifiers = new HashSet<>();
        final List<String> content = new ArrayList<>();
        if (!ctx.isForDuplicates()) {
            new CollectIdentifiers<Void, Void>(identifiers, cancel).scan(tree, null);
            try {
                int size = identifiers.size();
                ctx.getOut().write((size >> 24) & 0xFF);
                ctx.getOut().write((size >> 16) & 0xFF);
                ctx.getOut().write((size >>  8) & 0xFF);
                ctx.getOut().write((size >>  0) & 0xFF);
                for (String ident : identifiers) {
                    ctx.getOut().write(ident.getBytes(StandardCharsets.UTF_8));//XXX: might probably contain ';'
                    ctx.getOut().write(';');
                }
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        if (cancel.get()) {
            return;
        }
        new CollectIdentifiers<Void, Void>(new HashSet<>(), cancel) {
            private boolean encode = true;
            @Override
            public Void scan(Tree t, Void v) {
                if (t == null) return null;

                if (t instanceof StatementTree && Utilities.isMultistatementWildcardTree((StatementTree) t)) {
                    return null;
                }

                boolean[] goDeeper = new boolean[1];

                Input i = normalizeInput(t, goDeeper, null);
                try {
                    ctx.getOut().write('(');
                    ctx.getOut().write(kind2Encoded.get(i.kind));
                    if (!TO_IGNORE.contains(i.kind)) {
                        content.add(kind2EncodedString.get(i.kind));
                    }
                    if (i.name != null) {
                        if (encode) {
                            ctx.getOut().write('$');
                            ctx.getOut().write(i.name.getBytes(StandardCharsets.UTF_8));
                            ctx.getOut().write(';');
                        }
                        if (isIdentifierAcceptable(i.name)) content.add(i.name);
                    }

                    boolean oldEncode = encode;

                    encode &= goDeeper[0];
                    super.scan(t, v);
                    encode = oldEncode;

                    ctx.getOut().write(')');
                } catch (IOException ex) {
                    //XXX
                    Exceptions.printStackTrace(ex);
                }

                return null;
            }
            @Override
            public Void scan(Iterable<? extends Tree> nodes, Void p) {
                try {
                    ctx.getOut().write('(');
                    ctx.getOut().write(kind2Encoded.get(Kind.IDENTIFIER));
                    ctx.getOut().write('$');
                    ctx.getOut().write('(');
                    ctx.getOut().write(';');
                    super.scan(nodes, p);
                    ctx.getOut().write(')');
                } catch (IOException ex) {
                    //XXX
                    Exceptions.printStackTrace(ex);
                }
                return null;
            }
        }.scan(tree, null);

        ctx.setIdentifiers(identifiers);
        ctx.setContent(content);
    }

    @Override
    public boolean matches(InputStream encoded, AtomicBoolean cancel, BulkPattern patternIn) {
        try {
            return !matchesImpl(encoded, cancel, patternIn, false).isEmpty();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    @Override
    public Map<String, Integer> matchesWithFrequencies(InputStream encoded, BulkPattern patternIn, AtomicBoolean cancel) {
        try {
            return matchesImpl(encoded, cancel, patternIn, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyMap();
        }
    }

    public Map<String, Integer> matchesImpl(InputStream encoded, AtomicBoolean cancel, BulkPattern patternIn, boolean withFrequencies) throws IOException {
        BulkPatternImpl pattern = (BulkPatternImpl) patternIn;
        final NFA<Input, Res> nfa = pattern.toNFA();
        Deque<NFA.State> skips = new ArrayDeque<>();
        NFA.State active = nfa.getStartingState();
        int identSize = 0;

        identSize = encoded.read();
        identSize = (identSize << 8) + encoded.read();
        identSize = (identSize << 8) + encoded.read();
        identSize = (identSize << 8) + encoded.read();

        Set<String> identifiers = new HashSet<>(2 * identSize);

        while (identSize-- > 0) {
            if (cancel.get()) return null;
            int read = encoded.read();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //read name:
            while (read != ';') {
                baos.write(read);
                read = encoded.read();
            }

            identifiers.add(baos.toString(StandardCharsets.UTF_8));
        }

        Map<String, Integer> patternsAndFrequencies = new HashMap<>();
        int read = encoded.read();
        
        while (read != (-1)) {
            if (cancel.get()) return null;
            if (read == '(') {
                read = encoded.read(); //kind

                Kind k = encoded2Kind.get((read << 8) + encoded.read());

                read = encoded.read();

                String name;

                if (read == '$') {
                    //XXX:
                    read = encoded.read();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    //read name:
                    while (read != ';') {
                        baos.write(read);
                        read = encoded.read();
                    }

                    read = encoded.read();
                    name = baos.toString(StandardCharsets.UTF_8);
                } else {
                    name = null;
                }
                
                final NFA.State newActiveAfterVariable = nfa.transition(active, new Input(Kind.IDENTIFIER, "$", false));
                Input normalizedInput = new Input(k, name, false);
                boolean ignoreKind = normalizedInput.kind == Kind.IDENTIFIER || normalizedInput.kind == Kind.MEMBER_SELECT;

                NFA.State newActive = nfa.transition(active, normalizedInput);

                if (normalizedInput.name != null && !ignoreKind) {
                    newActive = nfa.join(newActive, nfa.transition(active, new Input(k, "$", false)));
                }

                active = newActive;

                skips.push(newActiveAfterVariable);
            } else {
                NFA.State newActiveAfterVariable = skips.pop();
                NFA.State newActive = nfa.transition(active, UP);
                NFA.State s2 = nfa.transition(newActiveAfterVariable, UP);

                active = nfa.join(newActive, s2);
                
                for (Res res : nfa.getResults(active)) {
                    if (identifiers.containsAll(pattern.getIdentifiers().get(res.patternIndex))) {
                        if (!withFrequencies) {
                            patternsAndFrequencies.put(res.pattern, 1);
                            return patternsAndFrequencies;
                        }
                        
                        Integer freqs = patternsAndFrequencies.get(res.pattern);

                        if (freqs == null) freqs = 0;

                        patternsAndFrequencies.put(res.pattern, freqs + 1);
                    }
                }

                read = encoded.read();
            }
        }

        return patternsAndFrequencies;
    }

    private static final Map<Kind, byte[]> kind2Encoded;
    private static final Map<Kind, String> kind2EncodedString;
    private static final Map<Integer, Kind> encoded2Kind;

    static {
        kind2Encoded = new EnumMap<>(Kind.class);
        kind2EncodedString = new EnumMap<>(Kind.class);
        encoded2Kind = new HashMap<>();

        for (Kind k : Kind.values()) {
            String enc = Integer.toHexString(k.ordinal());

            if (enc.length() < 2) {
                enc = "0" + enc;
            }

            final byte[] bytes = enc.getBytes(StandardCharsets.UTF_8);

            assert bytes.length == 2;

            kind2Encoded.put(k, bytes);
            kind2EncodedString.put(k, enc);

            encoded2Kind.put((bytes[0] << 8) + bytes[1], k);

        }
    }

    public static class BulkPatternImpl extends BulkPattern {

        private final NFA<Input, Res> nfa;

        private BulkPatternImpl(List<? extends String> patterns, List<? extends Set<? extends String>> identifiers,
                List<List<List<String>>> requiredContent, List<AdditionalQueryConstraints> additionalConstraints, NFA<Input, Res> nfa) {
            super(patterns, identifiers, requiredContent, additionalConstraints);
            this.nfa = nfa;
        }

        NFA<Input, Res> toNFA() {
            return nfa;
        }
        
    }

    private record Res(String pattern, int patternIndex) {}

    private record Input(Kind kind, String name, boolean end) {
        @Override
        public String toString() {
            return kind + ", " + name + ", " + end;
        }
    }

    private static final Input UP = new Input(null, null, true);

    private static boolean isIdentifierAcceptable(CharSequence content) {
        if (content.isEmpty() || content.charAt(0) == '$' || content.charAt(0) == '<') {
            return false;
        }
        String stringValue = content.toString();
        return !(stringValue.contentEquals("java") || "lang".equals(stringValue));
    }

    private static class CollectIdentifiers<R, P> extends CancellableTreeScanner<R, P> {

        private final Set<String> identifiers;

        public CollectIdentifiers(Set<String> identifiers, AtomicBoolean cancel) {
            super(cancel);
            this.identifiers = identifiers;
        }

        private void addIdentifier(Name ident) {
            if (!isIdentifierAcceptable(ident)) return;
            identifiers.add(ident.toString());
        }

        @Override
        public R visitMemberSelect(MemberSelectTree node, P p) {
            addIdentifier(node.getIdentifier());
            return super.visitMemberSelect(node, p);
        }

        @Override
        public R visitIdentifier(IdentifierTree node, P p) {
            addIdentifier(node.getName());
            return super.visitIdentifier(node, p);
        }

        @Override
        public R visitClass(ClassTree node, P p) {
            if (node.getSimpleName().length() == 0) {
                return scan(Utilities.filterHidden(null, node.getMembers()), p);
            }
            addIdentifier(node.getSimpleName());
            return super.visitClass(node, p);
        }

        @Override
        public R visitMethod(MethodTree node, P p) {
            addIdentifier(node.getName());
            return super.visitMethod(node, p);
        }

        @Override
        public R visitVariable(VariableTree node, P p) {
            addIdentifier(node.getName());
            return super.visitVariable(node, p);
        }

    }
}
