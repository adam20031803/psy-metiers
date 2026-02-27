package org.example.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.io.File;

public class EmailSender {

    // Configuration du serveur SMTP (à modifier selon votre serveur)
    private static final String SMTP_HOST = "smtp.gmail.com"; // Pour Gmail
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "mahdiabderrahmen8@gmail.com"; // Votre email
    private static final String PASSWORD = "aofj mvfr xopv wzbd"; // Votre mot de passe ou mot de passe d'application

    public static boolean sendEmail(String to, String subject, String content) {
        // Configurer les propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Créer une session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            // Envoyer l'email
            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendEmailWithAttachment(String to, String subject, String content, File attachment) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Partie texte
            jakarta.mail.internet.MimeBodyPart textPart = new jakarta.mail.internet.MimeBodyPart();
            textPart.setText(content);

            // Partie pièce jointe
            jakarta.mail.internet.MimeBodyPart attachmentPart = new jakarta.mail.internet.MimeBodyPart();
            attachmentPart.attachFile(attachment);

            // Assemblage
            jakarta.mail.Multipart multipart = new jakarta.mail.internet.MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Version avec template pour email professionnel
    public static boolean sendProfessionalEmail(String to, String coachName, String challengeTitle) {
        String subject = "Invitation à participer au challenge: " + challengeTitle;

        String content = String.format(
                "Bonjour %s,\n\n" +
                        "Nous sommes ravis de vous inviter à participer au challenge '%s' en tant que coach.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Challenge Manager Pro",
                coachName, challengeTitle
        );

        return sendEmail(to, subject, content);
    }
}