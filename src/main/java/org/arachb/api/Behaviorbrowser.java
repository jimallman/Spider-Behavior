package org.arachb.api;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This servlet should redirect?
 * @author pmidford
 *
 */

public class Behaviorbrowser extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {


        PrintWriter out = response.getWriter();
        out.println("{'Test works'}");
        out.flush();
        out.close();
    }
}
