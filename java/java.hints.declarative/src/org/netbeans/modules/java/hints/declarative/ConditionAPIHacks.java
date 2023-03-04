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

package org.netbeans.modules.java.hints.declarative;

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;
import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;

/**Methods that can be used from declarative hints' conditions, but that are not
 * a good solution for the problem they solve.
 *
 * @author lahvac
 */
public class ConditionAPIHacks {

    public @CheckForNull Variable getDeclaration(@NonNull Context ctx, @NonNull Variable forVar) {
        CompilationInfo info = APIAccessor.IMPL.getHintContext(ctx).getInfo();
        TreePath path = APIAccessor.IMPL.getSingleVariable(ctx, forVar);
        Element el = info.getTrees().getElement(path);

        if (el == null) return null;

        TreePath source = info.getTrees().getPath(el);

        if (source == null) return null;

        return APIAccessor.IMPL.enterAuxiliaryVariable(ctx, source);
    }
}
