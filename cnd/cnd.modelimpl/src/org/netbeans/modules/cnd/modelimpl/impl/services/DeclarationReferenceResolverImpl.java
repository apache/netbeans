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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentReferences;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver.class,  position = 100)
public class DeclarationReferenceResolverImpl extends CsmReferenceResolver {

    @Override
    public CsmReference findReference(CsmFile file, Document doc, int offset) {
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            CsmReference out = impl.getReference(offset);
            if (out != null) {
                // reference can be outdated
                // check if referenced object is still alive
                CsmObject referencedObject = out.getReferencedObject();
                if (referencedObject != null) {
                    return out;
                }
            }
        }
        return null;
    }

    @Override
    public Scope fastCheckScope(CsmReference ref) {
        return Scope.UNKNOWN;
    }

    @Override
    public boolean isKindOf(CsmReference ref, Set<CsmReferenceKind> kinds) {
        return FileComponentReferences.isKindOf(ref, kinds);
    }

    @Override
    public Collection<CsmReference> getReferences(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            return impl.getReferences();
        }
        return Collections.<CsmReference>emptyList();
    }
}
