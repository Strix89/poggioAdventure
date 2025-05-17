package com.mycompany.poggioadventure.model;

/**
 *
 * @author tomma
 */
public class ComponentePC extends AdvObject {
    public enum TipoComponente { CPU, RAM, SCHEDA_MADRE, ALIMENTATORE, PASTA_TERMICA, DISSIPATORE, GPU }
    private final TipoComponente tipo;

    public ComponentePC(int id, String name, String description, TipoComponente tipo) {
        super(id, name, description);
        this.tipo = tipo;
        setPickupable(true);
    }

    public TipoComponente getTipo() {
        return tipo;
    }
}
