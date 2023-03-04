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

package org.util;

import java.util.LinkedList;
import java.util.List;

import org.MyTestCase;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.xml.DomExternalizable;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Danila_Dugurov
 */

public class DomVisitorTest extends MyTestCase {
   
   public void testSimpleVisitor() {
      MyComponent component1 = new MyComponent("one", "leaf", 10);
      MyComponent component2 = new MyComponent("two", "composite", 20);
      MyComponent component3 = new MyComponent("three", "composite", 30);
      MyComponent component4 = new MyComponent("four", "leaf", 40);
      MyComponent component5 = new MyComponent("five", "leaf", 50);
      final List<MyComponent> deserialized = new LinkedList<MyComponent>();
      try {
         final Document document = DomUtil.parseXmlFile("<components/>");
         Element root = document.getDocumentElement();
         
         DomUtil.addChild(root, component1);
         DomUtil.addChild(root, component2);
         DomUtil.addChild(root, component3);
         DomUtil.addChild(root, component4);
         DomUtil.addChild(root, component5);
         
         DomVisitor visitor = new RecursiveDomVisitor() {
            public void visit(Element element) {
               if ("component".equals(element.getNodeName())) {
                  final MyComponent component = new MyComponent();
                  component.readXML(element);
                  deserialized.add(component);
               } else
                  super.visit(element);
            }
         };
         visitor.visit(document);
      } catch(ParseException wontHappend) {
      }
      assertEquals(component1, deserialized.get(0));
      assertEquals(component2, deserialized.get(1));
      assertEquals(component3, deserialized.get(2));
      assertEquals(component4, deserialized.get(3));
      assertEquals(component5, deserialized.get(4));
      System.out.println(component1);
      System.out.println(component2);
      System.out.println(component3);
      System.out.println(component4);
      System.out.println(component5);
   }
}
class MyComponent implements DomExternalizable {
   String name;
   
   String type;
   
   int cost;
   
   public MyComponent(String name, String type, int cost) {
      this.name = name;
      this.type = type;
      this.cost = cost;
   }
   
   public MyComponent() {
      
   }
   
   public void readXML(Element element) {
      final RecursiveDomVisitor visitor = new RecursiveDomVisitor() {
         public void visit(Element element) {
            if ("name".equals(element.getNodeName())) {
               name = element.getTextContent();
            } else if ("type".equals(element.getNodeName())) {
               type = element.getTextContent();
            } else if ("cost".equals(element.getNodeName())) {
               cost = Integer.parseInt(element.getTextContent());
            } else super.visit(element);
         }
      };
      visitor.visit(element);
   }
   
   public Element writeXML(Document document) {
      final Element root = document.createElement("component");
      Element element = document.createElement("name");
      element.setTextContent(name);
      root.appendChild(element);
      element = document.createElement("type");
      element.setTextContent(type);
      root.appendChild(element);
      element = document.createElement("cost");
      element.setTextContent(String.valueOf(cost));
      root.appendChild(element);
      return root;
   }
   
   public String toString() {
      return "name: " + name + " type: " + type + " cost: " + cost;
   }
   
   @Override
   public boolean equals(Object other) {
      if (other == null) return false;
      if (other instanceof MyComponent) {
         final MyComponent component = (MyComponent) other;
         return component.name.equals(this.name) &&
                 component.type.equals(this.type) &&
                 component.cost == this.cost;
      }//TODO: nullPointer he he.
      return false;
   }
   
   @Override
   public int hashCode() {
      return -1;//TODO: he-he
   }
   
}
