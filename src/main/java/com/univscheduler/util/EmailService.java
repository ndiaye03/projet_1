package com.univscheduler.util;


import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Service pour l'envoi d'emails.
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    /**
     * Laisser vides par defaut pour eviter un echec SMTP bloqueur.
     * Remplir ces valeurs pour activer l'envoi reel.
     */
    private static final String SENDER_EMAIL = "papalyndiaye0@gmail.com";
    private static final String SENDER_PASSWORD = "dgak etuz ekel uyhv";

    public static void envoyerEmail(String destinataire, String sujet, String contenu) {
        if (!configurationSmtpValide()) {
            System.out.println("[SIMULATION EMAIL] Vers : " + destinataire);
            System.out.println("Sujet : " + sujet);
            System.out.println("Contenu : " + contenu);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);
            Transport.send(message);
        } catch (MessagingException e) {
            System.err.println("[EMAIL] Erreur d'envoi : " + e.getMessage());
        }
    }

    private static boolean configurationSmtpValide() {
        return SENDER_EMAIL != null
                && SENDER_PASSWORD != null
                && !SENDER_EMAIL.isBlank()
                && !SENDER_PASSWORD.isBlank()
                && SENDER_EMAIL.contains("@");
    }
}
