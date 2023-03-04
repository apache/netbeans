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

package org.netbeans.test.mercurial.operators.actions;

import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 *
 * @author peter
 */
public class RevertAction extends ActionNoBlock{
    
    /** "Mercurial" menu item. */
    public static final String HG_ITEM = "Mercurial";
            
    /** "Revert" menu item. */
    public static final String REVERT_ITEM = "Revert";
    
    /** Creates a new instance of RevertAction */
    public RevertAction() {
        super(HG_ITEM + "|" + REVERT_ITEM, HG_ITEM + "|" + REVERT_ITEM);
    }
    
}
