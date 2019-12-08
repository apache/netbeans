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
        final AtomicReference<Object> value = new AtomicReference<>();
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
