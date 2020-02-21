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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

import java.lang.reflect.InvocationTargetException;

import java.util.Enumeration;

import java.beans.PropertyEditor;

import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Sheet;

/**
 * A Node to be used as a model for a single DummyPropertySheet.
 *
 * This Node allows detection of node dirtiness.
 * If any property of this Node is changed
 * DummyPropertySheet.Listener.nodeChanged() will be called.
 *
 * The way this is achieved though is a bit insane...
 * We use FilterNode which has it's getPropertySets() overriden to return
 * filtered Node.PropertySet's. 
 * The filtered Node.PropertySet has it's getProperties() overriden to return
 * filtered Node.Property's.
 * The filtered Node.Property has it's setValue() and restoreDefaultValue()
 * overriden to call the NodeListener.
 * 
 * The filter objects are cached and only regenerated if the original 
 * objects identity (but not contents) changes.
 *
 * You'd think that one could add a property change listener to a Node or
 * a Sheet but I couldn't get any of that to work. I think it relies
 * on the properties properly firing their change notifications +
 * proper registration. A grep through AbstractNode shows no publicly
 * available fireProperyChange() methods. perhaps BeanNode does all of
 * that but we're not working with beans.
 */

class DummyNode extends FilterNode {

    private final MyNode original;
    private final DummyPropertySheet.Listener listener;

    private Node.PropertySet[] cachedOriginalSet;
    private Node.PropertySet[] newSet;

    public DummyNode(String name, DummyPropertySheet.Listener listener) {
	super(new MyNode(name));
	this.listener = listener;
	original = (MyNode) getOriginal();
    }

    private static class MyNode extends AbstractNode {
	public MyNode(String name) {
	    super(Children.LEAF);
	    setName(name);
	}

	// "override" AbstractNode
	// setSheet() is protected final.
	public void setSheetAnyway(Sheet sheet) {
	    setSheet(sheet);
	}
    }

    // after AbstractNode
    void setSheet(Sheet sheet) {
	original.setSheetAnyway(sheet);		// delegate to original
    }

    // override FilterNode
    @Override
    public Node.PropertySet[] getPropertySets() {

	if (false) {
	    // For behaviour of performance comparison try this.
	    // Note, dialogs OK's and Apply's might not come on.
	    return original.getPropertySets();

	} else {
	    Node.PropertySet[] originalSet = original.getPropertySets();
	    if (originalSet != cachedOriginalSet) {
		cachedOriginalSet = originalSet;
		newSet = new Node.PropertySet[originalSet.length];
		for (int sx = 0; sx < originalSet.length; sx++) {
		    newSet[sx] = new FilterPropertySet(originalSet[sx],
						       listener);
		}
	    }
	    return newSet;
	}
    }

    private static class FilterProperty<T> extends Node.Property<T> {
	private final Node.Property<T> original;
	private final DummyPropertySheet.Listener listener;

	public FilterProperty(Node.Property<T> original,
			      DummyPropertySheet.Listener listener) {
	    super(original.getValueType());
	    this.original = original;
	    this.listener = listener;
	}

	//
	// Methods of Node.Property
	//
        @Override
	public boolean canRead() {
	    return original.canRead();
	}

        @Override
	public boolean canWrite() {
	    return original.canWrite();
	}

        @Override
	public boolean equals(Object property) {
	    if (! (property instanceof Node.Property))
		return false;
	    return original.equals(property);
	}

        @Override
	public String getHtmlDisplayName() {
	    return original.getHtmlDisplayName();
	}

        @Override
	public PropertyEditor getPropertyEditor() {
	    return original.getPropertyEditor();
	}

        @Override
	public T getValue() throws IllegalAccessException,
					InvocationTargetException {
	    return original.getValue();
	}

        @Override
	public Class<T> getValueType() {
	    return original.getValueType();
	}

        @Override
	public int hashCode() {
	    return original.hashCode();
	}

        @Override
	public boolean isDefaultValue() {
	    return original.isDefaultValue();
	}

        @Override
	public void restoreDefaultValue() throws IllegalAccessException,
						 InvocationTargetException {
	    original.restoreDefaultValue();
	    listener.propertyChanged();
	}

        @Override
	public void setValue(T val) throws IllegalAccessException,
						InvocationTargetException {
	    /* DEBUG
	    System.out.printf("FilterProperty.setValue() for %s\n",
		getName());
	    */
	    original.setValue(val);
	    listener.propertyChanged();
	}

        @Override
	public boolean supportsDefaultValue() {
	    return original.supportsDefaultValue();
	}

	//
	// Methods of FeatureDescriptor
	//
	@Override
	public Enumeration<String> attributeNames() {
	    return original.attributeNames();
	}

	@Override
	public String getDisplayName() {
	    return original.getDisplayName();
	}

	@Override
	public String getName() {
	    return original.getName();
	}

	@Override
	public String getShortDescription() {
	    return original.getShortDescription();
	}

	@Override
	public Object getValue(String attributeName) {
	    return original.getValue(attributeName);
	}

	@Override
	public boolean isExpert() {
	    return original.isExpert();
	}

	@Override
	public boolean isHidden() {
	    return original.isHidden();
	}

	@Override
	public boolean isPreferred(){
	    return original.isPreferred();
	}

	@Override
	public void setDisplayName(String displayName) {
	    original.setDisplayName(displayName);
	}

	@Override
	public void setExpert(boolean expert) {
	    original.setExpert(expert);
	}

	@Override
	public void setHidden(boolean hidden) {
	    original.setHidden(hidden);
	}

	@Override
	public void setName(String name) {
	    original.setName(name);
	}

	@Override
	public void setPreferred(boolean preferred) {
	    original.setPreferred(preferred);
	}

	@Override
	public void setShortDescription(String text) {
	    original.setShortDescription(text);
	}

	@Override
	public void setValue(String attributeName, Object value) {
	    original.setValue(attributeName, value);
	}

	//
	// Methods of Object
	//
	@Override
	protected Object clone() {
	    assert false : "Node.Property.clone(): NOT IMPLEMENTED";
	    return null;
	}

	@Override
	public String toString() {
	    return original.toString();
	}

	@Override
	protected void finalize() {
	    assert false : "Node.Property.finalize(): NOT IMPLEMENTED";
	}

	/* Already overriden
	public boolean equals(Object obj);
	public int hashCode();
	*/

	/* no need to override 
	public Class<? extends Object> getClass();
	*/

	/* final
	public void notify();
	public void notifyAll();
	public void wait();
	public void wait(long timeout);
	public void wait(long timeout, int nanos);
	*/
    }

    private static class FilterPropertySet extends Node.PropertySet {
	private final Node.PropertySet original;
	private final DummyPropertySheet.Listener listener;

	public FilterPropertySet(Node.PropertySet original,
				 DummyPropertySheet.Listener listener) {
	    this.original = original;
	    this.listener = listener;
	}

	//
	// Methods of Node.PropertySet
	//
	@Override
	public boolean equals(Object propertySet) {
	    if (! (propertySet instanceof Node.PropertySet))
		return false;
	    return original.equals(propertySet);
	}

	@Override
	public String getHtmlDisplayName() {
	    return original.getHtmlDisplayName();
	}

	private Node.Property<?>[] cachedOriginalProperties;
	private Node.Property<?>[] newProperties;

        @Override
	public Node.Property<?>[] getProperties() {
	    Node.Property<?>[] originalProperties =
		original.getProperties();
	    if (originalProperties != cachedOriginalProperties) {
		cachedOriginalProperties = originalProperties;
		newProperties =
		    new Node.Property<?>[originalProperties.length];
		for (int px = 0; px < originalProperties.length; px++) {
		    // We can't at compile-time determine which FilterProperty
		    // to call because the types of originalProperties[px] are
		    // only dynamically available.
		    @SuppressWarnings("unchecked")
		    Node.Property<?>filter = new FilterProperty(originalProperties[px], listener);
		    newProperties[px] = filter;
		}
	    }
	    return newProperties;
	}

	@Override
	public int hashCode() {
	    return original.hashCode();
	}

	//
	// Methods of FeatureDescriptor
	//
	@Override
	public Enumeration<String> attributeNames() {
	    return original.attributeNames();
	}

	@Override
	public String getDisplayName() {
	    return original.getDisplayName();
	}

	@Override
	public String getName() {
	    return original.getName();
	}

	@Override
	public String getShortDescription() {
	    return original.getShortDescription();
	}

	@Override
	public Object getValue(String attributeName) {
	    return original.getValue(attributeName);
	}

	@Override
	public boolean isExpert() {
	    return original.isExpert();
	}

	@Override
	public boolean isHidden() {
	    return original.isHidden();
	}

	@Override
	public boolean isPreferred() {
	    return original.isPreferred();
	}

	@Override
	public void setDisplayName(String displayName) {
	    original.setDisplayName(displayName);
	}

	@Override
	public void setExpert(boolean expert) {
	    original.setExpert(expert);
	}

	@Override
	public void setHidden(boolean hidden) {
	    original.setHidden(hidden);
	}

	@Override
	public void setName(String name) {
	    original.setName(name);
	}

	@Override
	public void setPreferred(boolean preferred) {
	    original.setPreferred(preferred);
	}

	@Override
	public void setShortDescription(String text) {
	    original.setShortDescription(text);
	}

	@Override
	public void setValue(String attributeName, Object value) {
	    original.setValue(attributeName, value);
	}

	//
	// Methods of Object
	//
	@Override
	protected Object clone() {
	    assert false : "FilterPropertySet.clone(): NOT IMPLEMENTED";
	    return null;
	}

	@Override
	public String toString() {
	    return original.toString();
	}

	@Override
	protected void finalize() {
	    assert false : "FilterPropertySet.finalize(): NOT IMPLEMENTED";
	}

	/* Already overriden
	public boolean equals(Object obj);
	public int hashCode();
	*/

	/* no need to override 
	public Class<? extends Object> getClass();
	*/

	/* final
	public void notify();
	public void notifyAll();
	public void wait();
	public void wait(long timeout);
	public void wait(long timeout, int nanos);
	*/
    }

}

