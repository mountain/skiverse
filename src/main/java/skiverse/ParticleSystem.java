package skiverse;

import mikera.arrayz.INDArray;
import mikera.vectorz.AVector;
import mikera.vectorz.Vector3;
import org.joml.AABBf;
import pl.pateman.dynamicaabbtree.AABBTree;
import pl.pateman.dynamicaabbtree.Boundable;
import pl.pateman.dynamicaabbtree.CollisionPair;
import pl.pateman.dynamicaabbtree.Identifiable;
import ski.SKI;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static mikera.arrayz.Arrayz.fillRandom;
import static mikera.arrayz.Arrayz.newArray;


public class ParticleSystem {

    static double width = 64;
    static int numOfParticles = 8192;
    static double injectRate = 128.0;
    static double escapeProb = 0.80;
    static double collisionThreshhold = 0.1;
    static double avergespeed = 0.0;

    static INDArray flag = newArray(numOfParticles);
    static INDArray mass = newArray(numOfParticles);
    static INDArray pos = newArray(numOfParticles, 3);
    static INDArray velo = newArray(numOfParticles, 3);
    static INDArray potn = newArray(numOfParticles);
    static INDArray knct = newArray(numOfParticles);
    static INDArray mmnt = newArray(numOfParticles, 3);

    static AABBTree<Entity> tree = new AABBTree<Entity>();
    static List<CollisionPair<Entity>> collisionPairs = new ArrayList<CollisionPair<Entity>>(numOfParticles / 10);

    static Map<Integer, Emission> emissionQueues = new HashMap<>();

    static List<SKI.Combinator> combinators = new ArrayList<SKI.Combinator>(numOfParticles);
    static Map<String, Integer> countMap = new HashMap<String, Integer>();

    static double t = 0;
    static double lastt = 0;
    static int counter = 0;

    static double mindt = 1.0;
    static double mindist = Double.MAX_VALUE;
    static int icollision = -1;
    static int jcollision = -1;

    static class Entity implements Identifiable, Boundable {
        public int idx;

        public Entity(int idx) {
            this.idx = idx;
        }

        @Override
        public AABBf getAABB(AABBf dest) {
            if (dest == null) {
                dest = new AABBf();
            }

            double thresh = collisionThreshhold / avergespeed;
            INDArray p = pos.slice(0, this.idx);
            INDArray v = velo.slice(0, this.idx);
            dest.setMin((float)p.get(0), (float)p.get(1), (float)p.get(2));
            dest.setMax((float)(p.get(0) + v.get(0) * thresh), (float)(p.get(1) + v.get(1) * thresh), (float)(p.get(2) + v.get(2) * thresh));
            return dest;
        }

        @Override
        public long getID() {
            return this.idx;
        }
    }

    public static void init() {
        fillRandom(flag, new Date().getTime());
        flag.multiply(2);
        flag.sub(1);

        fillRandom(pos, new Date().getTime());
        fillRandom(velo, new Date().getTime());
        pos.multiply(width);
        velo.multiply(2);
        velo.sub(1);
        velo.scale(0.7);

        for (int i = 0; i < numOfParticles; i++) {
            combinators.add(null);
        }

        for (int i = 0; i < numOfParticles; i++) {
            if (flag.get(i) < 0) {
                mass.set(i, 0.0);
                potn.set(i, 0.0);
                knct.set(i, 0.0);
                mmnt.slice(0, i).scale(0);
            } else {
                switch (i % 3) {
                    case 0: combinators.set(i, SKI.I()); break;
                    case 1: combinators.set(i, SKI.K()); break;
                    case 2: combinators.set(i, SKI.S()); break;
                }

                Vector3 ivelo = Vector3.create(velo.slice(0, i));
                double vsq = ivelo.dotProduct(ivelo);
                double m = combinators.get(i).mass();
                mass.set(i, m);
                potn.set(i, 0.0);
                knct.set(i, m * vsq / 2);
                mmnt.slice(0, i).set(ivelo.scaleCopy(m));
            }
        }
    }

    public static void refreshTree() {
        tree.clear();
        collisionPairs.clear();
        for(int i = 0; i < numOfParticles; i++) {
            if(flag.get(i) > 0.0) {
                tree.add(new Entity(i));
            }
        }
        tree.detectCollisionPairs(collisionPairs);

        avergespeed = Math.sqrt(knct.elementSum() / mass.elementSum() * 2);
    }

    public static void addIota(Vector3 position) {
        int i = 0;
        for (; i < numOfParticles; i++) {
            if (flag.get(i) < 0) break;
        }
        if (i >= numOfParticles) {
            return;
        }

        INDArray apos = pos.slice(0, i);
        if (position == null) {
            fillRandom(apos, new Date().getTime());
            apos.multiply(width);
        } else {
            apos.set(position);
        }

        Vector3 avelo = Vector3.create(velo.slice(0, i));
        fillRandom(avelo, new Date().getTime());
        avelo.multiply(2);
        avelo.sub(1);
        double length = Math.sqrt(avelo.dotProduct(avelo));
        avelo.scale(1.0 / length);

        combinators.set(i, SKI.iota());
        double vsq = avelo.dotProduct(avelo);
        double m = combinators.get(i).mass();
        mass.set(i, m);
        knct.set(i, m * vsq / 2);
        mmnt.slice(0, i).set(avelo.scaleCopy(m));

        if (position == null) {
            potn.set(i, 0.0);
        } else {
            potn.set(i, -m * vsq / 2);
        }

        flag.set(i, 0.0);
    }

    public static void addIota(int origin, Vector3 position, Vector3 velocity) {
        if (flag.get(origin) < 0) return;
        SKI.Combinator o = combinators.get(origin);
        if (o == null) return;
        double mo = o.mass();

        int i = 0;
        for (; i < numOfParticles; i++) {
            if (flag.get(i) < 0) break;
        }
        if (i >= numOfParticles) {
            return;
        }

        INDArray apos = pos.slice(0, i);
        apos.set(position);

        combinators.set(i, SKI.iota());
        double vsq = velocity.dotProduct(velocity);
        double m = combinators.get(i).mass();
        AVector p = velocity.scaleCopy(m);

        mass.set(i, m);
        knct.set(i, m * vsq / 2);
        mmnt.slice(0, i).set(p);
        potn.set(i, 0.0);

        mmnt.slice(0, origin).sub(p);
        velo.slice(0, origin).set(Vector3.create(mmnt.slice(0, origin)).scaleCopy(1.0 / m));
        Vector3 v = Vector3.create(velo.slice(0, origin));
        knct.set(origin, v.dotProduct(v) / 2.0 * mo);
        potn.set(origin, potn.get(origin) - m * vsq / 2);

        flag.set(i, 0.0);
    }

    public static void freefly(double mdt) {
        for(int m = 0; m < numOfParticles; m++) {
            for (int n = 0; n < 3; n++) {
                if (flag.get(m) > 0.0) {
                    pos.set(m, n, (pos.get(m, n) + velo.get(m, n) * mdt + width) % width);
                }
            }
            if (flag.get(m) < collisionThreshhold) {
                Vector3 v = Vector3.create(velo.slice(0, m));
                v.scaleCopy(mindt);
                double ds = Math.sqrt(v.dotProduct(v));
                flag.set(m, flag.get(m) + ds);
            }
        }
    }

    public static void checkDeltaT() {
        mindt = 1.0;
        for(CollisionPair p: collisionPairs) {
            int i = ((Entity)p.getObjectA()).idx;
            int j = ((Entity)p.getObjectB()).idx;

            if (i != j) {
                Vector3 dpos = Vector3.create(pos.slice(0, i));
                dpos.sub(Vector3.create(pos.slice(0, j)));

                Vector3 dvelo = Vector3.create(velo.slice(0, i));
                dvelo.sub(Vector3.create(velo.slice(0, j)));

                double c1 = 2 * dpos.dotProduct(dvelo);
                double c2 = dvelo.dotProduct(dvelo);

                double dt = c1 / 2 / c2;
                if (dt > 0 && dt < mindt) {
                    mindt = dt;
                }
            }
        }
    }

    public static void checkCollision() {
        mindist = width;
        for(CollisionPair p: collisionPairs) {
            int i = ((Entity) p.getObjectA()).idx;
            int j = ((Entity) p.getObjectB()).idx;

            if (i != j) {
                Vector3 dpos = Vector3.create(pos.slice(0, i));
                dpos.sub(Vector3.create(pos.slice(0, j)));

                double c0 = dpos.dotProduct(dpos);
                if (c0 < mindist * mindist) {
                    mindist = Math.sqrt(c0);
                    icollision = i;
                    jcollision = j;
                }
            }
        }
    }

    public static void collisionMerge() {
        SKI.Combinator left = combinators.get(icollision);
        SKI.Combinator right = combinators.get(jcollision);

        if (left == null || right == null || flag.get(icollision) < 0.0 || flag.get(jcollision) < 0.0) {
            return;
        }

        flag.set(icollision, -1);
        flag.set(jcollision, -1);

        int i = 0;
        for (; i<numOfParticles; i++) {
            if (flag.get(i) < 0) break;
        }
        if (i >= numOfParticles) {
            return;
        }

        SKI.Combinator result = SKI.cons(left, right).eval();
        combinators.set(i, result);

        double lmass = mass.get(icollision);
        double rmass = mass.get(jcollision);
        double lknct = knct.get(icollision);
        double rknct = knct.get(jcollision);
        double lpotn = potn.get(icollision);
        double rpotn = potn.get(jcollision);

        double zmass = result.mass();
        Vector3 zmmnt = Vector3.create(mmnt.slice(0, icollision).addCopy(mmnt.slice(0, jcollision)));
        AVector zvelo = zmmnt.scaleCopy(1.0 / zmass);
        double zvsq = zvelo.dotProduct(zvelo);
        double zknct = zmass * zvsq / 2;
        double zpotn = potn.get(icollision) + potn.get(jcollision) + knct.get(icollision) + knct.get(jcollision) - zknct;

        Vector3 ipos = Vector3.create(pos.slice(0, icollision));
        Vector3 jpos = Vector3.create(pos.slice(0, jcollision));
        Vector3 zpos = ipos.addCopy(jpos);
        zpos.scale(0.5);

        zpotn = lmass + rmass - zmass + zpotn;
        if (zpotn > 0) {
            emissionQueues.put(i, new Emission(i));
        }

        combinators.set(i, result);
        mass.set(i, zmass);
        knct.set(i, zknct);
        potn.set(i, zpotn);
        pos.slice(0, i).set(zpos);
        velo.slice(0, i).set(zvelo);
        mmnt.slice(0, i).set(zvelo.scaleCopy(zmass));

        flag.set(i, 1.0);

        System.out.println("time %f".formatted(t));
        System.out.println("collision %d: %d, %d".formatted(counter, icollision, jcollision));
        System.out.println("script %d: %s, %s -> %s".formatted(counter, left.script(), right.script(), result.script()));
        System.out.println("------------------------------------------------------------------");
        System.out.println("mass %d: %f, %f -> %f".formatted(counter, left.mass(), right.mass(), result.mass()));
        System.out.println("kinect %d: %f, %f -> %f".formatted(counter, lknct, rknct, zknct));
        System.out.println("potential %d: %f, %f -> %f".formatted(counter, lpotn, rpotn, zpotn));
        System.out.println("------------------------------------------------------------------");
        System.out.println("time %f".formatted(t));
        System.out.println("total mass %d: %f".formatted(counter, mass.elementSum()));
        System.out.println("total kinect %d: %f".formatted(counter, knct.elementSum()));
        System.out.println("total potential %d: %f".formatted(counter, potn.elementSum()));
        System.out.println("------------------------------------------------------------------");
        System.out.println("time %f".formatted(t));
        System.out.println("particles %d".formatted(countMap.values().stream().reduce(0, new BinaryOperator() {
            @Override
            public Object apply(Object o, Object o2) {
                return ((Integer)o).intValue() + ((Integer)o2).intValue() ;
            }
        }).intValue()));
        System.out.println("diversity %d".formatted(countMap.size()));
        System.out.println("------------------------------------------------------------------");
        System.out.println("time %f".formatted(t));
        countMap.clear();
        for (int l = 0; l < numOfParticles; l++) {
            if (flag.get(l) >= 0.0 && combinators.get(l) != null) {
                String key = combinators.get(l).script();
                if (!countMap.containsKey(key)) {
                    countMap.put(key, 1);
                } else {
                    countMap.put(key, countMap.get(key) + 1);
                }
            }
        }
        LinkedHashMap<String, Integer> sortedMap = countMap.entrySet().stream().
                        sorted(Entry.comparingByValue()).
                        collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        for(Entry<String, Integer> entry: sortedMap.entrySet()) {
            System.out.println(String.format("%04d: %s", entry.getValue(), entry.getKey()));
        }
        System.out.println("------------------------------------------------------------------");
    }

    public static class Emission {
        private int index;
        public Emission(int index) {
            this.index = index;
        }
        void emit(double dt) {
            if (flag.get(index) <= 0) {
                emissionQueues.remove(index);
            } else {
                double potential = potn.get(index);
                if (potential <= 0) {
                    emissionQueues.remove(index);
                } else {
                    Vector3 v = Vector3.create(velo.slice(0, this.index));
                    double speed = Math.sqrt(v.dotProduct(v));

                    Vector3 emission = new Vector3();
                    fillRandom(emission, new Date().getTime());
                    emission.multiply(2);
                    emission.sub(1);
                    double length = Math.sqrt(emission.dotProduct(emission));
                    emission.scale(1.0 / length);
                    emission.crossProduct(v.scaleCopy(1.0 / speed));
                    emission.add(v);

                    Vector3 p = Vector3.create(pos.slice(0, this.index));
                    addIota(this.index, p, emission);
                }
            }
        }
    }

    public static void emmitIota(double dt) {
        for (int key: new ArrayList<Integer>(emissionQueues.keySet())) {
            emissionQueues.get(key).emit(dt);
        }
    }

    public static void injectIota(double dt) {
        double count = injectRate * dt;
        for (int i = 0; i < count; i++) {
            addIota(null);
        }
    }

    public static void escapeIota(double dt) {
        for (int i = 0; i < numOfParticles; i++) {
            if (flag.get(i) > 0 && combinators.get(i) != null) {
                String script = combinators.get(i).script();
                if (!script.contains("S") && !script.contains("K") && !script.contains("I")) {
                    if (Math.random() < escapeProb * dt) {
                        flag.set(i, -1.0);
                        combinators.set(i, null);
                        mass.set(i, 0.0);
                        knct.set(i, 0.0);
                        potn.set(i, 0.0);
                        pos.slice(0, i).scale(0.0);
                        velo.slice(0, i).scale(0.0);
                        mmnt.slice(0, i).scale(0.0);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        init();

        while(true) {
            refreshTree();
            checkDeltaT();

            emmitIota(mindt);
            injectIota(mindt);
            escapeIota(mindt);
            freefly(mindt);
            t = t + mindt;

            checkCollision();
            if (mindist < collisionThreshhold) {
                counter++;
                collisionMerge();
            }
        }

    }

}
