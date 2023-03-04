/**
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
package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Log;
import java.io.IOException;
import javax.tools.JavaFileObject;

/**
 *
 * @author lahvac
 */
public class ParsingUtils {
    
    public static CompilationUnitTree parseArbitrarySource(JavacTask task, JavaFileObject file) throws IOException {
        JavacTaskImpl taskImpl = (JavacTaskImpl) task;
        com.sun.tools.javac.util.Context context = taskImpl.getContext();
        Log log = Log.instance(context);
        JavaFileObject prevSource = log.useSource(file);
        try {
            ParserFactory fac = ParserFactory.instance(context);
            JCCompilationUnit cut = fac.newParser(file.getCharContent(true), true, true, true).parseCompilationUnit();
            
            cut.sourcefile = file;
            
            return cut;
        } finally {
            log.useSource(prevSource);
        }
    }
    
}
