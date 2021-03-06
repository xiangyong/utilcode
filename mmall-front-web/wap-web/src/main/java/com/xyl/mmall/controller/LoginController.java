/**
 * 
 */
package com.xyl.mmall.controller;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netease.print.security.authc.LogoutService;
import com.netease.print.security.util.SecurityContextUtils;
import com.xyl.mmall.constant.MmallConstant;
import com.xyl.mmall.framework.util.RegexUtils;
import com.xyl.mmall.mainsite.facade.OnlineActivityFacade;
import com.xyl.mmall.security.utils.CookieUtils;

/**
 * 登录相关。
 * 
 * @author lihui
 *
 */
@Controller
public class LoginController {
	
	private static Logger log = Logger.getLogger(LocationController.class);

	@Autowired
	private LogoutService logoutService;
	
	@Autowired
	private OnlineActivityFacade onlineActivityFacade;
	
	@Value("${mainsite.server}")
    private String domain;

	/**
	 * 跳转至登录页面。
	 * 
	 * @return
	 */
	@RequestMapping(value = "/login")
	public String login() {
		return "pages/login";
	}

	/**
	 * 用户登出。
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Subject subject = SecurityUtils.getSubject();
		try {
			subject.logout();
		} catch (SessionException ise) {
			log.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
		}
		
		boolean domainIsIp = RegexUtils.isValidIP(domain);
		
		// 1.用户退出,清理相关cookie
	    Cookie xylUN = CookieUtils.createCookie(MmallConstant.XYL_MAINSITE_USERNAME, null);
	    if (domainIsIp) xylUN.setDomain(domain);
	    xylUN.setMaxAge(0);
        response.addCookie(xylUN);
	    
        Cookie xylSess = CookieUtils.createCookie(MmallConstant.XYL_MAINSITE_SESS, null);
	    if (domainIsIp) xylSess.setDomain(domain);
        xylSess.setMaxAge(0);
        response.addCookie(xylSess);
        
        Cookie exp = CookieUtils.createCookie(MmallConstant.XYL_MAINSITE_EXPIRES, null);
	    if (domainIsIp) exp.setDomain(domain);
        exp.setMaxAge(0);
        response.addCookie(exp);

        Cookie nick = CookieUtils.createCookie(MmallConstant.COOKIE_USER_NICK_NAME, null, MmallConstant.MAIN_SITE_DOMAIN);
	    if (domainIsIp) nick.setDomain(domain);
        nick.setMaxAge(0);
        response.addCookie(nick);
        
        return new ResponseEntity<String>("", HttpStatus.OK);
	}

	/**
	 * 登录成功后做cookie认证以及session创建，并跳转至指定URL。
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/authAndRedirect", method = RequestMethod.GET)
	public void authAndRedirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		onlineActivityFacade.bindCouponPack(SecurityContextUtils.getUserId());
		String redirectURL = ServletRequestUtils.getStringParameter(request, "redirectURL", "");
		if (StringUtils.isNotBlank(redirectURL)) {
			WebUtils.issueRedirect(request, response, redirectURL);
		} else {
			WebUtils.issueRedirect(request, response, "/");
		}
	}
}
