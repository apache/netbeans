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

package org.netbeans.modules.languages.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Rule;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.T;


/**
 *
 * @author Jan Jancura
 */
class First {

    
    static First create (
        List<Rule>          rules, 
        Language            language
    ) throws ParseException {
        return new First (language, rules);
    }

    private Language        language;
    private List<Rule>      rules;
    private F[]             maps;
    private Fl[]            follow;
    
    
    private First (
        Language            language, 
        List<Rule>          rules
    ) throws ParseException {
        this.language = language;
        this.rules = rules;
        compute ();
    }
    
    private void compute () throws ParseException {
        Map<String,List<Integer>> ntToIndexes = new HashMap<String,List<Integer>> ();
        int i, k = rules.size ();
        for (i = 0; i < k; i++) {
            Rule cr = rules.get (i);
            String nt = cr.getNT ();
            List<Integer> l = ntToIndexes.get (nt);
            if (l == null) {
                l = new ArrayList<Integer> ();
                ntToIndexes.put (nt, l);
            }
            l.add (new Integer (i));
            language.getNTID (nt);
        }
//        follow = new Fl [language.getNTCount ()];
//                                                                                int followCount = 0;
//        for (i = 0; i < k; i++) {
//            Rule cr = rules.get (i);
//            String nt = cr.getNT ();
//            List right = cr.getRight ();
//            int ntId = language.getNTID (nt);
//            Iterator it = right.iterator ();
//            for (int j = 0; j < right.size (); j++) {
//                Object item = it.next ();
//                if (item instanceof String) {
//                    int ntId2 = language.getNTID ((String) item);
//                    if (follow [ntId2] == null)
//                        follow [ntId2] = new Fl ();
//                    follow [ntId2].fl.add (new int[] {i, j + 1});
//                    followCount++;
//                }
//            }
//
//        }
//                                                                                //S ystem.out.println("Follow (" + followCount + "):\n" + printFollow ());
//        int followCount2 = optimizeFollow ();
//                                                                                //S ystem.out.println("\n\n\nFollow2 (" + followCount + ":" + followCount2 + "):\n" + printFollow ());
        int maxDepth = 3;
        maps = new F [language.getNTCount ()];
        
        Iterator<String> it = ntToIndexes.keySet ().iterator ();
        while (it.hasNext ()) {
            String nt = it.next ();
            F firstForNT = new F ();
            int count = 0;
            int ntid = language.getNTID (nt);
            maps [ntid] = firstForNT;
            List<Integer> indexes = ntToIndexes.get (nt);
            for (int depthLimit = 1; depthLimit <= maxDepth; depthLimit++) {
                boolean changed = false;
//                System.out.println("nt: " + nt + " depth: " + depthLimit);
                Iterator<Integer> it3 = indexes.iterator ();
                while (it3.hasNext ()) {
                    int ruleIndex = it3.next ();
                    
                    Stack<String> ntPath = new Stack<String> ();
                    ntPath.push (nt);
                    Set<String> pathSet = new HashSet<String> ();
                    pathSet.add (nt);
                    
                    changed |= first (
                        rules.get (ruleIndex).getRight (), 
                        0, 
                        ruleIndex, 
                        depthLimit, 
                        firstForNT, 
                        ntToIndexes, 
                        new Stack<List> (), 
                        pathSet,
                        ntPath,
                        ntid,
                        new HashSet<Integer> (),
  //                      new Debug (nt + "=" + rules.get (ruleIndex).getRight ()),
                        new int[] {count}
                    );
                }
                if (!changed) break;
                if (depthLimit == maxDepth) {
                    String conflict = findConflict (firstForNT, depthLimit);
                    if (conflict != null) {
//                        Thread.dumpStack();
//                        System.out.println (firstForNT);
                        System.out.println ("Conflict: " + conflict);
                        //throw new ParseException ("Can not resolve first set for " + nt + ".\n Conflicting input: " + conflict);
                    }
                }
            }
            //AnalyserAnalyser.printF (f, null);
        }
        for (int j = 0; j < maps.length; j++) {
            if (maps [j] != null)
                maps [j] = s (maps [j]);
        }
    }
    
    private boolean first (
        List                        rightSide, 
        int                         indexInRightSide, 
        Integer                     ruleIndex, 
        int                         depthLimit, 
        F                           firstForNT, 
        Map<String,List<Integer>>   ntToIndexes, 
        Stack<List>                 rightSidesStack, 
        Set<String>                 absNTSet,
        Stack<String>               ntPath,
        int                         fNT,
        Set<Integer>                followABS,
//        Debug                       debug,
        int[]                       count
    ) throws ParseException {
//        if (debug.messages.size () > 300) {
//            System.out.println(debug);
//            throw new ParseException ();
//        }
        if (firstForNT.amp == null)
            firstForNT.amp = new HashSet<Integer> ();
        firstForNT.amp.add (ruleIndex);
        
        if (rightSide.size () <= indexInRightSide) {
            if (rightSidesStack.empty ()) {
                if (firstForNT.hash == null)
                    firstForNT.hash = new HashSet<Integer> ();
                firstForNT.hash.add (ruleIndex);
                return false;
                
//                if (followABS.contains (fNT))
//                    return false;
//                followABS.add (fNT);
//                
//                Fl fl = follow [fNT];
//                if (fl == null) return false;
//                boolean r = false;
//                Iterator<int[]> it = fl.fl.iterator ();
//                while (it.hasNext ()) {
//                    int[] is = it.next ();
//                    Rule newRule = rules.get (is [0]);
//                    int newNTId = language.getNTID (newRule.getNT ());
//                    r |= first (
//                        newRule.getRight (), 
//                        is [1], 
//                        ruleIndex, 
//                        depthLimit, 
//                        firstForNT, 
//                        ntToIndexes, 
//                        rightSidesStack, 
//                        new HashSet<String> (), 
//                        new Stack<String> (), 
//                        newNTId,
//                        followABS,
//                        new Debug (debug, "follow " + language.getNT (fNT)  + " : " + newRule + " : " + is [1] + " : <" + is [0] + " : " + followABS),
//                        tokens
//             org.netbeans.modules.languages.parser.First.first       );
//                }
//                if (language.getNT (fNT).equals ("S")) {
//                    F f = firstForNT.get (-1, null);
//                    f.amp = new HashSet<Integer> ();
//                    f.amp.add (ruleIndex);
//                }
//                followABS.remove (fNT);
//                return r;
            }
            List newRightSide = rightSidesStack.pop ();
            String nt = ntPath.pop ();
            absNTSet.remove (nt);
//            debug.add ("pop " + " : " + ntPath + " : " + ntPath.size () + "/" + rightSidesStack.size ());
            boolean r = first (
                newRightSide, 
                0, 
                ruleIndex, 
                depthLimit, 
                firstForNT, 
                ntToIndexes, 
                rightSidesStack, 
                absNTSet, 
                ntPath, 
                fNT, 
                followABS,
//                debug,
                count
            );
            rightSidesStack.push (newRightSide);
            ntPath.push (nt);
            absNTSet.add (nt);
            return r;
        }
        if (depthLimit < 1)
            return firstForNT.amp.size () > 1;
        
//        followABS = new HashSet<Integer> ();
        Object e = rightSide.get (indexInRightSide);
        if (e instanceof ASTToken) {
            T t = new T ((ASTToken) e);
            if (firstForNT.find (t.type, t.identifier) == null) { 
                count[0] ++;
//                if (count [0] % 1000 == 0)
//                    System.out.println(count[0]);
            }
            F newInFirst = firstForNT.get (t.type, t.identifier);
            boolean r = first (
                rightSide, 
                indexInRightSide + 1, 
                ruleIndex, 
                depthLimit - 1, 
                newInFirst, 
                ntToIndexes, 
                rightSidesStack, 
                new HashSet<String> (), 
                ntPath, 
                fNT,
                followABS.isEmpty () ? followABS : new HashSet<Integer> (),
//                debug,
                count
            );
            return r;
        } else {
            String nt = (String) e;
            if (absNTSet.contains (nt))
                return firstForNT.amp.size () > 1;
            List<Integer> newRuleIndexes = ntToIndexes.get (nt);
            if (newRuleIndexes == null)
                throw new ParseException (nt + " grammar rule not defined!");
            rightSidesStack.push (rightSide.subList (indexInRightSide + 1, rightSide.size ()));
            ntPath.push (nt);
            absNTSet.add (nt);
            boolean r = false;
            Iterator<Integer> it = newRuleIndexes.iterator ();
            while (it.hasNext ()) {
                Integer rn = it.next ();
                List newRightSide = rules.get (rn.intValue ()).getRight ();
                //Stack<List> newRightSidesStack = new Stack<List> ();
                //newRightSidesStack.addAll (rightSidesStack);
                r |= first (
                    newRightSide, 
                    0, 
                    ruleIndex, 
                    depthLimit, 
                    firstForNT, 
                    ntToIndexes, 
                    rightSidesStack, //newRightSidesStack,
                    absNTSet, 
                    ntPath, 
                    fNT,
                    followABS,
//                    new Debug (debug, "rule " + rules.get (rn.intValue ()) + " : " + ntPath.size () + "/" + rightSidesStack.size ()),
                    count
                );
            }
            rightSidesStack.pop ();
            ntPath.pop ();
            absNTSet.remove (nt);
            return r;
        }
    }

//    private F follow (int ntId) {
//        F f = new F ();
//        Fl fl = follow [ntId];
//        if (fl == null) return f;
//        boolean r = false;
//        Iterator<int[]> it = fl.fl.iterator ();
//        while (it.hasNext ()) {
//            int[] is = it.next ();
//            Rule newRule = rules.get (is [0]);
//            int newNTId = language.getNTID (newRule.getNT ());
//            r |= first (
//                newRule.getRight (), 
//                is [1], 
//                0, 
//                1, 
//                f, 
//                ntToIndexes, 
//                rightSidesStack, 
//                new HashSet<String> (), 
//                new Stack<String> (), 
//                newNTId,
//                followABS,
//                new Debug (debug, "follow " + language.getNT (fNT)  + " : " + newRule + " : " + is [1] + " : <" + is [0] + " : " + followABS),
//                tokens
//            );
//        }
//        if (language.getNT (fNT).equals ("S")) {
//            F f = firstForNT.get (-1, null);
//            f.amp = new HashSet<Integer> ();
//            f.amp.add (ruleIndex);
//        }
//        followABS.remove (fNT);
//        return r;
//    }
    
    int getRule (int nt, TokenInput input, Set<Integer> skipTokens) {
        F node = maps [nt];
        int i = 1;
        while (true) {
            ASTToken token = input.next (i);
            while (token != null && skipTokens.contains (token.getTypeID ())) {
                i++;
                token = input.next (i);
            }
            F newNode = null;
            if (token != null)
                newNode = node.find (token.getTypeID (), token.getIdentifier ());
            else
                newNode = node.find (-1, null);
            if (newNode == null) {
                //return node.nt;
                Set s = node.hash;
                if (s == null)
                    s = node.amp;
                if (s == null) 
                    return -1;
                if (s.size () > 1)
                    return -2;
                return ((Integer) s.iterator ().next ()).intValue ();
            }
            node = newNode;
            i++;
        }
    }
    
    private F s (F m) {
        if (m.amp == null || m.amp.size () < 2) {
            F f = new F ();
            f.amp = m.amp;
            return f;
        }
        if (m.ff != null)
            for (int i = 0; i < m.ff.length; i++) {
                if (m.ff [i] != null) {
                    F.FF ff = m.ff [i];
                    if (ff.f != null)
                        ff.f = s (ff.f);
                    if (ff.map != null) {
                        Iterator<String> it = ff.map.keySet ().iterator ();
                        while (it.hasNext ()) {
                            String id = it.next ();
                            ff.map.put (id, s (ff.map.get (id)));
                        }
                    }
                }                
            }
        return m;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append ("First:\n");
        for (int i = 0; i < maps.length; i++) {
            sb.append (language.getNT (i));
            F f = maps [i];
            if (f == null) {
                sb.append ("null");
                continue;
            }
            sb.append (" :");
            f.printSets (sb);
            sb.append ('\n');
            f.toString (sb, "  ");
        }
        return sb.toString ();
    }
    
    public String printFollow () {
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < follow.length;  i++) {
            if (follow [i] == null) continue;
            sb.append (language.getNT (i)).append(":\n");
            List<int[]> list = follow [i].fl;
            Iterator<int[]> it = list.iterator ();
            while (it.hasNext ()) {
                int[] is = it.next ();
                Rule rule = rules.get (is [0]);
                sb.append ("  ").append (rule).append (":").append(is [1]).append("\n");
            }
        }
        return sb.toString ();
    }
    
    private int optimizeFollow () {
        int count = 0;
        for (int i = 0; i < follow.length;  i++) {
            if (follow [i] == null) continue;
            List<int[]> list = follow [i].fl;
            Map<Integer,Set<Integer>> n = new HashMap<Integer,Set<Integer>> ();
            Iterator<int[]> it = list.iterator ();
            while (it.hasNext ())
                addToFollow (it.next (), n, new HashSet<Integer> ());
            follow [i].fl = convertFollow (n);
            count += follow [i].fl.size ();
        }
        return count;
    }
    
    private void addToFollow (int[] is, Map<Integer,Set<Integer>> n, Set<Integer> abs) {
        Rule rule = rules.get (is [0]);
        if (is [1] < rule.getRight ().size ()) {
            Set<Integer> s = n.get (is [0]);
            if (s == null) {
                s = new HashSet<Integer> ();
                n.put (is [0], s);
            }
            s.add (is [1]);
            return;
        }
        int ntId = language.getNTID (rule.getNT ());
        if (abs.contains (ntId))
            return;
        abs.add (ntId);
        if (follow [ntId] == null) return;
        Iterator<int[]> it = follow [ntId].fl.iterator ();
        while (it.hasNext ())
            addToFollow (it.next (), n, abs);
        abs.remove (ntId);
    }
    
    private List<int[]> convertFollow (Map<Integer,Set<Integer>> n) {
        List<int[]> list = new ArrayList<int[]> ();
        Iterator<Integer> it = n.keySet ().iterator ();
        while (it.hasNext ()) {
            int r = it.next ();
            Iterator<Integer> it2 = n.get (r).iterator ();
            while (it2.hasNext ())
                list.add (new int[] {r, it2.next ()});
        }
        return list;
    }

    private String findConflict (F f, int depth) {
        if (f.amp == null || f.amp.size () < 2) return null;
        if (depth == 0) return "";
        if (f.ff [0] != null && f.ff [0].f != null) {
            String result = findConflict (f.ff [0].f, depth - 1);
            if (result != null)
                return "EOF " + result;
        }
        if (f.ff [0] != null && f.ff [0].map != null) {
            Iterator<String> it = f.ff [0].map.keySet ().iterator ();
            while (it.hasNext ()) {
                String identifier = it.next ();
                F newF = f.ff [0].map.get (identifier);
                String result = findConflict (newF, depth - 1);
                if (result != null)
                    return "\"" + identifier + "\" "+ result;
            }
        }
        for (int i = 1; i < f.ff.length; i++) {
            if (f.ff [i] == null) continue;
            if (f.ff [i].f != null) {
                String result = findConflict (f.ff [i].f, depth - 1);
                if (result != null)
                    return "<" + language.getTokenType (i) + "> "+ result;
            }
            if (f.ff [i].map != null) {
                Iterator<String> it = f.ff [i].map.keySet ().iterator ();
                while (it.hasNext ()) {
                    String identifier = it.next ();
                    F newF = f.ff [i].map.get (identifier);
                    String result = findConflict (newF, depth - 1);
                    if (result != null)
                        return "<" + language.getTokenType (i) + ",\"" + identifier + "\"> "+ result;
                }
            }
        }
        return null;
    }
    
    
    // innerclasses ............................................................
    
    private class F {
        
        FF[] ff;
        //int nt;

        Set<Integer> amp;
        Set<Integer> hash;

        F find (int type, String id) {
            if (ff == null) return null;
            FF fff = ff [type + 1];
            if (fff == null)
                fff = ff [0];
            if (fff == null) return null;
            if (fff.map == null) return fff.f;
            F result = fff.map.get (id);
            if (result == null)
                return fff.f;
            return result;
        }

        F get (int type, String id) {
            if (ff == null)
                ff = new FF [language.getTokenTypeCount () + 1];
            FF fff = ff [type + 1];
            if (fff == null) {
                fff = new FF ();
                ff [type + 1] = fff;
            }
            if (id == null) {
                if (fff.f == null)
                    fff.f = new F ();
                return fff.f;
            }
            if (fff.map == null)
                fff.map = new HashMap<String,F> ();
            F result = fff.map.get (id);
            if (result == null) {
                result = new F ();
                fff.map.put (id, result);
            }
            return result;
        }
        
        public String toString () {
            StringBuilder sb = new StringBuilder ();
            printSets (sb);
            sb.append ('\n');
            toString (sb, "  ");
            return sb.toString ();
        }
        
        void toString (StringBuilder sb, String indent) {
            if (ff == null) return;
            if (ff [0] != null && ff [0].f != null) {
                F f = ff [0].f;
                sb.append (indent).append ("<").append ("EOF").append ("> ");
                f.printSets (sb);
                sb.append ("\n");
                f.toString (sb, indent + "  ");
            }
            if (ff [0] != null && ff [0].map != null) {
                Iterator<String> it = ff [0].map.keySet ().iterator ();
                while (it.hasNext ()) {
                    String id = it.next ();
                    F f = ff [0].map.get (id);
                    sb.append (indent).append ("<\"").append (id).append ("\"> ");
                    f.printSets (sb);
                    sb.append ("\n");
                    f.toString (sb, indent + "  ");
                }
            }
            for (int i = 1; i < ff.length; i++) {
                if (ff [i] == null) continue;
                String type = language.getTokenType (i - 1);
                if (ff [i].f != null) {
                    sb.append (indent).append ("<").append(type).append ("> ");
                    ff [i].f.printSets (sb);
                    sb.append ("\n");
                    ff [i].f.toString (sb, indent + "  ");
                }
                if (ff [i].map != null) {
                    Iterator<String> it = ff [i].map.keySet ().iterator ();
                    while (it.hasNext ()) {
                        String id = it.next ();
                        F f = ff [i].map.get (id);
                        sb.append (indent).append ("<").append(type).append (",\"").append (id).append ("\"> ");
                        f.printSets (sb);
                        sb.append ("\n");
                        f.toString (sb, indent + "  ");
                    }
                }
            }
        }
        
        void printSets (StringBuilder sb) {
            if (amp != null) {
                sb.append ("[");
                Iterator<Integer> it = amp.iterator ();
                while (it.hasNext ()) {
                    sb.append (it.next ());
                    if (it.hasNext ())
                        sb.append (",");
                }
                sb.append ("] ");
            }
            if (hash != null) {
                sb.append ("#[");
                Iterator<Integer> it = hash.iterator ();
                while (it.hasNext ()) {
                    sb.append (it.next ());
                    if (it.hasNext ())
                        sb.append (",");
                }
                sb.append ("]");
            }
        }
    
        private class FF {

            private F f;
            private Map<String,F> map;

        }
    }
    
    static class Fl {
        List<int[]> fl = new ArrayList<int[]> ();
    }
    
    static class Debug {
        
        private List<String> messages;
        
        Debug (Debug debug, String message) {
            messages = new ArrayList<String> (debug.messages);
            add (message);
        }
        
        Debug (String message) {
            messages = new ArrayList<String> ();
            add (message);
        }
        
        Debug add (String message) {
            messages.add (message);
            return this;
        }
        
        public String toString () {
            StringBuilder sb = new StringBuilder ();
            Iterator<String> it = messages.iterator ();
            if (it.hasNext ())
                sb.append (it.next ()).append ('\n');
            while (it.hasNext ())
                sb.append ("  ").append (it.next ()).append ('\n');
            return sb.toString ();
        }
    }
}


