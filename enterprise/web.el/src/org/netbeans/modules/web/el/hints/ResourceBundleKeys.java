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
package org.netbeans.modules.web.el.hints;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.Node;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ResourceBundles;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Hint for checking unknown resource bundle keys.
 *
 * @author Erno Mononen
 */
public final class ResourceBundleKeys extends ELRule {

    private ResourceBundles resourceBundles;

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(ELHintsProvider.Kind.DEFAULT);
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        FileObject fo = context.parserResult.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return false;
        }
        this.resourceBundles = ResourceBundles.get(fo);
        return resourceBundles.canHaveBundles();
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    protected void run(CompilationContext info, RuleContext context, List<Hint> result) {
        ELParserResult elResult = (ELParserResult)context.parserResult;
        for (ELElement each : elResult.getElements()) {
            if (!each.isValid()) {
                // broken AST, skip (perhaps could try just plain string search)
                continue;
            }
            for (Pair<AstIdentifier, Node> pair : resourceBundles.collectKeys(each.getNode(), info.context())) {
                String clearedKey = pair.second().getImage().replace("'", "").replace("\"", "");
                if (!resourceBundles.isValidKey(pair.first().getImage(), clearedKey)) {
                    Hint hint = new Hint(this,
                            NbBundle.getMessage(ResourceBundleKeys.class, "ResourceBundleKeys_Unknown", clearedKey),
                            elResult.getFileObject(),
                            each.getOriginalOffset(pair.second()),
                            Collections.<HintFix>emptyList(), 200);
                    result.add(hint);
                }
            }
        }
    }
}
