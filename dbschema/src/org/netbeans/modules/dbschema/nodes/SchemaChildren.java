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

package org.netbeans.modules.dbschema.nodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
//import org.openide.cookies.FilterCookie;
import org.openide.util.WeakListeners;

import org.netbeans.modules.dbschema.*;

/** Normal implementation of children list for a table element node.
 */
public class SchemaChildren extends Children.Keys {
    /** Converts property names to filter. */
    protected static HashMap propToFilter;

    /** The table element whose subelements are represented. */
    protected SchemaElement element;
    /** Filter for elements, or <code>null</code> to disable. */
    protected SchemaElementFilter filter;
    /** Factory for creating new child nodes. */
    protected DBElementNodeFactory factory;
    /** Weak listener to the element and filter changes */
    private PropertyChangeListener wPropL;
    /** Listener to the element and filter changes. This reference must
    * be kept to prevent the listener from finalizing when we are alive */
    private DBElementListener propL;
    /** Central memory of mankind is used when some elements are changed */
    protected Collection[] cpl;
    /** Flag saying whether we have our nodes initialized */
    private boolean nodesInited = false;
  
    static {
        propToFilter = new HashMap ();
        propToFilter.put (DBElementProperties.PROP_TABLES, new Integer (TableElementFilter.TABLE));
        propToFilter.put (DBElementProperties.PROP_COLUMNS, new Integer (TableElementFilter.COLUMN));
        propToFilter.put (DBElementProperties.PROP_INDEXES, new Integer (TableElementFilter.INDEX));
        propToFilter.put (DBElementProperties.PROP_KEYS, new Integer (TableElementFilter.FK));
    }
    
    /** Create class children with the default factory.
    * The children are initially unfiltered.
    * @param element attached class element (non-<code>null</code>)
    */
    public SchemaChildren (final SchemaElement element) {
        this(DefaultDBFactory.READ_ONLY, element);
    }

    /** Create class children.
    * The children are initially unfiltered.
    * @param factory the factory to use to create new children
    * @param element attached class element (non-<code>null</code>)
    */
    public SchemaChildren (final DBElementNodeFactory factory, final SchemaElement element) {
        super();
        this.element = element;
        this.factory = factory;
        this.filter = null;
    }

    /********** Implementation of filter cookie **********/

    /* @return The class of currently asociated filter or null
    * if no filter is asociated with these children.
    */
    public Class getFilterClass () {
        return SchemaElementFilter.class;
    }

    /* @return The filter currently asociated with these children
    */
    public Object getFilter () {
        return filter;
    }

    /* Sets new filter for these children.
    * @param filter New filter. Null == disable filtering.
    */
    public void setFilter (final Object filter) {
        if (!(filter instanceof SchemaElementFilter)) 
            throw new IllegalArgumentException();

        this.filter = (SchemaElementFilter)filter;
        // change element nodes according to the new filter
        if (nodesInited)
            refreshAllKeys ();
    }

    // Children implementation ..............................................................

    /* Overrides initNodes to run the preparation task of the
    * source element, call refreshKeys and start to
    * listen to the changes in the element too. */
    protected void addNotify () {
        refreshAllKeys ();
        
        // listen to the changes in the class element
        if (wPropL == null) {
            propL = new DBElementListener();
            wPropL = WeakListeners.propertyChange (propL, element);
        }  
        else {
            // #55249 - need to recreate the listener with the right element
            wPropL = WeakListeners.propertyChange (propL, element);
        }
        
        element.addPropertyChangeListener (wPropL);
        nodesInited = true;
    }

    protected void removeNotify () {
        setKeys (java.util.Collections.EMPTY_SET);
        nodesInited = false;
    }

    /* Creates node for given key.
    * The node is created using node factory.
    */
    protected Node[] createNodes (final Object key) {
        if (key instanceof TableElement)
            return new Node[] { factory.createTableNode((TableElement)key) };
            
        // ?? unknown type
        return new Node[0];
    }


    /************** utility methods ************/

    /** Updates all the keys (elements) according to the current filter &
    * ordering.
    */
    protected void refreshAllKeys () {
        cpl = new Collection [getOrder ().length];

        refreshKeys (SchemaElementFilter.TABLE);
    }

    /** Updates all the keys with given filter.
    */
    protected void refreshKeys (int filter) {
        int[] order = getOrder ();
        LinkedList keys = new LinkedList();
        
        // build ordered and filtered keys for the subelements
        for (int i = 0; i < order.length; i++)
            if (((order[i] & filter) != 0) || (cpl [i] == null)) 
                keys.addAll(cpl [i] = getKeysOfType(order[i]));
            else  
                keys.addAll(cpl [i]);
        
        // set new keys
        setKeys(keys);
    }

    /** Filters and returns the keys of specified type.
    */
    protected Collection getKeysOfType (final int elementType) {
        LinkedList keys = new LinkedList();

        if ((elementType & SchemaElementFilter.TABLE) != 0)
            filterModifiers(((SchemaElement)element).getTables(), keys);

        return keys;    
    }

    /** Filters MemberElements for modifiers, and adds them to the given collection.
    */
    private void filterModifiers (DBElement[] elements, Collection keys) {
        int i, k = elements.length;
        
        for (i = 0; i < k; i ++)
            keys.add (elements [i]);
    }

    /** Returns order form filter.
    */
    protected int[] getOrder () {
        return (filter == null || (filter.getOrder() == null)) ? SchemaElementFilter.DEFAULT_ORDER : filter.getOrder();
    }

    // innerclasses ...........................................................................

    /** The listener for listening to the property changes in the filter.
    */
    private final class DBElementListener implements PropertyChangeListener {
        public DBElementListener () {}
        
        /** This method is called when the change of properties occurs in the element.
        * PENDING - (for Hanz - should be implemented better, change only the
        * keys which belong to the changed property).
        * -> YES MY LORD! ANOTHER WISH?
        */
        public void propertyChange (PropertyChangeEvent evt) {
            Integer i = (Integer) propToFilter.get (evt.getPropertyName ());
            if (i != null)
                refreshKeys(i.intValue());
        }
    } // end of ElementListener inner class*/
}
