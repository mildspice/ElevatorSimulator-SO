package modulos;

import enums.EstadosPortas;
import tools.MonitorElevador;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * <b>Sub-módulo Portas - thread de simulação do funcionamento automático das
 * portas do elevador.</b>
 */
public class Portas extends Thread {

    private JFrame guiFrame;
    private JTextField displayDirecao;
    private JTextPane displayEffects;

    protected MonitorElevador monitor;
    protected Semaphore semaforoPortas;

    /**
     * Construtor para a thread.
     *
     * @param semaforo semaforo relacionado ao funcionamento das portas.
     * @param monitor objeto partilhado
     */
    public Portas(Semaphore semaforo, MonitorElevador monitor) {
        this.semaforoPortas = semaforo;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            this.criarJanela();

            while (!Thread.interrupted()) {
                //este semaforo serve para que o ciclo nao esteja a correr constantemente
                //as portas funcionam só quando é sinalizado
                semaforoPortas.acquire();

                if (monitor.isFloorReached() && !monitor.isEmFuncionamento()) {
                    monitor.setEstadoPortas(EstadosPortas.ABERTO);

                } else {
                    monitor.setEstadoPortas(EstadosPortas.FECHADO);
                }

            }

            this.guiFrame.dispose();
        } catch (InterruptedException ex) {
            this.guiFrame.dispose();
        }
    }

    private void criarJanela() {
        /* FAZER A JANELA DAS PORTAS
        this.guiFrame = new JFrame();

        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("[ Motor ]");
        guiFrame.setSize(200, 300);
        guiFrame.setLocationByPlatform(true);

        JPanel estadoMotor = new JPanel();
        JLabel label = new JLabel("Direcao Motor:");
        this.displayDirecao = new JTextField();
        displayDirecao.setPreferredSize(new Dimension(150, 50));
        displayDirecao.setHorizontalAlignment(JTextField.CENTER);
        estadoMotor.add(label);
        estadoMotor.add(this.displayDirecao);

        JPanel effects = new JPanel();
        this.displayEffects = new JTextPane();
        this.displayEffects.setPreferredSize(new Dimension(150, 150));
        StyledDocument doc = displayEffects.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        effects.add(this.displayEffects);

        guiFrame.add(estadoMotor, BorderLayout.NORTH);
        guiFrame.add(effects, BorderLayout.SOUTH);

        guiFrame.setVisible(true);
        */
    }
}
