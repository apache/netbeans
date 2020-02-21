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

package org.netbeans.modules.cnd.classview.model;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.openide.nodes.Children;

/**
 *
 */
public abstract class ClassifierNode  extends ObjectNode {
    
    protected ClassifierNode(CsmCompoundClassifier obj, Children.Array key) {
        super(obj, checkForwardLeaf(obj, key));
    }

    private static Children checkForwardLeaf(CsmClassifier cls, Children child) {
        if (CsmClassifierResolver.getDefault().isForwardClass(cls)) {
            return Children.LEAF;
        } else {
            return child;
        }
    }
}
