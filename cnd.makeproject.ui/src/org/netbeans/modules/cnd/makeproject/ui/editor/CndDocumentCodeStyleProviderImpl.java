/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
