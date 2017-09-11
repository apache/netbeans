/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
