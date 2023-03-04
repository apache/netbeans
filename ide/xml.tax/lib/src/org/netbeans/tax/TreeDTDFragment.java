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

package org.netbeans.tax;

import org.netbeans.tax.spec.DTD;


/**
 * Basically parameter entity treated as fragment. It's used
 * to model external DTDs and external parameter entities.
 */
public class TreeDTDFragment extends TreeDocumentFragment {
    /**
     * Creates new TreeDocumentFragment.
     * @throws InvalidArgumentException
     */
    public TreeDTDFragment() throws InvalidArgumentException {
        super();
    }

    /** Creates new TreeDocumentFragment -- copy constructor. */
    protected TreeDTDFragment (TreeDTDFragment documentFragment, boolean deep) {
        super (documentFragment, deep);
    }


    //
    // from TreeObject
    //

    /**
     */
    public Object clone (boolean deep) {
        return new TreeDTDFragment (this, deep);
    }

    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager() {
        return new ExternalDTDContentManager();
    }

    /**
     * External DTD content manager (assigned to externalDTDList).
     * All kids use as parent node wrapping TreeDocumentType.
     * All kids must be DTD.Child instances.
     */
    protected class ExternalDTDContentManager extends TreeParentNode.ChildListContentManager {

        /**
         */
        public TreeNode getOwnerNode () {
            return TreeDTDFragment.this;
        }

        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (DTD.Child.class, obj);
        }

    }

}
