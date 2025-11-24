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

package org.netbeans.modules.xml.jaxb;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * A place holder class
 * @author lgao
 */
@MIMEResolver.ExtensionRegistration(
    displayName="#JAXBResolver",
    extension="xjb",
    mimeType="text/x-jaxb-binding+xml",
    position=490
)
public class JAXBWizard {

    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.xml.jaxb.Bundle#CTL_SourceTabCaption",
        iconBase="org/netbeans/modules/xml/jaxb/resources/XML_file.png",
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="xml.text",
        mimeType="text/x-jaxb-binding+xml",
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditor(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    /** Creates a new instance of JAXBWizard */
    public JAXBWizard() {
    }
}
