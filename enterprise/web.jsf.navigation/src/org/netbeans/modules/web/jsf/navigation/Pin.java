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
 /* PinNode.java
 *
 * Created on March 22, 2007, 3:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class Pin extends PageFlowSceneElement {

    private final Page page;
    private boolean bDefault = true;
    private PageContentItem pageContentItem;

    /** Creates a default PinNode
     * @param page
     */
    public Pin(Page page) {
        super();
        this.page = page;
    }

    /**
     * Create a nondefault pin in a page which represents a page content item.
     * @param page for which the pin belongs.
     * @param pageContentItem
     */
    public Pin(Page page, PageContentItem pageContentItem) {
        this(page);
        assert pageContentItem != null;
        this.pageContentItem = pageContentItem;
        bDefault = false;
    }

    /**
     * Is this a default pin?
     * @return boolean is Default?
     */
    public boolean isDefault() {
        return bDefault;
    }

    @Override
    public String toString() {
        return new String("Pin[pagename=" + page.getDisplayName() + " isDefault=" + isDefault() + "] ");
    }



    /**
     * Is this a default pin?
     * @return Image pageContentItem Image
     */
    public Image getIcon(int type) {
        if (pageContentItem != null) {
            return pageContentItem.getBufferedIcon();
        }
        return null;
    }


    /**
     * Get the name of this pin.  Will return content item name.
     * @return String
     */
    @Override
    public String getName() {
        if (pageContentItem != null) {
            return pageContentItem.getName();
        }
        return null;
    }

    /**
     *
     * @return fromAction String
     */
    public String getFromAction() {
        if (pageContentItem != null) {
            return pageContentItem.getFromAction();
        }
        return null;
    }

    /**
     *
     * @return fromOutcome String
     */
    public String getFromOutcome() {
        if (!bDefault) {
            return pageContentItem.getFromOutcome();
        }
        return null;
    }

    public void setFromOutcome(String fromOutcome) {
        if (pageContentItem != null) {
            pageContentItem.setFromOutcome(fromOutcome);
        }
    }

    public void setFromAction(String fromAction) {
        if (pageContentItem != null) {
            pageContentItem.setFromAction(fromAction);
        }
    }

    /**
     *
     * @return
     */
    public Page getPage() {
        return page;
    }

    public Action[] getActions() {
        if (pageContentItem != null) {
            return pageContentItem.getActions();
        }
        return new Action[]{};
    }
    
    public  <T extends Cookie> T getCookie(Class<T> type) {
        return pageContentItem.getCookie(type);
    }


    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void destroy() throws IOException {
        if (pinNode != null) {
            pinNode.destroy();
        }
    }

    public boolean canDestroy() {
        return false;
    }

    public boolean canRename() {
        return false;
    }


    public Node getNode() {
        if (pinNode == null) {
            pinNode = new PinNode();
        }
        return pinNode;
    }
    
    private final Pin getInstance() {
        return this;
    }

    private Node pinNode;

    private class PinNode extends AbstractNode {
        public PinNode() {
            super(Children.LEAF);
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> type) {
            /* I needed to do this because it seems that the activatedNode requires some sort of DataObject to show things like Windows Title correctly */
            T cookie = getInstance().getCookie(type);
            if( cookie != null ){
                return cookie;
            }
            return page.getCookie(type);
        }


        @Override
        protected Sheet createSheet() {
            Sheet s = Sheet.createDefault();
            Set ss = s.get("general"); // NOI18N
            if (ss == null) {
                ss = new Sheet.Set();
                ss.setName("general"); // NOI18N
                ss.setDisplayName(NbBundle.getMessage(Pin.class, "General")); // NOI18N
                ss.setShortDescription(NbBundle.getMessage(Pin.class, "GeneralHint")); // NOI18N
                s.put(ss);
            }
            Set gs = ss;

            try {
                PropertySupport.Reflection p = new Reflection<String>(pageContentItem, String.class, "getName", "setName"); // NOI18N
                p.setName("fromView"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(Pin.class, "FromView")); // NOI18N
                p.setShortDescription(NbBundle.getMessage(Pin.class, "FromViewHint")); // NOI18N
                ss.put(p);

                p = new Reflection<String>(pageContentItem, String.class, "getFromOutcome", "setFromOutcome"); // NOI18N
                p.setName("fromOutcome"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(Pin.class, "Outcome")); // NOI18N
                p.setShortDescription(NbBundle.getMessage(Pin.class, "OutcomeHint")); // NOI18N
                ss.put(p);
            } catch (NoSuchMethodException nsme) {
                ErrorManager.getDefault().notify(nsme);
            }

            return s;
        }
    }
}
