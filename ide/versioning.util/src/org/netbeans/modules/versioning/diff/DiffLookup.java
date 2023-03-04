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

package org.netbeans.modules.versioning.diff;

import org.netbeans.modules.versioning.util.SimpleLookup;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;

/**
 * Lookup to be used for {@code DiffTopComponent}s.
 * When the lookup is passed new data
 *
 * @author Marian Petras
 * @since 1.9.1
 */
public class DiffLookup extends SimpleLookup implements PropertyChangeListener {

    private EditorCookie.Observable observableEditorCookie;
    private Object[] withoutSaveCookie;
    private Object[] withSaveCookie;
    private PropertyChangeListener weakList;

    public DiffLookup() {
        super();
    }

    @Override
    protected void validateData(Object[] data) throws IllegalArgumentException {
        super.validateData(data);

        boolean observableEditorCookiePresent = false;
        boolean fileObjectPresent = false;
        for (Object o : data) {
            if (o instanceof EditorCookie.Observable) {
                if (observableEditorCookiePresent) {
                    throw new IllegalArgumentException(
                            "multiple instances of EditorCookie.Observable in the data"); //NOI18N
                }
                observableEditorCookiePresent = true;
            }
            if (o instanceof FileObject) {
                if (fileObjectPresent) {
                    throw new IllegalArgumentException(
                            "multiple instances of FileObject in the data"); //NOI18N
                }
                fileObjectPresent = true;
            }
        }
    }

    @Override
    protected void setValidatedData(Object[] data) {
        EditorCookie.Observable observableEdCookie = null;
        SaveCookie saveCookie = null;
        FileObject fileObj = null;

        for (Object o : data) {
            if (o instanceof SaveCookie) {
                saveCookie = (SaveCookie) o;
                break;
            }

            if (o instanceof EditorCookie.Observable) {
                observableEdCookie = (EditorCookie.Observable) o;
            }

            if (o instanceof FileObject) {
                fileObj = (FileObject) o;
            }
        }

        if ((saveCookie == null) && (observableEdCookie != null)) {
            setDataSpecial(data, observableEdCookie, fileObj);
        } else {
            setDataSpecial(data, null, null);
        }
    }

    private void setDataSpecial(Object[] data,
                                EditorCookie.Observable editorCookie,
                                FileObject fileObj) {
        synchronized (dataSetLock) {
            Object[] newData;
            if (observableEditorCookie != null) {
                observableEditorCookie.removePropertyChangeListener(weakList);
            }
            this.observableEditorCookie = editorCookie;
            if (observableEditorCookie != null) {
                observableEditorCookie.addPropertyChangeListener(weakList = WeakListeners.propertyChange(this, observableEditorCookie));
            }

            if (observableEditorCookie == null) {
                this.withoutSaveCookie = null;
                this.withSaveCookie = null;
                newData = data;
            } else {
                this.withoutSaveCookie = data;
                this.withSaveCookie = CollectionUtils.appendItem(
                        withoutSaveCookie,
                        new EditorSaveCookie(editorCookie, fileObj));
                newData = observableEditorCookie.isModified()
                          ? withSaveCookie
                          : withoutSaveCookie;
            }
            setDataImpl(newData);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        synchronized (dataSetLock) {
            if (source != observableEditorCookie) {
                return;
            }
            setDataImpl(observableEditorCookie.isModified()
                        ? withSaveCookie
                        : withoutSaveCookie);
        }
    }

}
