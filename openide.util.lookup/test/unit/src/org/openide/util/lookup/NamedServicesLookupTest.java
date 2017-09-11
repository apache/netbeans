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

package org.openide.util.lookup;

import org.openide.util.Lookup;
import org.openide.util.lookup.implspi.ServiceLoaderLineTest;
import org.openide.util.test.MockLookup;


/** Test finding services from manifest.
 * @author Jaroslav Tulach
 */
public class NamedServicesLookupTest extends MetaInfServicesLookupTest {
    static {
        MockLookup.init();
    }
    private ClassLoader previousContextClassLoader;
    
    public NamedServicesLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        previousContextClassLoader = Thread.currentThread().getContextClassLoader();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(previousContextClassLoader);
        super.tearDown();
        ServiceLoaderLineTest.clearLookupsForPath();
    }
    
    

    @Override
    protected String prefix() {
        return "META-INF/namedservices/sub/path/";
    }
    
    @Override
    protected Lookup createLookup(ClassLoader c) {
        Thread.currentThread().setContextClassLoader(c);
        MockLookup.setInstances(c);
        Lookup l = Lookups.forPath("sub/path");
        return l;
    }
    
    //
    // this is not much inheriting test, as we mask most of the tested methods
    // anyway, but the infrastructure to generate the JAR files is useful
    //
    
    public @Override void testLoaderSkew() {}
    public @Override void testStability() throws Exception {}
    public @Override void testMaskingOfResources() throws Exception {}
    public @Override void testOrdering() throws Exception {}
    public @Override void testNoCallToGetResourceForObjectIssue65124() throws Exception {}
    public @Override void testSuperTypes() throws Exception {}
    public @Override void testSubTypes() throws Exception {}
    public @Override void testWrongOrderAsInIssue100320() throws Exception {}
    public @Override void testLookupObject() throws Exception {}
}
