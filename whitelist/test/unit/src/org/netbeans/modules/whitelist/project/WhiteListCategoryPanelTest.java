/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.whitelist.project;


import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 * @author Tomas Zezula
 */
public class WhiteListCategoryPanelTest extends NbTestCase {

    public WhiteListCategoryPanelTest(final String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }

    public void testWhiteListLookup() throws Exception {
        createWhiteListsFolder(Query1.class, Query2.class);
        final FileObject home = FileUtil.toFileObject(getWorkDir());
        final Project p = new MockProject(home);
        Lookup lkp = WhiteListLookupProvider.getEnabledUserSelectableWhiteLists(p);
        assertNotNull(lkp);
        assertTrue(lkp.lookupAll(WhiteListQueryImplementation.class).isEmpty());
        WhiteListLookupProvider.enableWhiteListInProject(p, Query1.class.getSimpleName(), true);
        Collection<? extends WhiteListQueryImplementation> items = lkp.lookupAll(WhiteListQueryImplementation.class);
        assertEquals(1,items.size());
        assertEquals(Query1.class,items.iterator().next().getClass());
        final Reference<Lookup> wr = new WeakReference<Lookup>(lkp);
        lkp = null;
        assertGC("Lookup gced", wr);    //NOI18N
        lkp = WhiteListLookupProvider.getEnabledUserSelectableWhiteLists(p);
        assertNotNull(lkp);
        items = lkp.lookupAll(WhiteListQueryImplementation.UserSelectable.class);
        assertEquals(1,items.size());
        assertEquals(Query1.class,items.iterator().next().getClass());
    }

    public void testDeadlock203187() throws Exception {
        createWhiteListsFolder(Query1.class, Query2.class);
        final FileObject home = FileUtil.toFileObject(getWorkDir());
        final Project p = new MockProject(home);
        final Lookup lkp = WhiteListLookupProvider.getEnabledUserSelectableWhiteLists(p);
        assertNotNull(lkp);
        final Object lck = new Object();
        final CountDownLatch l1 = new CountDownLatch(1);
        final CountDownLatch l2 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Lookup.Result<? extends WhiteListQueryImplementation> res = lkp.lookupResult(WhiteListQueryImplementation.class);
                res.addLookupListener(new LookupListener() {
                    @Override
                    public void resultChanged(LookupEvent ev) {
                        l1.countDown();
                        try {
                            l2.await();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        synchronized(lck) {
                            lkp.getClass();
                        }
                    }
                });
                res.allInstances();
                WhiteListLookupProvider.enableWhiteListInProject(p, Query1.class.getSimpleName(), true);
            }
        }).start();
        synchronized (lck) {
            l1.await();
            l2.countDown();
            lkp.lookup(WhiteListQueryImplementation.class);
        }
    }

    private static void createWhiteListsFolder(Class<? extends WhiteListQueryImplementation.UserSelectable>... queries) throws IOException {
        final FileObject root = FileUtil.getConfigRoot();
        final FileObject folder = FileUtil.createFolder(root,"org-netbeans-api-java/whitelists/");  //NOI18N
        final DataFolder target = DataFolder.findFolder(folder);
        for (Class<? extends WhiteListQueryImplementation.UserSelectable> q : queries) {
            InstanceDataObject.create(target, q.getSimpleName(), q);
        }
    }

    private final class MockAux implements AuxiliaryConfiguration, AuxiliaryProperties {

        private final Map<String,String> projProp = new HashMap<String, String>();
        private final Map<String,String> privProp = new HashMap<String, String>();
        private final Document projDoc;
        private final Document privDoc;

        private MockAux() throws ParserConfigurationException {
            projDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            privDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }

        @Override
        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            final Document doc = shared ? projDoc : privDoc;
            final NodeList nodeList = doc.getElementsByTagNameNS(namespace, elementName);
            assert nodeList.getLength() >= 0 && nodeList.getLength()<= 1;
            return (Element) (nodeList.getLength() == 0 ? null : nodeList.item(0));
        }

        @Override
        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            final Document doc = shared ? projDoc : privDoc;
            doc.appendChild(fragment);
        }

        @Override
        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            final Document doc = shared ? projDoc : privDoc;
            final NodeList nodeList = doc.getElementsByTagNameNS(namespace, elementName);
            assert nodeList.getLength() >= 0 && nodeList.getLength()<= 1;
            boolean changed = false;
            for (int i=0; i< nodeList.getLength(); i++) {
                doc.removeChild(nodeList.item(i));
                changed = true;
            }
            return changed;
        }

        @Override
        public String get(String key, boolean shared) {
            final Map<String,String> map = shared ? projProp : privProp;
            return map.get(key);
        }

        @Override
        public void put(String key, String value, boolean shared) {
            final Map<String,String> map = shared ? projProp : privProp;
            map.put(key, value);
        }

        @Override
        public Iterable<String> listKeys(boolean shared) {
            final Map<String,String> map = shared ? projProp : privProp;
            return Collections.unmodifiableSet(new HashSet<String>(map.keySet()));
        }

    }

    private final class MockProject implements Project {

        private final FileObject home;
        private final Lookup lkp;

        private MockProject (final FileObject home) throws ParserConfigurationException {
            assert home != null;
            this.home = home;
            this.lkp = Lookups.fixed(new MockAux());
        }

        @Override
        public FileObject getProjectDirectory() {
           return home;
        }

        @Override
        public Lookup getLookup() {
            return lkp;
        }
    }

    public static class QueryBase implements WhiteListQueryImplementation.UserSelectable {

        protected QueryBase() {}

        @Override
        public String getDisplayName() {
            return getId();
        }

        @Override
        public String getId() {
            return getClass().getSimpleName();
        }

        @Override
        public WhiteListImplementation getWhiteList(FileObject file) {
            return null;
        }
    }

    public static final class Query1 extends QueryBase {
    }

    public static final class Query2 extends QueryBase {
    }
}
