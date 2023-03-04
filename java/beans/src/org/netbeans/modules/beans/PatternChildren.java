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

package org.netbeans.modules.beans;

import java.util.*;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/** Implements children for basic source code patterns
* 
* @author Petr Hrebejk, Jan Jancura
*/
public final class PatternChildren extends Children.Keys<Pattern> {

//    static final RequestProcessor ANALYZER = new RequestProcessor("Bean patterns analyser", 1); // NOI18N

    private boolean wri = false;

//    private Listener elementListener = new Listener(this);
    
    private RequestProcessor.Task   refreshTask;
    
    /** Object for finding patterns in class */
    private PatternAnalyser patternAnalyser;
    
//    private final JavaClass classElement;
    
    public PatternChildren(List<Pattern> patterns /*, PatternFilters filters */ ) {
        resetKeys( patterns /*, filters */ );            
    }

    protected Node[] createNodes (Pattern key) {
        return new Node[] {createPatternNode(key)};
    }
    
    private Node createPatternNode(Object key) {

        if (key instanceof ClassPattern ) {
            return new PatternNode((ClassPattern)key, wri );
        }
        if (key instanceof IdxPropertyPattern)
            return new IdxPropertyPatternNode((IdxPropertyPattern) key, wri);
        if (key instanceof PropertyPattern)
            return new PropertyPatternNode((PropertyPattern) key, wri);
        if (key instanceof EventSetPattern)
            return new EventSetPatternNode((EventSetPattern) key, wri);

        return null;
    }


    void resetKeys( List<Pattern> patterns /*, PattenrMemberFilters filters */) {            
        setKeys( patterns/* filters.filter(descriptions) */ );
    }


// Constructors -----------------------------------------------------------------------

//    /** Create pattern children. The children are initilay unfiltered.
//     * @param classElement the atteached class. For this class we recognize the patterns
//     */ 
//
//    public PatternChildren (JavaClass classElement) {
//        this(classElement, true);
//    }
//
//    public PatternChildren (JavaClass classElement, boolean isWritable ) {
//        this.classElement = classElement;
//        patternAnalyser = new PatternAnalyser( classElement );
//        PropertyActionSettings.getDefault().addPropertyChangeListener(elementListener);
//        wri = isWritable;
//    }
//
//    protected void addNotify() {
//        super.addNotify();
//        refreshAllKeys();
//    }
//
//    /** Updates all the keys (elements) according to the current filter &
//    * ordering.
//    */
//    protected void refreshAllKeys () {
//            scheduleRefresh();
//    }
//    
//    private synchronized void scheduleRefresh() {
//        if (refreshTask == null) {
//            refreshTask = ANALYZER.create(elementListener);
//        }
//        refreshTask.schedule(200);
//    }
//
//    /** Updates all the keys with given filter. Overriden to provide package access tothis method.
//    */
//    protected void refreshKeys (int filter) {
//
//        // Method is added or removed ve have to re-analyze the pattern abd to
//        // registrate Children as listener
//        JMIUtils.beginTrans(false);
//        try {
//            try {
//                elementListener.unregisterAll();
//                elementListener.reassignMethodListener(this.classElement);
//                elementListener.reassignFieldListener(this.classElement);
//                elementListener.assignFeaturesListener(this.classElement);
//                patternAnalyser.analyzeAll();
//            } finally {
//                JMIUtils.endTrans();
//            }
//        } catch (JmiException e) {
//            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
//        }
//        
//        setKeys(collectAllKeys());
//    }
//    
//    private Collection collectAllKeys() {
//        List keys = new LinkedList();
//        keys.addAll(getKeysOfType(PatternFilter.PROPERTY));
//        keys.addAll(getKeysOfType(PatternFilter.IDXPROPERTY));
//        keys.addAll(getKeysOfType(PatternFilter.EVENT_SET));
//        return keys;
//    }
//
//    /** Gets the pattern analyser which manages the patterns */
//    PatternAnalyser getPatternAnalyser( ) {
//        return patternAnalyser;
//    }
//
//    public void removeAll() {
//        elementListener.unregisterAll();
//        setKeys(Collections.EMPTY_LIST);
//    }
//    
    
    // Utility methods --------------------------------------------------------------------

//    protected Collection getKeysOfType (int elementType) {
//
//        List keys = null;
//
//        if ((elementType & PatternFilter.PROPERTY) != 0)  {
//            keys = new ArrayList(patternAnalyser.getPropertyPatterns());
//            Collections.sort( keys, new PatternComparator() );
//        }
//        if ((elementType & PatternFilter.IDXPROPERTY) != 0) {
//            keys = new ArrayList(patternAnalyser.getIdxPropertyPatterns());
//            Collections.sort( keys, new PatternComparator() );
//        }
//        if ((elementType & PatternFilter.EVENT_SET) != 0) {
//            keys = new ArrayList(patternAnalyser.getEventSetPatterns());
//            Collections.sort( keys, new PatternComparator() );
//        }
//
//        //    if ((filter == null) || filter.isSorted ())
//        //      Collections.sort (keys, comparator);
//        return keys;
//    }
//

}
