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
package org.netbeans.spi.java.project.support.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.swing.JOptionPane;
import org.openide.actions.PasteAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Build",
        id = "org.netbeans.spi.java.project.support.ui.CreateClassAndCopy"
)
@ActionRegistration(
        displayName = "#CTL_CreateClassAndCopy"
)
@ActionReference(path = "Loaders/folder/any/Actions", position = 750)
@Messages("CTL_CreateClassAndCopy=PasteClass")
public final class CreateClassAndCopy implements ActionListener {

    private final DataFolder context;

    public CreateClassAndCopy(DataFolder context) {
        this.context = context;
    }

    static ExplorerManager findExplorerManager() {
        Throwable t = null;

        try {
            Class c = Class.forName("org.openide.windows.TopComponent"); // NOI18N

            // use reflection now
            Method m = c.getMethod("getRegistry"); // NOI18N
            Object o = m.invoke(null);

            c = Class.forName("org.openide.windows.TopComponent$Registry"); // NOI18N

            // use reflection now
            m = c.getMethod("getActivated"); // NOI18N
            o = m.invoke(o);

            if (o instanceof ExplorerManager.Provider) {
                return ((ExplorerManager.Provider) o).getExplorerManager();
            }
        } // exceptions from forName:
        catch (ClassNotFoundException x) {
        } catch (ExceptionInInitializerError x) {
        } catch (LinkageError x) {
        } // exceptions from getMethod:
        catch (SecurityException x) {
            t = x;
        } catch (NoSuchMethodException x) {
            t = x;
        } // exceptions from invoke
        catch (IllegalAccessException x) {
            t = x;
        } catch (IllegalArgumentException x) {
            t = x;
        } catch (InvocationTargetException x) {
            t = x;
        }

        if (t != null) {
            Logger.getLogger(PasteAction.class.getName()).log(Level.WARNING, null, t);
        }

        return null;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            if(!c.isDataFlavorAvailable(DataFlavor.stringFlavor))return;
            boolean containsClass = false;
            String data = (String) c.getData(DataFlavor.stringFlavor);
            String className = "";
            String uncommented = data.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");// NOI18N
            uncommented = uncommented.trim();
            StringTokenizer st = new StringTokenizer(uncommented, " ");// NOI18N
            if (st.hasMoreTokens() && st.nextToken().equals("public") && st.hasMoreTokens() && st.nextToken().equals("class") && st.hasMoreTokens()) {// NOI18N
                String cname = st.nextToken().trim();
                if (cname.charAt(cname.length() - 1) == '{') {
                    cname = cname.substring(0, cname.length() - 1);
                }
                if (SourceVersion.isIdentifier(cname)) {
                    containsClass = true;
                    className = cname;
                }
            }
            if (!containsClass) {
                JOptionPane.showMessageDialog(null, "Code not valid to create class");
                return;
            }
            Set<FileObject> files = this.context.files();
            if (files.size() != 1) {
                return;
            }
            String path = files.iterator().next().getPath();
            String packageName = "";
            File f = new File(path + "\\" + className + ".java");// NOI18N
            if (f.exists()) {
                JOptionPane.showMessageDialog(null, "Can not create class already present");
                return;
            }
            if (!f.createNewFile()) {
                JOptionPane.showMessageDialog(null, "Can not create file");
                return;
            }
            ExplorerManager explorerManager = findExplorerManager();
            if (explorerManager.getSelectedNodes().length != 1) {
                return;
            }
            Node selectedNode = explorerManager.getSelectedNodes()[0];
            while (!selectedNode.getName().equals("${src.dir}")) {// NOI18N
                packageName += selectedNode.getName();
                break;
            }
            try ( BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                data = "package " + packageName + ";\n" + data;// NOI18N
                bw.write(data);
            }

        } catch (UnsupportedFlavorException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}
