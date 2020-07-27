package narr;

public class NA {

    public static Array array(int... shape) {
        return new Array(shape);
    }

    public static Array zeros(int... shape) {
        return new Array(shape);
    }

    public static Array ones(int... shape) {
        Array array = new Array(shape);
        array.values.fill(1);
        return array;
    }

}
