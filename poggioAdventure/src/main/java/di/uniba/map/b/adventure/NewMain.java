/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package di.uniba.map.b.adventure;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;



/**
 *
 * @author valen
 */
public class NewMain {
    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy_HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Inizio esecuzione: " + dt.format(now));

    }

}
