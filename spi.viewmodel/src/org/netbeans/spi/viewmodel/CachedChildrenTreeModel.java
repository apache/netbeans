/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.viewmodel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;

/**
 * A TreeModel, which caches children objects and allow seamless update of children objects.
 * 
 * @author Martin Entlicher
 * @since 1.49
 */
public abstract class CachedChildrenTreeModel extends Object implements TreeModel, AsynchronousModelFilter {

    private final Map<Object, ChildrenTree> childrenCache = new WeakHashMap<Object, ChildrenTree>();
    private final Set<Object>   childrenToRefresh = new HashSet<Object>();
    
    @Override
    public Executor asynchronous(Executor original, CALL asynchCall, Object node) throws UnknownTypeException {
        if (CALL.CHILDREN.equals(asynchCall)) {
            boolean cache = cacheChildrenOf(node);
            if (cache) {
                synchronized (childrenCache) {
                    if (childrenToRefresh.remove(node)) {
                        childrenCache.remove(node);
                        return original;
                    }
                    if (childrenCache.containsKey(node)) {
                        return AsynchronousModelFilter.CURRENT_THREAD;
                    }
                }
            }
        }
        return original;
    }

    @Override
    public final Object[] getChildren (Object o, int from, int to)
    throws UnknownTypeException {
        Object[] ch;
        boolean cache = cacheChildrenOf(o);
        if (cache) {
            ChildrenTree cht;
            synchronized (childrenCache) {
                //ch = (List) childrenCache.get(o);
                cht = childrenCache.get(o);
            }
            if (cht != null) {
                ch = cht.getChildren();
            } else {
                ch = null;
            }
        } else ch = null;
        if (ch == null) {
            ch = computeChildren(o);
            if (ch == null) {
                throw new UnknownTypeException (o);
            } else {
                if (cache) {
                    ChildrenTree cht = new ChildrenTree(o);
                    cht.setChildren(ch);
                    synchronized (childrenCache) {
                        childrenCache.put(o, cht);
                    }
                }
            }
        }
        ch = reorder(ch);
        int l = ch.length;
        from = Math.min(l, from);
        to = Math.min(l, to);
        if (from == 0 && to == l) {
            return ch;
        } else {
            Object[] ch1 = new Object[to - from];
            System.arraycopy(ch, from, ch1, 0, to - from);
            ch = ch1;
        }
        return ch;
    }
    
    /**
     * Compute the children nodes. This is called when there are no children
     * cached for this node only.
     * @param node The node to compute the children for
     * @return The list of children
     * @throws UnknownTypeException When this implementation is not able to
     *         resolve children for given node type
     */
    protected abstract Object[] computeChildren(Object node) throws UnknownTypeException;
    
    /**
     * Can be overridden to decide which nodes to cache and which not.
     * @param node The node
     * @return <code>true</code> when the children of this node should be cached,
     *         <code>false</code> otherwise. The default implementation returns
     *         <code>true</code> always.
     */
    protected boolean cacheChildrenOf(Object node) {
        return true;
    }

    /**
     * Force a refresh of the cache.
     * @param node The node to refresh the cache for.
     */
    protected final void refreshCache(Object node) {
        synchronized (childrenCache) {
            childrenToRefresh.add(node);
        }
    }

    /**
     * Clear the entire cache.
     */
    protected final void clearCache() {
        synchronized (childrenCache) {
            childrenCache.clear();
            childrenToRefresh.clear();
        }
    }
    
    /**
     * Allows to reorder the children. This is called each time the children
     * are requested, even when they're cached.
     * @param nodes The original nodes returned by {@link #computeChildren(java.lang.Object)}
     *              or by the cache.
     * @return The reordered nodes. The default implementation returns the original nodes.
     */
    protected Object[] reorder(Object[] nodes) {
        return nodes;
    }
    
    /**
     * Force to recompute all cached children.
     * @throws UnknownTypeException When this implementation is not able to
     *         resolve children for some node type
     */
    protected final void recomputeChildren() throws UnknownTypeException {
        recomputeChildren(getRoot());
    }
    
    /**
     * Force to recompute children cached for the given node.
     * @param node The node to recompute the children for
     * @throws UnknownTypeException When this implementation is not able to
     *         resolve children for the given node type
     */
    protected final void recomputeChildren(Object node) throws UnknownTypeException {
        ChildrenTree cht;
        Set keys;
        synchronized (childrenCache) {
            cht = childrenCache.get(node);
            keys = childrenCache.keySet();
        }
        if (cht != null) {
            Object[] oldCh = cht.getChildren();
            Object[] newCh = computeChildren(node);
            cht.setChildren(newCh);
            for (int i = 0; i < newCh.length; i++) {
                if (keys.contains(newCh[i])) {
                    recomputeChildren(newCh[i]);
                }
            }
        }
    }
    
    private final static class ChildrenTree {
        
        //private Object node;
        private Object[] ch;
        
        public ChildrenTree(Object node) {
            //this.node = node;
        }
        
        public void setChildren(Object[] ch) {
            this.ch = ch;
        }
        
        public Object[] getChildren() {
            return ch;
        }
        
    }
    
}
