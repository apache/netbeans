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

/**
 * PropertiesTable.java
 *
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;     


class PropertiesTable extends JTable {

    private static final int margin = 6; 

    private boolean fontChanged = true;
    private int newHeight = 23; 
    
    private static final long serialVersionUID = -346761221423978739L;
    
    PropertiesTable() { 
	 super();
         initComponent();
    }

    void initComponent(){
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    @Override
    public TableCellRenderer getCellRenderer(int row, int col) {
	return super.getCellRenderer(row, col); 
    }
    
    @Override
    public void paint(Graphics g) {

	if (fontChanged) {
	    fontChanged = false; 

	    int height = 0; 
	    FontMetrics fm = g.getFontMetrics(getFont());
	    height = fm.getHeight() + margin;
	    if(height > newHeight) newHeight = height; 
	    this.setRowHeight(newHeight);
	    return;
	}

	super.paint(g);
    }
    
}
