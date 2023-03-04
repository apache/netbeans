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

package org.netbeans.modules.csl.navigation.actions;

import org.openide.filesystems.FileObject;
import org.openide.util.*;
import javax.swing.*;
import java.awt.event.*;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.parsing.api.Source;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 *
 * An action that opens editor and jumps to the element given in constructor.
 * Similar to editor's go to declaration action.
 *
 * @author tim, Dafe Simonek
 */
public final class OpenAction extends AbstractAction {
    
    private ElementHandle elementHandle;   
    private FileObject fileObject;
    private long start;
      
    public OpenAction(ElementHandle elementHandle, FileObject fileObject, long start) {
        this.elementHandle = elementHandle;
        this.fileObject = fileObject;
        this.start = start;
        putValue ( Action.NAME, NbBundle.getMessage ( OpenAction.class, "LBL_Goto" ) ); //NOI18N
    }
    
    public void actionPerformed (ActionEvent ev) {
        if (fileObject != null && elementHandle == null) {
            UiUtils.open(fileObject, (int)start);
            return;
        }
        ElementHandle handle = elementHandle;
        FileObject primaryFile = DataLoadersBridge.getDefault().getPrimaryFile(fileObject);

        if ((primaryFile != null) && (handle != null)) {
            Source js = Source.create(primaryFile);
            if (js != null) {
                UiUtils.open(js, handle);
            }
        }
    }

    public @Override boolean isEnabled () {
          return true;
    }

    
}
