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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.test.permanentUI.utils;

import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author Lukas Hasik
 */
public class NbMenuItem implements Comparable {

    private String name = "NONE";
    private char mnemo = 0;
    private String accelerator = null;
    private boolean enabled = false;
    private boolean radiobutton = false;
    private boolean checkbox = false;
    private boolean separator = false;
    private boolean checked = false;
    ArrayList<NbMenuItem> submenu = null;

    public NbMenuItem() {        
    }
    
    public NbMenuItem(String name) {
        this.name = name;
    }

    /**
     * @param it
     * @return instance of NbMenuItem constructed from parameter it */
    public NbMenuItem(JMenuItem it) {
        setName(it.getText());//getLabel();
        this.accelerator = (it.getAccelerator() == null) ? null : it.getAccelerator().toString();
        this.mnemo = (char) it.getMnemonic();
//        System.out.println("NbMenuItem ["+name+"] - mnemo: ["+it.getMnemonic()+"]"); why are the mnemonic always in capital?
        this.enabled = it.isEnabled();
        this.checkbox = it instanceof JCheckBoxMenuItem;
        this.radiobutton = it instanceof JRadioButtonMenuItem;
        this.checked = it.isSelected();
    }


    /** needed for comparing in TreeSet
     * @param obj
     * @return  */
    public int compareTo(Object obj) {
        NbMenuItem n = (NbMenuItem) obj;
        return (getName() != null) ? getName().compareTo(n.getName()) : n.getName().compareTo(getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getMnemo() {
        return mnemo;
    }

    public void setMnemo(char mnemo) {
        this.mnemo = Character.toUpperCase(mnemo); //all the mnemonic returned by JMenuItem.getMnemonic() are upper case
    }

    public String getAccelerator() {
        return accelerator;
    }

    public void setAccelerator(String accelerator) {
        this.accelerator = accelerator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRadiobutton() {
        return radiobutton;
    }

    public void setRadiobutton(boolean radiobutton) {
        this.radiobutton = radiobutton;
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
        setName("==========");
    }

    public ArrayList<NbMenuItem> getSubmenu () {
        return submenu;
    }

    public void setSubmenu(ArrayList<NbMenuItem> submenu) {
        this.submenu = submenu;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean equals(NbMenuItem obj) {
        
        if (this.isSeparator()) {
            return obj.isSeparator();
        } else {
            return (this.getName().equals(obj.getName())) &&
                    (Character.toUpperCase(this.getMnemo()) == Character.toUpperCase(obj.getMnemo()) ) && //TODO: for unknown reason the AbstractButton.getMnemonic() returns always capital letter
//                    (this.getMnemo() == obj.getMnemo()) &&
                    ((this.getSubmenu () != null) && (obj.getSubmenu () != null)) &&
                    //((this.getAccelerator()!= null)?this.getAccelerator().equals(obj.getAccelerator()):false) &&
                    (this.isCheckbox() && obj.isCheckbox()) &&
                    (this.isRadiobutton() && obj.isRadiobutton()) 
                    //&& (this.isEnabled() && obj.isEnabled())
                    ;
        }
    }

    public String findDifference(NbMenuItem obj) {
        String text = "";
        String compar = "diff[" + this.getName() + "], [" + obj.getName() + "] ";

        if ((this.isSeparator() && !obj.isSeparator()) || (!this.isSeparator() && obj.isSeparator())) {
            text = this.isSeparator() ? obj.getName() : this.getName() + " is on the same position as separator";
        } else {
            if (!(this.getName().equals(obj.getName()))) {
                text = ", NAMES differs [" + this.getName() + "], [" + obj.getName() + "]";
            } else {
                if (Character.toUpperCase(this.getMnemo()) != Character.toUpperCase(obj.getMnemo()) ) {//TODO: for unknown reason the AbstarctButton.getMnemonic() returns always capital letter
//                if (this.getMnemo() != obj.getMnemo()) {//TODO: for unknown reason the AbstarctButton.getMnemonic() returns always capital letter
                    text += ", MNEMONICS are NOT same [" + this.getMnemo() + "] != [" + obj.getMnemo() + "]";
                }
                if ((this.getSubmenu () != null) != (obj.getSubmenu () != null)) { //do they both have submenus?
                    text += ", " + (this.getSubmenu () != null ? obj.getName() : this.getName()) + " has NO SUBMENU";
                }
//                if (this.getAccelerator() != null) {
//                    if (!this.getAccelerator() .equals(obj.getAccelerator())) {
//                        text += "ACCELERATORS differ [" + this.getAccelerator() + " != " + obj.getAccelerator() + "]";
//                    }
//                }
                if (!this.isCheckbox() && obj.isCheckbox()) {
                    text += ", " + (this.isCheckbox() ? obj.getName() : this.getName()) + " is NOT CHECKBOX";
                }
                if (!this.isRadiobutton() && obj.isRadiobutton()) {
                    text += ", " + (this.isRadiobutton() ? obj.getName() : this.getName()) + " is NOT RADIOBUTTON";
                }
//                if (!this.isEnabled() && obj.isEnabled()) {
//                    text += ", " + (this.isEnabled() ? obj.getName() : this.getName()) + " is NOT ENABLED";
//                }


            }
        }
        return text.length()==0?text:compar+text+"\n";
    }

    @Override
    public String toString() {
        return this.getName() + " [acc:" + this.getAccelerator() + "] ["
                +this.getMnemo()+"] [e:" + this.isEnabled()+", ch:" + this.isCheckbox() + 
                ", r:"+ this.isRadiobutton()+", checked:"+this.isChecked()+", sep:" + this.isSeparator() +"]" 
                + ((this.getSubmenu() != null)?" SUBMENU":"");
    }
    
    
}
