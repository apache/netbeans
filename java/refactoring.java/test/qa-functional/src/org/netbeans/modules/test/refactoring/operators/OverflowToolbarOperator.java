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

package org.netbeans.modules.test.refactoring.operators;


import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.openide.awt.ToolbarWithOverflow;
import org.openide.util.Exceptions;



/**
 <p>
 @author Standa
 */
public class OverflowToolbarOperator extends ContainerOperator{
	final private ToolbarWithOverflow overflow;
	public OverflowToolbarOperator(ContainerOperator container){
		super(container);
		Component findSubComponent = container.findSubComponent(new ToolbarWithOverflowChooser());
		overflow = (ToolbarWithOverflow) findSubComponent;
	}
	
	public AbstractButton getButton(String tooltip) {
		AbstractButton findJButton = null;
		
		ComponentSearcher mySearcher= new ComponentSearcher(overflow);
		mySearcher.setOutput(getOutput());
		Component myComponent = mySearcher.findComponent(new ToolbarButtonChooser(tooltip,getComparator()), 0);
		if(myComponent != null){
			findJButton = (AbstractButton) myComponent;
		}
		
		if(findJButton!=null) return  findJButton;
		JPopupMenu menu = null;
		try {
			Field f = ToolbarWithOverflow.class.getDeclaredField("popup");
			f.setAccessible(true);
			Object get = f.get(overflow);
			menu = (JPopupMenu) get;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			ex.printStackTrace();
			return null;
		}
		menu.setVisible(true);
		ComponentSearcher searcher= new ComponentSearcher(menu);
		searcher.setOutput(getOutput());
		Component c = searcher.findComponent(new ToolbarButtonChooser(tooltip,getComparator()), 0);		
		return (AbstractButton) c;
	}
	
	private static class ToolbarWithOverflowChooser implements ComponentChooser {

		@Override
		public boolean checkComponent(Component comp){
			return comp instanceof ToolbarWithOverflow;
		}

		@Override
		public String getDescription(){
			return "Overflow Toolbar.";//To change body of generated methods, choose Tools | Templates.
		}
		
	}
	
	/** Chooser which can be used to find a component with given tooltip,
     * for example a toolbar button.
     */
    private static class ToolbarButtonChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public ToolbarButtonChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        @Override
        public boolean checkComponent(Component comp) {
			String Str1 = ((JComponent)comp).getToolTipText();
			String Str2 = buttonTooltip;
			boolean ret = comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        @Override
        public String getDescription() {
            return "Toolbar button with tooltip \""+buttonTooltip+"\".";
        }
    }
	
}
