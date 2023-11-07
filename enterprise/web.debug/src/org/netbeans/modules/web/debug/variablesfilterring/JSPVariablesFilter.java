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

package org.netbeans.modules.web.debug.variablesfilterring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 *
 * @author Libor Kotouc
 */
public class JSPVariablesFilter implements TreeModelFilter {
    
    private static final boolean verbose = true;
    
    
    /** Creates a new instance of JSPVariablesFilter */
    public JSPVariablesFilter() {
    }

    /**
     * 
     * Returns filtered root of hierarchy.
     * 
     * @param   original the original tree model
     * @return  filtered root of hierarchy
     */
    public Object getRoot(TreeModel original) {
        return original.getRoot ();
    }

    
    
    public Object[] getChildren(TreeModel original, Object parent, int from, int to)
        throws UnknownTypeException
    {
        Object[] visibleChildren = null;
        if (parent.equals (original.getRoot())) {
            //retrieve all children
            int parentChildrenCount = original.getChildrenCount(parent);
            Object[] children = original.getChildren(parent, 0, parentChildrenCount);
            parentChildrenCount = children.length;
            if (parentChildrenCount == 1 && children[0] instanceof java.lang.String) 
                return children;

            List visibleChildrenList = new ArrayList();
            ImplicitLocals implicitLocals = new ImplicitLocals();
            Object refThis = null;
            AttributeMap requestAttributes = new AttributeMap("request");
            AttributeMap sessionAttributes = new AttributeMap("session");
            AttributeMap applicationAttributes = new AttributeMap("application");
            for (int i = 0; i < parentChildrenCount; i++) {
                
                Object var = children[i];
                
                if (var instanceof LocalVariable) {
                    LocalVariable lvar = (LocalVariable)var;
                    if (ImplicitLocals.isImplicitLocal(lvar.getName())) {
                        implicitLocals.addLocal(lvar);

                        if (lvar instanceof ObjectVariable) {
                            String varName = lvar.getName();
                            if (varName.equals("request"))
                                requestAttributes = new AttributeMap((ObjectVariable)lvar);
                            else if (varName.equals("session"))
                                sessionAttributes = new AttributeMap((ObjectVariable)lvar);
                            else if (varName.equals("application"))
                                applicationAttributes = new AttributeMap((ObjectVariable)lvar);
                        }
                    }
                    else if (!isHiddenLocal(lvar.getName())) {
                        visibleChildrenList.add(var);
                    }
                }
                else if (var instanceof This)
                    refThis = var;
            }

            visibleChildrenList.add(0, applicationAttributes);
            visibleChildrenList.add(0, sessionAttributes);
            visibleChildrenList.add(0, requestAttributes);
            if (refThis != null)
                visibleChildrenList.add(0, refThis);
            visibleChildrenList.add(implicitLocals);

            if (to > visibleChildrenList.size()) {
                to = visibleChildrenList.size();
            }
            visibleChildren = visibleChildrenList.subList(from, to).toArray();
        }
/*        
        else if (parent instanceof LocalVariable || parent instanceof Field) {
            if (parent instanceof LocalVariable && 
                ((LocalVariable)parent).getDeclaredType().equals("javax.servlet.http.HttpSession") 
                ||
                parent instanceof Field && 
                ((Field)parent).getDeclaredType().equals("javax.servlet.http.HttpSession")) 
            {
                int parentChildrenCount = original.getChildrenCount(parent);
                Object[] sessionChildren = original.getChildren(parent, 0, parentChildrenCount);
                //TODO find child with name "session"
                Object session = sessionChildren[0];
                visibleChildren = original.getChildren(session, from, to);
            }
            else if (parent instanceof LocalVariable &&
                    ((LocalVariable)parent).getDeclaredType().equals("javax.servlet.ServletConfig")
                    ||
                    parent instanceof Field &&
                    ((Field)parent).getDeclaredType().equals("javax.servlet.ServletConfig"))
            {
                int parentChildrenCount = original.getChildrenCount(parent);
                Object[] configChildren = original.getChildren(parent, 0, parentChildrenCount);
                //TODO find child with name "session"
                Object config = configChildren[0];
                visibleChildren = original.getChildren(config, from, to);
            }
            else
                visibleChildren = original.getChildren(parent, from, to);
        }
*/
        else if (parent instanceof ImplicitLocals)
            visibleChildren = ((ImplicitLocals)parent).getLocals().subList(from, to).toArray ();
        else if (parent instanceof AttributeMap) {
            visibleChildren = ((AttributeMap)parent).getAttributes().subList(from, to).toArray();
//            Object[] attributes = ((AttributeMap)parent).entrySet().toArray();
//            visibleChildren = Arrays.asList(attributes).subList(from, to).toArray();
        }
        else if (parent instanceof AttributeMap.Attribute)
            visibleChildren = original.getChildren(((AttributeMap.Attribute)parent).getValue(), from, to);
        else
            visibleChildren = original.getChildren(parent, from, to);
        
        return visibleChildren;
    }

    public int getChildrenCount(TreeModel original, Object node) 
        throws UnknownTypeException
    {
        
        int countVisible = 0;

        //in case of ROOT
        if (node.equals (original.getRoot())) {
            countVisible = original.getChildrenCount(node);
            Object[] children = original.getChildren (node, 0, countVisible);
            //original.getChildrenCount(...) needn't be equal to original.getChildren (...).length()
            countVisible = children.length;
            if (countVisible == 1 && children[0] instanceof java.lang.String) 
                return countVisible;
            for (int i = 0; i < children.length; i++) {
                Object var = children[i];
                //show the locals except of hidden locals and implicit locals 
                if (var instanceof LocalVariable) {
                    if (isHiddenLocal(((LocalVariable)var).getName()) ||
                        ImplicitLocals.isImplicitLocal(((LocalVariable)var).getName()))
                        countVisible--;
                }
                //do not show anything but this
                else if (!(var instanceof This))
                    countVisible--;
            }
            //fold implicit locals and request/session/application attributes in the special nodes
            countVisible += 4;
        }
/*        
        else if (node instanceof LocalVariable || node instanceof Field) { 
            if (node instanceof LocalVariable && 
                ((LocalVariable)node).getDeclaredType().equals("javax.servlet.http.HttpSession") 
                ||
                node instanceof Field && 
                ((Field)node).getDeclaredType().equals("javax.servlet.http.HttpSession")) 
            {
                //TODO retrieve children only _once_ for all cases (maybe not)
                countVisible = original.getChildrenCount(node);
                Object[] children = original.getChildren (node, 0, countVisible);
                //TODO find child with name "session"
                Object session = children[0];
                countVisible = original.getChildrenCount(session);
            }
            else if (node instanceof LocalVariable &&
                    ((LocalVariable)node).getDeclaredType().equals("javax.servlet.ServletConfig")
                    ||
                    node instanceof Field &&
                    ((Field)node).getDeclaredType().equals("javax.servlet.ServletConfig"))
            {
                //TODO retrieve children only _once_ for all cases (maybe not)
                countVisible = original.getChildrenCount(node);
                Object[] children = original.getChildren (node, 0, countVisible);
                //TODO find child with name "config"
                Object config = children[0];
                countVisible = original.getChildrenCount(config);
            }
        }
 */
        else if (node instanceof ImplicitLocals)
            countVisible = ((ImplicitLocals)node).getLocals().size();
        else if (node instanceof AttributeMap)
            countVisible = ((AttributeMap)node).getAttributes().size();
        else if (node instanceof AttributeMap.Attribute)
            countVisible = original.getChildrenCount(((AttributeMap.Attribute)node).getValue());
        else
            countVisible = original.getChildrenCount(node);

        return countVisible;
    }

    /**
     * Returns true if node is leaf. You should not throw UnknownTypeException
     * directly from this method!
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.isLeaf (...)</code> method call only!
     * @return  true if node is leaf
     */
    public boolean isLeaf(TreeModel original, Object node) 
        throws UnknownTypeException 
    {
        boolean il;
        if (node instanceof ImplicitLocals)
            il = false;
        else if (node instanceof AttributeMap) 
            il = false;
        else if (node instanceof AttributeMap.Attribute) {
            Variable attributeValue = ((AttributeMap.Attribute)node).getValue();
            if (isLeafType(attributeValue.getType()))
                il = true;
            else
                il = original.isLeaf(attributeValue);
        }
        else
            il = original.isLeaf(node);
        
        return il;
    }

    /**
     * 
     * Unregisters given listener.
     * 
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
    }

    /**
     * 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
    }


    private static HashSet hiddenLocals = null;
    private static boolean isHiddenLocal(String aLocalName) {

        if (hiddenLocals == null) {
            hiddenLocals = new HashSet();
            
            hiddenLocals.add("_jspxFactory");
            hiddenLocals.add("_jspx_out");
            hiddenLocals.add("_jspx_page_context");

        }
        
        return hiddenLocals.contains(aLocalName);
    }

    private static HashSet leafType = null;
    private static boolean isLeafType (String type) {
        if (leafType == null) {
            leafType = new HashSet ();
            leafType.add ("java.lang.String");
            leafType.add ("java.lang.Character");
            leafType.add ("java.lang.Integer");
            leafType.add ("java.lang.Float");
            leafType.add ("java.lang.Byte");
            leafType.add ("java.lang.Boolean");
            leafType.add ("java.lang.Double");
            leafType.add ("java.lang.Long");
            leafType.add ("java.lang.Short");
        }
        return leafType.contains (type);
    }
    
//---------------------------------------------------------------------------------------    
//      inner classes
//---------------------------------------------------------------------------------------    
    
    public static class ImplicitLocals {
        private List locals = new ArrayList ();
        private static HashSet<String> localsNames = null;

        public static boolean isImplicitLocal(String aLocalName) {

            if (localsNames == null) {
                localsNames = new HashSet<>();
                localsNames.add("application");
                localsNames.add("config");
                localsNames.add("out");
                localsNames.add("page");
                localsNames.add("pageContext");
                localsNames.add("request");
                localsNames.add("response");
                localsNames.add("session");
            }

            return localsNames.contains(aLocalName);
        }
        
        void addLocal (LocalVariable local) {
            locals.add (local);
        }
        
        List getLocals () {
            return locals;
        }
        
        public boolean equals (Object o) {
            return o instanceof ImplicitLocals;
        }
        
        public int hashCode () {
            if (locals.size () == 0) return super.hashCode ();
            return locals.get (0).hashCode ();
        }
    }
    
    public static class AttributeMap {// extends java.util.HashMap {
        private ArrayList attributes = new ArrayList();
        private ObjectVariable owner = null;
        private String ownerName = null;

        public static class UnknownOwnerNameException extends RuntimeException {
            public UnknownOwnerNameException(String name) {
                super("Unknown owner name: " + name);
            }
        };
        
        public AttributeMap(String aOwnerName) {
            setOwnerName(aOwnerName);
        }
        
        public AttributeMap(ObjectVariable aVar) {
            owner = aVar;
            setOwnerName(((LocalVariable)owner).getName());
            Iterator it = new AttributeIterator();
            while (it.hasNext()) {
                Attribute attribute = (Attribute)it.next();
                if (attribute != null) {
                    attributes.add(attribute);
                }
            }
        }

        private void setOwnerName(String aOwnerName) {
            if (aOwnerName.equals("request") || aOwnerName.equals("session") || aOwnerName.equals("application"))
                ownerName = aOwnerName;
            else
                throw new UnknownOwnerNameException(aOwnerName);
        }
        
        public ArrayList getAttributes() { return attributes; }
        public String getOwnerName() { return ownerName; }
        
        public class Attribute {
            private String name;
            private Variable value;
            public Attribute(String aName, Variable aValue) {
                name = aName;
                value = aValue;
            }
            public String getName() { return name; }
            public Variable getValue() { return value; }
        }
        
        private class AttributeIterator implements Iterator {
            ObjectVariable reqAttributes = null;

            public AttributeIterator() {
                try {
                    reqAttributes = (ObjectVariable)owner.invokeMethod(
                            "getAttributeNames",
                            "()Ljava/util/Enumeration;",
                            new Variable[0]
                    );
                }
                catch (InvalidExpressionException e) {
                }
                catch (NoSuchMethodException e) {
                }
            }
            
            
            public boolean hasNext() {
                
                if (reqAttributes == null) return false;
                
                boolean ret = false;
                try {
                    Variable hasMoreElements = reqAttributes.invokeMethod(
                            "hasMoreElements",
                            "()Z",
                            new Variable[0]
                    );
                    ret = (hasMoreElements != null && "true".equals(hasMoreElements.getValue()));
                }
                catch (InvalidExpressionException e) {
                }
                catch (NoSuchMethodException e) {
                }

                return ret;
            }

            public Object next() {

                Object nextElement = null;
                try {
                    Variable attributeName = reqAttributes.invokeMethod(
                            "nextElement",
                            "()Ljava/lang/Object;",
                            new Variable[0]
                    );
                    // object collected or vm disconnected if null
                    if (attributeName != null) {
                        Variable attributeValue = owner.invokeMethod(
                                "getAttribute",
                                "(Ljava/lang/String;)Ljava/lang/Object;",
                                new Variable[] { attributeName }
                        );
                        nextElement = new AttributeMap.Attribute(
                                (attributeName.getValue() == null ? "" : attributeName.getValue()),
                                 attributeValue);
                    }
                }
                catch (InvalidExpressionException e) {
                }
                catch (NoSuchMethodException e) {
                }

                return nextElement;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        }
    }
}
