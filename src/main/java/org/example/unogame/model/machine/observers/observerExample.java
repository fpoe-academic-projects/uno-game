package org.example.unogame.model.machine.observers;

public class observerExample implements observer {
    /**
     * a specific example where an action is triggered when the observer is invoked
     */
    @Override
    public void update(String mensaje) {
        System.out.println("la maquina ha puesto una carta en el tablero" + mensaje);
    }
}
