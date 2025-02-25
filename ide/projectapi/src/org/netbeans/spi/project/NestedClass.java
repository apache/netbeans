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
 * Structure representing an identification of a nested class in a file.
 * 
 * <p>
 * <code>NestedClass</code> can be used to represent nested classes within parent class
 * Example:
 * If we have following structure: ParentClass (parent-of) ChildClass1 (parent-of) ChildClass2,
 * for ChildClass1 className field would contain "ChildClass1" and topLevelClassName would contain "ParentClass",
 * for ChildClass2 className field would contain "ChildClass1.ChildClass2" and topLevelClassName would contain "ParentClass"
 * </p>
 * 
 * @author Dusan Petrovic
 * 
 * @since 1.99
 */
public final class NestedClass {
 
    private final FileObject file;
    private final String className;
    private final String topLevelClassName;

    /**
     * Creates a new instance holding the specified identification
     * of a nested class.
     *
     * @param className name of a class inside the file
     * @param topLevelClassName top level name of a class inside the file
     * @param file file to be kept in the object
     * @exception  java.lang.IllegalArgumentException
     *             if the file or class name is {@code null}
     * @since 1.99
     */
    public NestedClass(String className, String topLevelClassName, FileObject file) {
        super();
        if (className == null) {
            throw new IllegalArgumentException("className is <null>");
        }
        if (topLevelClassName == null) {
            throw new IllegalArgumentException("topLevelClassName is <null>");
        }
        if (file == null) {
            throw new IllegalArgumentException("file is <null>");
        }
        this.className = className;
        this.topLevelClassName = topLevelClassName;
        this.file = file;
    }
    
    /**
     * Returns the file identification.
     *
     * @return file held by this object
     * @since 1.99
     */
    public FileObject getFile() {
        return file;
    }
    
    /**
     * Returns name of a nested class within a file.
     *
     * @return class name held by this object
     * @since 1.99
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Returns name of a top level class within a file.
     *
     * @return top level class name held by this object
     * @since 1.99
     */
    public String getTopLevelClassName() {
        return topLevelClassName;
    }
    
    /**
     * Returns fully qualified name.
     *
     * @param packageName name of the package where the class is
     * 
     * @return fully qualified name held by this object
     * @since 1.99
     */
    public String getFQN(String packageName) {
        return String.join(".", packageName, topLevelClassName, className);
    }
    
    /**
     * Returns fully qualified name.
     *
     * @param packageName name of the package where the class is
     * @param nestedClassSeparator separator for the nested classes
     * 
     * @return fully qualified name held by this object
     * @since 1.99
     */
    public String getFQN(String packageName, String nestedClassSeparator) {
        return String.join(".", packageName, String.join(nestedClassSeparator, topLevelClassName, className.replace(".", nestedClassSeparator)));
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.className);
        hash = 41 * hash + Objects.hashCode(this.topLevelClassName);
        hash = 41 * hash + Objects.hashCode(this.file);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != NestedClass.class)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final NestedClass other = (NestedClass) obj;
        return other.file.equals(file) && other.className.equals(className) && other.topLevelClassName.equals(topLevelClassName);
    }
}
