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
/**
 *
 * @author Nam Nguyen
 */

package org.netbeans.modules.xml.xam.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class SyncUnit {
    private final DocumentComponent target;
    private List<ChangeInfo> changes = new ArrayList<ChangeInfo>();
    private List<DocumentComponent> toRemove = new ArrayList<DocumentComponent>();
    private List<DocumentComponent> toAdd = new ArrayList<DocumentComponent>();
    private Map<String, Attr> removedAttributes = new HashMap<String, Attr>();
    private Map<String, Attr> addedAttributes = new HashMap<String, Attr>();
    private boolean componentChanged;
    private boolean hasTextContentChanges = false;
    private Set<String> nonDomainedChanges = new HashSet<String>(); //tagname of top level non-domained element
    
    public SyncUnit(DocumentComponent syncTarget) {
        if (syncTarget == null) {
            throw new IllegalArgumentException("Null syncTarget");
        }
        target = syncTarget;
    }
    
    public void addChange(ChangeInfo change) {
        if (! target.referencesSameNode(change.getParent())) {
            throw new IllegalArgumentException("ChangeInfo does not match target id");
        }
        changes.add(change);
        if (change.getChangedNode() instanceof Attr)  {
            Attr attr = (Attr) change.getChangedNode();
            if (change.isAdded()) {
                addToAddedAttributes(attr);
            } else {
                addToRemovedAttributes(attr);
            }
        } else if (! change.isDomainElement()) {
            Node actualChanged = change.getActualChangedNode();
            if (! (actualChanged instanceof Attribute || actualChanged instanceof Element)) {
                // should be text, cdata, comment...
                if (actualChanged.getNodeType() != Node.TEXT_NODE || 
                    ((Text)actualChanged).getNodeValue().trim().length() != 0) {
                    setHasTextContentChanges(true);
                    addNonDomainedElementChange(change);
                }
            } else {
                addNonDomainedElementChange(change);
            }
        }
    }
    
    public List<ChangeInfo> getChanges() { return changes; }
    public DocumentComponent getTarget() { return target; }
    public List<DocumentComponent> getToRemoveList() { return toRemove; }
    public void addToRemoveList(DocumentComponent c) { 
        if (c == null) {
            throw new IllegalArgumentException("Null component");
        }
        toRemove.add(c); 
    }
    public List<DocumentComponent> getToAddList() { return toAdd; }
    public void addToAddList(DocumentComponent c) { 
        if (c == null) {
            throw new IllegalArgumentException("Null component");
        }
        toAdd.add(c); 
    }
    public void setComponentChanged(boolean v) { componentChanged = v; }
    public boolean isComponentChanged() { return componentChanged; }
    public void addToAddedAttributes(Attr attr) {
        addedAttributes.put(attr.getName(), attr);
    }
    public Map<String,Attr> getAddedAttributes() {
        return addedAttributes;
    }
    
    public Map<String,Attr> getRemovedAttributes() {
        return removedAttributes;
    }
    
    public void addToRemovedAttributes(Attr attr) {
        removedAttributes.put(attr.getName(), attr);
    }

    public void merge(SyncUnit su) {
        if (target != su.getTarget()) {
            throw new IllegalArgumentException("Invalid sync unit for merge");
        }
        changes.addAll(su.getChanges());
        for (String name : su.getRemovedAttributes().keySet()) {
            addToRemovedAttributes(su.getRemovedAttributes().get(name));
        }
        for (String name : su.getAddedAttributes().keySet()) {
            addToAddedAttributes(su.getAddedAttributes().get(name));
        }
        
        if (! su.getToAddList().isEmpty()) {
            HashSet<Element> addSet = new HashSet<Element>();
            for (DocumentComponent component : toAdd) {
                addSet.add(component.getPeer());
            }
            for (DocumentComponent component : su.getToAddList()) {
                if (! addSet.contains(component.getPeer())) {
                    toAdd.add(component);
                }
            }
        }

        for (DocumentComponent component : su.getToRemoveList()) {
            if (! toRemove.contains(component)) {
                toRemove.add(component);
            }
        }
    }
    
    public void updateTargetReference() {
        AbstractDocumentComponent component = (AbstractDocumentComponent) target;
        if (component != null) {
            component.updateReference(getParentToRootPath());
        }
    }
    
    public ChangeInfo getLastChange() {
        if (changes.size() > 0) {
            return changes.get(changes.size()-1);
        } else {
            return null;
        }
    }
    
    public List<Element> getParentToRootPath() {
        if (getLastChange() == null) {
            return Collections.emptyList();
        } else {
            return getLastChange().getParentToRootPath();
        }
    }
    
    public boolean hasTextContentChanges() {
        return hasTextContentChanges;
    }

    public void setHasTextContentChanges(boolean val) {
        hasTextContentChanges = val;
    }
    
    public Set<String> getNonDomainedElementChanges() {
        return nonDomainedChanges;
    }

    public void addNonDomainedElementChange(ChangeInfo change) {
        if (change.getChangedNode() instanceof Element) {
            nonDomainedChanges.add(((Element)change.getChangedNode()).getTagName());
        }
    }
    
    public boolean hasWhitespaceChangeOnly() {
        for (ChangeInfo ci : getChanges()) {
            if (ci.isDomainElement()) {
                continue;
            }
            Node n = ci.getActualChangedNode();
            if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text != null && text.trim().length() > 0) {
                    return false;
                }
            } else if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                String name =  ((Attr) n).getName();
                Attr removed = getRemovedAttributes().get(name);
                if (removed == null) {
                    return false;
                }
                Attr added = getAddedAttributes().get(name);
                if (added == null) {
                    return false;
                }
                if (removed.getValue() == null || 
                    ! removed.getValue().equals(added.getValue())) {
                    return false;
                }
            } else {
                // node type must be either element or comment or cdata...
                return false;
            }
        }
        return true;
    }
}
