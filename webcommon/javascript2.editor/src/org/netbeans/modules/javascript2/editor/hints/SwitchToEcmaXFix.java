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
package org.netbeans.modules.javascript2.editor.hints;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.javascript2.editor.JsPreferences;
import org.netbeans.modules.javascript2.editor.JsVersion;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

final class SwitchToEcmaXFix implements HintFix {

    private final FileObject fo;
    private final JsVersion ecmascriptEdition;

    public SwitchToEcmaXFix(Snapshot snapshot, JsVersion ecmascriptEdition) {
        this.fo = snapshot.getSource().getFileObject();
        this.ecmascriptEdition = ecmascriptEdition;
    }

    @NbBundle.Messages(value = {
        "# {0} - ECMAScript Version targetted",
        "MSG_SwitchToEcmaX=Switch project to {0}"
    })
    @Override
    public String getDescription() {
        return Bundle.MSG_SwitchToEcmaX(ecmascriptEdition.name());
    }

    @Override
    public void implement() throws Exception {
        if (fo == null) {
            return;
        }
        Project p = FileOwnerQuery.getOwner(fo);
        if (p != null) {
            JsPreferences.putECMAScriptVersion(p, ecmascriptEdition);
        }
        EcmaLevelRule.refresh(fo);
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

}
