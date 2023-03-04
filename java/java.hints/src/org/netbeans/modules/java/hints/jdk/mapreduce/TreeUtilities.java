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
/*
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import com.sun.source.tree.Tree;

/**
 *
 * @author alexandrugyori
 */
public class TreeUtilities {

    public static boolean isCompoundAssignementAssignement(Tree.Kind kind) {
        return Tree.Kind.AND_ASSIGNMENT == kind
                || Tree.Kind.OR_ASSIGNMENT == kind
                || Tree.Kind.PLUS_ASSIGNMENT == kind
                || Tree.Kind.MINUS_ASSIGNMENT == kind
                || Tree.Kind.MULTIPLY_ASSIGNMENT == kind
                || Tree.Kind.DIVIDE_ASSIGNMENT == kind
                || Tree.Kind.REMAINDER_ASSIGNMENT == kind
                || Tree.Kind.LEFT_SHIFT_ASSIGNMENT == kind
                || Tree.Kind.RIGHT_SHIFT_ASSIGNMENT == kind
                || Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT == kind;
    }

    public static boolean isPreOrPostfixOp(Tree.Kind opKind) {
        return opKind == Tree.Kind.POSTFIX_INCREMENT
                || opKind == Tree.Kind.PREFIX_INCREMENT
                || opKind == Tree.Kind.POSTFIX_DECREMENT
                || opKind == Tree.Kind.PREFIX_DECREMENT;
    }
}
