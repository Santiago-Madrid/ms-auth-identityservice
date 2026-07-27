package com.wd.ms_auth.identityservice.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.world_dance.wd_lib_common.enums.Role;

@Target(ElementType.METHOD)

@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Role[] value();
}
