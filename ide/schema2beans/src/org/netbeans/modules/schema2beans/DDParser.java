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

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;


/**
 *  This class provides an implementation of the Iterator interface.
 *
 *  Providing a parsing string (/prop1/prop2/...), the iterator parses
 *  the specified BaseBean, searching for the specified properties.
 *  As the hasMore()/next() methods are called, the parser goes over
 *  all the instances of the BaseBean tree.
 *
 *  For example, assuming we have: /Book/Chapters/Title, the iterator
 *  would return all the titles of all the Chapters of the book.
 *
 *  The iterator always returns elements of the type specified as the
 *  last element of the parsing string. In the above example, the
 *  parser would return only Title elements.
 *
 *  If the parsing string is /EnterpriseBean/Entity, the parser would
 *  return all the Entity beans of the tree. Therefore, the type returned
 *  can be either a BaseBean or a final property type. Note that in case
 *  of a final property value, only objects are returned. Scalar types
 *  are wrapped using the appropriate class (int/Integer, ...).
 *
 *  TODO: support for attributes, keys and limited arrays (only first
 *  element for example).
 *
 */
public class DDParser implements Iterator {

    public static class DDLocation {
	BaseBean	root;
	String  	name;
	int		index;
	int 		type;
	
	public DDLocation(BaseBean r) {
	    this(r, null, -1, 0);
	}
	
	public DDLocation(BaseBean r, String n, int i, int type) {
	    this.root = r;
	    this.name = n;
	    if (i != -1) {
		//  This gets the internal index value that doesn't change
		//  even when an element of the array is removed
		this.index = r.indexToId(n, i);
	    } else
		this.index = i;
	    
	    this.type = type;
	}
	
	public void removeValue() {
	    if (this.index != -1) {
		this.root.removeValue(this.name,
		this.root.getValueById(this.name,
		this.index));
	    }
	    else
		this.root.setValue(this.name, null);
	}
	
	public void setValue(String value) {
	    Object v = value;
	    
	    if (Common.isBoolean(this.type))
		v = Boolean.valueOf(value);
	    
	    if (this.index != -1)
		this.root.setValueById(this.name, this.index, v);
	    else
		this.root.setValue(this.name, v);
	}
	
	public Object getValue() {
	    if (this.root == null || this.name == null)
		return null;

	    if (this.index != -1)
		return this.root.getValueById(this.name, this.index);
	    else
		return this.root.getValue(this.name);
	}
	
	public BaseBean getRoot() {
	    return this.root;
	}
	
	public String getName() {
	    return this.name;
	}
	
	public int getIndex() {
	    if (this.name != null)
		return this.root.idToIndex(this.name, this.index);
	    return this.index;
	}
	
	public boolean isNode() {
	    return Common.isBean(this.type);
	}
	
	public String toString() {	// BEGIN_NOI18N
	    if (this.root != null) {
		return "BaseBean(" + this.root.name() + "." +
		Integer.toHexString(this.root.hashCode()) + ") " +
		    this.name + "[" +
		    ((this.name==null)?"i"+this.index:
		    ""+this.root.idToIndex(this.name, this.index)) +
		    "] - isNode: " +
		    this.isNode();
	    } else {
		return "BaseBean(null)";
	    }
	}				// END_NOI18N
    }
    
    /**
     *	There is one such class per property specified in the parsing string.
     *	For example, if the parsing string is /Book/Chapters/Title,
     *	we would have three instances of the PropParser class. One for Book,
     *	one Chapters and another one for Title. Furthermore, the PropParser
     *	instances would be linked to each other through the parent/child
     *	attributes (Book.parent = null, Book.child = Chapters, ...)
     *
     *	This class really implements the Iterator logic, using a bottom/up
     *	algorithm: the DDParser.next() always asks for the element of the
     *	last PropParser (the one with child = null). Then this element
     *	either return what's available, either asks for its next parent
     *	in order to get new values. Its parent, in turns might asks for
     *	new values to its parents, and so on, until the root is reached and
     *	nothing else is available.
     */
    static class PropParser {
	String 		name;
	int		pos;
	Object[]	values;
	BaseProperty 	baseProp;
	PropParser	parent;
	PropParser	child;
	Object		cache;
	boolean		hasCache;
	BaseBean	curParent;
	String		keyName;
	String		keyValue;
	boolean		autoCreate;
	
	
	PropParser(String n, boolean autoCreate) {
	    int i = n.indexOf('=');
	    if (i != -1) {
		int j = n.indexOf('.');
		if (j != -1 && j < i) {
		    this.name = n.substring(0, j);
		    this.keyName = n.substring(j+1, i);
		    this.keyValue = n.substring(i+1);
		} else {
		    this.name = n.substring(0, i);
		    this.keyName = null;
		    this.keyValue = n.substring(i+1);
		}
	    } else {
		if(n.indexOf('.') != -1) {
		    throw new IllegalStateException(Common.getMessage(
		    "DDParserCannotUseKeyWithOutValue_msg", n));
		}
		
		this.name = n;
		this.keyName = null;
		this.keyValue = null;
	    }
	    
	    if (this.keyValue != null && (this.keyValue.length() > 0) &&
	    (this.keyValue.charAt(0) == '\'' ||
	    this.keyValue.charAt(0) == '"')) {	// NOI18N
		
		this.keyValue =
		this.keyValue.substring(1, this.keyValue.length()-1);
	    }
	    
	    this.autoCreate = autoCreate;
	    this.parent = null;
	    this.pos = 0;
	    this.cache = null;
	    this.parent = null;
	    this.child = null;
	    this.hasCache = false;
	    this.curParent = null;
	}
	
	DDLocation getLocation() {
	    return new DDParser.DDLocation(this.curParent, this.name,
					   ((this.baseProp.isIndexed())?
					    this.pos-1:-1),
					  ((BeanProp)this.baseProp).getType());
	}
	
	void setBaseProperty(BaseProperty bp) {
	    this.baseProp = bp;
	}
	
	BaseBean getCurBaseBean(boolean lastProp) {
	    
	    while(true) {
		int p = this.seekNext();
		if (p != -1) {
		    //	Pos for the next one if intermediate
		    if (!lastProp)
			this.pos++;
		    return (BaseBean)this.values[p];
		}
		try {
		    if (!this.updateValues())
			break;
		} catch(NoSuchElementException e) {
		    break;
		}
	    }
	    
	    return null;
	}
	
        int seekNext() {
            //	Seek doesn't move the pos - next() does
            if (this.baseProp.isBean()) {
                while(this.values.length>this.pos
                      && this.values[this.pos] == null) {
                    this.pos++;
                }
            }
            if (this.values.length == this.pos)
                return -1;
            return this.pos;
        }
	
	static final char WILD_CHAR = '*';
	private boolean checkValueMatch(Object o1, Object o2) {
	    
	    if (o1 == null || o2 == null) {
		return false;
	    } else {
		String s1 = o1.toString();
		String s2 = o2.toString();
		if (s1.charAt(0) == WILD_CHAR) {
		    return s2.endsWith(s1.substring(1));
		} else
		    if (s1.charAt(s1.length()-1) == WILD_CHAR) {
			return s2.startsWith(s1.substring(0, s1.length()-1));
		    } else {
			return s1.equals(s2);
		    }
	    }
	}
	
        //  BaseBean is the parent - get our values from it
        void setValues(BaseBean b) {
            this.curParent = b;
            if (baseProp.isIndexed()) {
                this.values = b.getValues(this.name);
            } else {
                this.values = new Object[1];
                this.values[0] = b.getValue(this.name);
            }
	    
            //  If any keyName/keyValue is used, apply it to the values
            if (this.baseProp.isBean()) {
                if (this.keyName != null && this.keyValue != null) {
		    
                    ArrayList arr = new ArrayList();
                    Object o1 =	Common.getComparableObject(this.keyValue);
                    for (int i=0; i<this.values.length; i++) {
                        BaseBean bb = (BaseBean)(this.values[i]);
                        if (bb != null) {
                            Object o2 = Common.
                                getComparableObject(bb.getValue(this.keyName));
			    
                            if (this.checkValueMatch(o1, o2)) {
                                arr.add(values[i]);
                            }
                        }
                    }
                    this.values = arr.toArray();
                }
            } else {
                if (this.keyValue != null) {
                    ArrayList arr = new ArrayList();
                    Object o1 =	Common.getComparableObject(this.keyValue);
                    for (int i=0; i<this.values.length; i++) {
                        if (this.values[i] != null) {
                            Object o2 =
                                Common.getComparableObject(this.values[i]);
			    
                            if (this.checkValueMatch(o1, o2)) {
                                arr.add(values[i]);
                            }
                        }
                    }
                    this.values = arr.toArray();
                }
            }
	    
            //	Find out if there is something left
            boolean empty = true;
            for (int i=0; i<this.values.length; i++)
                if (this.values[i] != null) {
                    empty = false;
                    break;
                }
	    
            if (empty) {
                if (this.autoCreate) {
                    if (this.baseProp.isBean()) {
                        BaseBean bb = b.newInstance(this.name);
                        if (this.keyName != null)
                            bb.setValue(this.keyName, this.keyValue);
                        if (this.baseProp.isIndexed())
                            b.addValue(this.name, bb);
                        else
                            b.setValue(this.name, bb);
		    
                        this.values = new Object[] {bb};
                    } else
                        if (this.keyValue != null) {
                            this.values = new Object[] {this.keyValue};
                            if (this.baseProp.isIndexed()) {
                                b.setValue(this.name, this.values);
                            } else {
                                b.setValue(this.name, this.keyValue);
                            }
                        }
                } else {
                    values = new Object[0];
                }
            }
	    
            this.pos = 0;
        }
	
        //  We already returned everything we have - try to get new ones
        boolean updateValues() {
            if (this.parent != null) {
                BaseBean b = null;
                do {
                    b = (BaseBean)this.parent.next();
                    if (b != null) {
                        this.setValues(b);
                        return true;
                    }
                } while (b == null);
            }
            return false;
        }
	
        //  If we can populate our cache with a new one, we have 1 more
        boolean hasNext() {
            if (!this.hasCache) {
                try {
                    this.cache = this.next();
                    this.hasCache = true;
                } catch(NoSuchElementException e) {
                    return false;
                }
            }
            return true;
        }
	
        //  Return the next one available
        Object next() {
            if (this.hasCache) {
                this.hasCache = false;
                return this.cache;
            } else {
                int p = this.seekNext();
                if (p != -1) {
                    this.pos++;
                    return this.values[p];
                } else {
                    if (this.updateValues() == true)
                        return this.next();
                    else
                        throw new NoSuchElementException();
                }
            }
        }
    }
    
    //
    //	This references the last PropParser of the structure described by
    //	the parsing string.
    //
    private PropParser		parser;
    private BaseBean		root;
    private Object		current;
    private boolean		singleRoot;
    private boolean		empty;
    
    /**
     *	Build the PropParser from the parsing String. The PropParser
     *	class does all the work.
     */
    public DDParser(BaseBean root, String parse) {
        //System.out.println("DDParser: parse="+parse+" root="+root);
	
	boolean autoCreate = false; // Force the creation of the path
	
	//
	//  Extract the property names from the parsing string
	//  and allocate the PropParser instances
	//	
	parse = parse.trim();
	
	if (parse.endsWith("!")) {	// NOI18N
	    autoCreate = true;
	    parse = parse.substring(0, parse.length()-1);
	}
	
	while (parse != null &&
           (parse.startsWith("/") || parse.startsWith(".")))	// NOI18N
	    parse = parse.substring(1);
	while (parse != null && parse.endsWith("/"))	// NOI18N
	    parse = parse.substring(0, parse.length()-1);
	
	if (parse.equals("")) {		// NOI18N
	    this.singleRoot = true;
	    this.root = root;
	    return;
	}
	
	
	PropParser 	prev = null;
	PropParser	cur = null;
	String 		n;
	
	int 		pos = 0;
	boolean 	skip = false;
	
	for (int i=0; i<parse.length(); i++) {
	    char c = parse.charAt(i);
	    
	    if (c == '"' || c == '\'')	// NOI18N
            skip = !skip;
	    
	    if (skip)
            continue;
	    
	    boolean last = (i==(parse.length()-1));
	    if (c == '/' || last) {
            if (last)
                n = parse.substring(pos, i+1);
            else
                n = parse.substring(pos, i);
            pos = i+1;
            if (root.getProperty() == null) {
                System.out.println("root.getProperty="+root.getProperty());
                System.out.println("parse="+parse);
                System.out.println("n="+n);
                System.out.println("!Skipping DDParser search!");
                continue;
            }
            if (!root.getProperty().hasName(n) || last) {
                cur = new PropParser(n, autoCreate);
                if (prev != null) {
                    prev.child = cur;
                    cur.parent = prev;
                }
                prev = cur;
            }
	    }
	}
	
	this.parser = cur;
	
	if (cur != null) {
	    while (cur.parent != null)
            cur = cur.parent;
	}
	
	//
	//  Initialize the PropParser instances
	//
	BaseBean	bean = root;
	do {
	    BaseProperty[] p = bean.listProperties();
	    String name = cur.name;
	    boolean found = false;
	    
	    for (int j=0; (j<p.length) && !found; j++) {
            if (p[j].hasName(name)) {
                //System.out.println("Found name="+name);
		    
                // Structure description of the property
                cur.setBaseProperty(p[j]);
                // Set the values
                cur.setValues(bean);
		    
                if (p[j].isBean())
                    bean = cur.getCurBaseBean(cur.child==null);
                else {
                    if (cur.child != null)
                        throw new IllegalStateException(Common.getMessage(
                            "FinalPropertyNotDeclaredAtEndOfParsingString_msg",
                                                                        name));
                }
                found = true;
            }
	    }
	    if (!found) {
            throw new NoSuchElementException(Common.getMessage(
                             "NotFoundInPropertyList_msg", name,
                             root.toString()));
	    }
	    cur = cur.child;
	} while (cur != null && bean != null);
	
	if (bean == null) {
	    //
	    // It might happen that one of the element in the parsing path
	    // doesn't exist in the current graph (simply because not set)
	    // In this case, the parser has nothing to return.
	    //
	    this.empty = true;
	}
	else
	    this.empty = false;
    }
    
    public boolean hasNext() {
        if (!this.empty) {
            boolean more;
            if (this.singleRoot)
                more = (this.root != null);
            else
                more = this.parser.hasNext();
            if (!more)
                this.current = null;
            return more;
        }
        return false;
    }
    
    public Object next() {
        if (!this.empty) {
            if (this.singleRoot) {
                if (this.root != null) {
                    this.current = this.root;
                    this.root = null;
                } else
                    throw new NoSuchElementException();
            } else
                this.current = this.parser.next();
            //System.out.println("next="+current);
            return this.current;
        }
        throw new NoSuchElementException();
    }
    
    public Object current() {
	return this.current;
    }
    
    public void remove() {
	throw new UnsupportedOperationException();
    }
    
    /**
     *	Return the current position of the parser. The position is made of
     *	a baseBean (parent of the property), the property name, and its index
     *	value, if this is an indexed property.
     */
    public DDLocation getLocation() {
	if (!this.empty) {
	    if (this.singleRoot)
		return new DDLocation((BaseBean)this.current);
	    else
		return this.parser.getLocation();
	}
	return null;
    }
}


