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

package org.netbeans.modules.db.metadata.model.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openide.util.Parameters;

/**
 * Represents the handle of a metadata element.
 *
 * <p>Metadata elements cannot escape the {@link MetadataModel#runReadAction} method.
 * Handles can be used to pass information about metadata elements out of this method.
 * The handle can be {@link #resolve resolved} to the corresponding
 * metadata element in another {@code runReadAction} method.</p>
 *
 * @param <T> the type of the metadata element that this handle was created for.
 *
 * @author Andrei Badea
 */
public class MetadataElementHandle<T extends MetadataElement> {

    // These integers place a particular element in the hierarchy of elements,
    // with CATALOG at the top
    private static final int CATALOG = 0;
    private static final int SCHEMA = 1;
    private static final int TABLE = 2;
    private static final int VIEW = 2;
    private static final int PROCEDURE = 2;
    private static final int COLUMN = 3;
    private static final int PARAMETER = 3;
    private static final int INDEX = 3;
    private static final int FOREIGN_KEY = 3;
    private static final int FOREIGN_KEY_COLUMN = 4;
    private static final int INDEX_COLUMN = 4;
    private static final int FUNCTION = 2;

    // The hierarchy of names for this element (e.g. ["mycatalog","myschema","mytable","mycolumn"])
    //
    // It is the combination of the hierarchy of names and kinds that uniquely identifies this
    // element in the metadata model.
    private final String[] names;

    // The hierarchy of element kinds for this element (e.g. [CATALOG,SCHEMA,TABLE,COLUMN]
    // or [CATALOG,SCHEMA,VIEW,COLUMN])
    //
    // It is the combination of the hierarchy of names and kinds that uniquely identifies this
    // element in the metadata model.
    private final Kind[] kinds;

    /**
     * Creates a handle for a metadata element.
     *
     * @param  <T> the type of the metadata element to create this handle for.
     * @param  element a metadata element.
     * @return the handle for the given metadata element.
     */
    public static <T extends MetadataElement> MetadataElementHandle<T> create(T element) {
        Parameters.notNull("element", element);
        List<String> names = new ArrayList<String>();
        List<Kind> kinds = new ArrayList<Kind>();
        MetadataElement current = element;
        while (current != null) {
            names.add(current.getInternalName());
            kinds.add(Kind.of(current));
            current = current.getParent();
        }
        Collections.reverse(names);
        Collections.reverse(kinds);
        String[] namesArray = names.toArray(new String[0]);
        Kind[] kindsArray = kinds.toArray(new Kind[0]);

        return new MetadataElementHandle<T>(namesArray,kindsArray);
    }

    // For use in unit tests.
    static <T extends MetadataElement> MetadataElementHandle<T> create(Class<T> clazz, String[] names, Kind[] kinds) {
        return new MetadataElementHandle<T>(names, kinds);
    }

    private MetadataElementHandle(String[] names, Kind[] kinds) {
        this.names = names;
        this.kinds = kinds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetadataElementHandle<?> other = (MetadataElementHandle<?>) obj;
        if (!Arrays.equals(this.kinds, other.kinds)) {
            return false;
        }
        if (!Arrays.equals(this.names, other.names)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (String name : names) {
            if (name != null) {
                hash ^= name.hashCode();
            } else {
                // Do not XOR with a constant, since multiple nulls would
                // cause the hash to just flip-flop between two values.
                hash++;
            }
        }
        for (Kind kind : kinds) {
            hash ^= kind.hashCode();
        }
        return hash;
    }

    /**
     * Resolves this handle to the corresponding metadata element, if any.
     *
     * @param  metadata the {@link Metadata} instance to resolve this element against.
     * @return the corresponding metadata element or null if it could not be found
     *         (for example because it is not present in the given {@code Metadata}
     *         instance, or because it has been removed).
     */
    @SuppressWarnings("unchecked")
    public T resolve(Metadata metadata) {
        int length = kinds.length;
        switch (kinds[length - 1]) {
            case CATALOG:
                return (T) resolveCatalog(metadata);
            case SCHEMA:
                return (T) resolveSchema(metadata);
            case TABLE:
                return (T) resolveTable(metadata);
            case VIEW:
                return (T) resolveView(metadata);
            case PROCEDURE:
                return (T) resolveProcedure(metadata);
            case COLUMN:
                return (T) resolveColumn(metadata);
            case PRIMARY_KEY:
                return (T) resolvePrimaryKey(metadata);
            case PARAMETER:
                return (T) resolveParameter(metadata);
            case FOREIGN_KEY:
                return (T) resolveForeignKey(metadata);
            case INDEX:
                return (T) resolveIndex(metadata);
            case FOREIGN_KEY_COLUMN:
                return (T) resolveForeignKeyColumn(metadata);
            case INDEX_COLUMN:
                return (T) resolveIndexColumn(metadata);
            case RETURN_VALUE:
                return (T) resolveReturnValue(metadata);
            case FUNCTION:
                return (T) resolveFunction(metadata);
            default:
                throw new IllegalStateException("Unhandled kind " + kinds[kinds.length -1]);
        }
    }

    private Catalog resolveCatalog(Metadata metadata) {
        return metadata.getCatalog(names[CATALOG]);
    }

    private Schema resolveSchema(Metadata metadata) {
        Catalog catalog = resolveCatalog(metadata);
        if (catalog != null) {
            String name = names[SCHEMA];
            if (name != null) {
                return catalog.getSchema(name);
            } else {
                return catalog.getSyntheticSchema();
            }
        }
        return null;
    }

    private Table resolveTable(Metadata metadata) {
        Schema schema = resolveSchema(metadata);
        if (schema != null) {
            return schema.getTable(names[TABLE]);
        }
        return null;
    }

    private View resolveView(Metadata metadata) {
        Schema schema = resolveSchema(metadata);
        if (schema != null) {
            return schema.getView(names[VIEW]);
        }
        return null;
    }

    private Procedure resolveProcedure(Metadata metadata) {
        Schema schema = resolveSchema(metadata);
        if (schema != null && kinds[PROCEDURE] == Kind.PROCEDURE) {
            return schema.getProcedure(names[PROCEDURE]);
        }
        return null;
    }

    private Function resolveFunction(Metadata metadata) {
        Schema schema = resolveSchema(metadata);
        if (schema != null && kinds[FUNCTION] == Kind.FUNCTION) {
            return schema.getFunction(names[FUNCTION]);
        }
        return null;
    }

    private Value resolveReturnValue(Metadata metadata) {
        Function proc = resolveFunction(metadata);
        if (proc != null) {
            return proc.getReturnValue();
        }
        Procedure proc2 = resolveProcedure(metadata);
        if (proc2 != null) {
            return proc2.getReturnValue();
        }
        return null;
    }

    private PrimaryKey resolvePrimaryKey(Metadata metadata) {
        Table table = resolveTable(metadata);
        if (table != null) {
            return table.getPrimaryKey();
        }

        return null;
    }

    private Column resolveColumn(Metadata metadata) {
        // A column can be part of a number of different metadata elements.
        // Find out which one and resolve appropriately
        switch (kinds[COLUMN - 1]) {
            case TABLE:
                Table table = resolveTable(metadata);
                if (table != null) {
                    return table.getColumn(names[COLUMN]);
                }
                return null;
            case PROCEDURE:
                Procedure proc = resolveProcedure(metadata);
                if (proc != null) {
                    return proc.getColumn(names[COLUMN]);
                }
                return null;
            case VIEW:
                View view = resolveView(metadata);
                if (view != null) {
                    return view.getColumn(names[COLUMN]);
                }
                return null;
            default:
                throw new IllegalStateException("Unhandled kind " + kinds[COLUMN -1]);
        }
    }

    private Parameter resolveParameter(Metadata metadata) {
        Procedure proc = resolveProcedure(metadata);
        if (proc != null) {
            return proc.getParameter(names[PARAMETER]);
        }
        Function proc2 = resolveFunction(metadata);
        if (proc2 != null) {
            return proc2.getParameter(names[PARAMETER]);
        }
        return null;
    }

    private Index resolveIndex(Metadata metadata) {
        Table table = resolveTable(metadata);
        if (table != null) {
            return table.getIndex(names[INDEX]);
        }
        return null;
    }

    private ForeignKey resolveForeignKey(Metadata metadata) {
        Table table = resolveTable(metadata);
        if (table != null) {
            return table.getForeignKeyByInternalName(names[FOREIGN_KEY]);
        }

        return null;
    }

    private ForeignKeyColumn resolveForeignKeyColumn(Metadata metadata) {
        ForeignKey key = resolveForeignKey(metadata);
        if (key != null) {
            return key.getColumn(names[FOREIGN_KEY_COLUMN]);
        }

        return null;
    }

    private IndexColumn resolveIndexColumn(Metadata metadata) {
        Index index = resolveIndex(metadata);
        if (index != null) {
            return index.getColumn(names[INDEX_COLUMN]);
        }

        return null;
    }

    // Not private becuase it's used in unit tests
    enum Kind {

        CATALOG(Catalog.class),
        SCHEMA(Schema.class),
        TABLE(Table.class),
        VIEW(View.class),
        PROCEDURE(Procedure.class),
        PARAMETER(Parameter.class),
        COLUMN(Column.class),
        PRIMARY_KEY(PrimaryKey.class),
        FOREIGN_KEY(ForeignKey.class),
        INDEX(Index.class),
        FOREIGN_KEY_COLUMN(ForeignKeyColumn.class),
        INDEX_COLUMN(IndexColumn.class),
        RETURN_VALUE(Value.class),
        FUNCTION(Function.class);

        public static Kind of(MetadataElement element) {
            return of(element.getClass());
        }

        public static Kind of(Class<? extends MetadataElement> clazz) {
            for (Kind kind : Kind.values()) {
                if (kind.clazz.equals(clazz)) {
                    return kind;
                }
            }
            throw new IllegalStateException("Unhandled class " + clazz);
        }

        private final Class<? extends MetadataElement> clazz;

        private Kind(Class<? extends MetadataElement> clazz) {
            this.clazz = clazz;
        }
    }
}
