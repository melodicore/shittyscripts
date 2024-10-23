## Dynamica

Have you ever thought that Java would be better if it was more like Javascript? Me neither, but I did it anyway. 
Introducing Dynamica, dynamic typing in Java.

### Obj

It all starts with the main class of the project, Obj, which represents every object ever created. You can create an Obj
very simply, like this:

```java
Obj obj = Obj.create();
```

Initially this object does not really do anything, but that's because it doesn't have any fields or functions. Fields
can be added like this:

```java
obj.set("fieldName", obj);
```

You may wonder why it only takes in the Obj type. That is because Obj represents every type. We will get to that later.

Retrieving fields is just as easy:

```java
Obj retrieved = obj.get("fieldName");
```

Functions work in a similar way, but use the Func functional interface to represent functions. Funcy! You can register a
function like this:

```java
obj.register("functionName", (aThis, params) -> Obj.create());
```

And you can call functions like this:

```java
obj.call("functionName", obj, retrieved);
```

Functions also only take in the Obj type as parameters. This is fine and all, but how would you ever do anything with a 
type that can only refer to itself? Here's the magic part:

```java
Obj fromString = Obj.of("This is a string");
```

Now this new object is backed by a native Java object, in this case a String. When you do this, the Obj goes through all
fields and methods of the native object using reflection, and registers these as fields and functions in the Obj. This
includes all static and private fields and methods, including all superclasses. Any retrieved native object will be 
wrapped to an Obj, and any Obj given as parameters that has a wrapped object will have that wrapped object given as a
parameter to the native field or method.

```java
Obj sub = fromString.call("substring", Obj.of(5));
System.out.println(sub);
```

This will make the console will print out `is a string`.

At this point you may wonder how does this work with methods that have the same name but different amounts of 
parameters. In actuality, the Obj can store an infinite amount of functions with the same name. When a function is 
called, if that function throws any kind of exception, the next function in the list will be called. An exception is 
only thrown to the user if none of the functions work.

```java
Obj otherSub = fromString.call("substring", Obj.of(0), Obj.of(7));
System.out.println(otherSub);
```

This will make the console print out `This is`.

Native methods with the same name are added to the end of the function list in the order they appear, the given class 
coming before its superclasses. Manually adding a function will be added to the front of the list so behavior can be
overridden.

```java
fromString.register("substring", (aThis, params) -> {
    if(params.length == 1) {
        return Obj.of("Oh bother");
    }
    throw new RuntimeException();
});
sub = fromString.call("substring", Obj.of(5));
otherSub = fromString.call("substring", Obj.of(0), Obj.of(7));
System.out.println(sub);
System.out.println(otherSub);
```

This will make the console print out:

```
Oh bother
This is
```

The three main native methods in Java, `hashCode`, `equals` and `toString` all have their equivalents in Obj. If the Obj
has a registered function that takes in the proper arguments and returns the proper type for these methods, the result
of that function will be returned, unwrapped because these methods cannot return Obj. If no function is present, 
`hashCode` and `toString` will return the string representation of the fields registered to the Obj, and equals uses the
static method `Obj.equals(Object, Object)`.

I hope you enjoy Java with these new freedoms!