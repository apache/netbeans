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

package org.apache.tools.ant.module.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.loader.AntProjectDataObject;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockChangeListener;

// XXX testBasicParsing
// XXX testMinimumChangesFired

/**
 * Test {@link AntProjectSupport} parsing functionality.
 * @author Jesse Glick
 */
public class AntProjectSupportTest extends NbTestCase {
    
    public AntProjectSupportTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    private FileObject scratch;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File scratchF = getWorkDir();
        scratch = FileUtil.toFileObject(scratchF);
        assertNotNull("FO for " + scratchF, scratch);
    }

    @RandomlyFails // NB-Core-Build #3638
    public void testInitiallyInvalidScript() throws Exception {
        final FileObject fo = scratch.createData("x.ant");
        assertEquals("it is an APDO", AntProjectDataObject.class, DataObject.find(fo).getClass());
        AntProjectCookie apc = new AntProjectSupport(fo);
        assertNull("invalid", apc.getDocument());
        assertNotNull("invalid", apc.getParseException());
        MockChangeListener l = new MockChangeListener();
        apc.addChangeListener(l);
        //l.expectNoEvents(5000);
        fo.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                OutputStream os = fo.getOutputStream();
                try {
                    os.write("<project default='x'><target name='x'/></project>".getBytes("UTF-8"));
                } finally {
                    os.close();
                }
            }
        });
        l.expectEvent(5000);
        /* XXX parseException usually turns out to be non-null here still; too many threads running around, who knows what is going on
        Throwable x = apc.getParseException();
        if (x != null) {
            throw (AssertionFailedError) new AssertionFailedError("now valid (no exc)").initCause(x);
        }
        Document doc = apc.getDocument();
        assertNotNull("now valid (have doc)", doc);
        assertEquals("one target", 1, doc.getElementsByTagName("target").getLength());
         */
    }
    
}
