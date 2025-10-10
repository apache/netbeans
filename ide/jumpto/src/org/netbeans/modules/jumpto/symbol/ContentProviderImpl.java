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
package org.netbeans.modules.jumpto.symbol;

import java.awt.Dialog;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.jumpto.common.AbstractModelFilter;
import org.netbeans.modules.jumpto.common.CurrentSearch;
import org.netbeans.modules.jumpto.common.Factory;
import org.netbeans.modules.jumpto.common.ItemRenderer;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.modules.jumpto.common.Models.MutableListModel;
import org.netbeans.modules.jumpto.common.Utils;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.spi.jumpto.support.AsyncDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
final class ContentProviderImpl implements GoToPanelImpl.ContentProvider {

    private static final Logger LOG = Logger.getLogger(ContentProviderImpl.class.getName());
    private static final RequestProcessor rp = new RequestProcessor (ContentProviderImpl.class);

    private final JButton okButton;
    private final AtomicReference<Collection<? extends SymbolProvider>> typeProviders = new AtomicReference<>();
    private final CurrentSearch<SymbolDescriptor> currentSearch = new CurrentSearch<>(() -> {
            class Filter extends AbstractModelFilter<SymbolDescriptor> implements ChangeListener {

                Filter() {
                    addChangeListener(this);
                }

                @Override
                public void stateChanged(ChangeEvent e) {
                    final SymbolDescriptorAttrCopier copier = currentSearch.getAttribute(SymbolDescriptorAttrCopier.class);
                    if (copier != null) {
                        copier.clearWrongCase();
                    }
                }

                @NonNull
                @Override
                protected String getItemValue(@NonNull final SymbolDescriptor item) {
                    String name = item.getSimpleName();
                    if (name == null) {
                        //The SymbolDescriptor does not provide simple name
                        //the symbol name contains parameter names, so it's needed to strip them
                        name = item.getSymbolName();
                        final String[] nameParts = name.split("\\s+|\\(");  //NOI18N
                        name = nameParts[0];
                    }
                    return name;
                }

                @Override
                protected void update(@NonNull final SymbolDescriptor item) {
                    String searchText = getSearchText();
                    if (searchText == null) {
                        searchText = "";    //NOI18N
                    }
                    SymbolProviderAccessor.DEFAULT.setHighlightText(item, searchText);
                    final SymbolDescriptorAttrCopier copier = currentSearch.getAttribute(SymbolDescriptorAttrCopier.class);
                    if (copier != null) {
                        if (item instanceof AsyncDescriptor && !((AsyncDescriptor<SymbolDescriptor>)item).hasCorrectCase()) {
                            copier.reportWrongCase((AsyncDescriptor<SymbolDescriptor>)item);
                        }
                    }
                }
            }
            return new Filter();
    });
    //@GuardedBy("this")
    private RequestProcessor.Task task;
    //@GuardedBy("this")
    private Worker running;
    //threading: accessed only in EDT
    private Dialog dialog;
    private volatile SymbolComparator itemsComparator;


    public ContentProviderImpl(final JButton okButton) {
        this.okButton = okButton;
    }


    void setDialog(final Dialog dialog) {
        this.dialog = dialog;
    }


    @Override
    public ListCellRenderer getListCellRenderer(
            @NonNull final JList list,
            @NonNull final ButtonModel caseSensitive) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        return ItemRenderer.Builder.create(
            list,
            caseSensitive,
            new SymbolDescriptorCovertor()).build();
    }

    @Override
    public boolean setListModel(GoToPanel panel, String text) {
        enableOK(false);
        final Worker workToCancel;
        final RequestProcessor.Task  taskToCancel;
        synchronized (this) {
            workToCancel = running;
            taskToCancel = task;
            running = null;
            task = null;
        }
        if (workToCancel != null) {
                workToCancel.cancel();
        }
        if (taskToCancel != null) {
                taskToCancel.cancel();
        }

        if ( text == null ) {
            currentSearch.resetFilter();
            panel.setModel(new DefaultListModel(), true);
            return false;
        }
        final boolean exact = text.endsWith(" "); // NOI18N
        final boolean isCaseSensitive = panel.isCaseSensitive();
        text = text.strip();
        if ( text.isEmpty() || !Utils.isValidInput(text)) {
            currentSearch.filter(
                    SearchType.EXACT_NAME,
                    text,
                    Collections.singletonMap(AbstractModelFilter.OPTION_CLEAR, Boolean.TRUE));
            panel.revalidateModel(true);
            return false;
        }

        final Pair<String,String> nameAndScope = Utils.splitNameAndScope(text);
        String name = nameAndScope.first();
        final SearchType searchType = Utils.getSearchType(name, exact, isCaseSensitive, null, null);
        if (searchType == SearchType.REGEXP || searchType == SearchType.CASE_INSENSITIVE_REGEXP) {
            name = Utils.removeNonNeededWildCards(name);
        }
        final String scope = Optional.ofNullable(nameAndScope.second())
                .map(Utils::removeNonNeededWildCards)
                .orElse(null);
        if (name.isEmpty()) {
            //Empty name, wait for next char
            currentSearch.resetFilter();
            panel.setModel(new DefaultListModel(), true);
            return false;
        }
        // Compute in other thread
        synchronized(this) {
            final SymbolDescriptorAttrCopier acp = currentSearch.getAttribute(SymbolDescriptorAttrCopier.class);
            final boolean correctCase = acp == null || acp.hasCorrectCase();
            if (currentSearch.isNarrowing(searchType, name, scope, correctCase)) {
                itemsComparator.setText(name);
                currentSearch.filter(searchType, name, null);
                enableOK(panel.revalidateModel(true));
                return false;
            } else {
                running = new Worker(text, name, searchType, panel);
                task = rp.post( running, 500);
                if ( panel.getStartTime() != -1 ) {
                    LOG.log(
                       Level.FINE,
                       "Worker posted after {0} ms.",   //NOI18N
                       System.currentTimeMillis() - panel.getStartTime());
                }
                return true;
            }
        }
    }

    @Override
    public void closeDialog() {
        if (dialog != null) {
            dialog.setVisible( false );
            DialogFactory.storeDialogDimensions(
                    new Dimension(dialog.getWidth(), dialog.getHeight()));
            dialog.dispose();
            dialog = null;
            cleanUp();
        }
    }

    @Override
    public boolean hasValidContent() {
        return this.okButton != null && this.okButton.isEnabled();
    }

    /*test*/
    @NonNull
    Runnable createWorker(
            @NonNull final String text,
            @NonNull final SearchType searchType,
            @NonNull final GoToPanel panel) {
        return new Worker(text, text, searchType, panel);
    }

    private void enableOK(final boolean enabled) {
        if (okButton != null) {
            okButton.setEnabled (enabled);
        }
    }

    private void cleanUp() {
        for (SymbolProvider provider : getTypeProviders()) {
            provider.cleanup();
        }
        final SymbolDescriptorAttrCopier attrCopier = currentSearch.setAttribute(SymbolDescriptorAttrCopier.class, null);
        if (attrCopier != null) {
            attrCopier.clearWrongCase();
        }
    }

    private Collection<? extends SymbolProvider> getTypeProviders() {
        Collection<? extends SymbolProvider> res = typeProviders.get();
        if (res == null) {
            res = Arrays.asList(Lookup.getDefault().lookupAll(SymbolProvider.class).toArray(new SymbolProvider[0]));
            if (!typeProviders.compareAndSet(null, res)) {
                res = typeProviders.get();
            }
        }
        return res;
    }

    private static final class SymbolDescriptorCovertor implements ItemRenderer.Convertor<SymbolDescriptor> {
        @Override
        public String getName(@NonNull final SymbolDescriptor item) {
            return item.getSymbolName();
        }

        @Override
        public String getHighlightText(@NonNull final SymbolDescriptor item) {
            return SymbolProviderAccessor.DEFAULT.getHighlightText(item);
        }

        @Override
        public String getOwnerName(@NonNull final SymbolDescriptor item) {
            return NbBundle.getMessage(GoToSymbolAction.class, "MSG_DeclaredIn", item.getOwnerName());
        }

        @Override
        public String getProjectName(@NonNull final SymbolDescriptor item) {
            return item.getProjectName();
        }

        @Override
        public String getFilePath(@NonNull final SymbolDescriptor item) {
            return item.getFileDisplayPath();
        }

        @Override
        public Icon getItemIcon(@NonNull final SymbolDescriptor item) {
            return item.getIcon();
        }

        @Override
        public Icon getProjectIcon(@NonNull final SymbolDescriptor item) {
            return item.getProjectIcon();
        }

        @Override
        public boolean isFromCurrentProject(@NonNull final SymbolDescriptor item) {
            return false;
        }
    }

    private class Worker implements Runnable {

        private final String text;
        private final String name;
        private final SearchType searchType;
        private final long createTime;
        private final GoToPanel panel;

        private volatile boolean isCanceled = false;
        private volatile SymbolProvider current;

        Worker(
                @NonNull final String text,
                @NonNull final String name,
                @NonNull final SearchType searchType,
                @NonNull final GoToPanel panel ) {
            this.text = text;
            this.name = name;
            this.searchType = searchType;
            this.panel = panel;
            this.createTime = System.currentTimeMillis();
            LOG.log(
                Level.FINE,
                "Worker for {0} - created after {1} ms.", //NOI18N
                new Object[]{text, System.currentTimeMillis() - panel.getStartTime()});
       }

        @Override
        public void run() {
            LOG.log(
                Level.FINE,
                "Worker for {0} - started {1} ms.", //NOI18N
                new Object[]{text, System.currentTimeMillis() - createTime});
            final List<SymbolDescriptor> transientItems = new ArrayList<>(512);
            Collection<? extends SymbolProvider> providers = getTypeProviders();
            int lastSize = -1, lastProvCount = providers.size();
            final int[] newSize = new int[1];
            final SymbolDescriptorAttrCopier attrCopier = new SymbolDescriptorAttrCopier();
            final SymbolComparator ic = SymbolComparator.create(
                    GoToSettings.getDefault().getSortingType(),
                    name,
                    Utils.isCaseSensitive(searchType),
                    GoToSettings.getDefault().isSortingPreferOpenProjects());
            itemsComparator = ic;
            final MutableListModel<SymbolDescriptor> model = Models.mutable(
                    ic,
                    currentSearch.resetFilter(),
                    attrCopier);
            try {
                while(true) {
                    final Result res = getSymbolNames(text, providers);
                    if (isCanceled) {
                        LOG.log(
                            Level.FINE,
                            "Worker for {0} exited after cancel {1} ms.", //NOI18N
                            new Object[]{text, System.currentTimeMillis() - createTime});
                        return;
                    }
                    final Collection<? extends SymbolDescriptor> toRemove = new ArrayList<>(transientItems);
                    final Collection<? extends SymbolDescriptor> toAdd = mergeSymbols(
                            transientItems,
                            res.symbols,
                            providers,
                            res.nonFinishedProviders,
                            attrCopier,
                            newSize);
                    final boolean done = res.retry <= 0;
                    final int newProvCount = res.nonFinishedProviders.size();
                    final boolean resultChanged = lastSize != newSize[0] || lastProvCount != newProvCount;
                    if (done || resultChanged) {
                        lastSize = newSize[0];
                        lastProvCount = newProvCount;
                        model.remove(toRemove);
                        model.add(toAdd);
                        attrCopier.checkWrongCase(toRemove, toAdd);
                        if ( isCanceled ) {
                            LOG.log(
                                Level.FINE,
                                "Worker for {0} exited after cancel {1} ms.", //NOI18N
                                new Object[]{text, System.currentTimeMillis() - createTime});
                            return;
                        }
                        LOG.log(
                            Level.FINE,
                            "Worker for text {0} finished after {1} ms.", //NOI18N
                            new Object[]{text, System.currentTimeMillis() - createTime});
                        SwingUtilities.invokeLater(() -> {
                            if (done) {
                                final Pair<String, String> nameAndScope = Utils.splitNameAndScope(text);
                                final SymbolDescriptorAttrCopier oldAttrCopier = currentSearch.setAttribute(SymbolDescriptorAttrCopier.class, attrCopier);
                                if (oldAttrCopier != null) {
                                    oldAttrCopier.clearWrongCase();
                                }
                                currentSearch.searchCompleted(
                                        searchType,
                                        nameAndScope.first(),
                                        nameAndScope.second());
                            }
                            if (!isCanceled) {
                                enableOK(panel.setModel(model, done));
                            }
                        });
                    }
                    if (done) {
                        return;
                    } else {
                        providers = res.nonFinishedProviders;
                        try {
                            Thread.sleep(res.retry);
                        } catch (InterruptedException ex) {
                            //pass
                        }
                    }
                }
            } finally {
                attrCopier.searchCompleted();
            }
        }

        public void cancel() {
            if ( panel.getStartTime() != -1 ) {
                LOG.log(
                    Level.FINE,
                    "Worker for text {0} canceled after {1} ms.", //NOI18N
                    new Object[]{text, System.currentTimeMillis() - createTime});
            }
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
        @CheckForNull
        private Result getSymbolNames(
                final String text,
                final Collection<? extends SymbolProvider> providers) {
            // TODO: Search twice, first for current project, then for all projects
            Collection<SymbolDescriptor> items;
            // Multiple providers: merge results
            items = new HashSet<>(128);
            String[] message = new String[1];
            int retry = 0;
            final Collection<SymbolProvider> nonFinishedProviders = Collections.newSetFromMap(new IdentityHashMap<SymbolProvider, Boolean>());
            for (SymbolProvider provider : providers) {
                current = provider;
                try {
                    if (isCanceled) {
                        return null;
                    }
                    LOG.log(
                        Level.FINE,
                        "Calling SymbolProvider: {0}", //NOI18N
                        provider);
                    final SymbolProvider.Context context = SymbolProviderAccessor.DEFAULT.createContext(null, text, searchType);
                    final SymbolProvider.Result result = SymbolProviderAccessor.DEFAULT.createResult(items, message, context, provider);
                    provider.computeSymbolNames(context, result);
                    final int providerRetry = SymbolProviderAccessor.DEFAULT.getRetry(result);
                    if (providerRetry > 0) {
                        nonFinishedProviders.add(provider);
                    }
                    retry = mergeRetryTimeOut(retry, providerRetry);
                } finally {
                    current = null;
                }
            }
            if ( !isCanceled ) {
                if (retry > 0 && message[0] == null) {
                    //SymbolProvider needs restart but did not provide
                    //warning message to user, use generic one
                    message[0] = NbBundle.getMessage(ContentProviderImpl.class, "TXT_PartialResults");
                }
                panel.setWarning(message[0]);
                return new Result (items, nonFinishedProviders, retry);
            } else {
                return null;
            }
        }

        private int mergeRetryTimeOut(
            final int t1,
            final int t2) {
            if (t1 == 0) {
                return t2;
            }
            if (t2 == 0) {
                return t1;
            }
            return Math.min(t1,t2);
        }

        @NonNull
        private Collection<? extends SymbolDescriptor> mergeSymbols(
            @NonNull final Collection<SymbolDescriptor> transientSymbols,
            @NonNull final Collection<? extends SymbolDescriptor> newSymbols,
            @NonNull final Collection<? extends SymbolProvider> usedProviders,
            @NonNull final Collection<? extends SymbolProvider> nonFinishedProviders,
            @NonNull final SymbolDescriptorAttrCopier attrCopier,
            @NonNull final int[] newSize) {
            transientSymbols.clear();
            newSize[0] = 0;
            for (Iterator<? extends SymbolDescriptor> it = newSymbols.iterator(); it.hasNext();) {
                final SymbolDescriptor newSymbol = it.next();
                if (nonFinishedProviders.contains(SymbolProviderAccessor.DEFAULT.getSymbolProvider(newSymbol))) {
                    newSize[0]++;
                    transientSymbols.add(newSymbol);
                }
                if (attrCopier.isResolved(newSymbol)) {
                    it.remove();
                }
            }
            return newSymbols;
        }
    }

    private static final class SymbolDescriptorAttrCopier implements Factory<SymbolDescriptor, Pair<? extends SymbolDescriptor,? extends SymbolDescriptor>> {

        private final Set</*@GuardedBy("hasWrongCase")*/AsyncDescriptor<SymbolDescriptor>> hasWrongCase;
        private final Set</*@GuardedBy("resolved")*/SymbolDescriptor> resolved;

        SymbolDescriptorAttrCopier() {
            hasWrongCase = Collections.synchronizedSet(new HashSet<AsyncDescriptor<SymbolDescriptor>>() {
                @Override
                public boolean removeAll(Collection<?> c) {
                    boolean modified = false;
                    for (Object o : c) {
                        modified |= remove(o);
                    }
                    return modified;
                }
            });
            resolved = Collections.synchronizedSet(new HashSet<SymbolDescriptor>());
        }

        void checkWrongCase(
                @NonNull final Collection<? extends SymbolDescriptor> remove,
                @NonNull final Collection<? extends SymbolDescriptor> add) {
            hasWrongCase.removeAll(remove);
            for (SymbolDescriptor d : add) {
                if (d instanceof AsyncDescriptor && !((AsyncDescriptor<SymbolDescriptor>)d).hasCorrectCase()) {
                    reportWrongCase((AsyncDescriptor<SymbolDescriptor>)d);
                }
            }
        }

        void clearWrongCase() {
            hasWrongCase.clear();
        }

        void reportWrongCase(@NonNull final AsyncDescriptor<SymbolDescriptor> d) {
            hasWrongCase.add(d);
        }

        boolean hasCorrectCase() {
            return hasWrongCase.isEmpty();
        }

        void searchCompleted() {
            resolved.clear();
        }

        boolean isResolved(@NonNull final SymbolDescriptor desc) {
            return resolved.contains(desc);
        }

        @Override
        @NonNull
        public SymbolDescriptor create(@NonNull final Pair<? extends SymbolDescriptor, ? extends SymbolDescriptor> p) {
            final SymbolDescriptor source = p.first();
            final SymbolDescriptor target = p.second();
            resolved.add(source);
            if (source instanceof AsyncDescriptor && !((AsyncDescriptor<SymbolDescriptor>)source).hasCorrectCase()) {
                hasWrongCase.remove(source);
            }
            SymbolProviderAccessor.DEFAULT.setHighlightText(
                    target,
                    SymbolProviderAccessor.DEFAULT.getHighlightText(source));
            SymbolProviderAccessor.DEFAULT.setSymbolProvider(
                    target,
                    SymbolProviderAccessor.DEFAULT.getSymbolProvider(source));
            return target;
        }
    }

    private static final class Result {
        final Collection<SymbolDescriptor> symbols;
        final int retry;
        final Collection<SymbolProvider> nonFinishedProviders;

        Result (
                @NonNull final Collection<SymbolDescriptor> symbols,
                @NonNull final Collection<SymbolProvider> providers,
                final int retry) {
            assert symbols != null;
            assert providers != null;
            this.symbols = symbols;
            this.nonFinishedProviders = providers;
            this.retry = retry;
            assert this.retry > 0 ? !this.nonFinishedProviders.isEmpty() :
                    this.nonFinishedProviders.isEmpty();
        }
    }
}
