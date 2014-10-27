package multicastcanvas;

import java.awt.Color;
import java.util.Random;

public class Player {
    private String _name;
    private int _x;
    private int _y;
    private Color _color;
    private Random _r;
    
    public Player(String name, int value_x, int value_y){
        _name = name;
        _x = value_x;
        _y = value_y;
        _color = new Color(Randomize(), Randomize(), Randomize(), 255);
    }
    
    public Player(String name, int value_x, int value_y, Color c){
        _name = name;
        _x = value_x;
        _y = value_y;
        _color = c;
    }
    
    public String getName(){
        return _name;
    }
    
    public int getX(){
        return  _x;
    }
    
    public int getY(){
        return  _y;
    }
    
    public Color getColor(){
        return _color;
    }
    
    public void setName(String n){
        _name = n;
    }
    
    public void setX(int n){
        _x = n;
    }
    
    public void setY(int n){
        _y = n;
    }
    
    public void setColor(Color c){
        _color = c;
    }
    
    public void newColor(Color c){
        _color = new Color((_color.getRed()+c.getRed())/2, (_color.getGreen()+c.getGreen())/2, (_color.getBlue()+c.getBlue())/2, 255);
    }
    
    public int Randomize(){
        _r = new Random();
        int value = _r.nextInt();
        
        if(value < 0)
            value = value*-1;
        
        return value%256;
    }
}
