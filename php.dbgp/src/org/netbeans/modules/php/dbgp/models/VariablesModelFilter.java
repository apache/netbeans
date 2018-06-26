/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        return list.toArray( new Object[list.size()] );
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
        };
        myShowFilters.set( filters );
    }

    static {
        initFilters();
    }

    private static AtomicReference<FilterType[]> myShowFilters;
}
