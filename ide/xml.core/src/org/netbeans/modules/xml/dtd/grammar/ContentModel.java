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

package org.netbeans.modules.xml.dtd.grammar;

import java.util.*;

/**
 * Implementation of queriable DTD content models. It is a hungry
 * automaton.
 *
 * @see ContentModelTest
 *
 * @author  Petr Kuzel
 */
abstract class ContentModel {

    /**
     * Create model by parsing its string representation "|,*?+()WS".
     * Caller must filter out <code>ANY</code>, <code>EMPTY</code> and
     * <code>(#PCDATA)</codE> content models.
     */
    public static final ContentModel parseContentModel(String model) {

        if (model == null || model.length() == 0) throw new IllegalArgumentException();

        PushbackStringTokenizer tokens = 
            new PushbackStringTokenizer(model, "|,*?+() \t\n", true);           // NOI18N
        String next = tokens.nextToken();            
        if (next.charAt(0) != '(' ) throw new IllegalStateException();
        return parseContentModel(tokens);
    }

    private static ContentModel parseContentModel(PushbackStringTokenizer tokens) {

        ContentModel model = null;
        List<ContentModel> models = new ArrayList<>(7);
        char type = 'E';
        char ch;            
        String next;

        do {

            next = tokens.nextToken();
            ch = next.charAt(0);
            if (ch == ' ' || ch == '\t' || ch == '\n') continue;
            if (ch == '#') { // #PCDATA
                do {
                    ch = tokens.nextToken().charAt(0);
                } while (ch == ' ' || ch == '\t' || ch == '\n');
                if (ch != '|') throw new IllegalStateException();
                continue; 
            } else if (ch == '(') {
                models.add(parseContentModel(tokens));
            } else if (ch == '|') {
                type = '|';
            } else if (ch == ',') {
                type = ',';
            } else if (ch == ')') {
                break;
            } else {
                model = new Element(next);

                // optional element multiplicity

                do {
                    next = tokens.nextToken();
                    ch = next.charAt(0);
                } while (ch == ' ' || ch == '\t' || ch == '\n');
                if (ch == '+') {
                    model = new MultiplicityGroup(model, 1, -1);
                } else if (ch == '?') {
                    model = new MultiplicityGroup(model, 0, 1);
                } else if (ch == '*') {
                    model = new MultiplicityGroup(model, 0, -1);
                } else if (ch == ')') {
                    // do not pushback!
                } else {
                    tokens.pushback(next);
                }
                models.add(model);
            }

        } while (ch != ')');

        // create models

        if (type == '|') {
            model = new Choice(models.toArray(new ContentModel[0]));
        } else if (type == ',') {
            model = new Sequence(models.toArray(new ContentModel[0]));
        } else {
            // note model contains last Element
        }

        // determine optional group multiplicity

        do {
            if (tokens.hasMoreTokens() == false) break;
            next = tokens.nextToken();
            ch = next.charAt(0);
        } while (ch == ' ' || ch == '\t' || ch == '\n');

        if (ch == '?') {
            model = new MultiplicityGroup(model, 0, 1);
        } else if (ch == '*') {
            model = new MultiplicityGroup(model, 0, -1);
        } else if (ch == '+') {
            model = new MultiplicityGroup(model, 1, -1);
        } else {
            tokens.pushback(next);
        }

        if(model == null) {
            //75881 fix - the XMLSchema.dtd file contains multiple nested sections e.g.
            //<!ENTITY % attrDecls    '((%attribute;| %attributeGroup;)*,(%anyAttribute;)?)'>
            if(models.size() == 1) {
                //there is just one model inside (the mentioned case)
                //so we can return it
                return models.get(0);
            }
        }

        return model;
    }

    /**
     * @return enumeration&lt;String&gt; or null if document is not valid.
     */
    public final Enumeration whatCanFollow(Enumeration en) {
        reset();
        Food food = new Food(en);
        if (eat(food)) {
            return possibilities();
        } else {
            return null;
        }                        
    }

    /**
     * Reinitializes the content model to initial state.
     */
    protected void reset() {
    }

    /**
     * Move the automaton to next state. It is may not be called
     * twice without reset!
     * @return true if accepted the food, false at root model indicate document error
     */
    protected abstract boolean eat(Food food);

    /**
     * Enumerate all <b>FIRST</b>s at current state.
     * It must not be called if <code>eat</code> returned <code>false</code>
     * @return possible completion
     */
    protected abstract Enumeration possibilities();

    /**
     * Does need the content model a reset because it is in final state?
     * @return true if it is in final state
     */
    protected boolean terminated() {
        return false;
    }
    
    /**
     * Is the content model in current state optional?
     */
    protected boolean isOptional() {
        return false;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~ Implemenation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Single element
     */
    private static class Element extends ContentModel {

        private final String name;

        private boolean full = false;

        public Element (String name) {
            this.name = name;
        }

        protected void reset() {
            full = false;
        }
        
        protected boolean eat(Food food) {
            if (food.hasNext() == false) {
                return true;
            } else {
                String next = food.next();
                if (this.name.equals(next)) {                
                    full = true;
                    return true;
                } else {
                    return false;
                }
            }
        }

        protected Enumeration possibilities() {
            if (terminated() == false) {
                return org.openide.util.Enumerations.singleton (name);
            } else {
                return org.openide.util.Enumerations.empty();
            }
        }

        protected boolean terminated() {
            return full;
        }
                
        public String toString() {
            return "Element[" + name + "]";
        }
    }

    
    /**
     * Mandatory sequence of models.
     */
    private static class Sequence extends ContentModel {

        private ContentModel[] models;

        // index of current model to use <0, models.length>
        private int current = 0;

        public Sequence(ContentModel[] models) {
            this.models = models;
        }

        /**
         * Reset all models upto (inclusive) current one.
         */
        protected void reset() {
            if(models == null)
                return;
            for (int i = 0; i<models.length; i++) {
                models[i].reset();
            }
            current = 0;
        }

        /**
         * Feed all sequence models until is enough food.
         */
        protected boolean eat(Food food) {

            while (food.hasNext()) {
                
                if (current == models.length) return true;
                
                int store = food.mark();
                boolean accept = models[current].eat(food);
                boolean more = food.hasNext();
                boolean terminated = models[current].terminated();
                
                if (accept == false) {
                    return false;
                } else if (more) {                    
                    current++;
                } else if (terminated == false && store != food.mark()) {
                    
                    // last model is possibly partially full, it could disapprove
                    // if more food was provided -> move all subsequent automatons
                    // to have accurate possibilities()

                    int level = food.mark();
                    for (int i = current + 1; i<models.length; i++) {
                        food.reset(store);
                        if (models[i].eat(food) == false) {
                            models[i].reset();
                            if (models[i].isOptional() == false) break;
                        } else {
                            if (store == food.mark()) {
                                // the automaton was unattracted
                                models[i].reset();
                            }
                        }
                    }
                    food.reset(level);
                    assert food.hasNext() == false : "Food mark/reset invariant is broken!";
                                        
                } else if (terminated) {
                    // it accepted and is complete
                    current++;                    
                }
            }
                                    
            return true;
        }

        protected boolean terminated() {
            if (current == models.length) return true;
            if (current < models.length - 1) return false;
            // last model may be active
            return models[current].terminated();
        }

        protected boolean isOptional() {
            for (int i = 0; i<models.length; i++) {
                if (models[i].isOptional() == false) return false;
            }
            return true;
        }
        
        protected Enumeration possibilities() {
            if (terminated() == false) {
                Enumeration en = org.openide.util.Enumerations.empty();
                for ( int i = current; i<models.length; i++) {
                    ContentModel next = models[i];                    
                    en = org.openide.util.Enumerations.concat (en, next.possibilities());
                    if (next.isOptional() == false) break;
                }
                return en;
            } else {
                return org.openide.util.Enumerations.empty();
            }
        }

        public String toString() {
            String ret = "Sequence[";
            for (int i = 0; i<models.length; i++) {
                ret += models[i].toString() + ", ";
            }
            return ret + " current=" + current + "]";
        }
    }
        
    /**
     * This content model allows options :-(.
     */
    private static class MultiplicityGroup extends ContentModel {

        private final int min; // 0 or 1
        private final int max; // -1 for infinity (we must always test for ==)
        private final ContentModel peer;

        // current occurence count
        private int current = 0;
        
        public MultiplicityGroup (ContentModel model, int min, int max) {
            this.peer = model;
            this.min = min;
            this.max = max;
            current = 0;
        }

        protected void reset() {
            current = 0;
            if(peer != null)
                peer.reset();
        }

        protected boolean eat(Food food) {

            boolean accept = current == min;
            while (food.hasNext()) {
                
                if (current == max) return true;
                
                int store = food.mark();                
                boolean accepted = (peer == null)?false:peer.eat(food);
                
                if (accepted == false) {
                    food.reset(store);
                    return accept;
                } else if (food.hasNext()) {
                    if (++current >= max && max > -1) {
                        return true;
                    };
                    peer.reset();
                } else if (peer.terminated()) {
                    // no more food, do not increment current for unterminated
                    current ++;
                }
                accept = true;
            }
            
            return true;
        }

        public Enumeration possibilities() {            
            if (peer == null || terminated()) {
                return org.openide.util.Enumerations.empty();
            }
            
            // we force peer reinitialization
            if (peer.terminated())
                peer.reset();
            return peer.possibilities();
        }

        protected boolean terminated() {
            if (current != max) return false;
            return peer.terminated();
        }

        protected boolean isOptional() {
            if (min <= current) return true;
            return peer.isOptional();
        }
        
        public String toString() {
            return "MultiplicityGroup[peer=" + peer + ", min=" + min + ", max=" + max + ", current=" + current + "]";
        }
    }

    /**
     * At least one sub-content model must eat.     
     */
    private static class Choice extends ContentModel {

        private ContentModel[] models;
        private boolean modelsThatNotAcceptedAtLeastOne[];

        private boolean terminated = false;

        // index of current model to use <0, models.length>
        private int current = 0;

        public Choice(ContentModel[] models) {
            this.models = models;
            modelsThatNotAcceptedAtLeastOne = new boolean[models.length];
        }


        /**
         * Reset all models upto (inclusive) current one.
         */
        protected void reset() {
            if(models == null)
                return;
            for (int i = 0; i<models.length; i++) {
                models[i].reset();
                modelsThatNotAcceptedAtLeastOne[i] = false;
            }
            current = 0;
            terminated = false;
        }

        /**
         * Feed all choice models until is enough food.
         * @return trua if at least one accepted
         */
        protected boolean eat(Food food) {

            boolean accepted = food.hasNext() == false;
            int newFood = food.mark();
            boolean acceptedAndHungry = food.hasNext() == false;

            while (food.hasNext()) {

                if (current == models.length) break;

                int store = food.mark();
                boolean accept = models[current].eat(food);
                
                if (accept) {                    
                    accepted = true;
                    if (store == food.mark()) {
                        modelsThatNotAcceptedAtLeastOne[current] = true;
                    }
                    if (food.hasNext() == false) {
                        acceptedAndHungry |= models[current].terminated() == false;
                    }
                    newFood = Math.max(newFood, food.mark());
                } else {
                    modelsThatNotAcceptedAtLeastOne[current] = true;
                }
                current++;
                food.reset(store);
            }
            
            food.reset(newFood);
            terminated = acceptedAndHungry == false;
            return accepted;
        }

        protected boolean terminated() {
            return terminated;
        }

        protected boolean isOptional() { 
            boolean optional = false;
            for (int i = 0; i<models.length; i++) {
                if (models[i].isOptional()) {
                    optional = true;
                    break;
                }
            }
//            System.err.println("  " + this + " optional=" + optional);
            return optional;
        }
        
        protected Enumeration possibilities() {
            if (terminated() == false) {
                Enumeration en = org.openide.util.Enumerations.empty();
                for ( int i = 0; i<models.length; i++) {
                    if (modelsThatNotAcceptedAtLeastOne[i]) continue;
                    ContentModel next = models[i];                    
                    en = org.openide.util.Enumerations.concat (en, next.possibilities());
                }
                return en;
            } else {
                return org.openide.util.Enumerations.empty();
            }
        }

        public String toString() {
            String ret = "Choice[";
            for (int i = 0; i<models.length; i++) {
                ret += models[i].toString() + ", ";
            }
            return ret + " current=" + current + "]";
        }
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~ Utility classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Feeding for content model automaton. It is a kind of lazy initialized
     * stack with fast backtracking.
     */
    private static class Food {

        // stack emulator
        private final List<String> list = new LinkedList<>();
        
        // source of lazy stack initilization
        private final Enumeration<String> en;
        
        // current stack position
        private int current;
        
        public Food (Enumeration en) {
            this.en = en;
            current = 0;
        }

        /**
         * @return current stack location
         */
        public int mark() {
            return current;
        }
        
        /**
         * Set new current
         */
        public void reset(int pos) {
            current = pos;
        }
        
        /**
         * Return next available element.
         * Must not be called if <code>hasNext</code> returns <code>null</code>
         */
        public String next() {
            if (hasNext() == false) {
                throw new IllegalStateException();
            } else {
                String next  = list.get(current);
                current++;
                return next;
            }
        }
        
        /** 
         * @return true if it is assured that next is available. 
         */
        public boolean hasNext() {
            if (list.size() > current) return true;
            if (en.hasMoreElements()) {
                String next = (String) en.nextElement();
                return list.add(next);                
            } else {
                return false;
            }
        }
    }
    
    /**
     * Partial implementation of single-pushback tokenizer.
     */
    private static class PushbackStringTokenizer extends StringTokenizer {
        
        private String pushback = null;
        
        public PushbackStringTokenizer(String tokens, String delim, boolean inc) {
            super(tokens, delim, inc);
        }
        
        public String nextToken() {
            String next;
            if (pushback != null) {
                next = pushback;
                pushback = null;
            } else {
                next = super.nextToken();
            }            
            return next;
        }
        
        public boolean hasMoreTokens() {
            if (pushback != null) {
                return true;
            } else {
                return super.hasMoreTokens();
            }
        }
        
        public void pushback(String pushback) {
            if (this.pushback != null) throw new IllegalStateException();
            this.pushback = pushback;
        }
    }
    
}
