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
package org.netbeans.modules.java.lsp.server.explorer;

import java.awt.Graphics2D;
import org.netbeans.modules.java.lsp.server.explorer.api.ExplorerManagerFactory;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import static java.awt.image.ImageObserver.ABORT;
import static java.awt.image.ImageObserver.ALLBITS;
import static java.awt.image.ImageObserver.ERROR;
import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.modules.java.lsp.server.URITranslator;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.netbeans.modules.java.lsp.server.explorer.api.ResourceData;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author sdedic
 */
public class TreeNodeRegistryImpl implements TreeNodeRegistry {
    private static final Logger LOG = Logger.getLogger(TreeNodeRegistryImpl.class.getName());

    private static final String ENCODING_BASE64 = "base64"; // NOI18N
    private static final String PROTO_LSPCACHE = "lspcache"; // NOI18N
    private static final String CACHE_ICONS_NAME = "icons"; // NOI18N
    private static final String EXT_PNG = ".png"; // NOI18N
    private static final String MIME_PNG = "image/png"; // NOI18N
    private static final String ICONS_PREFIX = "//icons/"; // NOI18N
    private static final String URLPREFIX_LSPCACHE = PROTO_LSPCACHE + ":" + ICONS_PREFIX; // NOI18N

    private final Lookup sessionLookup;
    private final Map<String, TreeViewProvider> providers = new HashMap<>();
    private final Map<Integer, TreeViewProvider> node2Provider = new HashMap<>();
    private final Map<Image, ImageDataOrIndex> images = new WeakHashMap<>();
    private final Map<URI, ImageDataOrIndex> imageData = new WeakHashMap<>();
    
    private int nodeCounter = 1;
    
    private NbCodeLanguageClient langClient;

    public TreeNodeRegistryImpl(Lookup sessionLookup) {
        this.sessionLookup = sessionLookup;
    }
    
    public void unregisterNode(int nodeId, Node n) {
        LOG.log(Level.FINEST, "Discarding node #{0}", nodeId);
        synchronized (this) {
            node2Provider.remove(nodeId);
        }
    }

    @Override
    public synchronized int registerNode(Node n, TreeViewProvider tvp) {
        int id = ++nodeCounter;
        LOG.log(Level.FINEST, "Registered node #{0}, {1}", new Object[] { id, n });
        node2Provider.put(id, tvp);
        return id;
    }
    
    @Override
    public Node findNode(int id) {
        return providerOf(id).findNode(id);
    }

    @Override
    public synchronized TreeViewProvider providerOf(int id) {
        TreeViewProvider p = node2Provider.get(id);
        return p != null ? p : TreeViewProvider.NONE;
    }
    
    public CompletionStage<TreeViewProvider> createProvider(String id) {
        LOG.log(Level.FINER, "Asked for {0}", id);
        synchronized (this) {
            TreeViewProvider p = providers.get(id);
            if (p != null) {
                return CompletableFuture.completedFuture(p);
            }
        }
        
        Lookup ctxLookup = new ProxyLookup(
            Lookups.forPath("Explorers/" + id), // NOI18N
            Lookups.forPath("Explorers/_all") // NOI18N
        );
        
        
        FileObject conf = FileUtil.getConfigFile("Explorers/" + id); // NOI18N
        boolean confirmDelete = conf != null &&conf.getAttribute("explorerConfirmsDelete") == Boolean.TRUE; // NOI18N
        
        for (ExplorerManagerFactory f : ctxLookup.lookupAll(ExplorerManagerFactory.class)) {
            CompletionStage<ExplorerManager> em = f.createManager(id, ctxLookup);
            if (em != null) {
                LOG.log(Level.FINER, "Creating provider from factory {0}", f);
                return em.thenApply(em2 -> registerManager(em2, id, ctxLookup, confirmDelete));
            }
        }
        CompletableFuture<TreeViewProvider> f = new CompletableFuture<>();
        f.completeExceptionally(new IllegalArgumentException("View " + id + " is not supported."));
        return f;
    }
    
    protected void notifyItemChanged(NodeChangedParams itemId) {}
    
    private synchronized TreeViewProvider registerManager(ExplorerManager em, String id, Lookup ctxLookup, boolean confirmDelete) {
        TreeViewProvider p = providers.get(id);
        if (p != null) {
            return p;
        } 
        em.addVetoableChangeListener(e -> {
            if (ExplorerManager.PROP_ROOT_CONTEXT.equals(e.getPropertyName())) {
                throw new PropertyVetoException("Root change not allowed", e);
            }
        });
        
        ActionMap map = new ActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));
        map.put("delete", ExplorerUtils.actionDelete(em, confirmDelete)); // NOI18N
        
        Lookup expLookup = ExplorerUtils.createLookup (em, map);
        
        // delegate the TreeViewProvider notification out:
        final TreeViewProvider tvp = new TreeViewProvider(id, em, this, new ProxyLookup(expLookup, ctxLookup)) {
            @Override
            protected void onDidChangeTreeData(Node n, int id) {
                int rootId = findId(em.getRootContext());
                if (n == null) {
                    notifyItemChanged(new NodeChangedParams(rootId));
                } else {
                    notifyItemChanged(new NodeChangedParams(rootId, id));
                }
            }
        };
        providers.put(id, tvp);
        return tvp;
    }

    @Override
    public ResourceData imageContents(URI uri) {
        String scheme = uri.getScheme();
        
        ImageDataOrIndex d;
        
        synchronized (this) {
            d = imageData.get(uri);
            if (d != null && d.imageData != null) {
                ResourceData rd = new ResourceData();
                rd.setContentSize(d.imageData.length);
                String base64Content = Base64.getEncoder().encodeToString(d.imageData).replace("\\n", ""); // NOI18N
                rd.setContent(base64Content);
                rd.setContentType(d.contentType);
                rd.setEncoding(ENCODING_BASE64);
                return rd;
            }
        }
        
        URL toOpen;
        try {
            // only accept certain schemes, so that the client can't reach entire local FS:
            if ("nbres".equals(scheme)) { // NOI18N
                toOpen = uri.toURL();
            } else if (PROTO_LSPCACHE.equals(scheme) && uri.getSchemeSpecificPart().startsWith(ICONS_PREFIX)) { // NOI18N
                // this branch is reached only iff the server stops after client receives image URI and before the client
                // asks for image contents: otherwise it is cached in ImageDataOrIndex structure.
                String fn = uri.getSchemeSpecificPart().substring(ICONS_PREFIX.length());
                File iconFile = new File(new File(URITranslator.getCacheDir(), "icons"), fn) ; // NOI18N
                toOpen = iconFile.toURI().toURL();
            } else {
                throw new IllegalArgumentException("Resource not found: " + uri);
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        
        URLConnection c = null;
        try {
            c = toOpen.openConnection();
            ResourceData rd = new ResourceData();
            try (InputStream istm = c.getInputStream()) {
                byte[] content = new byte[c.getContentLength()];
                istm.read(content);
                rd.setContentSize(content.length);
                String base64Content = Base64.getEncoder().encodeToString(content).replace("\\n", ""); // NOI18N
                rd.setContent(base64Content);
                rd.setContentType(c.getContentType());
                rd.setEncoding(ENCODING_BASE64);
                
                synchronized (this) {
                    if (d != null) {
                        // cache the image bits in the ImageDataOrIndex instance
                        d.contentType = c.getContentType();
                        d.imageData = content;
                    }
                }
            }
            return rd;
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    @Override
    public ImageDataOrIndex imageOrIndex(Image i) {
        ImageDataOrIndex res;
        synchronized (this) {
            res = images.get(i);
            if (res != null) {
                return res;
            }
        }
        String base64Content;
        URL u = ImageUtilities.findImageBaseURL(i);
        
        try {
            if (u != null) {
                URI u2 = TreeViewProvider.builtinURI2URI(TreeViewProvider.findImageURI(i));
                res = new ImageDataOrIndex(u2);
            } else {
                // write a cache file names using contents hash: in the case of server restart, the client
                // can ask a new server instance and the URI will point to the cached image bits. Overwrite just
                // once per session per image.
                byte[] data = printImagePngData(i);
                // I know it's old, but it's not for security, just as a semi-unique identifier.
                MessageDigest md = MessageDigest.getInstance("SHA1"); // NOI18N
                byte[] dg = md.digest(data);
                base64Content = Base64.getUrlEncoder().encodeToString(dg).replace("\\n", ""); // NOI18N
                String fname = base64Content + EXT_PNG; // NOI18N
                File iconsDir = new File(URITranslator.getCacheDir(), CACHE_ICONS_NAME);
                if (iconsDir.mkdirs() || iconsDir.isDirectory()) {
                    // in case the dir/file creation fails, it's still possible to serve the data bits from the memory.
                    File nf = new File(iconsDir, fname);
                    if (!nf.exists()) {
                        try (OutputStream ostm = Files.newOutputStream(nf.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                            ostm.write(data);
                        } catch (IOException ex) {
                            LOG.log(Level.FINE, "Error creating disk image cache {0}", nf);
                            LOG.log(Level.FINE, "Exception creating file/content:", ex);
                        }
                    }
                }
                res = new ImageDataOrIndex(new URI(URLPREFIX_LSPCACHE + fname)).
                        imageData(ENCODING_BASE64, data);
            }
        } catch (IOException | URISyntaxException | NoSuchAlgorithmException ex) {
            // log the error and cache failure
            LOG.log(Level.WARNING, "Could not load or print image: {0}", i);
            LOG.log(Level.WARNING, "Image content generation failed with exception:", ex);
            res = new ImageDataOrIndex(TreeItemData.NO_URI);
        }
        synchronized (this) {
            images.put(i, res);
            imageData.put(res.baseURI, res);
        }
        return res;
    }
    
    private byte[] printImagePngData(Image i) throws IOException {
        BufferedImage bi;
        if (i instanceof BufferedImage) {
            bi = (BufferedImage)i;
        } else {
            class IO implements ImageObserver {
                int bits;
                CountDownLatch cdl = new CountDownLatch(1);

                int height = -1;
                int width = -1;

                @Override
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    bits |= (HEIGHT | WIDTH) & infoflags;
                    synchronized (this) {
                        if ((infoflags & WIDTH) > 0) {
                            this.height = height;
                        }
                        if ((infoflags & HEIGHT) > 0) {
                            this.width = width;
                        }
                    }
                    if ((infoflags & (ABORT | ERROR)) > 0) {
                        cdl.countDown();
                        return false;
                    }
                    if ((infoflags & ALLBITS) > 0) {
                        cdl.countDown();
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            IO observer = new IO();
            int h = i.getHeight(observer);
            int w = i.getWidth(observer);
            if (h == -1 || w == -1) {
                try {
                    observer.cdl.await();
                } catch (InterruptedException ex) {
                }
                synchronized (observer) {
                    h = observer.height;
                    w = observer.width;
                    if (h == -1 || w == -1) {
                        LOG.log(Level.WARNING, "Could not realize image to get its size: {0}", i);
                        return null;
                    }
                }
            }
            bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = bi.createGraphics();
            bGr.drawImage(i, 0, 0, null);
            bGr.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", baos); // NOI18N
        baos.flush();
        return baos.toByteArray();
    }
}
