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
package org.netbeans.modules.payara.tooling.utils;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * String prefix tree used to find <code>String</code> in a set.
 * <p/>
 * This class is not thread safe so external synchronization may be needed in
 * multi threaded environment.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class StringPrefixTree<Type> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Prefix tree internal node representing single character of stored
     * strings.
     */
    private class Node {

        /** Mark state as final. */
        private boolean finalState;

        /** Store value in node. */
        private Type value;

        /** Next character. */
        private TreeMap<Character, Node> next;

        /**
         * Create <code>Character</code> object from given char value and apply
         * case sensitive settings on it.
         * <p/>
         * @param c Character to be used as <Character</code> object value.
         * @return <code>Character</code> object containing given char value
         *         with case sensitive settings rules applied.
         */
        Character toCharacter(final char c) {
            return Character.valueOf(
                    caseSensitive ? c : Character.toUpperCase(c));
        }

        /**
         * Creates an instance of internal tree node in inner state and
         * no stored value.
         */
        Node() {
            this.finalState = false;
            this.next = new TreeMap();
        }

        /**
         * Creates an instance of internal tree node in final state and stored
         * value.
         * <p/>
         * @param value Value to be stored into node.
         */
        Node(Type value) {
            this.finalState = true;
            this.next = new TreeMap();
            this.value = value;
        }

        /**
         * Mark node as final state.
         */
        void setFinal() {
            finalState = true;
        }

        /**
         * Mark node as inner state.
         */
        void setInner() {
            finalState = false;
        }

        /**
         * Store value into node.
         * <p/>
         * Old value is overwritten if exists.
         * <p/>
         * @param value Value to be stored into node.
         */
        void setValue(Type value) {
            this.value = value;
        }
        
        /**
         * Retrieve value from node.
         * <p/>
         * @return Value stored in node or <code>null</code> if no value
         *         is stored.
         */
        Type getValue() {
            return value;
        }

        /**
         * Get node state.
         * <p/>
         * @return Value of <code>true</code> for final state
         *         or <code>false</code> for inner state.
         */
        boolean isFinal() {
            return finalState;
        }

        /**
         * Add next state on transition for given character.
         * <p/>
         * @param c Character to set transition to next state.
         * @param node <code>Node</code> representing next transition.
         */
        void add(char c, Node node) {
            next.put(toCharacter(c), node);
        }

       /**
         * Remove next state on transition for given character.
         * <p/>
         * @param c Character to remove transition to next state.
         */
        Node remove(char c) {
            return next.remove(toCharacter(c));
        }

        /**
         * Get next state on transition for given character.
         * <p/>
         * @param c Character to get transition to next state.
         * @return <code>Node</code> representing next transition
         *         or <code>null</code> if there is no transition.
         */
        Node get(char c) {
            return next.get(toCharacter(c));
        }

        /**
         * Get number of transitions to next states from this node.
         * <p/>
         * @return Number of transitions to next states from this node.
         */
        int size() {
            return next.size();
        }

        /**
         * Destroy node.
         * <p/>
         * This is just garbage collector helper.
         */
        void destroy() {
            next.clear();
            next = null;
            value = null;
        }

        /**
         * Get string representation of node.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Value=");
            sb.append(value != null ? value.toString() : "null");
            sb.append(" Transitions=[");
            for (Iterator i = next.keySet().iterator(); i.hasNext(); ) {
                sb.append(i.next());
                if (i.hasNext()) {
                    sb.append(',');
                }
            }
            sb.append(']');
            return sb.toString();
        }

    }
    /**
     * Stack data storage.
     * <p/>
     * Used for non recursive tree walk trough. Need to store current
     * node and child nodes iterator.
     */
    private class StackItem {

        /** Tree node. */
        Node node;

        /** Child nodes iterator.
         * We don't need keys, walking trough child nodes is enough. */
        Iterator<Node> child;

        /**
         * Creates an instance of stack item (stack data storage).
         * <p/>
         * @param node Tree node to be processed.
         */
        StackItem(Node node) {
            this.node = node;
            child = node.next.values().iterator();
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Root tree node. */
    private Node root;

    /** Size of tree (number of stored strings. */
    private int size;

    /** Case sensitivity turned on (<code>true</code>)
     *  or off (<code>false</code>).
     *  All strings are stored and compared as upper case when turned on.
     */
    private boolean caseSensitive;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    public StringPrefixTree(boolean caseSensitive) {
        root = new Node();
        size = 0;
        this.caseSensitive = caseSensitive;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add value into node and finish <cose>String</code> based on nodes
     * already stored in prefix tree.
     * <p/>
     * This method will modify node that represents last character in newly
     * added <code>String</code>.
     * <p/>
     * @param node  Node to be marked as final and used to store value.
     * @param value Value to be stored into node.
     */
    private void finishExistingStringWithValue(Node node, Type value) {
        size++;
        node.setFinal();
        node.setValue(value);
    }

    /**
     * Remove value from node and mark node state as inner.
     * <p/>
     * This method will modify node that represents last character
     * of <code>String</code> being removed.
     * <p/>
     * @param node  Node to be marked as inner and to remove its value
     */
    private void removeValuefromString(Node node) {
        size--;
        node.setInner();
        node.setValue(null);
    }

    /**
     * Add value into node and finish <cose>String</code> based on nodes
     * (at least last one) newly added into prefix tree.
     * <p/>
     * This method adds new node representing last character in newly
     * added <code>String</code> under given node which represents character
     * before last one or nodes root.
     * <p/>
     * @param node  Node where new node with last character will be attached.
     * @param value Value to be stored into newly attached node.
     */
    private void finishNewStringWithValue(Node node, char c, Type value) {
        size++;
        node.add(c, new Node(value));
    }

    // This should be as fast as possible. Tree walktrough is implemented
    // as non-recursive.
    /**
     * Clear content of <code>String</code> prefix tree.
     */
    public void clear() {
        // Store old prefix tree.
        Node oldRoot = root;
        // Reinitialize as empty.
        root = new Node();
        size = 0;
        // Old structure cleanup.
        LinkedList<StackItem> stack = new LinkedList<>();
        StackItem item;
        stack.addFirst(new StackItem(oldRoot));
        while((item = stack.getLast())!= null) {
            // Tree step down.
            if (item.child.hasNext()) {
                stack.addLast(new StackItem((Node)item.child.next()));
            // Current node processing and tree setep up.
            } else {
                item.node.destroy();
                stack.removeLast();
            }
        }
    }

    /**
     * Add new <code>String</code> into prefix tree and set value mapped
     * to this string.
     * <p/>
     * <code>null</code> value of <code>str</code> argument is considered as
     * empty string.
     * <p/>
     * @param str   <code>String</code> to be added into prefix tree.
     * @param value Value mapped to newly added <code>String</code>.
     * @return Value of <code>true</code> if given <code>String</code> has been
     *         added or <code>false</code> if given <code>String</code> was
     *         already stored in prefix tree before and nothing has changed.
     */
    public boolean add(String str, Type value) {
        int strLen = str != null ? str.length() : 0;
        int lastIndex = strLen > 0 ? strLen - 1 : 0;
        Node act = root;
        // Process inner characters.
        for (int i = 0; i < lastIndex; i++) {
            char c = str.charAt(i);
            Node next = act.get(c);
            if (next == null) {
                act.add(c, act = new Node());
            } else {
                act = next;
            }
        }
        // Process final character.
        boolean result;
        if (strLen == 0 && !root.isFinal()) {
            result = true;
            finishExistingStringWithValue(root, value);
        } else {
            char c = str.charAt(lastIndex);
            Node next = act.get(c);
            if (next == null) {
                result = true;
                finishNewStringWithValue(act, c, value);
            } else {
                if (result = !next.isFinal()) {
                    finishExistingStringWithValue(next, value);
                }
            }
        }
        return result;
    }

    /**
     * Remove <code>String</code> from prefix tree and remove value mapped
     * to this string.
     * <P/>
     * @param str <code>String</code> to be removed from prefix tree.
     * @return Value mapped to removed node.
     */
    public Type remove(String str) {
        Type result = null;
        int pos = 0;
        int strLen = str != null ? str.length() : 0;
        Node act = root;
        Node last = null;
        LinkedList<Node> stack = new LinkedList<>();
        while (pos <= strLen && act != null) {
            if (pos < strLen) {
                stack.addLast(act);
                act = act.get(str.charAt(pos));
            }
            if (pos == strLen && act.isFinal()) {
                result = act.getValue();
                removeValuefromString(act);
            }
            pos++;
        }
        if (result != null) {
            pos = str.length();
            while (--pos >= 0 &&!act.isFinal() && act.size() == 0) {
                Node del = act;
                act = stack.removeLast();
                Node removed = act.remove(str.charAt(pos));
                if (removed == del) {
                    del.destroy();
                } else {
                    throw new VerifyError(
                            "Removed transition does not point to removed state.");
                }
            }
        }
        stack.clear();
        return result;
    }

    /**
     * Exact match of given <code>CharSequence</code> argument against
     * <code>String</code>s stored in prefix tree.
     * <p/>
     * <code>null</code> value of <code>str</code> argument is considered as
     * empty string. Matching is done from the beginning of given
     * <code>String</code> argument.
     * <p/>
     * @param str String to match against tree.
     * @return Value of matching <code>String</code> or <code>null</code> if
     *         no matching <code>String</code> was found.
     */
    public Type match(final CharSequence str) {
        return match(str, 0);
    }

    /**
     * Exact match of given <code>CharSequence</code> argument against
     * <code>String</code>s stored in prefix tree.
     * <p/>
     * <code>null</code> value of <code>str</code> argument is considered as
     * empty string. Matching is done from the beginning of given
     * <code>String</code> argument.
     * <p/>
     * @param str    String to match against tree.
     * @param offset Beginning index for searching.
     * @return Value of matching <code>String</code> or <code>null</code> if
     *         no matching <code>String</code> was found.
     */
    public Type match(final CharSequence str, final int offset) {
        int pos = offset;
        int strLen = str != null ? str.length() : 0;
        Type value = null;
        Node act = root;
        while (pos <= strLen && act != null) {
            if (pos == strLen && act.isFinal()) {
                value = act.getValue();
            }
            if (pos < strLen) {
                act = act.get(str.charAt(pos));
            }
            pos++;
        }
        return value;
    }

    /**
     * Longest possible match of given cyclic buffer <code>buff</code> argument
     * against <code>String</code>s stored in prefix tree.
     * <p/>
     * Search starts at <code>beg</code> index in <code>buff</code>. Next index
     * is evaluated as <code>(&lt;current index&gt; + 1) % buff.length</code>.
     * Maximum of <code>len</code> characters are compared and longest possible
     * <code>String</code> stored in prefix tree is evaluated as matching. Zero
     * length is evaluated as empty string.
     * <p/>
     * @param buff Cyclic buffer containing source string.
     * @param len  Length of string to be compared.
     * @param beg  Index of beginning of the string (1st character) in 
     *             cyclic buffer.
     * @return Value of matching <code>String</code> or <code>null</code> if
     *         no matching <code>String</code> was found.
     */
    public Type matchCyclicBuffer(final char[] buff, final int len,
            final int beg) {
        int pos = beg;
        int count = 0;
        Type value = null;
        Node act = root;
        while(count <= len && act != null) {
            if (act.isFinal()) {
                value = act.getValue();
            }           
            if (count < len) {
                act = act.get(buff[pos]);
            }
            count++;
            pos = (beg + count) % buff.length;
        }
        return value;
    }

    /**
     * Match longest possible <code>CharSequence</code> stored in prefix tree
     * against given <code>String</code> argument and return value stored under
     * this <code>String</code>.
     * <p/>
     * <code>null</code> value of <code>str</code> argument is considered as
     * empty string. Matching is done from the beginning of given
     * <code>String</code> argument.
     * <p/>
     * @param str <code>String</code> used to prefixMatch prefix tree against.
     * @return Value of longest possible <code>String</code> stored in prefix
     *         tree that is matching given <code>String</code> argument prefix
     *         or <code>null</code> if no such stored <code>String</code> was
     *         found.
     */
    public Type prefixMatch(final CharSequence str) {
        return prefixMatch(str, 0);
    }

    /**
     * Match longest possible <code>CharSequence</code> stored in prefix tree
     * against given <code>String</code> argument and return value stored under
     * this <code>String</code>.
     * <p/>
     * <code>null</code> value of <code>str</code> argument is considered as
     * empty string. Matching is done from the beginning of given
     * <code>String</code> argument.
     * <p/>
     * @param offset Beginning index for searching.
     * @param str <code>String</code> used to prefixMatch prefix tree against.
     * @return Value of longest possible <code>String</code> stored in prefix
     *         tree that is matching given <code>String</code> argument prefix
     *         or <code>null</code> if no such stored <code>String</code> was
     *         found.
     */
    public Type prefixMatch(final CharSequence str, final int offset) {
        int pos = offset;
        int strLen = str != null ? str.length() : 0;
        Type value = null;
        Node act = root;
        while (pos <= strLen && act != null) {
            if (act.isFinal()) {
                value = act.getValue();
            }           
            if (pos < strLen) {
                act = act.get(str.charAt(pos));
            }
            pos++;
        }
        return value;
    }

    /**
     * Get number of strings stored in prefix tree.
     * <p/>
     * @return Number of strings stored in prefix tree.
     */
    public int size() {
        return size;
    }

}
