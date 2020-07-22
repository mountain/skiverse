package ski;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ski.SKI.*;

class SKITest {

    @Test
    void testI() {
        Combinator fml = cons(I(), var("x"));
        assertEquals("(I, x)", fml.script());
        assertEquals("x", fml.eval().script());
    }

    @Test
    void testK() {
        Combinator fml = cons(cons(K(), var("x")), var("y")) ;
        assertEquals("((K, x), y)", fml.script());
        assertEquals("x", fml.eval().script());
    }

    @Test
    void testS() {
        Combinator fml = cons(cons(cons(S(), var("x")), var("y")), var("z"));
        assertEquals("(((S, x), y), z)", fml.script());
        assertEquals("((x, z), (y, z))", fml.eval().script());
    }

    @Test
    void testFalse() {
        Combinator fml = cons(cons(cons(S(), K()), var("x")), var("y"));
        assertEquals("(((S, K), x), y)", fml.script());
        assertEquals("y", fml.eval().script());
    }

    @Test
    void testReverse() {
        Combinator reverse = cons(cons(S(), cons(K(), cons(S(), I()))), K());
        Combinator fml = cons(cons(reverse, var("x")), var("y"));
        assertEquals("((((S, (K, (S, I))), K), x), y)", fml.script());
        assertEquals("(y, x)", fml.eval().script());
    }

    @Test
    void testKSKS() {
        Combinator fml = cons(cons(cons(K(), S()), K()), S());
        assertEquals("(((K, S), K), S)", fml.script());
        assertEquals("(S, S)", fml.eval().script());
    }

    @Test
    void testKKKSKS() {
        Combinator fml = cons(cons(cons(cons(cons(K(), K()), K()), S()), K()), S());
        assertEquals("(((((K, K), K), S), K), S)", fml.script());
        assertEquals("(S, S)", fml.eval().script());
    }

    @Test
    void testSKK() {
        Combinator skk = cons(cons(S(), K()), K());
        Combinator fml = cons(cons(skk, var("x")), var("y"));
        assertEquals("((S, K), K)", skk.script());
        assertEquals("(x, y)", fml.eval().script());
    }

    @Test
    void testIota2() {
        Combinator iota2 = cons(iota(), iota());
        Combinator fml = cons(iota2, var("x"));
        assertEquals("(ι, ι)", iota2.script());
        assertEquals("x", fml.eval().script());
    }

    @Test
    void testIota4() {
        Combinator iota4 = cons(iota(), cons(iota(), cons(iota(), iota())));
        Combinator fml = cons(cons(iota4, var("x")), var("y"));
        assertEquals("(ι, (ι, (ι, ι)))", iota4.script());
        assertEquals("x", fml.eval().script());
    }

    @Test
    void testIota5() {
        Combinator iota5 = cons(iota(), cons(iota(), cons(iota(), cons(iota(), iota()))));
        Combinator fml = cons(cons(cons(iota5, var("x")), var("y")), var("z"));
        assertEquals("(ι, (ι, (ι, (ι, ι))))", iota5.script());
        assertEquals("((x, z), (y, z))", fml.eval().script());
    }

    @Test
    void testReplicate() {
        Combinator replicator = cons(cons(S(), cons(K(), var("x"))), cons(cons(S(), I()), I()));
        Combinator fml = cons(replicator, var("y"));
        assertEquals("((S, (K, x)), ((S, I), I))", replicator.script());
        assertEquals("(x, (y, y))", fml.eval().script());
    }

    @Test
    void testSelfApplication() {
        Combinator replicator = cons(cons(S(), cons(K(), var("x"))), cons(cons(S(), I()), I()));
        Combinator selfappl = cons(replicator, replicator);
        //assertEquals("(x, y)", selfappl.eval().script());
    }

}

