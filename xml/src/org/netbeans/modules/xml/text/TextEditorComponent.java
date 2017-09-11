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
package org.netbeans.modules.xml.text;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.netbeans.modules.xml.util.Util;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.TopComponent.Description;

/**
 * CloneableEditor subclass taking care about listening and delegating these event to
 * support.
 * <p> 
 * Listens at: DataObject, caret
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
@Description(preferredID = "xml.source", iconBase="org/netbeans/modules/xml/resources/xmlObject.gif")
public class TextEditorComponent extends CloneableEditor {

    /** Serial Version UID */
    private static final long serialVersionUID =5983822115073046891L;    

    /** The support, subclass of EditorSupport */
    private TextEditorSupport support;
    
    /** Listener on caret movements */
    private CaretListener caretListener;


    //
    // init
    //

    /** Only for externalization */
    public TextEditorComponent () {
        super();
    }

    /** Creates new editor */
    public TextEditorComponent (TextEditorSupport editor) {
        super (editor, true);
        initialize();
        dockIntoEditorMode();
    }

    
    //
    // itself
    //

//    // called from inner class -- to be 1.2 compiler happy
//    protected void updateName () {
//        super.updateName();
//    }
        
    /** Obtain a support for this component */
    private void initialize () {
        
        //#25368 there is memory leak in window system for default persistence type
        // anyway remember position only for opened components
        putClientProperty("PersistenceType", "OnlyOpened");                     // NOI18N
        
        support = (TextEditorSupport)cloneableEditorSupport(); //(TextEditorSupport)obj.getCookie (TextEditorSupport.class);
        caretListener = new CaretListener() {
                public void caretUpdate (CaretEvent e) {
                    support.restartTimer (true);
                }
            };
        if (pane != null) {  //??? what if does not exist
            pane.addFocusListener (new FocusListener () {
                    public void focusGained (FocusEvent e) {
                    }
                    public void focusLost (FocusEvent e) {
                        support.syncDocument (true);
                    }
                });
        }
//          support.getDataObject().addPropertyChangeListener (new PropertyChangeListener () {
//                  public void propertyChange (PropertyChangeEvent ev) {
//                      if (DataObject.PROP_NAME.equals (ev.getPropertyName())) {
//                          TextEditorComponent.this.updateName();
//                      }
//                  }
//              });
    }

    /**
     */
    private void dockIntoEditorMode () {
	// dock into editor mode if possible        
        Workspace current = WindowManager.getDefault().getCurrentWorkspace();
        Mode editorMode = current.findMode (CloneableEditorSupport.EDITOR_MODE);
        if ( editorMode != null ) {
            editorMode.dockInto (this);
        }        
    }

//      /** Returns Editor pane for private use.
//       * @return Editor pane for private use.
//       */
//      private JEditorPane getEditorPane () {
//          return pane;
//      }

//      /* Is called from the clone method to create new component from this one.
//       * This implementation only clones the object by calling super.clone method.
//       * @return the copy of this object
//       */
//      protected CloneableTopComponent createClonedObject () {
//          return support.createTextEditorComponent();
//      }


    /* This method is called when parent window of this component has focus,
     * and this component is preferred one in it. This implementation adds 
     * performer to the ToggleBreakpointAction.
     */
    @Override
    protected void componentActivated () {
        if(pane != null)
            pane.addCaretListener (caretListener);
        super.componentActivated();
    }

    /*
     * This method is called when parent window of this component losts focus,
     * or when this component losts preferrence in the parent window. This 
     * implementation removes performer from the ToggleBreakpointAction.
     */
    @Override
    protected void componentDeactivated () {
        if(pane != null)
            pane.removeCaretListener (caretListener);
        super.componentDeactivated();
    }

    /** Deserialize this top component.
     * @param in the stream to deserialize from
     */
    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TextEditorComponent.readExternal()"); // NOI18N

        super.readExternal (in);

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("                   .readExternal(): support = " + cloneableEditorSupport()); // NOI18N

        initialize();

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\tdone."); // NOI18N
    }
    
    /** Serialize this top component.
     * Subclasses wishing to store state must call the super method, then write to the stream.
     * @param out the stream to serialize to
     */
    public void writeExternal (ObjectOutput out) throws IOException {        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TextEditorComponent.writeExternal(): support = " + cloneableEditorSupport()); // NOI18N

        super.writeExternal (out);        

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("\tdone."); // NOI18N
    }
    
}
