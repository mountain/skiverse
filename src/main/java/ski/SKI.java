package ski;

import java.util.HashMap;
import java.util.Map;

interface SKI {

    interface Combinator {
        String script();
        Context stream(int maxdepth, int curdepth);
        Combinator evaluate();
    }

    record Context(String script, Map<String, Combinator> references){};

    class CompositiveCombinator implements Combinator {
        public final Combinator left;
        public final Combinator right;
        private int counter = 0;

        CompositiveCombinator(Combinator left, Combinator right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String script() {
            return String.format("(%s, %s)", left.script(), right.script());
        }

        @Override
        public Context stream(int maxdepth, int curdepth) {
            if (curdepth == 0) {
                counter = 0;
            }
            if (curdepth < maxdepth) {
                Context ctx = stream(maxdepth, curdepth + 1);
            } else {

            }
            return null;
        }

        @Override
        public Combinator evaluate() {
            Context ctx = this.stream(1, 0);
            return switch(ctx.script) {
                case "(I, $1)" -> ctx.references.get("$1").evaluate();
                case "(ι, $1)" -> combine(combine(ctx.references.get("$1").evaluate(), S()), K());
                default  -> {
                    ctx = this.stream(2, 0);
                    yield switch (ctx.script) {
                        case "((K, $1), $2)" -> ctx.references.get("$1").evaluate();
                        default -> {
                            ctx = this.stream(3, 0);
                            yield switch (ctx.script) {
                                case "(((S, $1), $2), $3)" -> {
                                    Combinator $1 = ctx.references.get("$1").evaluate();
                                    Combinator $2 = ctx.references.get("$2").evaluate();
                                    Combinator $3 = ctx.references.get("$3").evaluate();
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
            public Context stream(int maxdepth, int curdepth) {
                return new Context("S", null);
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
            public Context stream(int maxdepth, int curdepth) {
                return new Context("K", null);
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
            public Context stream(int maxdepth, int curdepth) {
                return new Context("I", null);
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
            public Context stream(int maxdepth, int curdepth) {
                return new Context("ι", null);
            }

            @Override
            public Combinator evaluate() {
                return this;
            }
        };
    }

}
