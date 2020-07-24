package ski;

import java.util.HashMap;

public interface SKI {

    interface Combinator {
        double mass();
        String script();
        Context tokenize(int maxdepth);
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
                if (left instanceof CompositiveCombinator comp) {

                } else {
                    this.snippet.append(left.script());
                }
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
        double enrg = 0;

        CompositiveCombinator(Combinator left, Combinator right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double mass() {
            return left.mass() + right.mass();
        }

        @Override
        public String script() {
            return String.format("(%s, %s)", left.script(), right.script());
        }

        @Override
        public Context tokenize(int maxdepth) {
            Context ctx = new Context(maxdepth);
            ctx.visitLeft(left, 1);
            ctx.visitRoot();
            ctx.visitRight(right);
            return ctx;
        }

        protected Combinator check(Combinator val) {
            String a = this.script();
            String b = val.script();
            double ma = this.mass();
            double mb = val.mass();

            if (this.mass() >= val.mass()) {
                return val;
            } else {
                return this;
            }
        }

        @Override
        public Combinator eval() {
            Combinator val;
            Context ctx = this.tokenize(1);
            String script1 = ctx.script();
            return switch(script1) {
                case "(I, $1)" -> check(ctx.get("$1").eval());
                case "(ι, $1)" -> check(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval());
                //case "((ι, $1), $2)" -> check(cons(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval(), ctx.get("$2").eval()).eval());
                default  -> {
                    ctx = this.tokenize(2);
                    String script2 = ctx.script();
                    yield switch (script2) {
                        case "((K, $1), $2)" -> check(ctx.get("$1").eval());
                        //case "(((K, $1), $2), $3)" -> check(cons(ctx.get("$1").eval(), ctx.get("$3").eval()).eval());
                        default -> {
                            ctx = this.tokenize(3);
                            String script3 = ctx.script();
                            yield switch (script3) {
                                case "(((S, $1), $2), $3)" -> {
                                    Combinator $1 = ctx.get("$1").eval();
                                    Combinator $2 = ctx.get("$2").eval();
                                    Combinator $3 = ctx.get("$3").eval();
                                    yield check(cons(cons($1, $3).eval(), cons($2, $3).eval()).eval());
                                }
                                //case "((((S, $1), $2), $3), $4)" -> {
                                //    Combinator $1 = ctx.get("$1").eval();
                                //    Combinator $2 = ctx.get("$2").eval();
                                //    Combinator $3 = ctx.get("$3").eval();
                                //    Combinator $4 = ctx.get("$4").eval();
                                //    yield check(cons(cons(cons($1, $3).eval(), cons($2, $3).eval()).eval(), $4).eval());
                                //}
                                //case "(((((K, K), $1), $2), $3), $4)" -> check(cons(ctx.get("$2").eval(), ctx.get("$4").eval()).eval());
                                //case "(((ι, $1), $2), $3)" -> check(cons(cons(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval(), ctx.get("$2").eval()).eval(), ctx.get("$3").eval()).eval());
                                //case "((((ι, $1), $2), $3), $4)" -> check(cons(cons(cons(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval(), ctx.get("$2").eval()).eval(), ctx.get("$3").eval()).eval(), ctx.get("$4").eval()).eval());
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
            double enrg = 0;

            @Override
            public double mass() {
                return 0;
            }

            @Override
            public String script() {
                return name;
            }

            @Override
            public Context tokenize(int maxdepth) {
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
            double enrg = 0;

            @Override
            public double mass() {
                return 5;
            }

            @Override
            public String script() {
                return "S";
            }

            @Override
            public Context tokenize(int maxdepth) {
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
            double enrg = 0;

            @Override
            public double mass() {
                return 4;
            }

            @Override
            public String script() {
                return "K";
            }

            @Override
            public Context tokenize(int maxdepth) {
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
            double enrg = 0;

            @Override
            public double mass() {
                return 2;
            }

            @Override
            public String script() {
                return "I";
            }

            @Override
            public Context tokenize(int maxdepth) {
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
            double enrg = 0;

            @Override
            public double mass() {
                return 1;
            }

            @Override
            public String script() {
                return "ι";
            }

            @Override
            public Context tokenize(int maxdepth) {
                return null;
            }

            @Override
            public Combinator eval() {
                return this;
            }

        };
    }

}
