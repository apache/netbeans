/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.model.jclank.bridge.impl;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.clang.tools.services.*;
import org.clang.tools.services.support.*;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.openide.windows.OutputWriter;

/**
 *
 */
public final class CsmJClankSerivicesImpl {

    public static final boolean TRACE = false;

    public static APTTokenStream getAPTTokenStream(NativeFileItem nfi) {
        throw new UnsupportedOperationException();
    }

    public static void traceCompilationDB(Set<NativeProject> projects,
            raw_ostream out, raw_ostream err, boolean useURL,
            ClankProgressHandler handle, final AtomicBoolean cancelled) {
        assert out != null;
        assert err != null;
        Collection<ClankCompilationDataBase> dbs = CsmJClankCompilationDB.convertProjects(projects, useURL);
        ClankRunSettings settings = new ClankRunSettings();
        settings.out = out;
        settings.err = err;
        settings.progress = handle;
        settings.cancelled = new Interrupter() {
            @Override
            public boolean isCancelled() {
                return cancelled.get();
            }
        };
        ClankPreprocessorServices.dumpCompilations(dbs, settings);
    }

    public static void preprocess(Collection<NativeProject> projects,
            raw_ostream out, raw_ostream err,
            ClankProgressHandler handle, final AtomicBoolean cancelled) {
        assert out != null;
        assert err != null;
        Collection<ClankCompilationDataBase> dbs = CsmJClankCompilationDB.convertProjects(projects, true);
        ClankRunPreprocessorSettings settings = new ClankRunPreprocessorSettings();
        settings.out = out;
        settings.err = err;
        settings.TraceClankStatistics = true;
        settings.TraceStatisticsOS = out;
        settings.cancelled = new Interrupter() {
            @Override
            public boolean isCancelled() {
                return cancelled.get();
            }
        };
        settings.IncludeInfoCallbacks = new CollectIncludeInfoCallback(err);
        ClankPreprocessorServices.preprocess(dbs, settings);
    }

    public static void dumpTokens(NativeFileItem nfi) {
//        raw_ostream llvm_err = llvm.errs();
//        Preprocessor /*&*/ PP = ClankPreprocessorServices.getPreprocessor(CsmJClankCompilationDB.createEntry(nfi), llvm_err);
//        if (PP != null) {
//            // Start preprocessing the specified input file.
//            Token Tok/*J*/ = new Token();
//            PP.EnterMainSourceFile();
//            do {
//                PP.Lex(Tok);
//                PP.DumpToken(Tok, true, llvm_err);
//                llvm_err.$out($("\n"));
//            } while (Tok.isNot(tok.TokenKind.eof));
//        }
    }

    public static void dumpPreprocessed(NativeFileItem nfi,
            PrintWriter out, OutputWriter err,
            boolean printTokens,
            boolean printStatistics) {
        raw_ostream llvm_out = new PrintWriter_ostream(out);
        raw_ostream llvm_err = (err != null) ? new PrintWriter_ostream(err) : llvm_out;
        ClankRunPreprocessorSettings settings = new ClankRunPreprocessorSettings();
        settings.out = llvm_out;
        settings.err = llvm_err;
        settings.PrettyPrintTokens = printTokens;
        settings.TraceClankStatistics = printStatistics;
        settings.TraceStatisticsOS = llvm_out;
        settings.IncludeInfoCallbacks = new CollectIncludeInfoCallback(llvm_err);
        ClankCompilationDataBase db = CsmJClankCompilationDB.convertNativeFileItems(Collections.singletonList(nfi), nfi.getName(), true);
        ClankPreprocessorServices.preprocess(Collections.singletonList(db), settings);
    }

    private static class CollectIncludeInfoCallback extends FileInfoCallback {

        public CollectIncludeInfoCallback(raw_ostream llvm_err) {
            super(llvm_err);
        }
    }

}
