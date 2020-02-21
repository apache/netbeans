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
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.junit.Manager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModel;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 */
@RandomlyFails
public class IncrementalParseRepositoryComposite extends RepositoryValidationBase {
    private static final RequestProcessor RP = new RequestProcessor("Sleep");
    private volatile boolean isShutdown = false;
    private volatile boolean dumpModel = true;
    private volatile long trueParsingTime = 0;

    public IncrementalParseRepositoryComposite(String testName) {
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

    @Override
    protected void parsingTime(TraceModel.TestResult time) {
        trueParsingTime =time.getTime();
    }

    public void testRepository() throws Exception {
        File workDir = getWorkDir();
        setGoldenDirectory(workDir.getAbsolutePath());

        PrintStream streamOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(workDir+"/..", nimi + ".out"))));
        PrintStream streamErr = new FilteredPrintStream(new BufferedOutputStream(new FileOutputStream(new File(workDir+"/..", nimi + ".err"))));

        List<String> args = find();
        assert args.size() > 0;
        //args.add("-fq"); //NOI18N
        long currentTimeMillis = System.currentTimeMillis();
        doTest(args.toArray(new String[]{}), streamOut, streamErr);
        assertNoExceptions();
        System.err.println("IncrementalParseRepositoryComposite: pure parsing took "+trueParsingTime+ " ms.");
        System.err.println("IncrementalParseRepositoryComposite: first (golden) pass took "+(System.currentTimeMillis()-currentTimeMillis)+ " ms.");
        getTestModelHelper().shutdown(true);
        //
        setUp2();
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!isShutdown) {
                        isShutdown = true;
                        getTestModelHelper().shutdown(false);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, 500);
        dumpModel = false;
        currentTimeMillis = System.currentTimeMillis();
        performTest2(args.toArray(new String[]{}), nimi + ".out", nimi + ".err");
        assertNoExceptions();
        System.err.println("IncrementalParseRepositoryComposite: pure parsing took "+trueParsingTime+ " ms.");
        System.err.println("IncrementalParseRepositoryComposite: second (interrupted) pass took "+(System.currentTimeMillis()-currentTimeMillis)+ " ms.");
        if (!isShutdown) {
            isShutdown = true;
            getTestModelHelper().shutdown(false);
        }
        //
        dumpModel = true;
        setUp3();
        currentTimeMillis = System.currentTimeMillis();
        try {
        performTest(args.toArray(new String[]{}), nimi + ".out", nimi + ".err");
            assertNoExceptions();
        } finally {
            System.err.println("IncrementalParseRepositoryComposite: pure parsing took "+trueParsingTime+ " ms.");
            System.err.println("IncrementalParseRepositoryComposite: last (finishing interrupted parse) pass took "+(System.currentTimeMillis()-currentTimeMillis)+ " ms.");
        }
    }

    private void setUp2() throws Exception {
        System.setProperty("cnd.repository.hardrefs", Boolean.FALSE.toString()); //NOI18N
        System.setProperty("org.netbeans.modules.cnd.apt.level","OFF"); // NOI18N
        assertNotNull("This test can only be run from suite", RepositoryValidationBase.getGoldenDirectory()); //NOI18N
        System.setProperty(PROPERTY_GOLDEN_PATH, RepositoryValidationBase.getGoldenDirectory());
        super.setUp();
    }

    private void setUp3() throws Exception {
        System.setProperty("cnd.repository.hardrefs", Boolean.FALSE.toString()); //NOI18N
        System.setProperty("org.netbeans.modules.cnd.apt.level", "OFF"); // NOI18N
        System.setProperty(PROPERTY_GOLDEN_PATH, RepositoryValidationBase.getGoldenDirectory());
        cleanCache = false;
        super.setUp();
    }

    private void performTest2(String[] args, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        File workDir = getWorkDir();

        File output = new File(workDir, goldenDataFileName);
        PrintStream streamOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(output)));
        File error = goldenErrFileName == null ? null : new File(workDir, goldenErrFileName);
        PrintStream streamErr = goldenErrFileName == null ? null : new FilteredPrintStream(new BufferedOutputStream(new FileOutputStream(error)));
        try {
            doTest(args, streamOut, streamErr, params);
        } finally {
            // restore err and out
            streamOut.close();
            if (streamErr != null) {
                streamErr.close();
            }
        }
    }

    @Override
    protected boolean returnOnShutdown() {
        if (!dumpModel) {
            if (CsmModelState.OFF == getTraceModel().getModel().getState()) {
                return true;
            }
            return false;
        } else {
            return super.returnOnShutdown();
        }
    }

    @Override
    protected boolean dumpModel() {
        return dumpModel;
    }

    @Override
    public File getGoldenFile(String filename) {
        String goldenDirPath = System.getProperty(PROPERTY_GOLDEN_PATH); // NOI18N
        if (goldenDirPath == null || goldenDirPath.length() == 0) {
            return super.getGoldenFile(filename);
        } else {
            return Manager.normalizeFile(new File(goldenDirPath+"/..", filename));
        }
    }
}
