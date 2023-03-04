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

import org.netbeans.modules.refactoring.java.spi.ui.JavaActionsImplementationProvider;
import org.openide.util.Lookup;

/**
 * @author Jan Becicka
 */
public final class JavaActionsImplementationFactory {
    
    private JavaActionsImplementationFactory(){}
    
    private static final Lookup.Result<JavaActionsImplementationProvider> implementations =
        Lookup.getDefault().lookup(new Lookup.Template(JavaActionsImplementationProvider.class));

    public static boolean canChangeParameters(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doChangeParameters(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                rafi.doChangeParameters(lookup);
                return;
            }
        }
    }

    public static boolean canIntroduceParameter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceParameter(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doIntroduceParameter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceParameter(lookup)) {
                rafi.doIntroduceParameter(lookup);
                return;
            }
        }
    }
    
    public static boolean canEncapsulateFields(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doEncapsulateFields(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                rafi.doEncapsulateFields(lookup);
                return;
            }
        }
    }
    public static boolean canExtractInterface(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractInterface(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doExtractInterface(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractInterface(lookup)) {
                rafi.doExtractInterface(lookup);
                return;
            }
        }
    }
    
    public static void doExtractSuperclass(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractSuperclass(lookup)) {
                rafi.doExtractSuperclass(lookup);
                return;
            }
        }
    }
    
    public static boolean canExtractSuperclass(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canExtractSuperclass(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doInnerToOuter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInnerToOuter(lookup)) {
                rafi.doInnerToOuter(lookup);
                return;
            }
        }
    }
    
    public static boolean canInnerToOuter(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInnerToOuter(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doPullUp(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPullUp(lookup)) {
                rafi.doPullUp(lookup);
                return;
            }
        }
    }
    
    public static boolean canPullUp(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPullUp(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doPushDown(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPushDown(lookup)) {
                rafi.doPushDown(lookup);
                return;
            }
        }
    }
    
    public static boolean canPushDown(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPushDown(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doUseSuperType(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canUseSuperType(lookup)) {
                rafi.doUseSuperType(lookup);
                return;
            }
        }
    }
    
    public static boolean canUseSuperType(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canUseSuperType(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean canInline(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInline(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doInline(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canInline(lookup)) {
                rafi.doInline(lookup);
                return;
            }
        }
    }
    
    public static boolean canIntroduceLocalExtension(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceLocalExtension(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doIntroduceLocalExtension(Lookup lookup) {
        for (JavaActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canIntroduceLocalExtension(lookup)) {
                rafi.doIntroduceLocalExtension(lookup);
                return;
            }
        }
    }
}
