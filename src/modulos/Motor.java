package modulos;

import enums.EstadosMotor;
import tools.MonitorElevador;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * <h1>Sub-módulo - Motor</h1>
 * <p>
 * <b>- Thread que vai simular o controlo e monitorização do motor do
 * elevador.</b>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public class Motor extends Thread {

    //variáveis da janela
    private JFrame guiFrame;
    private JTextField displayDirecao;
    private JTextArea displayEffects;

    private final int ITERATION_TIME = 200; //tempo relacionado à seta de display
    protected MonitorElevador monitor;
    protected Semaphore semaforoMotor;

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

    /**
     * <b>Método responsável pelo funcionamento da thread.</b>
     * <p>
     * NOTAS (estão espalhados ao longo do código vários comentários
     * importantes, no entanto alguns vão ser colocados aqui por conveniência do
     * javadoc.):
     * </p>
     * <p>
     * - Quando o main lança a thread do motor, este fica à espera de um sinal
     * (através do uso de um semáforo) para que comece a funcionar.
     * </p><p>
     * Repare-se que só se faz um acquire(), nao se faz um release(), para que
     * na proxima iteração o semaforo esteja com 0 permições novamente.
     * </p>
     * <p>
     * {@code while (!this.monitor.isFloorReached()) { ...
     * if (!monitor.isEmFuncionamento()) {
     * monitor.espera();
     * } ... }} - Este ciclo representa o motor que se mantém a funcionar até
     * que seja sinalizado a chegada ao destino, isto é, sinalizado pelo botão
     * específico da botoneira ("if" dentro do ciclo). No entanto, também vai
     * imprimindo na janela uma sinalização do funcionamento do motor.
     * </p>
     */
    @Override
    public void run() {
        try {
            this.criarJanela();
            monitor.setDirecaoMotor(EstadosMotor.STOPPED);
            displayDirecao.setText(monitor.getDirecaoMotor().message());
            displayEffects.setText(monitor.getDirecaoMotor().prettyDisplay()[0]);

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
                semaforoMotor.acquire();

                //faz display na janela da direção atual do motor
                displayDirecao.setText(monitor.getDirecaoMotor().message());

                //tempo de espera até que o elevador comece a andar
                Thread.sleep(monitor.MOVEMENT_WAITING_TIME);
                //sinalização
                monitor.setFlagFuncionamento(true);
                monitor.acordaTodas();

                /**
                 * Este ciclo representa o motor que se mantém a funcionar até
                 * que seja sinalizado a chegada ao destino, isto é, sinalizado
                 * pelo botão específico da botoneira ("if" dentro do ciclo). No
                 * entanto, também vai imprimindo na janela uma sinalização do
                 * funcionamento do motor.
                 */
                int i = 0;
                while (!this.monitor.isFloorReached()) {
                    //reset da sinalização
                    if (i == monitor.getDirecaoMotor().prettyDisplay().length) {
                        i = 0;
                    }
                    Thread.sleep(ITERATION_TIME);
                    displayEffects.setText(monitor.getDirecaoMotor().prettyDisplay()[i]);
                    i++;

                    //Quando o motor fôr parado manualmente ...
                    if (!monitor.isEmFuncionamento()) {
                        //guarda o estado anterior temporariamente
                        EstadosMotor temp = monitor.getDirecaoMotor();
                        //pára o motor
                        monitor.setDirecaoMotor(EstadosMotor.STOPPED);
                        displayDirecao.setText(monitor.getDirecaoMotor().message());
                        displayEffects.setText("\n\n\n\n* Elevador parado manualmente! *\n");

                        monitor.espera();

                        //volta ao estado anterior
                        monitor.setDirecaoMotor(temp);
                        displayDirecao.setText(monitor.getDirecaoMotor().message());
                    }
                    Thread.sleep(ITERATION_TIME);
                }

                //chegada ao destino, paragem do elevador
                monitor.setDirecaoMotor(EstadosMotor.STOPPED);
                displayDirecao.setText(monitor.getDirecaoMotor().message());
                displayEffects.setText(monitor.getDirecaoMotor().prettyDisplay()[0]);
                monitor.setFlagFuncionamento(false);
                monitor.acordaTodas();
            }

            this.guiFrame.dispose();
        } catch (InterruptedException ex) {
            this.guiFrame.dispose();
        }
    }

    /**
     * Cria o JFrame relativo ao sub-módulo Motor onde estará presente a
     * informação relativa ao estado direcional do elevador.
     */
    private void criarJanela() {
        this.guiFrame = new JFrame();

        guiFrame.setTitle("[ Display Motor ]");
        guiFrame.setLocationByPlatform(true);
        //evita o fecho normal da janela. Obriga o uso do botão EXIT
        guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel estadoMotor = new JPanel();
        JLabel label = new JLabel("{Direcao Motor}");
        this.displayDirecao = new JTextField();
        displayDirecao.setEditable(false);
        displayDirecao.setPreferredSize(new Dimension(150, 50));
        displayDirecao.setHorizontalAlignment(JTextField.CENTER);
        estadoMotor.add(label);
        estadoMotor.add(this.displayDirecao);

        JPanel effects = new JPanel();
        this.displayEffects = new JTextArea();
        displayEffects.setEditable(false);
        displayEffects.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayEffects);
        scrollPane.setPreferredSize(new Dimension(250, 300));
        effects.add(scrollPane);

        guiFrame.add(estadoMotor, BorderLayout.NORTH);
        guiFrame.add(effects, BorderLayout.SOUTH);

        guiFrame.pack();
        guiFrame.setVisible(true);

    }
}
