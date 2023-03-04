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
package org.openide.loaders;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUIUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/** Default DataObject.Factory implementation.
 * 
 * @author Jaroslav Tulach
 * @param <T> type of DataObject to create
 */
class MimeFactory<T extends DataObject> implements DataObject.Factory {
    final Class<? extends T> clazz;
    final Constructor<? extends T> factory;
    final String mimeType;
    Image img;
    final FileObject fo;

    public MimeFactory(Class<? extends T> clazz, String mimeType, Image img, FileObject fo) {
        super();
        this.clazz = clazz;
        this.mimeType = mimeType;
        this.img = img;
        try {
            this.factory = clazz.getConstructor(FileObject.class, MultiFileLoader.class);
            this.factory.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
        }
        this.fo = fo;
    }
    
    public static MimeFactory<DataObject> layer(FileObject fo) throws ClassNotFoundException {
        String className = (String) fo.getAttribute("dataObjectClass"); // NOI18N
        if (className == null) {
            throw new IllegalStateException("No attribute dataObjectClass for " + fo);
        }
        String mimeType = (String)fo.getAttribute("mimeType"); // NOI18N
        
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = MimeFactory.class.getClassLoader();
        }
        Class<? extends DataObject> clazz = l.loadClass(className).asSubclass(DataObject.class);
        return new MimeFactory<DataObject>(clazz, mimeType, null, fo);
    }

    public DataObject findDataObject(FileObject fo, Set<? super FileObject> recognized) throws IOException {
        DataObject obj = null;
        Exception e = null;
        try {
            obj = factory.newInstance(fo, DataLoaderPool.getDefaultFileLoader());
        } catch (InstantiationException ex) {
            e = ex;
        } catch (IllegalAccessException ex) {
            e = ex;
        } catch (IllegalArgumentException ex) {
            e = ex;
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof IOException) {
                throw (IOException)ex.getTargetException();
            }
            e = ex;
        }
        if (obj == null) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
        if (obj instanceof MultiDataObject) {
            MultiDataObject mdo = (MultiDataObject) obj;
            mdo.getCookieSet().assign(DataObject.Factory.class, this);
        }
        return obj;
    }
    
    final Image getImage(int type) {
        if (img == null && fo != null) {
            img = ImageUtilities.loadImage("org/openide/loaders/empty.gif", true); // NOI18N
            try {
                img = FileUIUtils.getImageDecorator(fo.getFileSystem()).annotateIcon(img, type, Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return img;
    }
    
    final Action[] getActions() {
        FileObject actions = FileUtil.getConfigFile(
            "Loaders/" + mimeType + "/Actions"
        );
        if (actions != null) {
            DataFolder folder = DataFolder.findFolder(actions);
            try {
                return (Action[]) new DataLdrActions(folder, null).instanceCreate();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return DataLoaderPool.getDefaultFileLoader().getSwingActions();
    }
}
