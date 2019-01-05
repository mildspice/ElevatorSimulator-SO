package modulos;

import enums.EstadosPortas;
import tools.MonitorElevador;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;

/**
 * <h1>Sub-módulo - Portas</h1>
 * <p>
 * <b>- Thread de simulação do funcionamento automático das portas do
 * elevador.</b>
 * </p>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public class Portas extends Thread {

    private JFrame guiFrame;
    private JTextField displayEstado;
    private JTextArea displayPretty;

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

    /**
     * <b>Método responsável pelo funcionamento da thread.</b>
     * <p>
     * DEVELOPER NOTE: Novamente, as operações da thread estão dentro de um
     * ciclo que quebrará quando a thread for interrompida.
     * </p>
     */
    @Override
    public void run() {
        this.criarJanela();
        displayEstado.setText(monitor.getEstadoPortas().message());
        displayPretty.setText(monitor.getEstadoPortas().prettyDisplay());
        try {
            while (!Thread.interrupted()) {
                //este semaforo serve para que o ciclo nao esteja a correr constantemente
                //as portas funcionam só quando é sinalizado
                semaforoPortas.acquire();

                //LOG
                EstadosPortas temp = monitor.getEstadoPortas();

                /*
                NOTAS: 
                - quando o botao S é utilizado, o elevador está parado mas em 
                princípio não estará parado num piso!
                - para que seja possível utilizar os botoes das portas é preciso
                que o elevador esteja completamente parado!
                 */
                if (!monitor.isEmFuncionamento() && monitor.isFloorReached()
                        || monitor.isBotaoPortasClicked()
                        && monitor.getBotaoPortas().equals(EstadosPortas.ABERTO)) {
                    monitor.setEstadoPortas(EstadosPortas.ABERTO);

                    //quando o elevador foi parado manualmente e o utilizador
                    //decide abrir as portas
                    if (!monitor.isFloorReached()) {
                        monitor.printWarning("\nWARNING - "
                                + "O elevador nao se encontra num piso!?! \n"
                                + "Voce pode cair!! Ai meu deus ...!", true);
                    }

                } else if (monitor.isEmFuncionamento()
                        || monitor.isBotaoPortasClicked()
                        && monitor.getBotaoPortas().equals(EstadosPortas.FECHADO)) {
                    monitor.setEstadoPortas(EstadosPortas.FECHADO);
                }

                displayEstado.setText(monitor.getEstadoPortas().message());
                displayPretty.setText(monitor.getEstadoPortas().prettyDisplay());
                monitor.resetBotaoPortas();
                
                //LOG
                if (temp != monitor.getEstadoPortas()) {
                    monitor.counterPortas();
                    monitor.writeLog(Thread.currentThread(), monitor.getEstadoPortas());
                }
            }

            this.guiFrame.dispose();
        } catch (InterruptedException ex) {
            this.guiFrame.dispose();
        }
    }

    /**
     * Cria o JFrame relativo ao sub-módulo Portas onde estará presente
     * informação relativa ao estado das portas.
     */
    private void criarJanela() {
        this.guiFrame = new JFrame();

        guiFrame.setTitle("[ Display Portas ]");
        guiFrame.setLocationByPlatform(true);
        //evita o fecho normal da janela. Obriga o uso do botão EXIT
        guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel estadoPortas = new JPanel();
        JLabel label = new JLabel("{Estado Portas}");
        this.displayEstado = new JTextField();
        displayEstado.setEditable(false);
        displayEstado.setPreferredSize(new Dimension(150, 50));
        displayEstado.setHorizontalAlignment(JTextField.CENTER);
        estadoPortas.add(label);
        estadoPortas.add(this.displayEstado);

        JPanel pretty = new JPanel();
        this.displayPretty = new JTextArea(23, 15);
        displayPretty.setEditable(false);
        displayPretty.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayPretty);
        scrollPane.setPreferredSize(new Dimension(250, 300));
        pretty.add(scrollPane);

        guiFrame.add(estadoPortas, BorderLayout.NORTH);
        guiFrame.add(pretty, BorderLayout.SOUTH);

        guiFrame.pack();
        guiFrame.setVisible(true);
    }
}
