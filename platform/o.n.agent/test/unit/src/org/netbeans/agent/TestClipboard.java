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
package org.netbeans.agent;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.JTextField;
import org.netbeans.agent.hooks.api.TrackingHooks;
import org.netbeans.agent.hooks.api.TrackingHooks.Hooks;

/**
 *
 * @author lahvac
 */
public class TestClipboard {
    public static void main(String... args) throws IOException {
        Clipboard testClipboard = new Clipboard("test") {
            @Override
            public void setContents(Transferable contents, ClipboardOwner owner) {
                System.err.println("setContents");
            }
            @Override
            public Transferable getContents(Object requestor) {
                System.err.println("getContents");
                return null;
            }
        };
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected Clipboard getClipboard() {
                return testClipboard;
            }
        }, 0, Hooks.CLIPBOARD);
        JTextField textField = new JTextField("test");
        textField.getCaret().setDot(0);
        textField.getCaret().moveDot(1);
        System.err.println("going to copy:");
        textField.copy();
        System.err.println("going to cut:");
        textField.cut();
        System.err.println("going to paste:");
        textField.paste();
    }
}
