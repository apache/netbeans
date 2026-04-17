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

package org.netbeans.spi.project;

import java.util.Objects;
import org.openide.filesystems.FileObject;

/**
 * Structure representing an identification of a single method/function
 * in a file.
 *
 * @since 1.19
 */
public final class SingleMethod {

    private final FileObject file;
    private final String methodName;
    private final NestedClass nestedClass;

    /**
     * Creates a new instance holding the specified identification
     * of a method/function in a file.
     *
     * @param nestedClass nested class containing the method
     * @param file file to be kept in the object
     * @param methodName name of a method inside the file
     * 
     * @since 1.99
     */
    private SingleMethod(NestedClass nestedClass, FileObject file, String methodName) {
        this.methodName = methodName;
        this.file = file;
        this.nestedClass = nestedClass;
    }
    
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
        this(null, nonNull(file, "file"), nonNull(methodName, "methodName"));
    }
    
    /**
     * Creates a new instance holding the specified identification
     * of a method/function in nested class in a file.
     *
     * @param methodName name of a method inside the file     
     * @param nestedClass nested class containing the method
     * 
     * @exception  java.lang.IllegalArgumentException
     *             if the nested class name is {@code null}
     * @since 1.99
     */
    public SingleMethod(String methodName, NestedClass nestedClass) {
        this(nonNull(nestedClass, "nestedClass"), nonNull(nestedClass.getFile(), "file"), nonNull(methodName, "methodName"));
    }
    
    private static <T> T nonNull(T value, String paramName) {
        if (value == null) {
            throw new IllegalArgumentException(paramName + " is <null>");
        }
        return value;
    }
    
    /**
     * Returns the nested class containing the method.
     *
     * @return nested class containing the method
     * @since 1.99
     */
    public NestedClass getNestedClass() {
        return nestedClass;
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
        return other.file.equals(file) && other.methodName.equals(methodName) && Objects.equals(other.nestedClass, nestedClass);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.file.hashCode();
        hash = 29 * hash + this.methodName.hashCode();
        hash = 29 * hash + Objects.hashCode(this.nestedClass);
        return hash;
    }
}
