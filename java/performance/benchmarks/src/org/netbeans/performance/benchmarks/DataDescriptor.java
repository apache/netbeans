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
package org.netbeans.performance.benchmarks;

import java.io.Serializable;

/**
 * Describes data
 */
public class DataDescriptor implements Serializable {
    private String klassName;
    private String testName;
    private Object arg;

    /** Sets all data */
    final void set(String klassName, String testName, Object arg) {
        this.klassName = klassName;
        this.testName = testName;
        this.arg = arg;
    }

    /** @getter for klassName for that this DD was created */
    protected final String getClassName() {
        return klassName;
    }
    
    /** @getter for testName for that this DD was created */
    protected final String getTestName() {
        return testName;
    }
    
    /** @getter for arg for that this DD was created */
    protected final Object getArgument() {
        return arg;
    }
    
    /** @return hashCode */
    public int hashCode() {
        return klassName.hashCode() ^ testName.hashCode() ^ arg.hashCode();
    }
    
    /** @return boolean iff obj equals this */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof DataDescriptor) {
            DataDescriptor dd = (DataDescriptor) obj;
            return klassName.equals(dd.klassName) && testName.equals(dd.testName) && arg.equals(dd.arg);
        }
        
        return false;
    }
}
