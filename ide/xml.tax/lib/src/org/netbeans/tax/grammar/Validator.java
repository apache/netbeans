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
package org.netbeans.tax.grammar;

import org.netbeans.tax.TreeNode;
import org.netbeans.tax.TreeElement;
import org.netbeans.tax.TreeAttribute;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public interface Validator {

    //      /** */
    //      public boolean canAppendNode (TreeNode appendNode);

    /** */
    public boolean canInsertNodeAt (TreeNode insertNode, int index);

    //      /** */
    //      public boolean canInsertNodeBefore (TreeNode insertNode, TreeNode beforeNode);

    //      /** */
    //      public boolean canInsertNodeAfter (TreeNode insertNode, TreeNode afterNode);
    
    
    /** */
    public boolean canRemoveNode (TreeNode removeNode);
    
    
    /** */
    public boolean canReplaceNode (TreeNode oldNode, TreeNode newNode);
    
    
    /** */
    public boolean canAddAttribute (TreeElement element, TreeAttribute attribute);
    
    /** */
    public boolean canRemoveAttribute (TreeAttribute attribute);
    
    /** */
    public boolean canChangeAttributeValue (TreeAttribute attribute, String newValue);
    
}
