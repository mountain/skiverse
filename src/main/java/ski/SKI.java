package ski;

import java.util.HashMap;

interface SKI {

    interface Combinator {
        String script();
        Context stream(int maxdepth);
        Combinator eval();
    }

    class Context extends HashMap<String, Combinator> {
        int counter;
        int maxdepth;
        StringBuffer snippet;

        Context(int maxdepth) {
            this.counter = 1;
            this.snippet = new StringBuffer();
            this.maxdepth = maxdepth;
        }

        public String script() {
            return this.snippet.toString();
        }

        public void visitLeaf(Combinator leaf) {
            this.snippet.append(leaf.script());
        }

        public void visitLeft(Combinator left, int curdepth) {
            this.snippet.append("(");
            if (curdepth <= this.maxdepth && left instanceof CompositiveCombinator comp) {
                this.visitLeft(comp.left, curdepth + 1);
                this.visitRoot();
                this.visitRight(comp.right);
            } else {
                this.snippet.append(left.script());
            }
        }

        public void visitRoot() {
            this.snippet.append(",");
            this.snippet.append(" ");
        }

        public void visitRight(Combinator right) {
            String key = String.format("$%d", this.counter);
            this.snippet.append(key);
            this.snippet.append(")");
            this.put(key, right);
            this.counter++;
        }

    }

    class CompositiveCombinator implements Combinator {
        public final Combinator left;
        public final Combinator right;
        private int counter = 1;

        CompositiveCombinator(Combinator left, Combinator right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String script() {
            return String.format("(%s, %s)", left.script(), right.script());
        }

        @Override
        public Context stream(int maxdepth) {
            Context ctx = new Context(maxdepth);
            ctx.visitLeft(left, 1);
            ctx.visitRoot();
            ctx.visitRight(right);
            return ctx;
        }

        @Override
        public Combinator eval() {
            Context ctx = this.stream(1);
            return switch(ctx.script()) {
                case "(I, $1)" -> ctx.get("$1").eval();
                case "(ι, $1)" -> cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval();
                case "((ι, $1), $2)" -> cons(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval(), ctx.get("$2").eval()).eval();
                default  -> {
                    ctx = this.stream(2);
                    yield switch (ctx.script()) {
                        case "((K, $1), $2)" -> ctx.get("$1").eval();
                        case "(((K, $1), $2), $3)" -> cons(ctx.get("$1").eval(), ctx.get("$3").eval()).eval();
                        default -> {
                            ctx = this.stream(3);
                            yield switch (ctx.script()) {
                                case "(((S, $1), $2), $3)" -> {
                                    Combinator $1 = ctx.get("$1").eval();
                                    Combinator $2 = ctx.get("$2").eval();
                                    Combinator $3 = ctx.get("$3").eval();
                                    yield cons(cons($1, $3).eval(), cons($2, $3).eval()).eval();
                                }
                                case "((((S, $1), $2), $3), $4)" -> {
                                    Combinator $1 = ctx.get("$1").eval();
                                    Combinator $2 = ctx.get("$2").eval();
                                    Combinator $3 = ctx.get("$3").eval();
                                    Combinator $4 = ctx.get("$4").eval();
                                    yield cons(cons(cons($1, $3).eval(), cons($2, $3).eval()).eval(), $4).eval();
                                }
                                case "(((((K, K), $1), $2), $3), $4)" -> cons(ctx.get("$2").eval(), ctx.get("$4").eval()).eval();
                                case "(((ι, $1), $2), $3)" -> cons(cons(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval(), ctx.get("$2").eval()).eval(), ctx.get("$3").eval()).eval();
                                case "((((ι, $1), $2), $3), $4)" -> cons(cons(cons(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval(), ctx.get("$2").eval()).eval(), ctx.get("$3").eval()).eval(), ctx.get("$4").eval()).eval();
                                default -> this;
                            };
                        }
                    };
                }
            };
        }
    }

    static Combinator cons(Combinator a, Combinator b) {
        return new CompositiveCombinator(a, b);
    }

    static Combinator var(String name) {
        return new Combinator() {

            @Override
            public String script() {
                return name;
            }

            @Override
            public Context stream(int maxdepth) {
                return null;
            }

            @Override
            public Combinator eval() {
                return this;
            }

        };
    }

    static Combinator S() {
        return new Combinator() {

            @Override
            public String script() {
                return "S";
            }

            @Override
            public Context stream(int maxdepth) {
                return null;
            }

            @Override
            public Combinator eval() {
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
            public Context stream(int maxdepth) {
                return null;
            }

            @Override
            public Combinator eval() {
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
            public Context stream(int maxdepth) {
                return null;
            }

            @Override
            public Combinator eval() {
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
            public Context stream(int maxdepth) {
                return null;
            }

            @Override
            public Combinator eval() {
                return this;
            }

        };
    }

}
