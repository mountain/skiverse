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
import java.util.stream.Collectors;

import static mikera.arrayz.Arrayz.fillRandom;
import static mikera.arrayz.Arrayz.newArray;


public class ParticleSystem {

    static double width = 64;
    static int numOfParticles = 8192;
    static double injectRate = 1.0;
    static double escapeProb = 0.99;

    static INDArray flag = newArray(numOfParticles);
    static INDArray mass = newArray(numOfParticles);
    static INDArray pos = newArray(numOfParticles, 3);
    static INDArray velo = newArray(numOfParticles, 3);
    static INDArray potn = newArray(numOfParticles);
    static INDArray knct = newArray(numOfParticles);
    static INDArray mmnt = newArray(numOfParticles, 3);

    static AABBTree<Entity> tree = new AABBTree<Entity>();
    static List<CollisionPair<Entity>> collisionPairs = new ArrayList<CollisionPair<Entity>>(numOfParticles / 10);

    static List<SKI.Combinator> combinators = new ArrayList<SKI.Combinator>(numOfParticles);
    static Map<String, Integer> countMap = new HashMap<String, Integer>();

    static double t = 0;
    static double lastt = 0;
    static int counter = 0;

    static double mindt = width;
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
            INDArray p = pos.slice(0, this.idx);
            INDArray v = velo.slice(0, this.idx);
            dest.setMin((float)p.get(0), (float)p.get(1), (float)p.get(2));
            dest.setMax((float)(p.get(0) + v.get(0) * 0.2), (float)(p.get(1) + v.get(1) * 0.2), (float)(p.get(2) + v.get(2) * 0.2));
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

        for (int i = 0; i < numOfParticles; i++) {
            combinators.add(null);
            if (flag.get(i) < 0) continue;

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

    public static void refreshTree() {
        tree.clear();
        collisionPairs.clear();
        for(int i = 0; i < numOfParticles; i++) {
            if(flag.get(i) > 0.0) {
                tree.add(new Entity(i));
            }
        }
        tree.detectCollisionPairs(collisionPairs);
    }

    public static void addIota() {
        int i = 0;
        for (; i<numOfParticles; i++) {
            if (flag.get(i) < 0) break;
        }

        INDArray apos = pos.slice(0, i);
        INDArray avelo = pos.slice(0, i);
        fillRandom(apos, new Date().getTime());
        fillRandom(avelo, new Date().getTime());
        apos.multiply(width);
        avelo.multiply(2);
        avelo.sub(1);

        combinators.set(i, SKI.iota());
        Vector3 ivelo = Vector3.create(velo.slice(0, i));
        double vsq = ivelo.dotProduct(ivelo);
        double m = combinators.get(i).mass();
        mass.set(i, m);
        potn.set(i, 0.0);
        knct.set(i, m * vsq / 2);
        mmnt.slice(0, i).set(ivelo.scaleCopy(m));

        flag.set(i, 1.0);
        lastt = t;
    }

    public static void freefly(double mdt) {
        for(int m = 0; m < numOfParticles; m++) {
            for(int n = 0; n < 3; n++) {
                if(flag.get(m) > 0.0) {
                    pos.set(m, n, (pos.get(m, n) + velo.get(m, n) * mdt + width) % width);
                }
            }
        }
    }

    public static void checkDeltaT() {
        mindt = 0.2;
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
        SKI.Combinator result = SKI.cons(left, right).eval();

        flag.set(icollision, -1);
        flag.set(jcollision, -1);

        int i = 0;
        for (; i<numOfParticles; i++) {
            if (flag.get(i) < 0) break;
        }
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

        //double mloss = lmass + rmass - zmass;
        //for(int k = 0; k < mloss; k++) {
        //    addIota();
        //}
        //for (int k = 0; k < zpotn; k++) {
        //    addIota();
        //}

        combinators.set(i, result);
        mass.set(i, zmass);
        knct.set(i, zknct);
        potn.set(i, 0.0);
        velo.slice(0, i).set(zvelo);
        mmnt.slice(0, i).set(zvelo.scaleCopy(zmass));

        flag.set(i, 1.0);

        System.out.println("time %f".formatted(t));
        System.out.println("collision %d: %d, %d".formatted(counter, icollision, jcollision));
        System.out.println("script %d: %s, %s -> %s".formatted(counter, left.script(), right.script(), result.script()));
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
        countMap.clear();
        for (int k = 0; k < numOfParticles; k++) {
            if (flag.get(k) > 0) {
                String key = combinators.get(k).script();
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

    public static void injectIota() {
        double prob = injectRate / numOfParticles;
        for (int i = 0; i < numOfParticles; i++) {
            if(Math.random() < prob) {
                addIota();
            }
        }
    }

    public static void escapeIota() {
        for (int i = 0; i < numOfParticles; i++) {
            if (flag.get(i) > 0 && combinators.get(i).script().equals("ι")) {
                if(Math.random() < escapeProb) {
                    flag.set(i, -1);
                }
            }
        }
    }

    // Elastic collision: https://en.wikipedia.org/wiki/Elastic_collision
    public static void elasticCollision() {
        double imass = mass.get(icollision);
        double jmass = mass.get(jcollision);
        Vector3 ipos = Vector3.create(pos.slice(0, icollision));
        Vector3 jpos = Vector3.create(pos.slice(0, jcollision));
        Vector3 ivelo = Vector3.create(velo.slice(0, icollision));
        Vector3 jvelo = Vector3.create(velo.slice(0, jcollision));
        Vector3 dvelo = Vector3.create(ivelo.subCopy(jvelo));
        Vector3 dpos = Vector3.create(ipos.subCopy(jpos));

        double iratio = 2 * jmass / (imass + jmass);
        double jratio = 2 * imass / (imass + jmass);
        double distsq = dpos.dotProduct(dpos);
        double c1 = dpos.dotProduct(dvelo);

        ivelo.sub(dpos.scaleCopy(iratio * c1 / distsq));
        jvelo.sub(dpos.scaleCopy(- jratio * c1 / distsq));
        velo.slice(0, icollision).set(ivelo);
        velo.slice(0, jcollision).set(jvelo);
    }

    public static void main(String[] args) {
        init();

        while(true) {
            refreshTree();
            checkDeltaT();

            t = t + mindt;

            freefly(mindt);

            if (t - lastt > 0.1) {
                injectIota();
                escapeIota();
            }

            checkCollision();

            if (mindist < 0.1) {
                counter++;
                collisionMerge();
            }
        }

    }

}
