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
package org.netbeans.modules.cnd.model.jclank.bridge.trace;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 */
abstract class JClankDiagnosticAbstractProvider implements CndDiagnosticProvider {

    @Override
    public void dumpInfo(Lookup context, PrintWriter printOut) {
        Collection<? extends DataObject> allFiles = context.lookupAll(DataObject.class);
        Set<NativeFileItem> nfis = new LinkedHashSet<>();
        for (DataObject dob : allFiles) {
            NativeFileItemSet nfs = dob.getLookup().lookup(NativeFileItemSet.class);
            if (nfs == null) {
                printOut.printf("NO NativeFileItemSet in %s %n", dob); // NOI18N
                continue;
            }
            if (nfs.isEmpty()) {
                printOut.printf("EMPTY NativeFileItemSet in %s %n", dob); // NOI18N
                continue;
            }
            for (NativeFileItem nfi : nfs.getItems()) {
                nfis.add(nfi);
            }
        }
        doNativeFileItemDiagnostic(nfis, printOut);
    }

    protected abstract void doNativeFileItemDiagnostic(Set<NativeFileItem> nfis, PrintWriter printOut);

}
