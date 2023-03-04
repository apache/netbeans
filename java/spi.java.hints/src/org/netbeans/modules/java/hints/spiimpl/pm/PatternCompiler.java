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

import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Map;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.api.java.source.matching.Pattern;

/**XXX: cancelability!
 *
 * @author Jan Lahoda
 */
public class PatternCompiler {

    public static Pattern compile(CompilationInfo info, String pattern, Map<String, TypeMirror> constraints, Iterable<? extends String> imports) {
        Scope scope = Utilities.constructScope(info, constraints, imports);

        if (scope == null) {
            return null; //TODO: can happen?
        }

        Tree patternTree = Utilities.parseAndAttribute(info, pattern, scope);

        return Pattern.createPatternWithFreeVariables(new TreePath(new TreePath(info.getCompilationUnit()), patternTree), constraints);
    }

}
