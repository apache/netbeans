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

package org.netbeans.modules.java.hints.spi;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;

/** Represents a rule to be run on the java source.
 *
 * @author Petr Hrebejk
 */
public interface TreeRule extends Rule {
    
    /** Get the treekinds this rule should run on
     */
    public Set<Tree.Kind> getTreeKinds();

    /** Run the test on given CompilationUnit and return list of Errors or
     * warrnings to be shown in the editor.
     */
    public List<ErrorDescription> run( CompilationInfo compilationInfo, TreePath treePath );

  
}
