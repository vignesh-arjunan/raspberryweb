package org.raspi.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmail {

    public static enum MailProvider {

        GOOGLE("smtp.gmail.com"),
        MICROSOFT("smtp.live.com"),
        YAHOO("smtp.mail.yahoo.com");

        private final String smtpHost;

        MailProvider(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        public String getSmtpHost() {
            return smtpHost;
        }

    }

    public static void send(String username, String password, String fromEmail, String toEmail, String subject, String msgText1, File attachement, MailProvider mailProvider) throws MessagingException, IOException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailProvider.getSmtpHost());
//      props.put("mail.smtp.host", "smtp.mail.yahoo.com");
//	props.put("mail.smtp.host", "smtp.live.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

//        try {
        // create a message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail));
        InternetAddress[] address = {new InternetAddress(toEmail)};
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(subject);

        // create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(msgText1);

        // create the Multipart and add its parts to it
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);

        if (attachement != null) {
            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();
            // attach the file to the message
            mbp2.attachFile(attachement);
            mp.addBodyPart(mbp2);
        }

        // add the Multipart to the message
        msg.setContent(mp);

        // set the Date: header
        msg.setSentDate(new Date());

        // send the message
        Transport.send(msg);
        System.out.println("Mail sent");

    }

}
