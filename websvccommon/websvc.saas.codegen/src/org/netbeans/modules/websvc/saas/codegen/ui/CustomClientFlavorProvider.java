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
package org.netbeans.modules.websvc.saas.codegen.ui;

import org.netbeans.modules.websvc.saas.codegen.ui.CustomClientEditorDrop;
import java.awt.datatransfer.Transferable;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Ayub Khan
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider.class)
public class CustomClientFlavorProvider implements ConsumerFlavorProvider {

    public CustomClientFlavorProvider() {
    }

    public Transferable addDataFlavors(Transferable transferable) {
        try {
            if (transferable.isDataFlavorSupported(ConsumerFlavorProvider.CUSTOM_METHOD_FLAVOR)) {
                Object data = transferable.getTransferData(ConsumerFlavorProvider.CUSTOM_METHOD_FLAVOR);
                if (data instanceof CustomSaasMethod) {
                    CustomSaasMethod method = (CustomSaasMethod) data;
                    ExTransferable t = ExTransferable.create(transferable);
                    CustomClientEditorDrop editorDrop = new CustomClientEditorDrop(method);
                    ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(editorDrop);
                    t.put(s);
                    return t;
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return transferable;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {

        private CustomClientEditorDrop drop;

        ActiveEditorDropTransferable(CustomClientEditorDrop drop) {
            super(CustomClientEditorDrop.FLAVOR);

            this.drop = drop;
        }

        public Object getData() {
            return drop;
        }
    }
}
