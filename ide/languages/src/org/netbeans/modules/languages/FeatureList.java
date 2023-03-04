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
package org.netbeans.modules.languages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParserManager.State;


/**
 *
 * @author hanz
 */
public class FeatureList {
    
    private Map<String,List<Feature>>   features;
    private Map<String,FeatureList>     lists;
    
    void add (Feature feature) {
        Selector selector = feature.getSelector ();
        FeatureList list = this;
        if (selector != null) {
            List<String> path = selector.getPath ();
            for (int i = path.size () - 1; i >= 0; i--) {
                String name = path.get (i);
                if (list.lists == null)
                    list.lists = new HashMap<String,FeatureList> ();
                FeatureList newList = list.lists.get (name);
                if (newList == null) {
                    newList = new FeatureList ();
                    list.lists.put (name, newList);
                }
                list = newList;
            }
        }
        if (list.features == null)
            list.features = new HashMap<String,List<Feature>> ();
        List<Feature> fs = list.features.get (feature.getFeatureName ());
        if (fs == null) {
            fs = new ArrayList<Feature> ();
            list.features.put (feature.getFeatureName (), fs);
        }
        fs.add (feature);
    }
    
    void importFeatures (FeatureList featureList) {
        
    }
    
    public List<Feature> getFeatures (String featureName) {
        List<Feature> list = new ArrayList<Feature>();
        collectFeatures(list, featureName);
        return list;
    }
    
   public Feature getFeature (String featureName) {
        List<Feature> features = getFeatures (featureName);
        if (features.isEmpty ()) return null;
        return features.get (0);
    }
    
    private void collectFeatures(List<Feature> result, String featureName) {
        if (features != null) {
            List<Feature> list = features.get(featureName);
            if (list != null) {
                result.addAll(list);
            }
        }
        if (lists != null) {
            for (Entry<String, FeatureList> entry : lists.entrySet()) {
                FeatureList fList = entry.getValue();
                if (fList != null) {
                    fList.collectFeatures(result, featureName);
                }
            } // for
        } // if
    }
    
    public List<Feature> getFeatures (String featureName, String id) {
        if (lists == null) return Collections.<Feature>emptyList ();
        FeatureList list = lists.get (id);
        if (list == null) return Collections.<Feature>emptyList ();
        if (list.features == null) return Collections.<Feature>emptyList ();
        List<Feature> result = list.features.get (featureName);
        if (result != null) return result;
        return Collections.<Feature>emptyList ();
    }
    
    public Feature getFeature (String featureName, String id) {
        List<Feature> features = getFeatures (featureName, id);
        if (features.isEmpty ()) return null;
        return features.get (0);
    }

    public List<Feature> getFeatures (String featureName, ASTPath path) {
        List<Feature> result = null;
        FeatureList list = this;
        for (int i = path.size () - 1; i > 0; i--) {
            ASTItem item = path.get (i);
            String name = item instanceof ASTNode ? ((ASTNode) item).getNT () : ((ASTToken) item).getTypeName ();
            if (list.lists == null) break;
            list = list.lists.get (name);
            if (list == null) break;
            if (list.features == null) continue;
            List<Feature> l = list.features.get (featureName);
            if (l == null) continue;
            if (result == null)
                result = new ArrayList<Feature> (l);
            else
                result.addAll (l);
        }
        if (result == null) return Collections.<Feature>emptyList ();
        return result;
    }
    
    public Feature getFeature (String featureName, ASTPath path) {
        List<Feature> features = getFeatures (featureName, path);
        if (features.isEmpty ()) return null;
        return features.get (0);
    }
    
    void evaluate (
        State                           state, 
        List<ASTItem>                   path, 
        Map<String,Set<ASTEvaluator>>   evaluatorsMap                           //,Map<Object,Long> times
    ) {
        FeatureList list = this;
        for (int i = path.size () - 1; i > 0; i--) {
            ASTItem item = path.get (i);
            String name = item instanceof ASTNode ? ((ASTNode) item).getNT () : ((ASTToken) item).getTypeName ();
            if (list.lists == null) return;
            list = list.lists.get (name);
            if (list == null) return;
            if (list.features != null) {
                Iterator<String> it = evaluatorsMap.keySet ().iterator ();
                while (it.hasNext ()) {
                    String featureName = it.next ();
                    if (featureName == null) {
                        Set<ASTEvaluator> evaluators = evaluatorsMap.get (null);
                        Iterator<ASTEvaluator> it2 = evaluators.iterator ();
                        while (it2.hasNext ()) {
                            ASTEvaluator evaluator = it2.next ();
                            Collection<List<Feature>> featureListsC = list.features.values ();
                            Iterator<List<Feature>> it3 = featureListsC.iterator ();
                            while (it3.hasNext ()) {
                                List<Feature> featureList = it3.next ();
                                Iterator<Feature> it4 = featureList.iterator ();
                                while (it4.hasNext ()) {                        //long time = System.currentTimeMillis ();
                                    evaluator.evaluate (state, path, it4.next ());
                                                                                //Long l = times.get (evaluator);time = System.currentTimeMillis () - time; if (l != null) time += l.longValue (); times.put (evaluator, time);
                                }
                            }
                        }
                    } else {
                        List<Feature> features = list.features.get (featureName);
                        if (features == null) continue;
                        Set<ASTEvaluator> evaluators = evaluatorsMap.get (featureName);

                        Iterator<Feature> it2 = features.iterator ();
                        while (it2.hasNext ()) {
                            Feature feature =  it2.next ();
                            Iterator<ASTEvaluator> it3 = evaluators.iterator ();
                            while (it3.hasNext ()) {
                               ASTEvaluator evaluator = it3.next ();            //long time = System.currentTimeMillis ();
                               evaluator.evaluate (state, path, feature);       //Long l = times.get (evaluator);time = System.currentTimeMillis () - time; if (l != null) time += l.longValue (); times.put (evaluator, time);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        toString (sb, "");
        return sb.toString ();
    }
        
    private void toString (StringBuilder sb, String selector) {
        sb.append ("Selector: ").append (selector).append ("\n");
        if (features != null) {
            Iterator<String> it = features.keySet ().iterator ();
            while (it.hasNext ()) {
                String featureName = it.next ();
                Iterator<Feature> it2 = features.get (featureName).iterator ();
                while (it2.hasNext ()) {
                    Feature feature = it2.next ();
                    sb.append ("  ").append (featureName).append (": ").append (feature).append ("\n");
                }
            }
        }
        if (lists != null) {
            Iterator<String> it = lists.keySet ().iterator ();
            while (it.hasNext ()) {
                String s = it.next ();
                lists.get (s).toString (sb, selector + "." + s);
            }
        }
    }
}
