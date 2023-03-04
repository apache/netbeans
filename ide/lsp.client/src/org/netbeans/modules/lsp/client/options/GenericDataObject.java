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
package org.netbeans.modules.lsp.client.options;

import java.awt.Image;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jlahoda
 */
public class GenericDataObject extends MultiDataObject {

    private static final Set<Reference<GenericDataObject>> REGISTRY = new HashSet<>();
    private static final Map<String, Image> mimeType2Icon = new HashMap<>();

    private final String mimeType;

    public GenericDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        this.mimeType = FileUtil.getMIMEType(pf);
        registerEditor(mimeType, false);
        synchronized (REGISTRY) {
            REGISTRY.add(new WeakReference<>(this));
        }
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    protected Node createNodeDelegate() {
        DataNode node = new DataNode(this, Children.LEAF, getLookup()) {
            @Override
            public Image getIcon(int type) {
                synchronized (mimeType2Icon) {
                    return mimeType2Icon.computeIfAbsent(mimeType, mt -> {
                        FileObject iconFile = FileUtil.getConfigFile("Loaders/" + mimeType + "/Factories/icon.png");
                        return Utils.loadIcon(iconFile, type);
                    });
                }
            }
            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }
        };
//        node.setI
//        if (icon != null) {
//            node.setIconBaseWithExtension(icon);
//        }
        return node;
    }

    public static void invalidate() {
        HashSet<Reference<GenericDataObject>> regCopy;

        synchronized (REGISTRY) {
            regCopy = new HashSet<>(REGISTRY);
        }

        //TODO: synchronization, only invalidate DOs whose mime type were removed:
        for (Reference<GenericDataObject> r : regCopy) {
            GenericDataObject god = r.get();
            if (god != null) {
                try {
                    god.setValid(false);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        synchronized (mimeType2Icon) {
            mimeType2Icon.clear();
        }
    }

    public static Factory factory() {
        return (fo, recognized) -> {
            MultiFileLoader defaultLoader;

            try {
                Method getDefaultFileLoader = DataLoaderPool.class.getDeclaredMethod("getDefaultFileLoader");
                getDefaultFileLoader.setAccessible(true);
                defaultLoader = (MultiFileLoader) getDefaultFileLoader.invoke(null);
            } catch (ReflectiveOperationException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }

            try {
                return new GenericDataObject(fo, defaultLoader);
            } catch (DataObjectExistsException ex) {
                try {
                    if (ex.getDataObject() instanceof GenericDataObject) {
                        throw ex;
                    }
                    ex.getDataObject().setValid(false);
                } catch (PropertyVetoException ex1) {
                    Exceptions.printStackTrace(ex1);
                    return null;
                }
                return new GenericDataObject(fo, defaultLoader);
            }
        };
    }
}

