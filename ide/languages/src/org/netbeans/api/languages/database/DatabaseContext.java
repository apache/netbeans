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
package org.netbeans.api.languages.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTToken;


/**
 *
 * @author Jan Jancura
 * @author Caoyuan Deng
 */
public class DatabaseContext extends DatabaseItem {

    private DatabaseContext                 parent;
    private String                          type;
    private List<DatabaseUsage>             usages;
    private List<DatabaseContext>           contexts;
    private List<DatabaseDefinition>        definitions;
    private boolean                         usagesSorted = false;
    private boolean                         contextsSorted = false;
    private boolean                         definitionsSorted = false;
    private DatabaseDefinition              enclosingDefinition;

        
    public DatabaseContext (DatabaseContext parent, String type, int offset, int endOffset) {
        super (offset, endOffset);
        this.parent = parent;
        this.type = type;
    }

    protected void setParent(DatabaseContext parent) {
        this.parent = parent;
    }
    
    public DatabaseContext getParent() {
        return parent;
    }
    
    public String getType () {
        return type;
    }

    public void setEnclosingDefinition(DatabaseDefinition enclosingDefinition) {
        this.enclosingDefinition = enclosingDefinition;
    }
    
    public DatabaseDefinition getEnclosingDefinition() {
        if (enclosingDefinition != null) {
            return enclosingDefinition;
        } else {
            if (parent != null) {
                return parent.getEnclosingDefinition();
            } else {
                return null;
            }
        }        
    }
    

    public void addDefinition (DatabaseDefinition definition) {
        definitionsCache = null;
        if (definitions == null)
            definitions = new ArrayList<DatabaseDefinition> ();
        definitions.add(definition);
        definitionsSorted = false;
    }

    public void addUsage (DatabaseUsage usage) {
        if (usages == null)
            usages = new ArrayList<DatabaseUsage> ();
        usages.add(usage);
        usagesSorted = false;
    }

    /**
     * Only accept ASTToken here, which has precise toString() and offset, endOffset etc.
     */
    public void addUsage(ASTToken item, DatabaseDefinition definition) {
        DatabaseUsage usage = new DatabaseUsage("", item.getOffset(), item.getEndOffset());
        definition.addUsage(usage);
        usage.setDatabaseDefinition(definition);
        addUsage(usage);
    }
    
    public void addContext (ASTItem item, DatabaseContext context) {
        if (contexts == null)
            contexts = new ArrayList<DatabaseContext> ();
        contexts.add(context);
        contextsSorted = false;
    }
    
    private void addItem (DatabaseItem databaseItem) {
        if (databaseItem instanceof DatabaseUsage) {
            addUsage((DatabaseUsage)databaseItem);
        } else if (databaseItem instanceof DatabaseDefinition) {
            addDefinition((DatabaseDefinition)databaseItem);
        } else if (databaseItem instanceof DatabaseContext) {
            addContext(null, (DatabaseContext)databaseItem);
        }
    }

    private List<DatabaseDefinition> definitionsCache;
    
    /**
     * All definitions from this context. Not cached - slow!!!
     */
    public List<DatabaseDefinition> getDefinitions () {
        if (definitionsCache == null) {
            if (definitions == null) 
                definitionsCache = Collections.<DatabaseDefinition>emptyList ();
            else {
                definitionsCache = new ArrayList<DatabaseDefinition> ();
                Iterator<DatabaseDefinition> it = definitions.iterator ();
                while (it.hasNext ()) {
                    definitionsCache.add (it.next());
                }
            }
        }
        return definitionsCache;
    }
    
    public List<DatabaseDefinition> getAllVisibleDefinitions (int offset) {
        Map<String,DatabaseDefinition> map = new HashMap<String, DatabaseDefinition> ();
        addDefinitions (map, offset);
        return new ArrayList<DatabaseDefinition> (map.values ());
    }

    /**
     * Returns map of all JSItem from all contexts containing given offset.
     */
    private void addDefinitions (Map<String,DatabaseDefinition> map, int offset) {
        if (definitions != null) {
            Iterator<DatabaseDefinition> it = definitions.iterator ();
            while (it.hasNext ()) {
                DatabaseDefinition definition = it.next ();
                map.put (definition.getName (), definition);
            }
        }
        if (contexts != null) {
            Iterator<DatabaseContext> it = contexts.iterator ();
            while (it.hasNext()) {
                DatabaseContext context = it.next ();
                if (context.getOffset() <= offset && offset < context.getEndOffset())
                    context.addDefinitions (map, offset);
            }
        }
    }
    
    public DatabaseItem getDatabaseItem (int offset) {
        if (definitions != null) {
            if (!definitionsSorted) {
                definitions.sort(new ItemsComparator());
                definitionsSorted = true;
            }
            int low = 0;
            int high = definitions.size() - 1;
            while (low <= high) {
                int mid = (low + high) >> 1;
                DatabaseDefinition middle = definitions.get(mid);
                if (offset < middle.getOffset())
                    high = mid - 1;
                else
                if (offset >= middle.getEndOffset ())
                    low = mid + 1;
                else
                    return middle;
            }
        }
        
        if (usages != null) {
            if (!usagesSorted) {
                usages.sort(new ItemsComparator());
                usagesSorted = true;
            }
            int low = 0;
            int high = usages.size() - 1;
            while (low <= high) {
                int mid = (low + high) >> 1;
                DatabaseUsage middle = usages.get(mid);
                if (offset < middle.getOffset())
                    high = mid - 1;
                else
                if (offset >= middle.getEndOffset ())
                    low = mid + 1;
                else
                    return middle;
            }
        }
        
        if (contexts != null) {
            if (!contextsSorted) {
                contexts.sort(new ItemsComparator());
                contextsSorted = true;
            }
            int low = 0;
            int high = contexts.size() - 1;
            while (low <= high) {
                int mid = (low + high) >> 1;
                DatabaseContext middle = contexts.get(mid);
                if (offset < middle.getOffset())
                    high = mid - 1;
                else
                if (offset >= middle.getEndOffset ())
                    low = mid + 1;
                else
                    return middle.getDatabaseItem (offset);
            }
        }
        return null;
    }
    
    public DatabaseContext getDatabaseContext (int offset) {
        if (contexts == null || contexts.isEmpty()) return this;
        
        if (!contextsSorted)
            contexts.sort(new ItemsComparator ());
        contextsSorted = true;
        
	int low = 0;
	int high = contexts.size () - 1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    DatabaseContext middle = contexts.get (mid);
            if (offset < middle.getOffset ())
		high = mid - 1;
            else
            if (offset >= middle.getEndOffset ())
		low = mid + 1;
            else {
                DatabaseContext context = middle.getDatabaseContext(offset);
                if (context == null) return middle;
                return context;
            }
	}
        return this;
    }
    
    /**
     * Returns JSItem with given name closest to Context on given offset.
     */
    public DatabaseDefinition getDefinition (String name, int offset) {
        if (definitions != null) {
            if (!definitionsSorted)
                definitions.sort(new ItemsComparator());
            definitionsSorted = true;
            Iterator<DatabaseDefinition> it = definitions.iterator ();
            while (it.hasNext ()) {
                DatabaseDefinition definition = it.next ();
                //if (databaseItem.getEndOffset () >= offset) break;
                if (definition.getName ().equals (name))
                    return definition;
            }
        }
        if (parent != null)
            return parent.getDefinition (name, offset);
        return null;
    }
    
//    public String getAsText () {
//        StringBuilder sb = new StringBuilder ();
//        getAsText (sb, "");
//        return sb.toString ();
//    }
//    
//    private void getAsText (StringBuilder sb, String indent) {
//        sb.append (indent).append (getOffset ()).append ("\n");
//        if (definitions != null) {
//            Iterator<DatabaseDefinition> it = definitions.iterator ();
//            while (it.hasNext ()) {
//                DatabaseDefinition definition = it.next ();
//                sb.append (indent + "  ").append (definition.getName ()).append (" (").append (definition.getType ()).append (')');
//                Iterator<DatabaseUsage> it3 = definition.getUsages ().iterator ();
//                while (it3.hasNext ())
//                    sb.append (' ').append (it3.next ().getOffset ());
//                sb.append ("\n");
//            }
//        }
//        if (contexts != null) {
//            Iterator<DatabaseContext> it2 = contexts.iterator ();
//            while (it2.hasNext ()) {
//                DatabaseContext  context = it2.next ();
//                context.getAsText (sb, indent + "  ");
//            }
//        }
//        sb.append (indent).append (getEndOffset ()).append ("\n");
//    }

    public List<DatabaseContext> getContexts() {
        if (contexts == null) {
            return Collections.<DatabaseContext>emptyList();
        }
        return contexts;
    }
    
      public void addContext(DatabaseContext context) {
        if (contexts == null) {
            contexts = new ArrayList<DatabaseContext>();
	}
        context.setParent(this);
	contexts.add(context);
    }
    
  
    public DatabaseContext getClosestContext(int offset) {
        DatabaseContext result = null;
        if (contexts != null) {
            /** search children first */
            for (DatabaseContext child : contexts) {
                if (child.contains(offset)) {
                    result = child.getClosestContext(offset);
		    break;
		}
	    }  
	}
	if (result != null) {
            return result;
	} else {
            if (this.contains(offset)) {
                return this;
	    } else {
                /* we should return null here, since it may under a parent context's call, 
		 * we shall tell the parent there is none in this and children of this
		 */
                return null; 
	    } 
	}
    }

    private boolean contains(int offset) {
        return offset >= getOffset() && offset < getEndOffset();
    }

    public <T extends DatabaseDefinition> T getFirstDefinition(Class<T> clazz) {
        if (definitions == null) return null;
        for (DatabaseDefinition dfn : definitions) {
            if (clazz.isInstance(dfn)) {
                return (T) dfn;
            }
        }
        return null;
    }
    
    public <T extends DatabaseDefinition> Collection<T> getDefinitions(Class<T> clazz) {
        if (definitions == null) return Collections.<T>emptyList();
        Collection<T> result = new ArrayList<T>();
        for (DatabaseDefinition dfn: definitions) {
            if (clazz.isInstance(dfn)) {
                result.add((T) dfn);
            }
        }
        return result;
    }
    
    public void collectDefinitionsInScope(Collection<DatabaseDefinition> scopeDefinitions) {
        if (definitions != null) {
            scopeDefinitions.addAll(definitions);
        } 
	if (parent != null) {
	    parent.collectDefinitionsInScope(scopeDefinitions);
	}
    }
    
    public <T extends DatabaseDefinition> T getDefinitionInScopeByName(Class<T> clazz, String name) {
        T result = null;
	if (definitions != null) {
	    for (DatabaseDefinition dfn : definitions) {
                if (clazz.isInstance(dfn) && name.equals(dfn.getName())) {
                    result = (T) dfn;
		    break;
	        }
	    }
	}
	if (result != null) {
            return result;
	} else {
            if (parent != null) {
                return parent.getDefinitionInScopeByName(clazz, name);
	    } else {
                return null;
	    }
	} 
    }
    
    public <T extends DatabaseDefinition> T getEnclosingDefinition(Class<T> clazz, int offset) {
        DatabaseContext context = getClosestContext(offset);
        return context.getEnclosingDefinitionRecursively(clazz);
    }
    
    public <T extends DatabaseDefinition> T getEnclosingDefinition(Class<T> clazz) {
        return getEnclosingDefinitionRecursively(clazz);
    }

    private <T extends DatabaseDefinition> T getEnclosingDefinitionRecursively(Class<T> clazz) {
        DatabaseDefinition result = getEnclosingDefinition();
        if (result != null && clazz.isInstance(result)) {
            return (T) result;
        } else {
            DatabaseContext parentCtx = getParent();
            if (parentCtx != null) {
                return parentCtx.getEnclosingDefinition(clazz);
            } else {
                return null;
            }
        }        
    }

    
    @Override
    public String toString () {
        return "Context " + getOffset () + "-" + getEndOffset ();
    }
    
    
    private static class ItemsComparator implements Comparator<DatabaseItem> {

        public int compare (DatabaseItem o1, DatabaseItem o2) {
            return o1.getOffset () < o2.getOffset () ? -1 : 1;
        }
    }
}
