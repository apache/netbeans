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
package org.netbeans.api.java.source;

import java.io.IOException;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.org">RKo</a>)
 * @deprecated Do not use. Use {@link GeneratorUtilities#importComments(com.sun.source.tree.Tree, com.sun.source.tree.CompilationUnitTree) } instead.
 */
@Deprecated
public final class CommentCollector {
    private static CommentCollector instance = null;

    /**
     * @deprecated Do not use. Use {@link GeneratorUtilities#importComments(com.sun.source.tree.Tree, com.sun.source.tree.CompilationUnitTree) } instead.
     */
    @Deprecated
    public static synchronized CommentCollector getInstance() {
        if (instance == null) {
            instance = new CommentCollector();
        }
        return instance;
    }

    private CommentCollector() {
    }


    /**
     * @deprecated Do not use. Use {@link GeneratorUtilities#importComments(com.sun.source.tree.Tree, com.sun.source.tree.CompilationUnitTree) } instead.
     */
    @Deprecated
    public void collect(WorkingCopy copy) throws IOException {
        GeneratorUtilities.importComments(copy, copy.getCompilationUnit(), copy.getCompilationUnit());
    }

    /**
     * @deprecated Do not use. Use {@link GeneratorUtilities#importComments(com.sun.source.tree.Tree, com.sun.source.tree.CompilationUnitTree) } instead.
     */
    @Deprecated
    public void collect(TokenSequence<JavaTokenId> ts, CompilationInfo ci) {
        GeneratorUtilities.importComments(ci, ci.getCompilationUnit(), ci.getCompilationUnit());
    }

}
