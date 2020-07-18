package skiverse;

public interface Term {

    double mass();
    long color();
    String string();

    class S implements Term {
        @Override
        public double mass() {
            return 1;
        }

        @Override
        public long color() {
            return 0x010000;
        }

        @Override
        public String string() {
            return "S";
        }

    }

    class K implements Term {
        @Override
        public double mass() {
            return 1;
        }

        @Override
        public long color() {
            return 0x010000;
        }

        @Override
        public String string() {
            return "K";
        }

    }

    class I implements Term {
        @Override
        public double mass() {
            return 4;
        }

        @Override
        public long color() {
            return 0x010000;
        }

        @Override
        public String string() {
            return "I";
        }

    }

}
