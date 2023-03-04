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
package org.netbeans.modules.editor.fold;

import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldManagerFactory;

/*
 * FoldHierarchyExecutionTest.java
 * JUnit based test
 *
 * Created on June 27, 2004, 1:03 AM
 */


/**
 *
 * @author mmetelka
 */
public class FoldHierarchyTestEnv {
    
    private JEditorPane pane;
    
    public FoldHierarchyTestEnv(FoldManagerFactory factory) {
        this(new FoldManagerFactory[] { factory });
    }

    public FoldHierarchyTestEnv(FoldManagerFactory... factories) {
        pane = new JEditorPane();
        assert (getMimeType() != null);
        pane.setDocument(new BaseDocument(false, "text/plain"));

        FoldManagerFactoryProvider.setForceCustomProvider(true);
        FoldManagerFactoryProvider provider = FoldManagerFactoryProvider.getDefault();
        assert (provider instanceof CustomProvider)
            : "setForceCustomProvider(true) did not ensure CustomProvider use"; // NOI18N

        CustomProvider customProvider = (CustomProvider)provider;
        customProvider.removeAllFactories(); // cleanup all registered factories
        customProvider.registerFactories(getMimeType(), factories);
    }

    public JEditorPane getPane() {
        return pane;
    }
    
    public AbstractDocument getDocument() {
        return (AbstractDocument)getPane().getDocument();
    }
    
    public String getMimeType() {
        return pane.getEditorKit().getContentType();
    }
    
    public FoldHierarchy getHierarchy() {
        FoldHierarchy hierarchy = FoldHierarchy.get(getPane());
        FoldHierarchyExecution.waitHierarchyInitialized(getPane());
        assert (hierarchy != null);
        return hierarchy;
    }
    
}
