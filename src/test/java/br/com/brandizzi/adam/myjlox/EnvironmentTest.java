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
    public void testDefineIndex() {
        Environment environment = new Environment();
        
        Assert.assertEquals(0, environment.define("a", "1"));
        Assert.assertEquals(1, environment.define("b", "12"));
        Assert.assertEquals(2, environment.define("c", "123"));
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
    
    @Test
    public void testDefineAndGet() {
        Environment environment = new Environment();
        
        int index1 = environment.define("a", "1");
        Assert.assertEquals("1", environment.get(index1));
        int index2 = environment.define("b", "12");
        Assert.assertEquals("12", environment.get(index2));
        int index3 = environment.define("c", "123");
        Assert.assertEquals("123", environment.get(index3));
    }

    @Test
    public void testDefineAndGetAt() {
        Environment ancestor = new Environment();
        Environment environment = new Environment(ancestor);
        
        int index1 = ancestor.define("a", "1");
        Assert.assertEquals("1", environment.getAt(1, index1));
        int index2 = ancestor.define("aa", "1");
        Assert.assertEquals("1", environment.getAt(1, index2));
        int index3 = environment.define("b", "12");
        Assert.assertEquals("12", environment.getAt(0, index3));
        int index4 = environment.define("c", "123");
        Assert.assertEquals("123", environment.getAt(0, index4));
    }
    
    @Test
    public void testDefineAndAssign() {
        Environment environment = new Environment();
        
        int index1 = environment.define("a", "1");
        environment.assign(index1, "2");
        Assert.assertEquals("2", environment.get(index1));
    }


    @Test
    public void testDefineAndAssignAt() {
        Environment ancestor = new Environment();
        Environment environment = new Environment(ancestor);
        
        int index1 = ancestor.define("a", "1");
        int index2 = environment.define("aa", "1");
        
        environment.assignAt(0, index1, "e");
        environment.assignAt(1, index1, "a");
        
        Assert.assertEquals(environment.getAt(0, index1), "e");
        Assert.assertEquals(environment.getAt(1, index2), "a");


    }
    
    private Token variable(String varName) {
        return new Token(TokenType.IDENTIFIER, varName, varName, 1);
    }

}
