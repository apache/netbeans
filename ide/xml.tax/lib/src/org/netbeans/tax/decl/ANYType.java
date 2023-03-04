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
package org.netbeans.tax.decl;

import org.netbeans.tax.*;

/** ANY is mixed. */
public class ANYType extends LeafType {

    //
    // init
    //

    public ANYType () {
        super ();
    }

    public ANYType (ANYType anyType) {
        super (anyType);
    }


    //
    // from TreeObject
    //

    /**
     */
    public Object clone () {
        return new ANYType (this);
    }

    //
    // itself
    //

    /**
     */
    public boolean allowElements () {
        return true;
    }

    /**
     */
    public boolean allowText () {
        return true;
    }
    
    /**
     */
    public String getName () {
        return "ANY"; // NOI18N
    }
    
    /**
     */
    public String toString () {
        return "ANY"; // NOI18N
    }
    
}
