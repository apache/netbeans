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
package org.netbeans.modules.refactoring.java.ui;

/**
 *
 * @author Jan Becicka
 */
public final class JavaRenameProperties {
    
    private boolean isRenameGettersSetters;
    private boolean isRenameTestClass;
    private boolean isRenameTestClassMethod;
    private boolean noChangeOK;

    public boolean isIsRenameGettersSetters() {
        return isRenameGettersSetters;
    }

    public void setIsRenameGettersSetters(boolean isRenameGettersSetters) {
        this.isRenameGettersSetters = isRenameGettersSetters;
    }

    public boolean isIsRenameTestClass() {
        return isRenameTestClass;
    }

    public void setIsRenameTestClass(boolean isRenameTestClass) {
        this.isRenameTestClass = isRenameTestClass;
    }

    public boolean isIsRenameTestClassMethod() {
	return isRenameTestClassMethod;
    }

    public void setIsRenameTestClassMethod(boolean isRenameTestClassMethod) {
	this.isRenameTestClassMethod = isRenameTestClassMethod;
    }

    public boolean isNoChangeOK() {
        return noChangeOK;
    }

    public void setNoChangeOK(boolean noChangeOK) {
        this.noChangeOK = noChangeOK;
    }
}
