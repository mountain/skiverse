package skiverse;

public interface Atom {

    double mass();
    long color();
    String composition();

    static Atom composite(Atom a, Atom b) {
        return new Atom(){
            public final Atom left = a;
            public final Atom right = b;

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
        };
    }

    class S implements Atom {
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

    }

    class K implements Atom {
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

    }

    class I implements Atom {
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

    }

}
