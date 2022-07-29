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
package org.netbeans.modules.refactoring.spi.ui;

import org.openide.util.Cancellable;

/**
 * ExpandableTreeElements are shown in the Find Usages results.
 * ExpandableTreeElement allows a TreeElement to have children. It will be
 * iterated when the TreeElement is expanded. The iterator is allowed to block,
 * it will be called from outside of AWT.
 * 
 * @author Jan Lahoda
 * @author Ralph Benjamin Ruijs &lt;ralphbenjamin@netbeans.org&gt;
 * @see TreeElement
 * @since 1.30
 */
public interface ExpandableTreeElement extends TreeElement, Iterable<TreeElement>, Cancellable {

    /**
     * Gives an estimate of the amount of children this element has.
     * The estimatedChildCount will be used to give the user a rough count
     * of found occurrences.
     * @return the estimated child count
     */
    public int estimateChildCount();
    
}
