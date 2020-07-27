package narr;

import mikera.arrayz.INDArray;
import mikera.vectorz.Op;
import narr.random.Generators;

import java.util.Iterator;

import static mikera.arrayz.Arrayz.newArray;

public class Array {

    @FunctionalInterface
    public interface Operator {
        void apply(int i, Array componient);
    }

    protected INDArray values;

    public Array(int... shape) {
        this.values = newArray(shape);
    }

    public Array(INDArray array) {
        this.values = array;
    }

    public double element(int... indexes) {
        return this.values.get(indexes);
    }

    public double norm() {
        double lengthsq = 0.0;
        for (Iterator<Double> it = this.values.elementIterator(); it.hasNext(); ) {
            double x = it.next();
            lengthsq += x * x;
        }
        return Math.sqrt(lengthsq);
    }

    public double sum() {
        return this.values.elementSum();
    }

    public double mean() {
        return this.values.elementSum() / this.values.elementCount();
    }

    public Array fill(Generators.DoubleGenerator g) {
        this.values.applyOp(new Op() {
            @Override
            public double apply(double x) {
                return g.generate();
            }
        });
        return this;
    }

    public Array set(int pos, double val) {
        this.values.set(pos, val);
        return this;
    }

    public Array set(int pos1, int pos2, double val) {
        this.values.set(pos1, pos2, val);
        return this;
    }

    public Array scale(double factor) {
        this.values.scale(factor);
        return this;
    }

    public Array along(int dim, Operator op) {
        int[] shape = this.values.getShape();
        for (int i = 0; i < shape[dim]; i++) {
            INDArray slice = this.values.slice(dim, i);
            op.apply(i, new Array(slice));
        }

        return this;
    }

}
