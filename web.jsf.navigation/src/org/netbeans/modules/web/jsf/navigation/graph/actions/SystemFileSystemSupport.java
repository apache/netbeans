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


package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;


/**
 * Provides support for retrieving instances from the system filesystem (SFS) folder.
 * Currently it supports retrieving <code>Action</code> instances.
 * <p>
 * Note: It doesn't support the retrieving of instances from the subfolders.
 * </p>
 *
 * @author Peter Zavadsky
 */
public final class SystemFileSystemSupport {

    /** Inteface defining the action provider. */
    interface ActionsProvider {
        Action[] getActions();
    }

    /** Dummy impl of <code>ActionsProvider</code> used for the cases
     * the folder in the system filesystem doesn't exist. */
    private static ActionsProvider DUMMY_ACTIONS_PROVIDER = new ActionsProvider() {
        public Action[] getActions() {
            return new Action[0];
        }
    };

    /** Maps <code>DataFolder</code> to <code>ActionsProvider</code>. */
    private static final Map<DataFolder,ActionsProvider> dataFolder2actionsProvider = new WeakHashMap<DataFolder,ActionsProvider>();


    private SystemFileSystemSupport() {
    }


    /** Provides the actions retrieved from the specified folder in SFS.
     * If the specified folder doesn't exist, an empty array is returned.
     * The <code>null</code> values in the array represent separators.
     * <p>
     * Note: It doesn't retrieve the actions from the subfolders.
     * </p>
     * @param folderPath specifies the path to the folder in SFS
     * @return Action[] */
    public static Action[] getActions(String folderPath) {
        return getActionProvider(folderPath).getActions();
    }

    /** Gets <code>ActionProvider</code> for specified folder in SFS.
     * @param folderPath specifies the path to the folder in SFS */
    private static ActionsProvider getActionProvider(String folderPath) {
        DataFolder dataFolder = getDataFolder(folderPath);
        if (dataFolder == null) {
            return DUMMY_ACTIONS_PROVIDER;
        }

        synchronized (dataFolder2actionsProvider) {
            ActionsProvider actionsProvider = (ActionsProvider)dataFolder2actionsProvider.get(dataFolder);
            if (actionsProvider == null) {
                actionsProvider = new DefaultActionsProvider(dataFolder);
                dataFolder2actionsProvider.put(dataFolder, actionsProvider);
            }
            return actionsProvider;
        }
    }


    private static DataFolder getDataFolder(String folderPath) {
        FileObject fileObject = FileUtil.getConfigFile(folderPath);
        if (fileObject == null) {
            return null;
        }

        return DataFolder.findFolder(fileObject);
    }


    private static class DefaultActionsProvider extends FolderInstance implements ActionsProvider {

        public DefaultActionsProvider(DataFolder dataFolder) {
            super(dataFolder);
        }

        /** Gets the action array. */
        public Action[] getActions() {
            try {
                return (Action[])instanceCreate();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (ClassNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

            return new Action[0];
        }

        /** Creates the actions. */
        protected Object createInstance(InstanceCookie[] cookies)
        throws IOException, ClassNotFoundException {
            List<Action> actions = new ArrayList<Action>();
            for (int i = 0; i < cookies.length; i++) {
                Class clazz = cookies[i].instanceClass();
                if (JSeparator.class.isAssignableFrom(clazz)) {
                    // XXX <code>null</code> is interpreted as a separator.
                    actions.add(null);
                    continue;
                }

                Object object;
                try {
                    object = cookies[i].instanceCreate();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    continue;
                } catch (ClassNotFoundException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    continue;
                }
                
                if (object instanceof Action) {
                    actions.add((Action)object);
                    continue;
                } else {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new IllegalStateException("There is an unexpected object=" + object + // NOI18N
                            ", in the folder instance=" + this)); // NOI18N
                    continue;
                }
            }

            return actions.toArray(new Action[0]);
        }

        /** Currently not recursive. */
        protected InstanceCookie acceptFolder(DataFolder df) {
            return null;
        }
    } // End of DefaultActionsProvider class.

}

