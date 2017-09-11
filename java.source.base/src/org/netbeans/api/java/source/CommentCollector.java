/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
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
