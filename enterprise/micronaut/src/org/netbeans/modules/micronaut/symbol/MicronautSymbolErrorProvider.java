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
package org.netbeans.modules.micronaut.symbol;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = ErrorProvider.class)
public final class MicronautSymbolErrorProvider implements ErrorProvider {

    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        if (context.errorKind() != ErrorProvider.Kind.ERRORS || context.isCancelled()) {
            return Collections.emptyList();
        }
        FileObject fo = context.file();
        Project project = fo != null ? FileOwnerQuery.getOwner(fo) : null;
        if (project == null) {
            return Collections.emptyList();
        }
        try {
            return MicronautSymbolSearcher.getSymbolsWithPathDuplicates(project, fo).stream()
                    .filter(descriptor -> descriptor.getFileObject() == fo)
                    .map(descriptor -> desc2diag(descriptor))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }

    @NbBundle.Messages({
        "# {0} - symbol name",
        "ERR_Duplicated_URI_path=Duplicated endpoint URI path: {0}"
    })
    private Diagnostic desc2diag(MicronautSymbolSearcher.SymbolDescriptor descriptor) {
        OffsetRange offsetRange = descriptor.getOffsetRange(null);
        return Diagnostic.Builder.create(() -> offsetRange.getStart(), () -> offsetRange.getEnd(), Bundle.ERR_Duplicated_URI_path(descriptor.getName()))
                .setCode("WARN_Duplicated_MN_Data_Endpoint_Path " + offsetRange.getStart() + " - " + offsetRange.getEnd())
                .setSeverity(Diagnostic.Severity.Warning)
                .build();
    }
}
