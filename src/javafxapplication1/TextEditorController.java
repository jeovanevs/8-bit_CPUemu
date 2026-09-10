/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 * @author Jorge
 */
public class TextEditorController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextArea code_TextArea;

    @FXML
    private Button save_btn;

    @FXML
    private Button insect_btn;

    @FXML
    private Button load_btn;

    @FXML
    private Button help_btn;

    // Recebe o código binário compilado e o devolve ao controlador da CPU.
    private Consumer<List<String>> codeConsumer;

    // Permite que a janela do editor seja conectada à RAM da janela principal.
    public void setCodeConsumer(Consumer<List<String>> codeConsumer) {
        this.codeConsumer = codeConsumer;
    }

    @FXML
    public void drawSavePane(ActionEvent event) {
        Stage st = new Stage();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(st);

        if (file != null) {
            this.saveFile(this.code_TextArea.getText(), file);
        }
    }

    @FXML
    private void showHelp(ActionEvent event) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://littleemu-app.blogspot.com/2020/12/littleemu-instruction-set-instruccion.html"));
        } catch (IOException | URISyntaxException ex) {
            
        }
    }

    private void saveFile(String content, File file) {
        try {
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
        }
    }

    // Abre um arquivo Assembly e coloca o texto no editor para edição ou inspeção.
    @FXML
    private void loadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir código");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos de texto (*.txt)", "*.txt"));
        File file = fileChooser.showOpenDialog(this.code_TextArea.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            this.code_TextArea.setText(content);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao abrir arquivo");
            alert.setHeaderText(null);
            alert.setContentText("Não foi possível ler o arquivo selecionado.");
            alert.showAndWait();
        }
    }

    /**
     * Inspeciona o Assembly e converte cada instrução para 8 bits.
     *
     * Exemplos aceitos:
     *   ADD A,B       -> 10000001
     *   LOAD_A 13D    -> 00101101
     *   HALT          -> 11110000
     *
     * Exemplos rejeitados:
     *   ADD A         -> faltam dois registradores
     *   LOAD_A        -> falta o endereço de memória
     */
    @FXML
    public void inspect(ActionEvent event) {
        // A lista recebe as instruções de máquina geradas para cada linha válida.
        // Por exemplo, ADD A,B gera "1000" + "00" + "01" = "10000001".
        ArrayList<String> code = new ArrayList<>();
        String text = this.format(this.getCode()).toUpperCase();
        ArrayList<String> sourceLines = new ArrayList<>();

        // Normaliza as linhas antes da análise: remove espaços externos,
        // comentários e diretivas que não representam instruções executáveis.
        for (String rawLine : text.split("\\r?\\n")) {
            String line = rawLine.trim();
            int commentIndex = line.indexOf("//");
            if (commentIndex >= 0) {
                line = line.substring(0, commentIndex).trim();
            }
            if (!line.isEmpty() && !line.startsWith("#")) {
                sourceLines.add(line);
            }
        }
        String[] lines = sourceLines.toArray(new String[0]);

        // Converte cada instrução Assembly para o formato binário da CPU.
        for (int i = 0; i < sourceLines.size(); i++) {
            String line = sourceLines.get(i);
            String[] lineParts = line.split("\\s+", 2);
            String opcode = lineParts[0];
            if (opcode.equals("ADD") || opcode.equals("SUB")) {
                if (lineParts.length != 2) {
                    this.throwErrorAlert("A instrução " + opcode + " exige dois registradores.");
                    return;
                }
                if (opcode.equals("ADD")) {
                    code.add(i, "1000");
                } else if (opcode.equals("SUB")) {
                    code.add(i, "1001");
                }
                String[] arguments = lineParts[1].split(",");
                if (arguments.length != 2) {
                    this.throwErrorAlert("A instrução " + opcode + " exige dois registradores na linha: " + line);
                    return;
                }
                for (int j = 0; j < 2; j++) {
                    // Aceita espaços ao redor da vírgula, mas não argumentos vazios.
                    arguments[j] = arguments[j].trim();
                    if (arguments[j].isEmpty()) {
                        this.throwErrorAlert("Registrador ausente na linha: " + line);
                        return;
                    }
                    if (arguments[j].length() == 1) {//Si el primer argumento es la letra de un registro
                        if (!arguments[j].equals("A") && !arguments[j].equals("B") && !arguments[j].equals("C") && !arguments[j].equals("D")) {
                            this.throwErrorAlert("Registro inválido referenciado em: " + lines[i]);
                            return;
                        } else {
                            if (arguments[j].equals("A")) {
                                code.set(i, code.get(i) + "00");
                            } else if (arguments[j].equals("B")) {
                                code.set(i, code.get(i) + "01");
                            } else if (arguments[j].equals("C")) {
                                code.set(i, code.get(i) + "10");
                            } else if (arguments[j].equals("D")) {
                                code.set(i, code.get(i) + "11");
                            }
                        }
                    } else {// Si el primer argumento esta dado en un sistema numerico
                        char ns = arguments[j].charAt(arguments[j].length() - 1);
                        String regSt = arguments[j].split(ns + "")[0];
                        if (ns != 'B' && ns != 'D' && ns != 'H') {
                            this.throwErrorAlert("Sistema numérico desconhecido em: " + lines[i]);
                            return;
                        } else {
                            if (ns == 'B') {
                                if (regSt.length() > 2) {
                                    this.throwErrorAlert("Há apenas 4 registros disponíveis (00, 01, 10, 11) em binário.\nRegistro com mais de dois dígitos referenciado na linha: " + lines[i]);
                                    return;
                                }
                                String toAdd = this.fillZeros(regSt, ns, 2, lines[i]);
                                if (toAdd != null) {
                                    code.set(i, code.get(i) + toAdd);
                                } else {
                                    return;
                                }
                            } else if (ns == 'D') {
                                if (regSt.length() > 1) {
                                    this.throwErrorAlert("Há apenas 4 registros disponíveis (0, 1, 2, 3) em decimal.\nRegistro com mais de um dígito referenciado na linha: " + lines[i]);
                                    return;
                                } else {
                                    if (this.isValidDecimal(regSt)) {
                                        if (Integer.parseInt(regSt) > 3) {
                                            this.throwErrorAlert("Há apenas 4 registros disponíveis (0, 1, 2, 3) em decimal.\nRegistro inexistente referenciado na linha: " + lines[i]);
                                            return;
                                        }
                                    }else{
                                        this.throwErrorAlert("Dígito não decimal encontrado na linha: " + lines[i]);
                                        return;
                                    }
                                }
                                String toAdd = this.fillZeros(regSt, ns, 1, lines[i]);
                                if (toAdd != null) {
                                    code.set(i, code.get(i) + toAdd);
                                } else {
                                    return;
                                }
                            } else if (ns == 'H') {
                                if (regSt.length() > 1) {
                                    this.throwErrorAlert("Há apenas 4 registros disponíveis (0, 1, 2, 3) em hexadecimal.\nRegistro com mais de um dígito referenciado na linha: " + lines[i]);
                                    return;
                                } else {
                                    if (regSt.equals("4") || regSt.equals("5") || regSt.equals("6") || regSt.equals("7") || regSt.equals("8") || regSt.equals("9") || regSt.equals("A") || regSt.equals("B")
                                            || regSt.equals("C") || regSt.equals("D") || regSt.equals("E") || regSt.equals("F")) {
                                        this.throwErrorAlert("Há apenas 4 registros disponíveis (0, 1, 2, 3) em hexadecimal.\nRegistro inexistente referenciado na linha: " + lines[i]);
                                        return;
                                    }
                                    if (Integer.parseInt(regSt) > 3) {
                                        this.throwErrorAlert("Há apenas 4 registros disponíveis (0, 1, 2, 3) em hexadecimal.\nRegistro inexistente referenciado na linha: " + lines[i]);
                                        return;
                                    }
                                }
                                String toAdd = this.fillZeros(regSt, ns, 1, lines[i]);
                                if (toAdd != null) {
                                    code.set(i, code.get(i) + toAdd);
                                } else {
                                    return;
                                }
                            }

                        }
                    }
                }
            } else if (opcode.equals("JUMP") || opcode.equals("JUMP_NEG") || opcode.equals("JUMP_ZRO") || opcode.equals("JUMP_ABV") || opcode.equals("JUMP_OFW")
                    || opcode.equals("LOAD_A") || opcode.equals("LOAD_B") || opcode.equals("LOAD_C") || opcode.equals("LOAD_D")
                    || opcode.equals("STORE_A") || opcode.equals("STORE_B") || opcode.equals("STORE_C") || opcode.equals("STORE_D")) {
                if (lineParts.length != 2) {
                    this.throwErrorAlert("A instrução " + opcode + " exige um endereço de memória.");
                    return;
                }
                // O último caractere identifica a base: B (binária), D (decimal) ou H (hexadecimal).
                String operand = lineParts[1].trim();
                if (operand.length() < 2) {
                    this.throwErrorAlert("Endereço de memória ausente ou incompleto na linha: " + line);
                    return;
                }
                if (opcode.equals("JUMP")) {
                    code.add(i, "1010");
                } else if (opcode.equals("JUMP_NEG")) {
                    code.add(i, "1011");
                } else if (opcode.equals("JUMP_ZRO")) {
                    code.add(i, "1100");
                } else if (opcode.equals("JUMP_ABV")) {
                    code.add(i, "1101");
                } else if (opcode.equals("JUMP_OFW")) {
                    code.add(i, "1110");
                } else if (opcode.equals("LOAD_A")) {
                    code.add(i, "0010");
                } else if (opcode.equals("LOAD_B")) {
                    code.add(i, "0001");
                } else if (opcode.equals("LOAD_C")) {
                    code.add(i, "0011");
                } else if (opcode.equals("LOAD_D")) {
                    code.add(i, "0000");
                } else if (opcode.equals("STORE_A")) {
                    code.add(i, "0100");
                } else if (opcode.equals("STORE_B")) {
                    code.add(i, "0101");
                } else if (opcode.equals("STORE_C")) {
                    code.add(i, "0110");
                } else if (opcode.equals("STORE_D")) {
                    code.add(i, "0111");
                }
                char base = operand.charAt(operand.length() - 1);
                // Separa o valor do endereço do sufixo antes da validação numérica.
                // Exemplo: 13D vira valor "13" e base "D", gerando "1101".
                String dirSt = operand.substring(0, operand.length() - 1);
                if (base == 'B') {
                    if (dirSt.length() > 4) {
                        this.throwErrorAlert("Faixa de memória do LittleEmu: (0000-1111) em binário.\nTentativa de endereçar a memória com mais de 4 dígitos na linha: " + lines[i]);
                        return;
                    }
                    String toAdd = this.fillZeros(dirSt, base, 4, lines[i]);
                    if (toAdd != null) {
                        code.set(i, code.get(i) + toAdd);
                    } else {
                        return;
                    }
                } else if (base == 'D') {
                    if (dirSt.length() > 2) {
                        this.throwErrorAlert("Faixa de memória do LittleEmu: (0-15) em decimal.\nTentativa de endereçar a memória com mais de 2 dígitos na linha: " + lines[1]);
                        return;
                    }else{
                        if(this.isValidDecimal(dirSt)){
                            if(Integer.parseInt(dirSt) > 15){
                                this.throwErrorAlert("Faixa de memória do LittleEmu: (0-15) em decimal.\nTentativa de endereçar a memória fora da faixa na linha: " + lines[i]);
                                return;
                            }
                        }else{
                            this.throwErrorAlert("Dígito não decimal encontrado na linha: " + lines[i]);
                            return;
                        }
                    }
                    String toAdd = this.fillZeros(dirSt, base, 2, lines[i]);
                    if (toAdd != null) {
                        code.set(i, code.get(i) + this.getIntBinaryAsString(Integer.parseInt(dirSt)));
                    } else {
                        return;
                    }
                } else if (base == 'H') {
                    if(dirSt.length() > 1){
                        this.throwErrorAlert("Faixa de memória do LittleEmu: (0-F) em hexadecimal.\nTentativa de endereçar a memória com mais de 1 dígito na linha: " + lines[i]);
                        return;
                    }
                    String toAdd = this.fillZeros(dirSt, base, 1, lines[i]);
                    if (toAdd != null) {
                        code.set(i, code.get(i) + this.getHexBynaryAsString(toAdd));
                    } else {
                        return;
                    }
                }else{
                    this.throwErrorAlert("O sufixo que identifica o formato do endereço foi omitido ou está incorreto.\nLinha: " + lines[i]);
                    return;
                }
            }else if(opcode.equals("HALT")){
                if(lineParts.length > 1){
                    this.throwErrorAlert("Erro de sintaxe na linha: " + line + "\nA instrução HALT não deve receber parâmetros.");
                    return;
                }else{
                    code.add("11110000");
                }
            }else if(opcode.equals("STR_VAR")){
                this.throwErrorAlert("A instrução STR_VAR ainda não é suportada.");
                return;
            }else{
                this.throwErrorAlert("OPCODE desconhecido; a instrução não é válida.\nLinha: " + line);
                return;
            }
        }

        if (code.size() > 16) {
            this.throwErrorAlert("O programa possui mais de 16 instruções e não cabe na RAM.");
            return;
        }

        // Só envia o programa depois que todas as instruções passaram pela inspeção.
        if (this.codeConsumer != null) {
            this.codeConsumer.accept(code);
        }

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Inspeção concluída");
        successAlert.setHeaderText(null);
        successAlert.setContentText("O código passou na inspeção com sucesso.");
        successAlert.showAndWait();
    }
    
    public String getIntBinaryAsString(int i){
        switch (i) {
            case 0:
                return "0000";
            case 1:
                return "0001";
            case 2:
                return "0010";
            case 3:
                return "0011";
            case 4:
                return "0100";
            case 5:
                return "0101";
            case 6:
                return "0110";
            case 7:
                return "0111";
            case 8:
                return "1000";
            case 9:
                return "1001";
            case 10:
                return "1010";
            case 11:
                return "1011";
            case 12:
                return "1100";
            case 13:
                return "1101";
            case 14:
                return "1110";
            case 15:
                return "1111";
            default:
                return null;
        }      
    }
    
    public String getHexBynaryAsString(String hex){
        switch (hex) {
            case "0":
                return "0000";
            case "1":
                return "0001";
            case "2":
                return "0010";
            case "3":
                return "0011";
            case "4":
                return "0100";
            case "5":
                return "0101";
            case "6":
                return "0110";
            case "7":
                return "0111";
            case "8":
                return "1000";
            case "9":
                return "1001";
            case "A":
                return "1010";
            case "B":
                return "1011";
            case "C":
                return "1100";
            case "D":
                return "1101";
            case "E":
                return "1110";
            case "F":
                return "1111";
            default:
                return null;
        }  
    }

    public String fillZeros(String st, char base, int size, String line) {
        String toReturn = "";
        switch (base) {
            case 'B':
                int len = st.length();
                if (this.isValidBinary(st)) {
                    for (int i = 0; i < size - len; i++) {
                        toReturn = toReturn + "0";
                    }
                    toReturn = toReturn + st;
                    return toReturn;
                } else {
                    this.throwErrorAlert("Dígito não binário encontrado na linha: " + line);
                    return null;
                }
            case 'D':
                if (this.isValidDecimal(st)) {
                    if (st.length() == 1) {
                        toReturn = "0";
                    }
                    toReturn = toReturn + st;
                    return toReturn;
                } else {
                    this.throwErrorAlert("Dígito não decimal encontrado na linha: " + line);
                    return null;
                }
            case 'H':
                if (this.isValidHex(st)) {
                    return st;
                } else {
                    this.throwErrorAlert("Dígito não hexadecimal encontrado na linha: " + line);
                    return null;
                }
            default:
                return null;
        }
    }

    public void throwErrorAlert(String error) {
        Alert al = new Alert(Alert.AlertType.ERROR);
        al.setTitle("Erro ao montar o código");
        al.setContentText(error);
        al.showAndWait();
    }

    public boolean isValidBinary(String st) {
        for (int i = 0; i < st.length(); i++) {
            if (st.charAt(i) != '0' && st.charAt(i) != '1') {
                return false;
            }
        }
        return true;
    }

    public boolean isValidDecimal(String st) {
        for (int i = 0; i < st.length(); i++) {
            char c = st.charAt(i);
            if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7' && c != '8' && c != '9') {
                return false;
            }
        }
        return true;
    }

    public boolean isValidHex(String st) {
        for (int i = 0; i < st.length(); i++) {
            char c = st.charAt(i);
            if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7'
                    && c != '8' && c != '9' && c != 'A' && c != 'B' && c != 'C' && c != 'D' && c != 'E' && c != 'F') {
                return false;
            }
        }
        return true;
    }

    public void formatCode(ActionEvent event) {
        this.code_TextArea.setText(this.format((this.code_TextArea.getText()).toUpperCase()));
    }

    public String format(String st) {
        LinkedList<Character> ls = new LinkedList<>();
        for (int i = 0; i < st.length(); i++) {
            ls.add(st.charAt(i));
        }
        for (int i = 0; i < ls.size(); i++) {
            if (ls.get(i) == ' ') {
                try {
                    if (ls.get(i + 1) == '\n') {
                        ls.remove(i);
                        i = i - 1;
                    } else if (ls.get(i - 1) == ',') {
                        ls.remove(i);
                        i = i - 1;
                    }
                } catch (Exception ex) {
                }

            }
        }
        for (int i = 0; i < ls.size(); i++) {
            if (ls.get(i) == ',') {
                try {
                    if (ls.get(i + 1) == ',') {
                        ls.remove(i);
                        i = i - 1;
                    }
                } catch (Exception ex) {
                }

            }
        }
        for (int i = 0; i < ls.size(); i++) {
            if (ls.get(i) == '\n') {
                try {
                    if (ls.get(i + 1) == ' ') {
                        ls.remove(i + 1);
                        i = i - 1;
                    }
                } catch (Exception ex) {
                }
            }
        }
        for (int i = 0; i < ls.size(); i++) {
            if (ls.get(i) == '\n') {
                try {
                    if (ls.get(i + 1) == '\n') {
                        ls.remove(i);
                        i = i - 1;
                    }
                } catch (Exception ex) {
                }
            }
        }
        for (int i = 0; i < ls.size(); i++) {
            if (ls.get(i) == ' ') {
                try {
                    if (ls.get(i + 1) == ' ') {
                        ls.remove(i);
                        i = i - 1;
                    } else if (ls.get(i + 1) == ',') {
                        ls.remove(i);
                        i = i - 1;
                    }
                } catch (Exception ex) {
                }

            }
        }
        String toReturn = "";
        for (Character c : ls) {
            toReturn = toReturn + c;
        }
        return toReturn;
    }

    public String getCode() {
        return this.code_TextArea.getText();
    }

    public int countLines(String st) {
        boolean endText = false;
        int count = 0;
        for (int i = 0; i < st.length(); i++) {
            if (st.charAt(i) == '\n') {
                count = count + 1;
            }
        }
        return count;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
