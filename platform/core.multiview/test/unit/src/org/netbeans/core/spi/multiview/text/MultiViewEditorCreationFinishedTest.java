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

package org.netbeans.core.spi.multiview.text;

import java.awt.GraphicsEnvironment;
import java.io.Serializable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.text.CloneableEditorCreationFinishedTest;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/** Ensuring compatible behavior between 
 */
public class MultiViewEditorCreationFinishedTest extends CloneableEditorCreationFinishedTest {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewEditorCreationFinishedTest.class);
    }

    public MultiViewEditorCreationFinishedTest(String s) {
        super(s);
    }

    @Override
    protected Pane createPane(CloneableEditorSupport sup) {
        final Lookup lkp = Lookups.fixed(sup);
        class P implements Serializable, Lookup.Provider {
            @Override
            public Lookup getLookup() {
                return lkp;
            }
        }
        CloneableTopComponent pane = MultiViews.createCloneableMultiView("text/x-compat-test", new P());
        return (Pane) pane;
    }
    
    @MultiViewElement.Registration(
            displayName="editor",
            mimeType="text/x-compat-test",
            persistenceType=TopComponent.PERSISTENCE_NEVER,
            preferredID="editor"
    )
    public static MultiViewEditorElement create(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
}
