package com.univscheduler;

import com.univscheduler.dao.DatabaseManager;
import com.univscheduler.util.UIUtils;
import com.univscheduler.view.LoginView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public class MainApp {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
            afficherErreurDemarrage("Erreur non geree dans le thread " + thread.getName(), throwable)
        );

        try {
            UIUtils.setLookAndFeel();
            DatabaseManager.getInstance().initialiserBDD();

            SwingUtilities.invokeLater(() -> {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            });
        } catch (Throwable throwable) {
            afficherErreurDemarrage("Le lancement de l'application a echoue.", throwable);
        }
    }

    private static void afficherErreurDemarrage(String contexte, Throwable throwable) {
        throwable.printStackTrace();

        StringBuilder message = new StringBuilder();
        message.append(contexte).append('\n').append('\n');
        message.append(throwable.getClass().getSimpleName());
        if (throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
            message.append(" : ").append(throwable.getMessage());
        }
        message.append('\n').append('\n');
        message.append("Base SQLite : ").append(DatabaseManager.getInstance().getDatabasePath());

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(message);
            return;
        }

        JOptionPane.showMessageDialog(
            null,
            message.toString(),
            "UNIV-SCHEDULER - Erreur",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
