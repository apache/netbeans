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
public class ImportAction extends SvnAction {

    /**
     * "Versioning" menu item.
     */
    public static final String VERSIONING_ITEM = "Versioning";
    /**
     * "Import..." menu item.
     */
    public static final String IMPORT_MAIN_ITEM = "Import into Repository...";
    /**
     * "Import..." popup item.
     */
    public static final String IMPORT_POPUP_ITEM = "Import into Subversion Repository...";

    /**
     * Creates a new instance of ImportAction
     */
    public ImportAction() {
        super(TEAM_ITEM + "|" + SVN_ITEM + "|" + IMPORT_MAIN_ITEM, VERSIONING_ITEM + "|" + IMPORT_POPUP_ITEM);
    }
}
