import com.jogamp.opengl.math.Ray;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.Vector;

public class RayCasting3D extends  PApplet {

    int tileSize = 32;
    int rows = 11;
    int cols = 15;
    int windowWidth = cols * tileSize;
    int windowHeight = rows * tileSize;
    float FOVangle = deg2rad(60);
    float wallStripWidth = 20;
    int numOfRays = (int) (windowWidth / wallStripWidth);

    Player player;
    Grid grid;
    //Ray[] rays;
    Vector<Ray> rays;

    public  void settings(){
        size(windowWidth, windowHeight);
        grid = new Grid();
        player = new Player();
        //rays = new Ray[numOfRays];
    }

    public void draw(){
        update();

        grid.printGrid();
        for(Ray ray : rays){
            ray.printRay();
        }
        player.printPlayer();

        //rayAngle += FOVangle / numOfRays;
    }

    public void update(){
        player.updatePlayer();
        castRays();
    }

    class Grid {
        int[][] wall;
        Grid(){
            this.wall = new int[32][32];
            for (int i = 0; i < cols; i++){
                for ( int j = 0; j < rows; j++) {
                    if (i == 0 || i == cols - 1 || j == 0) {
                        this.wall[i][j] = 1;
                    } else {
                        this.wall[i][j] = 0;
                    }
                }
            }
        }

        public void printGrid(){
            for (int i = 0; i < cols; i++){
                for ( int j = 0; j < rows; j++){
                    int tileX = i * tileSize;
                    int tileY = j * tileSize;
                    int tileColor = this.wall[i][j] == 1 ? 0 : 255;
                    stroke(0);
                    strokeWeight(1);
                    fill(tileColor);
                    rect(tileX, tileY, tileSize, tileSize);
                }
            }
        }

        public boolean hasWall(float x, float y){
            if ( x < 0 || x > windowWidth || y < 0 || y > windowHeight){
                return true;
            }
            int indexX = floor(x / tileSize);
            int indexY = floor(y / tileSize);
            if (wall[indexX][indexY] == 1){
                return true;
            }
            return false;
        }

    }

    class Player {

        float x;
        float y;
        float radius;
        float walkDirection;
        float turnDirection;
        float moveSpeed;
        float rotationSpeed;
        float rotationAngle;

        Player() {
            this.x = windowWidth / 2;
            this.y = windowHeight / 2;
            this.radius = 10;
            this.walkDirection = 0;
            this.turnDirection = 0;
            this.moveSpeed = 2.0F;
            this.rotationSpeed = (2 * (PI / 180));
            this.rotationAngle = (PI / 2);
        }

        public void updatePlayer(){
            this.rotationAngle += this.turnDirection * this.rotationSpeed;
            float step = this.walkDirection * this.moveSpeed;
            float tempX = this.x + cos(rotationAngle) * step;
            float tempY = this.y + sin(rotationAngle) * step;
            if (! grid.hasWall(tempX, tempY)){
                this.x = tempX;
                this.y = tempY;
            }
        }

        public void printPlayer(){
            noStroke();
            fill(128, 0, 0);
            circle(x, y, radius);
            //stroke(128, 0, 0);
            //line(this.x, this.y, this.x + cos(this.rotationAngle) * 30, this.y + sin(this.rotationAngle) * 30);
        }
    }

    class Ray {
        private boolean isRayFacingLeft;
        private boolean isRayFacingRight;
        private boolean isRayFacinUP;
        private float rayAngle;
        private boolean isRayFacingDown;
        private float wallHitX;
        private float wallHitY;
        private float distance;

        Ray(float rayAngle){
            this.rayAngle = normalizeAngle(rayAngle);
            this.wallHitX = 0;
            this.wallHitY = 0;
            this.distance = 0;

            this.isRayFacingDown = this.rayAngle > 0 && this.rayAngle < PI;
            this.isRayFacinUP = !this.isRayFacingDown;

            this.isRayFacingRight = this.rayAngle < 0.5 * PI || this.rayAngle > 1.5 * PI;
            this.isRayFacingLeft = !this.isRayFacingRight;
        }

        public void printRay(){
            float rayLength = 30;
            //stroke(160, 160, 160, (float) 0.0);
            stroke(160,160,160,100);
            line(player.x, player.y, player.x + cos(this.rayAngle) * rayLength, player.y + sin(this.rayAngle) * rayLength);
        }

        public void cast(int columnID){
           float xStep; float yStep;
           float xIntersect; float yIntersect;
           //horizontal intersections
           yIntersect = floor(player.y / tileSize) * tileSize;
           yIntersect += this.isRayFacingDown ? tileSize : 0;
           xIntersect = player.x + (yIntersect - player.y) / tan(this.rayAngle);

           yStep = tileSize;
           yStep *= this.isRayFacinUP ? -1 : 1;
           xStep = tileSize / tan(this.rayAngle);
           xStep *= (this.isRayFacingLeft && xStep > 0) ? -1 : 1;
           xStep *= (this.isRayFacingRight && xStep < 0) ? -1 :1;
        }

    }

    public void castRays(){
        rays = new Vector<>();
        int columnID = 0;
        float rayAngle = player.rotationAngle - (FOVangle / 2);
        //for (int i = 0; i < numOfRays; i++){
        for (int i = 0; i < 1; i++){
            Ray ray = new Ray(rayAngle);
            //rays[i] = ray;
            ray.cast(columnID);
            rays.add(ray);

            rayAngle += FOVangle / numOfRays ;
            columnID++;
        }
    }


    public float distance(PVector a, PVector b){
        return (float) sqrt(pow(a.x - b.x, 2) + pow(a.y - b.y, 2));
    }

    public float normalizeAngle(float angle){
        angle = angle % (2 * PI);
        if(angle < 0){
           angle = (2 * PI) + angle;
        }
        return angle;
    }

    public float deg2rad(float degrees) {
        return degrees * (PI / 180);
    }

    public void keyPressed(){
            if (keyCode == UP){
               player.walkDirection = +1;
            }
            else if (keyCode == DOWN){
                player.walkDirection = -1;
            }
            else if (keyCode == LEFT){
                player.turnDirection = -1;
            }
            else if (keyCode == RIGHT){
                player.turnDirection = +1;
            }
    }

    public void keyReleased(){
            if (keyCode == UP){
                player.walkDirection = 0;
            }
            else if (keyCode == DOWN){
                player.walkDirection = 0;
            }
            else if (keyCode == LEFT){
                player.turnDirection = 0;
            }
            else if (keyCode == RIGHT){
                player.turnDirection = 0;
            }
    }

    public static void main(String... args){
        PApplet.main("RayCasting2D");
    }
}

