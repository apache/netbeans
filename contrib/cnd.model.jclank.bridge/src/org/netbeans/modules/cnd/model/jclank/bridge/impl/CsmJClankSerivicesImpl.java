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
