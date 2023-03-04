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
package org.netbeans.performance.benchmarks.bde;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/** Describes one test */
public final class TestDefinition {

    private String className;
    private List methodPatterns;
    private List arguments;

   /** Creates new Interval */
    public TestDefinition(String className, List methodPatterns, List arguments) {
        this.className = className;
        if (methodPatterns == null) {
            this.methodPatterns = new ArrayList(1);
        } else {
            this.methodPatterns = methodPatterns;
        }
        if (arguments == null) {
            this.arguments = new ArrayList(1);
        } else {
            this.arguments = arguments;
        }
    }
    
    /** @return className */
    public String getClassName() {
        return className;
    }
    
    /** @return iteration of method patterns (Strings) */
    public Iterator getMethodPatterns() {
        return Collections.unmodifiableList(methodPatterns).iterator();
    }
    
    /** @return iteration of ArgumentSeries */
    public Iterator getArgumentSeries() {
        return Collections.unmodifiableList(arguments).iterator();
    }
}
