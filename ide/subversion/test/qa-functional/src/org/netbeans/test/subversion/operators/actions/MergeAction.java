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
package org.netbeans.test.subversion.operators.actions;

/**
 *
 * @author peter
 */
public class MergeAction extends SvnAction {

    /**
     * "Copy" menu item
     */
    public static final String COPY_MENU_ITEM = "Copy";
    /**
     * "Merge" menu item.
     */
    public static final String MERGE_MENU_ITEM = "Merge to...";
    /**
     * "Merge" popup item.
     */
    public static final String MERGE_POPUP_ITEM = "Merge Changes...";

    /**
     * Creates a new instance of MergeAction
     */
    public MergeAction() {
        super(TEAM_ITEM + "|" + COPY_MENU_ITEM + "|" + MERGE_MENU_ITEM, SVN_ITEM + "|" + COPY_MENU_ITEM + "|" + MERGE_POPUP_ITEM);
    }
}
