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
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.Hacks.InspectAndTransformOpener;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=InspectAndTransformOpener.class)
public class InspectAndTransformOpenerImpl implements InspectAndTransformOpener {

    @Override
    public void openIAT(final HintMetadata hm) {
        Node[] n = TopComponent.getRegistry().getActivatedNodes();
        final Lookup context = n.length > 0 ? n[0].getLookup():Lookup.EMPTY;
        org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities.invokeAfterScanFinished(new Runnable() {
            @Override
            public void run() {
                InspectAndRefactorUI.openRefactoringUI(new ProxyLookup(context, Lookups.singleton(hm)));
            }
        }, Bundle.CTL_ApplyPatternAction());
    }
    
}
