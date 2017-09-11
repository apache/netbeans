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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public class FixClassesSupport {
    
    /**
     * Reload the classes in debugger.
     * @param debugger
     * @param classes
     * @param statusCallback Success and status text
     */
    public static void reloadClasses(final JPDADebugger debugger,
                                     Map<String, FileObject> classes) {
        final Map<String, byte[]> map = new HashMap<String, byte[]>();
        for (String className : classes.keySet()) {
            FileObject fo = classes.get(className);
            InputStream is = null;
            try {
                is = fo.getInputStream();
                long fileSize = fo.getSize();
                byte[] bytecode = new byte[(int) fileSize];
                is.read(bytecode);
                map.put(className,
                        bytecode);
                System.out.println(" " + className);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        if (map.isEmpty()) {
            //System.out.println(" No class to reload");
            return ;
        }

        RequestProcessor rp;
        try {
            Session s = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
            rp = s.lookupFirst(null, RequestProcessor.class);
            if (rp == null) {
                rp = new RequestProcessor(FixClassesSupport.class.getName());
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return ;
        }

        rp.post(new Runnable() {
            @Override
            public void run() {
                String error = null;
                try {
                    debugger.fixClasses(map);
                } catch (UnsupportedOperationException uoex) {
                    error = NbBundle.getMessage(FixClassesSupport.class, "MSG_FixUnsupported", uoex.getLocalizedMessage());
                } catch (NoClassDefFoundError ncdfex) {
                    error = NbBundle.getMessage(FixClassesSupport.class, "MSG_FixMismatch", ncdfex.getLocalizedMessage());
                } catch (VerifyError ver) {
                    error = NbBundle.getMessage(FixClassesSupport.class, "MSG_FixVerifierProblems", ver.getLocalizedMessage());
                } catch (UnsupportedClassVersionError ucver) {
                    error = NbBundle.getMessage(FixClassesSupport.class, "MSG_FixUnsupportedVersion", ucver.getLocalizedMessage());
                } catch (ClassFormatError cfer) {
                    error = NbBundle.getMessage(FixClassesSupport.class, "MSG_FixNotValid", cfer.getLocalizedMessage());
                } catch (ClassCircularityError ccer) {
                    error = NbBundle.getMessage(FixClassesSupport.class, "MSG_FixCircularity", ccer.getLocalizedMessage());
                } catch (RuntimeException vmdisc) {
                //} catch (VMDisconnectedException vmdisc) {
                    if ("com.sun.jdi.VMOutOfMemoryException".equals(vmdisc.getClass().getName())) {
                        error = NbBundle.getMessage(FixClassesSupport.class, "MSG_FixOOME");
                    } else if ("com.sun.jdi.VMDisconnectedException".equals(vmdisc.getClass().getName())) {
                        //BuildArtifactMapper.removeArtifactsUpdatedListener(url, ArtifactsUpdatedImpl.this);
                        return ;
                    } else {
                        throw vmdisc;
                    }
                }
                if (error != null) {
                    notifyError(debugger, error);
                    setStatusText(debugger, error);
                } else {
                    setStatusText(debugger, NbBundle.getMessage(FixClassesSupport.class, "MSG_FixSuccess"));
                }
            }
        });
    }

    static void notifyError(JPDADebugger debugger, String error) {
        try {
            debugger.getClass().getMethod("actionErrorMessageCallback",
                                          new Class[] { Object.class, String.class })
                    .invoke(debugger,
                            new Object[]{ ActionsManager.ACTION_FIX, error });
        } catch (Exception | Error ex) {
            Exceptions.printStackTrace(ex);
            return ;
        }
        setStatusText(debugger, error);
    }
    
    static void setStatusText(JPDADebugger debugger, String status) {
        try {
            debugger.getClass().getMethod("actionStatusDisplayCallback",
                                          new Class[] { Object.class, String.class })
                    .invoke(debugger,
                            new Object[]{ ActionsManager.ACTION_FIX, status });
        } catch (Exception | Error ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static final class ClassesToReload {

        private static ClassesToReload instance;

        // debugger -> src root FileObject -> class name -> class FileObject
        private final Map<JPDADebugger, Map<FileObject, Map<String, FileObject>>> classesByDebugger =
                new WeakHashMap<>();
        private final PropertyChangeSupport pch = new PropertyChangeSupport(this);

        private ClassesToReload() {}

        public static synchronized ClassesToReload getInstance() {
            if (instance == null) {
                instance = new ClassesToReload();
            }
            return instance;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pch.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pch.removePropertyChangeListener(l);
        }

        public void addClassToReload(JPDADebugger debugger, FileObject src,
                                     String className, FileObject fo) {
            synchronized (this) {
                Map<FileObject, Map<String, FileObject>> srcRoots = classesByDebugger.get(debugger);
                if (srcRoots == null) {
                    srcRoots = new HashMap<>();
                    classesByDebugger.put(debugger, srcRoots);
                }
                Map<String, FileObject> classes = srcRoots.get(src);
                if (classes == null) {
                    classes = new HashMap<>();
                    srcRoots.put(src, classes);
                }
                classes.put(className, fo);
            }
            pch.firePropertyChange("classesToReload", null, className);
        }

        public synchronized boolean hasClassesToReload(JPDADebugger debugger, Set<FileObject> enabledSourceRoots) {
            Map<FileObject, Map<String, FileObject>> srcRoots = classesByDebugger.get(debugger);
            if (srcRoots != null) {
                for (FileObject src : srcRoots.keySet()) {
                    if (enabledSourceRoots.contains(src)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Map<String, FileObject> popClassesToReload(JPDADebugger debugger, Set<FileObject> enabledSourceRoots) {
            Map<String, FileObject> classes = new HashMap<>();
            synchronized (this) {
                Map<FileObject, Map<String, FileObject>> srcRoots = classesByDebugger.get(debugger);
                if (srcRoots != null) {
                    Set<FileObject> sourceRoots = new HashSet<>(srcRoots.keySet());
                    for (FileObject src : sourceRoots) {
                        if (enabledSourceRoots.contains(src)) {
                            classes.putAll(srcRoots.remove(src));
                        }
                    }
                }
            }
            if (classes.size() > 0) {
                pch.firePropertyChange("classesToReload", null, null);
            }
            return classes;
        }

    }
    
}
