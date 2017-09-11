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
