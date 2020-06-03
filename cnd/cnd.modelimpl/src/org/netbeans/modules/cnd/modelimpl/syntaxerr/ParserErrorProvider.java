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
