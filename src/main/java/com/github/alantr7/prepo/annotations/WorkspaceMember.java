package com.github.alantr7.prepo.annotations;

import javax.annotation.Priority;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@InterceptorBinding
public @interface WorkspaceMember {

    String workspaceParam() default "workspace";

}
