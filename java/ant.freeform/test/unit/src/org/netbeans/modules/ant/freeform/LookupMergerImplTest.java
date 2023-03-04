/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
