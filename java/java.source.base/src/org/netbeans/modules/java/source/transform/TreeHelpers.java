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
package org.netbeans.modules.java.source.transform;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.Tag;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author lahvac
 */
public class TreeHelpers {
    public static List<? extends Tree> getCombinedTopLevelDecls(CompilationUnitTree cut) {
        JCCompilationUnit cu = (JCCompilationUnit) cut;
        return cu.defs.stream()
                      .filter(t -> t.hasTag(Tag.CLASSDEF) || t.hasTag(Tag.MODULEDEF))
                      .collect(Collectors.toList());
    }

}
