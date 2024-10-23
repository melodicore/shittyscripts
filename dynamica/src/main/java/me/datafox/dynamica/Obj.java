package me.datafox.dynamica;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * An object that can represent any object regardless of type. Has an arbitrary number of fields and functions that can
 * be added to it during runtime. Can also wrap a native object, in which case it will automatically reflect upon the
 * native object's accessible fields and methods and registers them appropriately.
 *
 * @author datafox
 */
public final class Obj {
    private final Object parent;

    private final Map<String, FieldWrapper> fields;

    private final Map<String, Deque<Func>> funcs;

    private Obj(Object object) {
        parent = object;
        fields = new TreeMap<>();
        funcs = new TreeMap<>();
        if(parent != null) {
            reflect(parent.getClass());
        }
    }

    /**
     * @param name name of the field to be retrieved
     * @return value of the retrieved field
     */
    public Obj get(String name) {
        FieldWrapper f = fields.get(name);
        if(f == null) {
            return null;
        }
        try {
            return f.get();
        } catch(Throwable e) {
            throw new ObjAccessException("Could not retrieve field " + name, e);
        }
    }

    /**
     * @param name name of the field to be set
     * @param obj value for the field to be set
     */
    public void set(String name, Obj obj) {
        try {
            if(fields.containsKey(name)) {
                fields.get(name).set(obj);
                return;
            }
            fields.put(name, new FakeFieldWrapper(obj));
        } catch(Throwable e) {
            throw new ObjAccessException("Could not set field " + name, e);
        }
    }

    /**
     * @return names of all currently registered fields
     */
    public Obj getFieldNames() {
        return wrap(fields.keySet());
    }

    /**
     * @param name name of the function to be called
     * @param params parameters for the function to be called
     * @return value returned by the function or {@code null}
     */
    public Obj call(String name, Obj ... params) {
        if(!funcs.containsKey(name)) {
            throw new ObjAccessException("Could not call func " + name);
        }
        Throwable lastThrowable = null;
        for(Func func : funcs.get(name)) {
            try {
                return func.call(params);
            } catch(Throwable e) {
                lastThrowable = e;
            }
        }
        if(lastThrowable != null) {
            throw new ObjAccessException("Could not call func " + name, lastThrowable);
        }
        throw new ObjAccessException("Could not call func " + name);
    }

    /**
     * @param name name of the function to be registered
     * @param func function to be registered
     */
    public void register(String name, Func func) {
        if(!funcs.containsKey(name)) {
            funcs.put(name, new LinkedList<>());
        }
        if(func instanceof MethodFunc) {
            funcs.get(name).addLast(func);
        } else {
            funcs.get(name).addFirst(func);
        }
    }

    /**
     * @return names of all currently registered functions
     */
    public Obj getFuncNames() {
        return wrap(funcs.keySet());
    }

    /**
     * If one or more functions called hashCode are present, this method will iterate over them until one is found that
     * has no parameter and returns an integer. If none of them do, the hashcode of all fields will be returned instead.
     *
     * @return hash code of this object
     */
    @Override
    public int hashCode() {
        if(funcs.containsKey("hashCode")) {
            for(Func func : funcs.get("hashCode")) {
                Obj hash;
                try {
                    hash = func.call();
                } catch(Throwable ignored) {
                    continue;
                }
                if(unwrap(hash) instanceof Integer i) {
                    return i;
                }
            }
        }
        return fields.hashCode();
    }

    /**
     * If one or more functions called equals are present, this method will iterate over them until one is found that
     * has one parameter and returns a boolean. If none of them do, {@link Obj#equals(Object, Object)} will be called
     * instead.
     *
     * @return {@code true} if this object is equal to the specified object
     */
    @Override
    public boolean equals(Object obj) {
        if(funcs.containsKey("equals")) {
            for(Func func : funcs.get("equals")) {
                Obj equal;
                try {
                    equal = func.call(wrap(obj));
                } catch(Throwable ignored) {
                    continue;
                }
                if(unwrap(equal) instanceof Boolean b) {
                    return b;
                }
            }
        }
        return equals(this, obj);
    }

    /**
     * If one or more functions called toString are present, this method will iterate over them until one is found that
     * has no parameter and returns a string. If none of them do, the string representation of all fields will be
     * returned instead.
     *
     * @return string representation of this object
     */
    @Override
    public String toString() {
        if(funcs.containsKey("toString")) {
            for(Func func : funcs.get("toString")) {
                Obj string;
                try {
                    string = func.call();
                } catch(Throwable ignored) {
                    continue;
                }
                if(unwrap(string) instanceof String s) {
                    return s;
                }
            }
        }
        return fields.toString();
    }

    /**
     * @return object that is not backed by any native object
     */
    public static Obj create() {
        return new Obj(null);
    }

    /**
     * @param o native object to back this object with
     * @return object that is backed by the specified native object
     */
    public static Obj of(Object o) {
        if(o instanceof Obj obj) {
            return obj;
        }
        return new Obj(o);
    }

    /**
     * If both objects are {@link Obj} and not backed with a native object, their fields are compared. Otherwise
     * {@link Objects#equals(Object, Object)} is used.
     *
     * @param a first object to be compared
     * @param b second object to be compared
     * @return {@code true} if the specified objects are equal to each other
     */
    public static boolean equals(Object a, Object b) {
        if(a == b) {
            return true;
        }
        if(a == null || b == null) {
            return false;
        }
        a = unwrap(a);
        b = unwrap(b);
        if(a instanceof Obj ao && b instanceof Obj bo) {
            return ao.fields.equals(bo.fields);
        }
        return Objects.equals(a, b);
    }

    private void reflect(Class<?> aClass) {
        Arrays.stream(aClass.getDeclaredFields())
                .filter(Field::trySetAccessible)
                .map(ActualFieldWrapper::new)
                .forEach(f -> fields.putIfAbsent(f.field.getName(), f));
        Arrays.stream(aClass.getDeclaredMethods())
                .filter(Method::trySetAccessible)
                .map(MethodFunc::new)
                .forEach(f -> register(f.method.getName(), f));

        if(aClass.getSuperclass() != null) {
            reflect(aClass.getSuperclass());
        }
    }

    private static Object unwrap(Object object) {
        if(object == null) {
            return null;
        }
        if(object instanceof Obj obj && obj.parent != null) {
            return obj.parent;
        }
        return object;
    }

    private static Obj wrap(Object object) {
        if(object == null) {
            return null;
        }
        if(object instanceof Obj obj) {
            return obj;
        }
        return new Obj(object);
    }

    private interface FieldWrapper {
        void set(Obj obj) throws IllegalAccessException;

        Obj get() throws IllegalAccessException;
    }

    private class ActualFieldWrapper implements FieldWrapper {
        private final Field field;

        private ActualFieldWrapper(Field field) {
            this.field = field;
        }

        @Override
        public void set(Obj obj) throws IllegalAccessException {
            field.set(parent, unwrap(obj));
        }

        @Override
        public Obj get() throws IllegalAccessException {
            return wrap(field.get(parent));
        }

        @Override
        public int hashCode() {
            try {
                return get().hashCode();
            } catch(Throwable ignored) {
                return 0;
            }
        }

        @Override
        public boolean equals(Object obj) {
            try {
                return Obj.equals(get(), obj);
            } catch(Throwable ignored) {
                return obj == null;
            }
        }

        @Override
        public String toString() {
            try {
                return get().toString();
            } catch(Throwable ignored) {
                return "null";
            }
        }
    }

    private static class FakeFieldWrapper implements FieldWrapper {
        private Object object;

        private FakeFieldWrapper(Object object) {
            this.object = object;
        }

        @Override
        public void set(Obj obj) {
            object = unwrap(obj);
        }

        @Override
        public Obj get() {
            return wrap(object);
        }

        @Override
        public int hashCode() {
            if(get() == null) {
                return 0;
            }
            return get().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return Obj.equals(get(), obj);
        }

        @Override
        public String toString() {
            if(get() == null) {
                return "null";
            }
            return get().toString();
        }
    }

    private class MethodFunc implements Func {
        private final Method method;

        private MethodFunc(Method method) {
            this.method = method;
        }

        @Override
        public Obj call(Obj ... params) throws InvocationTargetException, IllegalAccessException {
            Object[] arr = Arrays
                    .stream(params)
                    .map(Obj::unwrap)
                    .toArray(Object[]::new);
            return wrap(method.invoke(parent, arr));
        }
    }
}
