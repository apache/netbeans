/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.actions.simple;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.actions.spi.ActionProvider;
import org.netbeans.actions.spi.ContainerProvider;
import org.openide.xml.XMLUtil;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderAdapter;

/** A quick n dirty, really ugly class that parses the XML file 
 *  and builds a bunch
 * of hashmaps with the results.
 */

public class Interpreter implements org.xml.sax.DocumentHandler {
    private URL url;
    private boolean parsed = false;
    /** Create a new instance of NBTheme */
    public Interpreter(URL url) {
        this.url = url;
    }
    
    private void ensureParsed() {
        if (!parsed) {
            parse();
            parsed = true;
        }
    }

    
    private void parse(){
        try{
            Parser p = new XMLReaderAdapter(XMLUtil.createXMLReader());
            p.setDocumentHandler(this);
            String externalForm = url.toExternalForm();
            InputSource is = new InputSource(externalForm);
            try {
                p.parse(is);
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                if (npe.getCause() != null) {
                    npe.getCause().printStackTrace();
                }
            }
        }
        catch(java.io.IOException ie){
            ie.printStackTrace();
        }
        catch(org.xml.sax.SAXException se){
            se.printStackTrace();
        }
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
    }
    
    public void startDocument() throws org.xml.sax.SAXException {
    }
    
    private static final String ACTIONSET = "actionset";
    private static final String CONTAINER = "container";
    private static final String ACTION = "action";
    private static final String CONSTRAINT = "constraint";
    private static final String KEY = "key";
    private boolean inActionSet = false;
    private boolean inContainer = false;
    private String currContainer = null;
    private String currAction = null;
    private boolean inAction = false;
    private boolean inConstraint = false;
    private boolean inInvoker = false;
    private String currInvoker = null;
    private String currConstraint = null;
    private static final String INVOKER = "invoker";
    private boolean inItem = false;
    private static final String ITEM = "item";
    boolean inKey = false;
    private String currItem = null;
    
    private boolean inKeystroke = false;
    private Map keystrokesToActions = new HashMap();
    
    private Map containersToItems = new HashMap();
    private Map constraintsToKeys = new HashMap();
    private Map actionsToConstraints = new HashMap();
    private Set toolbarContainers = new HashSet();
    private Set menuContainers = new HashSet();
    private Map constraints = new HashMap();
    private Map invokers = new HashMap();
    
    public String[] getContainerNames() {
        ensureParsed();
        String[] result = new String[containersToItems.size()];
        result = (String[]) containersToItems.keySet().toArray(result);
        return result;
    }
    
    public String[] getActionNames(String containerCtx) {
        ensureParsed();
        List l = (List) containersToItems.get(containerCtx);
        if (l == null) {
            throw new IllegalArgumentException ("Not a known container ctx: " + containerCtx);
        }
        String[] result = new String [l.size()];
        result = (String[]) l.toArray(result);
        return result;
    }
    
    public boolean contextContainsAction(String containerCtx, String action) {
        return Arrays.asList(getActionNames(containerCtx)).contains(action);
    }
    
    public int getState (String action, Map context) {
        ensureParsed();
        List l = (List) actionsToConstraints.get(action);
        if (l == null) {
            return ActionProvider.STATE_ENABLED | ActionProvider.STATE_VISIBLE;
        }
        Iterator i = l.iterator();
        
        boolean enabled = true;
        boolean visible = true;
        
        while (i.hasNext()) {
            Object o = null;
            SimpleConstraint c = null;
            try {
                o = i.next();
                c = (SimpleConstraint) constraints.get(o);
            } catch (ClassCastException cce) {
                throw new ClassCastException ("Looking for SimpleConstraint but got " + o);
            }
            if (c.isEnabledType()) {
                enabled &= c.test(context);
            } else {
                visible &= c.test(context); 
            }
            if (!enabled && !visible) {
                break;
            }
        }
        int result = 0;
        if (enabled) result |= ActionProvider.STATE_ENABLED;
        if (visible) result |= ActionProvider.STATE_VISIBLE;
        return result;
    }
    
    
    private void addItemToContainer(String container, String item) {
        List l = (List) containersToItems.get(container);
        if (l == null) {
            l = new ArrayList ();
            containersToItems.put(container, l);
        }
        l.add(item);
    }
    
    private void addKeyToConstraint (String constraint, SimpleKey key) {
        List l = (List) constraintsToKeys.get(constraint);
        if (l == null) {
            l = new ArrayList ();
            constraintsToKeys.put(constraint, l);
        }
        l.add(key);
    }
    
    public SimpleInvoker getInvoker (String action) {
        String invoker = (String) actionsToInvokers.get(action);
        SimpleInvoker result = (SimpleInvoker) invokers.get(invoker);
        return result;
    }
    
    private HashMap actionsToInvokers = new HashMap();
    private void addInvokerToAction (String action, String invoker) throws SAXException {
        String inv = (String) actionsToInvokers.get(action);
        if (inv == null) {
            actionsToInvokers.put(action, invoker);
        } else {
            throw new SAXException ("Attempt to redefine invoker " + inv + " on " + action + " with " + invoker);
        }
    }    
    
    private void addConstraintsToAction (String action, String constraints) {
        String[] cns;
        if (constraints.indexOf(',') != -1) {
            StringTokenizer tk = new StringTokenizer(constraints, ",");
            cns = new String[tk.countTokens()];
            int i=0;
            while (tk.hasMoreTokens()) {
                cns[i] = tk.nextToken();
                i++;
            }
        } else {
            cns = new String[] {constraints};
        }
        List l = (List) actionsToConstraints.get(action);
        if (l == null) {
            l = new ArrayList ();
            actionsToConstraints.put(action, l);
        }
        l.addAll(Arrays.asList(cns));
    }
    
    private void addKeyStrokeToAction (String action, AttributeList l) throws SAXException {
        KeyStroke ks = attsToKeystroke(l);
        keystrokesToActions.put(ks, action);
        actionsToKeystrokes.put (action, ks);
    }
    
    private void addIconToAction (String action, String imgpath) {
        imagesToIcons.put (action, imgpath);
    }
    
    public Icon getIconForAction (String action) {
        String partialPath = (String) imagesToIcons.get(action);
        if (partialPath != null) {
            String s = url.toExternalForm();
            int idx = s.lastIndexOf("/");
            String urlString = s.substring(0, idx) + "/" + partialPath;
            try {
                URL url = new URL (urlString);
                return new ImageIcon(Toolkit.getDefaultToolkit().getImage(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private HashMap imagesToIcons = new HashMap();
    
    public KeyStroke[] getAllKeystrokes() {
        KeyStroke[] result = new KeyStroke[keystrokesToActions.size()];
        result = (KeyStroke[]) keystrokesToActions.keySet().toArray(result);
        return result;
    }
    
    public String[] getAllActionsBoundToKeystrokes() {
        String[] result = new String[actionsToKeystrokes.size()];
        result = (String[]) actionsToKeystrokes.keySet().toArray(result);
        return result;
    }
    
    public KeyStroke getKeyStrokeForAction(String action) {
        return (KeyStroke) actionsToKeystrokes.get(action);
    }
    
    public String getActionForKeystroke (KeyStroke stroke) {
        return (String) keystrokesToActions.get(stroke);
    }
    
    private Map actionsToKeystrokes = new HashMap();
    
    String KATT_MODIFIERS="modifiers";
    String KATT_KEY="key";
    String KATT_TYPEDKEY="typedkey";
    String KVAL_DEFAULTMOD = "defaultAccelerator";
    private KeyStroke attsToKeystroke(AttributeList l) throws SAXException {
        String modifiers = l.getValue(KATT_MODIFIERS);
        String key = l.getValue(KATT_KEY);
        String typedKey = l.getValue(KATT_TYPEDKEY);
        
        StringBuffer sb = new StringBuffer();
        if (typedKey != null) {
            sb.append ("typed ");
            sb.append (typedKey);
        } else {
            if (modifiers != null) {
                int dmod = modifiers.indexOf (KVAL_DEFAULTMOD);
                if (dmod != -1) {
                    StringBuffer rep = new StringBuffer(modifiers);
                    rep.replace (dmod, KVAL_DEFAULTMOD.length(), getDefaultModifiersString());
                    modifiers = rep.toString();
                }
                
                sb.append (modifiers);
                sb.append (' ');
            }
            sb.append (key);
        }
        
        KeyStroke result = KeyStroke.getKeyStroke(sb.toString());
        if (result == null) {
            throw new SAXException("Misdefined keystroke:" + sb.toString());
        }
        return result;
    }
        
        
    private String getDefaultModifiersString() {
        if (defaultModsText == null) {
            int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            String toConvert = KeyEvent.getKeyModifiersText(mask).toLowerCase();
            char[] chars = toConvert.toCharArray();
            for (int i=0; i < chars.length; i++) {
                if (chars[i] == '+') {
                    chars[i] = ' ';
                }
            }
            defaultModsText = new String(chars);
            int cidx = toConvert.indexOf("command");
            //Replace apple's meaningless "command" with meta
            if (cidx != -1) {
                StringBuffer sb = new StringBuffer(defaultModsText);
                sb.replace (cidx, cidx + 7, "meta");
                defaultModsText = sb.toString();
            }
            
        }
        return defaultModsText;
    }
    
    private String defaultModsText = null;

    
    private static final String MENU_TYPE="menu";
    private static final String TOOLBAR_TYPE="toolbar";
    private static final String KEYSTROKE = "keystroke";
    
    private String currMode = null;
    private String currType = null;
    
    String[] menus = null;
    public String[] getMenus() {
        ensureParsed();
        if (menus == null) {
            ArrayList al = new ArrayList();
            Iterator i = menuContainers.iterator();
            while (i.hasNext()) {
                String s = (String) i.next();
                if (!ContainerProvider.CONTEXTMENU_CONTEXT.equals(s)) {
                    al.add (s);
                }
            }
            menus = new String[al.size()];
            menus = (String[]) al.toArray(menus);
        }
        return menus;
    }

    public String[] getToolbars() {
        ensureParsed();
        String[] result = new String[toolbarContainers.size()];
        result = (String[]) toolbarContainers.toArray(result);
        return result;
    }
    private static final String CONTEXTMENU_TYPE = "contextmenu";
    
    public void startElement(String tag, AttributeList atts) throws SAXException {

        if (ACTIONSET.equals(tag)) {
            inActionSet = true;
        }
        
        if (CONTAINER.equals(tag)) {
            inContainer = true;
            currContainer = findName(atts);
            String type = findType(atts);
            if (MENU_TYPE.equals(type)) {
                menuContainers.add(currContainer);
            } else if (TOOLBAR_TYPE.equals(type)) {
                toolbarContainers.add(currContainer);
            } else if (CONTEXTMENU_TYPE.equals(type)) {
                menuContainers.add(currContainer);
            } else {
                throw new SAXException ("Unknown container type: " + type);
            }
        }

        if (ITEM.equals(tag)) {
            inItem = true;
            if (!inContainer) {
                throw new SAXException ("Item declared outside of container context " + atts);
            }
            currItem = findName(atts);
            addItemToContainer (currContainer, currItem);
        }
        
        if (ACTION.equals(tag)) {
            inAction = true;
            if (inContainer || inKey || inInvoker || inConstraint) {
                throw new SAXException ("Cannot create an action here");
            }
            currAction = findName(atts);
            String constraints = findConstraints(atts);
            if (constraints != null) {
                addConstraintsToAction(currAction, constraints);
            }
            
            String icon = findIcon (atts);
            if (icon != null) {
                addIconToAction (currAction, icon);
            }
            String invoker = findInvoker(atts);
            addInvokerToAction(currAction, invoker);
        }
        
        if (CONSTRAINT.equals(tag)) {
            inConstraint = true;
            currKeys = new HashMap();
            currConstraint = findName(atts);
            currType = findType(atts);
        }
        
        if (KEY.equals(tag)) {
            if (!inConstraint) {
                throw new SAXException ("Keys can only be defined inside constraints");
            }
            inKey = true;
            currMode = findMode(atts);
            currMethod = findKeyMethod(atts);
            currClass = findKeyClass(atts);
            currValue = findKeyValue(atts);
            currMatch = findMatch(atts);
            currMustContain = currMode == null ? true : "mustcontain".equals(currMode);
            if (currValue == null) {
                throw new SAXException ("Key must always have a value attribute");
            }
            //addKeyToConstraint (currConstraint, value);
        }
        
        if (KEYSTROKE.equals(tag)) {
            inKeystroke = true;
            String act = atts.getValue("action");
            if (act == null) {
                throw new IllegalArgumentException ("keystroke must map to an action");
            }
            addKeyStrokeToAction(act, atts);
        }
        
        if (INVOKER.equals(tag)) {
            inInvoker = true;
            currInvoker = findName(atts);
            String type = findType(atts);
            lastInvokerMethod = findMethod(atts);
            lastInvokerClass = findClass(atts);
            lastInvokerWasDirect = ITYPE_DIRECT.equals(type);
        }
    }
    
    private boolean currMustContain = false;
    private String currMatch = null;
    private String currMethod = null;
    private String currClass = null;
    private String currValue = null;
    private Map currKeys = null;
    private static final String ITYPE_DIRECT = "direct";
    private String lastInvokerMethod = null;
    private String lastInvokerClass = null;
    private boolean lastInvokerWasDirect = true;
    
    public void endElement(java.lang.String tag) throws org.xml.sax.SAXException {

        if (ACTIONSET.equals(tag)) {
            inActionSet = false;
        }
        
        if (CONTAINER.equals(tag)) {
            inContainer = false;
            currContainer = null;
        }

        if (ITEM.equals(tag)) {
            inItem = false;
            currItem = null;
        }
        
        if (ACTION.equals(tag)) {
            inAction = false;
            currAction = null;
        }
        
        if (CONSTRAINT.equals(tag)) {
            boolean enabledType = currType == null || CTYPE_ENABLED.equals(currType);
            
            List l = (List) constraintsToKeys.get(currConstraint);
            if (l == null) {
                throw new SAXException ("Constraint " + currConstraint + " defines no keys"); 
            }
            SimpleKey[] keys = new SimpleKey[l.size()];
            keys = (SimpleKey[]) l.toArray(keys);
            ArrayList includes = new ArrayList(keys.length);
            ArrayList excludes = new ArrayList(keys.length);
            for (int i=0; i < keys.length; i++) {
                if (keys[i].isMustContain()) {
                    includes.add (keys[i]);
                } else {
                    excludes.add (keys[i]);
                }
            }
            SimpleKey[] incs = new SimpleKey [includes.size()];
            SimpleKey[] excs = new SimpleKey [excludes.size()];
            incs = (SimpleKey[]) includes.toArray(incs);
            excs = (SimpleKey[]) excludes.toArray(excs);
            
            constraints.put (currConstraint, new SimpleConstraint(currConstraint, incs, excs, enabledType));
            
            inConstraint = false;
            currConstraint = null;
            currKeys = null;
            currType = null;
            currMatch = null;
        }
        
        if (KEY.equals(tag)) {
            boolean exclude = MODE_EXCLUSIVE.equals(currMode);
            /*
            currMode = findMode(atts);
            currType = findType(atts);
            currMethod = findKeyMethod(atts);
            currClass = findKeyClass(atts);
            currValue = findKeyValue(atts);            
             */
            SimpleKey key = new SimpleKey (currValue, currMethod, currClass, currMustContain, currMatch);
            addKeyToConstraint (currConstraint, key);
            currMode = null;
            currType = null;
            currMethod = null;
            currClass = null;
            currValue = null;
            inKey = false;
        }
        
        if (INVOKER.equals(tag)) {
            SimpleInvoker inv = new SimpleInvoker (currInvoker, lastInvokerClass, 
                lastInvokerMethod, lastInvokerWasDirect);
            invokers.put(inv.getName(), inv);
            inInvoker = false;
            currInvoker = null;
        }
    }    

    private static final String MODE_EXCLUSIVE="mustnotcontain";
    private static final String CTYPE_ENABLED="enabled";
    
    private static final String ATT_NAME="name";
    private static final String ATT_TYPE="type";
    private static final String ATT_CONSTRAINTS="constraints";
    private String findName(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_NAME);
        if (result != null) {
            return result.intern();
        } else {
            throw new SAXException ("No name supplied");
        }
    }
    
    private static final String ATT_ICON="icon";
    private String findIcon (AttributeList l) throws SAXException {
        String result = l.getValue(ATT_ICON);
        if (result != null) {
            return result.intern();
        }
        return null;
    }
    
    

    private static final String ATT_METHOD = "method";
    private static final String ATT_CLASS = "class";
    private String findMethod(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_METHOD);
        if (result != null) {
            return result.intern();
        } else {
            throw new SAXException ("No method supplied");
        }
    }
    
    private static final String ATT_MATCH="match";
    private String findMatch(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_MATCH);
        if (result != null) {
            return result.intern();
        } 
        return null;
    }    
    
    private String findKeyMethod(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_METHOD);
        if (result != null) {
            return result.intern();
        } 
        return null;
    }

    private String findKeyClass(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_CLASS);
        if (result != null) {
            return result.intern();
        }
        return null;
    }
    

    private String findClass(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_CLASS);
        if (result != null) {
            return result.intern();
        } else {
            throw new SAXException ("No class supplied");
        }
    }
    
    
    private String findType(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_TYPE);
        if (result != null) {
            return result.intern();
        } else {
            return null;
        }
    }

    private String findConstraints(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_CONSTRAINTS);
        if (result != null) {
            return result.intern();
        } else {
            return null;
        }
    }
    private static final String ATT_VALUE="value";
    private String findKeyValue(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_VALUE);
        if (result != null) {
            return result.intern();
        } else {
            throw new SAXException ("Key must have a value");
        }
    }    
    
    private static final String ATT_INVOKER="invoker";
    private String findInvoker(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_INVOKER);
        if (result != null) {
            return result.intern();
        } else {
            throw new SAXException ("Action must have an invoker");
        }
    }
    
    private static final String ATT_MODE="mode";
    private String findMode(AttributeList l) throws SAXException {
        String result = l.getValue(ATT_MODE);
        if (result != null) {
            return result.intern();
        } 
        return null;
    }
    
    public void characters(char[] p1,int p2,int p3) throws org.xml.sax.SAXException {
    }
    
    public void setDocumentLocator(org.xml.sax.Locator p1) {
    }
    
    public void ignorableWhitespace(char[] p1,int p2,int p3) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(java.lang.String p1,java.lang.String p2) throws org.xml.sax.SAXException {
    }

    
}
