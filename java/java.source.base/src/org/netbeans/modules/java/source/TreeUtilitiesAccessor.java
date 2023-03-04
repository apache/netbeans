/**
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
package org.netbeans.modules.java.source;

import com.sun.source.tree.StatementTree;
import com.sun.source.util.SourcePositions;
import com.sun.tools.javac.api.JavacTaskImpl;
import org.netbeans.api.java.source.TreeUtilities;

/**
 *
 * @author lahvac
 */
public abstract class TreeUtilitiesAccessor {

    private static volatile TreeUtilitiesAccessor INSTANCE;

    public static TreeUtilitiesAccessor getInstance() {
        TreeUtilitiesAccessor result = INSTANCE;

        if (result == null) {
            synchronized (TreeUtilitiesAccessor.class) {
                if (INSTANCE == null) {
                    Class c = TreeUtilities.class;
                    try {
                        Class.forName(c.getName(), true, c.getClassLoader());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    assert INSTANCE != null;
                }

                return INSTANCE;
            }
        }

        return result;
    }

    public static void setInstance(TreeUtilitiesAccessor instance) {
        assert instance != null;
        INSTANCE = instance;
    }

    protected TreeUtilitiesAccessor() {
    }

    public abstract StatementTree parseStatement(JavacTaskImpl task, String stmt, SourcePositions[] sourcePositions);

}
