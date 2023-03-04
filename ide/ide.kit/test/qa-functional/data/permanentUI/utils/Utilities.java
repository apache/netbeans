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

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.jemmy.ComponentChooser;

/**
 *
 * @author Lukas Hasik
 */
public class Utilities {

    private static boolean debug = false;
    /* it is used, when there is project name in menu structure. */
    private static String projectName;

    /**
     * reads menus like http://wiki.netbeans.org/MainMenu
     *
     *
     * | View | V (mnemonics) ======== Editors > E Code Folds > C
     * ============================ Web Browser W IDE Log L
     * ============================ Toolbars > T [x] Show Editor Toolbar h [ ]
     * Show Line Numbers S [x] Show Diff Sidebar D [ ] Show Versioning Labels V
     * ============================ Full Screen F
     *
     *
     * @param filename
     * @return parsed menu structure
     */
    public static NbMenuItem readMenuStructureFromFile(String filename) throws IllegalStateException {
        NbMenuItem parsedMenu = new NbMenuItem();
        try {
            //first use a Scanner to get each line
            Scanner scanner = new Scanner(new File(filename));
            //starts "| Item |"
            String menuName = scanner.nextLine();
            if (debug) {
                System.out.println("1: " + menuName);
            }
            int from;
            if ((from = menuName.indexOf("| ")) != -1) {
                try {
                    parsedMenu.setName(menuName.substring(from + "| ".length(), menuName.lastIndexOf(" |")));
                    char mnemo = menuName.substring(menuName.lastIndexOf(" |") + "| ".length()).trim().charAt(0);
                    parsedMenu.setMnemo(Character.isLetter(mnemo) ? mnemo : '-');
                } catch (StringIndexOutOfBoundsException ex) {
                    System.err.println("Wrong format of \"Menu name\":"
                            + "It has to be in following format \"| menuName |\" in GoldenFile.txt");
                    System.err.println("Format of your \"Menu name\" is: \""
                            + menuName + "\" Don't forget for whiteSpaces!!!");
                    System.err.println("It throws exception: " + ex.getLocalizedMessage());
                }
            } else {
                System.out.println("Wrong file: missing header - menu name as | menuName |");
                throw new IllegalStateException("Wrong file: missing header - menu name as | menuName |");
            }
            //skip ====== bellow menu name
            menuName = scanner.nextLine();
            if (debug) {
                System.out.println("2: " + menuName);
            }
            if (!(menuName.matches("^={5,}+\\s*+"))) {
                System.err.println("Wrong file: missing ===== - bellow  menu name");
                throw new IllegalStateException("Wrong file: missing ===== - bellow  menu name");
            }
            //parse the menu items structure
            ArrayList<NbMenuItem> submenu = new ArrayList<NbMenuItem>();
            while (scanner.hasNextLine()) {
                submenu.add(parseMenuLineText(scanner.nextLine()));
            }
            parsedMenu.setSubmenu(submenu);
            scanner.close();
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }

        return parsedMenu;
    }

    public static NbMenuItem readSubmenuStructureFromFile(String filename, String projectName) throws IllegalStateException {
        Utilities.projectName = new String(projectName);
        return readSubmenuStructureFromFile(filename);
    }

    /**
     * reads submenus like http://wiki.netbeans.org/MainMenu
     *
     * Toolbars > [x] Build B [ ] Debug D [x] Edit E [x] File F [ ] Memory M
     * ======================== Small Toolbar Icons S ========================
     * Reset Toolbars R Customize... C
     *
     * @param filename
     * @return parsed submenu structure
     */
    public static NbMenuItem readSubmenuStructureFromFile(String filename) throws IllegalStateException {
        NbMenuItem parsedMenu = new NbMenuItem();
        try {
            //first use a Scanner to get each line
            Scanner scanner = new Scanner(new File(filename));
            //starts "Toolbars                 > [x] Build                  B"
            String submenuName = scanner.nextLine();
            int to;
            if ((to = submenuName.indexOf(">")) != -1) {
                parsedMenu.setName(submenuName.substring(0, to).trim());
            } else {
                throw new IllegalStateException("Wrong file: missing header - submenu name                 > [x] submenu item                  B");
            }

            ArrayList<NbMenuItem> submenu = new ArrayList<NbMenuItem>();
            submenu.add(parseMenuLineText(submenuName.substring(to + 1)));
            while (scanner.hasNextLine()) {
                submenu.add(parseMenuLineText(scanner.nextLine().trim()));
            }
            parsedMenu.setSubmenu(submenu);
            scanner.close();
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }

        return parsedMenu;

    }

    /**
     * Parses menu line like IDE Log L ============================ Toolbars > T
     * [x] Show Editor Toolbar h [ ] Show Line Numbers S (x) Show Diff Sidebar D
     *
     * @param lineText
     * @return parsed menu item from line
     */
    public static NbMenuItem parseMenuLineText(String lineText) {
        //parse line
        Scanner line = new Scanner(lineText);
        NbMenuItem menuitem = new NbMenuItem();
        if (debug) {
            System.out.println("Parsing line: " + line);
        }
        //is it separator? "======="
        if (line.hasNext("^={5,}+\\s*")) { //at least 5x =

            menuitem.setSeparator(true);
        } else {
            //does the line start with ( ?
            String isRadio = line.findInLine("\\(.\\)");
            if (isRadio != null) {
                //System.out.println("parsing radiobutton: " + isRadio);
                menuitem.setRadiobutton(true);
                menuitem.setChecked(isRadio.indexOf("o") != -1);
            } else {
                //does the line start with [ ?
                String isCheck = line.findInLine("\\[.\\]");
                if (isCheck != null) {
                    //System.out.println("parsing checkbox: " + isCheck);
                    menuitem.setCheckbox(true);
                    menuitem.setChecked(isCheck.indexOf("x") != -1);
                }
            }

            //read menu item text
            StringBuffer text = new StringBuffer();
            boolean read = true;
            while (read && line.hasNext()) {
                String partOfText = line.next();
                if (partOfText.length() == 1 && partOfText.charAt(0) != '/') {
                    if (partOfText.charAt(0) == '>') {
                        menuitem.setSubmenu(new ArrayList<NbMenuItem>());
                    } else if (partOfText.charAt(0) == '-') {
                        // There is following project name, which has to be
                        // loaded right now. It is dynamicly changing.
                        partOfText = partOfText + " " + projectName;
                        text.append(partOfText);
                        text.append(" ");
                    } else {
                        //it must be the mnemonic
                        menuitem.setMnemo(partOfText.charAt(0));
                        read = false;
                    }
                } else {
                    text.append(partOfText);
                    text.append(" ");
                }
            }
            menuitem.setName(text.substring(0, text.length() - 1)); //remove the last " "

        }

        return menuitem;
    }

    /**
     * Prints NbMenuItem to the printstream
     *
     * @param out PrintStream
     * @param menu NbMenuItem
     * @param separator
     */
    public static void printMenuStructure(PrintStream out, NbMenuItem menu, String separator, int level) {
        String checked = " ";
        String output = separator;
        if (menu == null) {
            return;
        }
        if (menu.isChecked()) {
            checked = "x";
        }
        if (menu.isRadiobutton()) {
            output += "(" + checked + ") ";
        }
        if (menu.isCheckbox()) {
            output += "[" + checked + "] ";
        }

        output += menu.getName() + " ";
        ArrayList<NbMenuItem> submenu = menu.getSubmenu();
        if (submenu != null) {
            output += "> ";
        }
        if (menu.getMnemo() != 0) {
            output += "   " + menu.getMnemo();
        }
        out.println(output);

        //print submenu
        if (level > 0 && submenu != null) {
            Iterator<NbMenuItem> sIt = submenu.iterator();
            while (sIt.hasNext()) {
                printMenuStructure(out,/*(NbMenuItem)*/ sIt.next(), separator + separator, level - 1);
            }
        }
    }

    /**
     * Parses files like http://wiki.netbeans.org/NewProjectWizard Java Java
     * Application Java Desktop Application Java Class Library Java Project with
     * Existing Sources Java Free-form Project
     *
     * @param filename
     * @return
     */
    public static ArrayList<String> parseFileByLines(String filename) {
        ArrayList<String> textLines = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                textLines.add(trimTextLine(scanner.nextLine()));
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return textLines;
    }

    public static ArrayList<String> parseFileByLinesLeaveSpaces(String filename) {
        ArrayList<String> textLines = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                int spaces = 0;
                while (nextLine.charAt(spaces) == ' ') {
                    spaces++;
                }
                nextLine = nextLine.substring(spaces);
                for (int i = 0; i < spaces / 4; i++) {
                    nextLine = "+-" + nextLine;
                }

//                for(int i = 0; nextLine.charAt(i)==' ';i++){
//                    nextLine = nextLine.substring(4);
//                    nextLine = 
////                    nextLine.replaceFirst("    ", "+-");
////                    System.out.println("replacing");
//                }
                textLines.add(nextLine);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return textLines;
    }

    public static NbMenuItem parseSubTreeByLines(String filename) {
        Hashtable<Integer, NbMenuItem> levelRoots = new Hashtable<Integer, NbMenuItem>();
        NbMenuItem mainNode = new NbMenuItem();
        int actLevel = -1;
        levelRoots.put(new Integer(actLevel), mainNode);
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                int spaces = 0;
                while (nextLine.charAt(spaces) == ' ') {
                    spaces++;
                }
                nextLine = nextLine.substring(spaces).trim();
                NbMenuItem newNode = new NbMenuItem();
                newNode.setName(nextLine);
                actLevel = spaces / 4;//every level is intended
                //NbMenuItem node =
                ArrayList<NbMenuItem> submenu = levelRoots.get(actLevel - 1).getSubmenu();
                if (submenu == null) {
                    submenu = new ArrayList<NbMenuItem>();
                }
                submenu.add(newNode);
                levelRoots.get(actLevel - 1).setSubmenu(submenu);//set new submenu with the new node in it

                levelRoots.put(actLevel, newNode);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mainNode;
    }

    /**
     * trims unnecessary spaces from text line
     *
     * @param line
     * @return
     */
    public static String trimTextLine(String line) {
        StringBuffer trimmedText = new StringBuffer();
        String words[] = line.split(" ");
        Scanner lineScan = new Scanner(line);
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                trimmedText.append(words[i]);
                trimmedText.append(" ");
            }
        }
        int trimmedTextLengt = trimmedText.length();
        if (trimmedTextLengt > 0) {
            trimmedTextLengt--; //remove the last space if line is not empty
        }
        return trimmedText.substring(0, trimmedTextLengt);

    }

    /**
     * Compares two NbMenuItems. Return a String with description of all the
     * differences
     *
     * @param menuOrigin
     * @param menuCompare
     * @param submenuLevel
     * @return
     */
    public static String compareNbMenuItems(NbMenuItem menuOrigin, NbMenuItem menuCompare, int submenuLevel) {
        String returnText = "";
        System.out.println(menuOrigin.toString() + " comparing with \n" + menuCompare); //DEBUG
        if (!menuOrigin.equals(menuCompare)) {
            returnText = menuOrigin.findDifference(menuCompare);
        }
        if (submenuLevel > 0) {
            if ((menuOrigin.getSubmenu() != null) && (menuCompare.getSubmenu() != null)) {
                //lets traverse thought the submenu
                Iterator<NbMenuItem> itOrigin = menuOrigin.getSubmenu().iterator();
                Iterator<NbMenuItem> itCompare = menuCompare.getSubmenu().iterator();
                NbMenuItem originItem = null;
                NbMenuItem compareItem = null;
                while (itOrigin.hasNext() || itCompare.hasNext()) {
                    if (itOrigin.hasNext()) {
                        if (itCompare.hasNext()) { //both items exist
                            originItem = itOrigin.next();
                            compareItem = itCompare.next();
                            returnText += compareNbMenuItems(originItem, compareItem, submenuLevel - 1);
                        } else { //compareItem doesn't exist
                            originItem = itOrigin.next();
                            returnText += originItem.getName() + " should NOT be in the menu. [" + originItem.toString() + "] \n";
                        }
                    } else {
                        if (itCompare.hasNext()) {//originItem doesn't exist
                            compareItem = itCompare.next();
                            returnText += compareItem.getName() + " is missing in the menu. [" + compareItem.toString() + "] \n";
                        } else {
                            returnText += "BOTH ITEMS ARE NULL. THIS STATE SHOULDN'T HAPPEN";
                        }
                    }

                }
            }
        }
        return returnText;
    }

    /**
     * Filter out all NbMenuItem.separators from array
     *
     * @param array
     * @return
     */
    public static ArrayList<NbMenuItem> filterOutSeparators(ArrayList<NbMenuItem> array) {
        ArrayList<NbMenuItem> newArray = new ArrayList<NbMenuItem>();
        for (Iterator<NbMenuItem> it = array.iterator(); it.hasNext();) {
            NbMenuItem nbMenuItem = it.next();
            if (!nbMenuItem.isSeparator()) { //it is not separator {
                newArray.add(nbMenuItem);
//                System.out.println("adding - " + nbMenuItem.getName()); //DEBUG
            }
        }
        return newArray;
    }

    public static NbMenuItem getMenuByName(String menuName, NbMenuItem aMenu) {
        //browse throught the menu
        if (menuName.equals(aMenu.getName())) {
            return aMenu;
        }
        if (aMenu.getSubmenu() != null) { //recursively for submenu

            Iterator<NbMenuItem> aMenuIt = aMenu.getSubmenu().iterator();
            while (aMenuIt.hasNext()) {
                NbMenuItem ret = getMenuByName(menuName, aMenuIt.next());
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    public static String readFileToString(String filename) {
        if (!(new File(filename).exists())) {
            return "file " + filename + " is empty";
        }
        FileInputStream fis = null;
        byte[] b = null;

        try {
            fis = new FileInputStream(filename);

            int x = fis.available();

            b = new byte[x];

            fis.read(b);
        } catch (IOException ex) {
            System.out.println("problems with diff file - nothing with the test");
            ex.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                System.out.println("just closing the diff file - nothing with the test");
                ex.printStackTrace();
            }
        }

        return new String(b);
    }

    public static ArrayList<Component> findComponentsInContainer(Container cont, ComponentChooser chooser, boolean recursive) {
        Component[] components = cont.getComponents();
        ArrayList<Component> results = new ArrayList<Component>();
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                if (chooser.checkComponent(components[i])) {
                    results.add(components[i]);
                    //System.out.println("Added :"+components[i].toString());
                }
                if (recursive && components[i] instanceof Container) {
                    ArrayList<Component> aa = findComponentsInContainer((Container) components[i], chooser, recursive);
                    //System.out.println("adding all " + aa);
                    results.addAll(aa);
                }
            }
        }

        return results;
    }
}
