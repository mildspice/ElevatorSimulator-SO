package tools;

import enums.EstadosMotor;
import enums.EstadosPortas;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * <h1>Divisão do módulo Main</h1>
 * <b>- Shared Object</b>
 * <p>
 * <b>Objeto a ser partilhado entre todas as threads que contém todas as
 * variáveis e métodos convenientes.</b>
 * </p>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public class MonitorElevador {

    //tempo de espera predefinido entre as execuções
    public final int MOVEMENT_WAITING_TIME = 1000;
    /* variáveis da janela principal */
    private JTextField displayFloor;
    private JTextArea displayFloorQueue;
    private JTextArea displayFloorPretty;
    private JTextArea displayAvisos;
    private JTextArea displayAvisosLast;
    private JButton workButton;

    /* variáveis gerais do funcionamento do elevador */
    protected Semaphore semaforoExclusaoMutua;
    private boolean flagFloorReached = true;
    private boolean flagFuncionamento = false;
    //valor a ser colocado através do ficheiro "properties"
    private int NUM_PISOS;
    private int pisoAtual = 1;
    //valor a ser colocado através do ficheiro "properties"
    private int CARGA_TOTAL;
    private int cargaAtual = 0;

    /* variáveis relacionadas ao funcionamento da Botoneira */
    //piso = indice do array + 1, ex. botoesPisos[2] = "PISO3"
    private String[] botoesPisos;
    //acabei por colocar as portas como tu (Rodrigo) tinhas inicialmente
    //(como boolean) true - A, false - F.
    private EstadosPortas doorButton;
    private boolean doorButtonClick;
    private boolean chave;
    //esta é a fila para a introdução dos pisos de destino. Para o escalonamento
    //vai ser usado o FIFO em princípio.
    private final ArrayList<String> floorQueue = new ArrayList<>();

    /* variáveis relacionadas ao estado do motor e das portas */
    private EstadosMotor direcaoMotor;
    private EstadosPortas estadoPortas = EstadosPortas.ABERTO;

    /* variáveis do log */
    private int vezesExecutado = 0;
    private int vezesMovimentoPortas = 0;
    private int cargaTotalTransportada = 0;

    /**
     * Construtor do objeto partilhado.
     *
     * @param exclusaoMutua semaforo para o controlo da realização de operações
     * "sensíveis"
     * @throws java.io.IOException sinaliza se o ficheiro de configurações não
     * foi lido
     */
    public MonitorElevador(Semaphore exclusaoMutua) throws IOException {
        setDefinitions();
        this.semaforoExclusaoMutua = exclusaoMutua;
    }

    /**
     * Realiza um notify único para uma thread que esteja "adormecida".
     */
    public synchronized void acorda() {
        this.notify();
    }

    /**
     * "Acorda" todas as threads que estejam "adormecidas" no momento.
     */
    public synchronized void acordaTodas() {
        this.notifyAll();
    }

    /**
     * "Adormece" a thread que chama este método, ficando nesse estado até que
     * outra thread a "acorde"
     *
     * @throws java.lang.InterruptedException exceção resultante da interrupção
     * da thread enquanto "espera" neste "wait"
     */
    public synchronized void espera() throws InterruptedException {
        while (!this.flagFuncionamento) {
            this.wait();
        }
    }

    /**
     * Retorna o número total de pisos existentes
     *
     * @return numero total de pisos
     */
    public int getNumPisos() {
        return NUM_PISOS;
    }

    /**
     * Retorna a identificação dos botões referentes à botoneira do elevador
     *
     * @return array de String com o nome dos botões dos pisos
     */
    public String[] getBotoesPisos() {
        return this.botoesPisos;
    }

    /**
     * Retorna o conjunto de pisos introduzidos pelo utilizador postos em fila.
     *
     * @return array list com a fila de todo os pisos introduzidos
     */
    public ArrayList<String> getFloorQueue() {
        return this.floorQueue;
    }

    /**
     * Guarda o clique feito no botão das portas.
     *
     * @param botao estado referente ao botão das portas
     */
    public synchronized void clickBotaoPortas(EstadosPortas botao) {
        this.doorButton = botao;
        this.doorButtonClick = true;
    }

    /**
     * Retorna a informação do último clique feito no botão das portas.
     *
     * @return instância da enumeração com o estado selecionado
     */
    public synchronized EstadosPortas getBotaoPortas() {
        return this.doorButton;
    }

    /**
     * Diz se o botão das portas foi utilizado.
     *
     * @return boolean referente ao botão das portas
     */
    public synchronized boolean isBotaoPortasClicked() {
        return this.doorButtonClick;
    }

    /**
     * Reseta o estado do clique do botao das portas. (mais um método acoplado,
     * mas conveniente) (esta é a "segunda versão mais fácil" no uso dos botões
     * das portas, ou seja não é necessáriamente eficiente [sendo que a primeira
     * seria alterar o estado da porta diretamente no clique do botão, mas isso
     * seria muito "crude"])
     */
    public synchronized void resetBotaoPortas() {
        this.doorButtonClick = false;
    }

    /**
     * Este método serve para que o estado do JToggleButton relativo à chave que
     * está na botoneira seja "visto" por todas as outras threads. (ver api
     * sobre JToggleButton ItemListener) (Este método só vai ser utilizado pelo
     * botão do JFrame)
     *
     * @param state estado boleano da chave
     */
    public synchronized void setEstadoChave(boolean state) {
        this.chave = state;
    }

    /**
     * Retorna o estado da chave.
     *
     * @return estado boleano da chave
     */
    public synchronized boolean isChaveAcionada() {
        return this.chave;
    }

    /**
     * Altera a direção do motor para se deslocar para cima ou para baixo.
     *
     * @param estado enum constant referente à direção
     */
    public synchronized void setDirecaoMotor(EstadosMotor estado) {
        this.direcaoMotor = estado;
    }

    /**
     * Retorna o estado direcional do motor.
     *
     * @return enum constant sobre o estado atual do motor
     */
    public synchronized EstadosMotor getDirecaoMotor() {
        return this.direcaoMotor;
    }

    /**
     * Retorna o estado atual das portas. Sensível ao estado da chave!
     *
     * @param estado enum constant referente ao estado das portas
     * @return boolean referente ao sucesso do método
     */
    public synchronized boolean setEstadoPortas(EstadosPortas estado) {
        if (chave) {
            printWarning("Chave Acionada!\n"
                    + "Movimento das Portas Impedido.", true);
            return false;
        } else {
            this.estadoPortas = estado;
            return true;
        }
    }

    /**
     * Retorna o estado atual das portas.
     *
     * @return enum constant sobre o estado das portas
     */
    public synchronized EstadosPortas getEstadoPortas() {
        return this.estadoPortas;
    }

    /**
     * Altera a sinalização para se o elevador se encontra no piso em questão
     *
     * @param flag boolean para a sinalização de chagada ao destino
     */
    public synchronized void setFloorReachedFlag(boolean flag) {
        this.flagFloorReached = flag;
    }

    /**
     * Retorna a sinalização sobre se o elevador se encontra no piso em questão
     *
     * @return retorna se o elevador se encontra num piso ou não
     */
    public synchronized boolean isFloorReached() {
        return this.flagFloorReached;
    }

    /**
     * Altera o estado funcional do elevador. Sensível ao estado da chave!
     *
     * @param flag booleano para o andamento do elevador
     * @return boolean referente ao sucesso do método (necessário para a
     * botoneira)
     */
    public synchronized boolean setFlagFuncionamento(boolean flag) {
        if (chave) {
            printWarning("Chave Acionada!\n"
                    + "Deslocacao do elevador impedida.", true);
            return false;
        } else {
            this.flagFuncionamento = flag;
            return true;
        }
    }

    /**
     * Retorna o estado funcional do elevador.
     *
     * @return retorna se o elevador está ou não em funcionamento
     */
    public synchronized boolean isEmFuncionamento() {
        return this.flagFuncionamento;
    }

    /**
     * Atualiza a posição do elevador.
     *
     * @param piso numero do piso onde se encontra o elevador
     */
    public synchronized void setPisoAtual(int piso) {
        if (piso > NUM_PISOS) {
            printWarning("Problemas na organizacao dos pisos !!!!", false);
        } else {
            this.pisoAtual = piso;
        }
    }

    /**
     * Retorna a posição atual do elevador
     *
     * @return numero do piso onde se encontra atualmente o elevador
     */
    public synchronized int getPisoAtual() {
        return this.pisoAtual;
    }

    /**
     * Atualiza o peso que se econtra dentro do elevador atualmente
     *
     * @param carga peso total presente no elevador
     * @return true - peso não excede a carga, false - no contrário
     */
    public synchronized boolean setCargaAtual(int carga) {
        if (carga > CARGA_TOTAL) {
            this.workButton.setBackground(Color.RED);
            return false;
        } else {
            this.cargaAtual = carga;
            this.workButton.setBackground(Color.GREEN);
            return true;
        }
    }

    /**
     * Retorna a posição atual do elevador
     *
     * @return peso total que se encontra atualmente dentro o elevador
     */
    public synchronized int getCargaAtual() {
        return this.cargaAtual;
    }

    /**
     * Este método junto com os métodos "diplayFloor()" e "removeFloorReached()"
     * existem para que o acesso às operações feitas por eles sejam syncronized,
     * evintando problemas com o acesso mútuo à 'floor queue'.
     *
     * @param floor identificação do piso
     */
    public synchronized void addFloorToQueue(String floor) {
        this.floorQueue.add(floor);
    }

    /**
     * Este método junto com os métodos "addFloorToQueue()" e "displayFloor()"
     * existem para que o acesso às operações feitas por eles sejam
     * synchronized, evintando problemas com o acesso mútuo à 'floor queue'.
     */
    public synchronized void removeFloorReached() {
        this.floorQueue.remove(0);
    }

    /**
     * Este método junto com os métodos "addFloorToQueue()" e
     * "removeFloorReached()" existem para que o acesso às operações feitas por
     * eles sejam syncronized, evintando problemas com o acesso mútuo às
     * instâncias com a 'queue' e a janela de impressão.
     */
    public synchronized void displayQueue() {
        if (this.floorQueue.isEmpty()) {
            this.displayFloorQueue.setText("Sem pisos em fila de espera.\n");
        } else {
            int i = 1;
            this.displayFloorQueue.setText("-> [" + this.floorQueue.get(0) + "] ");
            while (i < this.floorQueue.size()) {
                this.displayFloorQueue.append("-> [" + this.floorQueue.get(i) + "] ");
                i++;
            }
        }
    }

    /**
     * Just for the sake of being synchronized e para que as variáveis das
     * janelas também estejam em private.
     */
    public synchronized void displayPisoAtual() {
        String prettyFloorDisplay
                = "_______|" + "==[" + this.getPisoAtual() + "]==" + "|______\n"
                + "|_____________________|\n"
                + "|__|=======|=======|__|\n"
                + "|__|=======|=======|__|\n"
                + "|__|=======|=======|__|\n"
                + " [ ElEvAtOr SiMuLaToR ]";
        this.displayFloor.setText("Piso Atual: " + this.getPisoAtual());
        this.displayFloorPretty.setText(prettyFloorDisplay);
    }

    /**
     * Imprime uma mensagem na janela de avisos e mensagens de erro.
     *
     * @param message string a imprimir.
     * @param append true - print em modo append, false - apaga o texto anterior
     */
    public synchronized void printWarning(String message, boolean append) {
        if (!append) {
            this.displayAvisosLast.setText(this.displayAvisos.getText());
            this.displayAvisos.setText(message
                    + "\n******************************");
        } else {
            this.displayAvisos.append("\n" + message
                    + "\n******************************");
        }
    }

    //------------------------------------------------------------------
    //JSwing e Logs
    /**
     * Cria o JFrame relativo ao módulo MAIN onde serão imprimidas as mensagens
     * mais gerais do funcionamento do elevador e informação relativa aos pisos.
     *
     * @return instância do JFrame
     */
    public JFrame criarJanelaPrincipal() {
        /* FAZER JANELA PRINCIPAL DO ELEVADOR */
        JFrame janela = new JFrame();

        janela.setTitle("[ Display Principal Elevador ]");
        janela.setLocationByPlatform(true);
        //evita o fecho normal da janela. Obriga o uso do botão EXIT
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(1, 3));
        this.displayFloor = new JTextField();
        displayFloor.setEditable(false);
        displayFloor.setPreferredSize(new Dimension(75, 100));
        this.displayFloorPretty = new JTextArea();
        displayFloorPretty.setEditable(false);
        displayFloorPretty.setPreferredSize(new Dimension(175, 100));
        displayFloorPretty.setFont(new Font("monospaced", Font.PLAIN, 12));
        displayPanel.add(displayFloor);
        displayPanel.add(displayFloorPretty);

        JPanel floorPanel = new JPanel();
        floorPanel.setLayout(new GridLayout(1, 3));
        workButton = new JButton("- Carga Atual -");
        workButton.setEnabled(false);
        workButton.setBackground(Color.DARK_GRAY);
        workButton.setForeground(Color.BLACK);
        workButton.setToolTipText("Botão de sinalizacao sobre a carga atual "
                + "dentro do elevador.");
        floorPanel.add(workButton);
        this.displayFloorQueue = new JTextArea();
        displayFloorQueue.setEditable(false);
        displayFloorQueue.setLineWrap(true);
        displayFloorQueue.setWrapStyleWord(true);
        displayFloorQueue.setText("(janela para impressao da fila de espera "
                + "dos pisos inseridos)");
        JScrollPane queueScrollPane = new JScrollPane(displayFloorQueue);
        queueScrollPane.setPreferredSize(new Dimension(200, 150));
        floorPanel.add(displayFloorQueue);
        JButton apagarAvisos = new JButton("Apagar");
        apagarAvisos.setToolTipText("Limpar a area de texto. A janela de novas "
                + "mensagens de aviso ficara vazia.");
        apagarAvisos.setPreferredSize(new Dimension(100, 125));
        apagarAvisos.addActionListener(
                (ActionEvent e) -> {
                    displayAvisosLast.setText(displayAvisos.getText());
                    displayAvisos.setText("");
                });
        floorPanel.add(apagarAvisos);

        JPanel avisosPanel = new JPanel();
        avisosPanel.setLayout(new GridLayout(2, 1));
        this.displayAvisosLast = new JTextArea(10, 30);
        displayAvisosLast.setEditable(false);
        displayAvisosLast.setLineWrap(true);
        displayAvisosLast.setWrapStyleWord(true);
        displayAvisosLast.setFont(new Font("monospaced", Font.PLAIN, 12));
        displayAvisosLast.setText("(janela para guardar avisos anteriores)");
        JScrollPane avisosLastScrollPane = new JScrollPane(displayAvisosLast);
        avisosLastScrollPane.setPreferredSize(new Dimension(200, 150));
        this.displayAvisos = new JTextArea(10, 30);
        displayAvisos.setEditable(false);
        displayAvisos.setText("(janela para impressao de avisos)");
        displayAvisos.setLineWrap(true);
        displayAvisos.setWrapStyleWord(true);
        displayAvisos.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane avisosScrollPane = new JScrollPane(displayAvisos);
        avisosScrollPane.setPreferredSize(new Dimension(200, 150));
        avisosPanel.add(avisosLastScrollPane);
        avisosPanel.add(avisosScrollPane);

        janela.add(displayPanel, BorderLayout.NORTH);
        janela.add(floorPanel, BorderLayout.CENTER);
        janela.add(avisosPanel, BorderLayout.SOUTH);

        janela.pack();
        janela.setVisible(true);

        this.displayPisoAtual();
        return janela;
    }

    /**
     * Escreve para um ficheiro de Log (movimento do elevador).
     * .<p>
     * Este tipos de ficheiros é mesmo do Java e escreverá logo de uma maneira
     * predefinida.</p>
     *
     * @param thread passa para o ficheiro a Thread que criou o log
     * @param pisoInicial passa para o ficheiro o piso inicial do deslocamento
     * @param pisoFinal passa para o ficheiro o destino do deslocamento.
     * @param direcao direcao do deslocamento
     * @param carga peso deslocado
     * @param tempoDescolacao tempo do deslocamento
     */
    public synchronized void writeLog(
            Thread thread, int pisoInicial, int pisoFinal, EstadosMotor direcao,
            int carga, double tempoDescolacao) {
        Logger logger = Logger.getLogger("ElevatorLog");
        FileHandler fh;

        try {
            fh = new FileHandler("ElevatorLog.log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);

            this.semaforoExclusaoMutua.acquire();
            logger.log(Level.INFO, "- Deslocação do Elevador - {0}, "
                    + "Piso Inicial: [{1}], Piso Final: [{2}], Direcao: [{3}], "
                    + "Peso transportado: [{4}], Tempo de trabalho: [{5}]",
                    new Object[]{thread.getName(), pisoInicial, pisoFinal,
                        direcao, carga, tempoDescolacao});
            fh.close();
            this.semaforoExclusaoMutua.release();

        } catch (InterruptedException ex) {
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(MonitorElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Escreve para um ficheiro de Log (paragens).
     * .<p>
     * Este tipos de ficheiros é mesmo do Java e escreverá logo de uma maneira
     * predefinida.</p>
     *
     * @param thread passa para o ficheiro a Thread que criou o log
     * @param timeStopped
     */
    public synchronized void writeLog(Thread thread, double timeStopped) {
        Logger logger = Logger.getLogger("ElevatorLog");
        FileHandler fh;

        try {
            fh = new FileHandler("ElevatorLog.log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);

            this.semaforoExclusaoMutua.acquire();
            logger.log(Level.INFO, "- Paragem do elevador - {0}, "
                    + "Duração da paragem: [{1}]",
                    new Object[]{thread.getName(), timeStopped,});
            fh.close();
            this.semaforoExclusaoMutua.release();

        } catch (InterruptedException ex) {
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(MonitorElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Escreve para um ficheiro de Log (uso das portas).
     * .<p>
     * Este tipos de ficheiros é mesmo do Java e escreverá logo de uma maneira
     * predefinida.</p>
     *
     * @param thread passa para o ficheiro a Thread que criou o log
     * @param portas
     */
    public synchronized void writeLog(
            Thread thread, EstadosPortas portas) {
        Logger logger = Logger.getLogger("ElevatorLog");
        FileHandler fh;

        try {
            fh = new FileHandler("ElevatorLog.log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);

            this.semaforoExclusaoMutua.acquire();
            logger.log(Level.INFO, "- Utilização das Portas - {0}, "
                    + "Estado das Portas: [{1}]",
                    new Object[]{thread.getName(), portas.toString()});
            fh.close();
            this.semaforoExclusaoMutua.release();

        } catch (InterruptedException ex) {
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(MonitorElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Incrementa na varíavel o número de vezes que o elevador foi executado
     */
    public void counterExecucao() {
        vezesExecutado++;
    }

    /**
     * Incrementa na varíavel o número de movimentos das portas
     */
    public void counterPortas() {
        vezesMovimentoPortas++;
    }

    /**
     * Incrementa o peso total que vai sendo utilizado no elevador.
     */
    public void counterPesoTotal() {
        cargaTotalTransportada = cargaTotalTransportada + cargaAtual;
    }

    /**
     * Cria um relatório do funcionamento geral quando se fecha o programa.
     * (aberto a mais ideias para informação a adicionar aos logs)
     *
     * @param execTime tempo total de execução do programa
     */
    public void reportGeneration(double execTime) {

        Logger logger = Logger.getLogger("GeneralExecutionLog");
        FileHandler fh;

        try {

            fh = new FileHandler("GeneralExecutionLog.log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);

            logger.log(Level.CONFIG, "Numero de Pisos: {0} Carga Maxima: ",
                    new Object[]{NUM_PISOS, CARGA_TOTAL});
            logger.log(Level.INFO, "Numero de vezes executado: [{0}], Total de peso transportado: "
                    + "[{1}], Total Movimentos das Portas [{2}], Tempo total de trabalho: [{3}]",
                    new Object[]{vezesExecutado, cargaTotalTransportada,
                        vezesMovimentoPortas, execTime});
            fh.close();

        } catch (IOException | SecurityException ex) {
            Logger.getLogger(MonitorElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Lê o ficheiro de configurações, procura pelas propriedades definidas,
     * nomeadamente a carga e o número de pisos, e coloca nas variáveis.
     *
     * @throws java.io.IOException exceção resultante da inexistência do
     * ficheiro dentro do diretório "root" (neste caso /src/). (NOTA: exceção
     * vai ser tratada imediatamente antes da criação da instância)
     */
    private void setDefinitions() throws IOException {

        File deffile = new File("definicoes.properties");

        Properties p = new Properties();
        p.load(new FileInputStream(deffile));

        NUM_PISOS = Integer.parseInt(p.getProperty("pisos"));
        botoesPisos = new String[NUM_PISOS];
        for (int i = 0; i < this.botoesPisos.length; i++) {
            this.botoesPisos[i] = "PISO" + (i + 1);
        }
        CARGA_TOTAL = Integer.parseInt(p.getProperty("carga"));
    }
}
