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

package org.netbeans.api.debugger.test;


import java.io.IOException;
import java.util.*;

/**
 * A test DebuggerInfo cookie.
 *
 * @author Maros Sandor
 */
public class TestDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-test-TestDICookie";

    private Map args;

    private TestDICookie(Map args) {
        this.args = args;
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param args arguments to be used
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static TestDICookie create(Map args) {
        return new TestDICookie (args);
    }

    /**
     * Returns map of arguments to be used.
     *
     * @return map of arguments to be used
     */
    public Map getArgs () {
        return args;
    }

    private Set infos = new HashSet();

    public void addInfo(Object s) {
        infos.add(s);
    }

    public boolean hasInfo(Object s) {
        return infos.contains(s);
    }
}
