/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.ide;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.icon.PainterIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.diff.PatchUtils;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.favorites.api.Favorites;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.versioning.util.SearchHistorySupport;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.team.ide.spi.IDEServices.class)
public class IDEServicesImpl implements IDEServices {
    private static final Logger LOG = Logger.getLogger(IDEServicesImpl.class.getName());
    private final RequestProcessor RP = new RequestProcessor("Netbeans IDE Services for Team", 10); // IDE
    private Method fillStackTraceAnalyzer;

    @Override
    public boolean providesOpenDocument() {
        return true;
    }
    
    @Override
    @NbBundle.Messages({"LBL_OpenDocument=Open Document", 
                        "# {0} - to be opened documents path",  "MSG_CannotOpen=Could not open document with path\n {0}",
                        "# {0} - to be found documents path",  "MSG_CannotFind=Could not find document with path\n {0}"})
    public void openDocument(final String path, final int offset) {
        final FileObject fo = findFile(path);
        if ( fo != null ) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataObject od = DataObject.find(fo);
                        boolean ret = NbDocument.openDocument(od, offset, -1, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                        if(!ret) {
                            notifyError(Bundle.LBL_OpenDocument(), Bundle.MSG_CannotOpen(path));
                        }
                    } catch (DataObjectNotFoundException e) {
                        IDEServicesImpl.LOG.log(Level.SEVERE, null, e);
                    }
                }
            });
        } else {
            notifyError(Bundle.LBL_OpenDocument(), Bundle.MSG_CannotFind(path));
        }
    }

    @Override
    public boolean providesJumpTo() {
        return true;
    }

    @Override
    public void jumpTo(String resourcePath, String title) {
        TypeDescriptor td = TypeBrowser.browse(title, resourcePath, null);
        if(td != null) {
            td.open();
        }
    }
    
    @Override
    public boolean providesPluginUpdate() {
        return true;
    }

    @Override
    @NbBundle.Messages({"LBL_Error=Error",
                        "# {0} - pluginName", "MSG_CannotBeInstalled={0} plugin cannot be installed"})
    public Plugin getPluginUpdates(String cnb, final String pluginName) {
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        for (UpdateUnit u : units) {
            if(u.getCodeName().equals(cnb)) {
                List<UpdateElement> elements = u.getAvailableUpdates();
                final boolean isInstalled = u.getInstalled() != null;
                if(elements != null) {
                    for (final UpdateElement updateElement : elements) {
                        // even if there is more UpdateElements (more plugins with different versions),
                        // we will return the first one - it is given that it will have the highest version.
                        return new Plugin() {
                            @Override
                            public String getDescription() {
                                return updateElement.getDescription();
                            }
                            @Override
                            public boolean installOrUpdate() {
                                OperationContainer<InstallSupport> oc = isInstalled ? 
                                        OperationContainer.createForUpdate() : 
                                        OperationContainer.createForInstall();
                                if (oc.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                                    oc.add(updateElement);
                                    return PluginManager.openInstallWizard(oc);
                                } else {
                                    notifyError(Bundle.LBL_Error(), Bundle.MSG_CannotBeInstalled(pluginName)); 
                                }
                                return false;
                            }
                        };
                    }                    
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private static void notifyError (final String title, final String message) {
        NotifyDescriptor nd = new NotifyDescriptor(message, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[] {NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notifyLater(nd);
    }          

    @Override
    public boolean providesPatchUtils() {
        return true;
    }

    @Override
    public void applyPatch(final File patchFile) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                final File context = selectPatchContext();
                if (context != null) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                PatchUtils.applyPatch(patchFile, context);
                                Project project = FileOwnerQuery.getOwner(Utilities.toURI(context));
                                if(project != null && !OpenProjects.getDefault().isProjectOpen(project)) {
                                    OpenProjects.getDefault().open(new Project[] {project}, false);
                                }
                            } catch (IOException ex) {
                                LOG.log(Level.INFO, ex.getMessage(), ex);
                            } 
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean isPatch(File patchFile) throws IOException {
        return PatchUtils.isPatch(patchFile);
    }

    private File selectPatchContext() {
        PatchContextChooser chooser = new PatchContextChooser();
        ResourceBundle bundle = NbBundle.getBundle(IDEServicesImpl.class);
        JButton ok = new JButton(bundle.getString("LBL_Apply")); // NOI18N
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                chooser,
                bundle.getString("LBL_ApplyPatch"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.bugtracking.patchContextChooser")); // NOI18N
        File context = null;
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            context = chooser.getSelectedFile();
        }
        return context;
    }

    @Override
    public boolean providesOpenHistory() {
        return true;
    }

    @Override
    public boolean openHistory(String resourcePath, int line) {
        FileObject fo = findFile(resourcePath);
        File file = fo != null ? FileUtil.toFile(fo) : null;
        if(file == null) {
            LOG.log(Level.INFO, "No file available for path {0}", resourcePath);
            return false;
        }
        try {
            SearchHistorySupport support = SearchHistorySupport.getInstance(file);
            if(support != null) {
                return support.searchHistory(line);
            }
        } catch (IOException ex) {            
            LOG.log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private  FileObject findFile(String resourcePath) {
        return GlobalPathRegistry.getDefault().findResource(resourcePath);
    }    

    @Override
    public BusyIcon createBusyIcon() {
        return new SwingXBusyIcon();
    }

    @Override
    public boolean canOpenInFavorites() {
        return true;
    }

    @Override
    public void openInFavorites(File workingDir) {
        WindowManager.getDefault().findTopComponent("favorites").requestActive(); // NOI18N
        try {
            FileObject fo = FileUtil.toFileObject(workingDir);
            Favorites.getDefault().selectWithAddition(fo);
        } catch (IOException ex) {
            Logger.getLogger(IDEServicesImpl.class.getName()).log(Level.FINE, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(IDEServicesImpl.class.getName()).log(Level.FINE, ex.getMessage(), ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(IDEServicesImpl.class.getName()).log(Level.FINE, ex.getMessage(), ex);
        }
    }

    @Override
    public DatePickerComponent createDatePicker () {
        return new JXDatePickerImpl();
    }

    @Override
    public boolean isPluginInstalled(String cnb) {
        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        for (UpdateUnit u : units) {
            if(u.getCodeName().equals(cnb) && u.getInstalled() != null) {
                return true;
            }
        }
        return false;        
    }

    @Override
    public boolean providesShutdown(boolean restart) {
        return true;
    }

    @Override
    public void shutdown(boolean restart) {
        if(restart) {
            LifecycleManager.getDefault().markForRestart();
        }
        LifecycleManager.getDefault().exit();
    }

    @Override
    public boolean providesOpenInStackAnalyzer() {
        if(fillStackTraceAnalyzer != null) {
            return true;
        }
        try {
            TopComponent win = WindowManager.getDefault ().findTopComponent ("AnalyzeStackTopComponent");
            if(win != null) {
                Class c = win.getClass();            
                fillStackTraceAnalyzer = c.getDeclaredMethod("fill", BufferedReader.class);            
                fillStackTraceAnalyzer.setAccessible(true);
                return true;
            }
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    @Override
    public void openInStackAnalyzer(BufferedReader s) {
        assert fillStackTraceAnalyzer != null;
        try {
            TopComponent win = WindowManager.getDefault ().findTopComponent ("AnalyzeStackTopComponent");
            assert win != null;
            if(win != null) {
                win.open();
                win.requestActive();
                fillStackTraceAnalyzer.invoke(win, s);
            }
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static class SwingXBusyIcon extends PainterIcon implements BusyIcon {
        private static final int SIZE = 16;
        private static final int POINTS = 8;

        private int currentFrame;
        private BusyPainter busyPainter;

        SwingXBusyIcon() {
            super(new Dimension(SIZE, SIZE));
            busyPainter = new BusyPainter(SIZE);            
            busyPainter.setPoints(POINTS);
            setPainter(busyPainter);
        }

        @Override
        public void tick() {
            currentFrame = (currentFrame + 1) % POINTS;
            busyPainter.setFrame(currentFrame);
        }
    }
}
