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

package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.ide.ergonomics.Utilities;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.Lookup;

/** Support for special dataobjects that can dynamically FoD objects.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FodDataObjectFactory implements DataObject.Factory {
    private static MultiFileLoader delegate;
    private static final Set<FileObject> ignore = Collections.newSetFromMap(new WeakHashMap<>());

    private final FileObject definition;
    private final FeatureInfo info;
    
    private FodDataObjectFactory(FileObject fo) {
        this.definition = fo;
        this.info = FoDLayersProvider.getInstance().whichProvides(definition);
    }


    public static DataObject.Factory create(FileObject fo) {
        return new FodDataObjectFactory(fo);
    }

    @Override
    public DataObject findDataObject(FileObject fo, Set<? super FileObject> recognized) throws IOException {
        if (fo.isFolder()) {
            return null;
        }
        if (info != null && info.isEnabled()) {
            return null;
        }
        if (fo.getMIMEType().endsWith("+xml")) {
            OpenAdvancedAction.registerCandidate(fo);
            return null;
        }
        if (ignore.contains(fo)) {
            return null;
        }
        if (delegate == null) {
            Enumeration<DataLoader> en = DataLoaderPool.getDefault().allLoaders();
            while (en.hasMoreElements()) {
                DataLoader d = en.nextElement();
                if (d instanceof MultiFileLoader) {
                    delegate = (MultiFileLoader)d;
                }
            }
            assert delegate instanceof MultiFileLoader;
        }
        return new Cookies(fo, delegate);
    }

    private final class Cookies extends MultiDataObject
    implements OpenCookie, EditCookie, LineCookie, ChangeListener {
        private final FileObject fo;
        private final ChangeListener weakL;
        private Boolean open;
        
        private Cookies(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
            this.fo = fo;
            this.weakL = WeakListeners.change(this, FeatureManager.getInstance());
            FeatureManager.getInstance().addChangeListener(weakL);
        }

        @Override
        protected Node createNodeDelegate() {
            DataNode dn = new DataNode(this, Children.LEAF);
            dn.setIconBaseWithExtension("org/netbeans/modules/ide/ergonomics/fod/file.png");
            return dn;
        }

        @Override
        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }

        @Override
        public void open() {
            delegate(true);
        }

        @Override
        public void edit() {
            delegate(false);
        }

        @Override
        public Line.Set getLineSet() {
            delegate(null);
            DataObject obj;
            try {
                obj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
            if (this == obj) {
                return null;
            }
            LineCookie lc = obj.getLookup().lookup(LineCookie.class);
            return lc.getLineSet();
        }
        
        private void delegate(Boolean open) {
            FeatureInfo info = FoDLayersProvider.getInstance().whichProvides(definition);
            String msg = NbBundle.getMessage(FodDataObjectFactory.class, "MSG_Opening_File", fo.getNameExt());

            FoDLayersProvider.LOG.log(Level.FINER, "Opening file {0}", this);
            this.open = open;
            boolean success = Utilities.featureDialog(info, msg, msg);
            if (success) {
                finishOpen();
            }
        }


        private void finishOpen() {
            ignore.add(getPrimaryFile());
            try {
                DataObject obj = DataObject.find(fo);
                FoDLayersProvider.LOG.log(Level.FINER, "finishOpen {0}", obj);
                if (obj == this) {
                    obj.setValid(false);
                    obj = DataObject.find(fo);
                }
                Boolean doOpen = open;
                if (doOpen == null) {
                    return;
                }
                Class<?> what = doOpen ? OpenCookie.class : EditCookie.class;
                Object oc = obj.getLookup().lookup(what);
                if (oc == this) {
                    obj.setValid(false);
                    obj = DataObject.find(fo);
                    oc = obj.getLookup().lookup(what);
                }
                if (oc instanceof OpenCookie) {
                    ((OpenCookie)oc).open();
                }
                if (oc instanceof EditCookie) {
                    ((EditCookie)oc).edit();
                }
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            FeatureInfo info = FoDLayersProvider.getInstance().whichProvides(definition);
            FoDLayersProvider.LOG.log(Level.FINER, "Refresh state of {0}", this);
            ignore.add(getPrimaryFile());
            if (info == null || info.isEnabled()) {
                dispose();
            }
        }
    } // end Cookies
}
