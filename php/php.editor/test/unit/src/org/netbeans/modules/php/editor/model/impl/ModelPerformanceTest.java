/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.model.impl;

import java.io.IOException;
import java.util.Date;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.php.editor.model.Model;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ModelPerformanceTest extends ModelTestBase {

    public ModelPerformanceTest(String testName) {
        super(testName);
    }

    public void testModelPerformance() throws Exception {
        Source testSource = getTestSource("testfiles/model/performance/performance.php");
        Date start = new Date();
        Model model = getModel(testSource);
        Date end = new Date();
        long time = end.getTime() - start.getTime();
        System.out.println("Creating model takes: " + time);
        assertTrue(time < 3000);
    }

    @Override
    protected FileObject[] createSourceClassPathsForTest() {
        FileObject dataDir = FileUtil.toFileObject(getDataDir());
        try {
            return new FileObject[]{toFileObject(dataDir, "testfiles/model/performance", true)}; //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}
