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

package org.netbeans.installer.downloader.impl;

import org.netbeans.installer.downloader.Pumping.Section;


import java.util.Collections;
import java.util.List;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.xml.DomExternalizable;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Danila_Dugurov
 */
public class SectionImpl implements Section, DomExternalizable {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Instance
  protected long start;
  protected long length;
  protected long offset;
  private PumpingImpl owner;
  
  protected SectionImpl(PumpingImpl owner, long start, long length) {
    this.owner = owner;
    this.start = start;
    this.length = length;
    this.offset = start;
  }
  
  //before readXML
  protected SectionImpl(PumpingImpl owner) {
    this.owner = owner;
  }
  
  public Pair<Long, Long> getRange() {
    return Pair.create(start, start + length);
  }
  
  public long offset() {
    return offset;
  }
  
  public long length() {
    return length;
  }
  
  public long start() {
    return start;
  }
  
  public void shiftOffset(long delta) {
    offset += delta;
    owner.fireChanges("pumpingUpdate");
  }
  
  public List<Pair<String, String>> headers() {
    if (owner.acceptBytes) {
      if (length > 0) {
        final long end = start + length - 1;
        return Collections.singletonList(Pair.create("Range", "bytes=" + offset + "-" + end));
      } else if (length == -1) {
        return Collections.singletonList(Pair.create("Range", "bytes=" + offset + "-"));
      }
    } else {
      offset = start;
    }
    return Collections.emptyList();
  }
  
  public void readXML(Element element) {
    final DomVisitor visitor = new RecursiveDomVisitor() {
      public void visit(Element element) {
        final String name = element.getNodeName();
        if ("start".equals(name)) {
          start = Long.valueOf(element.getTextContent());
        } else if ("length".equals(name)) {
          length = Long.valueOf(element.getTextContent());
        } else if ("offset".equals(name)) {
          offset = Long.valueOf(element.getTextContent());
        } else
          super.visit(element);
      }
    };
    visitor.visit(element);
  }
  
  public Element writeXML(Document document) {
    final Element root = document.createElement("section");
    DomUtil.addElement(root, "start", String.valueOf(start));
    DomUtil.addElement(root, "length", String.valueOf(length));
    DomUtil.addElement(root, "offset", String.valueOf(offset));
    return root;
  }
}
