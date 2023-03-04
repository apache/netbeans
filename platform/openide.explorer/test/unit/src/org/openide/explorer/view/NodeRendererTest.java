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

package org.openide.explorer.view;

import java.awt.Component;
import javax.swing.JList;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.HtmlRenderer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NodeRendererTest extends NbTestCase {

    public NodeRendererTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testEmptyStringCanBeRendered() {
        NodeRenderer nr = new NodeRenderer();
        JList jl = new JList();
        jl.setCellRenderer(nr);
        Component res = nr.getListCellRendererComponent(jl, " ", 0, false, false);
        assertNotNull("Used to throw an exception", res);
        if (res instanceof HtmlRenderer.Renderer) {
            return;
        }
        fail("Shall be an HtmlRenderer.Renderer: " + res);
    }

}
