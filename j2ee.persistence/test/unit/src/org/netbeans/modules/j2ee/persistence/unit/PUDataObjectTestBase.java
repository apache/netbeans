/**
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

package org.netbeans.modules.j2ee.persistence.unit;

import java.net.URI;
import java.util.Enumeration;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Erno Mononen, Andrei Badea
 */
public abstract class PUDataObjectTestBase extends NbTestCase {

    private  static DummyProject project = new DummyProject();
    
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        ((Lkp)Lookup.getDefault()).setLookups(new Object[] { new PUMimeResolver(), new Pool(), new PUFOQI(project) });

        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        assertEquals("The default MIMEResolver is not our resolver!", PUMimeResolver.class, Lookup.getDefault().lookup(MIMEResolver.class).getClass());
        assertEquals("The default DataLoaderPool is not our pool!", Pool.class, Lookup.getDefault().lookup(DataLoaderPool.class).getClass());
    }

    public PUDataObjectTestBase(String name) {
        super(name);
        project.setDirectory(FileUtil.toFileObject(getDataDir()));
    }

    /**
     * Our default lookup.
     */
    public static final class Lkp extends ProxyLookup {

        public Lkp() {
            setLookups(new Object[0]);
        }

        public void setLookups(Object[] instances) {
            ClassLoader l = PersistenceEditorTestBase.class.getClassLoader();
            setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l)
            });
        }
    }

    /**
     * DataLoaderPool which is registered in the default lookup and loads
     * PUDataLoader.
     */
    public static final class Pool extends DataLoaderPool {

        public Enumeration loaders() {
            return Enumerations.singleton(new PUDataLoader());
        }
    }

    /**
     * MIME Resolver that associates persistence.xml with PUDataLoader.
     */
    public static final class PUMimeResolver extends MIMEResolver {

        public String findMIMEType(FileObject fo) {
            if (fo.getName().startsWith("persistence")){
                return PUDataLoader.REQUIRED_MIME;
            }
            return null;
        }
    }

    /**
     * Returns dummy project implementation. Needed since persistence unit needs
     * to be associated with {@link Project} owner. Also see issue #74426.
     */
    private static final class PUFOQI implements FileOwnerQueryImplementation {

        Project dummyProject;
        
        PUFOQI(Project dummy){
            dummyProject = dummy;
        }

        public Project getOwner(URI file) {
            return dummyProject;
        }

        public Project getOwner(FileObject file) {
            return dummyProject;
        }
    }
    
    private static class DummyProject implements Project{
        private FileObject dir;
        /**
         * Dummy project have to have not null dir for some editor kits, for example XMLKit from xml.text module
         * @param dir 
         */
        public void setDirectory(FileObject dir){
            this.dir = dir;
        }
        @Override
            public Lookup getLookup() { return Lookup.EMPTY; }
        @Override
            public FileObject getProjectDirectory() { return dir; }
    }
}
