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
