package enums;

/**
 * <h1>Enumeração com as opções para o estado das portas do elevador.</h1>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public enum EstadosPortas {
    ABERTO, FECHADO;

    //mais uma coisa completamente desnecessária, agora com a imagem de uma porta ...
    private final String doorClosed
            = "______|=======|______\n"
            + "|___________________|\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|  .--.\n"
            + "|__|=======|=======|__|  |/\\|\n"
            + "|__|=======|=======|__|  |\\/|\n"
            + "|__|=======|=======|__|  '..'\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|\n"
            + "|__|=======|=======|__|\n";
    private final String doorOpen
            = "_______|_______|_______\n"
            + "|_____________________|\n"
            + "|__|=|           |=|__|\n"
            + "|__|=|           |=|__|\n"
            + "|__|=|           |=|__|\n"
            + "|__|=|   _____   |=|__|\n"
            + "|__|=|  /     \\  |=|__|  .''.\n"
            + "|__|=| | .\\ /. | |=|__|  |/\\|\n"
            + "|__|=| |  ___  | |=|__|  |\\/|\n"
            + "|__|=|  \\_____/  |=|__|  '..'\n"
            + "|__|=|     |     |=|__|\n"
            + "|__|=|   / | \\   |=|__|\n"
            + "|__|=|   \\ | /   |=|__|\n"
            + "|__|=|    / \\    |=|__|\n"
            + "|__|=|___|___|___|=|__|\n";

    /**
     * Retorna uma linha única com uma mensagem referente ao estado da
     * enumeração da instância
     *
     * @return string com a mensagem
     */
    public String message() {
        switch (this) {
            case ABERTO:
                return "**** <-  OPEN  -> ****";
            case FECHADO:
                return "**** -> CLOSED <- ****";
            default:
                return null;
        }
    }

    /**
     * Retorna uma string de várias linhas com uma mensagem referente ao estado
     * da enumeração da instância
     *
     * @return string com a mensagem, prettier.
     */
    public String prettyDisplay() {
        switch (this) {
            case ABERTO:
                return doorOpen;
            case FECHADO:
                return doorClosed;
            default:
                return null;
        }
    }
}
