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

@OptionsPanelController.ContainerRegistration(
    id=CndUIConstants.TOOLS_OPTIONS_CND_CATEGORY_ID,
    categoryName="#CndOptionsCategory_Name", // NOI18N
//    title="#CndOptions_Title"
    iconBase="org/netbeans/modules/cnd/toolchain/ui/options/cnd_32.png", // NOI18N
    position=700
)
package org.netbeans.modules.cnd.toolchain.ui.options;

import org.netbeans.modules.cnd.utils.ui.CndUIConstants;
import org.netbeans.spi.options.OptionsPanelController;
