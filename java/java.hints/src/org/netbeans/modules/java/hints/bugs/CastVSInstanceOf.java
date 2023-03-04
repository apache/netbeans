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

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.util.TreePath;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.CastVSInstanceOf", description = "#DESC_org.netbeans.modules.java.hints.bugs.CastVSInstanceOf", category="bugs", suppressWarnings="CastConflictsWithInstanceof", options=Options.QUERY)
public class CastVSInstanceOf {

    @TriggerPatterns({
        @TriggerPattern(value="if ($var instanceof $instanceofClass) {" +
                              "$type $name = ($castClass) $var;" +
                              "$stmts$;" +
                              "}"),
        @TriggerPattern(value="if ($var instanceof $instanceofClass) {" +
                              "final $type $name = ($castClass) $var;" +
                              "$stmts$;" +
                              "}"),
        @TriggerPattern(value="if ($var instanceof $instanceofClass) {" +
                              "$name = ($castClass) $var;" +
                              "$stmts$;" +
                              "}")
    })
    public static ErrorDescription hint(HintContext ctx) {
        TypeMirror tm1 = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$instanceofClass"));
        TreePath castClass = ctx.getVariables().get("$castClass");
        TypeMirror tm2 = ctx.getInfo().getTrees().getTypeMirror(castClass);
        Types t = ctx.getInfo().getTypes();

        if (!(Utilities.isValidType(tm1) && Utilities.isValidType(tm2))) {
            return null;
        }

        tm1 = t.erasure(tm1);
        tm2 = t.erasure(tm2);

        if (t.isSubtype(tm1, tm2)) {
            return null;
        }

        if (t.isSubtype(tm2, tm1)) {
            return null;
        }

        return ErrorDescriptionFactory.forTree(ctx, castClass, NbBundle.getMessage(CastVSInstanceOf.class,"ERR_CastVSInstanceOf"));
    }

}
