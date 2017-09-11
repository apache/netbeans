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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

import org.netbeans.modules.dbschema.*;

/** Superclass of nodes representing elements in the database metadata
 * hierarchy. 
 * <p>Element nodes generally:
 * <ul>
 * <li>Have an associated icon, according to {@link #resolveIconBase}.
 * <li>Have a display name based on the element's properties.
 * <li>Have some node properties (displayable on the property sheet), according
 * to the element's properties, and with suitable editors.
 * <li>Permit renames and deletes, if a member element and writeable.
 * <li>As permitted by the element, and a writable flag in the node,
 * permit cut/copy/paste operations, as well as creation of new members.
 * </ul>
 */
public abstract class DBElementNode extends AbstractNode implements	IconStrings, DBElementProperties, Node.Cookie {

	/** Default return value of getIconAffectingProperties method. */
	private static final String[] ICON_AFFECTING_PROPERTIES = new String[]{};

	/** Associated element. */
	protected DBElement element;

	/** Is this node read-only or are modifications permitted? */
	protected boolean writeable;

	/** Listener to forbid its garbage collection */
	private transient PropertyChangeListener listener;

	/** Create a new element node.
	 *
	 * @param element element to represent
	 * @param children child nodes
	 * @param writeable <code>true</code> if this node should allow
	 * modifications.  These include writable properties, clipboard operations,
	 * deletions, etc.
	 */
	public DBElementNode(DBElement element, Children children, boolean writeable) {
		super(children);
		this.element = element;
		this.writeable = writeable;
		setIconBase(resolveIconBase());
        setName(element.getName().getName());
		setDisplayName(element.getName().getName());
		listener = new ElementListener();
		element.addPropertyChangeListener(WeakListeners.propertyChange(listener, element));
	}

	/** Get the currently appropriate icon base.
	 * Subclasses should make this sensitive to the state of the element--for
	 * example, a pk column may have a different icon than a regular one.
	 * The icon will be automatically changed whenever a {@link
	 * #getIconAffectingProperties relevant} change is made to the element. 
	 * @return icon base
	 * @see AbstractNode#setIconBase
	 */
	abstract protected String resolveIconBase();

	/** Get the names of all element properties which might affect the choice of
	 * icon.  The default implementation returns an empty array.
	 * @return the property names, from {@link DBElementProperties}
	 */
	protected String[] getIconAffectingProperties() {
		return ICON_AFFECTING_PROPERTIES;
	}
  
	public HelpCtx getHelpCtx () {
        return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }

	/** Test whether this node can be renamed.
	 * The default implementation assumes it can if this node is {@link
	 * #writeable}.
	 * @return <code>true</code> if this node can be renamed
	 */
	public boolean canRename () {
        return writeable;
    }

	/** Test whether this node can be deleted.
	 * The default implementation assumes it can if this node is {@link
	 * #writeable}.
	 * @return <code>true</code> if this node can be renamed
	 */
	public boolean canDestroy () {
        return writeable;
    }

	/* Copy this node to the clipboard.
	 *
	 * @return {@link ExTransferable.Single} with one flavor, {@link
	 * NodeTransfer#nodeCopyFlavor}
	 * @throws IOException if it could not copy
	 */
	public Transferable clipboardCopy () throws IOException {
		ExTransferable ex = ExTransferable.create(super.clipboardCopy());

		ex.put(new ElementStringTransferable());

		return ex;
	}

	/* Cut this node to the clipboard.
	 *
	 * @return {@link ExTransferable.Single} with one flavor, {@link
	 * NodeTransfer#nodeCopyFlavor}
	 * @throws IOException if it could not cut
	 */
	public Transferable clipboardCut () throws IOException {
		if (!writeable)
			throw new IOException();

		ExTransferable ex = ExTransferable.create(super.clipboardCut());
		ex.put(new ElementStringTransferable());
        
		return ex;
	}

	/** Transferable for elements as String. */
	class ElementStringTransferable extends ExTransferable.Single {
		/** Construct new Transferable for this node. */
		ElementStringTransferable() { super(DataFlavor.stringFlavor); }

		/** @return the data as String */
		protected Object getData() { return element.toString(); }
	}
  
	/** Test whether this node can be copied.
	 * The default implementation returns <code>true</code>.
	 * @return <code>true</code> if it can
	 */
	public boolean canCopy() {
        return true;
    }

	/** Test whether this node can be cut.
	 * The default implementation assumes it can if this node is {@link
	 * #writeable}.
	 * @return <code>true</code> if it can
	 */
	public boolean canCut() {
        return writeable;
    }

	/** Calls super.fireCookieChange. The reason why is redefined
	 * is only to allow the access from this package.
	 */
	void superFireCookieChange() {
        fireCookieChange();
    }

	/** Get a cookie from this node.
	 * First tries the node itself, then {@link DBElement#getCookie}.
	 * Since {@link DBElement} implements <code>Node.Cookie</code>, it is
	 * possible to find the element from a node using code such as:
	 * <p><code><pre>
	 * Node someNode = ...;
	 * ColumnElement element = 
	 * 	(ColumnElement)someNode.getCookie(ColumnElement.class);
	 * if (element != null) { ... }
	 * </pre></code>
	 * @param type the cookie class
	 * @return the cookie or <code>null</code>
	 */
	public Node.Cookie getCookie (Class type) {
		Node.Cookie c = super.getCookie(type);

        if (c == null && (type.isAssignableFrom(DBElementProvider.class) || 
            type.isAssignableFrom(DBElement.class)))
        {
            c = new DBElementProvider(element);
        }

		return c;
	}
      
	/** Test for equality.
	 * @return <code>true</code> if the represented {@link DBElement}s are equal
	 */
	public boolean equals (Object o) {
		return ((o instanceof DBElementNode) && (element.equals(((DBElementNode)o).element)));
	}

	/** Get a hash code.
	 * @return the hash code from the represented {@link DBElement}
	 */
	public int hashCode() {
        return element.hashCode();
    }

	/** Create a node property representing the element's name.
	 * @param canW if <code>false</code>, property will be read-only
	 * @return the property.
	 */
	protected Node.Property createNameProperty (boolean canW) {
		return new ElementProp(Node.PROP_NAME, String.class,canW) {
			/** Gets the value */
			public Object getValue () {
                return ((DBElement) element).getName().getFullName();
			}
		};
	}

	void superSetName(String name) {
        super.setName(name);
    }
  
	// ================== Element listener =================================

	/** Listener for changes of the element's property changes.
	 * It listens and changes updates the iconBase and displayName
	 * if the changed property could affect them.
	 */
	private class ElementListener implements PropertyChangeListener {
            public ElementListener () {}
            
		/** Called when any element's property changed.
		*/
		public void propertyChange (PropertyChangeEvent evt) {
			String propName = evt.getPropertyName();

			if (propName == null) {
				setIconBase(resolveIconBase());
			} else {
				// icon
				String[] iconProps = getIconAffectingProperties();
				for (int i = 0; i < iconProps.length; i++)
					if (iconProps[i].equals(propName)) {
						setIconBase(resolveIconBase());
						break;
					}

				if (propName.equals(Node.PROP_NAME)) {
					// set inherited name - this code should rather in 
					// DBMemberElementNode,
					// but we safe one instance of listener for each node
					// if it will be here. [Petr]
					try {
						superSetName(((DBMemberElement)DBElementNode.this.element).getName().toString());
					} catch (ClassCastException e) {
						// it is strange - PROP_NAME has only member element.
					}
				} else {
					if (propName.equals(Node.PROP_COOKIE)) {
						// Fires the changes of the cookies of the element.
						superFireCookieChange();
						return;
					}
				}
			}
			DBElementNode.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
	}

	// ================== Property support for element nodes =================

	/** Property support for element nodes properties.
	 */
	static abstract class ElementProp extends PropertySupport {
		/** Constructs a new ElementProp - support for properties of
		 * element hierarchy nodes.
		 *
		 * @param name The name of the property
		 * @param type The class type of the property
		 * @param canW The canWrite flag of the property
		 */
		public ElementProp (String name, Class type, boolean canW) {
			super(name, type, NbBundle.getMessage(DBElementNode.class, "PROP_" + name), NbBundle.getMessage(DBElementNode.class, "HINT_" + name), true, canW); //NOI18N
		}

		/** Setter for the value. This implementation only tests
		 * if the setting is possible.
		 *
		 * @param val the value of the property
		 * @exception IllegalAccessException when this ElementProp was
		 * constructed like read-only.
		 */
		public void setValue (Object val) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			if (!canWrite())
				throw new IllegalAccessException(NbBundle.getMessage(DBElementNode.class, "MSG_Cannot_Write")); //NOI18N
		}

	}
}
