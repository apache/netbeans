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
package org.netbeans.modules.html.custom.hints;

import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
@NbBundle.Messages(value = {
    "# {0} - element name",
    "removeElementFromProjectConfiguration=Remove element \"{0}\" from the project's custom elements"
})
public final class RemoveElementFix implements HintFix {
    private final String elementName;
    private final String elementContextName;
    private final Snapshot snapshot;

    public RemoveElementFix(String elementName, String elementContextName, Snapshot snapshot) {
        this.elementName = elementName;
        this.elementContextName = elementContextName;
        this.snapshot = snapshot;
    }

    @Override
    public String getDescription() {
        return Bundle.removeElementFromProjectConfiguration(elementName);
    }

    @Override
    public void implement() throws Exception {
        Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
        Tag tag = conf.getTag(elementName);
        if(tag != null) {
            conf.remove(tag);
            conf.store();
            LexerUtils.rebuildTokenHierarchy(snapshot.getSource().getDocument(true));
        }
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
