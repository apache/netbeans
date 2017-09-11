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
