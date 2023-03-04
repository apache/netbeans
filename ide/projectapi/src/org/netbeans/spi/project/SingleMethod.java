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

package org.netbeans.spi.project;

import org.openide.filesystems.FileObject;

/**
 * Structure representing an identification of a single method/function
 * in a file.
 *
 * @since 1.19
 */
public final class SingleMethod {

    private FileObject file;
    private String methodName;

    /**
     * Creates a new instance holding the specified identification
     * of a method/function in a file.
     *
     * @param file file to be kept in the object
     * @param methodName name of a method inside the file
     * @exception  java.lang.IllegalArgumentException
     *             if the file or method name is {@code null}
     * @since 1.19
     */
    public SingleMethod(FileObject file, String methodName) {
        super();
        if (file == null) {
            throw new IllegalArgumentException("file is <null>");
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName is <null>");
        }
        this.file = file;
        this.methodName = methodName;
    }

    /**
     * Returns the file identification.
     *
     * @return file held by this object
     * @since 1.19
     */
    public FileObject getFile() {
        return file;
    }

    /**
     * Returns name of a method/function within the file.
     *
     * @return method/function name held by this object
     * @since 1.19
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Standard command for running single method/function
     *
     * @since 1.19
     */
    public static final String COMMAND_RUN_SINGLE_METHOD = "run.single.method";

    /**
     * Standard command for running single method/function in debugger
     *
     * @since 1.19
     */
    public static final String COMMAND_DEBUG_SINGLE_METHOD = "debug.single.method";

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != SingleMethod.class)) {
            return false;
        }
        SingleMethod other = (SingleMethod) obj;
        return other.file.equals(file) && other.methodName.equals(methodName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.file.hashCode();
        hash = 29 * hash + this.methodName.hashCode();
        return hash;
    }
}
