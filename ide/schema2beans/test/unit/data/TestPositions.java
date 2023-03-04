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

/*
 *	TestPositions - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestPositions.xml
 *	file and this java test should be kept in sync.
 *
 * 	Test the following:
 *
 *	single String: get/set/remove/set/get
 *	boolean (from true): get/set true/get/set false/get/set true/get
 *	boolean (from false): get/set false/get/set true/get/set false/get
 *	String[]: get/set (null & !null)/add/remove
 *	Bean: remove/set(null)/create bean and graph of beans/set/add
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import menus.*;


public class TestPositions extends BaseTest {
    public static void main(String[] argv) {
        TestPositions o = new TestPositions();
        if (argv.length > 0)
            o.setDocumentDir(argv[0]);
        try {
            o.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
    
    public void run() throws Exception {
        Menus menus;

        this.readDocument();

        out("creating the bean graph");
        menus = Menus.read(doc);
	
        //	Check that we can read the graph an it is complete
        out("bean graph created");
        menus.write(out);

        out("Check to make sure that elements with the same name get put into the right spot");
        Foo foo = menus.getFoo();
        check(foo.sizeName() == 2, "There are 2 names");
        check("name2".equals(foo.getName2()), "name2 is in the right spot");
        check("name3".equals(foo.getName3()), "name3 is in the right spot");

        out("Adding some colors to the menu.");
        Menu menu = menus.getMenu(0);
        menu.addMenuItem("red");
        menu.addMenuItem("magenta");
        menu.addMenuItem("blue");
        menu.addSeparator(new Separator());
        menu.addMenuItem("cyan");
        menu.addSeparator(new Separator());
        menu.addMenuItem("green");
        menus.write(out);
        check(8 == menu.sizeMenuItem(), "sizeMenuItem="+menu.sizeMenuItem());
        out(menu.getMenuItem());

        out("Replacing menu items with many numbers");
        menu.setMenuItem(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        menus.write(out);

        out("Removing 7");
        menu.removeMenuItem("7");
        menus.write(out);

        out("Replacing menu items with few letters");
        menu.setMenuItem(new String[] {"a", "b", "c"});
        menus.write(out);
    }
}
