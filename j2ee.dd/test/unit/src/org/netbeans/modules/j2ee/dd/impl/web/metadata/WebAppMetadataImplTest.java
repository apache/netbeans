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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.dd.impl.web.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebFragment;
import org.netbeans.modules.j2ee.dd.api.web.WebFragmentProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Petr Slechta
 */
public class WebAppMetadataImplTest extends NbTestCase {

    private FileObject dataFolder;

    public WebAppMetadataImplTest(String testName) {
        super(testName);
    }

    @Before
    @Override
    public void setUp() {
        System.out.println("setUp() .......................");
        dataFolder = FileUtil.toFileObject(getDataDir());
        assertTrue("dataFolder not found", dataFolder != null);
    }

    /**
     * Fragments: A B C D E F
     * Constraints: (relative ordering) O<A O<C B<O O<C F<O F<B  (O=others)
     * Expected sort result: F B D E C A
     */
    @Test
    public void testSortFragments1a() {
        System.out.println("testSortFragments1a() .........");
        List<WebFragment> list = getFragments(1, new String[] {"A","B","C","D","E","F"});
        testOrder(list, new String[] {"A","B","C","D","E","F"});
        List<WebFragment> sorted = WebAppMetadataImpl.sortFragments(null, list);
        testOrder(sorted, new String[] {"F","B","D","E","C","A"});
    }

    /**
     * Fragments: web.xml A B C D
     * Constraints: (absolute ordering) C A
     * Expected sort result: C A
     */
    @Test
    public void testSortFragments1b() {
        System.out.println("testSortFragments1b() .........");
        List<WebFragment> list = getFragments(1, new String[] {"A","B","C","D"});
        testOrder(list, new String[] {"A","B","C","D"});
        WebApp webXml = getWebXml(1, "A");
        List<WebFragment> sorted = WebAppMetadataImpl.sortFragments(webXml, list);
        testOrder(sorted, new String[] {"C","A"});
    }

    /**
     * Fragments: web.xml A B C D
     * Constraints: (absolute ordering) C others A
     * Expected sort result: C B D A
     */
    @Test
    public void testSortFragments1c() {
        System.out.println("testSortFragments1c() .........");
        List<WebFragment> list = getFragments(1, new String[] {"A","B","C","D"});
        testOrder(list, new String[] {"A","B","C","D"});
        WebApp webXml = getWebXml(1, "B");
        List<WebFragment> sorted = WebAppMetadataImpl.sortFragments(webXml, list);
        testOrder(sorted, new String[] {"C","B","D","A"});
    }

    /**
     * Fragments: A B C D E F
     * Constraints: (relative ordering) O<A A<C B<O O<D E<O  (O=others)
     * Expected sort result: B E F A C D
     */
    @Test
    public void testSortFragments2a() {
        System.out.println("testSortFragments2a() .........");
        List<WebFragment> list = getFragments(2, new String[] {"A","B","C","D","E","F"});
        testOrder(list, new String[] {"A","B","C","D","E","F"});
        List<WebFragment> sorted = WebAppMetadataImpl.sortFragments(null, list);
        testOrder(sorted, new String[] {"B","E","F","A","C","D"});
    }

    /**
     * Fragments: web.xml A B C D E F
     * Constraints: (absolute ordering) F D C B
     * Expected sort result: F D C B
     */
    @Test
    public void testSortFragments2b() {
        System.out.println("testSortFragments2b() .........");
        List<WebFragment> list = getFragments(2, new String[] {"A","B","C","D","E","F"});
        testOrder(list, new String[] {"A","B","C","D","E","F"});
        WebApp webXml = getWebXml(2, "A");
        List<WebFragment> sorted = WebAppMetadataImpl.sortFragments(webXml, list);
        testOrder(sorted, new String[] {"F","D","C","B"});
    }

    /**
     * Fragments: web.xml A B C D E F
     * Constraints: (absolute ordering) F D others C B
     * Expected sort result: F D A E C B
     */
    @Test
    public void testSortFragments2c() {
        System.out.println("testSortFragments2c() .........");
        List<WebFragment> list = getFragments(1, new String[] {"A","B","C","D","E","F"});
        testOrder(list, new String[] {"A","B","C","D","E","F"});
        WebApp webXml = getWebXml(2, "B");
        List<WebFragment> sorted = WebAppMetadataImpl.sortFragments(webXml, list);
        testOrder(sorted, new String[] {"F","D","A","E","C","B"});
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------
    private void testOrder(List<WebFragment> list, String[] order) {
        int i = 0;
        for (WebFragment f : list) {
            try {
                String[] names = f.getName();
                String name = names != null && names.length > 0 ? names[0] : null;
                assertTrue("fragment "+i+" does not have a name: "+list, name != null);
                assertTrue("fragment "+i+" in list has wrong order. expected: "+order[i]+", found: "+name, order[i].equals(name));
            } catch (VersionNotSupportedException ex) {
                throw new AssertionFailedErrorException("getName() failed for fragment "+i+" in "+list, ex);
            }
            i++;
        }
    }

    private WebApp getWebXml(int testNo, String name) {
        String fileName = "fragments-test"+testNo+"/web"+name+".xml";
        FileObject fo = dataFolder.getFileObject(fileName);
        assertTrue("web.xml '"+fileName+"' not found", fo != null);

        try {
            return DDProvider.getDefault().getDDRoot(fo);
        }
        catch (IOException ex) {
            throw new AssertionFailedErrorException("getWebXml failed", ex);
        }
    }

    private List<WebFragment> getFragments(int testNo, String[] names) {
        List<WebFragment> res = new ArrayList<WebFragment>();
        for (String name : names) {
            WebFragment f = getFragment(testNo, name);
            res.add(f);
        }
        return res;
    }

    private WebFragment getFragment(int testNo, String name) {
        FileObject fo = getFragmentFile(testNo, name);
        try {
            return WebFragmentProvider.getDefault().getWebFragmentRoot(fo);
        } catch (IOException ex) {
            throw new AssertionFailedErrorException("getFragment failed", ex);
        }
    }

    private FileObject getFragmentFile(int testNo, String name) {
        String fileName = "fragments-test"+testNo+"/web-fragment"+name+".xml";
        FileObject fo = dataFolder.getFileObject(fileName);
        assertTrue("web fragment '"+fileName+"' not found", fo != null);
        return fo;
    }

}
