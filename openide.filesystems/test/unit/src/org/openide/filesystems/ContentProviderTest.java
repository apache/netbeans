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
package org.openide.filesystems;

import java.net.URL;
import java.util.Collection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.Repository.LayerProvider;
import org.openide.util.Lookup;

public class ContentProviderTest extends NbTestCase {
    static {
        MockServices.setServices(MyProvider.class);
    }

    public ContentProviderTest(String name) {
        super(name);
    }

    @RandomlyFails // http://deadlock.netbeans.org/job/NB-Core-Build/9882/testReport/
    public void testCheckAFileFromOurLayer() {
        FileObject fo = FileUtil.getConfigFile("foo/bar");
        assertNotNull("foo/bar is provided", fo);
        assertEquals("value is val", "val", fo.getAttribute("x"));
    }
    
    public void testRefreshTheProvider() throws Exception {
        MyProvider my = Lookup.getDefault().lookup(MyProvider.class);
        assertNotNull("My provider found", my);
        
        my.clear();
        
        FileObject fo = FileUtil.getConfigFile("foo/bar");
        assertNull("foo/bar is no longer available", fo);
        
    }
    
    public static final class MyProvider extends LayerProvider {
        private boolean empty;
        
        @Override
        protected void registerLayers(Collection<? super URL> context) {
            assertTrue("Context is empty: " + context, context.isEmpty());
            if (empty) {
                return;
            }
            context.add(ContentProviderTest.class.getResource("test-layer-attribs.xml"));
        }

        final void clear() throws Exception {
            empty = true;
            refresh();
        }
    }
}
