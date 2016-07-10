package pku.abe.auth;

import pku.abe.commons.json.JsonBuilder;
import com.google.api.client.repackaged.com.google.common.base.Strings;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class AuthFilter implements Filter {
    @Resource
    private SignAuthService signAuthService;

    @Resource
    private LinkedWebAuthService linkedWebAuthService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        // 登录、注册、下载demo不需要token
        // OPTIONS请求主要解决跨域问题,不用验证
        // 获取app logo的请求不用验证
        if (httpRequest.getMethod().equals("OPTIONS") || path.contains("/user/login") || path.contains("/user/register")
                || path.contains("/v1/request_demo") || path.contains("/images/")) {
            chain.doFilter(request, response);
            return;
        }

        String authInfo = httpRequest.getHeader("Authorization");
        if (Strings.isNullOrEmpty(authInfo)) {
            response.getWriter().write(getAuthFailedMsg());
            return;
        }
        String[] authInfos = authInfo.split(":");
        if (authInfos.length != 2) {
            response.getWriter().write(getAuthFailedMsg());
            return;
        }

        if (authInfos[0] == null || authInfos[1] == null) {
            response.getWriter().write(getAuthFailedMsg());
            return;
        }

        String authMethod = authInfos[0];
        String[] authInfoContent = authInfos[1].trim().split(" ");

        if (authInfoContent.length != 2) {
            response.getWriter().write(getAuthFailedMsg());
            return;
        }

        if (authInfoContent[0] == null || authInfoContent[1] == null) {
            response.getWriter().write(getAuthFailedMsg());
            return;
        }

        String token = authInfoContent[0];
        String user_id = authInfoContent[1];

        if (authMethod.trim().equals("Sign")) {
            if (signAuthService.doAuth(request, response)) {
                chain.doFilter(request, response);
            } else {
                httpRequest = (HttpServletRequest) request;
                String responseBody = "{\"sign auth failed, " + httpRequest.getRequestURI() + "\"}";
                byte[] responseData = responseBody.getBytes();
                ServletOutputStream output = response.getOutputStream();
                output.write(responseData);
                output.flush();
                output.close();
            }
        } else if (authMethod.trim().equals("Token")) {
            if (linkedWebAuthService.doAuth(request, user_id, token)) {
                try {
                    chain.doFilter(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                httpRequest = (HttpServletRequest) request;
                String responseBody = "{\"basic auth failed, " + httpRequest.getRequestURI() + "\"}";
                byte[] responseData = responseBody.getBytes();
                ServletOutputStream output = response.getOutputStream();
                output.write(responseData);
                output.flush();
                output.close();
            }
        } else {
            response.getWriter().write(getAuthFailedMsg());
            return;
        }

    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    private String getAuthFailedMsg() {
        JsonBuilder jb = new JsonBuilder();
        jb.append("error_code", 40100);
        jb.append("err_msg", "Auth failed!");
        return jb.flip().toString();
    }

}
