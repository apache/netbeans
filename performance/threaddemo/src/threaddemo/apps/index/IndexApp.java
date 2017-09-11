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

package threaddemo.apps.index;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import threaddemo.locking.LockAction;
import threaddemo.model.Phadhail;

/**
 * Launcher + GUI class for the index.
 * @author Jesse Glick
 */
public class IndexApp extends JFrame {
    
    private final Index index;
    private final DefaultTableModel tableModel;
    
    public IndexApp(Phadhail root) {
        super("XML Element Index [" + root.getPath() + "]");
        index = new IndexImpl(root);
        index.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                refreshTable();
            }
        });
        tableModel = new DefaultTableModel(0, 2);
        DefaultTableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column = new TableColumn(0);
        column.setHeaderValue("XML Element Name");
        columns.addColumn(column);
        column = new TableColumn(1);
        column.setHeaderValue("Occurrences");
        columns.addColumn(column);
        index.start();
        getContentPane().add(new JScrollPane(new JTable(tableModel, columns)));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                index.cancel();
            }
        });
        pack();
    }
    
    private void refreshTable() {
        final SortedMap<String,Integer> data = index.getLock().read(new LockAction<SortedMap<String,Integer>>() {
            public SortedMap<String,Integer> run() {
                return new TreeMap<String,Integer>(index.getData());
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // XXX clumsy
                int rows = tableModel.getRowCount();
                for (int i = 0; i < rows; i++) {
                    tableModel.removeRow(0);
                }
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    tableModel.addRow(new Object[] {
                        entry.getKey(),
                        entry.getValue(),
                    });
                }
            }
        });
    }
    
}
