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

package gui.windowsystem;

import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;

/**
 * @author  lhasik@netbeans.org, mmirilovic@netbeans.org
 */
public class MenuChecker {

    /** Open all menus in menubar
     * @param menu  to be visited */
    private static void visitMenuBar(JMenuBar menu) {
        JMenuBarOperator op = new JMenuBarOperator(menu);
        for (MenuElement element : menu.getSubElements()) {
            if (element instanceof JMenuItem) {
                op.pushMenu(op.parseString(((JMenuItem) element).getText(), "/"), new DefaultStringComparator(true, true));
                try {
                    op.wait(200);
                }catch(Exception e) {}
            }
        }
    }

    /** Get MenuBar and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menubar */
    private static List<NbMenu> getMenuBarArrayList(JMenuBar menu) {
        visitMenuBar(menu);

        MenuElement [] elements = menu.getSubElements();

        List<NbMenu> list = new ArrayList<NbMenu>();
        for(int k=0; k < elements.length; k++) {
//            if(elements[k] instanceof JMenuItem) {
//                list.add(new NbMenu((JMenuItem)elements[k], null));
                JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                JMenu item = menuOp.getMenu(k);
                list.add(new NbMenu(item, getMenuArrayList(item)));
//            }
            /*if(elements[k] instanceof JMenuBar) {
                JMenuBarOperator menuOp = new JMenuBarOperator(menu);
                list.add(getMenuArrayList(menuOp.getMenu(0)));
            }
             */
        }
        return list;
    }

    /** Get Menu and tranfer it to ArrayList.
     * @param menu menu to be tranfered
     * @return tranfered menu */
    private static List<NbMenu> getMenuArrayList(JMenu menu) {
        MenuElement [] elements = menu.getSubElements();
        List<NbMenu> list = new ArrayList<NbMenu>();

        for(int k=0; k < elements.length; k++) {

            if (elements[k] instanceof JPopupMenu) {
                JPopupMenu item = (JPopupMenu) elements[k];
                list.add(new NbMenu(item, getPopupMenuArrayList(item)));
            }

            if (elements[k] instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) elements[k];
                list.add(new NbMenu(item, null));
            }

        }
        return list;
    }

    /** Get PopupMenu and transfer it to ArrayList.
     * @param popup menu to be tranfered
     * @return transfered menu */
    private static List<NbMenu> getPopupMenuArrayList(JPopupMenu popup) {
        MenuElement [] elements = popup.getSubElements();
        List<NbMenu> list = new ArrayList<NbMenu>();

        for(int k=0; k < elements.length; k++) {
            if (elements[k] instanceof JMenu) {
                JMenu item = (JMenu) elements[k];
                list.add(new NbMenu(item, getMenuArrayList(item)));
            } else if (elements[k] instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) elements[k];
                list.add(new NbMenu(item, null));
            }
        }
        return list;
    }

    private static NbMenu menuBar() {
        JMenuBar bar = MainWindowOperator.getDefault().getJMenuBar();
        return new NbMenu(bar, getMenuBarArrayList(bar));
    }

    private static String subprefix(String prefix, NbMenu item) {
        return item.name == null ? prefix : prefix + "[" + item.name + "] ";
    }

    public static String checkMnemonicCollision() {
        StringBuilder collisions = new StringBuilder("");
        NbMenu all = menuBar();
        //System.err.println(all);
        checkMnemonicCollision(all, "", collisions);
        return collisions.toString();
    }

    /** Check mnemonics in menu structure. */
    private static void checkMnemonicCollision(NbMenu menu, String prefix, StringBuilder collisions) {
        if (menu.submenu == null) {
            return;
        }
        Map<Integer,String> check = new HashMap<Integer,String>();
        for (NbMenu item : menu.submenu) {
            if (item.mnemo != 0) {
                if (check.containsKey(item.mnemo)) {
                    char k = (char) item.mnemo;
                    collisions.append("\n" + prefix + "mnemonic='" + k +  "' : " + item.name + " collides with " + check.get(item.mnemo));
                } else {
                    check.put(item.mnemo, item.name);
                }
            }
            checkMnemonicCollision(item, subprefix(prefix, item), collisions);
        }
    }

    public static String checkShortCutCollision() {
        StringBuilder collisions = new StringBuilder("");
        checkShortCutCollision(menuBar(), "", collisions);
        return collisions.toString();
    }

    /** check shortcuts in menu structure */
    private static void checkShortCutCollision(NbMenu menu, String prefix, StringBuilder collisions) {
        if (menu.submenu == null) {
            return;
        }
        Map<String,String> check = new HashMap<String,String>();
        for (NbMenu item : menu.submenu) {
            if (item.accelerator != null) {
                if (check.containsKey(item.accelerator)) {
                    collisions.append("\n" + prefix + "accelerator ='" + item.accelerator +  "' : " + item.name + " collides with " + check.get(item.accelerator));
                } else {
                    check.put(item.accelerator, item.name);
                }
            }
            checkShortCutCollision(item, subprefix(prefix, item), collisions);
        }
    }

    private static class NbMenu {
        /** label of menuitem */
        final String name;
        /** mnemonic in int */
        final int mnemo;
        /** jasne ? */
        final String accelerator;
        final List<NbMenu> submenu;

        NbMenu(JMenuItem menu, List<NbMenu> submenu) {
            name = menu.getText();//getLabel();
            accelerator = (menu.getAccelerator() == null) ? null : menu.getAccelerator().toString();
            mnemo = menu.getMnemonic();
            this.submenu = submenu;
        }

        NbMenu(JPopupMenu menu, List<NbMenu> submenu) {
            name = menu.getLabel();
            accelerator = null;
            mnemo = 0;
            this.submenu = submenu;
        }

        NbMenu(JMenuBar menubar, List<NbMenu> submenu) {
            name = "menubar";
            accelerator = null;
            mnemo = 0;
            this.submenu = submenu;
        }

        public @Override String toString() {
            return submenu == null ? name : name + submenu;
        }

    }

}
