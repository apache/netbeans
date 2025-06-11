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
package org.netbeans.test.permanentUI.utils;

import java.awt.Component;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.JSeparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import org.netbeans.jemmy.operators.JMenuBarOperator;

import org.netbeans.jellytools.MainWindowOperator;

/**
 * @author  lhasik@netbeans.org, mmirilovic@netbeans.org
 */
public class MenuChecker {

    /** Creates a new instance of MenuChecker */
    public MenuChecker() {
    }

    private static final String ALPHABET = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";

    /** Open all menus in menubar
     * @param menu  to be visited */
    public static void visitMenuBar(JMenuBar menu) {
        MenuElement[] elements = menu.getSubElements();

        JMenuBarOperator op = new JMenuBarOperator(menu);

        for (int k = 0; k < elements.length; k++) {
            if (elements[k] instanceof JMenuItem) {
                op.pushMenu(((JMenuItem) elements[k]).getText(), "/", true, true);
                try {
                    op.wait(200);
                } catch (Exception e) {
                }
            }
        }
    }

    /** Get MenuBar and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menubar  - !separator is ignored
     */
    public static ArrayList<NbMenuItem> getMenuBarArrayList(JMenuBar menu) {
 //       System.out.println("getMenuBarArrayList " + menu.getName());
        visitMenuBar(menu);

        MenuElement[] elements = menu.getSubElements();

        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();
        for (int k = 0; k < elements.length; k++) {
            if (elements[k] instanceof JPopupMenu.Separator) {
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (elements[k] instanceof JMenuItem) {

                    NbMenuItem item = new NbMenuItem((JMenuItem) elements[k]);
                    JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                    item.setSubmenu(getMenuArrayList(menuOp.getMenu(k)));
                    list.add(item);
                }
            }
        }
        return list;
    }

    /** Get Menu and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menu  - !separator is ignored
     */
    public static ArrayList<NbMenuItem> getMenuArrayList(JMenu menu) {
//        System.out.println("getMenuArrayList: " + menu.getText());//DEBUG
//        menu.list();//DEBUG
        MenuElement[] elements = menu.getSubElements();
        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();

        for (int k = 0; k < elements.length; k++) {
//            System.out.print("getMenuArrayList: ");
//            ((JComponent) elements[k]).list(System.out);
            if (elements[k] instanceof JSeparator) {
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (elements[k] instanceof JPopupMenu) {
                    list.addAll(getPopupMenuArrayList((JPopupMenu) elements[k]));
                } else {
                    if (elements[k] instanceof JMenuItem) {
                        NbMenuItem item = new NbMenuItem((JMenuItem) elements[k]);
                        item.setName(item.getName());
                        list.add(item);
                    } else {
                        System.out.println("getMenu unknown:" + elements[k].toString());
                    }
                }
            }

        }
        return list;
    }

    /** Get PopupMenu and transfer it to ArrayList.
     * @param popup menu to be tranfered
     * @return transfered menu - !separator is ignored
     */
    public static ArrayList<NbMenuItem> getPopupMenuArrayList(JPopupMenu popup) {
        //System.out.print("getPopupMenuArrayList: "); popup.list(); //DEBUG
        MenuElement[] elements = popup.getSubElements();
        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();

        for (MenuElement menuElement : elements) {
            //System.out.print("getPopupMenuArrayList: ");
//            ((JComponent) menuElement).list();
            if (menuElement instanceof JSeparator) {
                //System.out.println("adding separator");//DEBUG
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (menuElement instanceof JMenu) {
                    NbMenuItem mitem = new NbMenuItem((JMenuItem) menuElement);
                    mitem.setName(mitem.getName());
                    mitem.setSubmenu (getMenuArrayList((JMenu) menuElement));
                    list.add(mitem);
                } else if (menuElement instanceof JMenuItem) //if()
                {
                    if (!((JMenuItem) menuElement).isVisible()) {
                        continue;
                    }
                    NbMenuItem item = new NbMenuItem((JMenuItem) menuElement);
                    item.setName(item.getName());
                    list.add(item);
                } else {
                    System.out.println("getPopup unknown:" + menuElement.toString());
                }
            }
        }
        return list;
    }
    /**
     * 
     * @param component
     * @return all menu items in the menu component
     */
    public static ArrayList<NbMenuItem> getMenuItemsList(JComponent component) {
        Component items[] = component.getComponents();
        
        ArrayList<NbMenuItem> list = new ArrayList<NbMenuItem>();
        
        for (Component menuItem : items) {
            if (menuItem instanceof JSeparator) {
//                System.out.println("adding separator");//DEBUG
                NbMenuItem separator = new NbMenuItem();
                separator.setSeparator(true);
                list.add(separator);
            } else {
                if (menuItem instanceof JMenu) {
                    NbMenuItem mitem = new NbMenuItem((JMenuItem) menuItem);
                    mitem.setName(mitem.getName());
                    mitem.setSubmenu (getMenuItemsList((JComponent)menuItem));
                    list.add(mitem);
                } else if (menuItem instanceof JMenuItem) {//if()                
                    NbMenuItem item = new NbMenuItem((JMenuItem) menuItem);
                    item.setName(item.getName());
                    list.add(item);
                } else {
                    System.out.println("getMenuItemsList unknown:" + menuItem.toString());
                }
            }
        }
        return list;
    }

    public static String checkMnemonicCollision() {
        return checkMnemonicCollision(getMenuBarArrayList(MainWindowOperator.getDefault().getJMenuBar()), "").toString();
    }

    /** Check mnemonics in menu structure.
     * @param list
     * @return  
     */
    private static StringBuffer checkMnemonicCollision(List<NbMenuItem> list, String menuName) {
        StringBuffer collisions = new StringBuffer("");
        HashMap<Character, List<NbMenuItem>> mnemoMap = new HashMap<Character, List<NbMenuItem>>();
        List<NbMenuItem> noMnemoList = new ArrayList<NbMenuItem>();
        boolean printHeader = false;
        String title = menuName.isEmpty() ? "Main Menu" : menuName;

        // build a map of used mnemos
        for (NbMenuItem item : list) {
            if (item.getMnemo() != 0) {
                Character mnemonic = new Character(item.getMnemo());
                List<NbMenuItem> collisionList = mnemoMap.get(mnemonic);
                if (collisionList == null) {
                    collisionList = new ArrayList<NbMenuItem>();
                }
                collisionList.add(item);
                mnemoMap.put(mnemonic, collisionList);
            } else {
                noMnemoList.add(item);
            }
        }
        String[] alpha = ALPHABET.split(",");
        List<String> unusedMnemos = new ArrayList<String>(Arrays.asList(alpha));

        // print collisions
        for (Map.Entry<Character, List<NbMenuItem>> entry : mnemoMap.entrySet()) {
            List<NbMenuItem> collisionList = entry.getValue();
            unusedMnemos.remove(Character.toString(entry.getKey()).toLowerCase());
            if (collisionList.size() > 1) {
                printHeader = true;
                collisions.append("\n\n Mnemonic Collision mnemonic : ").append(entry.getKey());
                collisions.append(", items: ");
                for (NbMenuItem nbMenuItem : collisionList) {
                    collisions.append(nbMenuItem.getName());
                    if (nbMenuItem != collisionList.get(collisionList.size() - 1)) {
                        collisions.append(", ");
                    }
                }

            }
        }

        // print missing mnemos
        if (!noMnemoList.isEmpty()) {
            printHeader = true;
            collisions.append("\n\n No Mnemonic set for: " );
            for (NbMenuItem nbMenuItem : noMnemoList) {
                collisions.append("\n\t").append(nbMenuItem.getName());
                collisions.append(" - suggestions: ").append(getMnemoSuggestion(nbMenuItem.getName(), unusedMnemos));
            }
        }
        if (printHeader) {
            collisions.append("\n Available mnemonics: ");
            for (Iterator<String> it = unusedMnemos.iterator(); it.hasNext();) {
                String mnemo = it.next();
                collisions.append(mnemo);
                if (it.hasNext()) {
                    collisions.append(", ");
                }
            }
            collisions.insert(0, "\n #################### " + title + " menu mnemonic test ####################");
            collisions.insert(0, "\n\n ==============================================================================");
        }

        for (NbMenuItem nbMenuItem : list) {
            if (nbMenuItem.getSubmenu() != null && !nbMenuItem.getSubmenu().isEmpty() && nbMenuItem.isEnabled()) {
                collisions.append(checkMnemonicCollision(nbMenuItem.getSubmenu(), (menuName.isEmpty() ? "" : menuName + "/")  + nbMenuItem.getName()));
            }
        }
        return collisions;
    }

    private static String getMnemoSuggestion(String menuName, List<String> unusedMnemos) {
        ArrayList<String> unusedMnemosCopy = new ArrayList<String>(unusedMnemos);
        String mainSuggestion = "";
        String minorSuggestion = "";
        String[] words = menuName.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) {
                continue;
            }

            String letter = Character.toString(words[i].charAt(0)).toLowerCase();
            if (unusedMnemosCopy.remove(letter)) {
                if (!mainSuggestion.isEmpty()) {
                    mainSuggestion += ", ";
                }
                mainSuggestion += letter.toUpperCase();
            }

        }

        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) {
                continue;
            }
            for (int j = 1; j < words[i].length(); j++) {
                String letter = Character.toString(words[i].charAt(j)).toLowerCase();
                if (unusedMnemosCopy.remove(letter)) {
                    if (!minorSuggestion.isEmpty()) {
                        minorSuggestion += ", ";
                    }
                    minorSuggestion += letter;
                }
            }
        }
        String suggestion = mainSuggestion.isEmpty() ? minorSuggestion :  mainSuggestion + ", " + minorSuggestion;
        return suggestion.isEmpty() ? "(none)" : suggestion;
    }

    public static String checkShortCutCollision() {
        return checkShortCutCollision(getMenuBarArrayList(MainWindowOperator.getDefault().getJMenuBar())).toString();
    }

    /** check shortcuts in menu structure
     * @param a
     * @return  
     */
    private static StringBuffer checkShortCutCollision(ArrayList a) {
        StringBuffer collisions = new StringBuffer("");
        Iterator it = a.iterator();
        HashMap check = new HashMap();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof NbMenuItem) {
                NbMenuItem item = (NbMenuItem) o;

                if (item.getAccelerator() != null) {
                    //stream.println("checking : " + item.name + " - " + item.accelerator);
                    if (check.containsKey(item.getAccelerator())) {
                        collisions.append("\n !!!!!! Collision! accelerator ='" + item.getAccelerator() + "' : " + item.getName() + " is in collision with " + check.get(item.getAccelerator()));
                    } else {
                        check.put(item.getAccelerator(), item.getName());
                    }

                }
            }

            if (o instanceof ArrayList) {
                collisions.append(checkShortCutCollision((ArrayList) o));
            }

        }

        return collisions;
    }

    public static int getElementPosition(String element, MenuElement[] array) {
        int p = -1;
        for (int i = 0; i <
                array.length; i++) {
            if (element.equals(((JMenuItem) array[i]).getText())) {
                return i;
            }

        }
        return p;
    }

}


