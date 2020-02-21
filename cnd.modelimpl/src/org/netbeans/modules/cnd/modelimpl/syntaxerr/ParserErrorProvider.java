/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.syntaxerr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ParserErrorFilter;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Error provider based on parser errors
 */
@ServiceProviders({
@ServiceProvider(service=CsmErrorProvider.class, position=10),
@ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1500)
})
public final class ParserErrorProvider extends CsmErrorProvider {

    private static final boolean ENABLE = CndUtils.getBoolean("cnd.parser.error.provider", true);

    @Override
    protected boolean validate(Request request) {
        return ENABLE && super.validate(request) && !disableAsLibraryHeaderFile(request.getFile());
    }

    @Override
    public boolean isSupportedEvent(EditorEvent kind) {
        return kind == EditorEvent.DocumentBased || kind == EditorEvent.FileBased;
    }

    @Override
    protected  void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        Collection<CsmErrorInfo> errorInfos = new ArrayList<>();
        Collection<CsmParserProvider.ParserError> errors = new ArrayList<>();
        Thread currentThread = Thread.currentThread();
        FileImpl file = (FileImpl) request.getFile();
        currentThread.setName("Provider "+getName()+" prosess "+file.getAbsolutePath()); // NOI18N
        if (request.isCancelled()) {
            return;
        }
        ReadOnlyTokenBuffer buffer = file.getErrors(errors);
        if (buffer != null) {
            if (request.isCancelled()) {
                return;
            }
            ParserErrorFilter.getDefault().filter(errors, errorInfos, buffer, request.getFile());
            for (Iterator<CsmErrorInfo> iter = errorInfos.iterator(); iter.hasNext() && ! request.isCancelled(); ) {
                if (request.isCancelled()) {
                    return;
                }
                response.addError(iter.next());
            }
        }
    }

    @Override
    public String getName() {
        return "syntax-error"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ParserErrorProvider.class, "Show-syntax-error"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ParserErrorProvider.class, "Show-syntax-error-AD"); //NOI18N
    }
}
