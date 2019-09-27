/*
 * SumServlet.java
 *
 * Created on 07 January 2005, 15:28
 */

package org.netbeans.test.servlets;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Administrator
 * @version
 */
public class DivideServlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String contextPath = request.getContextPath();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet DIVIDE</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet DivideServlet at " + contextPath + "</h1>");
        
        org.netbeans.test.freeformlib.Multiplier d = new org.netbeans.test.freeformlib.Multiplier();
        try {
            String attributeX = request.getParameter("x");
            if (attributeX == null) {
                attributeX = "";
            }
            d.setX(Double.parseDouble(attributeX));
        } catch(NumberFormatException e) {
        }
        try {
            String attributeY = request.getParameter("y");
            if (attributeY == null) {
                attributeY = "";
            }
            d.setY(Double.parseDouble(attributeY));
        } catch(NumberFormatException e) {
        }
        
        if (d.getY() == 0) {
            out.println("<b>y</b> can't be 0!");
        } else {
            out.println("" + d.getX() + " / " + d.getY() + " = " + d.getMultiplication());
        }
        
        out.println("<br/>");
        out.println("<a href=\"index.jsp\">Go back to index.jsp</a>");
        out.println("</body>");
        out.println("</html>");
        
        out.close();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
