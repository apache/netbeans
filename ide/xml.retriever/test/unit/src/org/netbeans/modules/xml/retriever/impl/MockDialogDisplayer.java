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
package org.netbeans.modules.xml.retriever.impl;

import java.awt.Dialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Benjamin Asbach
 */
public class MockDialogDisplayer extends DialogDisplayer {

    boolean invoked;

    Object descriptionValue;

    public MockDialogDisplayer() {
    }

    @Override
    public Object notify(NotifyDescriptor descriptor) {
        this.invoked = true;

        descriptor.setValue(descriptionValue);
        return null;
    }

    @Override
    public Dialog createDialog(DialogDescriptor descriptor) {
        this.invoked = true;

        return null;
    }

    public void reset() {
        this.invoked = false;
        this.descriptionValue = DialogDescriptor.NO_OPTION;
    }
}
