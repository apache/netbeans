/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
