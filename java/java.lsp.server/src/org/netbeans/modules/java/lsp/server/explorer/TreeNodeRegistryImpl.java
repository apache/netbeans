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
package org.netbeans.modules.java.lsp.server.explorer;

import java.awt.Graphics2D;
import org.netbeans.modules.java.lsp.server.explorer.api.ExplorerManagerFactory;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author sdedic
 */
public class TreeNodeRegistryImpl implements TreeNodeRegistry {
    private static final Logger LOG = Logger.getLogger(TreeNodeRegistryImpl.class.getName());
    private static final String NODE_ATTR_LSP_ID = "lspId"; // NOI18N
    
    private final Lookup sessionLookup;
    private final Map<String, TreeViewProvider> providers = new HashMap<>();
    private final Map<Integer, TreeViewProvider> node2Provider = new HashMap<>();
    private final Map<Image, ImageDataOrIndex> images = new WeakHashMap<>();
    
    private int nodeCounter = 1;
    private int imageCounter = 1;
    
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
    public ImageDataOrIndex imageOrIndex(Image i) {
        ImageDataOrIndex res;
        synchronized (this) {
            res = images.get(i);
            if (res != null) {
                return res.imageIndex == -1 ? null : new ImageDataOrIndex(res.imageIndex);
            }
        }
        String base64Content;
        URI imageURI = null;
        try {
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
                        Thread.sleep(1000);
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
            base64Content = Base64.getEncoder().encodeToString(baos.toByteArray()).replace("\\n", ""); // NOI18N
            imageURI = new URI("data:image/png;base64," + base64Content); // NOI18N
        } catch (IOException | URISyntaxException ex) {
            // log the error and cache failure
            res = new ImageDataOrIndex(null, -1);
        }
        synchronized (this) {
            if (res == null) {
                res = new ImageDataOrIndex(imageURI, imageCounter++);
            }
            images.put(i, res);
        }
        return res;
    }
}
