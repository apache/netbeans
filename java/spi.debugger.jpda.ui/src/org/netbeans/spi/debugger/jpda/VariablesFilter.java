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

package org.netbeans.spi.debugger.jpda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.apiregistry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * This filter allows to change nodes in Locals View and Watches View for
 * some concrete variable types. For example itallows to define "logical" view
 * for java.util.Hashtable.
 *
 * Instances of this filter should be registerred in: "
 * "META-INF\debugger\netbeans-JPDADebuggerEngine\org.netbeans.spi.debugger.jpda.VariablesFilter"
 * file.
 * 
 * The implementor might want to implement also {@link org.netbeans.spi.viewmodel.ExtendedNodeModelFilter}
 * for extra functionality.
 *
 * @author   Jan Jancura
 */
public abstract class VariablesFilter {
    

    /** 
     * Returns set of fully quilified class names (like java.lang.String) this
     * filter is registerred to.
     *
     * @return set of fully quilified class names
     */
    public abstract String[] getSupportedTypes ();

    /** 
     * Returns set of fully quilified class names (like java.lang.String) this
     * filter is registerred to.
     *
     * @return set of fully quilified class names
     */
    public abstract String[] getSupportedAncestors ();
    
    /** 
     * Returns filtered children for given variable on given indexes.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    public abstract Object[] getChildren (
        TreeModel      original,
        Variable       variable, 
        int            from, 
        int            to
    ) throws UnknownTypeException;
    
    /** 
     * Returns number of filtered children for given variable.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     *
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  number of filtered children for given variable
     */
    public abstract int getChildrenCount (
        TreeModel      original,
        Variable       variable
    ) throws UnknownTypeException;
    
    /**
     * Returns true if variable is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public abstract boolean isLeaf (
        TreeModel      original,
        Variable       variable
    ) throws UnknownTypeException;
    
    
    // NodeModelFilter
    
    /**
     * Returns filterred display name for given variable.
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    public abstract String getDisplayName (NodeModel original, Variable variable) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred icon for given variable.
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public abstract String getIconBase (NodeModel original, Variable variable) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred tooltip for given variable.
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    public abstract String getShortDescription (NodeModel original, Variable variable) 
    throws UnknownTypeException;
    
    
    // NodeActionsProviderFilter
    
    /**
     * Returns set of actions for given variable.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     * @return  set of actions for given variable
     */
    public abstract Action[] getActions (
        NodeActionsProvider original, 
        Variable variable
    ) throws UnknownTypeException;
    
    /**
     * Performs default action for given variable.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     */
    public abstract void performDefaultAction (
        NodeActionsProvider original, 
        Variable variable
    ) throws UnknownTypeException;
    
    
    // TableModelFilter
    
    /**
     * Returns filterred value to be displayed in column <code>columnID</code>
     * and for variable <code>variable</code>. Column ID is defined in by 
     * {@link org.netbeans.spi.viewmodel.ColumnModel#getID}, and variables are defined by values returned from
     * {@link TreeModel#getChildren}.
     *
     * @param   original the original table model
     * @param   variable a variable returned from {@link TreeModel#getChildren} for this row
     * @param   columnID a id of column defined by {@link org.netbeans.spi.viewmodel.ColumnModel#getID}
     * @throws  ComputingException if the value is not known yet and will 
     *          be computed later
     * @throws  UnknownTypeException if there is no TableModel defined for given
     *          parameter type
     *
     * @return value of variable representing given position in tree table.
     */
    public abstract Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException;
    
    /**
     * Filters original isReadOnly value from given table model.
     *
     * @param  original the original table model
     * @param  variable a variable returned from {@link TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link org.netbeans.spi.viewmodel.ColumnModel#getID}
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return true if variable on given position is read only
     */
    public abstract boolean isReadOnly (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException;
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * for variable <code>variable</code>. Column ID is defined in by 
     * {@link org.netbeans.spi.viewmodel.ColumnModel#getID}, and variable are defined by values returned from
     * {@link TreeModel#getChildren}.
     *
     * @param  original the original table model
     * @param  variable a variable returned from {@link TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link org.netbeans.spi.viewmodel.ColumnModel#getID}
     * @param  value a new value of variable on given position
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     */
    public abstract void setValueAt (
        TableModel original, 
        Variable variable, 
        String columnID, 
        Object value
    ) throws UnknownTypeException;


    /**
     * Declarative registration of a VariablesFilter implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     *
     * @author Martin Entlicher
     * @since 2.19
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         */
        String path() default "";

    }

    static class ContextAware extends VariablesFilter implements ContextAwareService<VariablesFilter> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        public VariablesFilter forContext(ContextProvider context) {
            return (VariablesFilter) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        public String[] getSupportedTypes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] getSupportedAncestors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object[] getChildren(TreeModel original, Variable variable, int from, int to) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getChildrenCount(TreeModel original, Variable variable) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isLeaf(TreeModel original, Variable variable) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName(NodeModel original, Variable variable) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getIconBase(NodeModel original, Variable variable) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getShortDescription(NodeModel original, Variable variable) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Action[] getActions(NodeActionsProvider original, Variable variable) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void performDefaultAction(NodeActionsProvider original, Variable variable) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getValueAt(TableModel original, Variable variable, String columnID) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isReadOnly(TableModel original, Variable variable, String columnID) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setValueAt(TableModel original, Variable variable, String columnID, Object value) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            return new ContextAware(serviceName);
        }

    }
    
}
