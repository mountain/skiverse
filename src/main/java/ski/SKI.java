package ski;

import java.util.HashMap;
import java.util.HashSet;

public interface SKI {

    class Detector extends HashMap<Thread, HashSet<String>> {
        public boolean commit(String script) {
            Thread cur = Thread.currentThread();
            if (!this.containsKey(cur)) {
                this.put(cur, new HashSet<>());
            }
            return this.get(cur).add(script);
        }
    }

    Detector detector = new Detector();

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
            if (left instanceof CompositiveCombinator comp) {
                if (curdepth <= this.maxdepth) {
                    this.visitLeft(comp.left, curdepth + 1);
                    this.visitRoot();
                    this.visitRight(comp.right);
                } else {
                    String key = String.format("$%d", this.counter);
                    this.snippet.append(key);
                    this.put(key, left);
                    this.counter++;
                }
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
            String sa = this.script();
            String sb = val.script();
            double ma = this.mass();
            double mb = val.mass();

            //return val;
            if (ma > mb) {
                return val;
            } else if (ma == mb) {
                if (!sa.equals(sb)) {
                    return val;
                }
            }
            return this;
        }

        @Override
        public Combinator eval() {
            Context ctx = this.tokenize(0);
            String script0 = ctx.script();
            return switch(script0) {
                case "(I, $1)" -> check(ctx.get("$1"));
                case "(ι, $1)" -> check(cons(cons(ctx.get("$1").eval(), S()).eval(), K()).eval());
                default  -> {
                    ctx = this.tokenize(1);
                    String script1 = ctx.script();
                    yield switch (script1) {
                        case "((I, $1), $2)" -> {
                            Combinator $1 = ctx.get("$1");
                            Combinator $2 = ctx.get("$2");
                            if(detector.commit(this.script())) {
                                yield check(cons($1.eval(), $2.eval()).eval());
                            } else {
                                yield check(cons($1.eval(), $2.eval()));
                            }
                        }
                        case "((K, $1), $2)" -> check(ctx.get("$1"));
                        case "((ι, $1), $2)" -> {
                            Combinator $1 = ctx.get("$1");
                            Combinator $2 = ctx.get("$2");
                            yield check(cons(cons(cons($1, S()).eval(), K()).eval(), $2).eval());
                        }
                        default -> {
                            ctx = this.tokenize(2);
                            String script2 = ctx.script();
                            yield switch (script2) {
                                case "(((S, $1), $2), $3)" -> {
                                    Combinator $1 = ctx.get("$1");
                                    Combinator $2 = ctx.get("$2");
                                    Combinator $3 = ctx.get("$3");
                                    if(detector.commit(this.script())) {
                                        yield check(cons(cons($1, $3).eval(), cons($2, $3).eval()).eval());
                                    } else {
                                        yield check(cons(cons($1, $3), cons($2, $3)).eval());
                                    }
                                }
                                case "(((K, $1), $2), $3)" -> {
                                    Combinator $1 = ctx.get("$1");
                                    Combinator $3 = ctx.get("$3");
                                    yield check(cons($1, $3).eval());
                                }
                                case "(((ι, $1), $2), $3)" -> {
                                    Combinator $1 = ctx.get("$1");
                                    Combinator $2 = ctx.get("$2");
                                    Combinator $3 = ctx.get("$3");
                                    yield check(cons(cons(cons(cons($1, S()).eval(), K()).eval(), $2).eval(), $3).eval());
                                }
                                case "(($1, $2), ($3, $4))" -> {
                                    Combinator $1 = ctx.get("$1");
                                    Combinator $2 = ctx.get("$2");
                                    Combinator $3 = ctx.get("$3");
                                    Combinator $4 = ctx.get("$4");
                                    yield check(cons(cons($1, $2).eval(), cons($3, $4).eval()).eval());
                                }
                                case "((($1, $2), $3), $4)" -> {
                                    Combinator $1 = ctx.get("$1").eval();
                                    Combinator $2 = ctx.get("$2");
                                    Combinator $3 = ctx.get("$3");
                                    Combinator $4 = ctx.get("$4");
                                    if(detector.commit(this.script())) {
                                        yield check(cons(cons(cons($1, $2), $3).eval(), $4.eval()).eval());
                                    } else {
                                        yield check(cons(cons(cons($1, $2), $3).eval(), $4.eval()));
                                    }
                                }
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
