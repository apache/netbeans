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

package org.netbeans.modules.jumpto.type;

import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.jumpto.EntityComparator;
import org.netbeans.modules.jumpto.common.AbstractModelFilter;
import org.netbeans.modules.jumpto.common.CurrentSearch;
import org.netbeans.modules.jumpto.common.ItemRenderer;
import org.netbeans.modules.jumpto.common.Utils;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.modules.sampler.Sampler;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * XXX split into action and support class, left this just to minimize diff
 * XXX Don't look for all projects (do it lazy in filter or renderer)
 * @author Petr Hrebejk
 */
public class GoToTypeAction extends AbstractAction implements GoToPanel.ContentProvider {

    static final Logger LOGGER = Logger.getLogger(GoToTypeAction.class.getName()); // Used from the panel as well

    private SearchType nameKind;
    private static ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    private static final RequestProcessor rp = new RequestProcessor ("GoToTypeAction-RequestProcessor",1);      //NOI18N
    private static final RequestProcessor PROFILE_RP = new RequestProcessor("GoToTypeAction-Profile",1);        //NOI18N
    private Worker running;
    private RequestProcessor.Task task;
    GoToPanel panel;
    private Dialog dialog;
    private JButton okButton;
    private Collection<? extends TypeProvider> typeProviders;
    private final Collection<? extends TypeProvider> implicitTypeProviders;
    private final TypeBrowser.Filter typeFilter;
    private final String title;
    private final boolean multiSelection;
    private final CurrentSearch<TypeDescriptor> currentSearch;
    private volatile TypeComparator itemsComparator;

    /** Creates a new instance of OpenTypeAction */
    public GoToTypeAction() {
        this(
            NbBundle.getMessage( GoToTypeAction.class, "DLG_GoToType" ),
            null,
            true
        );
    }

    public GoToTypeAction(String title, TypeBrowser.Filter typeFilter, boolean multiSelection, TypeProvider... typeProviders) {
        super( NbBundle.getMessage( GoToTypeAction.class,"TXT_GoToType") );
        putValue("PopupMenuText", NbBundle.getBundle(GoToTypeAction.class).getString("editor-popup-TXT_GoToType")); // NOI18N
        this.title = title;
        this.typeFilter = typeFilter;
        this.implicitTypeProviders = typeProviders.length == 0 ? null : Collections.unmodifiableCollection(Arrays.asList(typeProviders));
        this.multiSelection = multiSelection;
        this.currentSearch = new CurrentSearch(() -> new AbstractModelFilter<TypeDescriptor>() {
            @Override
            @NonNull
            protected String getItemValue(@NonNull final TypeDescriptor item) {
                return item.getSimpleName();
            }
            @Override
            protected void update(@NonNull final TypeDescriptor item) {
                String searchText = getSearchText();
                if (searchText == null) {
                    searchText = "";    //NOI18N
                }
                TypeProviderAccessor.DEFAULT.setHighlightText(item, searchText);
            }
        });
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        final Iterable<? extends TypeDescriptor> selectedTypes = getSelectedTypes();
        if (selectedTypes.iterator().hasNext()) {
            JumpList.checkAddEntry();
            for (TypeDescriptor td : selectedTypes) {
                td.open();
            }
        }
    }

    public Iterable<? extends TypeDescriptor> getSelectedTypes() {
        return getSelectedTypes(true);
    }

    public Iterable<? extends TypeDescriptor> getSelectedTypes(final boolean visible) {
        return getSelectedTypes(visible, null);
    }

    public Iterable<? extends TypeDescriptor> getSelectedTypes(final boolean visible, String initSearchText) {
        Iterable<? extends TypeDescriptor> result = Collections.emptyList();
        try {
            panel = new GoToPanel(this, multiSelection);
            dialog = createDialog(panel);

            if (initSearchText != null) {
                panel.setInitialText(initSearchText);
            } else {
                Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
                if (arr.length > 0) {
                    EditorCookie ec = arr[0].getCookie (EditorCookie.class);
                    if (ec != null) {
                        JEditorPane recentPane = NbDocument.findRecentEditorPane(ec);
                        if (recentPane != null) {
                            initSearchText = org.netbeans.editor.Utilities.getSelectionOrIdentifier(recentPane);
                            if (initSearchText != null && org.openide.util.Utilities.isJavaIdentifier(initSearchText)) {
                                panel.setInitialText(initSearchText);
                            } else {
                                panel.setInitialText(arr[0].getName());
                            }
                        }
                    }
                }
            }

            dialog.setVisible(visible);
            result = panel.getSelectedTypes();

        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return result;
    }

    @Override
    public boolean isEnabled () {
        return OpenProjects.getDefault().getOpenProjects().length>0;
    }

    // Implementation of content provider --------------------------------------


    @Override
    public ListCellRenderer getListCellRenderer(
            @NonNull final JList list,
            @NonNull final ButtonModel caseSensitive) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        return ItemRenderer.Builder.create(
                list,
                caseSensitive,
                new TypeDescriptorConvertor()).build();
    }


    @Override
    public boolean setListModel( GoToPanel panel, String text ) {
        assert SwingUtilities.isEventDispatchThread();
        enableOK(false);
        //handling http://netbeans.org/bugzilla/show_bug.cgi?id=178555
        //add a MouseListener to the messageLabel JLabel so that the search can be cancelled without exiting the dialog
        final GoToPanel goToPanel = panel;
        final MouseListener warningMouseListener = new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (running != null) {
                    running.cancel();
                    task.cancel();
                    running = null;
                }
                goToPanel.setListPanelContent(NbBundle.getMessage(GoToPanel.class, "TXT_SearchCanceled"),false); // NOI18N
            }
        };
        panel.setMouseListener(warningMouseListener);
        if ( running != null ) {
            running.cancel();
            task.cancel();
            running = null;
        }

        if ( text == null ) {
            currentSearch.resetFilter();
            panel.setModel(EMPTY_LIST_MODEL);
            return false;
        }

        final boolean exact = text.endsWith(" "); // NOI18N
        text = text.trim();
        if ( text.isEmpty() || !Utils.isValidInput(text)) {
            currentSearch.filter(
                    SearchType.EXACT_NAME,
                    text,
                    Collections.singletonMap(AbstractModelFilter.OPTION_CLEAR, Boolean.TRUE));
            panel.revalidateModel();
            return false;
        }

        final Pair<String,String> nameAndScope = Utils.splitNameAndScope(text);
        String name = nameAndScope.first();        
        nameKind = Utils.getSearchType(name, exact, panel.isCaseSensitive(), null, null);
        if (nameKind == SearchType.REGEXP || nameKind == SearchType.CASE_INSENSITIVE_REGEXP) {
            name = Utils.removeNonNeededWildCards(name);
        }
        final String scope = Optional.ofNullable(nameAndScope.second())
                .map(Utils::removeNonNeededWildCards)
                .orElse(null);
        if (name.isEmpty() && scope == null) {
            //Empty name, wait for next char
            currentSearch.resetFilter();
            panel.setModel(EMPTY_LIST_MODEL);
            return false;
        }

        // Compute in other thread
        if (currentSearch.isNarrowing(nameKind, name, scope, true)) {
            itemsComparator.setText(name);
            currentSearch.filter(nameKind, name, null);
            enableOK(panel.revalidateModel());
            return false;
        } else {
            running = new Worker(text, name, panel.isCaseSensitive());
            task = rp.post( running, 500);
            if ( panel.time != -1 ) {
                LOGGER.log( Level.FINE, "Worker posted after {0} ms.", System.currentTimeMillis() - panel.time ); //NOI18N
            }
            return true;
        }
    }

    @Override
    public void closeDialog() {
        // Closing event can be sent several times.
        if (dialog == null ) { // #172568
            return; // OK - the dialog has already been closed.
        }
        dialog.setVisible( false );
        cleanup();
    }

    @Override
    public boolean hasValidContent () {
        return this.okButton != null && this.okButton.isEnabled();
    }

    // Private methods ---------------------------------------------------------

    /** Creates the dialog to show
     */
   private Dialog createDialog( final GoToPanel panel) {

        okButton = new JButton (NbBundle.getMessage(GoToTypeAction.class, "CTL_OK"));
        okButton.getAccessibleContext().setAccessibleDescription(okButton.getText());
        okButton.setEnabled (false);
        panel.getAccessibleContext().setAccessibleName( NbBundle.getMessage( GoToTypeAction.class, "AN_GoToType") ); //NOI18N
        panel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( GoToTypeAction.class, "AD_GoToType") ); //NOI18N

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            panel,                             // innerPane
            title, // displayName
            true,
            new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
            okButton,
            DialogDescriptor.DEFAULT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new DialogButtonListener( panel ) );                                 // Action listener

         dialogDescriptor.setClosingOptions(new Object[] {okButton, DialogDescriptor.CANCEL_OPTION});

        // panel.addPropertyChangeListener( new HelpCtxChangeListener( dialogDescriptor, helpCtx ) );
//        if ( panel instanceof HelpCtx.Provider ) {
//            dialogDescriptor.setHelpCtx( ((HelpCtx.Provider)panel).getHelpCtx() );
//        }

        Dialog d = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        // Set size when needed
        final int width = UiOptions.GoToTypeDialog.getWidth();
        final int height = UiOptions.GoToTypeDialog.getHeight();
        if (width != -1 && height != -1) {
            d.setPreferredSize(new Dimension(width,height));
        }

        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        final Dimension dim = d.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        d.setBounds(Utilities.findCenterBounds(dim));
        initialDimension = dim;
        d.addWindowListener(new WindowAdapter() {
            public @Override void windowClosed(WindowEvent e) {
                cleanup();
            }
        });

        return d;

    }

    private Dimension initialDimension;

    private void cleanup() {
        assert SwingUtilities.isEventDispatchThread();
        if ( GoToTypeAction.this.dialog != null ) { // Closing event for some reson sent twice

            // Save dialog size only when changed
            final int currentWidth = dialog.getWidth();
            final int currentHeight = dialog.getHeight();
            if (initialDimension != null && (initialDimension.width != currentWidth || initialDimension.height != currentHeight)) {
                UiOptions.GoToTypeDialog.setHeight(currentHeight);
                UiOptions.GoToTypeDialog.setWidth(currentWidth);
            }
            initialDimension = null;
            // Clean caches
            GoToTypeAction.this.dialog.dispose();
            GoToTypeAction.this.dialog = null;
            //1st) Cancel current task
            if ( running != null ) {
                running.cancel();
                task.cancel();
                running = null;
            }
            //2nd do clean up in the same thread as init to prevent races
            rp.submit(() -> {
                assert rp.isRequestProcessorThread();
                if (typeProviders != null) {
                    for (TypeProvider provider : typeProviders) {
                        provider.cleanup();
                    }
                    typeProviders = null;
                }
            });
        }
    }

    private void enableOK(final boolean enable) {
        if (okButton != null) {
            okButton.setEnabled (enable);
        }
    }

    // Private classes ---------------------------------------------------------


    private static final class TypeDescriptorConvertor implements ItemRenderer.Convertor<TypeDescriptor> {
        @Override
        public String getName(@NonNull final TypeDescriptor item) {
            return item.getTypeName();
        }

        @Override
        public String getHighlightText(@NonNull final TypeDescriptor item) {
            return TypeProviderAccessor.DEFAULT.getHighlightText(item);
        }

        @Override
        public String getOwnerName(@NonNull final TypeDescriptor item) {
            return item.getContextName();
        }

        @Override
        public String getProjectName(@NonNull final TypeDescriptor item) {
            return item.getProjectName();
        }

        @Override
        public String getFilePath(@NonNull final TypeDescriptor item) {
            return item.getFileDisplayPath();
        }

        @Override
        public Icon getItemIcon(@NonNull final TypeDescriptor item) {
            return item.getIcon();
        }

        @Override
        public Icon getProjectIcon(@NonNull final TypeDescriptor item) {
            return item.getProjectIcon();
        }

        @Override
        public boolean isFromCurrentProject(@NonNull final TypeDescriptor item) {
            return false;
        }
    }

    private class Worker implements Runnable {

        private volatile boolean isCanceled = false;
        private volatile TypeProvider current;
        private final String text;
        private final String name;
        private final boolean caseSensitive;
        private final long createTime;

        public Worker(
                String text,
                String name,
                final boolean caseSensitive) {
            this.text = text;
            this.name = name;
            this.caseSensitive = caseSensitive;
            this.createTime = System.currentTimeMillis();
            LOGGER.log( Level.FINE, "Worker for {0} - created after {1} ms.",   //NOI18N
                    new Object[]{
                        text,
                        System.currentTimeMillis() - panel.time
                    });
        }

        @Override
        public void run() {
            final Future<?> f = OpenProjects.getDefault().openProjects();
            if (!f.isDone()) {
                try {
                    SwingUtilities.invokeLater(() -> {
                        panel.updateMessage(NbBundle.getMessage(GoToTypeAction.class, "TXT_LoadingProjects"));
                    });
                    f.get();
                } catch (InterruptedException | ExecutionException ex) {
                    LOGGER.fine(ex.getMessage());
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        panel.updateMessage(NbBundle.getMessage(GoToTypeAction.class, "TXT_Searching"));
                    });
                }
            }
            int lastSize = -1;
            final TypeComparator ic = TypeComparator.create(
                    GoToSettings.getDefault().getSortingType(),
                    name,
                    caseSensitive,
                    GoToSettings.getDefault().isSortingPreferOpenProjects());
            itemsComparator = ic;
            final Models.MutableListModel baseModel = Models.mutable(
                ic,
                currentSearch.resetFilter(),
                null);
            final ListModel model = typeFilter != null ?
                    FilteredListModel.create(baseModel, new FilterAdaptor(typeFilter), NbBundle.getMessage(GoToTypeAction.class, "LBL_Computing")) :
                    baseModel;
            for (;;) {
                final int[] retry = new int[1];

                Profile profile = initializeProfiling();
                try {
                    LOGGER.log( Level.FINE, "Worker for {0} - started {1} ms.",     //NOI18N
                            new Object[]{
                                text,
                                System.currentTimeMillis() - createTime
                            });

                    final List<? extends TypeDescriptor> types = getTypeNames(text, retry);
                    if ( isCanceled ) {
                        LOGGER.log( Level.FINE, "Worker for {0} exited after cancel {1} ms.",   //NOI18N
                                new Object[]{
                                    text,
                                    System.currentTimeMillis() - createTime
                                });
                        return;
                    }
                    final int newSize = types.size();
                    final boolean done = retry[0] <= 0;
                    final boolean resultChanged = lastSize != newSize;
                    //Optimistic the types just added, but safer is compare the collections.
                    //Unfortunatelly no TypeDescriptor impl provides equals.
                    if (resultChanged || done) {
                        lastSize = newSize;
                        if (resultChanged) {
                            baseModel.clear();
                            baseModel.add(types);
                        }
                        if (isCanceled) {
                            return;
                        }
                        SwingUtilities.invokeLater(() -> {
                            if (done) {
                                final Pair<String, String> nameAndScope = Utils.splitNameAndScope(text);
                                currentSearch.searchCompleted(nameKind, nameAndScope.first(), nameAndScope.second());
                            }
                            if (resultChanged && !isCanceled) {
                                enableOK(panel.setModel(model));
                            }
                        });
                    }
                } finally {
                    if (profile != null) {
                        try {
                            profile.stop();
                        } catch (Exception ex) {
                            LOGGER.log(Level.INFO, "Cannot stop profiling", ex);    //NOI18N
                        }
                    }
                }

                if (retry[0] > 0) {
                    try {
                        Thread.sleep(retry[0]);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    return;
                }
            } // for
        }

        public void cancel() {
            if ( panel.time != -1 ) {
                LOGGER.log( Level.FINE, "Worker for text {0} canceled after {1} ms.",   //NOI18N
                        new Object[]{
                            text,
                            System.currentTimeMillis() - createTime
                        });
            }
            TypeProvider _provider;
            synchronized (this) {
                isCanceled = true;
                _provider = current;
            }
            if (_provider != null) {
                _provider.cancel();
            }
        }

        @SuppressWarnings("unchecked")
        private List<? extends TypeDescriptor> getTypeNames(final String text, int[] retry) {
            // TODO: Search twice, first for current project, then for all projects
            final Set<TypeDescriptor> items = new HashSet<>();
            final String[] message = new String[1];
            final TypeProvider.Context context = TypeProviderAccessor.DEFAULT.createContext(null, text, nameKind);
            assert rp.isRequestProcessorThread();
            if (typeProviders == null) {
                typeProviders = implicitTypeProviders != null ? implicitTypeProviders : Lookup.getDefault().lookupAll(TypeProvider.class);
            }
            for (TypeProvider provider : typeProviders) {
                if (isCanceled) {
                    return null;
                }
                current = provider;
                long start = System.currentTimeMillis();
                try {
                    LOGGER.log(Level.FINE, "Calling TypeProvider: {0}", provider);  //NOI18N
                    final TypeProvider.Result result = TypeProviderAccessor.DEFAULT.createResult(items, message, context);
                    provider.computeTypeNames(context, result);
                    retry[0] = mergeRetryTimeOut(retry[0], TypeProviderAccessor.DEFAULT.getRetry(result));
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Provider ''" + provider.getDisplayName() + "'' yields an exception", ex);
                } finally {
                    current = null;
                }
                long delta = System.currentTimeMillis() - start;
                LOGGER.log(Level.FINE, "Provider ''{0}'' took {1} ms.",     //NOI18N
                        new Object[]{
                            provider.getDisplayName(),
                            delta
                        });
            }
            if ( !isCanceled ) {
                final ArrayList<TypeDescriptor> result = new ArrayList<>(items);
                panel.setWarning(message[0]);
                return result;
            } else {
                return null;
            }
        }

        private int mergeRetryTimeOut(
            int t1,
            int t2) {
            if (t1 == 0) {
                return t2;
            }
            if (t2 == 0) {
                return t1;
            }
            return Math.min(t1,t2);
        }
    }

    final void waitSearchFinished() {
        assert SwingUtilities.isEventDispatchThread();
        task.waitFinished();
    }

    private class DialogButtonListener implements ActionListener {

        private GoToPanel panel;

        public DialogButtonListener( GoToPanel panel ) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if ( e.getSource() == okButton) {
                panel.setSelectedTypes();
            }
        }

    }


    private Profile initializeProfiling() {
        boolean assertsOn = false;
        assert assertsOn = true;
        if (!assertsOn) {
            return null;
        }

        Sampler profiler = Sampler.createSampler("jumpto"); //NOI18N
        if (profiler == null) {
            return null;
        }
        return new Profile(profiler).start();
    }

    private class Profile implements Runnable {
        private final long time;
        private volatile  Sampler profiler;
        private volatile boolean profiling;

        public Profile(Sampler profiler) {
            time = System.currentTimeMillis();
            this.profiler = profiler;
        }

        Profile start() {
            PROFILE_RP.post(this, 3000); // 3s
            return this;
        }

        @Override
        public synchronized void run() {
            if (profiler != null) {
                profiling = true;
                profiler.start();
            }
        }

        private synchronized void stop() throws Exception {
            long delta = System.currentTimeMillis() - time;

            Sampler ss = profiler;
            profiler = null;
            if (!profiling) {
                return;
            }
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                ss.stopAndWriteTo(dos);
                dos.close();
                if (dos.size() > 0) {
                    Object[] params = new Object[]{out.toByteArray(), delta, "GoToType" };      //NOI18N
                    Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params); //NOI18N
                } else {
                    LOGGER.log(Level.WARNING, "no snapshot taken"); // NOI18N
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class FilterAdaptor implements Models.Filter<TypeDescriptor> {
        private final TypeBrowser.Filter filter;

        FilterAdaptor(@NonNull final TypeBrowser.Filter filter) {
            Parameters.notNull("filter", filter);   //NOI18N
            this.filter = filter;
        }

        @Override
        public boolean accept(TypeDescriptor item) {
            return filter.accept(item);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void remmoveChangeListener(ChangeListener listener) {
        }
    }
}
