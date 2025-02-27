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
package org.netbeans.modules.refactoring.java.api;

import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Convert Inner to Top-Level refactoring implementation class. This refactoring
 * is capable of converting an inner class into a top-level class.
 *
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see org.netbeans.modules.refactoring.api.AbstractRefactoring
 * @see org.netbeans.modules.refactoring.api.RefactoringSession
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public final class InnerToOuterRefactoring extends AbstractRefactoring {

    // parameters of the refactoring
    private String className;
    private String referenceName;

    private boolean innerIsRecord;

    /**
     * Creates a new instance of InnerToOuterRefactoring.
     *
     * @param sourceType An inner class that should be converted to a top-level
     * class.
     */
    public InnerToOuterRefactoring(TreePathHandle sourceType) {
        super(Lookups.singleton(sourceType));
    }

    /**
     * Returns the type the members of which should be pulled up by this
     * refactoring.
     *
     * @return Source of the members to be pulled up.
     */
    public TreePathHandle getSourceType() {
        return getRefactoringSource().lookup(TreePathHandle.class);
    }

    // --- PARAMETERS ----------------------------------------------------------
    /**
     * Returns the name for the top-level class to be created.
     *
     * @return Class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets name for the top-level class to be created.
     *
     * @param className Class name.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns name of the field that should be generated as a reference to the
     * original outer class. If null, no field will be generated.
     *
     * @return Name of the field to be generated or null if no field will be
     * generated.
     */
    public String getReferenceName() {
        return referenceName;
    }

    /**
     * Sets name of the field that should be generated as a reference to the
     * original outer class. Can be set to null which indicates that no field
     * should be generated.
     *
     * @param referenceName Name of the field or null if no field should be
     * generated.
     */
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    /**
     * Inner records need special handling because of the RecordComponents which
     * are declared before the first curly brace, which differs from class,
     * interface and enum.
     *
     * Also, the compact constructor should be considered.
     *
     * A compact constructor consists of the name of the Record, no parameters
     * (not even the parens) and a block that does NOT assign the fields.
     *
     * @return the current value for this refactoring
     */
    public boolean isInnerIsRecord() {

        return innerIsRecord;
    }

    /**
     * Inner records need special handling because of the RecordComponents which
     * are declared before the first curly brace, which differs from class,
     * interface and enum.
     *
     * Also, the compact constructor should be considered.
     *
     * A compact constructor consists of the name of the Record, no parameters
     * (not even the parens) and a block that does NOT assign the fields.
     *
     * @param innerIsRecord use when inner class needs the special handling of 
     * an inner record.
     */
    public void setInnerIsRecord(boolean innerIsRecord) {
        this.innerIsRecord = innerIsRecord;
    }

}
