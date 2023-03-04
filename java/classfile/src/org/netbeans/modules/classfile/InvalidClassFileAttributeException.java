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

package org.netbeans.modules.classfile;

/**
 * Thrown when a classfile attribute does not follow the specified format.
 * This is an RuntimeException subclass rather than an Exception because it 
 * is very unlikely that a classfile can be read without exception (such as 
 * when the classfile is truncated or overwritten) yet have an invalid attribute.  
 * Making this an Exception would force existing client code to be updated with
 * almost no benefit in robustness to those clients.
 * 
 * @author Thomas Ball
 */
public final class InvalidClassFileAttributeException extends RuntimeException {
    
    InvalidClassFileAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    private static final long serialVersionUID = -2988920220798200016L;
}
