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
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 *	TestPurchaseOrder - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestPurchaseOrder.xml
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

import purchaseorder.*;


public class TestPurchaseOrder extends BaseTest {
    public static void main(String[] argv) {
        TestPurchaseOrder o = new TestPurchaseOrder();
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
        PurchaseOrder po;

        this.readDocument();

        out("creating the bean graph");
        po = PurchaseOrder.read(doc);
	
        //	Check that we can read the graph an it is complete
        out("bean graph created");
        po.write(out);

        out("Adjusting shipTo");
        USAddress shipTo = new USAddress();
        po.setShipTo(shipTo);
        shipTo.setCountry("B&B's are us");
        shipTo.setName("To you & me");
        po.write(out);

        out("Adding a Daffodil");
        Items items = po.getItems();
        Item item = new Item();
        item.setProductName("Daffodil");
        item.setQuantity(3);
        item.setUSPrice(new java.math.BigDecimal("1.00"));
        item.setComment("Yellow & Green");
        int position = items.addItem(item);
        po.write(out);

        check(items.getItem(position) == item, "addItem returned correct position");

        out("Removing that Daffodil");
        items.removeItem(item);
        po.write(out);

        Item[] itemsArray = items.getItem();
        /*
        out(""+itemsArray.length);
        check(itemsArray.length == 2, "we should have 2 items now");
        */
        check(items.sizeItem() == 2, "sizeItem should return 2");

        PurchaseOrder po2 = new PurchaseOrder(po);
        check(po != po2, "po should not be po2");
        check(po.equals(po2), "po should equal po2");
        check(po2.equals(po), "po2 should equal po");
        /*
        po.write(out);
        po2.write(out);
        */

        po._setSchemaLocation("flurp");
        po.write(out);
    }
}
