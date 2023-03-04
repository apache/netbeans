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

package org.netbeans.modules.web.jsf;

import java.io.Serializable;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author petr
 */
public class JSFConfigEditorContextImpl implements JSFConfigEditorContext, Serializable{
    static final long serialVersionUID = -4802489998350639459L;

    JSFConfigDataObject jsfDataObject;
    /** Creates a new instance of JSFConfigContextImpl */
    public JSFConfigEditorContextImpl(JSFConfigDataObject data) {
        jsfDataObject = data;
    }
    
    public FileObject getFacesConfigFile() {
        return jsfDataObject.getPrimaryFile();
    }

    public UndoRedo getUndoRedo() {
        return jsfDataObject.getEditorSupport().getUndoRedoManager();
    }

    public void setMultiViewTopComponent(TopComponent topComponent) {
        jsfDataObject.getEditorSupport().setMVTC(topComponent);
    }

}
