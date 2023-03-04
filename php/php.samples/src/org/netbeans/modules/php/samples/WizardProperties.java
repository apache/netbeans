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

package org.netbeans.modules.php.samples;

import org.openide.WizardDescriptor;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
interface WizardProperties {
    String NAME = "name"; //NOI18N
    String PROJ_DIR = "projdir"; //NOI18N
    String WIZARD_ERROR_MSG = WizardDescriptor.PROP_ERROR_MESSAGE; //NOI18N
    String SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N
    String CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; //NOI18N
    String DB_NAME = "dbName"; //NOI18N
}
