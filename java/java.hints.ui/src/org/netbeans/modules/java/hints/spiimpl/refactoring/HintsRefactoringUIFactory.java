/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.HintsRefactoringFactory;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = HintsRefactoringFactory.class)
public class HintsRefactoringUIFactory implements HintsRefactoringFactory {

    public static Collection<? extends HintDescription> join(Map<HintMetadata, Collection<? extends HintDescription>> hints) {
        List<HintDescription> descs = new LinkedList<>();

        for (Collection<? extends HintDescription> c : hints.values()) {
            descs.addAll(c);
        }

        return descs;
    }

    @Override
    public RefactoringUI createRefactoringUI(Map<HintMetadata, Collection<? extends HintDescription>> hints) {
        if (hints.isEmpty()) {
            return null;
        }
        HintMetadata m = hints.keySet().iterator().next();
        InspectAndRefactorUI.HintWrap wrap = new InspectAndRefactorUI.HintWrap(m, join(hints));
        return new InspectAndRefactorUI(null, true, false, Lookups.singleton(wrap));
    }
    
}
