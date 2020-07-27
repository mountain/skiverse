package narr.random;

import clojure.lang.IPersistentVector;
import clojure.lang.Tuple;

public class Generators {

    @FunctionalInterface
    public interface DoubleGenerator {
        double generate();
    }

    @FunctionalInterface
    public interface BooleanGenerator {
        boolean generate();
    }

    public static DoubleGenerator uniform_01 = () -> Math.random();
    public static DoubleGenerator uniform_n1p1 = () -> 2 * Math.random() - 1;
    public static BooleanGenerator uniform_boolean = () -> Math.random() >=0 ? true: false;

    private IPersistentVector uniformSpherical() {
        double theta = 2 * Math.PI * uniform_01.generate();
        double phi = Math.acos(1 - 2 * uniform_01.generate());
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        return Tuple.create(x, y, z);
    }

}
