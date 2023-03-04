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
