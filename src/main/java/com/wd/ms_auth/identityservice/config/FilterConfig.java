package com.wd.ms_auth.identityservice.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wd.ms_auth.identityservice.filter.JwtValidationFilter;

@Configuration
public class FilterConfig {


    /**
     * Registramos el filtro de validacion de jwt para que spring lo reconozca y lo ejecute en cada peticion
     * @param jwtValidationFilter
     * @return 
     */
    @Bean
    FilterRegistrationBean<JwtValidationFilter> jwtFilter(JwtValidationFilter jwtValidationFilter) {
        // Creamos un contenedor de registro del bean para el filtro
        FilterRegistrationBean<JwtValidationFilter> registrationBean = new FilterRegistrationBean<>();

        // Es decirle a Spring que este filtro es el que quiero que trabaje
        registrationBean.setFilter(jwtValidationFilter);

        // Definir el alcance del filtro, quiero que revise todas las peticiones que entren en mi app
        registrationBean.addUrlPatterns("/*");

        // Establecemos la prioridad de ejecucion de los filtros
        registrationBean.setOrder(0);

        // Retornamos el bean configurado para que spring lo guarde en su contexto (inyección)
        return registrationBean;
    }
}
