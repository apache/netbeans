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

import com.sun.tools.javac.code.ClassFinder;
import com.sun.tools.javac.util.Context;
import javax.tools.JavaFileObject;

/**
 *
 * @author lahvac
 */
public class NBClassFinder extends ClassFinder {

    public static void preRegister(Context context) {
        context.put(classFinderKey, new Context.Factory<ClassFinder>() {
            public ClassFinder make(Context c) {
                return new NBClassFinder(c);
            }
        });
    }

    public NBClassFinder(Context context) {
        super(context);
    }

    @Override
    protected JavaFileObject preferredFileObject(JavaFileObject a, JavaFileObject b) {
        if (b.getName().toLowerCase().endsWith(".sig")) {
            //do not prefer sources over sig files (unless sources are newer):
            boolean prevPreferSource = preferSource;
            try {
                preferSource = false;
                return super.preferredFileObject(a, b);
            } finally {
                preferSource = prevPreferSource;
            }
        }
        return super.preferredFileObject(a, b);
    }

}
