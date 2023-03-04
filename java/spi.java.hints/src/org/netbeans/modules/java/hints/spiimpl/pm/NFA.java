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

package org.netbeans.modules.java.hints.spiimpl.pm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lahvac
 */
public class NFA<I, R> {

    /*XXX: private*/ final int stateCount;
    private final int startingState;
    private final Set<I> inputs;
    private final Map<Key<I>, State> transitionTable;
    private final Map<Integer, R> finalStates;

    private final State startingStateObject;

    private NFA(int startingState, int stateCount, Set<I> inputs, Map<Key<I>, State> transitionTable, Map<Integer, R> finalStates) {
        this.startingState = startingState;
        this.stateCount = stateCount;
        this.inputs = inputs;
        this.transitionTable = transitionTable;
        this.finalStates = finalStates;

        startingStateObject = State.create().mutableOr(startingState);
    }

    public State getStartingState() {
        return startingStateObject;
    }

    public State transition(final State active, final I input) {
        State result = null;

        for (int i : active) {
//        for (int i = active.nextSetBit(0); i >= 0; i = active.nextSetBit(i+1)) {
             State target = transitionTable.get(Key.create(i, input));

             if (target != null) {
                 if (result == null) {
                     result = State.create();
                 }
                 
                 result.mutableOr(target);
             }
        }

        State r;

        //XXX:
        if (result == null) {
            r = startingStateObject;
        } else {
            r = result.mutableOr(startingState);//???
        }

        return r;
    }

    public Set<R> getResults(State bs) {
        Set<R> result = new HashSet<>();

        for (int i : bs) {
//        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
            if (finalStates.get(i) != null) {
                result.add(finalStates.get(i));
            }
        }

        return result;
    }

    public static <I, R> NFA<I, R> create(int startingState, int stateCount, Set<I> inputs, Map<Key<I>, State> transitionTable, Map<Integer, R> finalStates) {
        return new NFA<>(startingState, stateCount, inputs, transitionTable, finalStates);
    }

    public State join(State s1, State s2) {
        State bs = State.create();

        bs.mutableOr(s1);
        bs.mutableOr(s2);

        return bs;
    }

    public static final class Key<I> {
        private final int state;
        private final I   input;

        private Key(int state, I input) {
            this.state = state;
            this.input = input;
        }

        public static <I> Key<I> create(int state, I input) {
            return new Key<>(state, input);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key<?> other = (Key<?>) obj;
            if (this.state != other.state) {
                return false;
            }
            if (this.input != other.input && (this.input == null || !this.input.equals(other.input))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + this.state;
            hash = 83 * hash + (this.input != null ? this.input.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "[" + state + ", " + input + "]";
        }

    }

    public static final class State extends HashSet<Integer> {
        private State() {
            super(4);
        }

        public static State create() {
            return new State();
        }

        public State mutableOr(int state) {
            add(state);
            return this;
        }

        public State mutableOr(State or) {
            addAll(or);
            return this;
        }

    }

//    public static final class State extends BitSet {
//        private State() {}
//
//        public static State create() {
//            return new State();
//        }
//
//        public State mutableOr(int state) {
//            set(state);
//            return this;
//        }
//
//        public State mutableOr(State or) {
//            or(or);
//            return this;
//        }
//
//    }

}
