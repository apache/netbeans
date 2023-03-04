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

package threaddemo.views;

import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.spi.looks.Look;
import org.openide.actions.DeleteAction;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

// XXX could have other operations - insert etc.

/**
 * Look for a DOM Document or one of its elements.
 * @author Jesse Glick
 */
public class ElementLook extends Look<Element> implements EventListener {
    
    private static final Logger logger = Logger.getLogger(ElementLook.class.getName());
    
    public ElementLook() {
        super("ElementLook");
    }
    
    public String getDisplayName() {
        return "DOM Elements";
    }
    
    protected void attachTo(Element e) {
        EventTarget et = (EventTarget) e;
        // Node{Inserted,Removed} is fired *before* the change. This is better;
        // fired *after* it has taken effect.
        et.addEventListener("DOMSubtreeModified", this, false);
    }
    
    protected void detachFrom(Element e) {
        EventTarget et = (EventTarget) e;
        et.removeEventListener("DOMSubtreeModified", this, false);
    }
    
    public void handleEvent(Event evt) {
        // XXX for some reason, sometimes if refactoring is done while an XML phadhail is
        // expanded, some infinite loop occurs and this method is called repeatedly
        try {
            Element parent = (Element)evt.getCurrentTarget();
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "ElementLook: event on {0}: {1}; co={2}", new Object[] {parent.getTagName(), evt, getChildObjects(parent, null)});
            }
            fireChange(parent, Look.GET_CHILD_OBJECTS | Look.GET_DISPLAY_NAME);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    
    public String getName(Element e, Lookup env) {
        return e.getTagName();
    }
    
    public String getDisplayName(Element e, Lookup env) {
        return fullText(e);
    }
    
    private static String fullText(Element el) {
        StringBuffer buf = new StringBuffer();
        buf.append('<');
        buf.append(el.getNodeName());
        NamedNodeMap attrs = el.getAttributes();
        int len = attrs.getLength();
        for (int i = 0; i < len; i++) {
            Attr attr = (Attr)attrs.item(i);
            buf.append(' ');
            buf.append(attr.getName());
            buf.append('=');
            buf.append('"');
            try {
                buf.append(XMLUtil.toAttributeValue(attr.getValue()));
            } catch (CharConversionException e) {
                e.printStackTrace();
            }
            buf.append('"');
        }
        if (el.getElementsByTagName("*").getLength() > 0) {
            // Have some sub-elements.
            buf.append('>');
        } else {
            buf.append("/>");
        }
        return buf.toString();
    }
    
    public boolean isLeaf(Element e, Lookup env) {
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                return false;
            }
        }
        return true;
    }
    
    public List<?> getChildObjects(Element e, Lookup env) {
        NodeList nl = e.getChildNodes();
        List<Element> l = new ArrayList<Element>(Math.max(nl.getLength(), 1));
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                l.add((Element) n);
            }
        }
        return l;
    }
    
    public boolean canDestroy(Element e, Lookup env) {
        return !(e.getParentNode() instanceof Document);
    }
    
    public void destroy(Element e, Lookup env) {
        e.getParentNode().removeChild(e);
    }
    
    public Action[] getActions(Element e, Lookup env) {
        return new Action[] {
            SystemAction.get(DeleteAction.class),
        };
    }
    
}
