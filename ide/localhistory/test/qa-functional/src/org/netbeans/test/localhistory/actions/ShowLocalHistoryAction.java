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
/*
 * ShowLocalHistoryAction.java
 *
 * Created on February 2, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.localhistory.actions;

import org.netbeans.jellytools.actions.Action;

/**
 *
 * @author peter
 */
public class ShowLocalHistoryAction extends Action {
    
    public static final String LH_ITEM = "Local History";
    public static final String SHOW_LOCAL_HISTORY_ITEM = "Show Local History";
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public ShowLocalHistoryAction() {
        super(LH_ITEM + "|" + LH_ITEM + "|" + SHOW_LOCAL_HISTORY_ITEM, LH_ITEM + "|" + SHOW_LOCAL_HISTORY_ITEM);
    }
    
}
