/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.core.jaxws.actions;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;

import org.netbeans.modules.websvc.api.support.java.SourceUtils;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;

final class DispatchCompilerTask implements CancellableTask<WorkingCopy> {

    public void run(WorkingCopy workingCopy) throws Exception {
        boolean changed = false;
        workingCopy.toPhase(Phase.RESOLVED);
        TreeMaker make = workingCopy.getTreeMaker();
        CompilationUnitTree cut = workingCopy.getCompilationUnit();
        CompilationUnitTree copy = cut;
        if (!JaxWsCodeGenerator.foundImport("javax.xml.namespace.QName", copy)) {
            copy = make.addCompUnitImport(copy,
                    make.Import(make.Identifier("javax.xml.namespace.QName"), false));
            changed = true;
        }
        if (!JaxWsCodeGenerator.foundImport("javax.xml.transform.Source", copy)) {
            copy = make.addCompUnitImport(copy,
                    make.Import(make.Identifier("javax.xml.transform.Source"), false));
            changed = true;
        }
        if (!JaxWsCodeGenerator.foundImport("javax.xml.ws.Dispatch", copy)) {
            copy = make.addCompUnitImport(copy,
                    make.Import(make.Identifier("javax.xml.ws.Dispatch"), false));
            changed = true;
        }
        if (!JaxWsCodeGenerator.foundImport("javax.xml.transform.stream.StreamSource", copy)) {
            copy = make.addCompUnitImport(copy,
                    make.Import(make.Identifier("javax.xml.transform.stream.StreamSource"), false));
            changed = true;
        }
        if (!JaxWsCodeGenerator.foundImport("javax.xml.ws.Service", copy)) {
            copy = make.addCompUnitImport(copy,
                    make.Import(make.Identifier("javax.xml.ws.Service"), false));
            changed = true;
        }
        if (!JaxWsCodeGenerator.foundImport("java.io.StringReader", copy)) {
            copy = make.addCompUnitImport(copy,
                    make.Import(make.Identifier("java.io.StringReader"), false));
            changed = true;
        }
        if (changed) {
            workingCopy.rewrite(cut, copy);
        }
    }

    public void cancel() {
    }
}