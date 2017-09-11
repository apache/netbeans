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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test for merging action providers.
 * @author Jesse Glick
 */
public class LookupMergerImplTest extends NbTestCase {

    private static List<String> targetsRun = new ArrayList<String>();
    static {
        Actions.TARGET_RUNNER = new Actions.TargetRunner() {
            public void runTarget(FileObject scriptFile, String[] targetNameArray, Properties props, ActionProgress listener) {
                targetsRun.add(scriptFile.getNameExt() + ":" + Arrays.toString(targetNameArray) + ":" + new TreeMap<Object,Object>(props));
            }
        };
    }

    /**
     * Create test.
     * @param name test name
     */
    public LookupMergerImplTest(String name) {
        super(name);
    }

    /**
     * Clear everything up.
     * @throws Exception for whatever reason
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        targetsRun.clear();
        clearWorkDir();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    /**
     * Test that natures can add action behaviors, but not to the exclusion of the default impl.
     * @throws Exception for various reasons
     */
    public void testActionBindingFromNatures() throws Exception {
        File base = getWorkDir();
        File src = new File(base, "src");
        File x1 = new File(src, "x1");
        FileObject x1fo = FileUtil.createData(x1);
        File x2 = new File(src, "x2");
        FileObject x2fo = FileUtil.createData(x2);
        File y1 = new File(src, "y1");
        FileObject y1fo = FileUtil.createData(y1);
        File y2 = new File(src, "y2");
        FileObject y2fo = FileUtil.createData(y2);
        File buildXml = new File(base, "build.xml");
        FileUtil.createData(buildXml);
        AntProjectHelper helper = FreeformProjectGenerator.createProject(base, base, getName(), buildXml);
        FreeformProject p = (FreeformProject) ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        FreeformProjectGenerator.TargetMapping tm = new FreeformProjectGenerator.TargetMapping();
        final String cmd = "twiddle-file";
        tm.name = cmd;
        tm.targets = Arrays.asList("twiddle");
        FreeformProjectGenerator.TargetMapping.Context context = new FreeformProjectGenerator.TargetMapping.Context();
        tm.context = context;
        context.folder = "src";
        context.format = "relative-path";
        context.property = "file";
        context.pattern = "^x";
        context.separator = null;
        FreeformProjectGenerator.putTargetMappings(helper, Arrays.asList(tm));
        final boolean[] ranMockAction = {false};
        class MockActionProvider implements ActionProvider { // similar to JavaActions
            public String[] getSupportedActions() {
                return new String[] {cmd};
            }
            public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
                ranMockAction[0] = true;
            }
            public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
                DataObject d = context.lookup(DataObject.class);
                FileObject f = d.getPrimaryFile();
                return f != null && !f.getNameExt().contains("2");
            }
        }
        ActionProvider proxy = new LookupMergerImpl().merge(Lookups.fixed(new MockActionProvider(), new Actions(p)));
        assertTrue(Arrays.asList(proxy.getSupportedActions()).contains(cmd));
        Lookup selection = Lookups.singleton(DataObject.find(x1fo));
        assertTrue(proxy.isActionEnabled(cmd, selection));
        proxy.invokeAction(cmd, selection);
        assertEquals("[build.xml:[twiddle]:{file=x1}]", targetsRun.toString());
        assertFalse(ranMockAction[0]);
        targetsRun.clear();
        selection = Lookups.singleton(DataObject.find(x2fo));
        assertTrue(proxy.isActionEnabled(cmd, selection));
        proxy.invokeAction(cmd, selection);
        assertEquals("[build.xml:[twiddle]:{file=x2}]", targetsRun.toString());
        assertFalse(ranMockAction[0]);
        targetsRun.clear();
        selection = Lookups.singleton(DataObject.find(y1fo));
        assertTrue(proxy.isActionEnabled(cmd, selection));
        proxy.invokeAction(cmd, selection);
        assertEquals("[]", targetsRun.toString());
        assertTrue(ranMockAction[0]);
        selection = Lookups.singleton(DataObject.find(y2fo));
        assertFalse(proxy.isActionEnabled(cmd, selection));
    }

}
