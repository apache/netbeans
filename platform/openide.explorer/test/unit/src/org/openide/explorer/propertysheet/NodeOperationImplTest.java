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
package org.openide.explorer.propertysheet;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.nodes.PropertySupport;

public class NodeOperationImplTest extends NbTestCase {
    public NodeOperationImplTest(String name) {
        super(name);
    }

    /**
     * Verifies that NodeOperationImpl is correctly initialized with the
     * accessor for showing custom editor dialog.
     */
    public void testCustomEditorDialog() {
        final Node.Property<String> prop = new PropertySupport.ReadOnly("", String.class, "", "") {
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return "dummy";
            }
        };
        try {
            // The property has no custom editor, no dialog will open, so we can
            // do blocking call.
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    NodeOperation.getDefault().showCustomEditorDialog(prop, new Object[]{});
                }
            });
        } catch (InterruptedException ex) {
        } catch (InvocationTargetException ex) {
            ex.getTargetException().printStackTrace();
            fail("Can't show custom editor dialog");
        }
    }
}
