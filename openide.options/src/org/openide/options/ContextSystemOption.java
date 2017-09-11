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
package org.openide.options;

import org.openide.util.io.NbMarshalledObject;

import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextProxy;
import java.beans.beancontext.BeanContextSupport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;


/** Provides a group of system options with this as the parent.
* You must still implement {@link SystemOption#displayName}, at least.
* The suboptions are automatically saved as a group.
*
* @author Ales Novak
*/
public abstract class ContextSystemOption extends SystemOption implements BeanContextProxy {
    /** beanContext property's key. */
    private static Object ctxt = new Object();
    private static final long serialVersionUID = -781528552645947127L;

    /** Reference to the bean context describing the structure of this option tree.
     * @deprecated To obtain bean context use {@link #getBeanContextProxy}.
     */
    protected BeanContext beanContext;

    /** Default constructor. */
    public ContextSystemOption() {
        // backward compatability
        beanContext = getBeanContext();
    }

    /** Add a new option to the set.
    * @param so the option to add
    */
    public final void addOption(SystemOption so) {
        getBeanContext().add(so);
    }

    /** Remove an option from the set.
    * @param so the option to remove
    */
    public final void removeOption(SystemOption so) {
        getBeanContext().remove(so);
    }

    /** Get all options in the set.
    * @return the options
    */
    public final SystemOption[] getOptions() {
        // [WARNING] call to getBeanContext().toArray() can return either SystemOptions
        // or something of another type (I detected BeanContextSupport)
        // It requires deep investigation ...
        int i;

        // [WARNING] call to getBeanContext().toArray() can return either SystemOptions
        // or something of another type (I detected BeanContextSupport)
        // It requires deep investigation ...
        int j;
        SystemOption[] options;

        Object[] objs = getBeanContext().toArray();

        // filter out everything not SystemOption
        for (i = 0, j = 0; i < objs.length; i++) {
            if (objs[i] instanceof SystemOption) {
                if (i > j) {
                    objs[j] = objs[i];
                }

                j++;
            }
        }

        options = new SystemOption[j];
        System.arraycopy(objs, 0, options, 0, j);

        return options;
    }

    /* Method from interface BeanContextProxy.
    * @return a BeanContext - tree of options
    */
    public final BeanContextChild getBeanContextProxy() {
        return getBeanContext();
    }

    private BeanContext getBeanContext() {
        return (BeanContext) getProperty(ctxt);
    }

    protected void initialize() {
        super.initialize();
        this.putProperty(ctxt, new OptionBeanContext(this));
    }

    /* Writes the beanContext variable to an ObjectOutput instance.
    * @param out
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        Object[] objects = getBeanContext().toArray();
        Arrays.sort(objects, new ClassComparator());

        for (int i = 0; i < objects.length; i++) {
            out.writeObject(new NbMarshalledObject(objects[i]));
        }

        out.writeObject(null);
    }

    /* Reads the beanContext variable from an ObjectInpuit instance.
    * @param in
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        Object obj = in.readObject();

        if (obj instanceof BeanContext) {
            // old version of serialization
            // XXX does this really work??
            beanContext = (BeanContext) obj;
        } else {
            // new version with safe serialization
            BeanContext c = getBeanContext();

            while (obj != null) {
                NbMarshalledObject m = (NbMarshalledObject) obj;

                // #18626 fix: deserialization of the context option should survive
                // deserialization of its children. They can belong to disabled
                // or removed modules.
                try {
                    c.add(m.get());
                } catch (Exception e) {
                    Logger.getLogger(ContextSystemOption.class.getName()).log(Level.WARNING, null, e);
                } catch (LinkageError e) {
                    Logger.getLogger(ContextSystemOption.class.getName()).log(Level.WARNING, null, e);
                }

                // read next
                obj = in.readObject();
            }
        }
    }

    /** Comparator of class names of objects. It is used in
     *  <code>writeExternal</code>.
     */
    private static class ClassComparator implements Comparator {
        ClassComparator() {
        }

        /** It Compares name of classes of two objects */
        public int compare(Object o1, Object o2) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
    }

    /** A hierarchy of SystemOptions.
    * Allows add/remove SystemOption beans only.
    * @warning many methods throws UnsupportedOperationException like BeanContextSupport does.
    */
    private static class OptionBeanContext extends BeanContextSupport implements PropertyChangeListener {
        private static final long serialVersionUID = 3532434266136225440L;
        private ContextSystemOption parent = null;

        public OptionBeanContext(ContextSystemOption p) {
            parent = p;
        }

        /** Overridden from base class.
        * @exception IllegalArgumentException if not targetChild instanceof SystemOption
        */
        public boolean add(Object targetChild) {
            if (!(targetChild instanceof SystemOption)) {
                throw new IllegalArgumentException("Not a SystemOption: " + targetChild); // NOI18N
            }

            boolean b = super.add(targetChild);

            if (b) {
                ((SystemOption) targetChild).addPropertyChangeListener(this);
            }

            return b;
        }

        public boolean remove(Object targetChild) {
            if (!(targetChild instanceof SystemOption)) {
                throw new IllegalArgumentException("Not a SystemOption: " + targetChild); // NOI18N
            }

            boolean b = super.remove(targetChild);

            if (b) {
                ((SystemOption) targetChild).removePropertyChangeListener(this);
            }

            return b;
        }

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (parent != null) {
                parent.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        }
    }
}
