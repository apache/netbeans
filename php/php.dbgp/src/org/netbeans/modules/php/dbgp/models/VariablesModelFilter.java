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
package org.netbeans.modules.php.dbgp.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.dbgp.models.nodes.AbstractModelNode;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author ads
 *
 */
public class VariablesModelFilter extends ViewModelSupport
    implements TreeModelFilter
{

    private static final String LOCALS_VIEW = "LocalsView";     // NOI18N

    public enum FilterType {
        SCALARS,
        ARRAY,
        OBJECT,
        RESOURCE,
        NULL,
        UNINIT,
        SUPERGLOBALS
        ;
    }

    public static void setFilters( FilterType[] types ) {
        myShowFilters.set( types );
        DebuggerEngine engine =
            DebuggerManager.getDebuggerManager().getCurrentEngine();
        TreeModelFilter filter = (TreeModelFilter)engine.lookupFirst( LOCALS_VIEW ,
                TreeModelFilter.class );
        if ( filter != null ) {
            assert filter instanceof VariablesModelFilter;
            ((VariablesModelFilter)filter).refresh();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModelFilter#getChildren(org.netbeans.spi.viewmodel.TreeModel, java.lang.Object, int, int)
     */
    @Override
    public Object[] getChildren( TreeModel original, Object node, int from,
            int to ) throws UnknownTypeException
    {
        List<Object> list = getFilteredChildren(original, node);
        if ( from >= list.size() ) {
            return new Object[0];
        }
        int end = Math.min( list.size(), to);
        list = list.subList(from, end);
        return list.toArray(new Object[0] );
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModelFilter#getChildrenCount(org.netbeans.spi.viewmodel.TreeModel, java.lang.Object)
     */
    @Override
    public int getChildrenCount( TreeModel original, Object node )
            throws UnknownTypeException
    {
        return getFilteredChildren(original, node).size();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModelFilter#getRoot(org.netbeans.spi.viewmodel.TreeModel)
     */
    @Override
    public Object getRoot( TreeModel original ) {
        return original.getRoot();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.viewmodel.TreeModelFilter#isLeaf(org.netbeans.spi.viewmodel.TreeModel, java.lang.Object)
     */
    @Override
    public boolean isLeaf( TreeModel original, Object node )
            throws UnknownTypeException
    {
        return original.isLeaf(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.models.ViewModelSupport#clearModel()
     */
    @Override
    public void clearModel() {
    }

    public static FilterType[] getFilters() {
        return myShowFilters.get();
    }

    private List<Object> getFilteredChildren( TreeModel original, Object node )
        throws UnknownTypeException
    {
        int size = original.getChildrenCount(node);
        List<Object> result = new ArrayList<>();
        Object[] children = original.getChildren( node , 0 , size );
        for (Object object : children) {
            if ( !(object instanceof AbstractModelNode )) {
                result.add( object );
            }
            else {
                AbstractModelNode var = (AbstractModelNode)object;
                FilterType[] types = getFilters();
                Set<FilterType> set = new HashSet<> (
                        Arrays.asList( types ) );
                if ( var.hasType( set) ) {
                    result.add( object );
                }
            }
        }
        return result;
    }


    private static void initFilters() {
        // TODO : should be deserilized between NB invocations
        myShowFilters = new AtomicReference<>();
        FilterType[] filters = new FilterType[] {
                FilterType.ARRAY,
                FilterType.OBJECT,
                FilterType.SCALARS,
                FilterType.SUPERGLOBALS,
                FilterType.RESOURCE,
                FilterType.NULL,
        };
        myShowFilters.set( filters );
    }

    static {
        initFilters();
    }

    private static AtomicReference<FilterType[]> myShowFilters;
}
