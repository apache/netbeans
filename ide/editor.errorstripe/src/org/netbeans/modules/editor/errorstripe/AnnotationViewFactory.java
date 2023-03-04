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

package org.netbeans.modules.editor.errorstripe;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.MimeTypeInitializer;
import org.netbeans.editor.SideBarFactory;
import org.openide.ErrorManager;



/**
 *
 * @author Jan Lahoda
 */
public class AnnotationViewFactory implements SideBarFactory, MimeTypeInitializer {

    /** Creates a new instance of AnnotationViewFactory */
    public AnnotationViewFactory() {
    }

    public JComponent createSideBar(JTextComponent target) {
        long start = System.currentTimeMillis();

        AnnotationView view = new AnnotationView(target);
        
        long end = System.currentTimeMillis();
        
        if (AnnotationView.TIMING_ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            AnnotationView.TIMING_ERR.log(ErrorManager.INFORMATIONAL, "creating AnnotationView component took: " + (end - start));
        }
        
        return view;
    }

    public void init(String mimeType) {
        AnnotationViewDataImpl.initProviders(mimeType);
    }
    
}
