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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spring.beans.index;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author alex
 */
public class SpringIndex {

    private final FileObject[] binaryRoots;

    public SpringIndex(FileObject fo) {
        this.binaryRoots = (ClassPath.getClassPath(fo, ClassPath.EXECUTE).getRoots());
    }

    public SpringIndex(ClassPath cp) {
        this.binaryRoots = cp.getRoots();
    }
    
    private QuerySupport createBinaryIndex() throws IOException {
        return QuerySupport.forRoots(SpringBinaryIndexer.INDEXER_NAME, SpringBinaryIndexer.INDEX_VERSION, binaryRoots);
    }

    public Map<String, FileObject> getAllSpringLibraryDescriptors() {
        Map<String, FileObject> map = new HashMap<String, FileObject>();
        try {
            Collection<? extends IndexResult> results = createBinaryIndex().query(
                    SpringBinaryIndexer.LIBRARY_MARK_KEY,
                    "true", //NOI18N
                    QuerySupport.Kind.EXACT,
                    SpringBinaryIndexer.LIBRARY_MARK_KEY, SpringBinaryIndexer.NAMESPACE_MARK_KEY);
            for (IndexResult result : results) {
                FileObject file = result.getFile(); //expensive? use result.getRelativePath?
                if (file != null) {
                    map.put(result.getValue(SpringBinaryIndexer.NAMESPACE_MARK_KEY), file);
                }

            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return map;

    }
}
