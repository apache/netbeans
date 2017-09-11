/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
