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

package org.netbeans.modules.beans.beaninfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/** Implements children for basic source code patterns
 *
 * @author Petr Hrebejk
 */
public final class BiChildren extends Children.Keys<Class<?>> {

    /** Object for finding patterns in class */
    private BiAnalyser       biAnalyser;


    // Constructors -----------------------------------------------------------------------

    /** Create pattern children. The children are initilay unfiltered.
     */ 

    public BiChildren ( BiAnalyser biAnalyser, Class<?>[] keys ) {
        super();
        this.biAnalyser = biAnalyser;
        setKeys( keys );
    }

    /** Called when the preparetion of nodes is needed
     */
    @Override
    protected void addNotify() {
        //refreshAllKeys ();
    }

    /** Called when all children are garbage collected */
    @Override
    protected void removeNotify() {
        setKeys( Collections.<Class<?>>emptySet() );
    }

    /** Gets the pattern analyser which manages the patterns */

    BiAnalyser getBiAnalyser( ) {
        return biAnalyser;
    }
    
    // Children.keys implementation -------------------------------------------------------

    /** Creates nodes for given key.
    */
    protected Node[] createNodes( final Class<?> key ) {
        if ( key == BiFeature.Descriptor.class )
            return createNodesFromFeatures( biAnalyser.getDescriptor() );
        if ( key == BiFeature.Property.class )
            return createNodesFromFeatures( biAnalyser.getProperties() );
        if ( key == BiFeature.IdxProperty.class )
            return createNodesFromFeatures( biAnalyser.getIdxProperties() );
        if ( key == BiFeature.EventSet.class )
            return createNodesFromFeatures( biAnalyser.getEventSets() );
        if ( key == BiFeature.Method.class )
            return createNodesFromFeatures( biAnalyser.getMethods() );


        /*
        if (key instanceof IdxPropertyPattern)
          return new Node[] { new IdxPropertyPatternNode((IdxPropertyPattern)key, true) };
        if (key instanceof PropertyPattern) 
          return new Node[] { new PropertyPatternNode((PropertyPattern)key, true) };
        if (key instanceof EventSetPattern)
          return new Node[] { new EventSetPatternNode((EventSetPattern)key, true) };
        */
        // Unknown pattern
        return new Node[0];
    }

    // Utility methods --------------------------------------------------------------------

    Node[] createNodesFromFeatures( List<? extends BiFeature> col ) {

        Iterator<? extends BiFeature> it = col.iterator();

        Node[] nodes = new Node[ col.size() ];

        for ( int i = 0; it.hasNext() && i < nodes.length; i++ )
            nodes[i] = new BiFeatureNode( it.next(), biAnalyser );

        return nodes;
    }

}
