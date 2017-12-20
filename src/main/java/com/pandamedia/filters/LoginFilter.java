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
 * Prevents access to pages in the clientsecure folder for users that are not
 * logged in.
 * 
 * @author Erika Bourque
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = {"/clientsecure/*"})
public class LoginFilter implements Filter{
    private static final Logger LOG = Logger.getLogger("LoginFilter.class");
    private ServletContext context;
    
    @Inject
    private UserActionBean uab;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        context.log("Login Filter");
        
        // Making sure user is not null or not persisted to db
        if (!uab.isLogin())
        {
            context.log("User not logged in.");
            String contextPath = ((HttpServletRequest) request)
                    .getContextPath();
            ((HttpServletResponse) response).sendRedirect(contextPath
                    + "/userconnection/login.xhtml");
            context.log(contextPath + "/userconnection/login.xhtml");
        }
        else
        {
            context.log("User is logged in.  id = " + uab.getCurrUser().getId());
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do here
    }   
}