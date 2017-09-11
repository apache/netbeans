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

package org.netbeans.modules.masterfs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions.IOHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Radek Matous
 */
public class ProvidedExtensionsProxy extends ProvidedExtensions {
    private Collection<BaseAnnotationProvider> annotationProviders;
    private static ThreadLocal  reentrantCheck = new ThreadLocal();
    
    /** Creates a new instance of ProvidedExtensionsProxy */
    public ProvidedExtensionsProxy(Collection/*AnnotationProvider*/ annotationProviders) {
        this.annotationProviders = annotationProviders;
    }
    
    @Override
    public IOHandler getCopyHandler(File from, File to) {
        if (to == null) {
            return null;
        }
        IOHandler retValue = null;
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext() && retValue == null;) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                ProvidedExtensions.IOHandler delgate = ((ProvidedExtensions)iListener).getCopyHandler(from, to);
                retValue = delgate != null ? new DelegatingIOHandler(delgate) : null;
            }
        }
        return retValue;
    }

    public ProvidedExtensions.DeleteHandler getDeleteHandler(final File f) {
        ProvidedExtensions.DeleteHandler retValue = null;
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext() && retValue == null;) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                ProvidedExtensions.DeleteHandler delegate = ((ProvidedExtensions)iListener).getDeleteHandler(f);
                retValue = delegate != null ? new DelegatingDeleteHandler(delegate) : null;
            } 
        }
        return retValue;                        
    }
    
    public ProvidedExtensions.IOHandler getRenameHandler(final File from, final String newName) {
        final File to = new File(from.getParentFile(), newName);
        IOHandler retValue = null;
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext() && retValue == null;) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                ProvidedExtensions.IOHandler delgate = ((ProvidedExtensions)iListener).getRenameHandler(from, newName);
                retValue = delgate != null ? new DelegatingIOHandler(delgate) : null;
            } 
        }
        return retValue;
    }
    
    public ProvidedExtensions.IOHandler getMoveHandler(final File from, final File to)  {
        if (to == null) {
            return null;
        }
        IOHandler retValue = null;
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext() && retValue == null;) {
            BaseAnnotationProvider provider = it.next();
            InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                ProvidedExtensions.IOHandler delgate = ((ProvidedExtensions)iListener).getMoveHandler(from, to);
                retValue = delgate != null ? new DelegatingIOHandler(delgate) : null;                
            }
        }
        return retValue;
    }
    
    public void createFailure(final FileObject parent, final String name, final boolean isFolder) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.createFailure(parent, name, isFolder);
                    }
                });                                                                                                                                                                                                                                                                                
            }
        }
    }
    
    public void beforeCreate(final FileObject parent, final String name, final boolean isFolder) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.beforeCreate(parent, name, isFolder);
                    }
                });                                                                                                                                                                                                                                                
            }
        }
    }
    
    public void deleteSuccess(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.deleteSuccess(fo);
                    }
                });                                                                                                                                                                                                                
            }
        }
    }
    
    public void deleteFailure(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.deleteFailure(fo);
                    }
                });                                                                                                                                                                                
            }
        }
    }
    
    public void createSuccess(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.createSuccess(fo);
                    }
                });                                                                                                                                                
            }
        }
    }
    
    public void beforeDelete(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener != null) {
                runCheckCode(new Runnable() {
                    public void run() {
                        iListener.beforeDelete(fo);
                    }
                });                                                                                                                
            }
        }
    }       

    public boolean canWrite(final File f) {
        final Boolean ret[] = new Boolean [] { null };
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ProvidedExtensions extension = (ProvidedExtensions)iListener;
                        if(ProvidedExtensionsAccessor.IMPL != null && 
                           ProvidedExtensionsAccessor.IMPL.providesCanWrite(extension)) 
                        {
                        ret[0] = ((ProvidedExtensions)iListener).canWrite(f);
                    }
                    }
                });                                                                                
                if(ret[0] != null && ret[0]) {
                    break;
            }
        }
        }
        return ret[0] != null ? ret[0] : super.canWrite(f);
    }
        
    
    public void beforeChange(final FileObject f) {    
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).beforeChange(f);
                    }
                });                                                                                
            }
        }
    }

    @Override
    public void fileLocked(final FileObject fo) throws IOException {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        ((ProvidedExtensions) iListener).fileLocked(fo);
                    }
                });
            }
        }
    }

    public void fileUnlocked(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).fileUnlocked(fo);
                    }
                });                
            }
        }
    }

    @Override
    public Object getAttribute(final File file, final String attrName) {
        final AtomicReference<Object> value = new AtomicReference();
        for (BaseAnnotationProvider provider : annotationProviders) {
            final InterceptionListener iListener = (provider != null) ? provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        value.set(((ProvidedExtensions) iListener).getAttribute(file, attrName));
                    }
                });
            }
            if (value.get() != null) {
               return value.get();
            }
        }
        return null;
    }

    @Override
    public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ? provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                ProvidedExtensions pe = (ProvidedExtensions)iListener;
                int prev = children.size();
                long ret = pe.refreshRecursively(dir, lastTimeStamp, children);
                assert ret != -1 || prev == children.size() : "When returning -1 from refreshRecursively, you cannot modify children: " + pe;
                if (ret != -1) {
                    return ret;
                }
            }
        }
        final File[] arr = dir.listFiles();
        if (arr != null) {
            children.addAll(Arrays.asList(arr));
        }
        return 0;
    }
    
    @Override
    public void createdExternally(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).createdExternally(fo);
                    }
                });
            }
        }
    }

    @Override
    public void deletedExternally(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).deletedExternally(fo);
                    }
                });
            }
        }
    }

    @Override
    public void fileChanged(final FileObject fo) {
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).fileChanged(fo);
                    }
                });
            }
        }
    }

    @Override
    public void beforeMove(final FileObject from, final File to) {
        if (to == null) {
            return;
        }
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).beforeMove(from, to);
                    }
                });
            }
        }
    }

    @Override
    public void moveSuccess(final FileObject from, final File to) {
        if (to == null) {
            return;
        }
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).moveSuccess(from, to);
                    }
                });
            }
        }
    }

    @Override
    public void moveFailure(final FileObject from, final File to) {
        if (to == null) {
            return;
        }
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).moveFailure(from, to);
                    }
                });
            }
        }
   }
    
    @Override
    public void beforeCopy(final FileObject from, final File to) {
        if (to == null) {
            return;
        }
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).beforeCopy(from, to);
                    }
                });
            }
        }
    }

    @Override
    public void copySuccess(final FileObject from, final File to) {
        if (to == null) {
            return;
        }
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).copySuccess(from, to);
                    }
                });
            }
        }
    }

    @Override
    public void copyFailure(final FileObject from, final File to) {
        if (to == null) {
            return;
        }
        for (Iterator<BaseAnnotationProvider> it = annotationProviders.iterator(); it.hasNext();) {
            BaseAnnotationProvider provider = it.next();
            final InterceptionListener iListener = (provider != null) ?  provider.getInterceptionListener() : null;
            if (iListener instanceof ProvidedExtensions) {
                runCheckCode(new Runnable() {
                    public void run() {
                        ((ProvidedExtensions)iListener).copyFailure(from, to);
                    }
                });
            }
        }
   }
    
    public static void checkReentrancy() {
        if (reentrantCheck.get() != null) {            
            Logger.getLogger("org.netbeans.modules.masterfs.ProvidedExtensionsProxy").log(Level.INFO,"Unexpected reentrant call", new Throwable());//NOI18N
            
        }
    }
        
    private static void runCheckCode(Runnable code) {
        try {
            reentrantCheck.set(Boolean.TRUE);
            code.run();
        } finally {
            reentrantCheck.set(null);
        }
    }
    private static void runCheckCode(FileSystem.AtomicAction code) throws IOException {
        try {
            reentrantCheck.set(Boolean.TRUE);
            code.run();
        } finally {
            reentrantCheck.set(null);
        }
    }
    
    private class DelegatingDeleteHandler implements ProvidedExtensions.DeleteHandler {
        private ProvidedExtensions.DeleteHandler delegate;
        private DelegatingDeleteHandler(final ProvidedExtensions.DeleteHandler delegate) {
            this.delegate = delegate;
        }
        public boolean delete(final File file) {
            final boolean[] retval = new boolean[1];
            runCheckCode(new Runnable() {
                public void run() {
                    retval[0] = delegate.delete(file);                    
                }
            });
            return retval[0];
        }        
    }
    
    private class DelegatingIOHandler implements ProvidedExtensions.IOHandler {
        private ProvidedExtensions.IOHandler delegate;
        private DelegatingIOHandler(final ProvidedExtensions.IOHandler delegate) {
            this.delegate = delegate;
        }        
        public void handle() throws IOException {
            final IOException[] retval = new IOException[1];
            runCheckCode(new Runnable() {
                public void run() {
                    try {
                        delegate.handle();
                    } catch (IOException ex) {
                        retval[0] = ex;
                    }
                }
            });
            if (retval[0] != null) {
                throw retval[0];
            }
        }
    }    
}
