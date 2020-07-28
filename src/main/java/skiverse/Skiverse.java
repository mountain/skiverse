package skiverse;

public class Skiverse {
    public static void main(String[] args) {
        ParticleSystem psys = new ParticleSystem();

        while(true) {
            psys.refresh();
            psys.checkDeltaT();

            psys.splitExec(psys.mindt);
            psys.emmitIota(psys.mindt);
            psys.injectIota(psys.mindt);
            psys.escapeIota(psys.mindt);

            psys.freefly(psys.mindt);

            psys.t = psys.t + psys.mindt;

            psys.checkCollision();
            if (psys.mindist < psys.collisionThreshhold) {
                psys.counter++;
                psys.collisionMerge();
            }
        }

    }
}
