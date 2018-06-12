package br.com.brandizzi.adam.myjlox;

import org.junit.Test;

import org.junit.Assert;

public class EnvironmentTest {
    
    @Test
    public void testDefine() {
        Environment environment = new Environment();
        
        environment.define("a", 1);
        Assert.assertEquals(1, environment.get(variable("a")));
        
    }

    @Test
    public void testAssign() {
        Environment environment = new Environment();
        
        environment.define("a", null);
        environment.assign(variable("a"), 2);
        Assert.assertEquals(2, environment.get(variable("a")));
        
    }

    @Test(expected = RuntimeError.class)
    public void testAssignBeforeDefiningError() {
        Environment environment = new Environment();
        
        environment.assign(variable("a"), 2);
    }

    @Test(expected = RuntimeError.class)
    public void testGetBeforeDefiningError() {
        Environment environment = new Environment();
        
        environment.get(variable("a"));
    }

    @Test
    public void testDefineInAncestor() {
        Environment ancestor = new Environment();
        Environment environment = new Environment(ancestor);
        
        ancestor.define("a", 1);
        Assert.assertEquals(1, environment.get(variable("a")));
    }


    @Test
    public void testAssignInAncestor() {
        Environment ancestor = new Environment();
        Environment environment = new Environment(ancestor);
        
        ancestor.define("a", 1);
        environment.assign(variable("a"), 2);
        Assert.assertEquals(2, ancestor.get(variable("a")));
    }

    @Test
    public void testShadowAncestor() {
        Environment ancestor = new Environment();
        Environment environment = new Environment(ancestor);
        
        ancestor.define("a", 1);
        environment.define("a", 2);
        Assert.assertEquals(1, ancestor.get(variable("a")));
        Assert.assertEquals(2, environment.get(variable("a")));
    }

    @Test
    public void testAssignAt() {
        Environment ancestor = new Environment();
        Environment environment = new Environment(ancestor);
        
        ancestor.define("a", 1);
        environment.define("a", 2);
        environment.assignAt(1, variable("a"), 3);
        Assert.assertEquals(3, ancestor.get(variable("a")));
        Assert.assertEquals(2, environment.get(variable("a")));
    }

    @Test
    public void testGetAt() {
        Environment ancestor = new Environment();
        Environment environment = new Environment(ancestor);
        
        ancestor.define("a", 1);
        environment.define("a", 2);
        Assert.assertEquals(1, environment.getAt(1, "a"));
        Assert.assertEquals(2, environment.getAt(0, "a"));
    }

    private Token variable(String varName) {
        return new Token(TokenType.IDENTIFIER, varName, varName, 1);
    }

}
