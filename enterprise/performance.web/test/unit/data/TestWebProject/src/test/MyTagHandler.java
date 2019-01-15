/*
 * MyTagHandler.java
 *
 * Created on 24. erven 2004, 17:16
 */
 
package test;           

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

/** 
 *
 * @author  lm97939
 * @version 
 */

public class MyTagHandler extends SimpleTagSupport {

    /**Called by the container to invoke this tag. 
    * The implementation of this method is provided by the tag library developer,
    * and handles all tag processing, body iteration, etc.
    */
    public void doTag() throws JspException {
        
        JspWriter out=getJspContext().getOut();

        try {
            getJspContext().getOut().write( "Hello, world!" );
        } catch (java.io.IOException ex) {
            throw new JspException(ex.getMessage());
        }

    }
}
