/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;


public class TreeModelExample4 implements TreeModel, NodeModel, 
NodeActionsProvider, TableModel {    
    
    public Object[] getChildren (Object parent, int from, int to) {
        if (parent == ROOT)
            return File.listRoots ();
        return ((File) parent).listFiles ();
    }
    
    public Object getRoot () {
        return ROOT;
    }
    
    public boolean isLeaf (Object node) {
        if (node == ROOT)
            return false;
        return ((File) node).isFile ();
    }
    
    public void addTreeModelListener (TreeModelListener l) {}
    public void removeTreeModelListener (TreeModelListener l) {}
    
    public String getDisplayName (Object node) {
        if (node == ROOT) return "Name";
        String name = ((File) node).getName ();
        if (name.length () < 1) return ((File) node).getAbsolutePath ();
        return name;
    }
    
    public String getIconBase (Object node) {
        if (node == ROOT) return "folder";
        if (((File) node).isDirectory ()) return "folder";
        return "file";
    }
    
    public String getShortDescription (Object node) {
        if (node == ROOT) return "Name";
        return ((File) node).getAbsolutePath ();
    }
    
    public Action[] getActions (final Object node) {
        return new Action [] {
            new AbstractAction ("Open") {
                public void actionPerformed (ActionEvent e) {
                    performDefaultAction (node);
                }
            },
            new AbstractAction ("Delete") {
                public void actionPerformed (ActionEvent e) {
                    ((File) node).delete ();
                }
            }
        };
    }
    
    public void performDefaultAction (Object node) {
        try {
            JFrame f = new JFrame ("View");
            f.getContentPane ().add (new JEditorPane (((File) node).toURL ()));
            f.pack ();
            f.show ();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Object getValueAt (Object row, String columnID) {
        try {
            if (row == ROOT) return null;
            if (columnID.equals ("sizeID")) {
                if (((File) row).isDirectory ()) return "<dir>";
                return "" + new FileInputStream ((File) row).getChannel ().size ();
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return "";
    }
    
    public boolean isReadOnly (Object row, String columnID) {
        return true;
    }
    
    public void setValueAt (Object row, String columnID, Object value) {
    }
    
    public static void main (String[] args) {
        TreeModelExample4 tme = new TreeModelExample4 ();
        ArrayList columns = new ArrayList ();
        columns.add (new ColumnModel () {
            public String getID () {
                return "sizeID";
            }

            public String getDisplayName () {
                return "size";
            }

            public Class getType () {
                return String.class;
            }
        });
        JComponent ttv = Models.createView (
            tme,              // TreeModel
            tme,              // NodeModel
            tme,              // TableModel
            tme,              // NodeActionsProvider
            columns           // list of ColumnModels
        );
        JFrame f = new JFrame ("Tree Model Example 4");
        f.getContentPane ().add (ttv);
        f.pack ();
        f.show ();
    }    
}
