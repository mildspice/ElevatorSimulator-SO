package modulos;

import enums.EstadosMotor;
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
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import tools.MonitorElevador;

/**
 * <b>Sub-módulo Botoneira - thread que vai simular o conjuto de botões que
 * possibilitam o uso do elevador.</b>
 */
public class Botoneira extends Thread {

    //variáveis da janela
    private JFrame guiFrame;
    private JPanel botoesPisos;
    private JPanel botoesEspeciais;
    private JToggleButton chave;
    private JTextField displayInput;

    protected MonitorElevador monitor;
    protected Semaphore semaforoBotoneira;

    /**
     * Construtor da thread.
     *
     * @param semaforo
     * @param monitor
     */
    public Botoneira(Semaphore semaforo, MonitorElevador monitor) {
        this.semaforoBotoneira = semaforo;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            this.criarJanela();

            /**
             * ainda não sei muito bem o que fazer com este ciclo, mas acho que
             * vai dar jeito. No fundo, vai ser para fazer operações especiais
             * em relação aos inputs ou para impedir o uso de botoes, ...
             */
            while (!Thread.interrupted()) {
                Thread.sleep(100);
                if (monitor.isEmFuncionamento()) {
                    chave.setEnabled(false);
                    chave.setSelected(false);
                } else {
                    chave.setEnabled(true);
                }
            }
        } catch (InterruptedException ex) {
            this.guiFrame.dispose();
        }
    }

    private void criarJanela() {
        /**
         * tutorial:
         * https://www.thoughtco.com/coding-a-simple-graphical-user-interface-2034064
         */
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
         * Criação do painel que vai ter os botoes dos pisos
         */
        botoesPisos = new JPanel();
        botoesPisos.setPreferredSize(new Dimension(100, 100));
        //Criação do layout em grelha 2 por 2* (*4 pisos)
        botoesPisos.setLayout(new GridLayout(2, monitor.NUM_PISOS / 2));
        for (String nomeBotao : monitor.getBotoesPisos()) {
            JButton botao = new JButton(nomeBotao);
            //faz override do método de Ação do botão 
            //(código que o botao vai correr...)
            botao.addActionListener((ActionEvent e) -> {
                if (!monitor.isEmFuncionamento()) {
                    monitor.setInput(nomeBotao);
                    displayInput.setText(nomeBotao);
                } else {
                    displayInput.setText("Elevador em andamento!");
                }
            });
            //adiciona o botao a uma célula da grelha
            botoesPisos.add(botao);
        }

        //painel dos botoes especiais
        botoesEspeciais = new JPanel();
        botoesEspeciais.setLayout(new GridLayout(2, 2));
        botoesEspeciais.setPreferredSize(new Dimension(140, 100));
        //botoes das portas
        for (String nomeBotao : monitor.getBotoesPortas()) {
            JButton botao = new JButton(nomeBotao);
            botao.addActionListener((ActionEvent e) -> {
                monitor.setInput(nomeBotao);
                displayInput.setText(nomeBotao);
            });
            botoesEspeciais.add(botao);
        }
        //botao de stop e chave
        JButton botaoStop = new JButton("S");
        botaoStop.addActionListener((ActionEvent e) -> {
            if (monitor.isEmFuncionamento()) {
                monitor.setFlagFuncionamento(false);
                displayInput.setText("Elevador parado manualmente!");
            } else {
                monitor.setFlagFuncionamento(true);
                monitor.acordaTodas();
                displayInput.setText("Elevador a funcionar.");
            }
        });
        botoesEspeciais.add(botaoStop);
        //a chave é um botao diferente (toggle)
        JToggleButton chave = new JToggleButton("K", false);
        //novamente faz override do método de "listen" do botão
        chave.addItemListener((ItemEvent e) -> {
            if (!monitor.isEmFuncionamento()) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    monitor.setChave(true);
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    monitor.setChave(false);
                }
            } else {
                displayInput.setText("Elevador em andamento!");
            }
        });
        botoesEspeciais.add(chave);

        //mostra os botoes introduzidos
        JPanel inputsPanel = new JPanel();
        JLabel inputsLabel = new JLabel("DISPLAY:");
        displayInput = new JTextField();
        displayInput.setText(monitor.getInput());
        displayInput.setPreferredSize(new Dimension(200, 100));
        inputsPanel.add(inputsLabel);
        inputsPanel.add(displayInput);

        //botão para sinalizar o fecho o processo
        JButton exit = new JButton("EXIT");
        exit.setPreferredSize(new Dimension(60, 100));
        exit.addActionListener((ActionEvent event) -> {
            //monitor.setInput("EXIT");
            Thread[] tarray = new Thread[Thread.activeCount()];
            Thread.enumerate(tarray);
            int i = 0;
            while (!tarray[i].getName().equals("[Thread_ControloElevador]")) {
                i++;
            }
            tarray[i].interrupt();
        });

        //adição dos paineis anteriores à frame principal
        guiFrame.add(botoesPisos, BorderLayout.NORTH);
        guiFrame.add(botoesEspeciais, BorderLayout.CENTER);
        guiFrame.add(inputsPanel, BorderLayout.SOUTH);
        guiFrame.add(exit, BorderLayout.EAST);

        guiFrame.pack();
        guiFrame.setVisible(true);

        /*
            //mostra erros eventuais e outros avisos 
            //(isto secalhar é melhor ficar na janela principal)
            JPanel avisosPanel = new JPanel();
            JLabel avisosLabel = new JLabel("AVISOS: ");
            JTextArea avisos = new JTextArea();
            avisos.setAutoscrolls(true);
            avisos.setPreferredSize(new Dimension(200, 200));
            avisosPanel.add(avisosLabel);
            avisosPanel.add(avisos);
         */
    }
}
