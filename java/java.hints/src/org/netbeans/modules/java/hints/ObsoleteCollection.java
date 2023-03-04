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

package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;

import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.ObsoleteCollection", description = "#DESC_org.netbeans.modules.java.hints.ObsoleteCollection", category="code_maturity", suppressWarnings="UseOfObsoleteCollectionType", options=Options.QUERY)
public class ObsoleteCollection {

    @TriggerPattern (value="java.util.Vector")
    public static ErrorDescription vector (HintContext ctx) {
        return hint(ctx, "java.util.ArrayList");
    }

    @TriggerPattern (value="java.util.Hashtable")
    public static ErrorDescription hashTable (HintContext ctx) {
        return hint(ctx, "java.util.HashMap");
    }

    private static ErrorDescription hint(HintContext ctx, String toCheck) {
        if (ctx.getInfo().getElements().getTypeElement(toCheck) == null) {
            //If the non-obsolete collection is not available (e.g. on a J2ME platform), do not show the warning:
            return null;
        }
        
        TreePath treePath = ctx.getPath ();
        return ErrorDescriptionFactory.forName (
            ctx,
            treePath,
            NbBundle.getMessage (ObsoleteCollection.class, "MSG_ObsoleteCollection")
        );
    }
}
