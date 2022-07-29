/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.util.Lookup;

/**
 * A query to find Javadoc root for the given classpath root.
 * @author David Konecny, Jesse Glick
 * @since org.netbeans.api.java/1 1.4
 */
public class JavadocForBinaryQuery {

    private static final Logger LOG = Logger.getLogger(JavadocForBinaryQuery.class.getName());
    
    private static final Lookup.Result<? extends JavadocForBinaryQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(JavadocForBinaryQueryImplementation.class);

    private JavadocForBinaryQuery () {
    }

    /**
     * Find Javadoc information for a classpath root containing Java classes.
     * <p>
     * These methods calls findJavadoc method on the JavadocForBinaryQueryImplementation 
     * instances registered in the lookup until a non null result is returned for given binaryRoot.
     * </p>
     * @param binary URL of a classpath root
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findJavadoc(URL binary) {
        ClassPathSupport.createResource(binary); // just to check for IAE; XXX might be unnecessary since CP ctor now check this too        
        LOG.log(
            Level.FINE,
            "JFBQ.findJavadoc: {0}",    //NOI18N
            binary);
        for  (JavadocForBinaryQueryImplementation impl : implementations.allInstances()) {
            Result r = impl.findJavadoc(binary);
            if (r != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                        Level.FINE,
                        "  got result {0} from {1}",    //NOI18N
                        new Object[]{
                            Arrays.asList(r.getRoots()),
                            impl
                        });
                }
                return r;
            } else {
                LOG.log(
                    Level.FINE,
                    "  got no result from {0}", //NOI18N
                    impl);
            }
        }
        LOG.fine("  got no results from any impl"); //NOI18N
        return EMPTY_RESULT;        
    }

    /**
     * Result of finding Javadoc, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public interface Result {
        
        /**
         * Get the Javadoc roots.
         * Each root should contain the main <code>index.html</code>, so that
         * for a class <code>pkg.Class</code> the generated documentation would
         * have a path <code>pkg/Class.html</code> relative to one of the roots.
         * @return array of roots of Javadoc documentation (may be empty but not null)
         */
        URL[] getRoots();
        
        /**
         * Add a listener to changes in the roots.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Remove a listener to changes in the roots.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
        
    }
    
    private static final Result EMPTY_RESULT = new EmptyResult();
    private static final class EmptyResult implements Result {
        private static final URL[] NO_ROOTS = new URL[0];
        EmptyResult() {}
        public URL[] getRoots() {
            return NO_ROOTS;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }    
    
}
