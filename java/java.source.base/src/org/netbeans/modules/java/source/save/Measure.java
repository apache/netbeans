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
package org.netbeans.modules.java.source.save;

import com.sun.tools.javac.tree.JCTree;
import java.util.Comparator;
import static org.netbeans.modules.java.source.save.Measure.*;
import static com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.DCTree;

/**
 * Used for distance measuring of two elements.
 *
 * @author  Martin Matula
 * @author  Tomas Hurka
 * @author  Pavel Flaska
 */
public class Measure {

    /**
     * Prevent instance creation outside the class.
     */
    private Measure() {
    }

    /**
     * Default measure based on equals.
     */
    static final Comparator<JCTree> DEFAULT = new Comparator<JCTree>() {

        /**
         * Compares two objects and returns distance between
         * them. (Value expressing how far they are.)
         *
         * @param first First object to be compared.
         * @param second Second object to be compared.
         * @return Distance between compared objects (0 = objects perfectly match,
         * <code>INFINITE_DISTANCE</code> = objects are completely different)
         */
        public int compare(JCTree first, JCTree second) {
            assert first != null && second != null : "Shouldn't pass null value!";

            if (first == second || first.equals(second)) {
                // pefectly match
                return OBJECTS_MATCH;
            } else {
                // does not match
                return INFINITE_DISTANCE;
            }
        }
    };
    
    /**
     * Default measure based on equals.
     */
    static final Comparator<DCTree> DOCTREE = new Comparator<DCTree>() {

        /**
         * Compares two objects and returns distance between
         * them. (Value expressing how far they are.)
         *
         * @param first First object to be compared.
         * @param second Second object to be compared.
         * @return Distance between compared objects (0 = objects perfectly match,
         * <code>INFINITE_DISTANCE</code> = objects are completely different)
         */
        public int compare(DCTree first, DCTree second) {
            assert first != null && second != null : "Shouldn't pass null value!";

            if (first == second || first.equals(second)) {
                // pefectly match
                return OBJECTS_MATCH;
            } else {
                // does not match
                return INFINITE_DISTANCE;
            }
        }
    };
    
    /**
     * Used for measuring distance of two <code>Method invocation arguments</code>s.
     */
    static final Comparator<DCTree> TAGS = new Comparator<DCTree>() {

        public int compare(DCTree t1, DCTree t2) {
            int distance = DOCTREE.compare(t1, t2);
            if (distance == INFINITE_DISTANCE) {
                if (t1.getKind() == t2.getKind()) {
                    return t1.pos == t2.pos ? ALMOST_THE_SAME : THE_SAME_KIND;
                }
            }
            return distance;
        }
    };

    /**
     * Used for measuring distance of two class members.
     * (for fields, methods, constructors, annotation attributes etc.)
     */
    static final Comparator<JCTree> REAL_MEMBER = new Comparator<JCTree>() {

        public int compare(JCTree t1, JCTree t2) {
            int distance = DEFAULT.compare(t1, t2);
            if (distance == INFINITE_DISTANCE) {
                if (t1.getKind() == t2.getKind()) {
                    return (t1.pos == t2.pos) ? ALMOST_THE_SAME : THE_SAME_KIND;
                }
            }
            return distance;
        }
    };

    static final Comparator<JCTree> MEMBER = new Comparator<JCTree>() {

        public int compare(JCTree t1, JCTree t2) {
            int distance = DEFAULT.compare(t1, t2);
            if (distance == INFINITE_DISTANCE) {
                if (t1.getKind() == t2.getKind() && t1.pos == t2.pos) {
                    return ALMOST_THE_SAME;
                }
            }
            return distance;
        }
    };

    /**
     * Used for measuring distance of two <code>Method invocation arguments</code>s.
     */
    static final Comparator<JCTree> ARGUMENT = new Comparator<JCTree>() {

        public int compare(JCTree t1, JCTree t2) {
            int distance = DEFAULT.compare(t1, t2);
            if (distance == INFINITE_DISTANCE) {
                if (t1.getKind() == t2.getKind()) {
                    return t1.pos == t2.pos ? ALMOST_THE_SAME : THE_SAME_KIND;
                }
            }
            return distance;
        }
    };

    /**
     * Used for measuring distance of two <code>variables separated by comma</code>s.
     */
    static final Comparator<JCTree> GROUP_VAR_MEASURE = ARGUMENT;

    /**
     * Value representing infinite distance - any distance value equal
     * or greater than this is represented as infinite (i.e. indicates
     * that the compared objects are distinct).
     */
    public static final int INFINITE_DISTANCE = 1000;

    /**
     * Objects perfectly matches, they are identical.
     */
    public static final int OBJECTS_MATCH = 0;

    /**
     * Objects are almost the same, kind is the same and pos is the same.
     */
    public static final int ALMOST_THE_SAME = 250;

    /**
     * Objects are the same kind, but different pos
     */
    public static final int THE_SAME_KIND = 750;
}
