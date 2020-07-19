package ski;

import java.util.HashMap;
import java.util.Map;

interface SKI {

    interface Combinator {
        String script();
        Context stream(int depth);
        Combinator evaluate();
    }

    record Context(String script, Map<String, Combinator> references){};

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
        public Context stream(int depth) {
            return null;
        }

        @Override
        public Combinator evaluate() {
            Context ctx = this.stream(1);
            return switch(ctx.script) {
                case "(I, $1)" -> ctx.references.get("$1").evaluate();
                default  -> {
                    ctx = this.stream(2);
                    yield switch (ctx.script) {
                        case "((K, $1), $2)" -> ctx.references.get("$1").evaluate();
                        default -> {
                            ctx = this.stream(3);
                            yield switch (ctx.script) {
                                case "(((S, $1), $2), $3)" -> {
                                    Combinator $1 = ctx.references.get("$1");
                                    Combinator $2 = ctx.references.get("$2");
                                    Combinator $3 = ctx.references.get("$3");
                                    yield combine(combine($1, $3), combine($2, $3));
                                }
                                default -> this;
                            };
                        }
                    };
                }
            };
        }
    }

    static Combinator combine(Combinator a, Combinator b) {
        return new CompositiveCombinator(a, b);
    }

    static Combinator S() {
        return new Combinator() {

            @Override
            public String script() {
                return "S";
            }

            @Override
            public Context stream(int depth) {
                return null;
            }

            @Override
            public Combinator evaluate() {
                return this;
            }
        };
    }

    static Combinator K() {
        return new Combinator() {

            @Override
            public String script() {
                return "K";
            }

            @Override
            public Context stream(int depth) {
                return null;
            }

            @Override
            public Combinator evaluate() {
                return this;
            }
        };
    }

    static Combinator I() {
        return new Combinator() {

            @Override
            public String script() {
                return "I";
            }

            @Override
            public Context stream(int depth) {
                return null;
            }

            @Override
            public Combinator evaluate() {
                return this;
            }
        };
    }

    static Combinator iota() {
        return new Combinator() {

            @Override
            public String script() {
                return "ι";
            }

            @Override
            public Context stream(int depth) {
                return null;
            }

            @Override
            public Combinator evaluate() {
                return this;
            }
        };
    }

}
