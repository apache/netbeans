import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.jsp.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.jsp.*;

class SimplifiedJSPServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 String testProp = ""; 


	void mergedScriptlets(
		HttpServletRequest request,
		HttpServletResponse response,
		HttpSession session,
		ServletContext application,
		JspWriter out,
		ServletConfig config,
		JspContext jspContext,
		Object page,
		PageContext pageContext,
		Throwable exception
	) throws Throwable {
  System.err.println(testProp);

	}
}