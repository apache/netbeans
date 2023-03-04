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

package org.netbeans.modules.editor.openide;

import javax.swing.text.EditorKit;
import org.openide.modules.ModuleInfo;
import org.openide.text.*;
import org.openide.util.Lookup;

/**
 *
 * @author mmetelka
 */
public class InheritedNotifyModifiedTest extends NotifyModifiedOnNbEditorLikeKitTest {

    /** Creates a new instance of InheritedUndoRedoTest */
    public InheritedNotifyModifiedTest(String methodName) {
        super(methodName);
    }

    protected @Override EditorKit createEditorKit() {
        return new org.netbeans.modules.editor.NbEditorKit();
    }

    protected @Override void setUp() {
        super.setUp();
        
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

}
