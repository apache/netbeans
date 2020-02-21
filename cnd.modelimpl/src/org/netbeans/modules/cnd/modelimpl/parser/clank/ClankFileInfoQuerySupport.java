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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.util.Exceptions;

/**
 *
 */
public class ClankFileInfoQuerySupport {

    public static List<CsmReference> getMacroUsages(FileImpl fileImpl, Interrupter interrupter) {
        List<CsmReference> out = new ArrayList<>();
        if (APTTraceFlags.DEFERRED_MACRO_USAGES) {
            Collection<PreprocHandler> handlers = fileImpl.getPreprocHandlersForParse(interrupter);
            if (interrupter.cancelled()) {
                return out;
            }
            if (handlers.isEmpty()) {
                DiagnosticExceptoins.register(new IllegalStateException("Empty preprocessor handlers for " + fileImpl.getAbsolutePath())); //NOI18N
            } else if (handlers.size() == 1) {
                PreprocHandler handler = handlers.iterator().next();
                out.addAll(ClankTokenStreamProducer.getMacroUsages(fileImpl, handler, interrupter));
            } else {
                TreeSet<CsmReference> result = new TreeSet<>(CsmOffsetable.OFFSET_COMPARATOR);
                for (PreprocHandler handler : handlers) {
                    if (interrupter.cancelled()) {
                        break;
                    }
                    // ask for concurrent entry if absent
                    result.addAll(ClankTokenStreamProducer.getMacroUsages(fileImpl, handler, interrupter));
                }
                out = new ArrayList<>(result);
            }
        } else {
            for (CsmReference reference : fileImpl.getReferences()) {
                if (interrupter.cancelled()) {
                    return out;
                }
                CsmObject referencedObject = reference.getReferencedObject();
                if (CsmKindUtilities.isMacro(referencedObject)) {
                    out.add(reference);
                }
            }
        }
        return out;
    }

    public static CsmOffsetable getGuardOffset(FileImpl fileImpl) {
        assert APTTraceFlags.USE_CLANK;
        return fileImpl.getFileGuard();
    }

    public static boolean hasGuardBlock(FileImpl fileImpl) {
        assert APTTraceFlags.USE_CLANK;
        return fileImpl.hasFileGuard();
    }

    /** returns expanded code or NULL on error */
    public static String expand(FileImpl fileImpl, String code, PreprocHandler handler, ProjectBase base, int offset) {
        assert APTTraceFlags.USE_CLANK;
        TokenStream ts = fileImpl.getTokenStreamForMacroExpansion(offset, offset, code, true);
        if (ts == null) {
            return null;
        }
        ts = new APTCommentsFilter(ts);

        StringBuilder sb = new StringBuilder(""); // NOI18N
        try {
            APTToken t = (APTToken) ts.nextToken();
            while (t != null && !APTUtils.isEOF(t)) {
                sb.append(t.getTextID());
                t = (APTToken) ts.nextToken();
            }
        } catch (TokenStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sb.toString();
    }
}
