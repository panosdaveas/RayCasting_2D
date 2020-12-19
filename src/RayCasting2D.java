import processing.core.PApplet;
import processing.core.PVector;

import java.util.Vector;

import static java.lang.Math.*;
import static java.lang.Math.pow;

public class RayCasting2D  extends PApplet {

    //Wall wall;
    Player player;
    Vector<Wall> walls;
    float xOffset = 0;
    float yOffset = 10000;

    public void settings(){
        size(400,400);
        pixelDensity(2);
        player = new Player();
        walls = randomMap(walls);
    }

    public void draw(){
        background(0);
        stroke(255);

        printMap();
        //player.update(mouseX, mouseY);
        player.update(noise(xOffset) * width, noise(yOffset) * height);
        player.print();
        player.look(walls);

        xOffset += 0.01;
        yOffset += 0.01;
    }

    class Wall {
        PVector a, b;

        Wall(float x1, float y1, float x2, float y2) {
            this.a = new PVector(x1, y1);
            this.b = new PVector(x2, y2);
        }

        void print(){
            strokeWeight(2);
            //stroke(0);
            line(a.x, a.y, b.x, b.y);
        }
    }

    class Ray {
        PVector pos;
        PVector dir;

        Ray(PVector myPos, float angle){
            this.pos = myPos;
            this.dir = PVector.fromAngle(angle);
        }

        void lookAt(float x, float y){
            dir.x = x - pos.x;
            dir.y = y - pos.y;
            dir.normalize();
        }

        void print(){
            float scale = 10;
            push();
            strokeWeight(1);
            translate(pos.x, pos.y);
            line(0, 0, dir.x * scale, dir.y * scale);
            pop();
        }

        PVector cast(Wall wall){
            //wall line points
            float x1 = wall.a.x;
            float y1 = wall.a.y;
            float x2 = wall.b.x;
            float y2 = wall.b.y;
            //ray line start points
            float x3 = pos.x;
            float y3 = pos.y;
            //ray line end points
            float x4 = pos.x + dir.x;
            float y4 = pos.y + dir.y;
            //https://en.wikipedia.org/wiki/Line–line_intersection
            //formula denominator
            float den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
            if (den == 0) { return null; } //den = 0, fraction -> infinity

            float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
            float u = - ((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;

            if (t > 0 && t < 1 && u > 0){
                PVector intersectionPoint = new PVector();
                intersectionPoint.x = x1 + t * (x2 - x1);
                intersectionPoint.y = y1 + t * (y2 -y1);
                return intersectionPoint;
            }else{
                return null;}
        }

    }

    class Player {
        PVector pos;
        Vector<Ray> rays;

        Player(){
            this.pos = new PVector(width /2, height/2);
            this.rays = new Vector<>();

            for( int angle = 0; angle < 360; angle+=1){
                this.rays.add(new Ray(this.pos, radians(angle)));
            }
        }

        void update(float x, float y){
            this.pos.set(x, y);
        }

        void look(Vector<Wall> walls){

            for (Ray ray : this.rays){
                PVector min = null;
                float max = Float.MAX_VALUE;
                for(Wall wall : walls) {
                    PVector pt = ray.cast(wall);
                    if (pt != null) {
                        float d = distance(pos, pt);
                        if (d < max){
                            max = d;
                            min = pt;
                        }
                    }
                }
                if (min != null) {
                    strokeWeight(1);
                    stroke(255, 100); //transparency
                    line(this.pos.x, this.pos.y, min.x, min.y);
                    //stroke(255);
                    //strokeWeight(4);
                    //point(min.x, min.y);
                }
            }
        }

        void print(){
            fill(255);
            circle(this.pos.x, this.pos.y, 4);
            for(Ray ray : this.rays){
                ray.print();
            }
        }
    }

    Vector<Wall> randomMap(Vector<Wall> walls){
        walls = new Vector<>();
        int numOfWalls = 5;
        for (int i = 0; i < numOfWalls; i++){
            float x1 = random(width);
            float y1 = random(height);
            float x2 = random(width);
            float y2 = random(height);
            walls.add(new Wall(x1, y1, x2, y2));
        }
        //boundaries//
        walls.add(new Wall(0, 0, width, 0));
        walls.add(new Wall(width, 0, width, height));
        walls.add(new Wall(width, height, 0, height));
        walls.add(new Wall(0, height, 0, 0));

        return walls;
    }

    public void printMap(){
        for(Wall wall : walls){
            strokeWeight(2);
            wall.print();
        }
    }

    float distance(PVector a, PVector b){
        return (float) sqrt(pow(a.x - b.x, 2) + pow(a.y - b.y, 2) + pow(a.z - b.z, 2));
    }

    public static void main(String... args){
        PApplet.main("RayCasting2D");
    }
}
