package modulos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.concurrent.Semaphore;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import tools.MonitorElevador;

/**
 * <h1>Sub-módulo - Botoneira</h1>
 * <p>
 * <b>- Thread que vai simular o conjuto de botões que possibilitam o uso do
 * elevador.</b>
 * </p>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public class Botoneira extends Thread {

    //variáveis da janela
    private JFrame guiFrame;
    private JPanel botoesPisos;
    private JPanel botoesEspeciais;
    private JToggleButton chave;
    private JTextArea displayInput;

    protected MonitorElevador monitor;
    protected Semaphore semaforoBotoneira;
    //semaforo usado pelos botões das portas
    protected Semaphore semaforoPortas;

    /**
     * Construtor da thread.
     * <p>
     * <b>(Nota: parte do código do programa poderá estar feito dentro dos
     * próprios botões.)</b>
     * </p>
     *
     * @param semaforo semaforo relacionado a funções especiais da botoneira
     * @param monitor objeto partilhado
     * @param semaforoPortas semaforo relacionado ao funcionamento das portas.
     */
    public Botoneira(Semaphore semaforo, MonitorElevador monitor, Semaphore semaforoPortas) {
        this.semaforoBotoneira = semaforo;
        this.monitor = monitor;
        this.semaforoPortas = semaforoPortas;
    }

    /**
     * <b>Método responsável pelo funcionamento da thread.</b>
     * <p>
     * No entanto, as operações desta thread fazem-se principalmente nos botões,
     * sendo que neste método estariam funções mais específicas para o
     * funcionamento desses botões.
     * </p>
     */
    @Override
    public void run() {
        try {
            this.criarJanela();
            Semaphore temp = new Semaphore(0);
            /**
             * ainda não sei muito bem o que fazer com este ciclo, mas acho que
             * vai dar jeito. No fundo, vai ser para fazer operações especiais
             * em relação aos inputs ou para impedir o uso de botoes, ...
             */
            while (!Thread.interrupted()) {

                temp.acquire();

            }
            this.guiFrame.dispose();
        } catch (InterruptedException ex) {
            this.guiFrame.dispose();
        }
    }

    /**
     * Cria o JFrame relativo ao sub-módulo Botoneira onde estarão todos os
     * botões relativos ao funcionamento do elevador, tal como algumas
     * informações pertinentes.
     */
    private void criarJanela() {
        this.guiFrame = new JFrame();

        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("[ Botoneira ]");
        /**
         * Design Note: An alternative option for setting the size of the window
         * is to call the pack() method of the JFrame class. This method
         * calculates the size of the window based on the graphical components
         * it contains. Because this sample application doesn't need to change
         * its window size, we'll just use ​the setSize() method.
         */
        //guiFrame.setSize(500, 500);
        guiFrame.setLocationByPlatform(true);

        /**
         * AQUI FAZ DISABLE AO FECHO DO JFRAME NORMAL, OBRIGA A CLICAR NO EXIT!!!!!
         */
        guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        /**
         * Criação do painel que vai ter os botoes dos pisos
         */
        this.botoesPisos = new JPanel();
        botoesPisos.setPreferredSize(new Dimension(100, 100));
        //Criação do layout em grelha 2 por 2* (*4 pisos)
        botoesPisos.setLayout(new GridLayout(2, monitor.getNumPisos() / 2));
        for (String nomeBotao : monitor.getBotoesPisos()) {

            JButton botao = new JButton(nomeBotao);
            //faz override do método de Ação do botão 
            //(código que o botao vai correr...)
            botao.addActionListener(
                    (ActionEvent e) -> {
                        /* CÓDIGO DOS BOTÕES DOS PISOS ! */
                        displayInput.setText(nomeBotao);
                        if (monitor.isEmFuncionamento()) {
                            displayInput.append("\n- Destino adicionado a fila.");
                        }
                        monitor.addFloorToQueue(nomeBotao);
                        monitor.displayQueue();

                        //liberta uma permição só quando o elevador estiver
                        //em estado IDLE
                        if (monitor.getFloorQueue().isEmpty() && semaforoBotoneira.availablePermits() < 1
                        || !monitor.isEmFuncionamento() && semaforoBotoneira.availablePermits() < 1) {
                            semaforoBotoneira.release();
                        }
                    });
            //adiciona o botao a uma célula da grelha
            botoesPisos.add(botao);
        }

        //painel dos botoes especiais
        this.botoesEspeciais = new JPanel();
        botoesEspeciais.setLayout(new GridLayout(2, 2));
        botoesEspeciais.setPreferredSize(new Dimension(140, 100));
        //botoes das portas
        final String[] botoesPortas = {"A", "F"};
        for (String nomeBotao : botoesPortas) {
            JButton botao = new JButton(nomeBotao);
            if (nomeBotao.equals("A")) {
                botao.addActionListener(
                        (ActionEvent e) -> {
                            /* CÓDIGO DOS BOTÕES DAS PORTAS */
                            if (!monitor.isEmFuncionamento()) {
                                monitor.setBotaoPortas(true);
                                displayInput.setText(nomeBotao + " - Aberto");
                            } else {
                                monitor.printWarning("Elevador a funcionar!", false);
                            }

                            semaforoPortas.release();
                        });
                botao.setToolTipText("Botao para abertura manual das portas.");
            } else if (nomeBotao.equals("F")) {
                botao.addActionListener(
                        (ActionEvent e) -> {
                            /* CÓDIGO DOS BOTÕES DAS PORTAS */
                            if (!monitor.isEmFuncionamento()) {
                                monitor.setBotaoPortas(false);
                                displayInput.setText(nomeBotao + " - Fechado");
                            } else {
                                monitor.printWarning("Elevador a funcionar!", false);
                            }

                            semaforoPortas.release();
                        });
                botao.setToolTipText("Botao para fecho manual das portas.");
            }
            botoesEspeciais.add(botao);
        }
        //botao de stop e chave
        JButton botaoStop = new JButton("S");
        botaoStop.setToolTipText("Utilize este botao para parar ou acionar "
                + "o movimento do elevador quando desejado!");
        botaoStop.addActionListener(
                (ActionEvent e) -> {
                    /* CÓDIGO DO BOTÃO DE STOP */
                    if (monitor.isEmFuncionamento()) {
                        monitor.setFlagFuncionamento(false);
                        monitor.printWarning("Paragem Sinalizada!", false);
                    } else {
                        if (monitor.getFloorQueue().isEmpty() && monitor.isFloorReached()) {
                            monitor.printWarning("Experimente selecionar um piso primeiro.", true);
                        } else {
                            monitor.setFlagFuncionamento(true);
                            semaforoPortas.release();
                            semaforoBotoneira.release();
                            monitor.acordaTodas();
                            monitor.printWarning("Elevador acionado!", false);
                        }
                    }

                });
        botoesEspeciais.add(botaoStop);
        //a chave é um botao diferente (toggle)
        this.chave = new JToggleButton("K", false);
        chave.setToolTipText("A chave so podera ser acionada quando o "
                + "elevador estiver parado!");
        //este ItemListener vai servir para detetar a alteração do estado do botão
        chave.addItemListener(
                (ItemEvent e) -> {
                    /* CÓDIGO DO BOTÃO TOGGLE DA CHAVE */
                    if (!monitor.isEmFuncionamento()) {
                        monitor.printWarning("Estado_Atual_Chave: ", true);
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            monitor.setEstadoChave(true);
                            monitor.printWarning("[Acionado]", true);
                        } else {
                            monitor.setEstadoChave(false);
                            monitor.printWarning("[Desativado]", true);
                        }
                    } else {
                        displayInput.setText("Elevador a funcionar!\n");
                        chave.setSelected(false);
                    }
                });
        botoesEspeciais.add(chave);

        //mostra os botoes introduzidos
        JPanel inputsPanel = new JPanel();
        JLabel inputsLabel = new JLabel("{DISPLAY}");
        this.displayInput = new JTextArea();
        displayInput.setEditable(false);
        displayInput.setPreferredSize(new Dimension(100, 75));
        JScrollPane scrollPane = new JScrollPane(displayInput);
        inputsPanel.add(inputsLabel);
        inputsPanel.add(scrollPane);

        //botão para sinalizar o fecho o processo
        JButton exit = new JButton("EXIT");
        exit.setToolTipText("Interrompe a thread principal (que interrompe todas "
                + "as threads secundarias) e escreve um Log final.");
        exit.setPreferredSize(new Dimension(60, 100));
        exit.addActionListener(
                (ActionEvent event) -> {
                    displayInput.setText("EXIT");
                    //procura pela thread principal e interrompe-a
                    Thread[] tarray = new Thread[Thread.activeCount()];
                    Thread.enumerate(tarray);
                    int i = 0;
                    while (i < Thread.activeCount() && !tarray[i].getName().equals("[Thread_ControloElevador]")) {
                        i++;
                    }
                    if (i < Thread.activeCount()) {
                        tarray[i].interrupt();
                        monitor.setEndtime(System.currentTimeMillis());
                        monitor.reportGeneration();
                    } else {
                        System.err.println("AVISO: \n"
                                + "Main Thread - [Thread_ControloElevador] "
                                + "- não encontrada!");
                    }
                });

        //adição dos paineis anteriores à frame principal
        guiFrame.add(botoesPisos, BorderLayout.NORTH);
        guiFrame.add(botoesEspeciais, BorderLayout.CENTER);
        guiFrame.add(inputsPanel, BorderLayout.SOUTH);
        guiFrame.add(exit, BorderLayout.EAST);

        guiFrame.pack();
        guiFrame.setVisible(true);
    }
}
