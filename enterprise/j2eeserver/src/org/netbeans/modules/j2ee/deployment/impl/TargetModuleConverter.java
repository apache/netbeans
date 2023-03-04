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

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

import org.netbeans.spi.settings.DOMConvertor;
import org.openide.util.NbBundle;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author  nn136682
 */
public class TargetModuleConverter extends DOMConvertor {

    private static final String PUBLIC_ID = "-//org_netbeans_modules_j2ee//DTD TargetModule 1.0//EN"; // NOI18N
    private static final String SYSTEM_ID = "nbres:/org/netbeans/modules/j2ee/deployment/impl/target-module.dtd"; // NOI18N
    private static final String E_TARGET_MODULE_LIST = "target-module-list";

    private static final String E_TARGET_MODULE = "target-module";
    private static final String A_ID = "id";
    private static final String A_INSTANCE_URL = "instance-url";
    private static final String A_TARGET_NAME = "target-name";
    private static final String A_TIMESTAMP = "timestamp";
    private static final String A_CONTENT_DIR = "content-dir";
    private static final String A_CONTEXT_ROOT = "context-root";

    private static final String CHARSET = "UTF-8"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(TargetModuleConverter.class.getName());

    // FIXME this is public only because of tests
    public static DOMConvertor create() {
        return new TargetModuleConverter();
    }
    
    /** Creates a new instance of TargetModuleConverter */
    protected TargetModuleConverter() {
        super(PUBLIC_ID, SYSTEM_ID, E_TARGET_MODULE_LIST);
    }
    
    @Override
    protected Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
        NodeList targetModuleElements =  element.getElementsByTagName(E_TARGET_MODULE);
        TargetModule[] targetModules = new TargetModule[targetModuleElements.getLength()];
        for (int i = 0; i < targetModules.length; i++) {
            Element te = (Element) targetModuleElements.item(i);
            String id = te.getAttribute(A_ID);
            String url = te.getAttribute(A_INSTANCE_URL);
            String targetName = te.getAttribute(A_TARGET_NAME);
            String timestamp = te.getAttribute(A_TIMESTAMP);
            String contentDir = te.getAttribute(A_CONTENT_DIR);
            String contextRoot = te.getAttribute(A_CONTEXT_ROOT);

            if (id == null || url == null || targetName == null) {
                throw new IOException(NbBundle.getMessage(TargetModuleConverter.class, "MSG_TargetModuleParseError"));
            }

            try {
                targetModules[i] = new TargetModule(id, url, targetName, Long.parseLong(timestamp), contentDir, contextRoot);
            } catch (NumberFormatException nfe) {
                throw new IOException(nfe);
            }
        }
        return new TargetModule.List(targetModules);
    }
    
    @Override
    protected void writeElement(org.w3c.dom.Document doc, org.w3c.dom.Element element, Object obj) throws IOException, DOMException {
        if (obj == null) {
            return;
        }
        
        if (!(obj instanceof TargetModule.List)) {
            throw new DOMException(
                DOMException.NOT_SUPPORTED_ERR, 
                NbBundle.getMessage(TargetModuleConverter.class, "MSG_NotSupportedObject", obj.getClass()));
        }
        
        TargetModule.List tmList = (TargetModule.List) obj;
        TargetModule[] targetModules = tmList.getTargetModules();
        for (int i=0; i<targetModules.length; i++) {
            Element tmElement = doc.createElement (E_TARGET_MODULE);
            tmElement.setAttribute(A_ID, targetModules[i].getId());
            tmElement.setAttribute(A_INSTANCE_URL, targetModules[i].getInstanceUrl());
            tmElement.setAttribute(A_TARGET_NAME, targetModules[i].getTargetName());
            tmElement.setAttribute(A_TIMESTAMP, String.valueOf(targetModules[i].getTimestamp()));
            tmElement.setAttribute(A_CONTENT_DIR, targetModules[i].getContentDirectory());
            tmElement.setAttribute(A_CONTEXT_ROOT, targetModules[i].getContextRoot());
            element.appendChild (tmElement);
        }
    }

    @Override
    public void registerSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of TargetModule
    }
    @Override
    public void unregisterSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        // Not needed:  there is not editing of TargetModule
    }

    private static final String DIR_TARGETMODULES = "TargetModules";
    private static FileObject targetModulesDir = null;
    private static FileObject getTargetModulesDir() throws IOException {
        assert Thread.holdsLock(TargetModuleConverter.class);
        if (targetModulesDir == null) {
            FileObject j2eeDir = FileUtil.getConfigFile("J2EE");
            targetModulesDir = j2eeDir.getFileObject(DIR_TARGETMODULES);
            if (targetModulesDir == null) {
                targetModulesDir = j2eeDir.createFolder(DIR_TARGETMODULES);
            }
        }
        return targetModulesDir;
    }

    public static synchronized boolean writeTargetModule(final TargetModule instance, String managerDir, String targetDir, String tmFileName) {
        try {
            FileObject managerDirFO = getTargetModulesDir().getFileObject(managerDir);
            if (managerDirFO == null) {
                managerDirFO = getTargetModulesDir().createFolder(managerDir);
            }
            FileObject targetDirFO = managerDirFO.getFileObject(targetDir);
            if (targetDirFO == null) {
                targetDirFO = managerDirFO.createFolder(targetDir);
            }
            final FileObject fo = FileUtil.createData(targetDirFO, tmFileName);

            FileLock lock = fo.lock();
            try {
                Writer writer = new OutputStreamWriter(fo.getOutputStream(lock), CHARSET);
                try {
                    create().write(writer, new TargetModule.List(instance));
                } finally {
                    writer.close();
                }
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return false;
        }
    }

    @CheckForNull
    public static synchronized TargetModule readTargetModule(String managerDir, String targetDir, String tmFileName) {
        try {
            FileObject dir = getTargetModulesDir().getFileObject(managerDir);
            if (dir != null) {
                dir = dir.getFileObject(targetDir);
                if (dir != null) {
                    final FileObject fo = dir.getFileObject(tmFileName);
                    if (fo != null) {
                        Reader reader = new InputStreamReader(fo.getInputStream(), CHARSET);
                        try {
                            TargetModule.List tml = (TargetModule.List) create().read(reader);
                            if (tml == null || tml.getTargetModules().length < 1) {
                                return null;
                            }
                            return tml.getTargetModules()[0];
                        } finally {
                            reader.close();
                        }
                    }
                }
            }
            return null;
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Could not read {0}", tmFileName);
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
    }

    @NonNull
    public static synchronized List<TargetModule> getTargetModulesByContextRoot(String managerDir, String targetDir, final String contextRoot) {
        try {
            FileObject dir = getTargetModulesDir().getFileObject(managerDir);
            if (dir != null) {
                dir = dir.getFileObject(targetDir);
                if (dir != null) {
                    java.util.Enumeration fos = dir.getChildren(false);
                    final ArrayList<TargetModule> result = new ArrayList<TargetModule>();
                    while (fos.hasMoreElements()) {
                        final FileObject fo = (FileObject) fos.nextElement();
                        final AtomicInteger exceptions = new AtomicInteger();
                        try {
                            Reader reader = new InputStreamReader(fo.getInputStream(), CHARSET);
                            try {
                                TargetModule.List tml = (TargetModule.List) create().read(reader);
                                if (tml != null && tml.getTargetModules().length > 0) {
                                    TargetModule tm = tml.getTargetModules()[0];
                                    if (contextRoot.equals(tm.getContextRoot())) {
                                        result.add(tm);
                                    }
                                }
                            } finally {
                                reader.close();
                            }
                        } catch (IOException ioe) {
                            if (exceptions.getAndIncrement() < 1) {
                                LOGGER.log(Level.WARNING, fo.getName(), ioe);
                            } else {
                                LOGGER.log(Level.INFO, fo.getName(), ioe);
                            }
                        }
                    }
                    return result;
                }
            }
            return Collections.emptyList();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Collections.emptyList();
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Collections.emptyList();
        }
    }

    public static synchronized void remove(String managerDir, String targetDir, String tmFileName) {
        try {
            FileObject dir = getTargetModulesDir().getFileObject(managerDir);
            if (dir != null) {
                dir = dir.getFileObject(targetDir);
                if (dir != null) {
                    final FileObject fo = dir.getFileObject(tmFileName);
                    if (fo != null) {
                        FileLock lock = fo.lock();
                        try {
                            fo.delete(lock);
                        } finally {
                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

}
