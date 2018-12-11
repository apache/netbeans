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
package org.netbeans.modules.java.source;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class TreeShims {

    public static List<? extends ExpressionTree> getExpressions(CaseTree node) {
        try {
            Method getExpressions = CaseTree.class.getDeclaredMethod("getExpressions");
            return (List<? extends ExpressionTree>) getExpressions.invoke(node);
        } catch (NoSuchMethodException ex) {
            return Collections.singletonList(node.getExpression());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    public static Tree getBody(CaseTree node) {
        try {
            Method getBody = CaseTree.class.getDeclaredMethod("getBody");
            return (Tree) getBody.invoke(node);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw TreeShims.<RuntimeException>throwAny(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException throwAny(Throwable t) throws T {
        throw (T) t;
    }
}
