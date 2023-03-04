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

package org.netbeans.modules.java.hints.finalize;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author Tomas Zezuls
 */
final class Util {

    private static final String FINALIZE = "finalize";  //NOI18N

    private Util() {}

    /**
     * @param tree
     * @return true if tree represents finalize() method
     */
    public static boolean isFinalize(final MethodTree tree) {
        assert tree != null;
        return FINALIZE.contentEquals(tree.getName()) && tree.getParameters().isEmpty();
    }

    /**
     * @param ctx hint context
     * @param tp MethodTree tree path
     * @return true if declared in j.l.Object
     */
    public static boolean isInObject(final HintContext ctx, final TreePath tp) {
        assert ctx != null;
        final Element methodElement = ctx.getInfo().getTrees().getElement(tp);
        if (methodElement != null) {
            final TypeElement owner = (TypeElement) methodElement.getEnclosingElement();
            return Object.class.getName().contentEquals(owner.getQualifiedName());
        }
        return false;
    }

}
