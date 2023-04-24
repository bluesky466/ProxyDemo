package me.linjw.demo.proxy;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

import leo.android.cglib.proxy.Enhancer;
import leo.android.cglib.proxy.MethodInterceptor;
import leo.android.cglib.proxy.MethodProxy;

public class MainActivity extends Activity {
    public static final String TAG = "ProxyDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                Log.d(TAG, "invoke " + method, new Exception());
                return null;
            }
        };
        ITestInterface testInterface = (ITestInterface) Proxy.newProxyInstance(
                getClassLoader(),
                new Class[]{ITestInterface.class},
                handler);
        dumpsysClass(testInterface);
        testInterface.foo();

        Enhancer e = new Enhancer();
        e.setSuperclass(TestClass.class);
        e.setInterceptor(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Object[] objects, MethodProxy methodProxy) {
                methodProxy.invokeSuper(o, objects);
                Log.d(TAG, "invoke " + methodProxy.getOriginalMethod(), new Exception());
                return null;
            }
        });
        TestClass testClass = (TestClass) e.create(getCacheDir().getAbsolutePath());
        dumpsysClass(testClass);
        testClass.foo();

    }

    private void appendModifiers(int modifiers, StringBuilder sb) {
        if ((modifiers & Modifier.PUBLIC) != 0) {
            sb.append("public ");
        }
        if ((modifiers & Modifier.PRIVATE) != 0) {
            sb.append("private ");
        }
        if ((modifiers & Modifier.PROTECTED) != 0) {
            sb.append("protect ");
        }
        if ((modifiers & Modifier.ABSTRACT) != 0) {
            sb.append("abstract ");
        }
        if ((modifiers & Modifier.STATIC) != 0) {
            sb.append("static ");
        }
        if ((modifiers & Modifier.FINAL) != 0) {
            sb.append("final ");
        }
    }

    private void dumpsysClass(Object obj) {
        Class clazz = obj.getClass();

        StringBuilder sb = new StringBuilder();
        sb.append("class ")
                .append(clazz.getSimpleName())
                .append(" extends ")
                .append(clazz.getSuperclass().getSimpleName());

        if (clazz.getInterfaces().length != 0) {
            sb.append(" implements ");
            for (Class iface : clazz.getInterfaces()) {
                sb.append(iface.getSimpleName())
                        .append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
        }

        sb.append(" {\n");
        for (Field field : clazz.getDeclaredFields()) {
            sb.append("\t");
            appendModifiers(field.getModifiers(), sb);
            sb.append(field.getType().getSimpleName())
                    .append(" ")
                    .append(field.getName())
                    .append(";\n");
        }

        for (Method method : clazz.getDeclaredMethods()) {
            sb.append("\t");
            appendModifiers(method.getModifiers(), sb);
            sb.append(method.getName())
                    .append("(");
            for (Parameter parameter : method.getParameters()) {
                sb.append(parameter.getType().getSimpleName())
                        .append(" ")
                        .append(parameter.getName())
                        .append(",");
            }
            if (method.getParameters().length != 0) {
                sb.delete(sb.length() - 1, sb.length());
            }
            sb.append(") { ... }\n");
        }

        sb.append("}");
        Log.d(TAG, sb.toString());
    }
}