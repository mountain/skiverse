package skiverse;

public interface Unit {

    double mass();
    long color();
    String composition();
    Unit eval();

    static Unit composite(Unit a, Unit b) {
        return new Molecular(a, b);
    }

    class Molecular implements Unit {

        public final Unit left;
        public final Unit right;

        Molecular(Unit left, Unit right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public double mass() {
            return left.mass() + right.mass();
        }

        @Override
        public long color() {
            return left.color() + right.color();
        }

        @Override
        public String composition() {
            return String.format("(%s, %s)", left.composition(), right.composition());
        }

        @Override
        public Unit eval() {
            Unit val = this;

            if (this.left.composition().equals('I')) {
                val = this.right.eval();
            } else if (this.left instanceof Molecular) {
                Molecular subterm = (Molecular)this.left;
                if(subterm.left.composition().equals('K')) {
                    val = subterm.right.eval();
                } else if (subterm.left instanceof Molecular) {
                    Molecular subsubterm = (Molecular)subterm.left;
                    if(subterm.left.composition().equals('K')) {
                        return subterm.right;
                    }
                }
            }

            return val;
        }
    }

    class S implements Unit {
        @Override
        public double mass() {
            return 1;
        }

        @Override
        public long color() {
            return 0x010000;
        }

        @Override
        public String composition() {
            return "S";
        }

        @Override
        public Unit eval() {
            return this;
        }

    }

    class K implements Unit {
        @Override
        public double mass() {
            return 1;
        }

        @Override
        public long color() {
            return 0x000100;
        }

        @Override
        public String composition() {
            return "K";
        }

        @Override
        public Unit eval() {
            return this;
        }

    }

    class I implements Unit {
        @Override
        public double mass() {
            return 4;
        }

        @Override
        public long color() {
            return 0x000001;
        }

        @Override
        public String composition() {
            return "I";
        }

        @Override
        public Unit eval() {
            return this;
        }

    }

}
