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

package org.netbeans.spi.java.queries;

import java.net.URL;
import org.openide.filesystems.FileObject;

/**
 * Query to find Java package root of unit tests for Java package root of
 * sources and vice versa.
 *
 * <p>A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the binary file and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.</p>
 *
 * <p>This interface assumes following mapping pattern between source
 * files and unit tests: <code>*.java -> *Test.java</code>. This mapping
 * is used for example for unit test generation and for searching test for
 * source. Usage of any other pattern will break this functionality.</p>
 *
 * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup"><code>Project.getLookup()</code></a>
 * @see org.netbeans.api.java.queries.UnitTestForSourceQuery
 * @deprecated Use {@link org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation} instead.
 * @author David Konecny
 * @since org.netbeans.api.java/1 1.4
 */
@Deprecated
public interface UnitTestForSourceQueryImplementation {
    
    /**
     * Returns the test root for a given source root.
     *
     * @param source a Java package root with sources
     * @return a corresponding Java package root with unit tests. The
     *     returned URL need not point to an existing folder. It can be null
     *     when no mapping from source to unit test is known.
     */
    URL findUnitTest(FileObject source);
    
    /**
     * Returns the source root for a given test root.
     *
     * @param unitTest a Java package root with unit tests
     * @return a corresponding Java package root with sources. It can be null
     *     when no mapping from unit test to source is known.
     */
    URL findSource(FileObject unitTest);
    
}
