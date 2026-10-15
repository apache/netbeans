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
package org.netbeans.modules.languages.hcl.ast;

import java.util.LinkedList;
import java.util.function.Predicate;

/**
 *
 * @author lkishalmi
 */
public final class HCLTreeWalker {
    public record Step(HCLElement parent, HCLElement node, int depth) {}

    private HCLTreeWalker() {}

    public static void depthFirst(HCLElement root, Predicate<Step> t) {
        LinkedList<Step> process = new LinkedList<>();
        process.push(new Step(null, root, 0));
        while (!process.isEmpty()) {
            Step current = process.pop();
            if (t.test(current)) {
                for (HCLElement e : current.node.elements()) {
                    process.push(new Step(current.node, e, current.depth + 1));
                }
            }
        }
    }

    public static void breadthFirst(HCLElement root, Predicate<Step> t) {
        LinkedList<Step> process = new LinkedList<>();
        process.add(new Step(null, root, 0));
        while (!process.isEmpty()) {
            var current = process.pop();
            if (t.test(current)) {
                for (HCLElement e : current.node.elements()) {
                    process.add(new Step(current.node, e, current.depth + 1));
                }
            }
        }
    }
}
