package skiverse;

import narr.Array;
import narr.NA;
import narr.random.Generators;
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


public class ParticleSystem {

    public static double width = 64;
    public static int numOfParticles = 8192;
    public static double emitRate = 6.0;
    public static double injectRate = 0.0;
    public static double escapeProb = 0.9;
    public static double collisionThreshhold = 0.1;

    protected int population = 0;
    protected int diversity = 0;

    protected Array flag = NA.zeros(numOfParticles);
    protected Array mass = NA.zeros(numOfParticles);
    protected Array posn = NA.zeros(numOfParticles, 3);
    protected Array velo = NA.zeros(numOfParticles, 3);
    protected Array mmnt = NA.zeros(numOfParticles, 3);
    protected Array sppd = NA.zeros(numOfParticles);
    protected Array potn = NA.zeros(numOfParticles);
    protected Array knct = NA.zeros(numOfParticles);

    protected double avergespeed = 0.0;

    protected AABBTree<Entity> tree = new AABBTree<Entity>();
    protected List<CollisionPair<Entity>> collisionPairs = new ArrayList<CollisionPair<Entity>>(numOfParticles / 10);

    protected Map<Integer, Emission> emissionQueues = new HashMap<>();
    protected Map<Integer, Split> splitQueues = new HashMap<>();

    protected List<SKI.Combinator> combinators = new ArrayList<SKI.Combinator>(numOfParticles);
    protected Map<String, Integer> countMap = new HashMap<String, Integer>();

    protected double t = 0;
    protected int counter = 0;

    protected double mindt = 1.0;
    protected double mindist = Double.MAX_VALUE;
    protected int icollision = -1;
    protected int jcollision = -1;

    public ParticleSystem() {
        flag.fill(Generators.uniform_n1p1);
        posn.fill(Generators.uniform_01).scale(width);
        velo.fill(Generators.uniform_n1p1).scale(0.7);
        velo.along(0, (i, v) -> sppd.set(i, v.norm()));

        for (int i = 0; i < numOfParticles; i++) {
            combinators.add(null);
        }

        for (int i = 0; i < numOfParticles; i++) {
            if (flag.element(i) > 0) {
                SKI.Combinator cmbn = null;
                switch (i % 3) {
                    case 0: cmbn = SKI.I(); break;
                    case 1: cmbn = SKI.K(); break;
                    case 2: cmbn = SKI.S(); break;
                }
                combinators.set(i, cmbn);
                double m = cmbn.mass();
                double v = sppd.element(i);

                mass.set(i, m);
                knct.set(i, m * v * v / 2.0);

                for (int j = 0; j < 3; j++) {
                    mmnt.set(i, j, velo.element(i, j) * m);
                }
            }
        }
        avergespeed = sppd.mean();
    }

    class Entity implements Identifiable, Boundable {
        public int idx;

        public Entity(int idx) {
            this.idx = idx;
        }

        @Override
        public AABBf getAABB(AABBf dest) {
            if (dest == null) {
                dest = new AABBf();
            }

            float thresh = (float)(collisionThreshhold / avergespeed);
            float px = (float)posn.element(this.idx, 0);
            float py = (float)posn.element(this.idx, 1);
            float pz = (float)posn.element(this.idx, 2);
            float vx = (float)velo.element(this.idx, 0);
            float vy = (float)velo.element(this.idx, 1);
            float vz = (float)velo.element(this.idx, 2);
            dest.setMin(px, py, pz);
            dest.setMax(px + vx * thresh, py + vy * thresh, pz + vz * thresh);
            return dest;
        }

        @Override
        public long getID() {
            return this.idx;
        }
    }

    protected int availableIndex() {
        int i = 0;
        for (; i < numOfParticles; i++) {
            if (flag.element(i) < 0) return i;
        }
        throw new RuntimeException("no available index exists");
    }

    public void refresh() {
        tree.clear();
        collisionPairs.clear();
        for(int i = 0; i < numOfParticles; i++) {
            if(flag.element(i) >= 0.0) {
                tree.add(new Entity(i));
            }
        }
        tree.detectCollisionPairs(collisionPairs);

        avergespeed = sppd.mean();
    }

    public void clear(int i) {
        flag.set(i, -1.0);
        combinators.set(i, null);
        mass.set(i, 0.0);
        sppd.set(i, 0.0);
        knct.set(i, 0.0);
        potn.set(i, 0.0);
        posn.set(i, 0, 0.0);
        posn.set(i, 1, 0.0);
        posn.set(i, 2, 0.0);
        velo.set(i, 0, 0.0);
        velo.set(i, 1, 0.0);
        velo.set(i, 2, 0.0);
        mmnt.set(i, 0, 0.0);
        mmnt.set(i, 1, 0.0);
        mmnt.set(i, 2, 0.0);
    }

    public void update(int i, SKI.Combinator cmbn, double m, double px, double py, double pz, double vx, double vy, double vz, double potential) {
        double f = cmbn.script().equals("ι") ? 0.0 : 1.0;
        double sq = vx * vx + vy * vy + vz * vz;
        double s = Math.sqrt(sq);
        double k = m * sq / 2;

        flag.set(i, f);
        combinators.set(i, cmbn);
        mass.set(i, m);
        sppd.set(i, s);
        knct.set(i, k);
        potn.set(i, potential);
        posn.set(i, 0, px);
        posn.set(i, 1, py);
        posn.set(i, 2, pz);
        velo.set(i, 0, vx);
        velo.set(i, 1, vy);
        velo.set(i, 2, vz);
        mmnt.set(i, 0, m * vx);
        mmnt.set(i, 1, m * vy);
        mmnt.set(i, 2, m * vz);
    }

    public void update(int e, int o) {
        double mmntfixx = mmnt.element(e, 0);
        double mmntfixy = mmnt.element(e, 0);
        double mmntfixz = mmnt.element(e, 0);
        double energyfix = knct.element(e) + potn.element(e);

        double m = mass.element(o);
        double mmntx = mmnt.element(o, 0);
        double mmnty = mmnt.element(o, 1);
        double mmntz = mmnt.element(o, 2);
        mmntx = mmntx - mmntfixx;
        mmnty = mmnty - mmntfixy;
        mmntz = mmntz - mmntfixz;
        double velox = mmntx / m;
        double veloy = mmnty / m;
        double veloz = mmntx / m;
        double spdsq = velox * velox + veloy * veloy + veloz * veloz;
        double speed = Math.sqrt(spdsq);
        double kinect = m * spdsq / 2.0;
        double oknct = knct.element(o);
        double opotn = potn.element(o);
        double energy = oknct + opotn;

        mmnt.set(o, 0, mmntx);
        mmnt.set(o, 1, mmnty);
        mmnt.set(o, 2, mmntz);
        velo.set(o, 0, velox);
        velo.set(o, 1, veloy);
        velo.set(o, 2, veloz);
        sppd.set(o, speed);
        knct.set(o, kinect);
        potn.set(o, energy - energyfix - kinect);

        System.out.println("------------------------------------------------------------------");
        System.out.println("Emission");
        System.out.println("time %f".formatted(t));
        System.out.println("emit: %d -> %d, %d".formatted(o, o, e));
        System.out.println("cmbn: %s -> %s, %s".formatted(combinators.get(o).script(), combinators.get(o).script(), combinators.get(e).script()));
        System.out.println("mass: %f -> %f, %f".formatted(mass.element(o), mass.element(o), combinators.get(e).mass()));
        System.out.println("knct: %f -> %f, %f".formatted(oknct, knct.element(o), knct.element(e)));
        System.out.println("potn: %f -> %f, %f".formatted(opotn, potn.element(o), potn.element(e)));
        System.out.println("------------------------------------------------------------------");
    }

    public void freefly(double mdt) {
        for(int m = 0; m < numOfParticles; m++) {
            for (int n = 0; n < 3; n++) {
                if (flag.element(m) >= 0.0) {
                    posn.set(m, n, (posn.element(m, n) + velo.element(m, n) * mdt + width) % width);
                }
            }
        }
    }

    public void checkDeltaT() {
        mindt = 1.0 / emitRate;
        for(CollisionPair p: collisionPairs) {
            int i = ((Entity)p.getObjectA()).idx;
            int j = ((Entity)p.getObjectB()).idx;

            if (i != j) {
                double distx = posn.element(i, 0) - posn.element(j, 0);
                double disty = posn.element(i, 1) - posn.element(j, 1);
                double distz = posn.element(i, 2) - posn.element(j, 2);

                double dvelox = velo.element(i, 0) - velo.element(j, 0);
                double dveloy = velo.element(i, 1) - velo.element(j, 1);
                double dveloz = velo.element(i, 2) - velo.element(j, 2);

                double c1 = distx * dvelox + disty * dveloy + distz * dveloz;
                double c2 = dvelox * dvelox + dvelox * dveloy + dvelox * dveloz;
                double dt = c1 / c2;
                if (dt > 0 && dt < mindt) {
                    mindt = dt;
                }
            }
        }
    }

    public void checkCollision() {
        mindist = width;
        for(CollisionPair p: collisionPairs) {
            int i = ((Entity) p.getObjectA()).idx;
            int j = ((Entity) p.getObjectB()).idx;

            if (i != j) {
                double distx = posn.element(i, 0) - posn.element(j, 0);
                double disty = posn.element(i, 1) - posn.element(j, 1);
                double distz = posn.element(i, 2) - posn.element(j, 2);

                double c0 = distx * distx + disty * disty + distz * distz;
                if (c0 < mindist * mindist) {
                    mindist = Math.sqrt(c0);
                    icollision = i;
                    jcollision = j;
                }
            }
        }
    }

    public void collisionMerge() {
        SKI.Combinator left = combinators.get(icollision);
        SKI.Combinator right = combinators.get(jcollision);

        if (left == null || right == null || flag.element(icollision) < 0.0 || flag.element(jcollision) < 0.0) {
            return;
        }

        flag.set(icollision, -1);
        flag.set(jcollision, -1);

        int i = 0;
        for (; i<numOfParticles; i++) {
            if (flag.element(i) < 0) break;
        }
        if (i >= numOfParticles) {
            return;
        }

        double lmass = mass.element(icollision);
        double rmass = mass.element(jcollision);
        double lknct = knct.element(icollision);
        double rknct = knct.element(jcollision);
        double lpotn = potn.element(icollision);
        double rpotn = potn.element(jcollision);

        double zposx = (posn.element(icollision, 0) * lmass + posn.element(jcollision, 0) * rmass) / (lmass + rmass);
        double zposy = (posn.element(icollision, 1) * lmass + posn.element(jcollision, 1) * rmass) / (lmass + rmass);
        double zposz = (posn.element(icollision, 2) * lmass + posn.element(jcollision, 2) * rmass) / (lmass + rmass);

        SKI.Combinator formula = SKI.cons(left, right);
        formula.supply(new SKI.Potential(lpotn + rpotn));
        SKI.Combinator result = formula.eval();
        combinators.set(i, result);

        double zmass = result.mass();
        double zmmntx = mmnt.element(icollision, 0) + mmnt.element(jcollision, 0);
        double zmmnty = mmnt.element(icollision, 1) + mmnt.element(jcollision, 1);
        double zmmntz = mmnt.element(icollision, 2) + mmnt.element(jcollision, 2);
        double zvelox = zmmntx / zmass;
        double zveloy = zmmnty / zmass;
        double zveloz = zmmntz / zmass;
        double zvelosq = zvelox * zvelox + zveloy * zveloy + zveloz * zveloz;
        double zknct = zmass * zvelosq / 2;
        double zpotn = potn.element(icollision) + potn.element(jcollision) + knct.element(icollision) + knct.element(jcollision) - zknct;
        zpotn = lmass + rmass - zmass + zpotn;
        if (zpotn > 0) {
            if (result instanceof SKI.CompositiveCombinator comp && comp.breakup) {
                splitQueues.put(i, new Split(i));
            } else {
                emissionQueues.put(i, new Emission(i));
            }
        }

        combinators.set(i, result);
        mass.set(i, zmass);
        knct.set(i, zknct);
        potn.set(i, zpotn);
        posn.set(i, 0, zposx);
        posn.set(i, 1, zposy);
        posn.set(i, 2, zposz);
        velo.set(i, 0, zvelox);
        velo.set(i, 1, zveloy);
        velo.set(i, 2, zveloz);
        mmnt.set(i, 0, zmmntx);
        mmnt.set(i, 1, zmmnty);
        mmnt.set(i, 2, zmmntz);
        sppd.set(i, Math.sqrt(zvelosq));

        flag.set(i, 1.0);

        population = countMap.values().stream().reduce(0, new BinaryOperator() {
            @Override
            public Object apply(Object o, Object o2) {
                return ((Integer)o).intValue() + ((Integer)o2).intValue() ;
            }
        }).intValue();
        diversity = countMap.size();

        System.out.println("==================================================================");
        System.out.println("Collision");
        System.out.println("time %f".formatted(t));
        System.out.println("collision %d: %d, %d".formatted(counter, icollision, jcollision));
        System.out.println("script %d: %s, %s -> %s".formatted(counter, left.script(), right.script(), result.script()));
        System.out.println("------------------------------------------------------------------");
        System.out.println("mass %d: %f, %f -> %f".formatted(counter, left.mass(), right.mass(), result.mass()));
        System.out.println("kinect %d: %f, %f -> %f".formatted(counter, lknct, rknct, zknct));
        System.out.println("potential %d: %f, %f -> %f".formatted(counter, lpotn, rpotn, zpotn));
        System.out.println("==================================================================");
        System.out.println("Statistic");
        System.out.println("time %f".formatted(t));
        System.out.println("total mass %d: %f".formatted(counter, mass.sum()));
        System.out.println("total kinect %d: %f".formatted(counter, knct.sum()));
        System.out.println("total potential %d: %f".formatted(counter, potn.sum()));
        System.out.println("------------------------------------------------------------------");
        System.out.println("time %f".formatted(t));

        System.out.println("particles %d".formatted(population));
        System.out.println("diversity %d".formatted(diversity));
        System.out.println("==================================================================");
        System.out.println("List");
        System.out.println("time %f".formatted(t));
        countMap.clear();
        for (int l = 0; l < numOfParticles; l++) {
            if (flag.element(l) >= 0.0 && combinators.get(l) != null) {
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
        System.out.println("==================================================================");
    }

    public class Emission {
        private int index;
        public Emission(int index) {
            this.index = index;
        }
        void emit(double dt) {
            if (flag.element(index) <= 0) {
                emissionQueues.remove(index);
            } else {
                double potential = potn.element(index);
                if (potential < 0.5) {
                    emissionQueues.remove(index);
                } else {
                    double rx = Generators.uniform_n1p1.generate();
                    double ry = Generators.uniform_n1p1.generate();
                    double rz = Generators.uniform_n1p1.generate();
                    double sqr = rx * rx + ry * ry + rz * rz;
                    double sr = Math.sqrt(sqr);
                    rx = rx / sr;
                    ry = ry / sr;
                    rz = rz / sr;

                    double vx = velo.element(this.index, 0);
                    double vy = velo.element(this.index, 1);
                    double vz = velo.element(this.index, 2);
                    double sqv = vx * vx + vy * vy + vz * vz;
                    double sv = Math.sqrt(sqv);
                    vx = vx / sv;
                    vy = vy / sv;
                    vz = vz / sv;

                    double tx = vy * rz - vz * ry + vx;
                    double ty = vz * rx - vx * rz + vy;
                    double tz = vx * ry - vy * rx + vz;
                    double sqt = tx * tx + ty * ty + tz * tz;
                    double st = Math.sqrt(sqt);

                    double threshdt = collisionThreshhold * 1.1 / st;
                    double px = posn.element(this.index, 0) + tx * threshdt;
                    double py = posn.element(this.index, 1) + ty * threshdt;
                    double pz = posn.element(this.index, 2) + tz * threshdt;

                    int e = availableIndex();
                    SKI.Combinator iota = SKI.iota();
                    update(e, iota, iota.mass(), px, py, pz, tx, ty, tz, 0);
                    update(e, this.index);
                }
            }
        }
    }

    public class Split {
        private int index;
        public Split(int index) {
            this.index = index;
        }
        void split(double dt) {
            if (flag.element(index) <= 0) {
                splitQueues.remove(index);
            } else {
                double potential = potn.element(index);
                if (potential < 0.5) {
                    splitQueues.remove(index);
                } else {
                    SKI.Combinator combinator = combinators.get(this.index);
                    if (combinator instanceof SKI.CompositiveCombinator) {
                        SKI.CompositiveCombinator cmbn = (SKI.CompositiveCombinator)combinator;
                        SKI.Combinator left = cmbn.left;
                        SKI.Combinator right = cmbn.right;
                        double ml = left.mass();
                        double mr = right.mass();
                        double mo = ml + mr;
                        double ko = knct.element(this.index);
                        double po = potn.element(this.index);
                        double energy = ko + po;
                        double a = mr / (ml + mr);
                        double b = ml / (ml + mr);
                        double s = Math.sqrt(2 * potn.element(this.index) / cmbn.mass());
                        if (cmbn.breakup) {
                            double rx = Generators.uniform_n1p1.generate();
                            double ry = Generators.uniform_n1p1.generate();
                            double rz = Generators.uniform_n1p1.generate();
                            double sqr = rx * rx + ry * ry + rz * rz;
                            double sr = Math.sqrt(sqr);
                            rx = rx / sr;
                            ry = ry / sr;
                            rz = rz / sr;

                            double px = posn.element(this.index, 0);
                            double py = posn.element(this.index, 1);
                            double pz = posn.element(this.index, 2);
                            double vx = velo.element(this.index, 0);
                            double vy = velo.element(this.index, 1);
                            double vz = velo.element(this.index, 2);
                            double sqv = vx * vx + vy * vy + vz * vz;
                            double sv = Math.sqrt(sqv);
                            vx = vx / sv;
                            vy = vy / sv;
                            vz = vz / sv;

                            double tx = (vy * rz - vz * ry) * s;
                            double ty = (vz * rx - vx * rz) * s;
                            double tz = (vx * ry - vy * rx) * s;

                            double threshdt = collisionThreshhold * 1.1 / s;
                            double dx = tx * threshdt;
                            double dy = ty * threshdt;
                            double dz = tz * threshdt;

                            int l = availableIndex();
                            update(l, left, left.mass(), px + dx, py + dy, pz + dz, vx + a * tx, vy + a * ty, vz + a * tz, 0);

                            int r = availableIndex();
                            update(r, right, right.mass(), px - dx, py - dy, pz - dz, vx - b * tx, vx - b * ty, vx - b * tz, 0);

                            double p = energy - knct.element(l) - knct.element(r);
                            potn.set(l, b * p);
                            potn.set(r, a * p);

                            clear(this.index);

                            System.out.println("------------------------------------------------------------------");
                            System.out.println("Split");
                            System.out.println("time %f".formatted(t));
                            System.out.println("split: %d -> %d, %d".formatted(this.index, l, r));
                            System.out.println("cmbn: %s -> %s, %s".formatted(cmbn.script(), left.script(), right.script()));
                            System.out.println("mass: %f -> %f, %f".formatted(mo, ml, mr));
                            System.out.println("knct: %f -> %f, %f".formatted(ko, knct.element(l), knct.element(r)));
                            System.out.println("potn: %f -> %f, %f".formatted(po, potn.element(l), potn.element(r)));
                            System.out.println("------------------------------------------------------------------");
                        }
                    }
                }
            }
        }
    }

    public void emmitIota(double dt) {
        for (int i = 0; i < emitRate * dt; i++) {
            for (int key : new ArrayList<Integer>(emissionQueues.keySet())) {
                emissionQueues.get(key).emit(dt);
            }
        }
    }

    public void injectIota(double dt) {
        double count = injectRate * dt;
        for (int i = 0; i < count; i++) {
            int j = availableIndex();
            SKI.Combinator iota = SKI.iota();

            double px = Generators.uniform_01.generate() * width;
            double py = Generators.uniform_01.generate() * width;
            double pz = Generators.uniform_01.generate() * width;
            double vx = Generators.uniform_n1p1.generate();
            double vy = Generators.uniform_n1p1.generate();
            double vz = Generators.uniform_n1p1.generate();
            double sq = vx * vx + vy * vy + vz * vz;
            double s = Math.sqrt(sq);
            vx = vx / s;
            vy = vy / s;
            vz = vz / s;

            update(j, iota, iota.mass(), px, py, pz, vx, vy, vz, 0);
        }
    }

    public void escapeIota(double dt) {
        if (t > 5000.0) {
            for (int i = 0; i < numOfParticles; i++) {
                if (flag.element(i) > 0 && combinators.get(i) != null) {
                    String script = combinators.get(i).script();
                    if (!script.contains("S") && !script.contains("K") && !script.contains("I")) {
                        if (Math.random() < escapeProb * dt) {
                            clear(i);
                        }
                    }
                }
            }
        }
    }

    public void splitExec(double dt) {
        for (int i = 0; i < emitRate * dt; i++) {
            for (int key : new ArrayList<Integer>(splitQueues.keySet())) {
                splitQueues.get(key).split(dt);
            }
        }
    }

}
