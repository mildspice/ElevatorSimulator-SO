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
            /*
            janela com os scans (int para os pisos e caracter para os especiais)
            [os números correspondem ao índice do array que está no monitor...]
            */
        }
    }
}
