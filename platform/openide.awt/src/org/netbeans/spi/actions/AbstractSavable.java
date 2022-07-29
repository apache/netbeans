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

package org.netbeans.spi.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.actions.Savable;
import org.netbeans.modules.openide.awt.SavableRegistry;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup.Template;

/** Default implementation of {@link Savable} interface and
 * additional contracts, including dealing with {@link Savable#REGISTRY}.
 * The human presentable name of the object to be saved is provided by
 * implementing {@link #findDisplayName()}. In case this object wants 
 * to be visually represented with an icon, it can also implement {@link Icon}
 * interface (and delegate to {@link ImageUtilities#loadImageIcon(java.lang.String, boolean)}
 * result). Here is example of typical implementation:
 * <pre>
class MySavable extends AbstractSavable {
    private final Object obj;
    public MySavable(Object obj) {
        this.obj = obj;
        register();
    }
    protected String findDisplayName() {
        return "My name is " + obj.toString(); // get display name somehow
    }
    protected void handleSave() throws IOException {
        // save 'obj' somehow
    }
    public boolean equals(Object other) {
        if (other instanceof MySavable) {
            return ((MySavable)other).obj.equals(obj);
        }
        return false;
    }
    public int hashCode() {
        return obj.hashCode();
    }
}
 * </pre>
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 * @since 7.33
 */
public abstract class AbstractSavable implements Savable {
    private static final Logger LOG = Logger.getLogger(Savable.class.getName());
    
    /** Constructor for subclasses. 
     */
    protected AbstractSavable() {
    }

    /** Implementation of {@link Savable#save} contract. Calls
     * {@link #handleSave} and {@link #unregister}.
     * 
     * @throws IOException if call to {@link #handleSave} throws IOException
     */
    @Override
    public final void save() throws IOException {
        Template<AbstractSavable> t = new Template<AbstractSavable>(AbstractSavable.class, null, this);
        for (Savable s : Savable.REGISTRY.lookup(t).allInstances()) {
            if (s == this) {
                handleSave();
                unregister();
                return;
            }
        }
        LOG.log(Level.WARNING, "Savable {0} is not in Savable.REGISTRY! " // NOI18N
                + "Have not you forgotten to call register() in constructor?", getClass()); // NOI18N
    }
    
    /** Finds suitable display name for the object this {@link Savable}
     * represents.
     * @return human readable, localized short string name
     */
    protected abstract String findDisplayName();
    
    /** To be overriden by subclasses to handle the actual save of 
     * the object.
     * 
     * @throws IOException 
     */
    protected abstract void handleSave() throws IOException;

    /** Equals and {@link #hashCode} need to be properly implemented 
     * by subclasses to correctly implement equality contract. 
     * Two {@link Savable}s should be equal
     * if they represent the same underlying object beneath them. Without
     * correct implementation proper behavior of {@link #register()} and
     * {@link #unregister()} cannot be guaranteed.
     * 
     * @param obj object to compare this one to, 
     * @return true or false
     */
    @Override
    public abstract boolean equals(Object obj);

    /** HashCode and {@link #equals} need to be properly implemented
     * by subclasses, so two {@link Savable}s representing the same object
     * beneath are really equal and have the same {@link #hashCode()}.
     * @return integer hash
     * @see #equals(java.lang.Object)
     */
    @Override
    public abstract int hashCode();
    
    
    /** Tells the system to register this {@link Savable} into {@link Savable#REGISTRY}.
     * Only one {@link Savable} (according to {@link #equals(java.lang.Object)} and
     * {@link #hashCode()}) can be in the registry. New call to {@link #register()}
     * replaces any previously registered and equal {@link Savable}s. After this call
     * the {@link Savable#REGISTRY} holds a strong reference to <code>this</code>
     * which prevents <code>this</code> object to be garbage collected until it
     * is {@link #unregister() unregistered} or {@link #register() replaced by
     * equal one}.
     */
    protected final void register() {
        SavableRegistry.register(this);
    }
    
    /** Removes this {@link Savable} from the {@link Savable#REGISTRY} (if it 
     * is present there, by relying on {@link #equals(java.lang.Object)} 
     * and {@link #hashCode()}). 
     */
    protected final void unregister() {
        SavableRegistry.unregister(this);
    }

    @Override
    public final String toString() {
        return findDisplayName();
    }
}
