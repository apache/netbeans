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

package org.netbeans.modules.applemenu;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.loaders.DataObject;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
@ActionID(category = "Tools",
id = "org.netbeans.modules.applemenu.ShowInFinder")
@ActionRegistration(displayName = "#CTL_ShowInFinder")
@ActionReferences({
    @ActionReference(path = "UI/ToolActions/Files", position = 1001)
})
public final class ShowInFinder implements ActionListener {
        
    private static final Logger LOG = Logger.getLogger(ShowInFinder.class.getName());

    private final DataObject context;

    private static final RequestProcessor RP = new RequestProcessor( "ShowInFinder", 1 );

    public ShowInFinder(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        RP.execute(new Runnable() {
            @Override
            public void run() {
                FileObject fobj = context.getPrimaryFile();
                if (fobj == null) {
                    return;
                }
                LOG.log(Level.FINE, "Selected file: {0}", FileUtil.getFileDisplayName(fobj));       //NOI18N
                if (FileUtil.getArchiveFile(fobj)!=null) {
                    fobj = FileUtil.getArchiveFile(fobj);
                }
                LOG.log(Level.FINE, "File to select in Finder: {0}", FileUtil.getFileDisplayName(fobj));    //NOI18N
                final File file = FileUtil.toFile(fobj);
                if (file == null) {
                    LOG.log(Level.INFO, "Ignoring non local file: {0}", FileUtil.getFileDisplayName(fobj)); //NOI18N
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ShowInFinder.class, "TXT_NoLocalFile")); //NOI18N
                    return;
                }
                try {
                    final Class<?> fmClz = Class.forName("com.apple.eio.FileManager");      //NOI18N
                    final Method m = fmClz.getDeclaredMethod("revealInFinder", File.class); //NOI18N
                    m.invoke(null, file);
                } catch (ClassNotFoundException e) {
                    LOG.log(Level.FINE, "Cannot load com.apple.eio.FileManager class.");    //NOI18N
                } catch (NoSuchMethodException e) {
                    LOG.log(Level.FINE, "No method revealInFinder(java.io.File) in the com.apple.eio.FileManager class.");    //NOI18N
                } catch (InvocationTargetException e) {
                    LOG.log(Level.FINE, "Cannot invoke method com.apple.eio.FileManager.revealInFinder(java.io.File).");    //NOI18N
                } catch (IllegalAccessException e) {
                    LOG.log(Level.FINE, "The method com.apple.eio.FileManager.revealInFinder(java.io.File) is not accessible"); //NOI18N
                }
            }
        });
    }        
}
