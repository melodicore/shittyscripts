package me.datafox.dynamica;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
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

    public Obj getFieldNames() {
        return wrap(fields.keySet());
    }

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

    public Obj getFuncNames() {
        return wrap(funcs.keySet());
    }

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

    public static Obj create() {
        return new Obj(null);
    }

    public static Obj of(Object o) {
        if(o instanceof Obj obj) {
            return obj;
        }
        return new Obj(o);
    }

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

        public FakeFieldWrapper(Object object) {
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
