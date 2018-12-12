package modulos;

import java.util.Scanner;
import tools.MonitorElevador;

/**
 * O problema com a botoneira é o facto de ter que ser possivel fazer inputs
 * enquanto o elevador está a funcionar, sem o interromper...
 *
 * Talvez, podemos fazer uma segunda janela que vai aceitando inputs....
 * 
 */
public class Botoneira extends Thread{
    
    protected MonitorElevador monitor;
    Scanner scanner;
    
    public Botoneira(MonitorElevador monitor) {
        this.monitor = monitor;
        scanner = new Scanner(System.in);
    }
    
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            
        }
    }
}
