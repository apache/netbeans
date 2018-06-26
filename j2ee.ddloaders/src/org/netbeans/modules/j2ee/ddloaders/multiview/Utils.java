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
package org.netbeans.modules.j2ee.ddloaders.multiview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.JTree;
import javax.swing.UIManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.openide.util.Exceptions;

/**
 * @author pfiala
 */
public class Utils {

    public static final String ICON_BASE_DD_VALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDValidIcon"; // NOI18N
    public static final String ICON_BASE_DD_INVALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDInvalidIcon"; // NOI18N
    public static final String ICON_BASE_EJB_MODULE_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbModuleNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTERPRISE_JAVA_BEANS_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbContainerNodeIcon"; // NOI18N
    public static final String ICON_BASE_SESSION_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/SessionNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTITY_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EntityNodeIcon"; // NOI18N
    public static final String ICON_BASE_MESSAGE_DRIVEN_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MessageNodeIcon"; // NOI18N
    public static final String ICON_BASE_MISC_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MiscNodeIcon"; // NOI18N

    private static BrowseFolders.FileObjectFilter imageFileFilter = new BrowseFolders.FileObjectFilter() {
        @Override
        public boolean accept(FileObject fileObject) {
            return fileObject.getMIMEType().startsWith("image/"); // NOI18N
        }
    };

    public static String browseIcon(EjbJarMultiViewDataObject dataObject) {
        FileObject fileObject = org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders.showDialog(
                dataObject.getSourceGroups(), imageFileFilter);
        String relativePath;
        if (fileObject != null) {
            FileObject projectDirectory = dataObject.getProjectDirectory();
            relativePath = FileUtil.getRelativePath(projectDirectory, fileObject);
        } else {
            relativePath = null;
        }
        return relativePath;
    }

    public static Color getErrorColor() {
        // inspired by org.openide.WizardDescriptor
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        return c == null ? new Color(89, 79, 191) : c;
    }

    public static JTree findTreeComponent(Component component) {
        if (component instanceof JTree) {
            return (JTree) component;
        }
        if (component instanceof Container) {
            Component[] components = ((Container) component).getComponents();
            for (int i = 0; i < components.length; i++) {
                JTree tree = findTreeComponent(components[i]);
                if (tree != null) {
                    return tree;
                }
            }
        }
        return null;
    }

    // TODO: remove this, but is used on too many places...
    public static String getBundleMessage(String messageId) {
        return NbBundle.getMessage(Utils.class, messageId);
    }

    /**
     * Returns true, if the passed string can be used as a qualified identifier.
     * it does not check for semantic, only for syntax.
     * The function returns true for any sequence of identifiers separated by
     * dots.
     */
    public static boolean isValidPackageName(String packageName) {
        String[] strings = packageName.split("[.]");  // NOI18N
        if (strings.length == 0) {
            return false;
        }
        for (int i = 0; i < strings.length; i++) {
            if (!Utilities.isJavaIdentifier(strings[i])) {
                return false;
            }
        }
        return packageName.charAt(packageName.length() - 1) != '.';
    }

    public static FileObject getPackageFile(ClassPath classPath, String packageName) {
        return classPath.findResource(packageToPath(packageName));
    }

    private static String packageToPath(String packageName) {
        return packageName.replace('.', '/');
    }

    public static String getPackage(String ejbClass) {
        final int i = ejbClass.lastIndexOf('.');
        if (i < 0) {
            return "";
        } else {
            return ejbClass.substring(0, i);
        }

    }

    public static void notifyError(Exception ex) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static FileObject getSourceFile(ClassPath classPath, String className) {
        return classPath.findResource(packageToPath(className) + ".java");
    }

    public static String getEjbDisplayName(Ejb ejb) {
        String name = ejb.getDefaultDisplayName();
        if (name == null) {
            name = ejb.getEjbName();
            if (name == null) {
                name = " ";  // NOI18N
            }
        }
        return name;
    }

    /**
     * Opens the editor for the given <code>ejbClass</code>.
     * @param ejbJarFile the ejb-jar.xml file where the class is defined.
     * @param ejbClass the FQN of the Ejb to be opened.
     */
     public static void openEditorFor(FileObject ejbJarFile, final String ejbClass) {
        EjbJar ejbModule = EjbJar.getEjbJar(ejbJarFile);
         // see #123848
         if (ejbModule == null) {
             displaySourceNotFoundDialog();
             return;
         }
       
        MetadataModel<EjbJarMetadata> ejbModel = ejbModule.getMetadataModel();
        try {
            FileObject classFo = ejbModel.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {

                        public FileObject run(EjbJarMetadata metadata) throws Exception {
                            return metadata.findResource(ejbClass.replace('.', '/') + ".java"); //NO18N
                        }
                    });

            final List<ElementHandle<TypeElement>> handle = new ArrayList<ElementHandle<TypeElement>>(1);
            if (classFo != null) {
                JavaSource source = JavaSource.forFileObject(classFo);
                source.runUserActionTask(new Task<CompilationController>() {

                            public void run(CompilationController controller) throws Exception {
                                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                TypeElement typeElement = controller.getElements().getTypeElement(ejbClass);
                                if (typeElement != null) {
                                    handle.add(ElementHandle.create(typeElement));
                                }
                            }
                        }, false);
            }
            if (!handle.isEmpty()) {
                ElementOpen.open(classFo, handle.get(0));
            } else {
                displaySourceNotFoundDialog();
            }

        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void displaySourceNotFoundDialog() {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(Utils.class, "MSG_sourceNotFound")));

    }

    /**
     * Make sure that the code will run in AWT dispatch thread
     * @param runnable
     */
    public static void runInAwtDispatchThread(Runnable runnable) {
        org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(runnable);
    }

}
