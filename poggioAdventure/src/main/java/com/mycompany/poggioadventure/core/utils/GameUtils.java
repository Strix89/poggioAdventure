/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.poggioadventure.core.utils;

import com.mycompany.poggioadventure.model.AdvObject;
import java.util.List;

/**
 *
 * @author pierpaolo
 */
public class GameUtils {

    /**
     *
     * @param inventory
     * @param id
     * @return
     */
    public static AdvObject getObjectFromInventory(List<AdvObject> inventory, int id) {
        for (AdvObject o : inventory) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }

}
