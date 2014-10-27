package multicastcanvas;


public class SimpleThread extends Thread{

    private MulticastCanvas janela;
    
    public SimpleThread(MulticastCanvas window){
        super();
        janela = window;
    }
    @Override
    public void run(){
        try{
            while(true){
                janela.recvMsg();
            }
        }catch(Exception e){
            this.interrupt();
        }
    }
    
}
