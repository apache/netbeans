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
