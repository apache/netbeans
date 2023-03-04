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

package org.netbeans.modules.csl.editor;

import javax.swing.text.JTextComponent;
import org.netbeans.editor.CodeFoldingSideBar;

/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 *
 *  Java Code Folding Side Bar. Component responsible for drawing folding signs and responding
 *  on user fold/unfold action.
 *
 * @todo Hmmm, we don't do anything here.... perhaps we should just nuke this class
 *   and use CodeFoldingSideBar directly??
 */
public class GsfCodeFoldingSideBar extends CodeFoldingSideBar{

    /** Creates a new instance of NbCodeFoldingSideBar */
    public GsfCodeFoldingSideBar(JTextComponent target) {
        super(target);

    }
    
    public javax.swing.JComponent createSideBar(javax.swing.text.JTextComponent target) {
        return new GsfCodeFoldingSideBar(target);
    }

}
