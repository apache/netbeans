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
package org.netbeans.modules.analysis.api;

import org.netbeans.modules.analysis.RunAnalysis;
import org.netbeans.modules.analysis.RunAnalysisPanel.DialogState;
import org.netbeans.modules.analysis.spi.Analyzer.WarningDescription;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**XXX: refers to SPI class (WarningDescription) - should be moved to the API!
 *
 * @author lahvac
 */
public class CodeAnalysis {
    
    public static void open(WarningDescription wd) {
        Node[] n = TopComponent.getRegistry().getActivatedNodes();
        final Lookup context = n.length > 0 ? n[0].getLookup():Lookup.EMPTY;
        RunAnalysis.showDialogAndRunAnalysis(
                new ProxyLookup(Utilities.actionsGlobalContext(), 
                        context), DialogState.from(wd));
    }
}
