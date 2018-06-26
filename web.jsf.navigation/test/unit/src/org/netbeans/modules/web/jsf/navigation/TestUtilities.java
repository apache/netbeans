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
package org.netbeans.modules.web.jsf.navigation;


import java.io.File;
import junit.framework.Assert;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.ClassParser;
import org.netbeans.modules.java.source.parsing.ClassParserFactory;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;


public class TestUtilities extends ProxyLookup {
    
    private static TestUtilities DEFAULT;
    private static final Lookup PROJECTS;
    
    static {
        TestUtilities.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestUtilities.class.getName());
        Assert.assertEquals(TestUtilities.class, Lookup.getDefault().getClass());
        Lookup p = Lookups.forPath("Services/AntBasedProjectTypes/");
        p.lookupAll(AntBasedProjectType.class);
        PROJECTS = p;
        setLookup(new Object[0]);
    }

    public TestUtilities() {
        Assert.assertNull(DEFAULT);
        DEFAULT = this;
        ClassLoader l = TestUtilities.class.getClassLoader();
        setLookups(new Lookup[] {
            Lookups.metaInfServices(l),
            Lookups.singleton(l)
        });
    }

    /**
     * Set the global default lookup.
     * Caution: if you don't include Lookups.metaInfServices, you may have trouble,
     * e.g. {@link #makeScratchDir} will not work.
     */
    public static void setLookup(Lookup l) {
        DEFAULT.setLookups(new Lookup[] {l});
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object... instances) {
        ClassLoader l = TestUtilities.class.getClassLoader();
        DEFAULT.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(l),
            Lookups.singleton(l),
            PROJECTS
        });
    }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class JavacParserProvider implements MimeDataProvider {
        private Lookup javaLookup = Lookups.fixed(
            new JavacParserFactory(),
            new JavaCustomIndexer.Factory()
        );
        private Lookup classLookup = Lookups.fixed(
            new ClassParserFactory(),
            new JavaCustomIndexer.Factory()
        );

        @Override
        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().endsWith(JavacParser.MIME_TYPE)) {
                return javaLookup;
}
            if (mimePath.getPath().endsWith(ClassParser.MIME_TYPE)) {
                return classLookup;
            }
            return Lookup.EMPTY;
        }
    }

    @ServiceProvider(service = InstalledFileLocator.class)
    public static class InstalledFileLocatorImpl extends InstalledFileLocator{

        @Override
        public File locate( String arg0, String arg1, boolean arg2 ) {
            return null;
        }

    }

}
