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
package org.netbeans.modules.java.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.source.save.DiffContext;

/** Temporary Should be removed soon
 *
 * @author phrebejk
 */
public class ElementHeaderFormater {

    private ElementHeaderFormater() {
    }
    
    public static String getMethodHeader(MethodTree tree, ClassTree enclosingClass, CompilationInfo info, String s) {
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        if (enclosingClass != null) {
            veryPretty.enclClass = (com.sun.tools.javac.tree.JCTree.JCClassDecl) enclosingClass;
        }
        return veryPretty.getMethodHeader(tree, s);
    }

    public static String getClassHeader(ClassTree tree, CompilationInfo info, String s) {
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        return veryPretty.getClassHeader(tree, s);
    }
    
    public static String getVariableHeader(VariableTree tree, CompilationInfo info, String s) {
        VeryPretty veryPretty = new VeryPretty(new DiffContext(info));
        return veryPretty.getVariableHeader(tree, s);
    }
    
}
