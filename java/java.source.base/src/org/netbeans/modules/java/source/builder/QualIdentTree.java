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
package org.netbeans.modules.java.source.builder;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

/**
 *
 * @author Jan Lahoda
 */
public class QualIdentTree extends JCTree.JCFieldAccess {

    private final String fqn;

    public QualIdentTree(JCExpression selected, Name name, Symbol sym) {
        super(selected, name, sym);
        this.fqn = null;
    }

    public QualIdentTree(JCExpression selected, Name name, String fqn) {
        super(selected, name, null);
        this.fqn = fqn;
    }

    public String getFQN() {
        return fqn;
    }

}
