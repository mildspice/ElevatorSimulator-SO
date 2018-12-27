package enums;

/**
 * <h1>Enumeração com as opções para o estado da direção do motor do
 * elevador.</h1>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public enum EstadosMotor {
    //stopped foi adicionado só por conveniência
    CIMA, BAIXO, STOPPED;

    //isto parece uma confusão desgraçada, mas é basicamente um array com
    //Strings "crescentes" que no fim fazem uma setinha (só perco tempo com isto ...).
    private final String[] prettyDown = {
        "     ||   ||\n     ||   ||\n     ||   ||\n\\\\   ||   ||   //\n"
        + " \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "     ||   ||\n     ||   ||\n\\\\   ||   ||   //\n \\\\  ||   ||  //\n"
        + "   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "     ||   ||\n\\\\   ||   ||   //\n \\\\  ||   ||  //\n   \\\\||   ||//\n"
        + "     \\\\   //\n       \\\\//",
        "\\\\   ||   ||   //\n \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n"
        + "       \\\\//",
        " \\\\  ||   ||  //\n   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "   \\\\||   ||//\n     \\\\   //\n       \\\\//",
        "     \\\\   //\n       \\\\//",
        "       \\\\//"};
    private final String[] prettyUp = {
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n"
        + "//   ||   ||   \\\\\n     ||   ||\n     ||   ||\n     ||   ||",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n"
        + "//   ||   ||   \\\\\n     ||   ||\n     ||   ||",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n"
        + "//   ||   ||   \\\\\n     ||   ||",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\\n"
        + "//   ||   ||   \\\\",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\\n //  ||   ||  \\\\",
        "       //\\\\\n     //   \\\\\n   //||   ||\\\\",
        "       //\\\\\n     //   \\\\",
        "       //\\\\"};
    private final String[] prettyStopped = {"            uuuuuuuuuuuuuuuu\n"
        + "        u=!u****************u!=u\n"
        + "      u=!u********************u!=u\n"
        + "  u=!u****************************u!=u\n"
        + "u=!u********************************u!=u\n"
        + "*!************************************!*\n"
        + "*!*=!===!=*===!!===*=!===!=***!!===!=*!*\n"
        + "*!**u!`=*******!!***!!*****!!**!!***!!**!*\n"
        + "*!*****uu!=****!!***!!*****!!**!!===!u*!*\n"
        + "*!**==***!!****!!***u!=***=!u**!!******!*\n"
        + "*!***====,*****==*****====,****==******!*\n"
        + "=u!=**********************************=!u=\n"
        + "  =u!=*****************************=!u=\n"
        + "      =u!=*********************=!u=\n"
        + "        =u!=*****************=!u=\n"
        + "            ================"};

    /**
     * Retorna uma linha única com uma mensagem referente ao estado da
     * enumeração da instância
     *
     * @return string com a mensagem
     */
    public String message() {
        switch (this) {
            case CIMA:
                return "*** |^  Going Up  ^| ***";
            case BAIXO:
                return "*** |v Going Down v| ***";
            case STOPPED:
                return "*** Engine Stopped ***";
            default:
                return "*** Direction Not Yet Taken || Elevator Stopped ***";
        }
    }

    /**
     * Retorna uma string de várias linhas com uma mensagem referente ao estado
     * da enumeração da instância
     *
     * @return string com a mensagem, prettier.
     */
    public String[] prettyDisplay() {
        switch (this) {
            case CIMA:
                return prettyUp;
            case BAIXO:
                return prettyDown;
            case STOPPED:
                return prettyStopped;
            default:
                return null;
        }
    }
}
