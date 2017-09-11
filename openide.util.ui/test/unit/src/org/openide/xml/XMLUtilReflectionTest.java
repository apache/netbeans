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

package org.openide.xml;

import java.io.*;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.Assert;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 *
 * @author  Jaroslav Tulach
 */
public class XMLUtilReflectionTest extends org.netbeans.junit.NbTestCase {
    private CharSequence log;

    public XMLUtilReflectionTest (String name) {
        super (name);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir();
        CountingSecurityManager.initialize();
    }

    public void testAccessToParserBuilders() throws Exception {
        assertParse(false, false);
        assertParse(false, true);
        assertParse(true, false);
        assertParse(true, true);
        CountingSecurityManager.assertMembers(4);
    }

    public void testAccessToParserBuildersAgain() throws Exception {
        testAccessToParserBuilders();
        assertParse(false, false);
        assertParse(false, true);
        assertParse(true, false);
        assertParse(true, true);
        CountingSecurityManager.assertMembers(0);
    }

    private static void assertParse(boolean validate, boolean namespace) throws Exception {
        try {
            XMLUtil.parse(new InputSource(new ByteArrayInputStream(new byte[0])), validate, namespace, null, null);
            fail("should fail with SAX ex");
        } catch (SAXParseException ex) {
            // OK
        }
    }

    public void testAccessToSAXBuilders() throws Exception {
        assertSAX(false, false);
        assertSAX(false, true);
        assertSAX(true, false);
        assertSAX(true, true);
        CountingSecurityManager.assertMembers(4);
    }

    public void testAccessToSAXBuildersAgain() throws Exception {
        testAccessToSAXBuilders();
        assertSAX(false, false);
        assertSAX(false, true);
        assertSAX(true, false);
        assertSAX(true, true);
        CountingSecurityManager.assertMembers(0);
    }

    private static void assertSAX(boolean validate, boolean namespace) throws Exception {
        assertNotNull("Reader provider", XMLUtil.createXMLReader(validate, namespace));
    }
    
    static final class CountingSecurityManager extends SecurityManager {
        public static void initialize() {
            if (System.getSecurityManager() instanceof CountingSecurityManager) {
                // ok
            } else {
                System.setSecurityManager(new CountingSecurityManager());
            }
            members.clear();
        }

        static void assertMembers(int cnt) {
            int myCnt = 0;
            StringWriter w = new StringWriter();
            PrintWriter p = new PrintWriter(w);
            Set<Who> m;
            synchronized (members) {
                m = new TreeSet<Who>(members.values());
            }
            for (Who wh : m) {
                if (wh.isIgnore()) {
                    continue;
                }

                myCnt += wh.count;
                wh.printStackTrace(p);
                wh.count = 0;
            }
            if (myCnt > cnt) {
                Assert.fail("Expected at much " + cnt + " reflection efforts, but was: " + myCnt + "\n" + w);
            }
        }

        static Map<Class,Who> members = Collections.synchronizedMap(new HashMap<Class, Who>());
        @Override
        public void checkMemberAccess(Class<?> clazz, int which) {
            if (clazz == null) {
                assertMembers(which);
            }

            Who w = members.get(clazz);
            if (w == null) {
                w = new Who(clazz);
                members.put(clazz, w);
            }
            w.count++;
        }

        private static class Who extends Exception implements Comparable<Who> {
            int hashCode;
            final Class<?> clazz;
            int count;

            public Who(Class<?> who) {
                super("");
                this.clazz = who;
            }

            @Override
            public void printStackTrace(PrintWriter s) {
                s.println("Members of class " + clazz.getName() + " initialized " + count + " times");
                super.printStackTrace(s);
            }

            @Override
            public int hashCode() {
                if (hashCode != 0) {
                    return hashCode;
                }
                hashCode = clazz.hashCode();
                for (StackTraceElement stackTraceElement : getStackTrace()) {
                    hashCode = hashCode * 2 + stackTraceElement.hashCode();
                }
                return hashCode;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Who other = (Who) obj;
                if (this.clazz != other.clazz) {
                    return false;
                }
                if (this.hashCode() != other.hashCode()) {
                    return false;
                }
                return Arrays.equals(getStackTrace(), other.getStackTrace());
            }

            public int compareTo(Who o) {
                if (o == this) {
                    return 0;
                }
                if (o.count < this.count) {
                    return -1;
                }
                if (o.count > this.count) {
                    return 1;
                }
                return this.clazz.getName().compareTo(o.clazz.getName());
            }

            private boolean isIgnore() {
                for (StackTraceElement stackTraceElement : getStackTrace()) {
                    if (stackTraceElement.getClassName().startsWith("org.openide.loaders.XMLDataObject$")) {
                        return false;
                    }
                    if (stackTraceElement.getClassName().equals("org.openide.loaders.XMLDataObject")) {
                        return false;
                    }
                    if (stackTraceElement.getClassName().equals("org.openide.nodes.FilterNode")) {
                        return true;
                    }
                    if (stackTraceElement.getClassName().equals("org.openide.loaders.DataNode")) {
                        return true;
                    }
                }
                return true;
            }
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

        @Override
        public void checkPermission(Permission perm) {
        }
    }
    
}
