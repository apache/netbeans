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

package org.netbeans.modules.javadoc.search;


import java.net.URL;
import java.util.Comparator;


/**
 * Represents one item found in document index.
 * It's produced by {@link IndexSearchThread} and communicated
 * back to {@link IndexSearch} UI.
 *
 * @author Petr Hrebejk
 */
final class DocIndexItem extends Object {

    /** Standard comparators */
    public static final Comparator<DocIndexItem> REFERENCE_COMPARATOR = new ReferenceComparator();
    public static final Comparator<DocIndexItem> TYPE_COMPARATOR = new TypeComparator();
    public static final Comparator<DocIndexItem> ALPHA_COMPARATOR = new AlphaComparator();

    private String text = null;
    private URL contextURL = null;
    private String spec = null;
    private String remark = null;
    private String pckg = null;
    private String declaringClass = null;    

    private int iconIndex = DocSearchIcons.ICON_NOTRESOLVED;

    public DocIndexItem ( String text, String remark, URL contextURL, String spec ) {
        this.text = text;
        this.remark = remark;
        this.contextURL = contextURL;
        this.spec = spec;
        if (spec != null ) { // spec format ../pckg/Classname.html
            int offset = spec.startsWith("../") ? 3 : spec.startsWith("./") ? 2 : 0; // NOI18N
            int length = spec.lastIndexOf('/');
            length = length < 0? spec.length(): length + 1;
            pckg = spec.substring(offset, length);
//            System.out.println("DII.length: " + length);
//            System.out.println("DII: " + length + ", " + pckg + " <- " + spec);
            pckg = pckg.replace('/', '.');
        }
    }

    public URL getURL () throws java.net.MalformedURLException {
        return contextURL != null? new URL( contextURL, spec ): null;
    }

    @Override
    public String toString() {
        if ( remark != null )
            return text + remark;
        else
            return text;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public void setIconIndex( int iconIndex ) {
        this.iconIndex = iconIndex;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark( String remark ) {
        this.remark = remark;
    }

    public String getPackage() {
        return pckg == null ? "" : pckg; // NOI18N
    }

    public void setPackage( String pckg ) {
//            System.out.println("DII.set: " + pckg);
        this.pckg = pckg;
    }

    /** Getter for property declaringClass.
     * @return Value of property declaringClass.
 */
    public java.lang.String getDeclaringClass() {
        return declaringClass;
    }    

    /** Setter for property declaringClass.
     * @param declaringClass New value of property declaringClass.
 */
    public void setDeclaringClass(java.lang.String declaringClass) {
        this.declaringClass = declaringClass;
    }
    
    /** Getter for property field.
     * @return Value of property field.
 */
    public java.lang.String getField() {
        return text != null ? text : "";    //NOI18N
    }

    /** Setter for property field.
     * @param Value of property field.
    */
    public void setField(String text) {
        this.text = text;
    }

    // COMPARATOR INNER CLASSES ----------------------------------------------------------------

    static final class ReferenceComparator implements Comparator<DocIndexItem> {

        public int compare( DocIndexItem dii1, DocIndexItem dii2 ) {
            int res = dii1.getPackage().compareTo(dii2.getPackage());

            return res != 0 ? res : DocIndexItem.ALPHA_COMPARATOR.compare( dii1, dii2 );
        }

        @Override
        public boolean equals( Object comp ) {
            return ( comp instanceof ReferenceComparator );
        }

        @Override
        public int hashCode() {
            return 353;
        }

    }

    static final class TypeComparator implements Comparator<DocIndexItem> {

        public int compare( DocIndexItem dii1, DocIndexItem dii2 ) {
            return dii1.getIconIndex() - dii2.getIconIndex();
        }

        @Override
        public boolean equals( Object comp ) {
            return ( comp instanceof TypeComparator );
        }

        @Override
        public int hashCode() {
            return -34;
        }
        
    }

    static final class AlphaComparator implements Comparator<DocIndexItem> {

        public int compare( DocIndexItem dii1, DocIndexItem dii2 ) {
            return dii1.toString().compareTo( dii2.toString() );
        }

        @Override
        public boolean equals( Object comp ) {
            return ( comp instanceof AlphaComparator );
        }

        @Override
        public int hashCode() {
            return 33;
        }
    }
}
