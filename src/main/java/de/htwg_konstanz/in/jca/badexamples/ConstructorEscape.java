package de.htwg_konstanz.in.jca.badexamples;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class EvilListener extends Thread {  
    private BlockingQueue<ConstructorEscape> queue = new ArrayBlockingQueue<ConstructorEscape>(1);
	 
    @Override public void run() {
	ConstructorEscape ce = null;
	try {
	    ce = queue.take();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	System.out.println(ce.getState());
    }
    
    void register(ConstructorEscape ce) {
	queue.add(ce);
    }
}


public class ConstructorEscape {
    private int state;
    
    public ConstructorEscape(EvilListener listener) throws InterruptedException {
	this.state = 42;
	listener.register(this);
    }

    int getState() {
	return state;
    }
    
    public static void main(String[] args) throws InterruptedException {
	EvilListener listener = new EvilListener();
	listener.start();
	new ConstructorEscape(listener);
    }

 

}
