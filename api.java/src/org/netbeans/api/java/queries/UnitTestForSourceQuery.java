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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.java.queries;

import java.net.URL;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 * Query to find Java package root of unit tests for Java package root of
 * sources and vice versa.
 *
 * @see org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation
 * @author David Konecny, Tomas Zezula
 * @since org.netbeans.api.java/1 1.4
 */
public class UnitTestForSourceQuery {

    @SuppressWarnings ("deprecation")   // NOI18N
    private static final Lookup.Result<? extends org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation.class);
    private static final Lookup.Result<? extends MultipleRootsUnitTestForSourceQueryImplementation> mrImplementations = 
        Lookup.getDefault().lookupResult(MultipleRootsUnitTestForSourceQueryImplementation.class);

    private UnitTestForSourceQuery() {
    }

    /**
     * Returns the test root for a given source root.
     *
     * @param source Java package root with sources
     * @return corresponding Java package root with unit tests. The
     *     returned URL does not have to point to existing file. It can be null
     *     when the mapping from source to unit test is not known.
     * @deprecated Use {@link #findUnitTests} instead.
     */
    public static URL findUnitTest(FileObject source) {
        URL[] result = findUnitTests (source);
        return result.length == 0 ? null : result[0];
    }


    /**
     * Returns the test roots for a given source root.
     *
     * @param source Java package root with sources
     * @return corresponding Java package roots with unit tests. The
     *     returned URLs do not have to point to existing files. It can be an empty
     *     array (but not null) when the mapping from source to unit tests is not known.
     * @since org.netbeans.api.java/1 1.7
     */
    @SuppressWarnings ("deprecation")   // NOI18N
    public static URL[] findUnitTests(FileObject source) {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source cannot be null"); // NOI18N
        }
        for (MultipleRootsUnitTestForSourceQueryImplementation query : mrImplementations.allInstances()) {            
            URL[] urls = query.findUnitTests(source);
            if (urls != null) {
                return urls;
            }
        }
        for (org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation query : implementations.allInstances()) {
            URL u = query.findUnitTest(source);
            if (u != null) {
                return new URL[] {u};
            }
        }
        return new URL[0];
    }

    /**
     * Returns the source root for a given test root.
     *
     * @param unitTest Java package root with unit tests
     * @return corresponding Java package root with sources. It can be null
     *     when the mapping from unit test to source is not known.
     * @deprecated Use {@link #findSources} instead.
     */
    public static URL findSource(FileObject unitTest) {
        URL[] result =  findSources (unitTest);
        return result.length == 0 ? null : result[0];
    }

    /**
     * Returns the source roots for a given test root.
     *
     * @param unitTest Java package root with unit tests
     * @return corresponding Java package roots with sources. It can be an empty array (not null)
     *     when the mapping from unit test to sources is not known.
     * @since org.netbeans.api.java/1 1.7
     */
    @SuppressWarnings ("deprecation")   // NOI18N
    public static URL[] findSources (FileObject unitTest) {
        if (unitTest == null) {
            throw new IllegalArgumentException("Parameter unitTest cannot be null"); // NOI18N
        }
        for (MultipleRootsUnitTestForSourceQueryImplementation query : mrImplementations.allInstances()) {
            URL[] urls = query.findSources(unitTest);
            if (urls != null) {
                return urls;
            }
        }
        for (org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation query : implementations.allInstances()) {            
            URL u = query.findSource(unitTest);
            if (u != null) {
                return new URL[] {u};
            }
        }
        return new URL[0];
    }

}
