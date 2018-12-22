package modulos;

import enums.EstadosMotor;
import tools.MonitorElevador;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * <b>Sub-módulo Motor - thread que vai simular o controlo e monitorização do
 * motor do elevador.</b>
 */
public class Motor extends Thread {

    //variáveis da janela
    private JFrame guiFrame;
    private JTextField displayDirecao;
    private JTextPane displayEffects;

    private final int ITERATION_TIME = 200;
    protected MonitorElevador monitor;
    protected Semaphore semaforoMotor;

    //isto parece uma confusão desgraçada, mas é basicamente um array com
    //Strings "crescentes" que no fim fazem uma setinha (só perco tempo com isto ...).
    private final String[] prettyDown = {
        "     ||   ||\n     ||   ||\n     ||   ||\n\\\\   ||   ||   //\n \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "     ||   ||\n     ||   ||\n\\\\   ||   ||   //\n \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "     ||   ||\n\\\\   ||   ||   //\n \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "\\\\   ||   ||   //\n \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        " \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "     \\\\   //\n       \\\\//",
        "       \\\\//"};
    private final String[] prettyUp = {
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n//   ||   ||   \\\\\n     ||   ||\n     ||   ||\n     ||   ||",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n//   ||   ||   \\\\\n     ||   ||\n     ||   ||",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n//   ||   ||   \\\\\n     ||   ||",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n//   ||   ||   \\\\",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\",
        "       //\\\\\n     //   \\\\",
        "       //\\\\"};

    /**
     * Construtor para a thread.
     *
     * @param semaforoMotor semaforo relacionado ao funcionamento do elevador.
     * @param monitor objeto partilhado
     */
    public Motor(Semaphore semaforoMotor, MonitorElevador monitor) {
        this.semaforoMotor = semaforoMotor;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        this.criarJanela();
        try {
            while (!Thread.interrupted()) {
                /**
                 * Quando o main lança a thread do motor, este fica à espera de
                 * um sinal (através do uso do semáforo) para que comece a
                 * funcionar.
                 *
                 * repare-se que só se faz um acquire(), nao se faz um
                 * release(), para que na proxima iteração o semaforo esteja com
                 * 0 permições novamente
                 */
                this.semaforoMotor.acquire();

                //faz display na janela da direção atual do motor
                displayDirecao.setText(monitor.getDirecaoMotor().message());

                //o motor começa primeiro que o "movimento do elevador" (-150)
                Thread.sleep(monitor.MOVEMENT_WAITING_TIME - 150);
                //sinalização
                monitor.setFlagFuncionamento(true);

                /**
                 * Este ciclo representa o motor que me mantém a funcionar até
                 * que seja sinalizado a chegada ao destino, ou seja sinalizado
                 * pelo botão específico da botoneira ("if" dentro do ciclo). No
                 * entanto, também vai imprimindo na janela uma sinalização do
                 * funcionamento do motor.
                 */
                int i = 0;
                while (!this.monitor.isFloorReached()) {
                    //reset da sinalização
                    if (i == prettyUp.length) {
                        i = 0;
                    }
                    Thread.sleep(ITERATION_TIME);
                    if (monitor.getDirecaoMotor() == EstadosMotor.CIMA) {
                        displayEffects.setText(prettyUp[i]);
                    } else {
                        displayEffects.setText(prettyDown[i]);
                    }
                    i++;

                    //Quando o motor fôr parado manualmente ...
                    if (!monitor.isEmFuncionamento()) {
                        EstadosMotor temp = monitor.getDirecaoMotor();
                        monitor.setDirecaoMotor(EstadosMotor.STOPPED);
                        displayDirecao.setText(monitor.getDirecaoMotor().message());
                        displayEffects.setText("\n\n\n\n* Elevador parado manualmente! *\n");

                        monitor.espera();

                        monitor.setDirecaoMotor(temp);
                        displayDirecao.setText(monitor.getDirecaoMotor().message());
                    }
                    Thread.sleep(ITERATION_TIME);
                }

                //pára o motor
                monitor.setDirecaoMotor(EstadosMotor.STOPPED);
                displayDirecao.setText(monitor.getDirecaoMotor().message());
                monitor.setFlagFuncionamento(false);
            }
            this.guiFrame.dispose();
        } catch (InterruptedException ex) {
            this.guiFrame.dispose();
        }
    }

    private void criarJanela() {
        this.guiFrame = new JFrame();

        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("[ Motor ]");
        /**
         * Design Note: An alternative option for setting the size of the window
         * is to call the pack() method of the JFrame class. This method
         * calculates the size of the window based on the graphical components
         * it contains. Because this sample application doesn't need to change
         * its window size, we'll just use ​the setSize() method.
         */
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
    }
}
