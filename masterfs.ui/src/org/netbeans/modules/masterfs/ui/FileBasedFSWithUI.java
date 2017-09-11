/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.ui;

import java.awt.Image;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.masterfs.filebasedfs.MasterFileSystemFactory;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.openide.filesystems.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Simple extension of master filesystem which provides 
 * icons for files.
 * 
 * @author sdedic
 */
public class FileBasedFSWithUI extends FileBasedFileSystem {
    private final StatusDecorator uiDecorator = new UiDecorator();
    
    @Override
    public StatusDecorator getDecorator() {
        return uiDecorator;
    }
    
    private class UiDecorator extends StatusImpl implements ImageDecorator {
        @Override
        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            Image retVal = null;

            Iterator<? extends BaseAnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                BaseAnnotationProvider ap = it.next();
                if (ap instanceof AnnotationProvider) {
                    retVal = ((AnnotationProvider)ap).annotateIcon(icon, iconType, files);
                }
            }
            if (retVal != null) {
                return retVal;
            }

            return icon;
        }
    }
    
    @ServiceProvider(service = MasterFileSystemFactory.class, 
            supersedes = "org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem$Factory")
    public static class Factory implements MasterFileSystemFactory {
        @Override
        public FileBasedFileSystem createFileSystem() {
            return new FileBasedFSWithUI();
        }
    }
}
