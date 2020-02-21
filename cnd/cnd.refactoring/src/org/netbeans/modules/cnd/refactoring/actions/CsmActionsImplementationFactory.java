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

package org.netbeans.modules.cnd.refactoring.actions;

import org.netbeans.modules.cnd.refactoring.spi.CsmActionsImplementationProvider;
import org.openide.util.Lookup;

/**
 */
public final class CsmActionsImplementationFactory {
    
    private CsmActionsImplementationFactory(){}
    
    private static final Lookup.Result<CsmActionsImplementationProvider> implementations = Lookup.getDefault().lookupResult(CsmActionsImplementationProvider.class);

    public static boolean canChangeParameters(Lookup lookup) {
        for (CsmActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doChangeParameters(Lookup lookup) {
        for (CsmActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                rafi.doChangeParameters(lookup);
                return;
            }
        }
    }

    public static boolean canEncapsulateFields(Lookup lookup) {
        for (CsmActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doEncapsulateFields(Lookup lookup) {
        for (CsmActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canEncapsulateFields(lookup)) {
                rafi.doEncapsulateFields(lookup);
                return;
            }
        }
    }
    
    public static boolean canPerformInlineRefactoring(Lookup lookup) {
        for (CsmActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canChangeParameters(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doInlineRefactoring(Lookup lookup) {
        for (CsmActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canPerformInlineRefactoring(lookup)) {
                rafi.doInlineRefactoring(lookup);
                return;
            }
        }
    }
    
}
