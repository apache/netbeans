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
import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;
import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author lahvac
 */
public abstract class APIAccessor {

    public static APIAccessor IMPL;

    static {
        try {
            Class.forName(Context.class.getName(), true, Context.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public abstract TreePath getSingleVariable(Context ctx, Variable var);
    public abstract HintContext getHintContext(Context ctx);

    public abstract Map<String, TreePath> getVariables(Context ctx);
    public abstract Map<String, Collection<? extends TreePath>> getMultiVariables(Context ctx);
    public abstract Map<String, String> getVariableNames(Context ctx);

    public abstract Variable enterAuxiliaryVariable(Context ctx, TreePath source);

}
