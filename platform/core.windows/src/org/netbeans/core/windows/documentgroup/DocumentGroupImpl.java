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
package org.netbeans.core.windows.documentgroup;

import java.text.Collator;
import javax.swing.JPanel;

/**
 * Public interface to document groups. The actual implementation is in GroupsManager class.
 *
 * @see GroupsManager
 *
 * @author S. Aubrecht
 */
public final class DocumentGroupImpl implements Comparable<DocumentGroupImpl> {

    private final String name;
    private final String displayName;


    DocumentGroupImpl( String name, String displayName ) {
        this.name = name;
        this.displayName = displayName;
    }

    public boolean open() {
        return GroupsManager.getDefault().openGroup( DocumentGroupImpl.this );
    }

    public boolean close() {
        return GroupsManager.getDefault().closeGroup( this );
    }

    @Override
    public String toString() {
        return displayName;
    }

    String getName() {
        return name;
    }

    @Override
    public int compareTo( DocumentGroupImpl o ) {
        Collator collator = Collator.getInstance();
        int res = collator.compare( displayName, o.displayName );
        if( 0 == res )
            res = collator.compare( name, o.name );
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final DocumentGroupImpl other = (DocumentGroupImpl) obj;
        if( (this.name == null) ? (other.name != null) : !this.name.equals( other.name ) ) {
            return false;
        }
        return true;
    }

    private static class WaitPane extends JPanel {
        public WaitPane() {
            setOpaque(false);
        }



    }
}
