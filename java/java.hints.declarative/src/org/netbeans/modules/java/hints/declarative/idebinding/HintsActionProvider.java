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
package org.netbeans.modules.java.hints.declarative.idebinding;

import java.util.Collection;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintRegistry;
import org.netbeans.modules.java.hints.declarative.HintDataObject;
import org.netbeans.modules.java.hints.spiimpl.refactoring.InspectAndRefactorUI;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=ActionProvider.class)
public class HintsActionProvider implements ActionProvider {

    @Override
    public String[] getSupportedActions() {
        return new String[]{ActionProvider.COMMAND_RUN_SINGLE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        assert ActionProvider.COMMAND_RUN_SINGLE.equals(command);
        
        HintDataObject hdo = context.lookup(HintDataObject.class);
        
        assert hdo != null;
        
        EditorCookie ec = hdo.getLookup().lookup(EditorCookie.class);
        final Document doc = ec.getDocument();
        Map<HintMetadata, Collection<? extends HintDescription>> hints;
        
        if (doc != null) {
            final String[] spec = new String[1];
            doc.render(() -> {
                try {
                    spec[0] = doc.getText(0, doc.getLength());
                } catch (BadLocationException ex) {
                    //should not happen...
                    Exceptions.printStackTrace(ex);
                    spec[0] = "";
                }
            });
            hints = DeclarativeHintRegistry.parseHints(hdo.getPrimaryFile(), spec[0]);
        } else {
            hints = DeclarativeHintRegistry.parseHintFile(hdo.getPrimaryFile());
        }

        if (hints.isEmpty()) {
            StatusDisplayer.getDefault().setStatusText("No hints specified in " + FileUtil.getFileDisplayName(hdo.getPrimaryFile()));
            return;
        }

        HintMetadata m = hints.entrySet().iterator().next().getKey();
        InspectAndRefactorUI.openRefactoringUI(Lookups.singleton(new InspectAndRefactorUI.HintWrap(m, DeclarativeHintRegistry.join(hints))));
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        assert ActionProvider.COMMAND_RUN_SINGLE.equals(command);
        return context.lookup(HintDataObject.class) != null;
    }
    
}
