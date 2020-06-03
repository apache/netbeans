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

package org.netbeans.modules.cnd.api.model.syntaxerr;

/**
 * Represents an error or warning
 */
public interface CsmErrorInfo {

    /**
     * Represents severity level
     */
    enum Severity { 
        ERROR,
        WARNING,
        HINT
    }

    /**
     * Gets error message 
     * @return the message
     */
    String getMessage();
    
    /** 
     * Gets severity 
     * @return this error/warning message severity
     */
    Severity getSeverity();

    /**
     * Gets error start offset 
     */
    public int getStartOffset();
    
    /**
     * Gets error end offset 
     */
    public int getEndOffset();
    
    default String getCustomType () {
        return null;
    }

    default int[] getStartOffsets() {
        return new int[]{getStartOffset()};
    }

    default int[] getEndOffsets() {
        return new int[]{getEndOffset()};
    }
}
