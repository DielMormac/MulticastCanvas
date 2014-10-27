package multicastcanvas;

import java.awt.Color;
import java.net.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MulticastCanvas extends JPanel{
    private static MulticastSocket working_socket;
    static final int MULTICAST_PORT = 4446;
    static final String MULTICAST_IP_ADDRESS = "230.0.0.1";
    InetAddress group = null;
    
    private static SimpleThread thread;
    
    private static JFrame frame;
    private static ArrayList<Player> list = new ArrayList<Player>();
    private static String lineCommand;
    private static boolean isRuning = true;
    
    private static Color CC;
    
    private static int fps = 45;
    
    MulticastCanvas() {
        this.addKeyListener(new KeyAdapter()  {
        public void keyPressed(KeyEvent e){
                MoveObject(e);
            }
        });
        
        try {
            working_socket = new MulticastSocket(MULTICAST_PORT);
            group = InetAddress.getByName(MULTICAST_IP_ADDRESS);
            working_socket.joinGroup(group);
        } catch (Exception err) { 
            JOptionPane.showMessageDialog(null, err.getMessage(), null, 
            JOptionPane.PLAIN_MESSAGE , null);
        }
    }
    public void sendMsg(byte[] msg) {
        try {
            InetAddress address = InetAddress.getByName(MULTICAST_IP_ADDRESS);
            DatagramPacket packet = new DatagramPacket(msg, msg.length, address, MULTICAST_PORT);
            working_socket.send(packet);
        } catch (Exception err) {
            JOptionPane.showMessageDialog(null, err.getMessage(), null, 
            JOptionPane.PLAIN_MESSAGE , null);
        }
    }

    public byte[] recvMsg() {
        byte[] msg = new byte[100];
        try {
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            working_socket.receive(packet);
            msg = packet.getData();
            String str = new String(msg);
            String[] rcv = str.trim().split(";");
            addUserInList(rcv[0], Integer.parseInt(rcv[1]), Integer.parseInt(rcv[2]), new Color(Integer.parseInt(rcv[3]),Integer.parseInt(rcv[4]), Integer.parseInt(rcv[5]), 255));
        } catch (Exception err) {
            JOptionPane.showMessageDialog(null, err.getMessage(), null, 
            JOptionPane.PLAIN_MESSAGE , null);
        }
        return msg;
    }
    
    private int getUserInList(String n){
        if(!list.isEmpty()){
            for(int i = 0; i < list.size(); i++){
                if(n.equals(list.get(i).getName()))
                    return i;
            }
        }
        return -1;
    }
    
    public void addUserInList(String n, int x, int y, Color c){
        if(x == -10 && y == -10){
            deleteUser(n);
        }
        else{
            int index = getUserInList(n);
            if(index == -1){
                list.add(new Player(n, x, y, c));
            }
            else{
                list.get(index).setX(x);
                list.get(index).setY(y);
                list.get(index).setColor(c);
            }
        }
    }
    public void deleteUser(String n){
        if(!list.isEmpty()){
            for(int i = 0; i < list.size(); i++){
                if(n.equals(list.get(i).getName())){
                    list.remove(i);
                }
            }
        }
    }
    void close(){
        try {
            working_socket.leaveGroup(group);
            working_socket.close();
        } catch (Exception err) {
            JOptionPane.showMessageDialog(null, err.getMessage(), null, 
            JOptionPane.PLAIN_MESSAGE , null);
        }
    }
    public void MoveObject(KeyEvent e) {
        int keyCode = e.getKeyCode();		 
        int offset = 5;
        switch( keyCode ) { 
            case KeyEvent.VK_UP:
                list.get(0).setY(list.get(0).getY()-offset);
                break;
            case KeyEvent.VK_DOWN:
                list.get(0).setY(list.get(0).getY()+offset);
                break;
            case KeyEvent.VK_LEFT:
                list.get(0).setX(list.get(0).getX()-offset);
                break;
            case KeyEvent.VK_RIGHT :
                list.get(0).setX(list.get(0).getX()+offset);
                break;
            case KeyEvent.VK_ESCAPE :
                if(isRuning){
                    isRuning = false;
                    lineCommand = new String(list.get(0).getName()+";-10;-10;0;0;0");
                    sendMsg(lineCommand.getBytes());
                    thread.interrupt();
                    setVisible(false);
                    frame.setVisible(false);
                    JOptionPane.showMessageDialog(null, "Você Saiu da sala!", "Fim", JOptionPane.PLAIN_MESSAGE , null);
                    System.exit(0);
                }
                break;
        }
    }

    @Override
    public void paint(Graphics g) {
        requestFocus();
        g.clearRect(0, 0, getWidth(), getHeight());
        for(int i = 0; i < list.size(); i++){
            g.setColor(list.get(i).getColor());
            g.fillRect(list.get(i).getX()-10, list.get(i).getY()-10, 20, 20);
        }
        checkArea();
        checkColision();
        lineCommand = new String(list.get(0).getName()+";"+String.valueOf(list.get(0).getX())+";"+String.valueOf(list.get(0).getY())+";"+String.valueOf(list.get(0).getColor().getRed())+";"+String.valueOf(list.get(0).getColor().getGreen())+";"+String.valueOf(list.get(0).getColor().getBlue()));
        sendMsg(lineCommand.getBytes());
        repaint();
    }
    
    public void checkColision(){
        if(list.size() > 1){
            for(int i = 1; i < list.size(); i++){
                if(list.get(i).getX() >= list.get(0).getX()-20 && list.get(i).getX() <= list.get(0).getX()+20){
                    if(list.get(i).getY() >= list.get(0).getY()-20 && list.get(i).getY() <= list.get(0).getY()+20){
                        list.get(0).newColor(list.get(i).getColor());
                        list.get(i).newColor(list.get(0).getColor());
                    }
                }
            }
        }
    }
    
    public void checkArea(){
        if(list.get(0).getX() > getWidth()){
            list.get(0).setX(0);
        }
        if(list.get(0).getX() < 0){
            list.get(0).setX(getWidth());
        }
        if(list.get(0).getY() > getHeight()){
            list.get(0).setY(0);
        }
        if(list.get(0).getY() < 0){
            list.get(0).setY(getHeight());
        }
    }
    public static void main(String[] args) {
            
        frame = new JFrame("Multicast Canvas - "+JOptionPane.showInputDialog("Informe o seu nome:"));
        JMenuBar menuBar = new JMenuBar();
        JMenu menuInfo = new JMenu("Info");
        menuInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuInfoMouseClicked(evt);
            }
        });
        JMenu menuCredits = new JMenu("Credits");
        menuCredits.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuCreditsMouseClicked(evt);
            }
        });
        menuBar.add(menuInfo);
        menuBar.add(menuCredits);
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        MulticastCanvas mCanvas = new MulticastCanvas();        
        frame.add(mCanvas);		
        frame.setSize(500, 500);

        Random rnd = new Random();
        list.add(new Player(Integer.toString(rnd.nextInt()), 250, 250));
        CC = JColorChooser.showDialog(frame,"Selecione uma cor",CC);
        if(CC == null)
            CC = Color.lightGray;
        list.get(0).setColor(CC);

        frame.setVisible(true);	

        thread = new SimpleThread(mCanvas);
        thread.start();
    }
    
    private static void menuInfoMouseClicked(MouseEvent evt) {
        JOptionPane.showMessageDialog(null, "-MOVA SEU BLOCO USANDO AS SETAS DIRECIONAIS\n"
                                            + "-PARA SAIR UTILIZE O BOTÃO [ESC]\n"
                                            + "-AO COLIDIR COM OUTRO USUÁRIO SUA COR SERÁ ALTERADA PARA A MÉDIA (RGB) DAS CORES ENTRES OS CUBOS COLIDIDOS\n",
                                            "Info", JOptionPane.PLAIN_MESSAGE , null);  
    }
    
    private static void menuCreditsMouseClicked(MouseEvent evt) {
        JOptionPane.showMessageDialog(null, "Centro Universitário Senac\n"
                                            + "Campus: Santo Amaro\n"
                                            + "Curso: Tecnologia em Jogos Digitais\n"
                                            + "Disciplina: Jogos em Rede\n"
                                            + "Alunos:\n"
                                            + "Marcos Antônio Marcon(Diel Mormac)\n",
                                            "Creditos", JOptionPane.PLAIN_MESSAGE , null);  
    }
}