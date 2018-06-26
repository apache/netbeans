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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.rest.nodes;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Liu
 */
public class OpenCookieFactory {
    
    public static OpenCookie create(Project project, String className) {
        return create(project, className, null);
    }
    
    public static OpenCookie create(Project project, String className, String methodName) {
        return new OpenCookieImpl(project, className, methodName);
    }
    
    private static class OpenCookieImpl implements OpenCookie {
        private String className;
        private String methodName;
        private Project project;
        
        public OpenCookieImpl(Project project, String className, String methodName) {
            this.project = project;
            this.className = className;
            this.methodName = methodName;
        } 
        
        @Override
        public void open() {
            if ( SwingUtilities.isEventDispatchThread()){
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        doOpen();
                    }
                },
                NbBundle.getMessage(OpenCookieFactory.class, "TXT_OpenResource"),    // NOI18N
                cancel,
                false);
            }
            else {
                doOpen();
            }
        }

        private void doOpen() {
            try {
                FileObject source = SourceGroupSupport.getFileObjectFromClassName(
                        className, project);
                if (source != null) {
                    final DataObject dataObj = DataObject.find(source);
                    if (dataObj != null) {
                        JavaSource javaSource = JavaSource.forFileObject(source);
                        final long[] position = JavaSourceHelper.getPosition(
                                javaSource, methodName);
                        
                        SwingUtilities.invokeLater( new Runnable() {
                            
                            @Override
                            public void run() {
                                OpenCookie oc = (OpenCookie) dataObj.getCookie(
                                        OpenCookie.class);
                                if (oc != null) {
                                    oc.open();
                                }
                                LineCookie lc = (LineCookie) dataObj.getCookie(
                                        LineCookie.class);
                                if (lc != null) {
                                    Line line = lc.getLineSet().getOriginal(
                                            (int) position[0]);
                                    line.show(ShowOpenType.OPEN, 
                                            ShowVisibilityType.NONE, (int) position[1]);
                                }
                                
                            }
                        });
                        
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
