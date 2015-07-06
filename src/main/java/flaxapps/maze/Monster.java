package flaxapps.maze;

import java.util.ArrayList;

import com.flaxapps.joglutil.AnimationHolder;
import com.flaxapps.joglutil.Vertex;
import com.jogamp.opengl.GL2;

/**
 *
 * @author Danny
 */

public class Monster {
    static final int STAGE_FOLLOW = 0;
    static final int STAGE_PASSIVE = 1;
    
    
    int stage = 1;
    AnimationHolder walkAnimation;
    float oangle;
    public Vertex position;
    boolean isMoving;
    public float speed;
    public float rangle = 0;
    ArrayList<Vertex> currentPoints;
    float[] hls = {100.0f, 100.0f, 100.0f};
    
    public float[] calcBounds(ArrayList<Vertex> a, boolean speedy){
   
      
        
        /*
        boolean first = true;
        for(int i = 0; i<a.size(); i++){
            Vertex v = a.get(i);
            v.x*=.25;
            v.y*=.25;
            v.z*=.25;
            if(first){
                first = false;
                max_x = v.x;
                min_x = v.x;
                max_z = v.z;
                min_z = v.z;
            }
            else{
                if(v.x>max_x){
                    max_x = v.x;
                }
                if(v.x<min_x){
                    min_x = v.x;
                }
                if(v.z>max_z){
                    max_z = v.z;
                }
                if(v.z<min_z){
                    min_z = v.z;
                }
            }
            
            
        }
        */
        
        
        Math.sin(rangle);
        
       if(speedy){
      //  float[] h = {Math.abs((float)Math.cos(rangle)*2.0f) + Math.abs((float)Math.sin(rangle)*0.2f), 2.0f, Math.abs((float)Math.sin(rangle)*2.0f) + Math.abs((float)Math.cos(rangle)*0.2f)};
      float[] h = {Math.abs((float)Math.cos(rangle)*2.0f) + Math.abs((float)Math.sin(rangle)*0.8f), 2.0f, Math.abs((float)Math.sin(rangle)*2.0f) + Math.abs((float)Math.cos(rangle)*0.8f)};
        return h;
      
    }
       else{
           float[] h = {Math.abs((float)Math.cos(rangle)*2.0f) + Math.abs((float)Math.sin(rangle)*2.0f), 2.0f, Math.abs((float)Math.sin(rangle)*2.0f) + Math.abs((float)Math.cos(rangle)*2.0f)};

           return h;
       }
        
    }
    
    
    public Monster(AnimationHolder wa, Vertex pos){
        walkAnimation = wa;
        position = pos;
    }
    public void start(){
        isMoving = true;
    }
    
    public boolean spotsPerson(PlatformMaze main){
      
      
     
     
     Vertex p = new Vertex(position.x*.25f,3.0f,position.z*.25f);
     boolean as = true;
     int cap = 0;
     while(as){
         //Speed = 3.0
         /* p.x += (float)Math.sin(rangle) * .3;    
          p.z += (float)Math.cos(rangle) * .3 */
         p.x += (float)Math.sin(rangle) * (speed - 1.5);    
          p.z += (float)Math.cos(rangle) * (speed - 1.5);
         // System.out.println("X: " + p.x + " Y: " + p.y + "Z: " + p.z);
         System.out.println("SPOTS");
          if(main.hitsWall(p,hls)){
             as = false;
          }
          if(cap>50){
             as = false;
          }
          cap++;
     }   
      
     Vertex mp = new Vertex(position.x*.25f,3.0f,position.z*.25f);
     Vertex ap = new Vertex(main.posX,3.0f,main.posZ);
     float d1 = distance(mp,ap);
             //(float)Math.sqrt(Math.pow(mp.x - ap.x,2) + Math.pow(mp.y - ap.y,2) + Math.pow(mp.z - ap.z,2));
     //Distance from dude to human
     float d2 = distance(mp,p);
     
             //(float)Math.sqrt(Math.pow(mp.x - p.x,2) + Math.pow(mp.y - p.y,2) + Math.pow(mp.z - p.z,2));
     //Distance from dude to next collision
   
     //System.out.println(p.x + " " + p.y + " " + p.z);
     if(d1>d2){
        //Dude will collide before hitting person, cannot see person
      //  System.out.println("Can't ee");
         return false;
     }
     
     //does = false;
     return true;
    }
    
    public void stop(){
        isMoving = false;
    }
    
    public float distance(Vertex mp, Vertex ap){
        return (float)Math.sqrt(Math.pow(mp.x - ap.x,2) + Math.pow(mp.y - ap.y,2) + Math.pow(mp.z - ap.z,2));
    }
    
    public void act(GL2 gl, PlatformMaze main){
        if(isMoving){
            System.out.println("ACT");
           float d = distance(new Vertex(main.posX,3.0f,main.posZ),new Vertex(0.25f*position.x,3.0f,.25f*position.z));
                   
                        //System.out.println("D: "+d);
                        
                        float xa = (float) ((.25*position.x) - main.posX);
                     float za = (float) (.25*position.z - main.posZ);
                     float ra = (float)(xa/za);
                     float a = (float) Math.atan(ra);
                    if(za>0.0){
                         a = (float) (Math.toRadians(180) + a);
                    } 
                        
                if(stage == STAGE_FOLLOW){
                   if(d>=30.0){
                       stage = STAGE_PASSIVE;
                   }
                           
                    
                    if(d<4.0){
                        stop();
                    }
                    
                  
                       
                    
                    rangle = a;
    
                    boolean animates = true;

                   if(!searchAndSeize(10.0f,180.0f,main)){
                       
                   
                                         
                                         animates = false;
                                        stage = STAGE_PASSIVE;

                    }

                    if(!animates){
                    // position.x -= (float)Math.sin(rangle) * speed;
                    //position.z -= (float)Math.cos(rangle) * speed;
                    hls = this.calcBounds(walkAnimation.drawStillFrame(position, gl, rangle),true);
                    } 
                    else{
                        position.x += (float)Math.sin(rangle) * speed;
                        position.z += (float)Math.cos(rangle) * speed;

                    hls = this.calcBounds(walkAnimation.drawFrame(position, gl, rangle),true);
                    }

                }
                else if(stage == STAGE_PASSIVE){
                    boolean spots = true;
                    //if(Math.abs(a - rangle)%Math.toRadians(360)<Math.toRadians(60)){
                       spots = this.searchAndSeize(10.0f, 180.0f, main);
                   /*
                    }
                    else{
                        //System.out.println("ASBS" + Math.toDegrees(Math.abs(a - rangle))%360);
                        spots = false;
                    }
                    */
                       
                    if(spots){
                        stage = STAGE_FOLLOW;
                    }
                    
                        if(d<=1.0f){
                        stage = STAGE_FOLLOW;
                        
                        }
                        else{
                    
                    position.x += (float)Math.sin(rangle) * (speed);
                        position.z += (float)Math.cos(rangle) * (speed);
                    if(main.hitsWall(new Vertex(position.x*.25f,3.0f,position.z*.25f), hls)){
                        position.x -= (float)Math.sin(rangle) * (speed);
                        position.z -= (float)Math.cos(rangle) * (speed);
                      
                        rangle = (float) ((float) (Math.toRadians(45) + (rangle)));// + Math.toRadians(r.nextInt(radius * 2) - radius));
                        hls = this.calcBounds(walkAnimation.drawStillFrame(position, gl, rangle),false);
                        
                    }
                    else{
                        
                        
                        hls = this.calcBounds(walkAnimation.drawFrame(position, gl, rangle),false);
                        
                    
                    }
                        }
                }
                
                
                
        }
        else{
        
            hls = this.calcBounds(walkAnimation.drawStillFrame(position, gl, rangle),true);
        }
    }
    
    
    
    public boolean searchAndSeize(float interval, float max, PlatformMaze main){
        
        oangle = rangle;
        for(int i = 0; i<=(max/interval); i++){
            
            rangle = oangle + (float)Math.toRadians(interval * i);
            
            if(this.spotsPerson(main)){
                return true;
            }
            
            rangle = oangle - (float)Math.toRadians(interval * i);
            
            if(this.spotsPerson(main)){
                return true;
            }
            System.out.println("S&S");
        }
        return false;
        
    }
    
}
