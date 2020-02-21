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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.ClankPreprocessorServices;
import org.clang.tools.services.ClankRunPreprocessorSettings;
import org.clang.tools.services.spi.ClankMemoryBufferProvider;
import org.clang.tools.services.support.PrintWriter_ostream;
import org.llvm.support.MemoryBuffer;
import org.llvm.support.llvm;
import org.llvm.support.raw_ostream;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStream;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.spi.APTBufferProvider;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 */
public class ClankDriverImpl {

    public interface ClankPreprocessorOutputImplementation extends ClankDriver.ClankPreprocessorOutput {

        ClankPreprocessorOutputImplementation prepareCachesIfPossible();
    }

    static final boolean TRACE = false;
    
    public static void invalidateImpl(FileSystem fs, CharSequence absPath) {
        if (APTTraceFlags.USE_CLANK) {
            ClankPreprocessorServices.invalidate(CndFileSystemProvider.toUrl(fs, absPath));
        }
    }

    public static void invalidateImpl(APTFileBuffer buffer) {
        if (APTTraceFlags.USE_CLANK) {
            // TODO: split by file system?
            invalidateImpl(buffer.getFileSystem(), buffer.getAbsolutePath());
        }
    }

    public static void invalidateAllImpl() {
        if (APTTraceFlags.USE_CLANK) {
            // TODO: split by file system?
            ClankPreprocessorServices.invalidateAll();
        }
    }

    public static boolean preprocessImpl(APTFileBuffer buffer,
            PreprocHandler ppHandler,
            final ClankDriver.ClankPreprocessorCallback callback,
            final org.netbeans.modules.cnd.support.Interrupter interrupter) {

        if (APTTraceFlags.TRACE_PREPROC || APTTraceFlags.TRACE_PREPROC_STACKS) {
            String text = "Preprocessing " + buffer; //NOI18N
            if (APTTraceFlags.TRACE_PREPROC_STACKS) {
                new Exception(text).printStackTrace(System.err);
            } else {
                System.err.println(text);
            }
        }        
        try {
            // TODO: prepare buffers mapping in url-like style for remote files;
            // note that for local files no "file://" prefix is added
            String path = CndFileSystemProvider.toUrl(buffer.getFileSystem(), buffer.getAbsolutePath()).toString();
            // prepare params to run preprocessor
            ClankRunPreprocessorSettings settings = new ClankRunPreprocessorSettings();
            settings.WorkName = path;
            boolean fortranFlavor = APTToClankCompilationDB.isFortran(ppHandler);
            settings.GenerateDiagnostics = true;
            if (CndUtils.isUnitTestMode() && !fortranFlavor) {
                settings.PrettyPrintDiagnostics = true;
                PrintWriter printWriter = new PrintWriter(System.err);
                settings.PrintDiagnosticsOS = new PrintWriter_ostream(printWriter);
            } else {
                settings.PrettyPrintDiagnostics = false;
                settings.PrintDiagnosticsOS = llvm.nulls();
            }
            settings.TraceClankStatistics = false;
            ClankPPCallback.CancellableInterrupter canceller = new ClankPPCallback.CancellableInterrupter(interrupter);
            settings.cancelled = canceller;
            raw_ostream traceOS = CndUtils.isUnitTestMode() ? llvm.nulls() : llvm.errs();
            ClankPPCallback fileTokensCallback = new ClankPPCallback(ppHandler, traceOS, callback, canceller);
            settings.IncludeInfoCallbacks = fileTokensCallback;
            ClankCompilationDataBase db = APTToClankCompilationDB.convertPPHandler(ppHandler, path);
            ClankMemoryBufferProvider provider = Lookup.getDefault().lookup(ClankMemoryBufferProvider.class);
            Map<String, MemoryBuffer> remappedBuffers = provider.getRemappedBuffers();
            if (!remappedBuffers.containsKey(path)) {
                MemoryBuffer fileContent;
                char[] chars = fortranFlavor ? fixFortranTokens(buffer) : buffer.getCharBuffer();
                fileContent = ClankMemoryBufferImpl.create(path, chars);
                remappedBuffers = new HashMap<String, MemoryBuffer>(remappedBuffers);
                remappedBuffers.put(path, fileContent);
            }
            ClankPreprocessorServices.preprocess(Collections.singleton(db), settings, remappedBuffers);
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private static char[] fixFortranTokens(APTFileBuffer buffer) throws IOException {
        // Fortran has special string concatenation (//)
        // and a logical operators (.and.) which produces bad token stream.
        // The method replaces:
        // //     -> ~~
        // .not.  -> ^
        // .ne.   -> /=
        // .neqv. -> <>
        // .eq.   -> ==
        // .eqv.  -> ==
        // .gt.   -> >
        // .ge.   -> >=
        // .lt.   -> <
        // .le.   -> <=
        // .and.  -> &&
        // .or.   -> ||
        // The class APTFortranFilterEx converts tokens ~~, ^ and <> back to right Fortran tokens.
        char[] chars = buffer.getCharBuffer();
        int i = 0;
        while (true) {
            if (i >= chars.length - 1) {
                break;
            }
            if (i < chars.length - 1) {
                if (chars[i] == '/' && chars[i+1] == '/') {
                    chars[i] = '~';
                    chars[i+1] = '~';
                }
            }
            if (chars[i] == '.') {
                if (i < chars.length - 3) {
                    if ((chars[i+1] == 'n' || chars[i+1] == 'N') &&
                        (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                         chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '/';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'e' || chars[i+1] == 'E') &&
                               (chars[i+2] == 'q' || chars[i+2] == 'Q') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '=';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'g' || chars[i+1] == 'G') &&
                               (chars[i+2] == 't' || chars[i+2] == 'T') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '>';
                        chars[i+2] = ' ';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'g' || chars[i+1] == 'G') &&
                               (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '>';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'l' || chars[i+1] == 'L') &&
                               (chars[i+2] == 't' || chars[i+2] == 'T') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '<';
                        chars[i+2] = ' ';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'l' || chars[i+1] == 'L') &&
                               (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '<';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                    } else if ((chars[i+1] == 'o' || chars[i+1] == 'O') &&
                               (chars[i+2] == 'r' || chars[i+2] == 'R') &&
                                chars[i+3] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '|';
                        chars[i+2] = '|';
                        chars[i+3] = ' ';
                    }
                }
                if (i < chars.length - 4) {
                    if ((chars[i+1] == 'a' || chars[i+1] == 'A') &&
                        (chars[i+2] == 'n' || chars[i+2] == 'N') &&
                        (chars[i+3] == 'd' || chars[i+3] == 'D') &&
                         chars[i+4] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '&';
                        chars[i+2] = '&';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                    } else if ((chars[i+1] == 'e' || chars[i+1] == 'E') &&
                        (chars[i+2] == 'q' || chars[i+2] == 'Q') &&
                        (chars[i+3] == 'v' || chars[i+3] == 'V') &&
                         chars[i+4] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '=';
                        chars[i+2] = '=';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                    } else if ((chars[i+1] == 'n' || chars[i+1] == 'N') &&
                               (chars[i+2] == 'o' || chars[i+2] == 'O') &&
                               (chars[i+3] == 't' || chars[i+3] == 'T') &&
                                chars[i+4] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '^';
                        chars[i+2] = ' ';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                    }
                }
                if (i < chars.length - 5) {
                    if ((chars[i+1] == 'n' || chars[i+1] == 'N') &&
                        (chars[i+2] == 'e' || chars[i+2] == 'E') &&
                        (chars[i+3] == 'q' || chars[i+3] == 'Q') &&
                        (chars[i+4] == 'v' || chars[i+3] == 'V') &&
                         chars[i+5] == '.') {
                        chars[i] = ' ';
                        chars[i+1] = '<';
                        chars[i+2] = '>';
                        chars[i+3] = ' ';
                        chars[i+4] = ' ';
                        chars[i+5] = ' ';
                    }
                }
            }
            i++;
        }
        return chars;
    }

//    private static Map<String, MemoryBuffer> getRemappedBuffers() {
//        Map<String, MemoryBuffer> result = Collections.<String, MemoryBuffer>emptyMap();
//        APTBufferProvider provider = Lookup.getDefault().lookup(APTBufferProvider.class);
//        if (provider != null) {
//            Collection<APTFileBuffer> buffers = provider.getUnsavedBuffers();
//            if (buffers != null && !buffers.isEmpty()) {
//                result = new HashMap<String, MemoryBuffer>();
//                for (APTFileBuffer buf : buffers) {
//                    String pathAsUrl = CndFileSystemProvider.toUrl(buf.getFileSystem(), buf.getAbsolutePath()).toString();
//                    ClankMemoryBufferImpl mb;
//                    try {
//                        mb = ClankMemoryBufferImpl.create(pathAsUrl, buf.getCharBuffer());
//                        result.put(pathAsUrl, mb);
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex); //TODO: error processing!!!!
//                    }
//                }
//            }
//        }
//        return result;
//    }

    public static ClankDriverImpl.ClankPreprocessorOutputImplementation extractPreprocessorOutputImplementation(ClankDriver.ClankFileInfo file) {
        // it is ClankFileInfoWrapper
        if (file instanceof ClankDriverImpl.ClankPreprocessorOutputImplementation) {
            return (ClankPreprocessorOutputImplementation) file;
        }
        return null;
    }    
    
    public static ClankDriverImpl.ClankPreprocessorOutputImplementation extractPreprocessorOutputImplementation(PreprocHandler ppHandler) {
        ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl)ppHandler.getIncludeHandler();
        ClankDriverImpl.ClankPreprocessorOutputImplementation cached = includeHandler.getPreprocessorOutputImplementation();
        return cached;
    }

    public static int extractFileIndex(PreprocHandler ppHandler) {
        ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl)ppHandler.getIncludeHandler();
        return includeHandler.getInclStackIndex();
    }

    public static final class ArrayBasedAPTTokenStream extends AbstractList<APTToken> implements APTTokenStream, TokenStream {

        private int index;
        private final int lastIndex;
        private final APTToken[] tokens;

        public ArrayBasedAPTTokenStream(APTToken[] tokens) {
            this.tokens = tokens;
            this.lastIndex = tokens.length;
            this.index = 0;
        }

        @Override
        public APTToken nextToken() {
            if (index < lastIndex) {
                return tokens[index++];
            } else {
                return APTUtils.EOF_TOKEN;
            }
        }

        @Override
        public APTToken get(int index) {
            return tokens[index];
        }
        
        public List<APTToken> toList() {
            return this;
        }

        @Override
        public int size() {
            if (tokens.length == 0) {
                return 0;
            } else if (tokens[tokens.length-1] == APTUtils.EOF_TOKEN) {
                return tokens.length-1;
            } else {
                return tokens.length ;
            }
        }

        @Override
        public String toString() {
            return APTUtils.debugString(new ArrayBasedAPTTokenStream(tokens)).toString();
        }
    }    
}
