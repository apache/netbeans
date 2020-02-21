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

package org.netbeans.modules.cnd.api.model.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.*;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.utils.Antiloop;

/**
 * utilities to merge/get inheritance information
 */
public final class CsmInheritanceUtilities {
    
        private static final ThreadLocal<Antiloop<CsmInheritance>> threadLocalInheritanceAntiloop = new ThreadLocal<Antiloop<CsmInheritance>>() {

            @Override
            protected Antiloop<CsmInheritance> initialValue() {
                return new Antiloop<CsmInheritance>();
            }

        };   
    
    /* 
     * visibility is ordered:
     * NONE < PUBLIC < PROTECTED < PRIVATE
     * using:
     * when use min visibility:
     *  PRIVATE => everything is seen in context about asked class
     *  PROTECTED => protected and public members are visible
     *  PUBLIC => only public members are visible
     *  NONE => nothing is visible from asked class
     */
    private static final int PRIVATE   = 1 << 0; //
    private static final int PROTECTED = 1 << 1; //
    private static final int PUBLIC    = 1 << 2; //
    private static final int NONE      = 1 << 3; //   
    
    // value used when need to pass info, that everything is visible
    public static final CsmVisibility MAX_VISIBILITY = CsmVisibility.PRIVATE;
    private static final Logger LOG = Logger.getLogger(CsmInheritanceUtilities.class.getSimpleName());
    
    /**
     * Creates a new instance of CsmInheritanceUtilities
     */
    private CsmInheritanceUtilities() {
    }
    
    private static int visToInt(CsmVisibility vis) {
        if (vis == CsmVisibility.NONE) {
            return NONE;
        } else if (vis == CsmVisibility.PRIVATE) {
            return PRIVATE;
        } else if (vis == CsmVisibility.PROTECTED) {
            return PROTECTED;
        } else {
            assert (vis == CsmVisibility.PUBLIC);
            return PUBLIC;
        }
    }
    
    private static CsmVisibility intToVis(int visInt) {
        switch (visInt) {
            case NONE:
                return CsmVisibility.NONE;
            case PRIVATE:
                return CsmVisibility.PRIVATE;
            case PROTECTED:
                return CsmVisibility.PROTECTED;
            default:
                assert (visInt == PUBLIC);
                return CsmVisibility.PUBLIC;
        }
    }
    
    // match if member is valid for input minimal visibility value
    public static boolean matchVisibility(CsmMember member, CsmVisibility minVisibility) {
        assert (member.getVisibility() != null) : "can't be null visibility";
        return matchVisibility(member.getVisibility(), minVisibility);
    }
    
    // match if "toCheck" visibility is valid for input minimal visibility value
    private static boolean matchVisibility(CsmVisibility toCheck, CsmVisibility minVisibility) {
        assert (toCheck != null && minVisibility != null);
        if (minVisibility == CsmVisibility.NONE) {
            // quick escape for "invisible"
            return false;
        }
        int memberVis = visToInt(toCheck);
        int minVis = visToInt(minVisibility);
        return minVis <= memberVis;
    }
    
    /*
     * class A {
     * };
     * class B "extends" A {
     * };
     * this method is used for getting visibility of members asked from class A
     * when handling class B's context (for example method of class B)
     * +---------------------------------------------------------+
     * |B extends A as    |    A's members visibility from B     |
     * |---------------------------------------------------------+
     * |public            |     protected, public                |
     * |protected         |     protected, public                |
     * |private           |     protected, public                |
     * |---------------------------------------------------------+
     *
     * for B-derived childs use getChildInheritanceVisibility
     */
    private static CsmVisibility getInheritanceVisibility(CsmVisibility inheritBA) {
        // for all other - protected and public level
        return CsmVisibility.PROTECTED;
    }

    /*
     * class A {
     * };
     * class B "extends" A {
     * };
     * this method is used for getting visibility of members asked from class A
     * when handling class B objects in not B's context (for example global method used B)
     * +---------------------------------------------------------------+
     * |B extends A as    |    A's members visibility where B is used  |
     * |---------------------------------------------------------------+
     * |public            |     public                                 |
     * |protected         |     invisible                              |
     * |private           |     invisible                              |
     * |---------------------------------------------------------------+
     *
     * for B-derived childs use getExtChildInheritanceVisibility
     */
    private static CsmVisibility getExtInheritanceVisibility(CsmVisibility inheritBA) {
        if (inheritBA == CsmVisibility.PUBLIC) {
            return CsmVisibility.PUBLIC;
        } else {
            return CsmVisibility.NONE;
        }
    }
    
    /*
     * class A {
     * };
     * class B "extends" A {
     * };
     * class C "extends" B {
     * };
     * this method is used for getting visibility of members asked from class A
     * when handling class C's context (for example method of class C)
     * +---------------------------------------------------------+
     * |B extends A as    |    A's members visibility from C     |
     * |---------------------------------------------------------+
     * |public            |     protected, public                |
     * |protected         |     protected, public                |
     * |private           |     invisible                        |
     * |---------------------------------------------------------+
     */
    private static CsmVisibility getChildInheritanceVisibility(CsmVisibility inheritBA) {
        if (inheritBA == CsmVisibility.PUBLIC || inheritBA == CsmVisibility.PROTECTED) {
            return CsmVisibility.PROTECTED;
        } else {
            return CsmVisibility.NONE;
        }
    }
    
//    /*
//     * class A {
//     * };
//     * class B "extends" A {
//     * };
//     * class C "extends" B {
//     * };
//     * this method is used for getting visibility of members asked from class A
//     * when handling class B in not B's context (for example global method used C)
//     * +---------------------------------------------------------------+
//     * |B extends A as    |    A's members visibility where C is used  |
//     * |---------------------------------------------------------------+
//     * |public            |     public                                 |
//     * |protected         |     invisible                              |
//     * |private           |     invisible                              |
//     * |---------------------------------------------------------------+
//     */
//    public static CsmVisibility getExtChildInheritanceVisibility(CsmVisibility inheritBA) {
//        if (inheritBA == CsmVisibility.PUBLIC) {
//            return CsmVisibility.PUBLIC;
//        } else {
//            return CsmVisibility.NONE;
//        }
//    }
    
    public static CsmVisibility mergeInheritedVisibility(CsmVisibility curVisibility, CsmVisibility inherVisibility) {
        return getMinVisibility(curVisibility, getInheritanceVisibility(inherVisibility));
    }    

    public static CsmVisibility mergeExtInheritedVisibility(CsmVisibility curVisibility, CsmVisibility inherVisibility) {
        return getMinVisibility(curVisibility, getExtInheritanceVisibility(inherVisibility));
    }    
    
    public static CsmVisibility mergeChildInheritanceVisibility(CsmVisibility curVisibility, CsmVisibility inheritBA) {
        return getMinVisibility(curVisibility, getChildInheritanceVisibility(inheritBA));
    }
    
    // get new minimal visibility as result of analyzing input visibilities
    private static CsmVisibility getMinVisibility(CsmVisibility vis1, CsmVisibility vis2) {
        assert (vis1 != null && vis2 != null);
        int visInt1 = visToInt(vis1);
        int visInt2 = visToInt(vis2);
        int newMinVis = Math.max(visInt1, visInt2);
        return intToVis(newMinVis);
    }    
    
    // get new maximal visibility as result of analyzing input visibilities
    private static CsmVisibility getMaxVisibility(CsmVisibility vis1, CsmVisibility vis2) {
        assert (vis1 != null && vis2 != null);
        int visInt1 = visToInt(vis1);
        int visInt2 = visToInt(vis2);
        int newMaxVis = Math.min(visInt1, visInt2);
        return intToVis(newMaxVis);
    }
    
    /**
     * gets info, how content of class "clazz" is visible from context defined by
     * "contextDeclaration". Context declaration could be class or function. If
     * it is function => function also could have associated class to check
     * Examples:
     *  - context class could be child of interested class => depending on depth
     *    could be different visibility
     *  - context is null => global context => only public is visible
     *  - context is friend of interested class => everything is visible
     *  - context class is not inherited from interested class and not a friend => global context => only public is visible
     */
    public static CsmVisibility getContextVisibility(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration) {
        return getContextVisibility(clazz, contextDeclaration, CsmVisibility.PUBLIC, false);
    }
    public static CsmVisibility getContextVisibility(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmVisibility defVisibilityValue, boolean checkInheritance) {
        return getContextVisibilityInfo(clazz, contextDeclaration, defVisibilityValue, checkInheritance).visibility;
    }

    public static final class ContextVisibilityInfo {
        public final  CsmVisibility visibility;
        public final  boolean friend;

        public ContextVisibilityInfo(CsmVisibility visibility, boolean friend) {
            this.visibility = visibility;
            this.friend = friend;
        }
    }

    public static ContextVisibilityInfo getContextVisibilityInfo(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmVisibility defVisibilityValue, boolean checkInheritance) {
        assert (clazz != null);
        long time = System.currentTimeMillis();
        try {
            CsmClass contextClass = CsmBaseUtilities.getContextClass(contextDeclaration);
            // if we are in the same class => we see everything
            if (areEqualClasses(clazz, contextClass)) {
                return new ContextVisibilityInfo(MAX_VISIBILITY, false);
            }
            // friend has maximal visibility
            if (CsmFriendResolver.getDefault().isFriend(contextDeclaration, clazz)) {
                return new ContextVisibilityInfo(MAX_VISIBILITY, true);
            }
            // nested classes should see at least themselves
            if (isNestedClass(contextClass, clazz)) {
                return new ContextVisibilityInfo(MAX_VISIBILITY, false);
            }
            // from global context only public members are visible, friend is checked above
            // return passed default public visibility
            if (contextClass == null || !checkInheritance) {
                return new ContextVisibilityInfo(defVisibilityValue, false);
            }

            List<CsmInheritance> chain = findInheritanceChain(contextClass, clazz);
            if (chain != null) {
                assert (chain.size() > 0);
                // walk through inheritance chain to find corrected visibility
                // by default we see public and protected members of parent
                CsmVisibility mergedVisibility = CsmVisibility.PROTECTED;
                for (int i = 0; i < chain.size(); i++) {
                    CsmInheritance inherit = chain.get(i);
                    if (i == 0) {
                        // create merged visibility based on child inheritance
                        mergedVisibility = CsmInheritanceUtilities.mergeInheritedVisibility(mergedVisibility, inherit.getVisibility());
                    } else {
                        // create merged visibility based on direct inheritance
                        mergedVisibility = CsmInheritanceUtilities.mergeChildInheritanceVisibility(mergedVisibility, inherit.getVisibility());
                    }
                }
                return new ContextVisibilityInfo(mergedVisibility, false);
            } else {
                // not inherited class see only public, friend was checked above
                // return passed default public visibility
                return new ContextVisibilityInfo(defVisibilityValue, false);
            }
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                time = System.currentTimeMillis() - time;
                LOG.log(Level.FINE, "getContextVisibilityInfo took {0}ms\n", new Object[]{time}); // NOI18N
            }
        }
    }
    
    /**
     * gets chain of inheritance
     * class B : public A {
     * }
     * class C : public B {
     * }
     * class D : public C {
     * } 
     * chain for findInheritanceChain(D, A) will be (D->C, C->B, B->A)
     * if no inheritance => return "null"
     */
    private static List<CsmInheritance> findInheritanceChain(CsmClass child, CsmClass parent) {
        if (child == null || parent == null) {
            return null;
        }
        long time = System.currentTimeMillis();
        try {
            CsmCacheMap cache = CsmCacheManager.getClientCache(InheritanceChainKey.class, INHERITANCE_INITIALIZER);
            Object key = new InheritanceChainKey(child, parent);
            List<CsmInheritance> res = (List<CsmInheritance>) CsmCacheMap.getFromCache(cache, key, null);
            if (res == null) {
                long resolveTime = System.currentTimeMillis();
                res = new ArrayList<CsmInheritance>();
                ClassifiersAntiLoop handledClasses = new ClassifiersAntiLoop();
                if (!findInheritanceChain(child, parent, res, handledClasses)) {
                    res = Collections.emptyList();
                }
                resolveTime = System.currentTimeMillis() - resolveTime;
                if (cache != null) {
                    cache.put(key, CsmCacheMap.toValue(res, resolveTime));
                }
            }
            if (res.isEmpty()) {
                res = null;
            }
            return res;
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                time = System.currentTimeMillis() - time;
                LOG.log(Level.FINE, "findInheritanceChain took {0}ms:\n\tchild={1}\n\tparent={2}\n", new Object[]{time, getPosition(child), getPosition(parent)});
            }
        }
    }

    /** Can we convert child to parent */
    public static boolean isAssignableFrom(CsmClass child, CsmClass parent) {
        assert (parent != null);
        if (areEqualClasses(parent, child)) {
            return true;
        }
        if (CsmKindUtilities.isTemplate(child) && CsmKindUtilities.isTemplateInstantiation(parent)) {
            Collection<CsmOffsetableDeclaration> baseTemplates = CsmInstantiationProvider.getDefault().getBaseTemplate(child);
            CsmInstantiation parentInstantiation = (CsmInstantiation) parent;
            CsmOffsetableDeclaration parentTemplateDeclaration = parentInstantiation.getTemplateDeclaration();            
            if (baseTemplates.contains(parentTemplateDeclaration)) {
                return true;
            }
        }
        List<CsmInheritance> chain = CsmInheritanceUtilities.findInheritanceChain(child, parent);
        return chain != null;
    }
    
    private static boolean findInheritanceChain(CsmClass child, CsmClass parent, 
                                        List<CsmInheritance> res, 
                                        ClassifiersAntiLoop handledClasses) {
        // remember visited childs
        // quick exit, if already handled before
        if (child == null || !handledClasses.add(child)) {
            return false;
        }
        // quick escapement if child doesn't have base classes
        Collection base = child.getBaseClasses();
        if (base == null || base.size() == 0) {
            return false;
        }
        // check if direct child of parent
        CsmInheritance inh = findDirectInheritance(child, parent);
        if (inh != null) {
            res.add(inh);
            return true;
        }
        // TODO: we have to find all chains and then select the right one
        // for now we are looking for the first found chain
        List<CsmInheritance> bestChain = null;
        CsmInheritance bestInh = null;
        for (Iterator it = base.iterator(); it.hasNext();) {
            CsmInheritance curInh = (CsmInheritance) it.next();
            List<CsmInheritance> curInhRes = new ArrayList<CsmInheritance>();
            if (findInheritanceChain(getCsmClass(curInh), parent, curInhRes, handledClasses)) {
                bestChain = curInhRes;
                bestInh = curInh;
                // TODO: comment as above
                // for now we stop on the first found chain
                break;
            }
        }        
        if (bestChain != null) {
            assert (bestChain.size() > 0);
            res.add(bestInh);
            res.addAll(bestChain);            
            return true;
        } 
        return false;
    }

    public static CsmClass getCsmClass(CsmInheritance inh) {
        CsmClass out = null;
        if (threadLocalInheritanceAntiloop.get().enter(inh)) {
            try {
                CsmClassifier classifier = inh.getClassifier();
                classifier = CsmBaseUtilities.getOriginalClassifier(classifier, inh.getContainingFile());
                if (CsmKindUtilities.isClass(classifier)) {
                    out = (CsmClass)classifier;
                }
            } finally {
                threadLocalInheritanceAntiloop.get().exit(inh);
            }
        }         
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getCsmClass for\n{0}\n=>getCsmClass=>\n{1}", new Object[] {inh, out});
        }
        return out;
    }

    private static CsmInheritance findDirectInheritance(CsmClass child, CsmClass parent) {
        assert (parent != null);
        Collection base = child.getBaseClasses();
        if (base != null && base.size() > 0) {
            for (Iterator it = base.iterator(); it.hasNext();) {
                CsmInheritance curInh = (CsmInheritance) it.next();
                if (areEqualClasses(parent, getCsmClass(curInh))) {
                    return curInh;
                }
            }
        }
        return null;
    }

    private static boolean areEqualClasses(CsmClass clazz, CsmClass contextClass) {
        assert clazz != null;
        if (clazz.equals(contextClass)) {
            return true;
        } else if (contextClass != null) {
            // TODO: may be move such logic into equals methods of instantiations?
            if (CsmKindUtilities.isTemplate(clazz) ||
                    CsmKindUtilities.isTemplateInstantiation(clazz)) {
                return clazz.getUniqueName().equals(contextClass.getUniqueName());
            }
        }
        return false;
    }

    private static boolean isNestedClass(CsmClass inner, CsmClass outer) {
        return inner != null && outer != null &&
               CharSequenceUtilities.startsWith(inner.getQualifiedName(),outer.getQualifiedName());
    }

    private static CharSequence getPosition(CsmClass obj) {
        CsmFile file = obj.getContainingFile();
        String position = file.getAbsolutePath().toString();
        int[] lineColumn = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, obj.getStartOffset());
        if (lineColumn != null) {
            position = "line=" + lineColumn[0] + ":" + lineColumn[1] + " " + position; // NOI18N
        }
        return position;
    }
    
    private static final Callable<CsmCacheMap> INHERITANCE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("INHERITANCE Cache", 1); // NOI18N
        }
    };     

    private static final class InheritanceChainKey {
        private final CsmClass child;
        private final CsmClass parent;

        public InheritanceChainKey(CsmClass child, CsmClass parent) {
            this.child = child;
            this.parent = parent;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 61 * hash + this.child.hashCode();
            hash = 61 * hash + this.parent.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InheritanceChainKey other = (InheritanceChainKey) obj;
            if (!this.child.equals(other.child)) {
                return false;
            }
            return this.parent.equals(other.parent);
        }            
    }
}
