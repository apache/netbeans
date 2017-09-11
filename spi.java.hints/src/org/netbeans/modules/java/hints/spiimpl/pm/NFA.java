/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009-2013 Sun Microsystems, Inc.
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
        Set<R> result = new HashSet<R>();

        for (int i : bs) {
//        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
            if (finalStates.get(i) != null) {
                result.add(finalStates.get(i));
            }
        }

        return result;
    }

    public static <I, R> NFA<I, R> create(int startingState, int stateCount, Set<I> inputs, Map<Key<I>, State> transitionTable, Map<Integer, R> finalStates) {
        return new NFA<I, R>(startingState, stateCount, inputs, transitionTable, finalStates);
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
            return new Key<I>(state, input);
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
