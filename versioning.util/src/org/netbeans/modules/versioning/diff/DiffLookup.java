/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
