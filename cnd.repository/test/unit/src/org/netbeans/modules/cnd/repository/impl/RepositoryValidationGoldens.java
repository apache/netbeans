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

package org.netbeans.modules.cnd.repository.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * This test parses project on hard refs and dumps it's full model and internals.
 * Model is dumped as ModelBuiltFromRepository.out/ModelBuiltFromRepository.err files.
 * This test does not persist model into repository.
 */
public class RepositoryValidationGoldens extends RepositoryValidationBase {

    public RepositoryValidationGoldens(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.repository.hardrefs", Boolean.TRUE.toString()); //NOI18N
        System.setProperty("org.netbeans.modules.cnd.apt.level","OFF"); // NOI18N
        System.setProperty("cnd.skip.err.check", Boolean.TRUE.toString()); //NOI18N
        System.setProperty("cnd.dump.skip.dummy.forward.classifier", Boolean.TRUE.toString()); //NOI18N
        super.setUp();
    }

    public void testRepository() throws Exception {
        
        File workDir = getWorkDir();
        
        setGoldenDirectory(workDir.getAbsolutePath());
        
        PrintStream streamOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(workDir, nimi + ".out"))));
        PrintStream streamErr = new FilteredPrintStream(new BufferedOutputStream(new FileOutputStream(new File(workDir, nimi + ".err"))));

        List<String> args = find();
        assert args.size() > 0;
        //args.add("-fq"); //NOI18N
        doTest(args.toArray(new String[]{}), streamOut, streamErr);
        assertNoExceptions();
    }
}
