package skiverse;

public class Skiverse {

    public static void main(String[] args) {
        Particle p1 = new Particle();
        p1.m = 1;
        p1.vx = 1;
        p1.vy = 0;
        p1.k = 1;

        Particle p2 = new Particle();
        p2.m = 2;
        p2.vx = 0;
        p2.vy = 1;
        p2.k = 2;

        Particle p3 = new Particle();
        p2.m = 3;
        p2.vx = 1 / 3;
        p2.vy = 2 / 3;
        p3.k = 5 / 3;

    }

}
