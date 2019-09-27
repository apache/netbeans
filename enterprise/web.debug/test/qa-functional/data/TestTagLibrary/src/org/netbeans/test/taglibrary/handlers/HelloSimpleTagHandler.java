/*
 * HelloSimpleTagHandler.java
 *
 * Created on 07 January 2005, 14:41
 */

package org.netbeans.test.taglibrary.handlers;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

/**
 *
 * @author  Administrator
 * @version
 */

public class HelloSimpleTagHandler extends SimpleTagSupport {

    /**
     * Initialization of name property.
     */
    private java.lang.String name;
    
    /**Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    public void doTag() throws JspException {
        
        JspWriter out=getJspContext().getOut();
        
        try {
            out.println("<h3>Hello " + name + "</h3>");
            
            out.println("<pre>BODY:");
            
            JspFragment f=getJspBody();
            if (f != null) f.invoke(out);
            
            out.println("\nEND</pre>");
            
        } catch (java.io.IOException ex) {
            throw new JspException(ex.getMessage());
        }
        
    }

    /**
     * Setter for the name attribute.
     */
    public void setName(java.lang.String value) {
        this.name = value;
    }
}
