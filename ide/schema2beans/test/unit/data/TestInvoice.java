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
 *	TestInvoice - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestInvoice.xml
 *	file and this java test should be kept in sync.
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import invoice.*;


public class TestInvoice extends BaseTest {
    public static void main(String[] argv) {
        TestInvoice o = new TestInvoice();
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
        Invoice invoice;

        this.readDocument();

        out("creating the bean graph");
        invoice = Invoice.read(doc);
	
        //	Check that we can read the graph an it is complete
        out("bean graph created");
        invoice.write(out);

        out("Adding an item");
        Item item = new Item();
        item.setId("123");
        item.setCategory("office");
        item.setQuantity(10);
        item.setPrice(new java.math.BigDecimal("15.99"));
        invoice.addItem(item);
        invoice.write(out);
    }
}
