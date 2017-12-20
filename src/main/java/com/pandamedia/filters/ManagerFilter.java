package com.pandamedia.filters;

import com.pandamedia.beans.UserActionBean;
import java.io.IOException;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Prevents access to the manager site for users that are not logged in and/or
 * are not managers.
 * 
 * @author Erika Bourque
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = {"/manager/*", "/manager/reports/*"})
public class ManagerFilter implements Filter{
    private static final Logger LOG = Logger.getLogger("ManagerFilter.class");
    private ServletContext context;
    
    @Inject
    private UserActionBean uab;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        context.log("Manager Filter");
        
        // Making sure user is logged in and is a manager
        if ((uab.isLogin()) && (uab.getCurrUser().getIsManager() == 1))
        {
            context.log("User is logged in and is a manager.  id = " + uab.getCurrUser().getId());
            chain.doFilter(request, response);            
        }
        else
        {
            context.log("User not logged in or is not a manager.");
            String contextPath = ((HttpServletRequest) request)
                    .getContextPath();
            context.log(contextPath + "/userconnection/login.xhtml");
            ((HttpServletResponse) response).sendRedirect(contextPath + "/userconnection/login.xhtml");
            
        }
    }

    @Override
    public void destroy() {
        // Nothing to do here
    }
    
}