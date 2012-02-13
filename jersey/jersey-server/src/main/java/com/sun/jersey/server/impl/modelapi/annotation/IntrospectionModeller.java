/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.server.impl.modelapi.annotation;

import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.api.model.AbstractField;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSetterMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.model.Parameter.Source;
import com.sun.jersey.api.model.Parameterized;
import com.sun.jersey.api.model.PathValue;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.impl.ImplMessages;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author japod
 */
public class IntrospectionModeller {

    private static final Logger LOGGER = Logger.getLogger(IntrospectionModeller.class.getName());

    public static AbstractResource createResource(Class<?> resourceClass) {
        final Class<?> annotatedResourceClass = getAnnotatedResourceClass(resourceClass);
        final Path rPathAnnotation = annotatedResourceClass.getAnnotation(Path.class);
        final boolean isRootResourceClass = (null != rPathAnnotation);

        final boolean isEncodedAnotOnClass = 
                (null != annotatedResourceClass.getAnnotation(Encoded.class));

        AbstractResource resource;

        if (isRootResourceClass) {
            resource = new AbstractResource(resourceClass,
                    new PathValue(rPathAnnotation.value()));
        } else { // just a subresource class
            resource = new AbstractResource(resourceClass);
        }

        workOutConstructorsList(resource, resourceClass.getConstructors(), 
                isEncodedAnotOnClass);

        workOutFieldsList(resource, isEncodedAnotOnClass);
        
        final MethodList methodList = new MethodList(resourceClass);

        workOutSetterMethodsList(resource, methodList, isEncodedAnotOnClass);
        
        final Consumes classScopeConsumesAnnotation = 
                annotatedResourceClass.getAnnotation(Consumes.class);
        final Produces classScopeProducesAnnotation = 
                annotatedResourceClass.getAnnotation(Produces.class);
        workOutResourceMethodsList(resource, methodList, isEncodedAnotOnClass, 
                classScopeConsumesAnnotation, classScopeProducesAnnotation);
        workOutSubResourceMethodsList(resource, methodList, isEncodedAnotOnClass, 
                classScopeConsumesAnnotation, classScopeProducesAnnotation);
        workOutSubResourceLocatorsList(resource, methodList, isEncodedAnotOnClass);

        workOutPostConstructPreDestroy(resource);

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(ImplMessages.NEW_AR_CREATED_BY_INTROSPECTION_MODELER(
                    resource.toString()));
        }

        return resource;
    }
    
    private static Class getAnnotatedResourceClass(Class rc) {
        if (rc.isAnnotationPresent(Path.class)) return rc;

        for (Class i : rc.getInterfaces())
            if (i.isAnnotationPresent(Path.class)) return i;

        return rc;
    }
    
    private static void addConsumes(
            AnnotatedMethod am,            
            AbstractResourceMethod resourceMethod, 
            Consumes consumeMimeAnnotation) {
        // Override annotation is present in method
        if (am.isAnnotationPresent(Consumes.class))
            consumeMimeAnnotation = am.getAnnotation(Consumes.class);
        
        resourceMethod.setAreInputTypesDeclared(consumeMimeAnnotation != null);
        resourceMethod.getSupportedInputTypes().addAll(
                MediaTypes.createMediaTypes(consumeMimeAnnotation));
    }

    private static void addProduces(
            AnnotatedMethod am,
            AbstractResourceMethod resourceMethod, 
            Produces produceMimeAnnotation) {
        // Override annotation is present in method
        if (am.isAnnotationPresent(Produces.class))
            produceMimeAnnotation = am.getAnnotation(Produces.class);
        
        resourceMethod.setAreOutputTypesDeclared(produceMimeAnnotation != null);
        resourceMethod.getSupportedOutputTypes().addAll(
                MediaTypes.createQualitySourceMediaTypes(produceMimeAnnotation));
    }

    private static void workOutConstructorsList(
            AbstractResource resource, 
            Constructor[] ctorArray, 
            boolean isEncoded) {
        if (null != ctorArray) {
            for (Constructor ctor : ctorArray) {
                final AbstractResourceConstructor aCtor = 
                        new AbstractResourceConstructor(ctor);
                processParameters(
                        resource.getResourceClass(),
                        ctor.getDeclaringClass(),
                        aCtor,
                        ctor,
                        isEncoded);
                resource.getConstructors().add(aCtor);
            }
        }
    }

    private static void workOutFieldsList(
            AbstractResource resource, 
            boolean isEncoded) {        
        Class c = resource.getResourceClass();
        if (c.isInterface())
            return;
        
        while (c != Object.class) {
             for (final Field f : c.getDeclaredFields()) {
                if (f.getDeclaredAnnotations().length > 0) {
                    final AbstractField af = new AbstractField(f);
                    Parameter p = createParameter(
                            resource.getResourceClass(),
                            f.getDeclaringClass(),
                            isEncoded,
                            f.getType(),
                            f.getGenericType(),
                            f.getAnnotations());
                    if (null != p) {
                        af.getParameters().add(p);
                        resource.getFields().add(af);
                    }
                }
             }
             c = c.getSuperclass();
        }
    }

    private static void workOutPostConstructPreDestroy(AbstractResource resource) {
        Class postConstruct = ReflectionHelper.classForName("javax.annotation.PostConstruct");
        if (postConstruct == null)
            return;

        Class preDestroy = ReflectionHelper.classForName("javax.annotation.PreDestroy");

        final MethodList methodList = new MethodList(resource.getResourceClass(), true);
        HashSet<String> names = new HashSet<String>();
        for (AnnotatedMethod m : methodList.
                hasAnnotation(postConstruct).
                hasNumParams(0).
                hasReturnType(void.class)) {
            Method method = m.getMethod();
            // only add method if not hidden/overridden
            if (names.add(method.getName())) {
                ReflectionHelper.setAccessibleMethod(method);
                resource.getPostConstructMethods().add(0, method);
            }
        }

        names = new HashSet<String>();
        for (AnnotatedMethod m : methodList.
                hasAnnotation(preDestroy).
                hasNumParams(0).
                hasReturnType(void.class)) {
            Method method = m.getMethod();
            // only add method if not hidden/overridden
            if (names.add(method.getName())) {
                ReflectionHelper.setAccessibleMethod(method);
                resource.getPreDestroyMethods().add(method);
            }
        }
    }

    private static void workOutSetterMethodsList(
            AbstractResource resource, 
            MethodList methodList,
            boolean isEncoded) {
        for (AnnotatedMethod m : methodList.
                hasNotMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class).
                hasNumParams(1).
                hasReturnType(void.class).
                nameStartsWith("set")) {
            
            final AbstractSetterMethod asm = new AbstractSetterMethod(resource, m.getMethod(), m.getAnnotations());
            Parameter p = createParameter(
                    resource.getResourceClass(),
                    m.getMethod().getDeclaringClass(),
                    isEncoded,
                    m.getParameterTypes()[0],
                    m.getGenericParameterTypes()[0],
                    m.getAnnotations());
            if (null != p) {
                asm.getParameters().add(p);
                resource.getSetterMethods().add(asm);
            }
        }        
    }
    
    private static void workOutResourceMethodsList(
            AbstractResource resource, 
            MethodList methodList, 
            boolean isEncoded,
            Consumes classScopeConsumesAnnotation, 
            Produces classScopeProducesAnnotation) {
        for (AnnotatedMethod m : methodList.hasMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class)) {

            final ReflectionHelper.ClassTypePair ct = getGenericReturnType(resource.getResourceClass(), m.getMethod());
            final AbstractResourceMethod resourceMethod = new AbstractResourceMethod(
                    resource,
                    m.getMethod(),
                    ct.c, ct.t,
                    m.getMetaMethodAnnotations(HttpMethod.class).get(0).value(),
                    m.getAnnotations());

            addConsumes(m, resourceMethod, classScopeConsumesAnnotation);
            addProduces(m, resourceMethod, classScopeProducesAnnotation);
            processParameters(
                    resourceMethod.getResource().getResourceClass(),
                    resourceMethod.getMethod().getDeclaringClass(),
                    resourceMethod, m, isEncoded);

            resource.getResourceMethods().add(resourceMethod);
        }
    }

    private static ReflectionHelper.ClassTypePair getGenericReturnType(
            Class concreteClass,
            Method m) {
        return getGenericType(concreteClass, m.getDeclaringClass(), m.getReturnType(), m.getGenericReturnType());
    }
    
    private static void workOutSubResourceMethodsList(
            AbstractResource resource, 
            MethodList methodList, 
            boolean isEncoded,
            Consumes classScopeConsumesAnnotation, 
            Produces classScopeProducesAnnotation) {

        for (AnnotatedMethod m : methodList.hasMetaAnnotation(HttpMethod.class).hasAnnotation(Path.class)) {

            final Path mPathAnnotation = m.getAnnotation(Path.class);
            final PathValue pv = new PathValue(mPathAnnotation.value());

            final boolean emptySegmentCase =  "/".equals(pv.getValue()) || "".equals(pv.getValue());
            
            if (!emptySegmentCase) {
                final ReflectionHelper.ClassTypePair ct = getGenericReturnType(resource.getResourceClass(), m.getMethod());
                final AbstractSubResourceMethod abstractSubResourceMethod = new AbstractSubResourceMethod(
                        resource,
                        m.getMethod(),
                        ct.c, ct.t,
                        pv,
                        m.getMetaMethodAnnotations(HttpMethod.class).get(0).value(),
                        m.getAnnotations());

                addConsumes(m, abstractSubResourceMethod, classScopeConsumesAnnotation);
                addProduces(m, abstractSubResourceMethod, classScopeProducesAnnotation);
                processParameters(
                        abstractSubResourceMethod.getResource().getResourceClass(),
                        abstractSubResourceMethod.getMethod().getDeclaringClass(),
                        abstractSubResourceMethod, m, isEncoded);

                resource.getSubResourceMethods().add(abstractSubResourceMethod);

            } else { // treat the sub-resource method as a resource method
                final ReflectionHelper.ClassTypePair ct = getGenericReturnType(resource.getResourceClass(), m.getMethod());
                final AbstractResourceMethod abstractResourceMethod = new AbstractResourceMethod(
                        resource,
                        m.getMethod(),
                        ct.c, ct.t,
                        m.getMetaMethodAnnotations(HttpMethod.class).get(0).value(),
                        m.getAnnotations());

                addConsumes(m, abstractResourceMethod, classScopeConsumesAnnotation);
                addProduces(m, abstractResourceMethod, classScopeProducesAnnotation);
                processParameters(
                        abstractResourceMethod.getResource().getResourceClass(),
                        abstractResourceMethod.getMethod().getDeclaringClass(),
                        abstractResourceMethod, m, isEncoded);

                resource.getResourceMethods().add(abstractResourceMethod);
            }
        }
    }
    
    private static void workOutSubResourceLocatorsList(
            AbstractResource resource, 
            MethodList methodList, 
            boolean isEncoded) {

        for (AnnotatedMethod m : methodList.hasNotMetaAnnotation(HttpMethod.class).
                hasAnnotation(Path.class)) {
            final Path mPathAnnotation = m.getAnnotation(Path.class);
            final AbstractSubResourceLocator subResourceLocator = new AbstractSubResourceLocator(
                    resource,
                    m.getMethod(),
                    new PathValue(
                        mPathAnnotation.value()),
                    m.getAnnotations());

            processParameters(
                    subResourceLocator.getResource().getResourceClass(),
                    subResourceLocator.getMethod().getDeclaringClass(),
                    subResourceLocator, m, isEncoded);

            resource.getSubResourceLocators().add(subResourceLocator);
        }
    }

    private static void processParameters(
            Class concreteClass,
            Class declaringClass,
            Parameterized parametrized, 
            Constructor ctor, 
            boolean isEncoded) {
        Class[] parameterTypes = ctor.getParameterTypes();
        Type[] genericParameterTypes = ctor.getGenericParameterTypes();
        // Workaround bug http://bugs.sun.com/view_bug.do?bug_id=5087240
        if (parameterTypes.length != genericParameterTypes.length) {
            Type[] _genericParameterTypes = new Type[parameterTypes.length];
            _genericParameterTypes[0] = parameterTypes[0];
            System.arraycopy(genericParameterTypes, 0, _genericParameterTypes, 1, genericParameterTypes.length);
            genericParameterTypes = _genericParameterTypes;
        }

        processParameters(
                concreteClass, declaringClass,
                parametrized,
                ((null != ctor.getAnnotation(Encoded.class)) || isEncoded),
                parameterTypes,
                genericParameterTypes,
                ctor.getParameterAnnotations());
    }

    private static void processParameters(
            Class concreteClass,
            Class declaringClass,
            Parameterized parametrized, 
            AnnotatedMethod method, 
            boolean isEncoded) {
        processParameters(
                concreteClass, declaringClass,
                parametrized,
                ((null != method.getAnnotation(Encoded.class)) || isEncoded),
                method.getParameterTypes(), 
                method.getGenericParameterTypes(), 
                method.getParameterAnnotations());
    }

    private static void processParameters(
            Class concreteClass,
            Class declaringClass,
            Parameterized parametrized,
            boolean isEncoded,
            Class[] parameterTypes,
            Type[] genericParameterTypes,
            Annotation[][] parameterAnnotations) {

        for (int i = 0; i < parameterTypes.length; i++) {
            Parameter parameter = createParameter(
                    concreteClass, declaringClass,
                    isEncoded, parameterTypes[i], 
                    genericParameterTypes[i], 
                    parameterAnnotations[i]);
            if (null != parameter) {
                parametrized.getParameters().add(parameter);
            } else {
                // clean up the parameters
                parametrized.getParameters().removeAll(parametrized.getParameters());
                break;
            }
        }
    }

    private static interface ParamAnnotationHelper<T extends Annotation> {

        public String getValueOf(T a);

        public Parameter.Source getSource();
    }

    private static Map<Class, ParamAnnotationHelper> createParamAnotHelperMap() {
        Map<Class, ParamAnnotationHelper> m = new WeakHashMap<Class, ParamAnnotationHelper>();
        m.put(Context.class, new ParamAnnotationHelper<Context>() {

            @Override
            public String getValueOf(Context a) {
                return null;
            }

            @Override
            public Parameter.Source getSource() {
                return Parameter.Source.CONTEXT;
            }
        });
        m.put(HeaderParam.class, new ParamAnnotationHelper<HeaderParam>() {

            @Override
            public String getValueOf(HeaderParam a) {
                return a.value();
            }

            @Override
            public Parameter.Source getSource() {
                return Parameter.Source.HEADER;
            }
        });
        m.put(CookieParam.class, new ParamAnnotationHelper<CookieParam>() {

            @Override
            public String getValueOf(CookieParam a) {
                return a.value();
            }

            @Override
            public Parameter.Source getSource() {
                return Parameter.Source.COOKIE;
            }
        });
        m.put(MatrixParam.class, new ParamAnnotationHelper<MatrixParam>() {

            @Override
            public String getValueOf(MatrixParam a) {
                return a.value();
            }

            @Override
            public Parameter.Source getSource() {
                return Parameter.Source.MATRIX;
            }
        });
        m.put(QueryParam.class, new ParamAnnotationHelper<QueryParam>() {

            @Override
            public String getValueOf(QueryParam a) {
                return a.value();
            }

            @Override
            public Parameter.Source getSource() {
                return Parameter.Source.QUERY;
            }
        });
        m.put(PathParam.class, new ParamAnnotationHelper<PathParam>() {

            @Override
            public String getValueOf(PathParam a) {
                return a.value();
            }

            @Override
            public Parameter.Source getSource() {
                return Parameter.Source.PATH;
            }
        });
        m.put(FormParam.class, new ParamAnnotationHelper<FormParam>() {

            @Override
            public String getValueOf(FormParam a) {
                return a.value();
            }

            @Override
            public Parameter.Source getSource() {
                return Parameter.Source.FORM;
            }
        });
        return Collections.unmodifiableMap(m);
    }
    private final static Map<Class, ParamAnnotationHelper> ANOT_HELPER_MAP = 
            createParamAnotHelperMap();

    @SuppressWarnings("unchecked")
    private static Parameter createParameter(
            Class concreteClass,
            Class declaringClass,
            boolean isEncoded, 
            Class<?> paramClass, 
            Type paramType, 
            Annotation[] annotations) {

        if (null == annotations) {
            return null;
        }

        Annotation paramAnnotation = null;
        Parameter.Source paramSource = null;
        String paramName = null;
        boolean paramEncoded = isEncoded;
        String paramDefault = null;
        
        /**
         * Create a parameter from the list of annotations.
         * Unknown annotated parameters are also supported, and in such a
         * cases the last unrecognized annotation is taken to be that
         * associated with the parameter.
         */
        for (Annotation annotation : annotations) {
            if (ANOT_HELPER_MAP.containsKey(annotation.annotationType())) {
                ParamAnnotationHelper helper = ANOT_HELPER_MAP.get(annotation.annotationType());
                paramAnnotation = annotation;
                paramSource = helper.getSource();
                paramName = helper.getValueOf(annotation);
            } else if (Encoded.class == annotation.annotationType()) {
                paramEncoded = true;
            } else if (DefaultValue.class == annotation.annotationType()) {
                paramDefault = ((DefaultValue) annotation).value();
            } else {
                paramAnnotation = annotation; 
                paramSource = Source.UNKNOWN;
                paramName = getValue(annotation);
            }
        }

        if (paramAnnotation == null) {
            paramSource = Parameter.Source.ENTITY;
        }

        ReflectionHelper.ClassTypePair ct = getGenericType(concreteClass, declaringClass, paramClass, paramType);
        paramType = ct.t;
        paramClass = ct.c;

        return new Parameter(
                annotations, paramAnnotation,
                paramSource,
                paramName, paramType, paramClass,
                paramEncoded, paramDefault);
    }

    private static String getValue(Annotation a) {
        try {
            Method m = a.annotationType().getMethod("value");
            if (m.getReturnType() != String.class)
                return null;
            return (String)m.invoke(a);
        } catch (Exception ex) {
        }
        return null;
    }

    private static ReflectionHelper.ClassTypePair getGenericType(
            final Class concreteClass,
            final Class declaringClass,
            final Class c,
            final Type t) {
        if (t instanceof TypeVariable) {
            ReflectionHelper.ClassTypePair ct = ReflectionHelper.resolveTypeVariable(
                    concreteClass,
                    declaringClass,
                    (TypeVariable)t);

            if (ct != null) {
                return ct;
            }
        } else if (t instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType)t;
            final Type[] ptts = pt.getActualTypeArguments();
            boolean modified =  false;
            for (int i = 0; i < ptts.length; i++) {
                ReflectionHelper.ClassTypePair ct =
                        getGenericType(concreteClass, declaringClass, (Class)pt.getRawType(), ptts[i]);
                if (ct.t != ptts[i]) {
                    ptts[i] = ct.t;
                    modified = true;
                }
            }
            if (modified) {
                ParameterizedType rpt = new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return ptts.clone();
                    }

                    @Override
                    public Type getRawType() {
                        return pt.getRawType();
                    }

                    @Override
                    public Type getOwnerType() {
                        return pt.getOwnerType();
                    }
                };
                return new ReflectionHelper.ClassTypePair((Class)pt.getRawType(), rpt);
            }
        } else if (t instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType)t;
            final ReflectionHelper.ClassTypePair ct =
                    getGenericType(concreteClass, declaringClass, null, gat.getGenericComponentType());
            if (gat.getGenericComponentType() != ct.t) {
                try {
                    Class ac = ReflectionHelper.getArrayClass(ct.c);
                    return new ReflectionHelper.ClassTypePair(ac, ac);
                } catch (Exception e) {
                }
            }
        }

        return new ReflectionHelper.ClassTypePair(c, t);
    }
}