package mx.edu.uaa.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Este es un Filtro de Servlet Puro. Se ejecuta ANTES de que Jersey despierte.
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        // 1. Agregamos los encabezados permisivos
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        res.setHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        res.setHeader("Access-Control-Allow-Credentials", "true");

        // 2. TRUCO DE MAGIA: Si es OPTIONS, respondemos OK y cortamos aquí.
        // Así Jersey nunca se entera y no lanza el error 405.
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 3. Si no es OPTIONS, dejamos pasar la petición hacia Jersey
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
