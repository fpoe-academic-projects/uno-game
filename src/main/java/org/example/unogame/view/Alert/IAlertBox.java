package org.example.unogame.view.Alert;

public interface IAlertBox {
    // Método para mostrar un mensaje de error
    void showMessage(String title, String header, String content);

    // Método para mostrar un mensaje de confirmación
    void showConfirm(String title, String header, String content);

}
