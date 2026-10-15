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

package org.netbeans.api.editor.settings;

import java.util.List;
import javax.swing.KeyStroke;

/**
 * Variant of <code>JTextComponent.KeyBinding</code> to hold several successive keystrokes.
 *
 * @author Miloslav Metelka, Martin Roskanin
 */

public final class MultiKeyBinding {

    /**
     * List of the keystrokes that invoke the action.
     */
    private List<KeyStroke> keyStrokeList;

    /**
     * The name of the action to be invoked by the keyStrokes.
     */
    private String actionName;


    /**
     * Constructor for assigning keystroke sequence to action
     * 
     * @param keyStrokes non-null successive keystrokes that must be pressed in order
     *    to invoke action. The passed array must not be modified by the caller.
     * @param actionName non-null name of the action that will be invoked.
     */
    public MultiKeyBinding(KeyStroke[] keyStrokes, String actionName) {
        if (keyStrokes == null) {
            throw new NullPointerException("keyStrokes cannot be null"); // NOI18N
        }
        if (actionName == null) {
            throw new NullPointerException("actionName cannot be null"); // NOI18N
        }
        this.keyStrokeList = new UnmodifiableArrayList<KeyStroke>(keyStrokes);
        this.actionName = actionName;
    }

    /**
     * Constructor for array single keystroke to action assignment.
     */
    public MultiKeyBinding(KeyStroke keyStroke, String actionName) {
        this(new KeyStroke[] { keyStroke }, actionName);
        if (keyStroke == null) {
            throw new NullPointerException("keyStroke cannot be null"); // NOI18N
        }
    }
    
    /**
     * Get the key of this keybinding at the given index of the keystroke sequence.
     * 
     * @param index &gt;=0 and &lt;{@link #getKeyStrokeCount()} index.
     * @return keystroke at the given index.
     * @see #getKeyStrokeCount()
     */
    public KeyStroke getKeyStroke(int index) {
        return keyStrokeList.get(index);
    }
    
    /**
     * Get total number of keystrokes contained in this keybinding sequence.
     *
     * @return &gt;=0 total count of keystrokes.
     */
    public int getKeyStrokeCount() {
        return keyStrokeList.size();
    }
    
    /**
     * Get list of keystrokes represented by this keybinding.
     *
     * @return non-null unmodifiable list of the keystrokes.
     */
    public List<KeyStroke> getKeyStrokeList() {
        return keyStrokeList;
    }
    
    /**
     * Get the name of the action which this keybinding triggers.
     *
     * @return action non-null name of the action triggered by the sequence
     *  of the keystrokes contained in this keybinding.
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Two keybindings are equal if and only if they represent
     * the same keystrokes sequence and the same action name.
     */
    public boolean equals(Object o) {
        if (o instanceof MultiKeyBinding) {
            MultiKeyBinding kb = (MultiKeyBinding)o;

            // Compare action names
            if (actionName == null) {
                if (kb.actionName != null) {
                    return false;
                }
            } else {
                if (!actionName.equals(kb.actionName)) {
                    return false;
                }
            }

            // Action names match, now compare action keys
            return keyStrokeList.equals(kb.keyStrokeList);
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        for (int i = 0; i < getKeyStrokeCount(); i++){
            result = 37*result + getKeyStroke(i).hashCode();
        }
        if (actionName != null) {
            result = 37*result + actionName.hashCode();
        }
        return result;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("keys("); // NOI18N
        for (KeyStroke ks : keyStrokeList) {
            sb.append(ks);
        }
        sb.append("), actionName="); // NOI18N
        sb.append(actionName);
        return sb.toString();
    }

    private static final class UnmodifiableArrayList<E> extends java.util.AbstractList<E>
    implements java.util.RandomAccess, java.io.Serializable {

        private static final long serialVersionUID = 0L;

        private E[] array;

	UnmodifiableArrayList(E[] array) {
            if (array == null) {
                throw new NullPointerException();
            }
	    this.array = array;
	}

	public int size() {
	    return array.length;
	}

	public Object[] toArray() {
	    return (Object[]) array.clone();
	}

	public E get(int index) {
	    return array[index];
	}

        public int indexOf(Object o) {
            if (o == null) {
                for (int i = 0; i < array.length; i++)
                    if (array[i] == null)
                        return i;
            } else {
                for (int i = 0; i < array.length; i++)
                    if (o.equals(array[i]))
                        return i;
            }
            return -1;
        }

        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }
        
        public boolean equals(Object o) {
            return (o instanceof UnmodifiableArrayList)
                ? java.util.Arrays.equals(this.array, ((UnmodifiableArrayList)o).array)
                : super.equals(o);
        }

    }

}
