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

package org.openide.filesystems;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup.Pair;

/**
 * Trying to mimic IZ 49418.
 *
 * @author Radek Matous
 */
public class MIMESupport49418Test extends NbTestCase {
    private FileSystem lfs;
    private static FileObject mimeFo;
    private static final String MIME_TYPE = "text/x-opqr";

    public MIMESupport49418Test(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport49418Test$Lkp");
        super.setUp();
        assertEquals("Our lookup is registered", Lkp.class, Lookup.getDefault().getClass());
        lfs = TestUtilHid.createLocalFileSystem(getName(), new String[]{"A.opqr", });
        mimeFo = lfs.findResource("A.opqr");
        assertNotNull(mimeFo);
    }


    public void testMIMEResolution()
            throws Exception {
        assertNull(Lookup.getDefault().lookup(Runnable.class));
        assertEquals(MIME_TYPE, mimeFo.getMIMEType());

    }

    /**
     * This is a pair that as a part of its instanceOf method queries the URL resolver.
     */
    @SuppressWarnings("unchecked")
    private static class QueryingPair extends Pair {
        public boolean beBroken;

        public String getId() {
            return getType().toString();
        }

        public String getDisplayName() {
            return getId();
        }

        public Class getType() {
            return getClass();
        }

        protected boolean creatorOf(Object obj) {
            return obj == this;
        }

        protected boolean instanceOf(Class c) {
            if (beBroken) {
                beBroken = false;
                assertEquals("content/unknown", mimeFo.getMIMEType());

            }
            return c.isAssignableFrom(getType());
        }

        public Object getInstance() {
            return this;
        }
    }


    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private static org.openide.util.lookup.InstanceContent ic;

        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }

        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }

        protected void initialize() {
            // a small trick to make the InheritanceTree storage to be used
            // because if the amount of elements in small, the ArrayStorage is 
            // used and it does not have the same problems like InheritanceTree
            for (int i = 0; i < 1000; i++) {
                ic.add(new Integer(i));
            }

            QueryingPair qp = new QueryingPair();
            ic.addPair(qp);
            ic.add(new MIMEResolver() {
                public String findMIMEType(FileObject fo) {
                    return MIME_TYPE;
                }
            });


            qp.beBroken = true;
        }

    } // end of Lkp
}
