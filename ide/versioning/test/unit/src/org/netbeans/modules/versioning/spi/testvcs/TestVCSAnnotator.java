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
package org.netbeans.modules.versioning.spi.testvcs;

import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.awt.Image;
import java.awt.event.ActionEvent;

/**
 * Annotator for TestVCS.
 * 
 * @author Maros Sandor
 */
public class TestVCSAnnotator extends VCSAnnotator {
    
    public TestVCSAnnotator() {
    }

    public String annotateName(String name, VCSContext context) {
        if (name.equals("annotate-me")) {
            return "annotated";
        }
        return name;
    }

    public Image annotateIcon(Image icon, VCSContext context) {
        return icon;
    }

    public Action[] getActions(VCSContext context, ActionDestination destination) {
        return new Action[] {
            new DummyAction()
        };
    }
    
    private static class DummyAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            // do nothing
        }
    }
}
