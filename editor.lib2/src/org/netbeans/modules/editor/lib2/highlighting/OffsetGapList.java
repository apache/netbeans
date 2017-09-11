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

package org.netbeans.modules.editor.lib2.highlighting;

/**
 * The implementation of <code>AbstractOffsetGapList</code> with
 * <code>Offset</code> elements.
 * 
 * @author Vita Stejskal
 */
public final class OffsetGapList<E extends OffsetGapList.Offset> extends AbstractOffsetGapList<E> {
    
    /** Creates a new instance of OffsetGapList */
    public OffsetGapList() {
        super();
    }

    public OffsetGapList(boolean fixedZeroOffset) {
        super(fixedZeroOffset);
    }
    
    protected int elementRawOffset(E elem) {
        return elem.getRawOffset();
    }

    protected void setElementRawOffset(E elem, int rawOffset) {
        elem.setRawOffset(rawOffset);
    }

    protected int attachElement(E elem) {
        return elem.attach(this);
    }

    protected void detachElement(E elem) {
        elem.detach(this);
    }

    protected E getAttachedElement(Object o) {
        if ((o instanceof Offset) && ((Offset) o).checkOwner(this)) {
            @SuppressWarnings("unchecked") //NOI18N
            E element = (E) o;
            return element; 
        } else {
            return null;
        }
    }

    /**
     * An offset gap list element. The <code>OffsetGapList</code> can accomodate
     * either instances of this class or any of its subclass.
     */
    public static class Offset {
        
        private int originalOrRawOffset;
        private OffsetGapList list;
        
        /**
         * Creates a new <code>Offset</code> object and sets its original offset
         * to the value passed in.
         * 
         * @param offset The original offset of this <code>Offset</code> object.
         */
        public Offset(int offset) {
            this.originalOrRawOffset = offset;
        }

        /**
         * Gets the offset of this <code>Offset</code> object. The offset is
         * either the original offset passed to the constructor if this <code>Offset</code>
         * instance has not been attached to a list yet or it is the real
         * offset of this instance, which reflects all offset updates in the list
         * (i.e. it gets updated when {@link AbstractOffsetGapList#defaultInsertUpdate} or
         * {@link AbstractOffsetGapList#defaultRemoveUpdate} is called).
         * 
         * @return The offset of this <code>Offset</code> instance.
         */
        public final int getOffset() {
            if (list == null) {
                return originalOrRawOffset;
            } else {
                return list.raw2Offset(getRawOffset());
            }
        }
        
        int attach(OffsetGapList list) {
            assert this.list == null : "Offset instances can only be added to one OffsetGapList."; //NOI18N
            this.list = list;
            return originalOrRawOffset;
        }

        void detach(OffsetGapList list) {
            assert this.list == list : "Can't detach from a foreign list."; //NOI18N
            this.list = null;
        }
        
        private boolean checkOwner(OffsetGapList list) {
            return this.list == list;
        }
        
        int getRawOffset() {
            return originalOrRawOffset;
        }
        
        void setRawOffset(int rawOffset) {
            this.originalOrRawOffset = rawOffset;
        }
    } // End of Offset class
}
