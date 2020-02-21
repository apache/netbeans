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
package org.netbeans.modules.cnd.makeproject.ui.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.CodeStyleWrapper;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.spi.CndDocumentCodeStyleProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
final class CndDocumentCodeStyleProviderImpl implements CndDocumentCodeStyleProvider {
    private static final WeakHashMap<MakeProject, FileChangeListenerImpl> styleCache = new WeakHashMap<MakeProject, FileChangeListenerImpl>();
    
    @ServiceProvider(service = MakeProjectLookupProvider.class)
    public static class CndDocumentCodeStyleProviderFactory implements MakeProjectLookupProvider {

        @Override
        public void addLookup(MakeProject owner, ArrayList<Object> ic) {
            ic.add(new CndDocumentCodeStyleProviderImpl(owner));
        }
    }
    private final MakeProject project;

    private CndDocumentCodeStyleProviderImpl(MakeProject owner) {
        project = owner;
    }

    @Override
    public String getCurrentCodeStyle(String mimeType, Document doc) {
        if (project.isProjectFormattingStyle() == MakeProject.FormattingStyle.Project) {
            CodeStyleWrapper style = project.getProjectFormattingStyle(mimeType);
            if (style != null) {
                return style.getStyleId();
            }
        } else if (project.isProjectFormattingStyle() == MakeProject.FormattingStyle.ClangFormat) {
            CodeStyleWrapper style = project.getProjectFormattingStyle(null);
            if (style != null) {
                String res = style.getStyleId();
                String displayName = style.getDisplayName();
                if ("file".equals(displayName)) { //NOI18N
                    FileChangeListenerImpl listener = styleCache.get(project);
                    if (listener == null || !res.equals(listener.fileName)) {
                      MakeConfigurationDescriptor cd = (MakeConfigurationDescriptor)project.getConfigurationDescriptorProvider().getConfigurationDescriptor();
                      FileObject styleFO;
                      if (!CndPathUtilities.isPathAbsolute(cd.getBaseDirFileSystem(), res)) {
                        styleFO = RemoteFileUtil.getFileObject(cd.getBaseDirFileObject(), res); //NOI18N
                      } else {
                        styleFO = RemoteFileUtil.getFileObject(res, project);
                      }
                      if (styleFO != null && styleFO.isValid() && styleFO.isData()) {
                          listener = new FileChangeListenerImpl(res, styleFO);
                          styleFO.addFileChangeListener(listener);
                          styleCache.put(project, listener);
                      }
                    }
                    if (listener != null) {
                      return listener.fileContent;
                    }
                } else {
                    return res;
                }
            }   
        }
        return null;
    }

  private static class FileChangeListenerImpl implements FileChangeListener {
    private String fileName;
    private FileObject styleFO;
    private String fileContent;

    public FileChangeListenerImpl(String fileName, FileObject styleFO) {
      this.fileName = fileName;
      try {
        fileContent = styleFO.asText();
      } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
      }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
      // what to do?
    }

    @Override
    public void fileChanged(FileEvent fe) {
      styleFO = fe.getFile();
      if (styleFO != null && styleFO.isValid()) {
        try {
          fileContent = styleFO.asText();
        } catch (IOException ex) {
          Exceptions.printStackTrace(ex);
        }
      }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
      // what to do?
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
      // what to do?
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
      // what to do?
    }
  }
    
}
