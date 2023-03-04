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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.source.tree.Tree;
import java.util.Collection;

/**
 * Support class to help assertions and error processing when evaluating an expression.
 *
 * @author Maros Sandor
 */
class Assert {

    static Object error(Tree node, String param) throws EvaluationException {
        return error(node, param, null);
    }

    static Object error(Tree node, String cause, Object p2) throws EvaluationException {
        return error(node, cause, new Object [] { p2 });
    }

    static Object error(Tree node, String cause, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p2, p3});
    }

    static Object error(Tree node, String cause, Object p1, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p1, p2, p3});
    }

    private static Object error (Tree node, String cause, Object [] params) throws EvaluationException {
        throw new EvaluationException(node, cause, params);
    }

    static void assertAssignable(Object o, Class aClass, Tree s, String p1, Object p2) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertAssignable(Object o, Class aClass, Tree s, String p1, Object p2, Object p3) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, Tree s, String p1, Object p2) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, Tree s, String p1, Object p2, Object p3) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, Tree node, String s) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(node, s);
        }
    }

    static void assertLess(int a, int b, Tree s, String p1, Object p2, Object p3) {
        if (a >= b) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotNull(Object obj, Tree s, String identifier) {
        if (obj == null) {
            error(s, identifier);
        }
    }

    static void assertNonEmpty(Collection collection, Tree s, String p1, Object p2) {
        if (collection == null || collection.size() == 0) {
            error(s, p1, p2);
        }
    }

    static void assertNotNull(Object obj, Tree node, String p1, Object p2) {
        if (obj == null) {
            error(node, p1, p2);
        }
    }

}
