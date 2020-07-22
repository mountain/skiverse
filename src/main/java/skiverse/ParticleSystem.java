package skiverse;

import mikera.arrayz.INDArray;
import mikera.vectorz.Vector3;
import org.joml.AABBf;
import pl.pateman.dynamicaabbtree.AABBTree;
import pl.pateman.dynamicaabbtree.Boundable;
import pl.pateman.dynamicaabbtree.CollisionPair;
import pl.pateman.dynamicaabbtree.Identifiable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static mikera.arrayz.Arrayz.fillRandom;
import static mikera.arrayz.Arrayz.newArray;


public class ParticleSystem {

    static double width = 16;
    static int numOfParticles = 256;
    static INDArray pos = newArray(numOfParticles, 3);
    static INDArray velo = newArray(numOfParticles, 3);

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
            dest.setMax((float)(p.get(0) + v.get(0)), (float)(p.get(1) + v.get(1)), (float)(p.get(2) + v.get(2)));
            return dest;
        }

        @Override
        public long getID() {
            return this.idx;
        }
    }

    public static void main(String[] args) {
        fillRandom(pos, new Date().getTime());
        fillRandom(velo, new Date().getTime());
        pos.multiply(width);
        velo.multiply(2);
        velo.sub(1);

        AABBTree<Entity> tree = new AABBTree<Entity>();
        List<CollisionPair<Entity>> collisionPairs = new ArrayList<CollisionPair<Entity>>(numOfParticles / 10);

        double t = 0;
        while(true) {
            tree.clear();
            collisionPairs.clear();
            for(int i = 0; i < numOfParticles; i++) {
                tree.add(new Entity(i));
            }
            tree.detectCollisionPairs(collisionPairs);

            double mdt = 10;
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
                    if (dt > 0 && dt < mdt) {
                        mdt = dt;
                    }
                }
            }

            t = t + mdt;
            for(int m = 0; m < numOfParticles; m++) {
                for(int n = 0; n < 3; n++) {
                    pos.set(m, n, (pos.get(m, n) + velo.get(m, n) * mdt + width) % width);
                }
            }

            double dm = Double.MAX_VALUE;
            for(CollisionPair p: collisionPairs) {
                int i = ((Entity) p.getObjectA()).idx;
                int j = ((Entity) p.getObjectB()).idx;

                if (i != j) {
                    Vector3 dpos = Vector3.create(pos.slice(0, i));
                    dpos.sub(Vector3.create(pos.slice(0, j)));

                    double c0 = dpos.dotProduct(dpos);
                    if (c0 < dm * dm) {
                        dm = Math.sqrt(c0);
                    }
                }
            }

            System.out.println("%f %f".formatted(t, dm));
            if(dm < 0.1) {
                break;
            }
        }
    }

}
