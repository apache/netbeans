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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javahelp;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.javahelp.Help;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

public class HelpCtxProcessorTest extends NbTestCase {

    public HelpCtxProcessorTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testProcess() throws Exception {
        final AtomicReference<HelpCtx> ctx = new AtomicReference<HelpCtx>();
        final AtomicBoolean showmaster = new AtomicBoolean();
        MockLookup.setLayersAndInstances(new Help() {
            public @Override Boolean isValidID(String id, boolean force) {
                return true;
            }
            public @Override void showHelp(HelpCtx _ctx, boolean _showmaster) {
                ctx.set(_ctx);
                showmaster.set(_showmaster);
            }
            public @Override void addChangeListener(ChangeListener l) {}
            public @Override void removeChangeListener(ChangeListener l) {}
        });
        File fooF = new File(getWorkDir(), "foo.xml");
        TestFileUtils.writeFile(fooF, "<!DOCTYPE helpctx PUBLIC '-//NetBeans//DTD Help Context 1.0//EN' '.../helpcontext-1_0.dtd'><helpctx id='foo' showmaster='true'/>");
        FileObject foo = FileUtil.toFileObject(fooF);
        assertNotNull(foo);
        Lookup lkp = DataObject.find(foo).getLookup();
        InstanceCookie i = lkp.lookup(InstanceCookie.class);
        assertNotNull(lkp.lookupAll(Object.class).toString(), i);
        Action a = (Action) i.instanceCreate();
        a.actionPerformed(null);
        assertEquals(new HelpCtx("foo"), ctx.get());
        assertTrue(showmaster.get());
    }

}
