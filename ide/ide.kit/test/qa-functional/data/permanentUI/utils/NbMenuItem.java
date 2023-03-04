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
