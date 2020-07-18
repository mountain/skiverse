package ski;

interface SKI {

    interface Combinator {
        String script();
        Combinator evaluate();
    }

    class CompositiveCombinator implements Combinator {
        public final Combinator left;
        public final Combinator right;

        CompositiveCombinator(Combinator left, Combinator right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String script() {
            return String.format("(%s, %s)", left.script(), right.script());
        }

        @Override
        public Combinator evaluate() {
            return null;
        }
    }

    static Combinator combine(Combinator a, Combinator b) {
        return new CompositiveCombinator(a, b);
    }

}
