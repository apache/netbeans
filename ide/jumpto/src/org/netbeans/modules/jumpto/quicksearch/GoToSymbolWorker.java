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


package org.netbeans.modules.jumpto.quicksearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ListModel;
import org.netbeans.modules.jumpto.symbol.SymbolComparator;
import org.netbeans.modules.jumpto.symbol.SymbolProviderAccessor;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Lookup;

/**
 * Copy/paste from GoToSymbolAction
 * @author  Jan Becicka
 */
public class GoToSymbolWorker implements Runnable {

    private volatile boolean isCanceled = false;
    private volatile SymbolProvider current;
    private final String text;
    private final long createTime;
    private Logger LOGGER = Logger.getLogger(GoToSymbolWorker.class.getName());
    private Collection<? extends SymbolProvider> typeProviders;


    public GoToSymbolWorker(String text) {
        this.text = text;
        this.createTime = System.currentTimeMillis();
        //LOGGER.fine("Worker for " + text + " - created after " + (System.currentTimeMillis() - panel.time) + " ms.");
    }

    private List<? extends SymbolDescriptor> types;

    public List<? extends SymbolDescriptor> getTypes() {
        return types==null?Collections.<SymbolDescriptor>emptyList():types;
    }

    public void run() {

        LOGGER.fine("Worker for " + text + " - started " + (System.currentTimeMillis() - createTime) + " ms.");

        types = getSymbolNames(text);
        if (isCanceled) {
            LOGGER.fine("Worker for " + text + " exited after cancel " + (System.currentTimeMillis() - createTime) + " ms.");
            return;
        }
        final ListModel fmodel = Models.fromList(types, null, null);
        if (isCanceled) {
            LOGGER.fine("Worker for " + text + " exited after cancel " + (System.currentTimeMillis() - createTime) + " ms.");
            return;
        }

//        if (!isCanceled && fmodel != null) {
//            LOGGER.fine("Worker for text " + text + " finished after " + (System.currentTimeMillis() - createTime) + " ms.");
//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    panel.setModel(fmodel);
//                    if (okButton != null && !types.isEmpty()) {
//                        okButton.setEnabled(true);
//                    }
//                }
//            });
//        }


    }

    public void cancel() {
//        if (panel.time != -1) {
//            LOGGER.fine("Worker for text " + text + " canceled after " + (System.currentTimeMillis() - createTime) + " ms.");
//        }
        SymbolProvider _provider;
        synchronized (this) {
            isCanceled = true;
            _provider = current;
        }
        if (_provider != null) {
            _provider.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    private List<? extends SymbolDescriptor> getSymbolNames(String text) {
        // TODO: Search twice, first for current project, then for all projects
        List<SymbolDescriptor> items;
        // Multiple providers: merge results
        items = new ArrayList<SymbolDescriptor>(128);
        String[] message = new String[1];
        SymbolProvider.Context context = SymbolProviderAccessor.DEFAULT.createContext(null, text, SearchType.CASE_INSENSITIVE_PREFIX);
        for (SymbolProvider provider : getTypeProviders()) {
            SymbolProvider.Result result = SymbolProviderAccessor.DEFAULT.createResult(items, message, context, provider);
            current = provider;
            if (isCanceled) {
                return null;
            }
            LOGGER.fine("Calling SymbolProvider: " + provider);
            provider.computeSymbolNames(context, result);
            current = null;
        }
        if (!isCanceled) {
            //time = System.currentTimeMillis();
            items.sort(SymbolComparator.create(GoToSettings.SortingType.LEXICOGRAPHIC, text, false, true));
            //panel.setWarning(message[0]);
            //sort += System.currentTimeMillis() - time;
            //LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort );
            return items;
        } else {
            return null;
        }
    }
    private Collection<? extends SymbolProvider> getTypeProviders() {
        if (typeProviders==null) {
            typeProviders = Arrays.asList(Lookup.getDefault().lookupAll(SymbolProvider.class).toArray(new SymbolProvider[0]));
        }
        return typeProviders;
    }

}

