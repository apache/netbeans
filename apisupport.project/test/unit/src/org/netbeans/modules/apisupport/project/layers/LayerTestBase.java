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

package org.netbeans.modules.apisupport.project.layers;

import junit.framework.Assert;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.EntityCatalog;
import org.xml.sax.EntityResolver;

/**
 * Superclass for tests that work with XML layers.
 * Does some setup, since TAX requires some special infrastructure.
 * @author Jesse Glick
 * @see "#62363"
 */
@Deprecated
public abstract class LayerTestBase extends NbTestCase {

    public static final class Lkp extends ProxyLookup {
        // Copied from org.netbeans.api.project.TestUtil:
        static {
            // XXX replace with MockServices
            System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
            Assert.assertEquals(Lkp.class, Lookup.getDefault().getClass());
            Lookup p = Lookups.forPath("Services/AntBasedProjectTypes/");
            p.lookupAll(AntBasedProjectType.class);
            projects = p;
            setLookup(new Object[0]);
        }
        private static Lkp DEFAULT;
        private static final Lookup projects;
        public Lkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            setLookups(new Lookup[] {
                        Lookups.metaInfServices(l),
                        Lookups.singleton(l)
                    });
        }
        public static void setLookup(Object[] instances) {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
                projects
            });
        }
    }
    
    protected LayerTestBase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        Lkp.setLookup(new Object[] {
            new TestCatalog(),
        });
    }
    
    /**
     * In the actual IDE, the default NetBeans Catalog will already be "mounted", so just for testing:
     */
    private static final class TestCatalog extends UserCatalog {
        
        public TestCatalog() {}
        
        public EntityResolver getEntityResolver() {
            return EntityCatalog.getDefault();
        }
        
    }
    
}
