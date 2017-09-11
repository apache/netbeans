/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import java.util.Collection;
import java.util.LinkedList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author Tomas Zezula
 */
public final class NewComilerTask extends ClasspathInfoTask {

    private CompilationController result;
    private long timestamp;
    private final int position;

    public NewComilerTask (final ClasspathInfo cpInfo, final int position, final CompilationController last, long timestamp) {
        super (cpInfo);
        this.position = position;
        this.result = last;
        this.timestamp = timestamp;
    }

    @Override
    public void run(@NonNull ResultIterator resultIterator) throws Exception {
        final Snapshot snapshot = resultIterator.getSnapshot();
        if (!JavacParser.MIME_TYPE.equals(snapshot.getMimeType())) {
            resultIterator = findEmbeddedJava(resultIterator);
        }
        if (resultIterator != null) {
            resultIterator.getParserResult();   //getParserResult calls setCompilationController
        }
    }

    @CheckForNull
    private ResultIterator findEmbeddedJava (@NonNull final ResultIterator theMess) throws ParseException {
        final Collection<Embedding> todo = new LinkedList<>();
        //BFS should perform better than DFS in this dark.
        for (Embedding embedding : theMess.getEmbeddings()) {
            if (position != -1 && !embedding.containsOriginalOffset(position)) {
                continue;
            }
            if (JavacParser.MIME_TYPE.equals(embedding.getMimeType())) {
                return theMess.getResultIterator(embedding);
            } else {
                todo.add(embedding);
            }
        }
        for (Embedding embedding : todo) {
            final ResultIterator res  = findEmbeddedJava(theMess.getResultIterator(embedding));
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public void setCompilationController (
            @NonNull final CompilationController result,
            final long timestamp) {
        assert result != null;
        this.result = result;
        this.timestamp = timestamp;
    }

    public CompilationController getCompilationController () {
        return result;
    }

    public long getTimeStamp () {
        return this.timestamp;
    }

}
