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
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;

/** Used to call "Add | Locale..." popup menu item on properties node.
 * @see Action
 * @see ActionNoBlock
 * @see org.netbeans.jellytools.nodes.PropertiesNode
 * @author <a href="mailto:vojtech.sigler@sun.com">Vojtech Sigler</a> */
public class AddLocaleAction extends ActionNoBlock {

    //This bundle is most probably incorrect, but I am unable to find the correct one
    private static final String addPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "New");

    private static final String localePopup = Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle",
            "LAB_NewLocaleAction");

    /** creates new AddLocaleAction instance */
    public AddLocaleAction() {
        super(null, addPopup + "|" + localePopup);
    }
}
