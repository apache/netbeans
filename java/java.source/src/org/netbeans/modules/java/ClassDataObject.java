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

package org.netbeans.modules.java;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "ClassResolver=Class Files"
})
@MIMEResolver.ExtensionRegistration(
    position=101,
    displayName="#ClassResolver",
    extension="class",
    mimeType="application/x-class-file"
)    
public final class ClassDataObject extends MultiDataObject {
    
    public ClassDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(new OpenSourceCookie());
    }

    public @Override Node createNodeDelegate() {
        return new JavaNode (this, false);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private final class OpenSourceCookie implements OpenCookie {
        
        @Override
        public void open() {
            final AtomicBoolean cancel = new AtomicBoolean();
            BaseProgressUtils.runOffEventDispatchThread(() -> {
                    try {
                        FileObject fo = getPrimaryFile();
                        FileObject binaryRoot = null;
                        String resourceName = null;
                        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                        if (cp == null || (binaryRoot = cp.findOwnerRoot(fo)) == null) {
                            cp = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
                            if (cp != null) {
                                binaryRoot = cp.findOwnerRoot(fo);
                                resourceName = cp.getResourceName(fo,'.',false);  //NOI18N
                            }
                        } else if (binaryRoot != null) {
                            resourceName = cp.getResourceName(fo,'.',false);  //NOI18N
                        }
                        if (cancel.get()) {
                            return;
                        }
                        ElementHandle<? extends Element> handle = null;
                        if (resourceName != null) {
                            if ("module-info".equals(resourceName)) {   //NOI18N
                                final String moduleName = SourceUtils.getModuleName(binaryRoot.toURL());
                                if (moduleName != null) {
                                    handle = ElementHandle.createModuleElementHandle(moduleName);
                                }
                            }
                            if (handle == null) {
                                handle = ElementHandle.createTypeElementHandle(ElementKind.CLASS, resourceName.replace('/', '.'));
                            }
                        }
                        FileObject resource = null;
                        if (binaryRoot != null) {
                            //Todo: Ideally it should do the same as ElementOpen.open () but it will require a copy of it because of the reverese module dep.
                            resource = SourceUtils.getFile(handle, ClasspathInfo.create(
                                    ClassPathSupport.createClassPath(binaryRoot),
                                    ClassPath.EMPTY,
                                    ClassPath.EMPTY));
                        }
                        if (cancel.get()) {
                            return;
                        }
                        if (resource !=null ) {
                            DataObject sourceFile = DataObject.find(resource);
                            OpenCookie oc = sourceFile.getCookie(OpenCookie.class);
                            if (oc != null) {
                                oc.open();
                            } else {
                                Logger.getLogger(ClassDataObject.class.getName()).log(Level.WARNING, "SourceFile: {0} has no OpenCookie", FileUtil.getFileDisplayName (resource)); //NOI18N
                            }
                        } else {
                            final BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);
                            if (beo != null && handle != null && cp != null) {
                                final ClasspathInfo cpInfo = ClasspathInfo.create(
                                        Optional.ofNullable(ClassPath.getClassPath(fo, ClassPath.BOOT))
                                            .orElse(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries()),
                                        cp,
                                        ClassPath.EMPTY);
                                if (beo.open(cpInfo, handle, cancel)) {
                                    return;
                                }
                            }
                            if (resourceName == null) {
                                resourceName = fo.getName();
                            }
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                                    ClassDataObject.class,
                                    "TXT_NoSources",    //NOI18N
                                    resourceName.replace('/', '.'))); //NOI18N
                        }
                    } catch (DataObjectNotFoundException nf) {
                        Exceptions.printStackTrace(nf);
                    }
                },
            NbBundle.getMessage(ClassDataObject.class, "TXT_OpenClassFile"),
            cancel,
            false);
        }
    }
}
