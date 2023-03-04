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
package org.netbeans.modules.javafx2.editor.fxml;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Anton Chechel
 */
public final class FXMLMultiViewHelper {
    
    private FXMLMultiViewHelper() {
    }
    
    @MultiViewElement.Registration(
        displayName="org.netbeans.modules.javafx2.editor.Bundle#CTL_SourceTabCaption", // NOI18N
        iconBase="org/netbeans/modules/javafx2/editor/resources/fxmlObject.png", // NOI18N
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID="xml.text", // NOI18N
        mimeType=JavaFXEditorUtils.FXML_MIME_TYPE,
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }
    
}
