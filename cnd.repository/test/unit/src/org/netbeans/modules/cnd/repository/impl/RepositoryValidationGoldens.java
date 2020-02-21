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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
