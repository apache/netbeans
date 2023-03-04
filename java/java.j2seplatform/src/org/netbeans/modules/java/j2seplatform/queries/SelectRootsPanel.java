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

/*
 * SelectSourcesPanel.java
 *
 * Created on Aug 8, 2011, 6:47:20 PM
 */
package org.netbeans.modules.java.j2seplatform.queries;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.j2seplatform.queries.SourceJavadocAttacherUtil.Function;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
class SelectRootsPanel extends javax.swing.JPanel implements ActionListener {

    static final int SOURCES = 0;
    static final int JAVADOC = 1;

    private static final RequestProcessor RP = new RequestProcessor(SelectRootsPanel.class);

    private final int mode;
    private final URL root;
    private final Callable<List<? extends String>> browseCall;
    private final Function<String,Collection<? extends URI>> convertor;
    private final Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins;
    private volatile DownloadService downloadService;

    /** Creates new form SelectSourcesPanel */
    SelectRootsPanel (
            final int mode,
            @NonNull final URL root,
            @NonNull final List<? extends URI> attachedRoots,
            @NonNull final Callable<List<? extends String>> browseCall,
            @NonNull final Function<String, Collection<? extends URI>> convertor,
            @NullAllowed final Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins) {
        assert (mode & ~1) == 0;
        assert root != null;
        assert browseCall != null;
        assert convertor != null;
        this.mode = mode;
        this.root = root;
        this.browseCall = browseCall;
        this.convertor = convertor;
        this.plugins = plugins;
        initComponents();
        final DefaultListModel<URI> model = new DefaultListModel<URI>();
        sources.setModel(model);
        sources.setCellRenderer(new RootRenderer());
        sources.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                enableSelectionSensitiveActions();
            }
        });
        for (URI r : attachedRoots)  {
            model.addElement(r);
        }
        addURL.setVisible(mode != 0);
        if (plugins != null && !plugins.isEmpty()) {
            download.setVisible(true);
            if (plugins.size() == 1) {
                download.setToolTipText(plugins.iterator().next().getDescription());
            }
        } else {
            download.setVisible(false);
        }
        enableSelectionSensitiveActions();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final DownloadService cs = downloadService;
        if (cs != null) {
            cs.cancel();
        }
    }

    @CheckForNull
    List<? extends URI> getRoots() throws Exception {
        final DefaultListModel<URI> lm = (DefaultListModel<URI>) sources.getModel();
        return Collections.unmodifiableList(Collections.list(lm.elements()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        attachTo = new javax.swing.JLabel();
        lblSources = new javax.swing.JLabel();
        add = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sources = new javax.swing.JList<URI>();
        remove = new javax.swing.JButton();
        up = new javax.swing.JButton();
        down = new javax.swing.JButton();
        addURL = new javax.swing.JButton();
        download = new javax.swing.JButton();

        attachTo.setText(getDescription());

        lblSources.setLabelFor(sources);
        org.openide.awt.Mnemonics.setLocalizedText(lblSources, getLabel());

        org.openide.awt.Mnemonics.setLocalizedText(add, org.openide.util.NbBundle.getMessage(SelectRootsPanel.class, "TXT_Browse")); // NOI18N
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browse(evt);
            }
        });

        sources.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(sources);

        org.openide.awt.Mnemonics.setLocalizedText(remove, org.openide.util.NbBundle.getMessage(SelectRootsPanel.class, "TXT_Remove")); // NOI18N
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remove(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(up, org.openide.util.NbBundle.getMessage(SelectRootsPanel.class, "TXT_MoveUp")); // NOI18N
        up.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUp(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(down, org.openide.util.NbBundle.getMessage(SelectRootsPanel.class, "TXT_MoveDown")); // NOI18N
        down.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDown(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addURL, org.openide.util.NbBundle.getMessage(SelectRootsPanel.class, "TXT_AddURL")); // NOI18N
        addURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addURL(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(download, org.openide.util.NbBundle.getMessage(SelectRootsPanel.class, "TXT_Download")); // NOI18N
        download.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                download(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attachTo)
                            .addComponent(lblSources))
                        .addGap(0, 131, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(remove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(add, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(down, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(up, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addURL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(download, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attachTo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSources)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(add)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addURL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(download)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(remove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(up)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(down)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                        .addGap(20, 20, 20))))
        );
    }// </editor-fold>//GEN-END:initComponents

private void browse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browse
    try {
        final Collection<Integer> added = new ArrayList<>();
        final List<? extends String> paths = browseCall.call();
        if (paths != null) {
            final DefaultListModel<URI> lm = (DefaultListModel<URI>) sources.getModel();
            final Set<URI> contained = new HashSet<>(Collections.list(lm.elements()));
            int index = sources.getSelectedIndex();
            index = index < 0 ? lm.getSize() : index + 1;
            for (String path : paths) {
                for (URI uri : convertor.call(path)) {
                    if (!contained.contains(uri)) {
                        lm.add(index, uri);
                        added.add(index);
                        index++;
                    }
                }
            }
        }
        select(added);
    } catch (Exception ex) {
        Exceptions.printStackTrace(ex);
    }
}//GEN-LAST:event_browse

    private void remove(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remove
        final DefaultListModel<URI> lm = (DefaultListModel<URI>) sources.getModel();
        final int[] index = sources.getSelectedIndices();
        for (int i=index.length-1; i>=0; i--) {
            lm.remove(index[i]);
        }
    }//GEN-LAST:event_remove

    private void moveUp(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUp
        final DefaultListModel<URI> lm = (DefaultListModel<URI>) sources.getModel();
        final int[] index = sources.getSelectedIndices();
        for (int i=0; i< index.length; i++) {
            final URI toMove = lm.remove(index[i]);
            lm.add(index[i]-1, toMove);
            index[i]--;
        }
        sources.setSelectedIndices(index);
    }//GEN-LAST:event_moveUp

    private void moveDown(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDown
        final DefaultListModel<URI> lm = (DefaultListModel<URI>) sources.getModel();
        final int[] index = sources.getSelectedIndices();
        for (int i=index.length-1; i>=0; i--) {
            final URI toMove = lm.remove(index[i]);
            lm.add(index[i]+1, toMove);
            index[i]++;
        }
        sources.setSelectedIndices(index);
    }//GEN-LAST:event_moveDown

    private void addURL(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addURL
        final NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
            NbBundle.getMessage(SelectRootsPanel.class,"TXT_RemoteJavadoc"),
            NbBundle.getMessage(SelectRootsPanel.class,"TXT_RemoteJavadoc_Title"),
            NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.PLAIN_MESSAGE);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            final String inputText = nd.getInputText();
            final DefaultListModel<URI> lm = (DefaultListModel<URI>) sources.getModel();
            final Set<URI> contained = new HashSet<>(Collections.list(lm.elements()));
            int index = sources.getSelectedIndex();
            index = index < 0 ? lm.getSize() : index + 1;
            try {
                URI uri = new URI(inputText);
                if (!contained.contains(uri)) {
                    lm.add(index, uri);
                    select(Collections.<Integer>singleton(index));
                    index++;
                }
            } catch (URISyntaxException ex) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(SelectRootsPanel.class, "TXT_InvalidRoot", inputText),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }//GEN-LAST:event_addURL

    private void download(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_download
        assert plugins != null;
        enableDownloadAction(false);
        DownloadService dn = new DownloadService(plugins, root, mode == 0);
        downloadService = dn;
        
        dn.runAsync((resFin) -> {
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    boolean finished = false;
                    try {
                        finished = !dn.call();
                    } catch (Exception e) {
                        //pass - finished is false
                    }
                    try {
                        if (finished) {
                            if (resFin.isEmpty()) {
                                DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(
                                        NbBundle.getMessage(
                                            SelectRootsPanel.class,
                                            mode == 0 ?
                                                "ERR_DownloadSourcesFailed" :
                                                "ERR_DownloadJavadocFailed",
                                            getDisplayName(root)),
                                        NotifyDescriptor.INFORMATION_MESSAGE));
                            } else {
                                final DefaultListModel<URI> lm = (DefaultListModel<URI>) sources.getModel();
                                final Set<URI> contained = new HashSet<>(Collections.list(lm.elements()));
                                int index = sources.getSelectedIndex();
                                index = index < 0 ? lm.getSize() : index + 1;
                                final List<Integer> added = new ArrayList<>();
                                for (URI uri : resFin) {
                                    if (!contained.contains(uri)) {
                                        lm.add(index, uri);
                                        added.add(index);
                                        index++;
                                    }
                                }
                                select(added);
                            }
                        }
                    } finally {
                        downloadService = null;
                        enableDownloadAction(true);
                        enableSelectionSensitiveActions();
                    }
                }
            });
        });
    }//GEN-LAST:event_download

    private void enableSelectionSensitiveActions() {
        final int[] indices = sources.getSelectedIndices();
        remove.setEnabled(indices.length > 0);
        up.setEnabled(indices.length > 0 && indices[0] != 0);
        down.setEnabled(indices.length > 0 && indices[indices.length-1] != sources.getModel().getSize()-1);
    }

    private void enableDownloadAction(final boolean enable) {
        download.setEnabled(enable);        
    }

    private void select(@NonNull final Collection<? extends Integer> toSelect) {
        final int[] indexes = new int[toSelect.size()];
        final Iterator<? extends Integer> it = toSelect.iterator();
        for (int i=0; it.hasNext(); i++) {
            indexes[i] = it.next();
        }
        sources.setSelectedIndices(indexes);
    }

    @NonNull
    private String getDescription() {
        final String displayName = getDisplayName(root);
        switch (mode) {
            case 0:
                return NbBundle.getMessage(SelectRootsPanel.class, "TXT_AttachSourcesTo",displayName);
            case 1:
                return NbBundle.getMessage(SelectRootsPanel.class, "TXT_AttachJavadocTo",displayName);
            default:
                throw new IllegalStateException(Integer.toString(mode));
        }
    }

    @NonNull
    private static String getDisplayName(@NonNull final URL root) {
        final File f = FileUtil.archiveOrDirForURL(root);
        return f == null ?
            root.toString() :
            f.isFile() ?
                f.getName() :
                f.getAbsolutePath();
    }

    @NonNull
    private String getLabel() {
        switch (mode) {
            case 0:
                return NbBundle.getMessage(SelectRootsPanel.class, "TXT_LocalSources");
            case 1:
                return NbBundle.getMessage(SelectRootsPanel.class, "TXT_LocalJavadoc");
            default:
                throw new IllegalStateException(Integer.toString(mode));
        }
    }

    private static class RootRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                final JList<?> list,
                Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            if (value instanceof URI) {
                final URI uri = (URI) value;
                if (uri.isAbsolute()) {
                    try {
                        URL url = uri.toURL();
                        String offset = null;
                        if ("jar".equals(url.getProtocol())) {  //NOI18N
                            final String surl = url.toExternalForm();
                            int offsetPos = surl.lastIndexOf("!/"); //NOI18N
                            if (offsetPos > 0 && offsetPos < surl.length()-3) {
                                offset = surl.substring(offsetPos+2);
                            }
                            url = FileUtil.getArchiveFile(url);
                        }
                        if ("file".equals(url.getProtocol())) { //NOI18N
                            final File file = Utilities.toFile(url.toURI());
                            value = offset == null ?
                                file.getAbsolutePath() :
                                NbBundle.getMessage(
                                    SelectRootsPanel.class,
                                    "PATTERN_RELPATH_IN_FILE",
                                    offset,
                                    file.getAbsolutePath());
                        }
                    } catch (MalformedURLException | URISyntaxException ex) {
                        //pass - value unchanged
                    }
                }
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class DownloadService extends SourceJavadocAttacherUtil.Downloader {

        public DownloadService(Collection<? extends SourceJavadocAttacherImplementation.Definer> plugins, URL root, boolean source) {
            super(plugins, root, source);
        }
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JButton addURL;
    private javax.swing.JLabel attachTo;
    private javax.swing.JButton down;
    private javax.swing.JButton download;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblSources;
    private javax.swing.JButton remove;
    private javax.swing.JList<URI> sources;
    private javax.swing.JButton up;
    // End of variables declaration//GEN-END:variables
}
