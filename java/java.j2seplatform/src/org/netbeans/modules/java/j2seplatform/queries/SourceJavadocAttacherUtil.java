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
package org.netbeans.modules.java.j2seplatform.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.SourceJavadocAttacher.AttachmentListener;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.java.j2seplatform.api.J2SEPlatformCreator;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public final class SourceJavadocAttacherUtil {

    private SourceJavadocAttacherUtil() {}

    public static void callListener(
        @NonNull final AttachmentListener listener,
        final boolean success) {
        if (success) {
            listener.attachmentSucceeded();
        } else {
            listener.attachmentFailed();
        }
    }
    
    private static Collection<SourceJavadocAttacherImplementation.Definer> filterPlugins(URL root, Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<SourceJavadocAttacherImplementation.Definer> result = new ArrayList<>();
        List<SourceJavadocAttacherImplementation.Definer> filtered = new ArrayList<>();
        for (SourceJavadocAttacherImplementation.Definer d : plugins) {
            if (d instanceof SourceJavadocAttacherImplementation.Definer2) {
                SourceJavadocAttacherImplementation.Definer2 d2 = (SourceJavadocAttacherImplementation.Definer2)d;
                if (d2.accepts(root)) {
                    filtered.add(d);
                }
            } else {
                filtered.add(d);
            }
        }
        return filtered;
    }
    
    private static final RequestProcessor RP = new RequestProcessor(SelectRootsPanel.class);

    /**
     * Downloads / obtains sources using Definer. Should run in a separate thread (use {@link #runAsync}).
     */
    static class Downloader extends AtomicBoolean implements Callable<Boolean>, Runnable, Cancellable {
        private final Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins;
        private final URL root;
        private final boolean source;
        private volatile List<? extends URI> result = Collections.emptyList();
        
        public Downloader(Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins, URL root, boolean source) {
            this.plugins = plugins;
            this.root = root;
            this.source = source;
        }
        
        @Override
        public Boolean call() {
            return get();
        }

        @Override
        public boolean cancel() {
            return compareAndSet(false, true);
        }
        
        String getTitle() {
            return source ? Bundle.TITLE_ObtainingSource() : Bundle.TITLE_ObtainingJavadoc();
        }

        public List<? extends URI> getResult() {
            return result;
        }
        
        public void runAsync(Consumer<List<? extends URI>> callback) {
            Task t = RP.post(this);
            t.addTaskListener((x) -> {
                callback.accept(result);
            });
        }

        @Override
        public void run() {
            for (SourceJavadocAttacherImplementation.Definer d : plugins) {
                if (get()) {
                    // cancelled.
                    return;
                }
                List<? extends URL> s = source ? d.getSources(root, this) : d.getJavadoc(root, this);
                if (s != null || s.isEmpty()) {
                    List<URI> r = new ArrayList<>();
                    for (URL u : s) {
                        try {
                            r.add(u.toURI());
                        } catch (URISyntaxException ex) {
                            // ignore
                        }
                    }
                    if (!r.isEmpty()) {
                        result = r;
                        return;
                    }
                }
            }
        }
    }
    
    @NbBundle.Messages({
        "TITLE_ObtainingSource=Obtaining source",
        "TITLE_ObtainingJavadoc=Obtaining javadoc",
    })
    private static List<? extends URI> waitAttachUsingPlugins(
            @NonNull final URL root,
            final boolean source,
            @NullAllowed final Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            return null;
        }
        Downloader h = new Downloader(plugins, root, source);
        
        // waits for the Downloader to finish.
        BaseProgressUtils.runOffEventDispatchThread(h, 
                source ? Bundle.TITLE_ObtainingSource() : Bundle.TITLE_ObtainingJavadoc(), 
                h, false);
        List<? extends URI> r = h.getResult();
        return r != null && !r.isEmpty() ? r : null;
    }            
    
    @CheckForNull
    @NbBundle.Messages({
        "TXT_SelectJavadoc=Select Javadoc",
        "TXT_InvalidJavadocRoot=Invalid Javadoc root"
    })
    public static List<? extends URI> selectJavadoc(
            @NonNull final URL root,
            @NonNull final List<? extends URI> attachedRoots,
            @NonNull final Callable<List<? extends String>> browseCall,
            @NonNull final Function<String,Collection<? extends URI>> convertor,
            @NullAllowed final Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins) {
        assert root != null;
        assert browseCall != null;
        assert convertor != null;
        String action = NbBundle.getMessage(J2SEPlatformCreator.class, "API_Ask_attachJavadocQuestion");
        if ("yes".equalsIgnoreCase(action)) { // NOI18N
            return waitAttachUsingPlugins(root, false, filterPlugins(root, plugins));
        } else if ("ask".equalsIgnoreCase(action)) { // NOI18N
            final SelectRootsPanel selectJavadoc = new SelectRootsPanel(
                    SelectRootsPanel.JAVADOC,
                    root,
                    attachedRoots,
                    browseCall,
                    convertor,
                    filterPlugins(root, plugins));
            final DialogDescriptor dd = new DialogDescriptor(selectJavadoc, Bundle.TXT_SelectJavadoc());
            dd.setButtonListener(selectJavadoc);
            if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                try {
                    return selectJavadoc.getRoots();
                } catch (Exception e) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                Bundle.TXT_InvalidJavadocRoot(),
                                NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }
        return null;
    }

    @CheckForNull
    @NbBundle.Messages({
        "TXT_SelectSources=Select Sources",
        "TXT_InvalidSourceRoot=Invalid Source root"
    })
    public static List<? extends URI> selectSources(
            @NonNull final URL root,
            @NonNull final List<? extends URI> attachedRoots,
            @NonNull final Callable<List<? extends String>> browseCall,
            @NonNull final Function<String,Collection<? extends URI>> convertor,
            Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins) {
        assert root != null;
        assert browseCall != null;
        assert convertor != null;
        String action = NbBundle.getMessage(J2SEPlatformCreator.class, "API_Ask_attachSourcesQuestion");
        if ("yes".equalsIgnoreCase(action)) { // NOI18N
            return waitAttachUsingPlugins(root, true, filterPlugins(root, plugins));
        } else if ("ask".equalsIgnoreCase(action)) { // NOI18N
            final SelectRootsPanel selectSources = new SelectRootsPanel(
                    SelectRootsPanel.SOURCES,
                    root,
                    attachedRoots,
                    browseCall,
                    convertor,
                    filterPlugins(root, plugins));
            final DialogDescriptor dd = new DialogDescriptor(selectSources, Bundle.TXT_SelectSources());
            dd.setButtonListener(selectSources);
            if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                try {
                    return selectSources.getRoots();
                } catch (Exception e) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                Bundle.TXT_InvalidSourceRoot(),
                                NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }
        return null;
    }

    @NonNull
    @NbBundle.Messages({
        "TXT_Select=Add ZIP/Folder",
        "MNE_Select=A"
    })
    public static Callable<List<? extends String>> createDefaultBrowseCall(
            @NonNull final String title,
            @NonNull final String filterDescription,
            @NonNull final File[] currentFolder) {
        assert title != null;
        assert filterDescription != null;
        assert currentFolder != null;
        final Callable<List<? extends String>> call = new Callable<List<? extends String>>() {
            @Override
            public List<? extends String> call() throws Exception {
                final JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled (true);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (Utilities.isMac()) {
                    //New JDKs and JREs are bundled into package, allow JFileChooser to navigate in
                    chooser.putClientProperty("JFileChooser.packageIsTraversable", "always");   //NOI18N
                }
                chooser.setDialogTitle(title);
                final FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        try {
                            return f.isDirectory() ||
                                FileUtil.isArchiveFile(Utilities.toURI(f).toURL());
                        } catch (MalformedURLException ex) {
                            return false;
                        }
                    }

                    @Override
                    public String getDescription() {
                        return filterDescription;
                    }
                };
                chooser.setFileFilter (filter);
                chooser.setApproveButtonText(Bundle.TXT_Select());
                chooser.setApproveButtonMnemonic(Bundle.MNE_Select().charAt(0));
                if (currentFolder[0] != null) {
                    chooser.setCurrentDirectory(currentFolder[0]);
                }
                if (chooser.showOpenDialog(Utilities.findDialogParent()) == JFileChooser.APPROVE_OPTION) {
                    currentFolder[0] = chooser.getCurrentDirectory();
                    final File[] files = filter(chooser.getSelectedFiles(), filter);
                    final List<String> result = new ArrayList<String>(files.length);
                    for (File f : files) {
                        result.add(f.getAbsolutePath());
                    }
                    return result;
                }
                return null;
            }
        };
        return call;
    }

    public static Function<String,Collection<? extends URI>> createDefaultURIConvertor(final boolean forSources) {
        return new Function<String, Collection<? extends URI>>() {
            @Override
            public Collection<? extends URI> call(String param) throws Exception {
                final File f = new File (param);
                assert f.isAbsolute() : param;
                FileObject fo = FileUtil.toFileObject(f);
                if (fo.isData()) {
                    fo = FileUtil.getArchiveRoot(fo);
                }
                final List<URI> result = new ArrayList<URI>();
                if (forSources) {
                    for (FileObject root : JavadocAndSourceRootDetection.findSourceRoots(fo,null)) {
                        result.add(root.toURL().toURI());
                    }
                } else {
                    FileObject root = JavadocAndSourceRootDetection.findJavadocRoot(fo);
                    if (root != null) {
                        result.add(root.toURL().toURI());
                    }
                }
                return result;
            }
        };
    }

    public static interface Function<P,R> {
        R call (P param) throws Exception;
    }

    private static File[] filter(
        @NonNull final File[] files,
        @NonNull final FileFilter filter) {
        final List<File> result = new ArrayList<>();
        for (File file : files) {
            if (filter.accept(file)) {
                result.add(file);
            }
        }
        return result.toArray(new File[0]);
    }
}
