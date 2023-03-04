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
package org.netbeans.modules.java.source.classpath;


import java.io.IOException;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class, position=125)
public class CacheSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private String FILE_PROTOCOL = "file";  //NOI18N

    /** Creates a new instance of CacheSourceForBinaryQueryImpl */
    public CacheSourceForBinaryQueryImpl() {
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (!FILE_PROTOCOL.equals (binaryRoot.getProtocol())) {
            return null;
        }
        URL sourceURL = JavaIndex.getSourceRootForClassFolder(binaryRoot);
        SourceForBinaryQuery.Result result = null;
        if (sourceURL != null) {
            for ( SourceForBinaryQueryImplementation impl :Lookup.getDefault().lookupAll(SourceForBinaryQueryImplementation.class)) {
                if (impl != this) {
                    result = impl.findSourceRoots(sourceURL);
                    if (result != null) {
                        break;
                    }
                }
            }
            result = new R (sourceURL, result);
            }
        return result;
    }

    private static class R implements SourceForBinaryQuery.Result {
        private final FileObject sourceRoot;
        private final SourceForBinaryQuery.Result delegate;

        public R (final URL sourceRootURL, final SourceForBinaryQuery.Result delegate) {
            assert sourceRootURL != null;
            this.sourceRoot = URLMapper.findFileObject(sourceRootURL);
            this.delegate = delegate;
        }

        public void removeChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public void addChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public FileObject[] getRoots() {

            FileObject[] result;
            //Is here SFBQ.Result for root?
            if (delegate != null) {
                //Yes - either [root*] or [] - nothing or unknown
                result = this.delegate.getRoots();
                if (result.length == 0) {
                    //nothing or unkown
                    if (this.sourceRoot != null && GlobalPathRegistry.getDefault().getSourceRoots().contains(this.sourceRoot)) {
                        //nothing
                        result = new FileObject[] {this.sourceRoot};
                    }
                    else {
                        //unknown
                        result = new FileObject[0];
                    }
                }
            }else {
                //No - unknown file - treat it like a source root
                if (this.sourceRoot == null) {
                    result = new FileObject[0];
                }
                else {
                    final FileObject[] aptRoots = resolveAptSourceCache(sourceRoot);
                    if (aptRoots.length == 0) {
                        result = new FileObject[] {this.sourceRoot};
                    }
                    else {
                        result = new FileObject[1+aptRoots.length];
                        result[0] = this.sourceRoot;
                        System.arraycopy(aptRoots, 0, result, 1, aptRoots.length);
                    }
                }
            }
            return result;
        }

        /**
         * Resolves the APT sources cache root
         * Depends on the JavaIndex rather than on AptCacheForSourceQuery due to performance reasons
         */
        private static FileObject[] resolveAptSourceCache(final FileObject sourceRoot) {
            try {
                final AnnotationProcessingQuery.Result result = AnnotationProcessingQuery.getAnnotationProcessingOptions(sourceRoot);
                final URL annotationOutputURL = result.sourceOutputDirectory();
                final FileObject userAnnotationOutput = annotationOutputURL == null ? null : URLMapper.findFileObject(annotationOutputURL);
                final FileObject cacheAnnoationOutput = FileUtil.toFileObject(JavaIndex.getAptFolder(sourceRoot.toURL(), false));
                return userAnnotationOutput == null ?
                    cacheAnnoationOutput == null ?
                        new FileObject[0] :
                        new FileObject[] {cacheAnnoationOutput}
                    : cacheAnnoationOutput == null ?
                        new FileObject[] {userAnnotationOutput} :
                        new FileObject[] {userAnnotationOutput, cacheAnnoationOutput};
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
    }
}
