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
/*
 * AssertionFileFailedError.java
 *
 * Created on February 15, 2001, 10:53 AM
 */

package org.netbeans.junit;

import junit.framework.AssertionFailedError;

/** Error thrown from assertFile file functions.
 * It describes the failure and holds the name of result of the file comapre process.
 * Generally, the error description should contain names of compared files.
 * @author  vstejskal
 * @version 1.0
 */
public class AssertionFileFailedError extends AssertionFailedError {
    protected String diffFile;
    /** Creates new AssertionFileFailedError 
     *  @param diffFile Fully-qualified name of the file containing differences (result of the file-diff).
     */
    public AssertionFileFailedError(String diffFile) {
        this(null, diffFile);
    }
    /** Creates new AssertionFileFailedError 
     *  @param message The error description menssage.
     *  @param diffFile Fully-qualified name of the file containing differences (result of the file-diff).
     */
    public AssertionFileFailedError(String message, String diffFile) {
        super(message);
        this.diffFile = diffFile;
    }
    public String getDiffFile() {
        return null != diffFile ? diffFile : "";
    }
}
