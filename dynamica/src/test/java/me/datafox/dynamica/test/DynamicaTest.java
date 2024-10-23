package me.datafox.dynamica.test;

import me.datafox.dynamica.Obj;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author datafox
 */
public class DynamicaTest {
    @Test
    public void objTest() {
        Obj obj = Obj.of(new ArrayList<>());
        System.out.println(obj.call("toString"));
    }
}
