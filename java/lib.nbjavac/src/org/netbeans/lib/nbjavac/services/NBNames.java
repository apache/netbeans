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
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

/**
 *
 * @author lahvac
 */
public class NBNames extends Names {

    public static void preRegister(Context context) {
        context.put(namesKey, new Context.Factory<Names>() {
            public Names make(Context c) {
                return new NBNames(c);
            }
        });
    }

    public static NBNames instance(Context context) {
        return (NBNames) Names.instance(context);
    }

    private final Context context;
    public final Name _org_netbeans_EnclosingMethod;
    public final Name _org_netbeans_TypeSignature;
    public final Name _org_netbeans_ParameterNames;
    public final Name _org_netbeans_SourceLevelAnnotations;
    public final Name _org_netbeans_SourceLevelParameterAnnotations;
    public final Name _org_netbeans_SourceLevelTypeAnnotations;

    protected NBNames(Context context) {
        super(context);
        context.put(namesKey, this);

        this.context = context;
        _org_netbeans_EnclosingMethod = fromString("org.netbeans.EnclosingMethod");
        _org_netbeans_TypeSignature = fromString("org.netbeans.TypeSignature");
        _org_netbeans_ParameterNames = fromString("org.netbeans.ParameterNames");
        _org_netbeans_SourceLevelAnnotations = fromString("org.netbeans.SourceLevelAnnotations");
        _org_netbeans_SourceLevelParameterAnnotations = fromString("org.netbeans.SourceLevelParameterAnnotations");
        _org_netbeans_SourceLevelTypeAnnotations = fromString("org.netbeans.SourceLevelTypeAnnotations");
    }

    public Context getContext() {
        return context;
    }
}
