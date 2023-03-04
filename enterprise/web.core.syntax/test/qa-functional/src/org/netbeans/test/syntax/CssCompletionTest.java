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
package org.netbeans.test.syntax;

import java.io.File;
import org.netbeans.test.web.FileObjectFilter;
import org.openide.filesystems.FileObject;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.web.RecurrentSuiteFactory;

/**
 *
 * @author Jindrich Sedek
 */
public class CssCompletionTest extends CompletionTest {

    /** Creates a new instance of CompletionTesJ2EE */
    public CssCompletionTest() {}

    public CssCompletionTest(String name, FileObject testFileObj) {
        super(name, testFileObj);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
    }

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            super();
            FileObjectFilter filter = new FileObjectFilter() {

                public boolean accept(FileObject fo) {
                    String ext = fo.getExt();
                    String name = fo.getName();
                    return (name.startsWith("test") || name.startsWith("Test")) && (ext.equals("css"));
                }
            };
            addTest(RecurrentSuiteFactory.createSuite(CssCompletionTest.class,
                    new CssCompletionTest().getProjectsDir(), filter));
        }
    }

    @Override
    public void runTest() throws Exception {
        test(testFileObj, "/**CC", "*/", false);
    }

    @Override
    public File getProjectsDir() {
        File datadir = new CssCompletionTest(null, null).getDataDir();
        return new File(datadir, "CSSCompletionTestProjects");
    }


}
